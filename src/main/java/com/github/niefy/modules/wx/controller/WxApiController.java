package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.niefy.common.utils.R;
import com.github.niefy.modules.wx.entity.WxAccount;
import com.github.niefy.modules.wx.entity.WxUser;
import com.github.niefy.modules.wx.service.WxAccountService;
import com.github.niefy.modules.wx.service.WxUserService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/mj")
public class WxApiController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private WxAccountService accountService;
    @Autowired
    private WxUserService userService;


    RestTemplate restTemplate = new RestTemplate();


    @GetMapping("/wxtoken")
    public R getWxToken() {
        List<WxAccount> list = accountService.list();
        String appid = list.get(0).getAppid();
        String secret = list.get(0).getSecret();
        String URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid + "&secret=" + secret;
        if (redisTemplate.opsForValue().get("wxtoken") == null) {
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
            if (response.getStatusCodeValue() == 200) {
                JSONObject jsonObject = JSON.parseObject(response.getBody());
                redisTemplate.opsForValue().set("wxtoken", response.getBody(), 600, TimeUnit.SECONDS);
                return R.ok().put(jsonObject);
            }
        } else {
            return R.ok().put(JSON.parseObject(redisTemplate.opsForValue().get("wxtoken").toString()));
        }
        return R.ok();
    }

    @PostMapping("/checkUserSubscribeOnline")
    public R checkUserSubscribeOnline(String openid) {
        String key = "checkSubscribeOnline_" + openid;
        if (redisTemplate.opsForValue().get(key) == null) {
            String accessToken = JSON.parseObject(getWxToken().get("data").toString()).getString("access_token");
            String URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + accessToken + "&openid=" + openid + "&lang=zh_CN";
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
            if (response.getStatusCodeValue() == 200) {
                redisTemplate.opsForValue().set(key, response.getBody(), 300, TimeUnit.SECONDS);
                JSONObject jsonObject = JSON.parseObject(response.getBody());
                return R.ok().put(jsonObject);
            }
        } else {
            return R.ok().put(JSON.parseObject(redisTemplate.opsForValue().get(key).toString()));
        }
        return R.ok();
    }

    @PostMapping("/checkUserSubscribe")
    public R checkUserSubscribe(String openid) {
        String key = "checkSubscribe_" + openid;
        if (redisTemplate.opsForValue().get(key) == null) {
            WxUser wxUser = userService.getById(openid);
            if (ObjectUtils.isNotEmpty(wxUser)) {
                redisTemplate.opsForValue().set(key, wxUser, 60, TimeUnit.SECONDS);
                return R.ok().put(wxUser);
            }
        } else {
            return R.ok().put(JSON.parseObject(redisTemplate.opsForValue().get(key).toString()));
        }
        return R.ok();
    }
}
