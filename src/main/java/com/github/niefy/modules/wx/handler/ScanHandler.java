package com.github.niefy.modules.wx.handler;

import com.github.niefy.modules.wx.entity.WxUser;
import com.github.niefy.modules.wx.service.MsgReplyService;
import com.github.niefy.modules.wx.service.WxUserService;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Binary Wang
 */
@Component
public class ScanHandler extends AbstractHandler {
    @Autowired
    MsgReplyService msgReplyService;
    @Autowired
    WxUserService wxUserService;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map,
                                    WxMpService wxMpService, WxSessionManager wxSessionManager) {
        //扫码事件处理
        logger.info("用户扫描带参二维码 OPENID:{},事件:{},ticket:{}",wxMpXmlMessage.getFromUser(), wxMpXmlMessage.getEventKey(),wxMpXmlMessage.getTicket());
        redisTemplate.opsForValue().set("ticket::"+wxMpXmlMessage.getTicket(),wxMpXmlMessage.getFromUser(),2, TimeUnit.SECONDS);
        WxUser wxUser = wxUserService.getById(wxMpXmlMessage.getFromUser());
        if (wxUser.isSubscribe()) {
            logger.info("openid:{},订阅状态:{}", wxUser.getOpenid(), wxUser.isSubscribe());
        }
        String appid = WxMpConfigStorageHolder.get();
        msgReplyService.tryAutoReply(appid, true, wxMpXmlMessage.getFromUser(), wxMpXmlMessage.getEventKey());

        return null;
    }
}
