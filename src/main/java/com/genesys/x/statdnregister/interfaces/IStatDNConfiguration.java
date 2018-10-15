package com.genesys.x.statdnregister.interfaces;

/**
 * Created by dburdick on 12/2/2015.
 */
public interface IStatDNConfiguration {
    String getConfigServerUrl();

    String getStatServerName();

    String getExtensionStatistic();

    String getVirtualQueueStatistic();

    String getClientApplication();

}
