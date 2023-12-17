package com.github.niefy.modules.wx.task;

import com.alibaba.fastjson.JSON;
import com.github.niefy.common.utils.DateUtils;
import com.github.niefy.modules.wx.service.TemplateMsgService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class WxMessageTask {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private TemplateMsgService templateMsgService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void message() {
        List<String> keys = new ArrayList<>(redisTemplate.keys("bindopenid::*"));
        keys.stream().forEach(key -> {
            String openid = redisTemplate.opsForValue().get(key).toString();
            String taskid = key.split("bindopenid::")[1];
            sendTemplateMsg(openid, taskid);
        });
    }

    void sendTemplateMsg(String openid, String taskid) {
        String appid = String.valueOf(redisTemplate.opsForValue().get("appid"));
        List<WxMpTemplateData> data = new ArrayList<>();
        data.add(new WxMpTemplateData("character_string2", taskid));
        data.add(new WxMpTemplateData("time3", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm")));
        String key = "mj-task-store::".concat(taskid);
        String delKey = "bindopenid::".concat(taskid);
        if (ObjectUtils.isNotEmpty(redisTemplate.opsForValue().get(key))) {
            String url = JSON.parseObject(redisTemplate.opsForValue().get(key).toString()).getString("imageUrl");
            String s = url.split("\\?ex=")[0];
            String replace = s.replace("https://cdn.discordapp.com", "http://www.ai-assistant.com.cn/api/cnd-discordapp");
            log.info("图片内容:{}",replace);
            if (StringUtils.isNotEmpty(url)) {
                WxMpTemplateMessage wxMpTemplateMessage = WxMpTemplateMessage.builder()
                        .templateId("K_WOhj5KoEgBc7MomCHL4wbq6i82ULsyxDDKepVnZVs")
                        .url(replace)
                        .toUser(openid)
                        .data(data)
                        .build();
                templateMsgService.sendTemplateMsg(wxMpTemplateMessage, appid);
                redisTemplate.delete(delKey);
            }
        }
    }
}
