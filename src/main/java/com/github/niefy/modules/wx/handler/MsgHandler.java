package com.github.niefy.modules.wx.handler;


import java.util.*;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.niefy.modules.wx.entity.WxMsg;
import com.github.niefy.modules.wx.service.MsgReplyService;
import com.github.niefy.modules.wx.service.WxMsgService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author Binary Wang
 */
@Component
public class MsgHandler extends AbstractHandler {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    MsgReplyService msgReplyService;
    @Autowired
    WxMsgService wxMsgService;
    private static final String TRANSFER_CUSTOMER_SERVICE_KEY = "人工";
    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService wxMpService,
                                    WxSessionManager sessionManager) {
        logger.info("监听到公众号后台用户消息={}",wxMessage.getContent());
        String textContent = wxMessage.getContent();
        String fromUser = wxMessage.getFromUser();
        String appid = WxMpConfigStorageHolder.get();
        if(wxMessage.getContent().length()>4 && wxMessage.getContent().substring(0,4).trim().toLowerCase().equals("@gpt")){
            String prompt = wxMessage.getContent().substring(4).trim();
            String gptResponse = askGPT(prompt,wxMessage.getFromUser());
            msgReplyService.gptReturn(appid, "text", fromUser, gptResponse);
            wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.TRANSFER_CUSTOMER_SERVICE,fromUser,null));
            return WxMpXmlOutMessage
                    .TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser())
                    .toUser(fromUser).build();
        } else if (wxMessage.getContent().length()>4 && wxMessage.getContent().substring(0,4).trim().toLowerCase().equals("@img")) {
            String prompt = wxMessage.getContent().substring(4).trim();
            JSONObject res = genImg(prompt,wxMessage.getFromUser());
            String imgUrl = String.format("%s\n%s生成中，系统即将返回生成结果...",res.getString("result"),prompt);
            String key = "bindopenid::".concat(res.getString("result"));
            redisTemplate.opsForValue().set(key,fromUser,3600, TimeUnit.SECONDS);
            msgReplyService.gptReturn(appid,"text", fromUser, imgUrl);
            wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.TEXT,fromUser,null));
            return WxMpXmlOutMessage
                    .TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser())
                    .toUser(fromUser).build();
        } else {
            boolean autoReplyed = msgReplyService.tryAutoReply(appid,false, fromUser, textContent);
            //当用户输入关键词如“你好”，“客服”等，并且有客服在线时，把消息转发给在线客服
            if (TRANSFER_CUSTOMER_SERVICE_KEY.equals(textContent) || !autoReplyed) {
                wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.TRANSFER_CUSTOMER_SERVICE,fromUser,null));
                return WxMpXmlOutMessage
                        .TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser())
                        .toUser(fromUser).build();
            }
        }
        return null;

    }

    public String askGPT(String prompt,String openid){
        MultiValueMap<String,Object> map = new LinkedMultiValueMap();
        map.put("prompt", Arrays.asList(prompt));
        map.put("openid",Arrays.asList(openid));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity requestEntity = new HttpEntity(map,headers);
        //python接口服务
        String response = restTemplate.exchange(
                "http://ai-assistant.com.cn:8081/wxcom/singlechat",
                HttpMethod.POST,
                requestEntity,
                String.class
        ).getBody();
        return JSON.parseObject(response).getString("content");
    }
    public JSONObject genImg(String prompt, String openid){
        Map<String,Object> map = new HashMap<>();
        map.put("prompt", prompt);
        map.put("openid",openid);
        map.put("base64",null);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity requestEntity = new HttpEntity(map,headers);
        String response = restTemplate.exchange(
                "http://ai-assistant.com.cn:8080/mj/submit/imagine",
                HttpMethod.POST,
                requestEntity,
                String.class
        ).getBody();
        logger.info("response:{}",JSON.parseObject(response));
        return JSON.parseObject(response);
    }

}
