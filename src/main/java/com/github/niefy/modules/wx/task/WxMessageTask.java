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
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WxMessageTask {

    @Autowired
    RedisTemplate redisTemplate;

    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private TemplateMsgService templateMsgService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void message() {
        String pattern ="taskdone-*";
        Set<String> keys = (Set<String>) this.redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(1000).build());
            return cursor.stream().map(String::new).collect(Collectors.toSet());
        });
        log.info("{}的所有数据keys是：{}",pattern,keys);
        ValueOperations<String, String> operations = this.redisTemplate.opsForValue();
        List<String> collect = keys.stream().map(operations::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("keys:{}",collect);
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
