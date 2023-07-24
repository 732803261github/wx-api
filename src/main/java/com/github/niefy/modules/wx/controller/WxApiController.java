package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.niefy.common.utils.DateUtils;
import com.github.niefy.common.utils.R;
import com.github.niefy.modules.wx.aspect.Limiting;
import com.github.niefy.modules.wx.entity.WxUser;
import com.github.niefy.modules.wx.service.TemplateMsgService;
import com.github.niefy.modules.wx.service.WxAccountService;
import com.github.niefy.modules.wx.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class WxApiController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private WxAccountService accountService;
    @Autowired
    private WxUserService userService;
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private TemplateMsgService templateMsgService;


    RestTemplate restTemplate = new RestTemplate();

    public static final String appId = "wx0f791e273d673f03";
    String secret = "";

    public String getWxToken() {
        secret = redisTemplate.opsForValue().get("secret").toString();
        String URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + secret;
        if (redisTemplate.opsForValue().get("access_token") == null) {
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
            if (response.getStatusCodeValue() == 200) {
                JSONObject jsonObject = JSON.parseObject(response.getBody());
                redisTemplate.opsForValue().set("access_token", response.getBody(), 90, TimeUnit.SECONDS);
                return jsonObject.getString("access_token");
            }
        } else {
            return JSONObject.parseObject(redisTemplate.opsForValue().get("access_token").toString()).getString("access_token");
        }
        return "";
    }


    @PostMapping("/authInfo")
    public R checkUserSubscribe(String openid) {
        String key = "authInfo::" + openid;
        if (redisTemplate.opsForValue().get(key) == null) {
            WxUser wxUser = userService.getById(openid);
            if (ObjectUtils.isNotEmpty(wxUser)) {
                redisTemplate.opsForValue().set(key, wxUser, 30, TimeUnit.SECONDS);
                return R.ok().put(wxUser);
            }
        } else {
            return R.ok().put(JSON.parseObject(redisTemplate.opsForValue().get(key).toString()));
        }
        return R.error();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        Random random = new Random();
        int randomInt = random.nextInt();
        System.out.println(randomInt);
    }

    @Limiting(limitNum = 2, name = "gencodeLimit")
    @GetMapping(value = "/gencode")
    public R pageAuth() throws UnsupportedEncodingException {
        String token = getWxToken();
        Random random = new Random();
        int randomNum = random.nextInt(900) + 100;
        long time = new Date().getTime();
        long scene_id = (long) randomNum + time;
        String url = String.format("https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s", token);
        JSONObject jsonObject = JSON.parseObject("{\"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": " + scene_id + "}}}");
        ResponseEntity<String> response = restTemplate.postForEntity(url, jsonObject, String.class);
        log.info("response:{}", response);
        if (response.getStatusCodeValue() == 200) {
            JSONObject qrcode = JSONObject.parseObject(response.getBody());
            if (StringUtils.isNotEmpty(qrcode.getString("ticket"))) {
                String tickUrl = String.format("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s", URLEncoder.encode(qrcode.getString("ticket"), "UTF-8"));
                Map<String, Object> map = new HashMap<>();
                map.put("ticket", qrcode.getString("ticket"));
                map.put("tickUrl", tickUrl);
                return R.ok().put(map);
            }
        } else {
            return R.error();
        }
        return R.error();
    }

    @PostMapping(value = "scanres")
    public R scanres(String ticket) {
        if (ObjectUtils.isNotEmpty(redisTemplate.opsForValue().get("ticket::" + ticket))) {
            String openid = redisTemplate.opsForValue().get("ticket::" + ticket).toString();
            return R.ok(openid);
        }
        return R.error();
    }

    @RequestMapping(value = "/infoSend")
    void sendTemplateMsg() {
        String appid = String.valueOf(redisTemplate.opsForValue().get("appid"));
        List<WxMpTemplateData> data = new ArrayList<>();
        data.add(new WxMpTemplateData("character_string2", "3878599093670556"));
        data.add(new WxMpTemplateData("time3", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm")));
        WxMpTemplateMessage wxMpTemplateMessage = WxMpTemplateMessage.builder()
                .templateId("K_WOhj5KoEgBc7MomCHL4wbq6i82ULsyxDDKepVnZVs")
                .url("http://ai-assistant.com.cn/api/cnd-discordapp/attachments/1120568025993715764/1129837389020414062/oliverdaniel_3878599093670556_Swans_fish_in_the_lake_during_the_4a97a352-a0ec-47c0-80a2-c6071031a23b.png?Authorization=9998@xunshu")
                .toUser("o731S6QvW6NlhTkJyGYJNItsu9a8")
                .data(data)
                .build();
        templateMsgService.sendTemplateMsg(wxMpTemplateMessage, appid);
    }
}
