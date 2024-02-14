package com.notayessir.controller;


import com.alibaba.fastjson2.JSONObject;
import com.notayessir.config.AppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Autowired
    private AppConfiguration appConfiguration;

    @RequestMapping("/index")
    public String index(){
        return JSONObject.toJSONString(appConfiguration);
    }

}
