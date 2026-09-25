package br.com.pradolabs.resourceserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/resource-server")
public class BlockingController {

    @GetMapping("/blocking/{sleepInSecond}")
    public String blocking(@PathVariable int sleepInSecond) {
        StringBuffer response = new StringBuffer();

        response.append("Start blocking for ").append(sleepInSecond).append(" seconds ");
        response.append("thread.name: ").append(Thread.currentThread().getName());
        response.append(" started at ").append(System.currentTimeMillis());

        try {
            Thread.sleep(sleepInSecond * 1000);
        } catch (InterruptedException e) {
            log.error("Error occurred while sleeping", e);
        }
        response.append(" ended at ").append(System.currentTimeMillis());
        return response.toString();
    }
}
