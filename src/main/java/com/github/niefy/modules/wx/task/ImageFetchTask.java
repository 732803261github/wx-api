package com.github.niefy.modules.wx.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ImageFetchTask {

    @Autowired
    RedisTemplate redisTemplate;

    RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0/10 * * * * ?")
    public void getImg() {
        String lastId = String.valueOf(redisTemplate.opsForValue().get("lastId"));
        String authorization = String.valueOf(redisTemplate.opsForValue().get("authorization"));
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", authorization);
        Map<String, Object> map = new HashMap<>();
        HttpEntity requestEntity = new HttpEntity(map, headers);
        String channel = String.format("https://discord.com/api/v9/channels/%s/messages?limit=50", redisTemplate.opsForValue().get("channel"));
        String api = channel+ (!"null".equals(lastId) ? "&before=" + lastId : "");
        String response = restTemplate.exchange(
                api,
                HttpMethod.GET,
                requestEntity,
                String.class
        ).getBody();
        JSONArray objects = JSON.parseArray(response);
        if (objects.size() > 0) {
            String nextId = ((JSONObject) objects.get(objects.size() - 1)).getString("id");
            if (!"null".equals(lastId)) {
                redisTemplate.delete(lastId);
            }
            redisTemplate.opsForValue().set("lastId", nextId, 3600, TimeUnit.SECONDS);
            for (Object object : objects) {
                for (Object attachments : ((JSONObject) object).getJSONArray("attachments")) {
                    String taskid = ((JSONObject) object).getString("content").split("]")[0].substring(3);
                    boolean isNumeric = taskid.matches("\\d+");
                    if (isNumeric) {
                        String string = ((JSONObject) attachments).getString("proxy_url");
                        String replace = string.replace("https://media.discordapp.net", "http://www.ai-assistant.com.cn/api/cnd-discordapp");
                        String url = replace + "?Authorization=9998@xunshu";
                        redisTemplate.opsForValue().getAndDelete(taskid);
                        redisTemplate.opsForValue().set(taskid, url, 3600, TimeUnit.SECONDS);
                    }
                }
            }
        } else {
            redisTemplate.delete("lastId");
        }
    }
}
