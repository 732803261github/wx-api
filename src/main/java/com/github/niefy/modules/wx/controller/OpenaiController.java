package com.github.niefy.modules.wx.controller;

import com.github.niefy.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
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
            if (hashKey.equals(apiKey)) {
                String dayUseKey = DateUtils.format(new Date(), "yyyy-MM-dd").concat("||").concat(key);
                increKey(key,dayUseKey);
                String userDayUseKey = DateUtils.format(new Date(), "yyyy-MM-dd").concat("||").concat(key);
                updateUserRecord("USE-".concat(openid), userDayUseKey);
            }
        });
    }

    public void increKey(String key,String dayUseKey) {
        if (ObjectUtils.isEmpty(redisTemplate.opsForHash().get(key, dayUseKey))) {
            Map<String, Object> map = new HashMap<>();
            map.put(dayUseKey, "1");
            redisTemplate.opsForHash().putAll(key, map);
            redisTemplate.opsForHash().getOperations().expire(key, 30, TimeUnit.DAYS);
        } else {
            redisTemplate.opsForHash().increment(key, dayUseKey, 1);
        }
    }

    public void updateUserRecord(String openid, String key) {
        if (ObjectUtils.isEmpty(redisTemplate.opsForHash().get(openid, key))) {
            Map<String, Object> map = new HashMap<>();
            map.put(key, "1");
            redisTemplate.opsForHash().putAll(openid, map);
            redisTemplate.opsForHash().getOperations().expire(openid, 30, TimeUnit.DAYS);
        } else {
            redisTemplate.opsForHash().increment(openid, key, 1);
        }
    }
}
