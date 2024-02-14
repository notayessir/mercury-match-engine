package com.notayessir.match.engine.publisher.impl;

import com.alibaba.fastjson2.JSONObject;
import com.notayessir.bo.MatchResultBO;
import com.notayessir.match.engine.publisher.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LogPublisher implements Publisher {
    @Override
    public void publish(MatchResultBO matchResultBO) {
        log.info(JSONObject.toJSONString(matchResultBO));
    }
}
