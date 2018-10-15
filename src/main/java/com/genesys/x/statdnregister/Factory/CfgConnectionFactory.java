package com.genesys.x.statdnregister.Factory;

import com.genesys.x.statdnregister.CfgConnection;
import com.genesys.x.statdnregister.interfaces.IStatDNConfiguration;
import com.genesyslab.platform.commons.protocol.ChannelListener;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.net.URISyntaxException;

/**
 * Created by dburdick on 12/3/2015.
 */
@Singleton
public class CfgConnectionFactory implements Provider<CfgConnection> {
    @Inject
    private IStatDNConfiguration config;
    @Inject
    private ChannelListener listener;

    private CfgConnection configServer;

    public CfgConnection get(){
        try {
            if (configServer == null){
                configServer = new CfgConnection(config, listener);
            }
            return configServer;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
