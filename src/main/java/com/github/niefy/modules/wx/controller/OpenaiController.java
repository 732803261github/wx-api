package com.github.niefy.modules.wx.controller;

import com.github.niefy.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class OpenaiController {
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping(value = "/record")
    public void recordApikey(String apiKey, String openid) {
        log.info("apiKey:{}", apiKey);
        List<String> keys = Arrays.asList(redisTemplate.opsForValue().get("keys").toString().split(","));
        keys.forEach(key -> {
            String hashKey = DigestUtils.sha1Hex(key);
            log.info("DigestUtils.sha1Hex({})={}",key,hashKey);
            if (hashKey.equals(apiKey)) {
                String day = DateUtils.format(new Date(), "yyyy-MM-dd").concat(key);
                int record = 0;
                if (redisTemplate.opsForValue().get(day) != null) {
                    record += Integer.parseInt(redisTemplate.opsForValue().get(day).toString());
                    redisTemplate.opsForValue().getAndSet(day, record);
                } else {
                    redisTemplate.opsForValue().set(day, 1, 30, TimeUnit.DAYS);
                }
            }
        });
    }
}
