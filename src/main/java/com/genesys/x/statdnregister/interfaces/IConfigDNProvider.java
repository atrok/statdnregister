package com.genesys.x.statdnregister.interfaces;

import com.genesyslab.platform.applicationblocks.com.objects.CfgDN;
import com.genesyslab.platform.applicationblocks.com.queries.CfgDNQuery;

import java.util.Collection;

/**
 * Created by dburdick on 12/2/2015.
 */
public interface IConfigDNProvider {
    public CfgDN getDN(String name);
    public CfgDN getDN(int dbid);
    public CfgDN getDN(CfgDNQuery query);
    public Collection<CfgDN> getDNs(CfgDNQuery query);
}
