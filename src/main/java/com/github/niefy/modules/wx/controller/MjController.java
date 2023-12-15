package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.niefy.common.utils.R;
import com.github.niefy.modules.wx.aspect.Limiting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class MjController {

    @Autowired
    private RedisTemplate redisTemplate;
    RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/data")
    public R retrieve_messages() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", String.valueOf(redisTemplate.opsForValue().get("authorization")));
        Map<String, Object> map = new HashMap<>();
        HttpEntity requestEntity = new HttpEntity(map, headers);
        String url = String.format("https://discord.com/api/v9/channels/%s/messages?limit=20", redisTemplate.opsForValue().get("channel"));
        String response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        ).getBody();
        List<String> list = new ArrayList<>();
        JSONArray objects = JSON.parseArray(response);
//        for (Object object : objects) {
//            for (Object attachments : ((JSONObject) object).getJSONArray("attachments")) {
//                String string = ((JSONObject) attachments).getString("proxy_url");
//                String replace = string.replace("https://media.discordapp.net", "http://www.ai-assistant.com.cn/api/cnd-discordapp");
//                list.add(replace + "?Authorization=9998@xunshu");
//            }
//        }
        return R.ok().put(objects);
    }

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
                    map.put(taskid, url);
                }
            });
            return R.ok().put(map);
        } else {
            return R.ok();
        }
    }
}
