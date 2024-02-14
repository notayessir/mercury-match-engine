package com.notayessir.match.engine.publisher.impl;

import com.alibaba.fastjson2.JSONObject;
import com.notayessir.bo.MatchResultBO;
import com.notayessir.config.AppConfiguration;
import com.notayessir.match.engine.publisher.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class KafkaPublisher implements Publisher {


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AppConfiguration appConfiguration;


    @Override
    public void publish(MatchResultBO matchResultBO) {
        kafkaTemplate.send(appConfiguration.getKafkaTopic(), JSONObject.toJSONString(matchResultBO));
    }
}
