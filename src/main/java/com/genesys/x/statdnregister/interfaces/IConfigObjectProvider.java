package com.genesys.x.statdnregister.interfaces;

import com.genesyslab.platform.applicationblocks.com.ICfgObject;
import com.genesyslab.platform.applicationblocks.com.ICfgQuery;

import java.util.Collection;

/**
 * Created by dburdick on 12/3/2015.
 */
public interface IConfigObjectProvider {
    public <TT extends ICfgObject> TT getObject(Class<TT> type, String name);
    public <TT extends ICfgObject> TT getObject(Class<TT> type, int dbid);
    public <TT extends ICfgObject> TT getObject(Class<TT> type, ICfgQuery<TT> query);
    public <TT extends ICfgObject> Collection<TT> getObjects(Class<TT> type, ICfgQuery<TT> query);
}
