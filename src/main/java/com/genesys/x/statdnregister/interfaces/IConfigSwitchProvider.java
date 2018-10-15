package com.genesys.x.statdnregister.interfaces;

import com.genesyslab.platform.applicationblocks.com.objects.CfgSwitch;
import com.genesyslab.platform.applicationblocks.com.queries.CfgSwitchQuery;

import java.util.Collection;

/**
 * Created by dburdick on 12/2/2015.
 */
public interface IConfigSwitchProvider {
    public CfgSwitch getSwitch(String name);
    public CfgSwitch getSwitch(int dbid);
    public CfgSwitch getSwitch(CfgSwitchQuery query);
    public Collection<CfgSwitch> getSwitches(CfgSwitchQuery query);
}
