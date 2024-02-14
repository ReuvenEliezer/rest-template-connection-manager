package com.rest.template.connection.manager;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = RestTemplateConnectionManagerApplication.class)
class RestTemplateConnectionManagerApplicationTests {

    /**
     * https://stackoverflow.com/questions/14866362/invalid-use-of-basicclientconnmanager-connection-still-allocated
     */
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConnectionManagerApplication.class);
    private static final String localhost = "http://localhost:";

    @Value("${spring.server.port}")
    private Integer serverPort;

//    @Autowired
//    @Qualifier("restTemplateWithBasicPoolManager")
    private RestTemplate restTemplate;
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    @Test
    void restTemplateWithBasicHttpClientConnectionManagerIllegalStateExceptionTest() {
        initRestTemplateWithHttpClientConnectionManager();
        assertThatThrownBy(() -> {
            ScheduledFuture<List<Future<String>>> schedule = executorService.schedule(this::sendReq, 0, TimeUnit.SECONDS);
            assertResponse(schedule);
        }).hasCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Connection [Not bound] is still allocated");
    }

    @Test
    void restTemplateWithPoolingHttpClientConnectionManagerTest() throws ExecutionException, InterruptedException {
        initRestTemplateWithPoolingHttpClientConnectionManager();
        ScheduledFuture<List<Future<String>>> schedule = executorService.schedule(this::sendReq, 0, TimeUnit.SECONDS);
        assertResponse(schedule);
    }


    @Test
    void restTemplateWithDefaultPoolManagerTest() throws ExecutionException, InterruptedException {
        initRestTemplateWithDefaultPoolManager();
        ScheduledFuture<List<Future<String>>> schedule = executorService.schedule(this::sendReq, 0, TimeUnit.SECONDS);
        assertResponse(schedule);
    }

    private void initRestTemplateWithDefaultPoolManager() {
        restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    private void initRestTemplateWithPoolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();
        HttpClient httpClient = buildHttpClient(connectionManager);
        initRestTemplateWithHttpClientConnectionManager(httpClient);
    }

    private void initRestTemplateWithHttpClientConnectionManager(HttpClient httpClient) {
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate = new RestTemplate(requestFactory);
    }

    private void initRestTemplateWithHttpClientConnectionManager() {
        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager();
        CloseableHttpClient httpClient = buildHttpClient(connectionManager);
        initRestTemplateWithHttpClientConnectionManager(httpClient);
    }

    private static RequestConfig createReqConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(0, TimeUnit.MILLISECONDS) // timeout to get connection from pool
                .setConnectTimeout(5000, TimeUnit.MILLISECONDS) // standard connection timeout
                .build();
    }

    private static CloseableHttpClient buildHttpClient(HttpClientConnectionManager connectionManager) {
        return HttpClientBuilder
                .create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(createReqConfig())
                .build();
    }

    private List<Future<String>> sendReq() {
        return IntStream.range(0, 100).mapToObj(index -> executorService.submit(() -> {
            logger.info("sending request");
            return restTemplate.getForObject(localhost + serverPort + "/", String.class);
        })).collect(Collectors.toList());
    }

    private static void assertResponse(ScheduledFuture<List<Future<String>>> schedule) throws InterruptedException, ExecutionException {
        for (Future<?> future : schedule.get()) {
            assertThat(future.get()).isEqualTo("ok");
        }
    }

}
