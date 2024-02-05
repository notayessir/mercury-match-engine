package com.notayessir.publisher;

import com.alibaba.fastjson2.JSONObject;
import com.notayessir.Publisher;
import com.notayessir.bo.MatchResultBO;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LogPublisher implements Publisher {
    @Override
    public void publish(MatchResultBO matchResultBO) {
        log.info(JSONObject.toJSONString(matchResultBO));
    }
}
