package com.wenyang.im.rpc;

import com.wenyang.im.rpc.jdbc.DBUtil;
import com.wenyang.im.rpc.mqtt.MqttApplcationContent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImSpringbootApplication {

    public static void main(String[] args) {
        DBUtil.init(null);
        new MqttApplcationContent().initMqttApplication();
        SpringApplication.run(ImSpringbootApplication.class, args);
    }

}
