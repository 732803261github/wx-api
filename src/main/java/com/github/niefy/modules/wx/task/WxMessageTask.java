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
        log.info("taskdone-*的所有数据keys是：{}",keys);
        keys.stream().forEach(key->{
            String taskid = key.split("taskdone-")[1];
            log.info("taskid={}",taskid);
            List<String> keys2 = new ArrayList<String>(redisTemplate.keys(taskid + "-*"));
            keys2.stream().forEach(key2->{
                System.out.println("openid="+key2.split(taskid+"-")[1]);
            });
        });
    }
    void sendTemplateMsg(String openid,String taskid) {
        String appid = String.valueOf(redisTemplate.opsForValue().get("appid"));
        List<WxMpTemplateData> data = new ArrayList<>();
        data.add(new WxMpTemplateData("character_string2", "3878599093670556"));
        data.add(new WxMpTemplateData("time3", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm")));
        String url = redisTemplate.opsForValue().get(taskid).toString();
        WxMpTemplateMessage wxMpTemplateMessage = WxMpTemplateMessage.builder()
                .templateId("K_WOhj5KoEgBc7MomCHL4wbq6i82ULsyxDDKepVnZVs")
                .url(url)
                .toUser(openid)
                .data(data)
                .build();
        templateMsgService.sendTemplateMsg(wxMpTemplateMessage, appid);
    }
}
