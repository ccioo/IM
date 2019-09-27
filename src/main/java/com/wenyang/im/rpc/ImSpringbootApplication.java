package com.wenyang.im.rpc;

import com.wenyang.im.rpc.mqtt.MqttApplcationContent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImSpringbootApplication {

    public static void main(String[] args) {
        new MqttApplcationContent().initMqttApplication();
        SpringApplication.run(ImSpringbootApplication.class, args);
    }

}
