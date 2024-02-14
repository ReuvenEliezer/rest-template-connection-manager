package com.rest.template.connection.manager.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
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
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();
        HttpClient httpClient = buildHttpClient(connectionManager);
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    @Bean //one thread
    public RestTemplate restTemplateWithBasicPoolManager() {
        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
        CloseableHttpClient httpClient = buildHttpClient(connectionManager);
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    private static CloseableHttpClient buildHttpClient(HttpClientConnectionManager connectionManager) {
        return HttpClientBuilder
                .create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(createReqConfig())
                .build();
    }

    private static RequestConfig createReqConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(0, TimeUnit.MILLISECONDS) // timeout to get connection from pool
                .setConnectTimeout(5000, TimeUnit.MILLISECONDS) // standard connection timeout
                .build();
    }



}

