package com.notayessir.publisher;

import com.alibaba.fastjson2.JSONObject;
import com.notayessir.Publisher;
import com.notayessir.bo.MatchCommandResultBO;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LogPublisher implements Publisher {
    @Override
    public void publish(MatchCommandResultBO matchCommandResultBO) {
        log.info(JSONObject.toJSONString(matchCommandResultBO));
    }
}
