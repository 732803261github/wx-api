package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.niefy.common.utils.R;
import com.github.niefy.modules.wx.aspect.Limiting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@RestController
public class OffLineImgGetController {

    @Autowired
    private RedisTemplate redisTemplate;
    RestTemplate restTemplate = new RestTemplate();

    @Limiting(limitNum = 500, name = "getImgLimit")
    @PostMapping(value = "/getImg")
    public R getImg(HttpServletRequest request) {
        if (ObjectUtils.isNotEmpty(request.getParameterMap().get("taskids[]"))) {
            List<String> taskids = Arrays.asList(request.getParameterMap().get("taskids[]"));
            Map<String, Object> map = new HashMap<>();
            taskids.stream().forEach(taskid -> {
                String key = "mj-task-store::".concat(taskid);
                if (ObjectUtils.isNotEmpty(redisTemplate.opsForValue().get(key))) {
                    String url = JSON.parseObject(redisTemplate.opsForValue().get(key).toString()).getString("imageUrl");
                    String replace = url.replace("https://cdn.discordapp.com", "http://www.ai-assistant.com.cn/api/cnd-discordapp");
                    map.put(taskid, replace);
                }
            });
            return R.ok().put(map);
        } else {
            return R.ok();
        }
    }
}
