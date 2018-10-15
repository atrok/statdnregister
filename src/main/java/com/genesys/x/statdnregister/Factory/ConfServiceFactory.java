package com.genesys.x.statdnregister.Factory;

import com.genesys.x.statdnregister.CfgConnection;
import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Created by dburdick on 12/3/2015.
 */
public class ConfServiceFactory implements Provider<IConfService> {
    private CfgConnection configServer;

    @Inject
    public ConfServiceFactory(CfgConnection configServer){
        this.configServer = configServer;
    }
    @Override
    public IConfService get() {
        return configServer.getConfigService();
    }
}
