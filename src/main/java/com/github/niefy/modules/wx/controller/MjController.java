package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.niefy.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
public class MjController {

    @Autowired
    private RedisTemplate redisTemplate;

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/data")
    public R retrieve_messages(){
        log.info("token:{}",redisTemplate.opsForValue().get("authorization"));
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", String.valueOf(redisTemplate.opsForValue().get("authorization")));
        HttpEntity requestEntity = new HttpEntity(headers);
        String response = restTemplate.exchange(
                "https://discord.com/api/v10/channels/1120568025993715764/messages",
                HttpMethod.GET,
                requestEntity,
                String.class
        ).getBody();
        log.info("响应成功：{}",response);
        return R.ok().put(response);
    }
}
