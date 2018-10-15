package com.genesys.x.statdnregister;

import com.genesys.x.statdnregister.interfaces.IConfigObjectProvider;
import com.genesyslab.platform.applicationblocks.com.*;
import com.genesyslab.platform.applicationblocks.com.objects.CfgFilter;
import com.genesyslab.platform.applicationblocks.com.objects.CfgService;
import com.genesyslab.platform.applicationblocks.com.queries.CfgFilterQuery;
import com.genesyslab.platform.applicationblocks.com.queries.CfgObjectiveTableQuery;
import com.genesyslab.platform.applicationblocks.com.runtime.factory.CfgFilterFactory;
import com.genesyslab.platform.commons.protocol.Protocol;
import com.genesyslab.platform.configuration.protocol.types.CfgObjectType;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by dburdick on 12/2/2015.
 */
public class ConfigObjectProvider implements IConfigObjectProvider {
    private IConfService configService;

    @Inject
    public ConfigObjectProvider(IConfService configService) {
        this.configService = configService;
    }

    public <TT extends ICfgObject> TT Query(CfgQuery<TT> query) {
        try {
            return configService.retrieveObject(query);
        } catch (ConfigException cx) {
            return null;
        }
    }

    public <TT extends ICfgObject> Collection<TT> QueryMultiple(CfgQuery<TT> query) {
        try {
            return configService.retrieveMultipleObjects(null, query);
        } catch (InterruptedException ix) {
            return new HashSet<TT>();
        } catch (ConfigException cx) {
            return new HashSet<TT>();
        }
    }

    @Override
    public <TT extends ICfgObject> TT getObject(Class<TT> type, String name) {
        CfgFilterBasedQuery q;
        return null;
    }

    @Override
    public <TT extends ICfgObject> TT getObject(Class<TT> type, int dbid) {
        return null;
    }

    @Override
    public <TT extends ICfgObject> TT getObject(Class<TT> type, ICfgQuery<TT> query) {
        try {
            return configService.retrieveObject(type, query);
        } catch (ConfigException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public <TT extends ICfgObject> Collection<TT> getObjects(Class<TT> type, ICfgQuery<TT> query) {
        try {
            return configService.retrieveMultipleObjects(type, query, 60000);
        } catch (ConfigException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
