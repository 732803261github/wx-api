package com.github.niefy.modules.wx.task;

import com.github.niefy.common.utils.DateUtils;
import com.github.niefy.modules.wx.service.TemplateMsgService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
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
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    StringRedisTemplateUtil redisUtil;

    @Autowired
    private TemplateMsgService templateMsgService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void message() {
        List<String> keys = redisUtil.keys("taskdone-*");
        keys.stream().forEach(key->{
            String taskid = key.split("taskdone-")[1];
            List<String> keys2 = new ArrayList<String>(redisTemplate.keys(taskid.concat("-*")));
            keys2.stream().forEach(key2->{
                String openid = key2.split(taskid.concat("-"))[1];
                sendTemplateMsg(openid,taskid);
            });
        });
    }
    void sendTemplateMsg(String openid,String taskid) {
        String appid = String.valueOf(redisTemplate.opsForValue().get("appid"));
        List<WxMpTemplateData> data = new ArrayList<>();
        data.add(new WxMpTemplateData("character_string2", taskid));
        data.add(new WxMpTemplateData("time3", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm")));
        String url = redisTemplate.opsForValue().get(taskid).toString();
        log.info("{},{},{}",openid,taskid,url);
        WxMpTemplateMessage wxMpTemplateMessage = WxMpTemplateMessage.builder()
                .templateId("K_WOhj5KoEgBc7MomCHL4wbq6i82ULsyxDDKepVnZVs")
                .url(url)
                .toUser(openid)
                .data(data)
                .build();
        templateMsgService.sendTemplateMsg(wxMpTemplateMessage, appid);
        redisTemplate.delete(taskid);
        redisTemplate.delete(taskid.concat("-").concat(openid));
        stringRedisTemplate.delete("taskdone-".concat(taskid));

    }
}
