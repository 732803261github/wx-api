package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.niefy.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class MidJourneyController {

    @Autowired
    private RedisTemplate redisTemplate;

    RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(value = "/json")
    public R retrieve_messages(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization",String.valueOf(redisTemplate.opsForValue().get("authorization")));
        HttpEntity requestEntity = new HttpEntity(headers);
        JSONObject response = restTemplate.exchange(
                "https://discord.com/api/v10/channels/1120568025993715764/messages",
                HttpMethod.GET,
                requestEntity,
                JSONObject.class
        ).getBody();
        log.info("响应成功：{}",response);
        return R.ok().put(response);
    }

}
