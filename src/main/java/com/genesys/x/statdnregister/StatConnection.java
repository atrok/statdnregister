package com.genesys.x.statdnregister;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesys.x.statdnregister.interfaces.IConfigAppProvider;
import com.genesys.x.statdnregister.interfaces.IStatDNConfiguration;
import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication;
import com.genesyslab.platform.applicationblocks.com.objects.CfgPortInfo;
import com.genesyslab.platform.commons.protocol.*;
import com.genesyslab.platform.reporting.protocol.StatServerProtocol;
import com.genesyslab.platform.reporting.protocol.statserver.*;
import com.genesyslab.platform.reporting.protocol.statserver.events.EventInfo;
import com.genesyslab.platform.reporting.protocol.statserver.events.EventStatisticClosed;
import com.genesyslab.platform.reporting.protocol.statserver.events.EventStatisticOpened;
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestCloseStatistic;
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestOpenStatistic;
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestOpenStatisticEx;
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestPeekStatistic;
import com.google.inject.Inject;

import java.util.*;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by dburdick on 12/3/2015.
 */
public class StatConnection implements ChannelListener, MessageHandler {
    @Inject
    private IConfigAppProvider appProvider;

    @Inject
    private IStatDNConfiguration config;

    @Inject
    ChannelListener channelListener;

    private static final Logger logger = LogManager.getLogger();
    
    private StatServerProtocol statServer;
    
    private RegistrationResult collector=new RegistrationResult();
    private ResultHandler resulthandler;
    
    public StatConnection() {
        statServer = new StatServerProtocol();
//        statServer.addChannelListener(this);
        statServer.addChannelListener(channelListener);
        statServer.setMessageHandler(this);
        logger.info("Creating Stat Connection");
    }
    
    public void setSetMessageHandler(MessageHandler mh){
    	statServer.setMessageHandler(mh);
    }

    public void open(){
        /*new Thread(new Runnable() {
            @Override
            public void run() {*/
        try {
            logger.info("Running");
            CfgApplication app = appProvider.getApplication(config.getStatServerName());
            int port = 0;

            if (app.getPortInfos() != null && app.getPortInfos().size() > 0) {
                for (CfgPortInfo pi : app.getPortInfos()) {
                    logger.printf(Level.INFO,"ID: %s Port: %s\n", pi.getId(), pi.getPort());
                    if (pi.getId() == "" || pi.getId() == "default") {
                    	logger.printf(Level.INFO,"getPort is %s\n",pi.getPort());
                        port = Integer.valueOf(pi.getPort());
                        break;
                    }
                }
            }

            if (port == 0) {
                logger.info("Getting port {}",app.getServerInfo().getPort());
                port = Integer.valueOf(app.getServerInfo().getPort());
            }
            Endpoint endpoint = new Endpoint(app.getServerInfo().getHost().getName(),
                    port);

            statServer.setEndpoint(endpoint);
            try {
                statServer.open();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
            /*}
        }).start();*/
    }

    public void close() {
        if (statServer.getState() != ChannelState.Closed) {
            try {
                statServer.close();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onChannelOpened(EventObject eventObject) {
        logger.trace("stat opened");
    }

    @Override
    public void onChannelClosed(ChannelClosedEvent channelClosedEvent) {
    	logger.trace("stat closed");
        
    }


    @Override
    public void onChannelError(ChannelErrorEvent channelErrorEvent) {
        logger.error("stat error");
    }

	private HashMap<String, HashMap> results=new HashMap<String,HashMap>();

    @Override
    public void onMessage(Message message){
    	
    	//System.out.println(message.toString());
    	
    	EventInfo ev=(EventInfo)message;
    	
    	if(ev != null){
    		collector.add(ev);
    	}
    	
    	if(resulthandler.getTreshold()>0&&resulthandler.getTreshold()==collector.processed()){
    		resulthandler.onResult(collector);
    	}
    	
    	
    }

    private HashMap<Integer, RequestOpenStatisticEx> statIDList = new HashMap<Integer, RequestOpenStatisticEx>();

    public EventInfo peekStatistic(int id) {

       /*
        *  if (!statIDList.containsKey(id)) {
        *
            return null;
        }

    */
        //System.out.format("Requesting statistic %d\n", id);
        RequestPeekStatistic peek = RequestPeekStatistic.create(id);
        try {
            //Message msg = statServer.request(peek, 1);
        	Message msg = null;
        	statServer.send(peek);
        	
            if (msg instanceof EventInfo) {
                return (EventInfo) msg;
            } else {
                if (msg == null){
                    //System.out.format("RequestPeekStatistic %d is null\n", id);
                }else {
                	logger.printf(Level.INFO,"Invalid result for %d. %s", id, msg.toString());
            }
            }

        } catch (ProtocolException e) {
        	logger.error(e);
        }

        return null;
    }

    public EventStatisticOpened openStatistic(String tenant, String objectId, StatisticObjectType objectType, String statType) {
        RequestOpenStatisticEx open;
        StatisticObject statObject = StatisticObject.create();
        statObject.setObjectId(objectId);
        statObject.setObjectType(objectType);
        statObject.setTenantName(tenant);

        @SuppressWarnings("deprecation")
		DnActionMask mainMask = ActionsMask.createDNActionsMask();
        //mainMask.setBit(DnActions.Monitored);
        mainMask.setBit(DnActions.Monitored);
       // DnActionMask relMask = ActionsMask.createDNActionsMask();
        DnActionMask relMask = ActionsMask.createDNActionsMask();

        StatisticMetricEx metric = StatisticMetricEx.create();
        //metric.setStatisticType(statType);
        metric.setCategory(StatisticCategory.CurrentNumber);
        metric.setMainMask(mainMask);
        metric.setRelativeMask(relMask);
        metric.setSubject(StatisticSubject.DNAction);
        
        Notification notification = Notification.create();
        notification.setMode(NotificationMode.NoNotification);

        //open = RequestOpenStatistic.create(statObject, metric, notification);
        open = RequestOpenStatisticEx.create();
        
        open.setNotification(notification);
        open.setStatisticObject(statObject);
        open.setStatisticMetricEx(metric);
        
        try {
            Message msg = statServer.request(open);
            if (msg instanceof EventStatisticOpened) {
            	collector.addRequest(open);
            	logger.printf(Level.DEBUG,"statIDList added: %d\n", ((EventStatisticOpened) msg).getReferenceId());
                return (EventStatisticOpened) msg;
            } else {
                logger.error("UNexpected:\n"+msg.toString());
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void closeStatistic(int id) {
        if (statIDList.containsKey(id)) {
            statIDList.remove(id);
            RequestCloseStatistic close = RequestCloseStatistic.create(id);
            try {
                Message msg = statServer.request(close);
                if (msg instanceof EventStatisticClosed) {

                } else {
                    logger.error(msg.toString());
                }
            } catch (ProtocolException e) {
            	logger.error(e);
            }
        }
    }

	public void setResultHandler(ResultHandler handler) {
		// TODO Auto-generated method stub
		this.resulthandler=handler;
	}
    
    

}
