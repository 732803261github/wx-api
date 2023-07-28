package com.github.niefy.modules.wx.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.niefy.common.utils.R;
import com.github.niefy.modules.wx.task.StringRedisTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class MjController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    StringRedisTemplateUtil redisUtil;

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/data")
    public R retrieve_messages() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", String.valueOf(redisTemplate.opsForValue().get("authorization")));
        Map<String, Object> map = new HashMap<>();
        HttpEntity requestEntity = new HttpEntity(map, headers);
        String url = String.format("https://discord.com/api/v9/channels/%s/messages?limit=20", redisTemplate.opsForValue().get("channel"));
        String response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        ).getBody();
        List<String> list = new ArrayList<>();
        JSONArray objects = JSON.parseArray(response);
        for (Object object : objects) {
            for (Object attachments : ((JSONObject) object).getJSONArray("attachments")) {
                String string = ((JSONObject) attachments).getString("proxy_url");
                String replace = string.replace("https://media.discordapp.net", "http://www.ai-assistant.com.cn/api/cnd-discordapp");
                list.add(replace + "?Authorization=9998@xunshu");
            }
        }
        return R.ok().put(list);
    }

    @GetMapping(value = "/getImg")
    public R getImg(String taskId) {
        String s = String.valueOf(redisTemplate.opsForValue().get(taskId));
        return R.ok().put(s);
    }

    @PostMapping(value = "/bind/task")
    public void bindTask(String openid, String taskid) {
        log.info("绑定开始，openid={},taskid={}",openid,taskid);
        String key = taskid + "-" + openid;
        redisTemplate.opsForValue().set(key, "", 30, TimeUnit.DAYS);
    }

    @PostMapping(value = "test")
    public void test(){
        List<String> keys = redisUtil.keys("taskdone-*");
        log.info("taskdone-*的所有数据keys是：{}",keys);
        keys.stream().forEach(key->{
            String taskid = key.split("taskdone-")[1];
            log.info("taskid={}",taskid);
            List<String> keys2 = (List<String>) redisTemplate.keys(taskid + "-*");
            log.info("key2={}",keys2);
            keys2.stream().forEach(key2->{
                System.out.println("openid="+key2.split(taskid+"-")[1]);
            });
        });
    }


    public static void main(String[] args) {
        String response = "[\n" +
                "{\n" +
                "\"id\": \"1129312174465101875\",\n" +
                "\"type\": 0,\n" +
                "\"content\": \"**[2652827195710015] Cute little girl drinking cola in the hot summer. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "\"channel_id\": \"1120568025993715764\",\n" +
                "\"author\": {\n" +
                "\"id\": \"936929561302675456\",\n" +
                "\"username\": \"Midjourney Bot\",\n" +
                "\"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "\"discriminator\": \"9282\",\n" +
                "\"public_flags\": 589824,\n" +
                "\"flags\": 589824,\n" +
                "\"bot\": true,复制|下载|删除\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": null,\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": null,\n" +
                "\"banner_color\": null\n" +
                "},\n" +
                "\"attachments\": [\n" +
                "{\n" +
                "\"id\": \"1129312173919846450\",\n" +
                "\"filename\": \"oliverdaniel_2652827195710015_Cute_little_girl_drinking_cola_in_56f6aed4-a822-4799-8da4-f2045b42bae9.png\",\n" +
                "\"size\": 6309005,\n" +
                "\"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129312173919846450/oliverdaniel_2652827195710015_Cute_little_girl_drinking_cola_in_56f6aed4-a822-4799-8da4-f2045b42bae9.png\",\n" +
                "\"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129312173919846450/oliverdaniel_2652827195710015_Cute_little_girl_drinking_cola_in_56f6aed4-a822-4799-8da4-f2045b42bae9.png\",\n" +
                "\"width\": 2048,\n" +
                "\"height\": 2048,\n" +
                "\"content_type\": \"image/png\"\n" +
                "}\n" +
                "],\n" +
                "\"embeds\": [],\n" +
                "\"mentions\": [\n" +
                "{\n" +
                "\"id\": \"1012268717368946718\",\n" +
                "\"username\": \"oliverdaniel\",\n" +
                "\"avatar\": null,\n" +
                "\"discriminator\": \"0\",\n" +
                "\"public_flags\": 0,\n" +
                "\"flags\": 0,\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": \"oliverdaniel\",\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": \"oliverdaniel\",\n" +
                "\"banner_color\": null\n" +
                "}\n" +
                "],\n" +
                "\"mention_roles\": [],\n" +
                "\"pinned\": false,\n" +
                "\"mention_everyone\": false,\n" +
                "\"tts\": false,\n" +
                "\"timestamp\": \"2023-07-14T07:23:23.998000+00:00\",\n" +
                "\"edited_timestamp\": null,\n" +
                "\"flags\": 0,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::1::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U1\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::2::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U2\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::3::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U3\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::4::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U4\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::reroll::0::56f6aed4-a822-4799-8da4-f2045b42bae9::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"emoji\": {\n" +
                "\"name\": \"\uD83D\uDD04\"\n" +
                "}\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::1::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V1\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::2::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V2\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::3::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V3\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::4::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V4\"\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"id\": \"1129311241345703966\",\n" +
                "\"type\": 0,\n" +
                "\"content\": \"**[7569307775639689] Cute little girl drinking cola in the hot summer. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "\"channel_id\": \"1120568025993715764\",\n" +
                "\"author\": {\n" +
                "\"id\": \"936929561302675456\",\n" +
                "\"username\": \"Midjourney Bot\",\n" +
                "\"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "\"discriminator\": \"9282\",\n" +
                "\"public_flags\": 589824,\n" +
                "\"flags\": 589824,\n" +
                "\"bot\": true,\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": null,\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": null,\n" +
                "\"banner_color\": null\n" +
                "},\n" +
                "\"attachments\": [\n" +
                "{\n" +
                "\"id\": \"1129311240771076277\",\n" +
                "\"filename\": \"oliverdaniel_7569307775639689_Cute_little_girl_drinking_cola_in_73caa3d3-4736-414d-9c3d-4c392b799813.png\",\n" +
                "\"size\": 6671458,\n" +
                "\"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129311240771076277/oliverdaniel_7569307775639689_Cute_little_girl_drinking_cola_in_73caa3d3-4736-414d-9c3d-4c392b799813.png\",\n" +
                "\"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129311240771076277/oliverdaniel_7569307775639689_Cute_little_girl_drinking_cola_in_73caa3d3-4736-414d-9c3d-4c392b799813.png\",\n" +
                "\"width\": 2048,\n" +
                "\"height\": 2048,\n" +
                "\"content_type\": \"image/png\"\n" +
                "}\n" +
                "],\n" +
                "\"embeds\": [],\n" +
                "\"mentions\": [\n" +
                "{\n" +
                "\"id\": \"1012268717368946718\",\n" +
                "\"username\": \"oliverdaniel\",\n" +
                "\"avatar\": null,\n" +
                "\"discriminator\": \"0\",\n" +
                "\"public_flags\": 0,\n" +
                "\"flags\": 0,\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": \"oliverdaniel\",\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": \"oliverdaniel\",\n" +
                "\"banner_color\": null\n" +
                "}\n" +
                "],\n" +
                "\"mention_roles\": [],\n" +
                "\"pinned\": false,\n" +
                "\"mention_everyone\": false,\n" +
                "\"tts\": false,\n" +
                "\"timestamp\": \"2023-07-14T07:19:41.525000+00:00\",\n" +
                "\"edited_timestamp\": null,\n" +
                "\"flags\": 0,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::1::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U1\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::2::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U2\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::3::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U3\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::4::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U4\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::reroll::0::73caa3d3-4736-414d-9c3d-4c392b799813::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"emoji\": {\n" +
                "\"name\": \"\uD83D\uDD04\"\n" +
                "}\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::1::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V1\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::2::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V2\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::3::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V3\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::4::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V4\"\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"id\": \"1129257193477247016\",\n" +
                "\"type\": 0,\n" +
                "\"content\": \"**[3654696650374942] Puppy --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "\"channel_id\": \"1120568025993715764\",\n" +
                "\"author\": {\n" +
                "\"id\": \"936929561302675456\",\n" +
                "\"username\": \"Midjourney Bot\",\n" +
                "\"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "\"discriminator\": \"9282\",\n" +
                "\"public_flags\": 589824,\n" +
                "\"flags\": 589824,\n" +
                "\"bot\": true,\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": null,\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": null,\n" +
                "\"banner_color\": null\n" +
                "},\n" +
                "\"attachments\": [\n" +
                "{\n" +
                "\"id\": \"1129257192885862410\",\n" +
                "\"filename\": \"oliverdaniel_3654696650374942_Puppy_f0673eb6-49ad-4101-8dd5-1b3851d96f81.png\",\n" +
                "\"size\": 7577331,\n" +
                "\"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129257192885862410/oliverdaniel_3654696650374942_Puppy_f0673eb6-49ad-4101-8dd5-1b3851d96f81.png\",\n" +
                "\"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129257192885862410/oliverdaniel_3654696650374942_Puppy_f0673eb6-49ad-4101-8dd5-1b3851d96f81.png\",\n" +
                "\"width\": 2048,\n" +
                "\"height\": 2048,\n" +
                "\"content_type\": \"image/png\"\n" +
                "}\n" +
                "],\n" +
                "\"embeds\": [],\n" +
                "\"mentions\": [\n" +
                "{\n" +
                "\"id\": \"1012268717368946718\",\n" +
                "\"username\": \"oliverdaniel\",\n" +
                "\"avatar\": null,\n" +
                "\"discriminator\": \"0\",\n" +
                "\"public_flags\": 0,\n" +
                "\"flags\": 0,\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": \"oliverdaniel\",\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": \"oliverdaniel\",\n" +
                "\"banner_color\": null\n" +
                "}\n" +
                "],\n" +
                "\"mention_roles\": [],\n" +
                "\"pinned\": false,\n" +
                "\"mention_everyone\": false,\n" +
                "\"tts\": false,\n" +
                "\"timestamp\": \"2023-07-14T03:44:55.509000+00:00\",\n" +
                "\"edited_timestamp\": null,\n" +
                "\"flags\": 0,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::1::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U1\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::2::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U2\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::3::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U3\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::4::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U4\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::reroll::0::f0673eb6-49ad-4101-8dd5-1b3851d96f81::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"emoji\": {\n" +
                "\"name\": \"\uD83D\uDD04\"\n" +
                "}\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::1::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V1\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::2::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V2\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::3::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V3\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::4::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V4\"\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"id\": \"1128641343909007401\",\n" +
                "\"type\": 19,\n" +
                "\"content\": \"**[6144234458317582] The spaceship and the airplane collided. --s 750 --v 5.2** - Image #4 <@1012268717368946718>\",\n" +
                "\"channel_id\": \"1120568025993715764\",\n" +
                "\"author\": {\n" +
                "\"id\": \"936929561302675456\",\n" +
                "\"username\": \"Midjourney Bot\",\n" +
                "\"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "\"discriminator\": \"9282\",\n" +
                "\"public_flags\": 589824,\n" +
                "\"flags\": 589824,\n" +
                "\"bot\": true,\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": null,\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": null,\n" +
                "\"banner_color\": null\n" +
                "},\n" +
                "\"attachments\": [\n" +
                "{\n" +
                "\"id\": \"1128641343539916850\",\n" +
                "\"filename\": \"oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c0817eea-4c46-4284-a64a-e36158d72cfe.png\",\n" +
                "\"size\": 1811877,\n" +
                "\"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128641343539916850/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c0817eea-4c46-4284-a64a-e36158d72cfe.png\",\n" +
                "\"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128641343539916850/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c0817eea-4c46-4284-a64a-e36158d72cfe.png\",\n" +
                "\"width\": 1024,\n" +
                "\"height\": 1024,\n" +
                "\"content_type\": \"image/png\"\n" +
                "}\n" +
                "],\n" +
                "\"embeds\": [],\n" +
                "\"mentions\": [\n" +
                "{\n" +
                "\"id\": \"1012268717368946718\",\n" +
                "\"username\": \"oliverdaniel\",\n" +
                "\"avatar\": null,\n" +
                "\"discriminator\": \"0\",\n" +
                "\"public_flags\": 0,\n" +
                "\"flags\": 0,\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": \"oliverdaniel\",\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": \"oliverdaniel\",\n" +
                "\"banner_color\": null\n" +
                "}\n" +
                "],\n" +
                "\"mention_roles\": [],\n" +
                "\"pinned\": false,\n" +
                "\"mention_everyone\": false,\n" +
                "\"tts\": false,\n" +
                "\"timestamp\": \"2023-07-12T10:57:45.530000+00:00\",\n" +
                "\"edited_timestamp\": null,\n" +
                "\"flags\": 0,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::high_variation::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"Vary (Strong)\",\n" +
                "\"emoji\": {\n" +
                "\"name\": \"\uD83E\uDE84\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::low_variation::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"Vary (Subtle)\",\n" +
                "\"emoji\": {\n" +
                "\"name\": \"\uD83E\uDE84\"\n" +
                "}\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::Outpaint::50::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"Zoom Out 2x\",\n" +
                "\"emoji\": {\n" +
                "\"name\": \"\uD83D\uDD0D\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::Outpaint::75::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"Zoom Out 1.5x\",\n" +
                "\"emoji\": {\n" +
                "\"name\": \"\uD83D\uDD0D\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::CustomZoom::c0817eea-4c46-4284-a64a-e36158d72cfe\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"Custom Zoom\",\n" +
                "\"emoji\": {\n" +
                "\"name\": \"\uD83D\uDD0D\"\n" +
                "}\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::pan_left::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"emoji\": {\n" +
                "\"name\": \"⬅\uFE0F\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::pan_right::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"emoji\": {\n" +
                "\"name\": \"➡\uFE0F\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::pan_up::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"emoji\": {\n" +
                "\"name\": \"⬆\uFE0F\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::pan_down::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"emoji\": {\n" +
                "\"name\": \"⬇\uFE0F\"\n" +
                "}\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::BOOKMARK::c0817eea-4c46-4284-a64a-e36158d72cfe\",\n" +
                "\"style\": 2,\n" +
                "\"emoji\": {\n" +
                "\"name\": \"❤\uFE0F\"\n" +
                "}\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"style\": 5,\n" +
                "\"label\": \"Web\",\n" +
                "\"url\": \"https://www.midjourney.com/app/jobs/c0817eea-4c46-4284-a64a-e36158d72cfe/\"\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "],\n" +
                "\"message_reference\": {\n" +
                "\"channel_id\": \"1120568025993715764\",\n" +
                "\"message_id\": \"1128599623762714675\",\n" +
                "\"guild_id\": \"1120568025381355652\"\n" +
                "},\n" +
                "\"referenced_message\": {\n" +
                "\"id\": \"1128599623762714675\",\n" +
                "\"type\": 0,\n" +
                "\"content\": \"**[6144234458317582] The spaceship and the airplane collided. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "\"channel_id\": \"1120568025993715764\",\n" +
                "\"author\": {\n" +
                "\"id\": \"936929561302675456\",\n" +
                "\"username\": \"Midjourney Bot\",\n" +
                "\"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "\"discriminator\": \"9282\",\n" +
                "\"public_flags\": 589824,\n" +
                "\"flags\": 589824,\n" +
                "\"bot\": true,\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": null,\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": null,\n" +
                "\"banner_color\": null\n" +
                "},\n" +
                "\"attachments\": [\n" +
                "{\n" +
                "\"id\": \"1128599623209074708\",\n" +
                "\"filename\": \"oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c09a2935-432a-464c-bb31-6250a418a3c1.png\",\n" +
                "\"size\": 7812030,\n" +
                "\"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128599623209074708/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c09a2935-432a-464c-bb31-6250a418a3c1.png\",\n" +
                "\"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128599623209074708/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c09a2935-432a-464c-bb31-6250a418a3c1.png\",\n" +
                "\"width\": 2048,\n" +
                "\"height\": 2048,\n" +
                "\"content_type\": \"image/png\"\n" +
                "}\n" +
                "],\n" +
                "\"embeds\": [],\n" +
                "\"mentions\": [\n" +
                "{\n" +
                "\"id\": \"1012268717368946718\",\n" +
                "\"username\": \"oliverdaniel\",\n" +
                "\"avatar\": null,\n" +
                "\"discriminator\": \"0\",\n" +
                "\"public_flags\": 0,\n" +
                "\"flags\": 0,\n" +
                "\"banner\": null,\n" +
                "\"accent_color\": null,\n" +
                "\"global_name\": \"oliverdaniel\",\n" +
                "\"avatar_decoration\": null,\n" +
                "\"display_name\": \"oliverdaniel\",\n" +
                "\"banner_color\": null\n" +
                "}\n" +
                "],\n" +
                "\"mention_roles\": [],\n" +
                "\"pinned\": false,\n" +
                "\"mention_everyone\": false,\n" +
                "\"tts\": false,\n" +
                "\"timestamp\": \"2023-07-12T08:11:58.672000+00:00\",\n" +
                "\"edited_timestamp\": null,\n" +
                "\"flags\": 0,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::1::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U1\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::2::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U2\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::3::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"U3\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::upsample::4::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "\"style\": 1,\n" +
                "\"label\": \"U4\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::reroll::0::c09a2935-432a-464c-bb31-6250a418a3c1::SOLO\",\n" +
                "\"style\": 2,\n" +
                "\"emoji\": {\n" +
                "\"name\": \"\uD83D\uDD04\"\n" +
                "}\n" +
                "}\n" +
                "]\n" +
                "},\n" +
                "{\n" +
                "\"type\": 1,\n" +
                "\"components\": [\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::1::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V1\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::2::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V2\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::3::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V3\"\n" +
                "},\n" +
                "{\n" +
                "\"type\": 2,\n" +
                "\"custom_id\": \"MJ::JOB::variation::4::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "\"style\": 2,\n" +
                "\"label\": \"V4\"\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "}\n" +
                "]";
        JSONArray objects = JSON.parseArray(response);
        List<String> list = new ArrayList<>();
//        boolean content = ((JSONObject) objects.get(0)).getString("content").contains("");
        for (Object object : objects) {
            String imgUrl = ((JSONObject) object).getJSONArray("attachments").getJSONObject(0).getString("proxy_url");
            list.add(imgUrl);
        }
    }
}
