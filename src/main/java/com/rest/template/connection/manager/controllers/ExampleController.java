package com.rest.template.connection.manager.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/")
public class ExampleController {
    private static final Log logger = LogFactory.getLog(ExampleController.class);

    private final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

    public ExampleController(PoolingHttpClientConnectionManager poolingHttpClientConnectionManager) {
        this.poolingHttpClientConnectionManager = poolingHttpClientConnectionManager;
    }

    @GetMapping()
    public String restTemplateTimeOut() {
        logger.info("ok");
        try {
            Thread.sleep(Duration.ofMillis(200).toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        poolingHttpClientConnectionManager.getRoutes().forEach(route -> {
//            logger.info("route: " + route);
//        });
//        logger.info(" poolingHttpClientConnectionManager.getRoutes() : " +  poolingHttpClientConnectionManager.getRoutes());

        return "ok";
    }


}
