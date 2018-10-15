package com.genesys.x.statdnregister;

import com.genesys.x.statdnregister.interfaces.IConfigObjectProvider;
import com.genesys.x.statdnregister.interfaces.IConfigSwitchProvider;
import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch;
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery;
import com.google.inject.Inject;

import java.util.Collection;

/**
 * Created by dburdick on 12/3/2015.
 */
public class ConfigSwitchProvider implements IConfigSwitchProvider {
    @Inject
    private IConfigObjectProvider configObjectProvider;

    @Override
    public CfgSwitch getSwitch(String name) {
        return configObjectProvider.getObject(CfgSwitch.class, name);
    }

    @Override
    public CfgSwitch getSwitch(int dbid) {
        return configObjectProvider.getObject(CfgSwitch.class, dbid);
    }

    @Override
    public CfgSwitch getSwitch(CfgSwitchQuery query) {
        return configObjectProvider.getObject(CfgSwitch.class, query);
    }

    @Override
    public Collection<CfgSwitch> getSwitches(CfgSwitchQuery query) {
        return configObjectProvider.getObjects(CfgSwitch.class, query);
    }
}
