package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.niefy.common.utils.Constant;
import com.github.niefy.common.utils.DateUtils;
import com.github.niefy.common.utils.R;
import com.github.niefy.common.utils.SHA1Util;
import com.github.niefy.modules.wx.entity.WxAccount;
import com.github.niefy.modules.wx.entity.WxUser;
import com.github.niefy.modules.wx.service.TemplateMsgService;
import com.github.niefy.modules.wx.service.WxAccountService;
import com.github.niefy.modules.wx.service.WxUserService;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    String appId = "wxbf2afefa7dfefa54";
    String secret = "061a3a89b17568868394680c89707d93";

    @GetMapping("/getWxToken")
    public R getWxToken() {
        String URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + secret;
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
        String redirectUri = URLEncoder.encode("http://www.ai-assistant.com.cn/wx/invoke", "UTF-8");
        String authorizeUrl = String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&state=%s#wechat_redirect", appId, redirectUri, "state");
        return authorizeUrl;
    }

    @GetMapping(value = "/invoke")
    public R invoke(HttpServletRequest request) throws UnsupportedEncodingException {
        String code = request.getParameter("code");
        if(StringUtils.isNotEmpty(code)){
            return R.ok().put(code);
        }else {
            return R.error("授权失败");
        }
    }

    @GetMapping("/getSignature")
    @ApiOperation(value = "获取微信分享的签名配置",notes = "微信公众号添加了js安全域名的网站才能加载微信分享")
    public R getShareSignature(HttpServletRequest request,String appid) throws WxErrorException {
        this.wxMpService.switchoverTo(appid);
        Map<String, String> wxMap = new TreeMap<>();
        String wxNoncestr = UUID.randomUUID().toString();
        String wxTimestamp = (System.currentTimeMillis() / 1000) + "";
        wxMap.put("noncestr", wxNoncestr);
        wxMap.put("timestamp", wxTimestamp);
        wxMap.put("jsapi_ticket", wxMpService.getJsapiTicket());
        wxMap.put("url", "http://www.ai-assistant.com.cn");
        // 加密获取signature
        StringBuilder wxBaseString = new StringBuilder();
        wxMap.forEach((key, value) -> wxBaseString.append(key).append("=").append(value).append("&"));
        String wxSignString = wxBaseString.substring(0, wxBaseString.length() - 1);
        // signature
        String wxSignature = SHA1Util.sha1(wxSignString);
        Map<String, String> resMap = new TreeMap<>();
        resMap.put("appId", appid);
        resMap.put("wxTimestamp", wxTimestamp);
        resMap.put("wxNoncestr", wxNoncestr);
        resMap.put("wxSignature", wxSignature);
        return R.ok().put(resMap);
    }
    @GetMapping(value = "getuserInfo")
    public R getuserInfo(String code){
        String url = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",appId,secret,code);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCodeValue() == 200) {
            JSONObject jsonObject = JSON.parseObject(response.getBody());
            log.info("jsonObject====" + jsonObject);
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");
            //第三步：拉取用户信息
            String userInfoUrl = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",access_token,openid);
            ResponseEntity<String> response2 = restTemplate.getForEntity(userInfoUrl, String.class);
            JSONObject userJson = JSONObject.parseObject(response2.getBody());
            return R.ok().put(userJson);
        } else {
            return R.error("系统异常");
        }
    }

//    @GetMapping(value = "/invoke")
//    public R invoke(HttpServletRequest request) {
//        JSONObject userJson = JSONObject.parseObject("{\n" +
//                "    \"country\": \"\",\n" +
//                "    \"province\": \"\",\n" +
//                "    \"city\": \"\",\n" +
//                "    \"openid\": \"o731S6QvW6NlhTkJyGYJNItsu9a8\",\n" +
//                "    \"sex\": 0,\n" +
//                "    \"nickname\": \"nope\",\n" +
//                "    \"headimgurl\": \"https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKXibCpNjT7gCEjSHWm02AzDmXpI0Z5gicw2t7VyS4klQ63NF5v9CNg4oE4vz2zcSlOibQe1C6pjegnQ/132\",\n" +
//                "    \"language\": \"\",\n" +
//                "    \"privilege\": []\n" +
//                "}");
//        return R.ok().put(userJson);
//    }

    @GetMapping(value = "/pcAuth")
    public R pageAuth() throws UnsupportedEncodingException {
        if (redisTemplate.opsForValue().get("qrcode") == null) {
            String token = JSON.parseObject(getWxToken().get("data").toString()).getString("access_token");
            String url = String.format("https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s", token);
            JSONObject jsonObject = JSON.parseObject("{\"action_name\": \"QR_LIMIT_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": \"pcAuth\"}}}");
            ResponseEntity<String> response = restTemplate.postForEntity(url, jsonObject, String.class);
            if (response.getStatusCodeValue() == 200) {
                JSONObject qrcode = JSONObject.parseObject(response.getBody());
                if (StringUtils.isNotEmpty(qrcode.getString("ticket"))) {
                    String tickUrl = String.format("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s", URLEncoder.encode(qrcode.getString("ticket"), "UTF-8"));
                    genQrcode(URLEncoder.encode(qrcode.getString("ticket"), "UTF-8"));
                    ResponseEntity<String> responseEntity = restTemplate.getForEntity(tickUrl, String.class);
                    redisTemplate.opsForValue().set("qrcode", responseEntity.getBody());
                    return R.ok().put(responseEntity);

                }
            } else {
                return R.error();
            }
        } else {
            return R.ok().put(redisTemplate.opsForValue().get("qrcode"));
        }
        return R.ok();
    }

    public void genQrcode(String ticket) {
        URL url = null;
        try {
            //请求的路径
            String qrUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + ticket;
            url = new URL(qrUrl);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream("/Users/chenxiaoming/Desktop/qrcode.jpg");//下载的位置及文件名
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @RequestMapping(value = "/infoSend")
    void sendTemplateMsg() {
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
}
