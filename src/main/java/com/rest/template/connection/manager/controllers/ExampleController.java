package com.rest.template.connection.manager.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/")
public class ExampleController {
    private static final Log logger = LogFactory.getLog(ExampleController.class);

    @GetMapping()
    public String restTemplateTimeOut() {
        logger.info("ok");
        try {
            Thread.sleep(Duration.ofMillis(20).toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "ok";
    }


}
