package com.github.niefy.modules.wx.handler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.niefy.config.TaskExcutor;
import com.github.niefy.modules.wx.entity.MsgReplyRule;
import com.github.niefy.modules.wx.entity.WxMsg;
import com.github.niefy.modules.wx.service.MsgReplyService;
import com.github.niefy.modules.wx.service.WxMsgService;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
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

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService wxMpService,
                                    WxSessionManager sessionManager) {
        logger.info("监听到公众号后台用户消息");
        logger.info("{}",wxMessage.getContent());
        String textContent = wxMessage.getContent();
        String fromUser = wxMessage.getFromUser();
        String appid = WxMpConfigStorageHolder.get();
        if(wxMessage.getContent().length()>4 && wxMessage.getContent().substring(0,4).trim().equals("@gpt")){
            String prompt = wxMessage.getContent().substring(4);
            logger.info("prompt={}",prompt);
            String gptResponse = askGPT(prompt);
            wxMsgService.addWxMsg(WxMsg.buildOutMsg(WxConsts.KefuMsgType.TRANSFER_CUSTOMER_SERVICE,fromUser,null));
            return WxMpXmlOutMessage
                    .TRANSFER_CUSTOMER_SERVICE().fromUser(wxMessage.getToUser())
                    .toUser(fromUser).build();
        }else {
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

    public String askGPT(String prompt){
        logger.info(prompt);
        Map<String, Object> map = new HashMap<>();
        map.put("prompt",prompt);
        HttpEntity requestEntity = new HttpEntity(map);
        String response = restTemplate.exchange(
                "http://ai-assistant.com.cn:8081/wxcom/message",
                HttpMethod.POST,
                requestEntity,
                String.class
        ).getBody();
        logger.info("response:{}",response);
        logger.info("response2:{}",JSON.parseObject(response));
        return "";
    }

}
