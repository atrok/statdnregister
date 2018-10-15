package com.genesys.x.statdnregister;

import com.genesys.x.statdnregister.Factory.CfgConnectionFactory;
import com.genesys.x.statdnregister.Factory.ConfServiceFactory;
import com.genesys.x.statdnregister.interfaces.*;
import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.commons.protocol.ChannelListener;
import com.google.inject.AbstractModule;

import java.io.IOException;

/**
 * Created by dburdick on 12/2/2015.
 */
public class StatModule extends AbstractModule {
    @Override
    protected void configure() {
        try {
            IStatDNConfiguration config = StatDNConfiguration.Create();
            bind(IStatDNConfiguration.class).toInstance(config);
            bind(ChannelListener.class).to(StatCfgListener.class);
            bind(CfgConnection.class).toProvider(CfgConnectionFactory.class);
            bind(IConfService.class).toProvider(ConfServiceFactory.class);
            bind(IConfigAppProvider.class).to(ConfigAppProvider.class);
            bind(IConfigDNProvider.class).to(ConfigDNProvider.class);
            bind(IConfigObjectProvider.class).to(ConfigObjectProvider.class);
            bind(IConfigSwitchProvider.class).to(ConfigSwitchProvider.class);

            //bind(CfgConnection.class).to(CfgConnection.class);
        } catch (IOException iox){
            System.out.println(iox.toString());
        }
    }
}
