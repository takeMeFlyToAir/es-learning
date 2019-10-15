package com.zzr.business.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * Created by zhaozhirong on 2019/10/15.
 */
@Configuration
public class ESTransportClientDecorator implements InitializingBean, DisposableBean {

    private TransportClient transportClient;

    private String host = "140.143.238.46";

    private Integer port = 9300;

    public ESTransportClientDecorator() {

    }

    public ESTransportClientDecorator(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public TransportClient getTransportClient() {
        if (transportClient == null) {
            transportClient = new PreBuiltTransportClient(Settings.EMPTY);

        }
        return transportClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        transportClient = new PreBuiltTransportClient(Settings.EMPTY);
        // Add the Transport Address to the TransportClient:
        transportClient.addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
    }

    @Override
    public void destroy() throws Exception {
        transportClient.close();
    }
}
