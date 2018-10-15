package com.genesys.x.statdnregister;

import com.genesys.x.statdnregister.interfaces.IConfigDNProvider;
import com.genesys.x.statdnregister.interfaces.IConfigObjectProvider;
import com.genesyslab.platform.applicationblocks.com.objects.CfgDN;
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNQuery;
import com.google.inject.Inject;

import java.util.Collection;

/**
 * Created by dburdick on 12/3/2015.
 */
public class ConfigDNProvider implements IConfigDNProvider {
    @Inject
    private IConfigObjectProvider configObjectProvider;

    @Override
    public CfgDN getDN(String name) {
        return configObjectProvider.getObject(CfgDN.class, name);
    }

    @Override
    public CfgDN getDN(int dbid) {
        return configObjectProvider.getObject(CfgDN.class, dbid);
    }

    @Override
    public CfgDN getDN(CfgDNQuery query) {
        return configObjectProvider.getObject(CfgDN.class, query);
    }

    @Override
    public Collection<CfgDN> getDNs(CfgDNQuery query) {
        return configObjectProvider.getObjects(CfgDN.class, query);
    }
}
