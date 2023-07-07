package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.niefy.common.utils.R;
import com.github.niefy.modules.wx.entity.WxAccount;
import com.github.niefy.modules.wx.entity.WxUser;
import com.github.niefy.modules.wx.service.WxAccountService;
import com.github.niefy.modules.wx.service.WxUserService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
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


    RestTemplate restTemplate = new RestTemplate();


    @GetMapping("/wxtoken")
    public R getWxToken() {
        if (redisTemplate.opsForValue().get("appid") == null) {
            List<WxAccount> list = accountService.list();
            redisTemplate.opsForValue().set("wxappid", list.get(0).getAppid());
            redisTemplate.opsForValue().set("wxsecret", list.get(0).getSecret());
        }
        String appid = redisTemplate.opsForValue().get("wxappid").toString();
        String secret = redisTemplate.opsForValue().get("wxsecret").toString();
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

/*    @PostMapping("/checkOnline")
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
    }*/

    @PostMapping("/check")
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

    @GetMapping(value = "/oauth")
    public String oauth(HttpServletResponse response) throws IOException {
        if (redisTemplate.opsForValue().get("appid") == null) {
            List<WxAccount> list = accountService.list();
            redisTemplate.opsForValue().set("wxappid", list.get(0).getAppid());
            redisTemplate.opsForValue().set("wxsecret", list.get(0).getSecret());
        }
        String appId = redisTemplate.opsForValue().get("wxappid").toString();
        String redirectUri = URLEncoder.encode("http://www.ai-assistant.com.cn/wx/invoke", "UTF-8");
        String authorizeUrl = String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=%s#wechat_redirect", appId, redirectUri, "state");
        return authorizeUrl; //重定向网页
    }

    @GetMapping(value = "/invoke")
    public void invoke(HttpServletRequest request) {
        String code = request.getParameter("code");
        String appid = redisTemplate.opsForValue().get("wxappid").toString();
        String secret = redisTemplate.opsForValue().get("wxsecret").toString();
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + appid +
                "&secret=" + secret +
                "&code=" + code +
                "&grant_type=authorization_code";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCodeValue() == 200) {
            JSONObject jsonObject = JSON.parseObject(response.getBody());
            log.info("jsonObject====" + jsonObject);
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");
            //第三步：拉取用户信息
            String userInfoUrl = "https://api.weixin.qq.com/sns/userinfo?" +
                    "access_token=" + access_token +
                    "&openid=" + openid +
                    "&lang=zh_CN";
            ResponseEntity<String> response2 = restTemplate.getForEntity(userInfoUrl, String.class);
            JSONObject userJson = JSONObject.parseObject(response2.getBody());
            log.info("userJson=====" + userJson);
        }
    }
}
