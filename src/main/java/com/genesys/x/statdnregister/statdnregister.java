package com.genesys.x.statdnregister;

import com.genesys.x.statdnregister.interfaces.IConfigAppProvider;
import com.genesys.x.statdnregister.interfaces.IConfigDNProvider;
import com.genesys.x.statdnregister.interfaces.IConfigSwitchProvider;
import com.genesys.x.statdnregister.interfaces.IStatDNConfiguration;
import com.genesyslab.platform.applicationblocks.com.ConfService;
import com.genesyslab.platform.applicationblocks.com.objects.*;
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNQuery;
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery;
import com.genesyslab.platform.commons.log.ILogger;
import com.genesyslab.platform.commons.log.Log;
import com.genesyslab.platform.commons.log.Log4J2LoggerFactoryImpl;
import com.genesyslab.platform.commons.protocol.Message;
import com.genesyslab.platform.commons.protocol.MessageHandler;
import com.genesyslab.platform.commons.protocol.ProtocolException;
import com.genesyslab.platform.configuration.protocol.types.CfgAppType;
import com.genesyslab.platform.configuration.protocol.types.CfgDNType;
import com.genesyslab.platform.reporting.protocol.statserver.DnActions;
import com.genesyslab.platform.reporting.protocol.statserver.DnStatus;
import com.genesyslab.platform.reporting.protocol.statserver.IStatisticStatus;
import com.genesyslab.platform.reporting.protocol.statserver.StatisticObject;
import com.genesyslab.platform.reporting.protocol.statserver.StatisticObjectType;
import com.genesyslab.platform.reporting.protocol.statserver.events.EventInfo;
import com.genesyslab.platform.reporting.protocol.statserver.events.EventStatisticOpened;
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestOpenStatisticEx;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public class statdnregister {
	private static final Logger logger = LogManager.getLogger();
	
	private static ILogger psdklogger;
	
	private static class DNResultHandler implements ResultHandler{

		int threshold=0;

		@Override
		public void onResult(RegistrationResult res) {
			// TODO Auto-generated method stub
			res.printJSON();
			res.empty();
		}

		@Override
		public void setTreshold(int i) {
			// TODO Auto-generated method stub
			this.threshold=i;
			
		}

		@Override
		public int getTreshold() {
			// TODO Auto-generated method stub
			return this.threshold;
		}
		
	}
	

	
	public static void main(String[] args) throws InterruptedException, ProtocolException {
		
		initLogger();
        psdklogger=Log.getLogger(ConfService.class);
        psdklogger.info("logging from psdklogger");
		
        Injector injector = Guice.createInjector(new StatModule());

        IStatDNConfiguration config = injector.getInstance(IStatDNConfiguration.class);

        CfgConnection cfgserver = injector.getInstance(CfgConnection.class);
        cfgserver.open(false);

        StatConnection statServer = injector.getInstance(StatConnection.class);
        statServer.open();
        
        DNResultHandler msghandler=getMessageHandler();
        
        statServer.setResultHandler(msghandler);


        ConfigObjectProvider searcher = injector.getInstance(ConfigObjectProvider.class);

        IConfigAppProvider appProvider = injector.getInstance(IConfigAppProvider.class);
        if (appProvider == null){
        	logger.error("appProvider is null");
        }
        IConfigSwitchProvider switchProvider = injector.getInstance(IConfigSwitchProvider.class);
        IConfigDNProvider dnProvider = injector.getInstance(IConfigDNProvider.class);

        try {
            CfgApplication app = appProvider.getApplication(config.getStatServerName());
            long start= System.currentTimeMillis();
            for (CfgConnInfo con: app.getAppServers()) {
                CfgApplication server = con.getAppServer();
                if (server.getType() == CfgAppType.CFGTServer) {
                    //System.out.println(server.getName());
                    CfgSwitchQuery q = new CfgSwitchQuery();
                    q.setTserverDbid(server.getDBID());
                    CfgSwitch s = switchProvider.getSwitch(q);
                    //System.out.println(s.getName());

                    CfgDNQuery dnq = new CfgDNQuery();
                    System.out.println(s.getDBID());
                    dnq.setSwitchDbid(s.getDBID());
                    Collection<CfgDN> dnlist = dnProvider.getDNs(dnq);
                    logger.printf(Level.INFO,"StatServer=%s, TServer=%s, Switch=%s, count=%d\n",app.getName(), server.getName(), s.getName(),dnlist.size());

                    for (CfgDN d: dnlist){
                        if (d.getType() == CfgDNType.CFGExtension){
                        	msghandler.setTreshold(msghandler.getTreshold()+1);
                        } else if (d.getType() == CfgDNType.CFGVirtACDQueue){
                        	msghandler.setTreshold(msghandler.getTreshold()+1);
                        }else if (d.getType() == CfgDNType.CFGRoutingQueue){
                        	msghandler.setTreshold(msghandler.getTreshold()+1);
                        }

                    }
                    
                    
                    for (CfgDN d: dnlist){
                        EventStatisticOpened opened = null;
                        String name = String.format("%s@%s", d.getNumber(), s.getName());
                        String tenant = d.getTenant().getName();
                        
                        if (d.getType() == CfgDNType.CFGExtension){
                            opened = statServer.openStatistic(tenant, name,
                                    StatisticObjectType.RegularDN, config.getExtensionStatistic());
                        } else if (d.getType() == CfgDNType.CFGVirtACDQueue){
                            opened = statServer.openStatistic(tenant, name,
                                    StatisticObjectType.Queue, config.getVirtualQueueStatistic());
                        }else if (d.getType() == CfgDNType.CFGRoutingQueue){
                            opened = statServer.openStatistic(tenant, name,
                                    StatisticObjectType.RoutePoint, config.getVirtualQueueStatistic());
                        }

                        if (opened != null){
                            //System.out.format("Registered %s with id %d\n", name, opened.getReferenceId());
                            statServer.peekStatistic(opened.getReferenceId());
                                                        
                        }
                    }
                }
            }
            logger.printf(Level.INFO,"Time elapsed: %d sec\n", (System.currentTimeMillis()-start)/1000);
        } catch (Exception ex){
            ex.printStackTrace();
        }

        /*
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        
        Thread.sleep(2000);
        cfgserver.close();
        statServer.close();

    }

	private static DNResultHandler getMessageHandler() {
		// TODO Auto-generated method stub
		return new DNResultHandler();
	}
	
	/**
	 * Enable PSDK logging by setting Logger factory and providing path to
	 * log4j2 configuration file. Alternately it is possible to enable logging
	 * with system properties:
	 * -Dcom.genesyslab.platform.commons.log.loggerFactory=com.genesyslab.platform.commons.log.Log4J2LoggerFactoryImpl
	 * -Dlog4j.configurationFile=path/to/log4j2.xml.
	 * 
	 *  See http://docs.genesys.com/Documentation/PSDK/latest/Developer/SettingUpLogging
	 */
	private static void initLogger() {
		ConfigurationSource source;
		InputStream is = null;
		try {			
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("log4j2.xml");	
			if(is==null) {
				logger.error("Can't initialize log with \'log4j2.xml\'. Check if file available in resources.");
			}
			else {
				source = new ConfigurationSource(is);			
				Configurator.initialize(null, source);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		Log.setLoggerFactory(new Log4J2LoggerFactoryImpl());
	}
}