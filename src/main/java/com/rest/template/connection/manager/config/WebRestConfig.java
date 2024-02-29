package com.rest.template.connection.manager.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebRestConfig {

    /**
     * https://stackoverflow.com/questions/14866362/invalid-use-of-basicclientconnmanager-connection-still-allocated
     */

    @Value("${http.max-conn-total:100}")
    private int maxConnTotal;

    @Value("${http.max-conn-per-route:20}")
    private int maxConnPerRoute;


    @Bean
    public RestTemplate restTemplateWithDefaultPoolManager() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Bean
    @Primary
    public RestTemplate restTemplateWithPoolManager() {
        HttpClient httpClient = buildHttpClient(poolingHttpClientConnectionManager());
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(createConnectionConfig())
                .setMaxConnTotal(maxConnTotal)
                .setMaxConnPerRoute(maxConnPerRoute)
                .build();
    }

    @Bean //one thread
    public RestTemplate restTemplateWithBasicPoolManager() {
        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
        connectionManager.setConnectionConfig(createConnectionConfig());

        HttpClient httpClient = buildHttpClient(connectionManager);
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    private static HttpClient buildHttpClient(HttpClientConnectionManager connectionManager) {
        return HttpClientBuilder
                .create()
                .setConnectionManager(connectionManager)
                .build();
    }

    private ConnectionConfig createConnectionConfig() {
        return ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(Duration.ofMillis(0)))// timeout to get connection from pool
                .setSocketTimeout(Timeout.of(Duration.ofSeconds(5))) // standard connection timeout
                .build();
    }


}

