package com.github.niefy.modules.wx.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RedisUtil {

    @Autowired
    RedisTemplate redisTemplate;

    public List<String> getKeys(String pattern){
        Set<String> keys = (Set<String>) this.redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(1000).build());
            return cursor.stream().map(String::new).collect(Collectors.toSet());
        });
        List<String> list = new ArrayList<>(keys);
        return list;
    }

    public String get(String key){
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        String string = valueOps.get(key).toString();
        return string;
    }

}
