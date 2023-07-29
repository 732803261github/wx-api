package com.github.niefy.modules.wx.service;

import com.github.niefy.common.utils.DateUtils;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 程序发送模板消息demo
 */
@SpringBootTest
class TemplateMsgServiceTest {
    @Autowired
    TemplateMsgService templateMsgService;
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 发送模板消息给用户
     * 添加消息模板指引：https://kf.qq.com/faq/170209E3InyI170209nIF7RJ.html
     * 示例消息模板为：{{first.DATA}} ↵商品名称：{{keyword1.DATA}} ↵购买时间：{{keyword2.DATA}} ↵{{remark.DATA}}
     */
    @Test
    void sendTemplateMsg() {
//        String appid = WxMpConfigStorageHolder.get();
        String appid = "wx0f791e273d673f03";
        List<WxMpTemplateData> data  = new ArrayList<>();
        data.add(new WxMpTemplateData("thing1","白天鹅，在湖里，捕鱼","red"));
        data.add(new WxMpTemplateData("character_string2","3878599093670556"));
        data.add(new WxMpTemplateData("time3", DateUtils.format(new Date(),"yyyy-MM-dd HH:mm")));
        WxMpTemplateMessage wxMpTemplateMessage = WxMpTemplateMessage.builder()
            .templateId("K_WOhj5KoEgBc7MomCHL48SGptwWr2uvvjwH9Zwv2Rg")
            .url("http://ai-assistant.com.cn/api/cnd-discordapp/attachments/1120568025993715764/1129837389020414062/oliverdaniel_3878599093670556_Swans_fish_in_the_lake_during_the_4a97a352-a0ec-47c0-80a2-c6071031a23b.png?Authorization=9998@xunshu")
            .toUser("o731S6QvW6NlhTkJyGYJNItsu9a8")
            .data(data)
            .build();
        templateMsgService.sendTemplateMsg(wxMpTemplateMessage,appid);
    }

    @Test
    void redisTest(){
        String key = "9244402853440658";
        ValueOperations valueOperations = redisTemplate.opsForValue();
        redisTemplate.delete(key);
    }
}
