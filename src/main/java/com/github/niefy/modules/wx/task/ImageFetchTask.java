package com.github.niefy.modules.wx.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ImageFetchTask {

    @Autowired
    private RedisTemplate redisTemplate;

    RestTemplate restTemplate = new RestTemplate();

    private final String authorization = String.valueOf(redisTemplate.opsForValue().get("authorization"));

    @Scheduled(cron = "* */1 * * * ?")
    public void getImg() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", authorization);
        Map<String, Object> map = new HashMap<>();
        HttpEntity requestEntity = new HttpEntity(map, headers);
        String response = restTemplate.exchange(
                "https://discord.com/api/v9/channels/1120568025993715764/messages?limit=10",
                HttpMethod.GET,
                requestEntity,
                String.class
        ).getBody();
        JSONArray objects = JSON.parseArray(response);
        for (Object object : objects) {
            for (Object attachments : ((JSONObject) object).getJSONArray("attachments")) {
                String taskid = ((JSONObject) object).getString("content").split("]")[0].substring(3);
                String string = ((JSONObject) attachments).getString("proxy_url");
                String replace = string.replace("https://media.discordapp.net", "http://www.ai-assistant.com.cn/api/cnd-discordapp");
                String url = replace + "?Authorization=9998@xunshu";
                redisTemplate.opsForValue().set(taskid, url);
            }
        }
    }
}
