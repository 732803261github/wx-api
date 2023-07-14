package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.niefy.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
public class WxMidController {

    @Autowired
    private RedisTemplate redisTemplate;

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/data")
    public R retrieve_messages(){
        log.info("token:{}",redisTemplate.opsForValue().get("authorization"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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
    @GetMapping(value = "/data2")
    public String test(){
        return "hello admin";
    }

}
