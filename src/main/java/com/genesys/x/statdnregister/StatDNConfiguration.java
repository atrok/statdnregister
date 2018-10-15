package com.genesys.x.statdnregister;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.genesys.x.statdnregister.interfaces.IStatDNConfiguration;
import com.google.inject.Singleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dburdick on 12/2/2015.
 */
@Singleton
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatDNConfiguration implements IStatDNConfiguration {
    private String configServerUrl;
    private String statServerName;
    private String extensionStatistic;
    private String virtualQueueStatistic;
    private String clientApplication;

    public static IStatDNConfiguration Create() throws IOException {
        JsonFactory factory;
        URL url = null;
        String configFiles[] = new String[]{"statdnconfig.yml", "statdnconfig.json"};
        boolean ymlFileType = true;

        for (String file: configFiles) {
            try {
                File f = new File(file);
                if (f.exists()) {
                    url = f.toURI().toURL();
                    break;
                }
                ymlFileType = false;

            } catch (MalformedURLException mux) {
            }
        }

        if (url == null){
            throw new FileNotFoundException("statdnconfig.[yml|json]");
        }

        if (ymlFileType) {
            factory = new YAMLFactory();
        } else {
            factory = new JsonFactory();
        }
        JsonParser parser = factory.createParser(url);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        //mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        IStatDNConfiguration config = mapper.readValue(url, StatDNConfiguration.class);

        return config;
    }

    @Override
    public String getConfigServerUrl() {
        return configServerUrl;
    }

    @JsonProperty("configserverurl")
    private void setConfigServerUrl(String configServerUrl) {
        this.configServerUrl = configServerUrl;
    }

    @Override
    public String getStatServerName() {
        return statServerName;
    }

    @JsonProperty("statservername")
    private void setStatServerName(String statServerName) {
        this.statServerName = statServerName;
    }

    @Override
    public String getExtensionStatistic() {
        return extensionStatistic;
    }

    @JsonProperty("extensionstatistic")
    private void setExtensionStatistic(String extensionStatistic) {
        this.extensionStatistic = extensionStatistic;
    }

    @Override
    public String getVirtualQueueStatistic() {
        return virtualQueueStatistic;
    }

    @JsonProperty("virtualqueuestatistic")
    private void setVirtualQueueStatistic(String virtualQueueStatistic) {
        this.virtualQueueStatistic = virtualQueueStatistic;
    }

    @Override
    public String getClientApplication() {
        return clientApplication;
    }

    @JsonProperty("clientapplication")
    private void setClientApplication(String clientApplication){
        this.clientApplication = clientApplication;
    }
}
