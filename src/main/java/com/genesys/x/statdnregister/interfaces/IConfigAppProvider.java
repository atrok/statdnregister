package com.genesys.x.statdnregister.interfaces;

import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication;
import com.genesyslab.platform.applicationblocks.com.queries.CfgApplicationQuery;

import java.util.Collection;

/**
 * Created by dburdick on 12/2/2015.
 */
public interface IConfigAppProvider {
    public CfgApplication getApplication(String name);
    public CfgApplication getApplication(int dbid);
    public CfgApplication getApplication(CfgApplicationQuery query);
    public Collection<CfgApplication> getApplications(CfgApplicationQuery query);
}
