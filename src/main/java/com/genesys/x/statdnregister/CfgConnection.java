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

    public IConfService getConfigService() { return this.configService; }

    @Inject
    public CfgConnection(IStatDNConfiguration config, ChannelListener listener) throws InterruptedException, URISyntaxException {
        System.out.println("Creating CfgConnection");
        URI uri = new URI(config.getConfigServerUrl());
        Endpoint ep = new Endpoint(uri);
        configServer = new ConfServerProtocol(ep, false);
        configServer.setClientName(config.getClientApplication());
        String unpw[] = uri.getUserInfo().split(":");
        configServer.setUserName(unpw[0]);
        configServer.setUserPassword(unpw[1]);
        this.configService = ConfServiceFactory.createConfService(configServer);

        configServer.addChannelListener(this);
        if (listener != null) {
            configServer.addChannelListener(listener);
        }
        //configServer.setMessageHandler(this);
    }

    public void close(){
        try {
            if (configServer.getState() != ChannelState.Closed) {
                configServer.close();
            }
        } catch (Exception ex){}
    }

    public void open() throws ProtocolException, InterruptedException {
        open(true);
    }

    public void open(boolean async) throws ProtocolException, InterruptedException {
        if (async){
            configServer.beginOpen();
        } else{
            openMutex = new Semaphore(0);
            configServer.beginOpen();
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
