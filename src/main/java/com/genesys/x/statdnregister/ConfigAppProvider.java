package com.genesys.x.statdnregister;

import com.genesys.x.statdnregister.interfaces.IConfigAppProvider;
import com.genesys.x.statdnregister.interfaces.IConfigObjectProvider;
import com.genesyslab.platform.applicationblocks.com.ConfigException;
import com.genesyslab.platform.applicationblocks.com.ICfgObject;
import com.genesyslab.platform.applicationblocks.com.ICfgQuery;
import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.applicationblocks.com.objects.CfgApplication;
import com.genesyslab.platform.applicationblocks.com.queries.CfgApplicationQuery;
import com.google.inject.Inject;

import java.util.Collection;

/**
 * Created by dburdick on 12/3/2015.
 */
public class ConfigAppProvider implements IConfigAppProvider {
    @Inject
    private IConfigObjectProvider configObjectProvider;
    @Inject
    private IConfService configService;

    @Override
    public CfgApplication getApplication(String name) {
        CfgApplicationQuery query = new CfgApplicationQuery();
        query.setName(name);
        try {
            return configService.retrieveObject(query);
        } catch (ConfigException e) {
            e.printStackTrace();
        }
        return (CfgApplication) configObjectProvider.getObject(CfgApplication.class, query);
    }

    @Override
    public CfgApplication getApplication(int dbid) {
        return (CfgApplication) configObjectProvider.getObject(CfgApplication.class, dbid);
    }

    @Override
    public CfgApplication getApplication(CfgApplicationQuery query) {
        return (CfgApplication) configObjectProvider.getObject(CfgApplication.class, query);
    }

    @Override
    public Collection<CfgApplication> getApplications(CfgApplicationQuery query) {
        return configObjectProvider.getObjects(CfgApplication.class, query);
    }
}
