package com.genesys.x.statdnregister;

import com.genesys.x.statdnregister.interfaces.IStatDNConfiguration;
import com.genesyslab.platform.applicationblocks.com.ConfServiceFactory;
import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.commons.connection.ConnectionClosedEvent;
import com.genesyslab.platform.commons.protocol.*;
import com.genesyslab.platform.configuration.protocol.ConfServerProtocol;
import com.genesyslab.platform.standby.WarmStandby;
import com.google.inject.Exposed;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.concurrent.Semaphore;

/**
 * Created by dburdick on 12/2/2015.
 */
@Singleton
public class CfgConnection implements ChannelListener, MessageHandler {
    private ConfServerProtocol configServer = null;
    private IConfService configService = null;
    private Thread receiver = null;
    private Semaphore openMutex;
    private WarmStandby ws;

    public IConfService getConfigService() { return this.configService; }

    @Inject
    public CfgConnection(IStatDNConfiguration config, ChannelListener listener) throws InterruptedException, URISyntaxException {
        System.out.println("Creating CfgConnection");
        String[] uri = config.getConfigServerUrl();
        //Endpoint ep = new Endpoint(uri);
        configServer=new ConfServerProtocol();
        configServer.setClientName(config.getClientApplication());

        Endpoint[] ep=initWarmStandby(config.getConfigServerUrl());
        ws = new WarmStandby(configServer, ep);
        
        this.configService = ConfServiceFactory.createConfService(configServer);

        configServer.addChannelListener(this);
        if (listener != null) {
            configServer.addChannelListener(listener);
        }
        //configServer.setMessageHandler(this);
    }

    private Endpoint[] initWarmStandby(String[] urls) throws URISyntaxException{
    	
    	Endpoint[] ep= new Endpoint[urls.length];
    	int i=0;
    	String unpw[]=null;
    	for (String u: urls){
    		URI uri = new URI(u);
    		ep[i]=new Endpoint(uri);
    		i++;
    		if(unpw==null)
    			unpw = uri.getUserInfo().split(":");
    		
    	}
        configServer.setUserName(unpw[0]);
        configServer.setUserPassword(unpw[1]);
    	return ep;
    	
    }
    
    
    public void close(){
        try {
            if (configServer.getState() != ChannelState.Closed) {
                //configServer.close();
            	ws.close();
            }
        } catch (Exception ex){}
    }

    public void open() throws ProtocolException, InterruptedException {
        open(true);
    }

    public void open(boolean async) throws ProtocolException, InterruptedException {
        if (async){
            //configServer.beginOpen();
        	ws.openAsync();
        } else{
            openMutex = new Semaphore(0);
            //configServer.beginOpen();
            ws.openAsync();
            openMutex.acquire();
            openMutex = null;
        }
    }

    public boolean isOpen(){
        return configServer != null && configServer.getState() == ChannelState.Opened;
    }

    @Override
    public void onChannelOpened(EventObject eventObject) {
/*        this.receiver = new Thread(new Runnable(){
           @Override
            public void run(){
               try {
                   while (configServer.getState() == ChannelState.Opened) {
                       configServer.receive(1000);
                   }
               } catch (InterruptedException ix){

               }
           }
        });
        this.receiver.start();*/

        if (openMutex != null)
            openMutex.release();
    }

    @Override
    public void onChannelClosed(ChannelClosedEvent channelClosedEvent) {
        if (this.receiver != null)
            this.receiver.interrupt();

        this.receiver = null;
    }

    @Override
    public void onChannelError(ChannelErrorEvent channelErrorEvent) {
    }

    @Override
    public void onMessage(Message message) {

    }
}
