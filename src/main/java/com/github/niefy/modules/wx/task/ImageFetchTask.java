package com.github.niefy.modules.wx.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ImageFetchTask {

    @Autowired
    RedisTemplate redisTemplate;

    RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0/59 * * * * ?")
    public void getImg() {
        String lastId = String.valueOf(redisTemplate.opsForValue().get("lastId"));
        String authorization = String.valueOf(redisTemplate.opsForValue().get("authorization"));
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", authorization);
        Map<String, Object> map = new HashMap<>();
        HttpEntity requestEntity = new HttpEntity(map, headers);
        String api = "https://discord.com/api/v9/channels/1120568025993715764/messages?limit=50" + (!"null".equals(lastId) ? "&before=" + lastId : "");
        log.info("请求api:{}", api);
        String response = restTemplate.exchange(
                api,
                HttpMethod.GET,
                requestEntity,
                String.class
        ).getBody();
        JSONArray objects = JSON.parseArray(response);
        if (objects.size() > 0) {
            String nextId = ((JSONObject) objects.get(objects.size() - 1)).getString("id");
            log.info("nextId:{},lastId=nextId:{}",nextId,lastId.equals(nextId));
            if (!"null".equals(lastId)) {
                redisTemplate.delete(lastId);
            }
            redisTemplate.opsForValue().set("lastId", nextId, 1, TimeUnit.DAYS);
            for (Object object : objects) {
                if (lastId.equals(nextId)) {
                    redisTemplate.delete("lastId");
                    log.info("循环结束，从最新开始");
                    break;
                }
                for (Object attachments : ((JSONObject) object).getJSONArray("attachments")) {
                    String taskid = ((JSONObject) object).getString("content").split("]")[0].substring(3);
                    boolean isNumeric = taskid.matches("\\d+");
                    if (isNumeric) {
                        String string = ((JSONObject) attachments).getString("proxy_url");
                        String replace = string.replace("https://media.discordapp.net", "http://www.ai-assistant.com.cn/api/cnd-discordapp");
                        String url = replace + "?Authorization=9998@xunshu";
                        redisTemplate.opsForValue().setIfAbsent(taskid, url, 30, TimeUnit.DAYS);
                    } else {
                        log.info("非前端生成，跳过存储:{}", ((JSONObject) object).getString("content"));
                    }
                }
            }
        }
    }

    /*public static void main(String[] args) {
        String response = "[\n" +
                "    {\n" +
                "        \"id\": \"1129700699438059520\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[8262694398550213] The wolf is coming. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129700698750210089\",\n" +
                "                \"filename\": \"oliverdaniel_8262694398550213_The_wolf_is_coming._c65f63de-b743-438f-9137-e6d8e46276ce.png\",\n" +
                "                \"size\": 8095579,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129700698750210089/oliverdaniel_8262694398550213_The_wolf_is_coming._c65f63de-b743-438f-9137-e6d8e46276ce.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129700698750210089/oliverdaniel_8262694398550213_The_wolf_is_coming._c65f63de-b743-438f-9137-e6d8e46276ce.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T09:07:15.570000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::c65f63de-b743-438f-9137-e6d8e46276ce\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::c65f63de-b743-438f-9137-e6d8e46276ce\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::c65f63de-b743-438f-9137-e6d8e46276ce\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::c65f63de-b743-438f-9137-e6d8e46276ce\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::c65f63de-b743-438f-9137-e6d8e46276ce::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::c65f63de-b743-438f-9137-e6d8e46276ce\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::c65f63de-b743-438f-9137-e6d8e46276ce\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::c65f63de-b743-438f-9137-e6d8e46276ce\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::c65f63de-b743-438f-9137-e6d8e46276ce\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129664234700144680\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[9618313360923021] Delicious barbecue --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129664233580285953\",\n" +
                "                \"filename\": \"oliverdaniel_9618313360923021_Delicious_barbecue_72a7ae64-6f8e-473a-8228-83ba62c20e67.png\",\n" +
                "                \"size\": 7530262,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129664233580285953/oliverdaniel_9618313360923021_Delicious_barbecue_72a7ae64-6f8e-473a-8228-83ba62c20e67.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129664233580285953/oliverdaniel_9618313360923021_Delicious_barbecue_72a7ae64-6f8e-473a-8228-83ba62c20e67.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T06:42:21.699000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::72a7ae64-6f8e-473a-8228-83ba62c20e67\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::72a7ae64-6f8e-473a-8228-83ba62c20e67\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::72a7ae64-6f8e-473a-8228-83ba62c20e67\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::72a7ae64-6f8e-473a-8228-83ba62c20e67\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::72a7ae64-6f8e-473a-8228-83ba62c20e67::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::72a7ae64-6f8e-473a-8228-83ba62c20e67\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::72a7ae64-6f8e-473a-8228-83ba62c20e67\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::72a7ae64-6f8e-473a-8228-83ba62c20e67\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::72a7ae64-6f8e-473a-8228-83ba62c20e67\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129641892494188624\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[4063082461298307] The little white rabbit is eating carrots. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129641891818917888\",\n" +
                "                \"filename\": \"oliverdaniel_4063082461298307_The_little_white_rabbit_is_eating_fdb222ef-d962-4d5c-86e6-7f69ea6fcae2.png\",\n" +
                "                \"size\": 7258103,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129641891818917888/oliverdaniel_4063082461298307_The_little_white_rabbit_is_eating_fdb222ef-d962-4d5c-86e6-7f69ea6fcae2.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129641891818917888/oliverdaniel_4063082461298307_The_little_white_rabbit_is_eating_fdb222ef-d962-4d5c-86e6-7f69ea6fcae2.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T05:13:34.902000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::fdb222ef-d962-4d5c-86e6-7f69ea6fcae2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::fdb222ef-d962-4d5c-86e6-7f69ea6fcae2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::fdb222ef-d962-4d5c-86e6-7f69ea6fcae2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::fdb222ef-d962-4d5c-86e6-7f69ea6fcae2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::fdb222ef-d962-4d5c-86e6-7f69ea6fcae2::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::fdb222ef-d962-4d5c-86e6-7f69ea6fcae2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::fdb222ef-d962-4d5c-86e6-7f69ea6fcae2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::fdb222ef-d962-4d5c-86e6-7f69ea6fcae2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::fdb222ef-d962-4d5c-86e6-7f69ea6fcae2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129639242470011000\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[3769927794978820] The little white rabbit is eating a carrot. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129639241870221403\",\n" +
                "                \"filename\": \"oliverdaniel_3769927794978820_The_little_white_rabbit_is_eating_24ca1e74-205e-490e-928f-f349d00e3cde.png\",\n" +
                "                \"size\": 7394760,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129639241870221403/oliverdaniel_3769927794978820_The_little_white_rabbit_is_eating_24ca1e74-205e-490e-928f-f349d00e3cde.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129639241870221403/oliverdaniel_3769927794978820_The_little_white_rabbit_is_eating_24ca1e74-205e-490e-928f-f349d00e3cde.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T05:03:03.087000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::24ca1e74-205e-490e-928f-f349d00e3cde\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::24ca1e74-205e-490e-928f-f349d00e3cde\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::24ca1e74-205e-490e-928f-f349d00e3cde\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::24ca1e74-205e-490e-928f-f349d00e3cde\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::24ca1e74-205e-490e-928f-f349d00e3cde::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::24ca1e74-205e-490e-928f-f349d00e3cde\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::24ca1e74-205e-490e-928f-f349d00e3cde\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::24ca1e74-205e-490e-928f-f349d00e3cde\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::24ca1e74-205e-490e-928f-f349d00e3cde\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129636678169018449\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[9445403642691040] The little white rabbit is eating carrots. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129636677514698873\",\n" +
                "                \"filename\": \"oliverdaniel_9445403642691040_The_little_white_rabbit_is_eating_00961531-8507-4d19-b7c8-30dfad60d0ea.png\",\n" +
                "                \"size\": 7167011,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129636677514698873/oliverdaniel_9445403642691040_The_little_white_rabbit_is_eating_00961531-8507-4d19-b7c8-30dfad60d0ea.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129636677514698873/oliverdaniel_9445403642691040_The_little_white_rabbit_is_eating_00961531-8507-4d19-b7c8-30dfad60d0ea.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T04:52:51.710000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::00961531-8507-4d19-b7c8-30dfad60d0ea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::00961531-8507-4d19-b7c8-30dfad60d0ea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::00961531-8507-4d19-b7c8-30dfad60d0ea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::00961531-8507-4d19-b7c8-30dfad60d0ea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::00961531-8507-4d19-b7c8-30dfad60d0ea::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::00961531-8507-4d19-b7c8-30dfad60d0ea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::00961531-8507-4d19-b7c8-30dfad60d0ea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::00961531-8507-4d19-b7c8-30dfad60d0ea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::00961531-8507-4d19-b7c8-30dfad60d0ea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129634677964152893\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[9320856763854124] apple, forest,big bad wolf, wood,unreal engine, cinematic lighting, UHD, super detail --aspect 2:3 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129634677595066378\",\n" +
                "                \"filename\": \"oliverdaniel_9320856763854124_apple_forestbig_bad_wolf_woodunre_77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7.png\",\n" +
                "                \"size\": 9093518,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129634677595066378/oliverdaniel_9320856763854124_apple_forestbig_bad_wolf_woodunre_77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129634677595066378/oliverdaniel_9320856763854124_apple_forestbig_bad_wolf_woodunre_77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7.png\",\n" +
                "                \"width\": 1792,\n" +
                "                \"height\": 2688,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T04:44:54.824000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::77f2f7cc-a9eb-4849-aac5-a1c6dd4c9cd7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129633422122430544\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[6205625088506705] apple, forest,big bad wolf, wood,unreal engine, cinematic lighting, UHD, super detail --aspect 2:3 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129633421589749800\",\n" +
                "                \"filename\": \"oliverdaniel_6205625088506705_apple_forestbig_bad_wolf_woodunre_10fd1d92-0fcd-407d-b8d0-39bb49d3feea.png\",\n" +
                "                \"size\": 9408632,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129633421589749800/oliverdaniel_6205625088506705_apple_forestbig_bad_wolf_woodunre_10fd1d92-0fcd-407d-b8d0-39bb49d3feea.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129633421589749800/oliverdaniel_6205625088506705_apple_forestbig_bad_wolf_woodunre_10fd1d92-0fcd-407d-b8d0-39bb49d3feea.png\",\n" +
                "                \"width\": 1792,\n" +
                "                \"height\": 2688,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T04:39:55.408000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::10fd1d92-0fcd-407d-b8d0-39bb49d3feea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::10fd1d92-0fcd-407d-b8d0-39bb49d3feea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::10fd1d92-0fcd-407d-b8d0-39bb49d3feea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::10fd1d92-0fcd-407d-b8d0-39bb49d3feea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::10fd1d92-0fcd-407d-b8d0-39bb49d3feea::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::10fd1d92-0fcd-407d-b8d0-39bb49d3feea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::10fd1d92-0fcd-407d-b8d0-39bb49d3feea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::10fd1d92-0fcd-407d-b8d0-39bb49d3feea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::10fd1d92-0fcd-407d-b8d0-39bb49d3feea\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129627427988066365\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[9305125481113265] apple, forest,big bad wolf, wood,unreal engine, cinematic lighting, UHD, super detail --aspect 2:3 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129627427602174012\",\n" +
                "                \"filename\": \"oliverdaniel_9305125481113265_apple_forestbig_bad_wolf_woodunre_4cede021-94c3-4830-959d-85d8c68a9b6b.png\",\n" +
                "                \"size\": 9102086,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129627427602174012/oliverdaniel_9305125481113265_apple_forestbig_bad_wolf_woodunre_4cede021-94c3-4830-959d-85d8c68a9b6b.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129627427602174012/oliverdaniel_9305125481113265_apple_forestbig_bad_wolf_woodunre_4cede021-94c3-4830-959d-85d8c68a9b6b.png\",\n" +
                "                \"width\": 1792,\n" +
                "                \"height\": 2688,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T04:16:06.295000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::4cede021-94c3-4830-959d-85d8c68a9b6b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::4cede021-94c3-4830-959d-85d8c68a9b6b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::4cede021-94c3-4830-959d-85d8c68a9b6b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::4cede021-94c3-4830-959d-85d8c68a9b6b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::4cede021-94c3-4830-959d-85d8c68a9b6b::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::4cede021-94c3-4830-959d-85d8c68a9b6b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::4cede021-94c3-4830-959d-85d8c68a9b6b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::4cede021-94c3-4830-959d-85d8c68a9b6b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::4cede021-94c3-4830-959d-85d8c68a9b6b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129625784470999042\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[1133727848508558] apple, forest,big bad wolf, wood,unreal engine, cinematic lighting, UHD, super detail --aspect 2:3 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129625783736999946\",\n" +
                "                \"filename\": \"oliverdaniel_1133727848508558_apple_forestbig_bad_wolf_woodunre_6427af2f-262e-4014-9e93-d8db4cf08d92.png\",\n" +
                "                \"size\": 10480433,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129625783736999946/oliverdaniel_1133727848508558_apple_forestbig_bad_wolf_woodunre_6427af2f-262e-4014-9e93-d8db4cf08d92.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129625783736999946/oliverdaniel_1133727848508558_apple_forestbig_bad_wolf_woodunre_6427af2f-262e-4014-9e93-d8db4cf08d92.png\",\n" +
                "                \"width\": 1792,\n" +
                "                \"height\": 2688,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T04:09:34.450000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::6427af2f-262e-4014-9e93-d8db4cf08d92\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::6427af2f-262e-4014-9e93-d8db4cf08d92\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::6427af2f-262e-4014-9e93-d8db4cf08d92\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::6427af2f-262e-4014-9e93-d8db4cf08d92\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::6427af2f-262e-4014-9e93-d8db4cf08d92::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::6427af2f-262e-4014-9e93-d8db4cf08d92\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::6427af2f-262e-4014-9e93-d8db4cf08d92\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::6427af2f-262e-4014-9e93-d8db4cf08d92\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::6427af2f-262e-4014-9e93-d8db4cf08d92\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129624928438734988\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[1901450771704728] apple, forest,big bad wolf, wood,unreal engine, cinematic lighting, UHD, super detail --aspect 2:3 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129624927780208720\",\n" +
                "                \"filename\": \"oliverdaniel_1901450771704728_apple_forestbig_bad_wolf_woodunre_c85275fd-6c7f-459e-a983-95b2e1addb6e.png\",\n" +
                "                \"size\": 9502013,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129624927780208720/oliverdaniel_1901450771704728_apple_forestbig_bad_wolf_woodunre_c85275fd-6c7f-459e-a983-95b2e1addb6e.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129624927780208720/oliverdaniel_1901450771704728_apple_forestbig_bad_wolf_woodunre_c85275fd-6c7f-459e-a983-95b2e1addb6e.png\",\n" +
                "                \"width\": 1792,\n" +
                "                \"height\": 2688,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T04:06:10.356000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::c85275fd-6c7f-459e-a983-95b2e1addb6e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::c85275fd-6c7f-459e-a983-95b2e1addb6e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::c85275fd-6c7f-459e-a983-95b2e1addb6e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::c85275fd-6c7f-459e-a983-95b2e1addb6e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::c85275fd-6c7f-459e-a983-95b2e1addb6e::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::c85275fd-6c7f-459e-a983-95b2e1addb6e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::c85275fd-6c7f-459e-a983-95b2e1addb6e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::c85275fd-6c7f-459e-a983-95b2e1addb6e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::c85275fd-6c7f-459e-a983-95b2e1addb6e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129620713674903552\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[8438848466994352] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129620712907350076\",\n" +
                "                \"filename\": \"oliverdaniel_8438848466994352__3861b680-1e07-420a-97f3-a8ba8954b7e4.png\",\n" +
                "                \"size\": 8461250,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129620712907350076/oliverdaniel_8438848466994352__3861b680-1e07-420a-97f3-a8ba8954b7e4.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129620712907350076/oliverdaniel_8438848466994352__3861b680-1e07-420a-97f3-a8ba8954b7e4.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:49:25.478000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::3861b680-1e07-420a-97f3-a8ba8954b7e4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::3861b680-1e07-420a-97f3-a8ba8954b7e4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::3861b680-1e07-420a-97f3-a8ba8954b7e4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::3861b680-1e07-420a-97f3-a8ba8954b7e4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::3861b680-1e07-420a-97f3-a8ba8954b7e4::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::3861b680-1e07-420a-97f3-a8ba8954b7e4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::3861b680-1e07-420a-97f3-a8ba8954b7e4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::3861b680-1e07-420a-97f3-a8ba8954b7e4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::3861b680-1e07-420a-97f3-a8ba8954b7e4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129620326414823505\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[4907129110591918] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129620326062510090\",\n" +
                "                \"filename\": \"oliverdaniel_4907129110591918__772b0e36-414a-40ef-95df-c7872a595b0d.png\",\n" +
                "                \"size\": 7761015,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129620326062510090/oliverdaniel_4907129110591918__772b0e36-414a-40ef-95df-c7872a595b0d.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129620326062510090/oliverdaniel_4907129110591918__772b0e36-414a-40ef-95df-c7872a595b0d.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:47:53.148000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::772b0e36-414a-40ef-95df-c7872a595b0d\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::772b0e36-414a-40ef-95df-c7872a595b0d\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::772b0e36-414a-40ef-95df-c7872a595b0d\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::772b0e36-414a-40ef-95df-c7872a595b0d\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::772b0e36-414a-40ef-95df-c7872a595b0d::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::772b0e36-414a-40ef-95df-c7872a595b0d\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::772b0e36-414a-40ef-95df-c7872a595b0d\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::772b0e36-414a-40ef-95df-c7872a595b0d\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::772b0e36-414a-40ef-95df-c7872a595b0d\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129620273663062046\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[8278429554534978] Vincent van Gogh, Sunflowers --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129620272899690506\",\n" +
                "                \"filename\": \"oliverdaniel_8278429554534978_Vincent_van_Gogh_Sunflowers_bc1315f3-845b-43f4-a3cb-e3c5f35a654b.png\",\n" +
                "                \"size\": 8833597,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129620272899690506/oliverdaniel_8278429554534978_Vincent_van_Gogh_Sunflowers_bc1315f3-845b-43f4-a3cb-e3c5f35a654b.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129620272899690506/oliverdaniel_8278429554534978_Vincent_van_Gogh_Sunflowers_bc1315f3-845b-43f4-a3cb-e3c5f35a654b.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:47:40.571000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::bc1315f3-845b-43f4-a3cb-e3c5f35a654b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::bc1315f3-845b-43f4-a3cb-e3c5f35a654b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::bc1315f3-845b-43f4-a3cb-e3c5f35a654b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::bc1315f3-845b-43f4-a3cb-e3c5f35a654b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::bc1315f3-845b-43f4-a3cb-e3c5f35a654b::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::bc1315f3-845b-43f4-a3cb-e3c5f35a654b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::bc1315f3-845b-43f4-a3cb-e3c5f35a654b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::bc1315f3-845b-43f4-a3cb-e3c5f35a654b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::bc1315f3-845b-43f4-a3cb-e3c5f35a654b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129620043060228126\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[9493726308799398] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129620042489794610\",\n" +
                "                \"filename\": \"oliverdaniel_9493726308799398__70a861c0-d173-4421-8961-8711e648d11e.png\",\n" +
                "                \"size\": 7904993,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129620042489794610/oliverdaniel_9493726308799398__70a861c0-d173-4421-8961-8711e648d11e.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129620042489794610/oliverdaniel_9493726308799398__70a861c0-d173-4421-8961-8711e648d11e.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:46:45.591000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::70a861c0-d173-4421-8961-8711e648d11e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::70a861c0-d173-4421-8961-8711e648d11e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::70a861c0-d173-4421-8961-8711e648d11e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::70a861c0-d173-4421-8961-8711e648d11e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::70a861c0-d173-4421-8961-8711e648d11e::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::70a861c0-d173-4421-8961-8711e648d11e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::70a861c0-d173-4421-8961-8711e648d11e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::70a861c0-d173-4421-8961-8711e648d11e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::70a861c0-d173-4421-8961-8711e648d11e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129619751975518248\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[4834439444949929] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129619751379939469\",\n" +
                "                \"filename\": \"oliverdaniel_4834439444949929__96eaa477-7caa-408d-9a66-52776d06afca.png\",\n" +
                "                \"size\": 7874146,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129619751379939469/oliverdaniel_4834439444949929__96eaa477-7caa-408d-9a66-52776d06afca.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129619751379939469/oliverdaniel_4834439444949929__96eaa477-7caa-408d-9a66-52776d06afca.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:45:36.191000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::96eaa477-7caa-408d-9a66-52776d06afca\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::96eaa477-7caa-408d-9a66-52776d06afca\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::96eaa477-7caa-408d-9a66-52776d06afca\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::96eaa477-7caa-408d-9a66-52776d06afca\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::96eaa477-7caa-408d-9a66-52776d06afca::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::96eaa477-7caa-408d-9a66-52776d06afca\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::96eaa477-7caa-408d-9a66-52776d06afca\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::96eaa477-7caa-408d-9a66-52776d06afca\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::96eaa477-7caa-408d-9a66-52776d06afca\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129619552628637726\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[3199660581715672] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129619552033050654\",\n" +
                "                \"filename\": \"oliverdaniel_3199660581715672__7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9.png\",\n" +
                "                \"size\": 8156695,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129619552033050654/oliverdaniel_3199660581715672__7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129619552033050654/oliverdaniel_3199660581715672__7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:44:48.663000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::7d863a4c-2e28-448e-8d1c-a9ff73cd6ae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129619301247234168\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[6567170702519352] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129619300488052786\",\n" +
                "                \"filename\": \"oliverdaniel_6567170702519352__05c55b65-9663-4121-a8f4-745a6db84b35.png\",\n" +
                "                \"size\": 7812971,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129619300488052786/oliverdaniel_6567170702519352__05c55b65-9663-4121-a8f4-745a6db84b35.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129619300488052786/oliverdaniel_6567170702519352__05c55b65-9663-4121-a8f4-745a6db84b35.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:43:48.729000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::05c55b65-9663-4121-a8f4-745a6db84b35\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::05c55b65-9663-4121-a8f4-745a6db84b35\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::05c55b65-9663-4121-a8f4-745a6db84b35\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::05c55b65-9663-4121-a8f4-745a6db84b35\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::05c55b65-9663-4121-a8f4-745a6db84b35::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::05c55b65-9663-4121-a8f4-745a6db84b35\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::05c55b65-9663-4121-a8f4-745a6db84b35\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::05c55b65-9663-4121-a8f4-745a6db84b35\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::05c55b65-9663-4121-a8f4-745a6db84b35\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129618992420618291\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[5849703656680051] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129618991732764783\",\n" +
                "                \"filename\": \"oliverdaniel_5849703656680051__87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05.png\",\n" +
                "                \"size\": 8197236,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129618991732764783/oliverdaniel_5849703656680051__87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129618991732764783/oliverdaniel_5849703656680051__87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:42:35.099000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::87cf2ff3-78d2-4d75-ba34-8a78ad7b4f05\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129618871188455574\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[2209548047964832] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129618870563524628\",\n" +
                "                \"filename\": \"oliverdaniel_2209548047964832__07da0044-c5d1-45af-9565-ad2ed59afe55.png\",\n" +
                "                \"size\": 8284586,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129618870563524628/oliverdaniel_2209548047964832__07da0044-c5d1-45af-9565-ad2ed59afe55.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129618870563524628/oliverdaniel_2209548047964832__07da0044-c5d1-45af-9565-ad2ed59afe55.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:42:06.195000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::07da0044-c5d1-45af-9565-ad2ed59afe55\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::07da0044-c5d1-45af-9565-ad2ed59afe55\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::07da0044-c5d1-45af-9565-ad2ed59afe55\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::07da0044-c5d1-45af-9565-ad2ed59afe55\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::07da0044-c5d1-45af-9565-ad2ed59afe55::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::07da0044-c5d1-45af-9565-ad2ed59afe55\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::07da0044-c5d1-45af-9565-ad2ed59afe55\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::07da0044-c5d1-45af-9565-ad2ed59afe55\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::07da0044-c5d1-45af-9565-ad2ed59afe55\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129618538177507440\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[6779720578448679] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129618537695154237\",\n" +
                "                \"filename\": \"oliverdaniel_6779720578448679__5dbc45bc-72b2-466c-81d3-030d4afdb4cd.png\",\n" +
                "                \"size\": 8379953,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129618537695154237/oliverdaniel_6779720578448679__5dbc45bc-72b2-466c-81d3-030d4afdb4cd.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129618537695154237/oliverdaniel_6779720578448679__5dbc45bc-72b2-466c-81d3-030d4afdb4cd.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:40:46.799000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::5dbc45bc-72b2-466c-81d3-030d4afdb4cd\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::5dbc45bc-72b2-466c-81d3-030d4afdb4cd\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::5dbc45bc-72b2-466c-81d3-030d4afdb4cd\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::5dbc45bc-72b2-466c-81d3-030d4afdb4cd\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::5dbc45bc-72b2-466c-81d3-030d4afdb4cd::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::5dbc45bc-72b2-466c-81d3-030d4afdb4cd\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::5dbc45bc-72b2-466c-81d3-030d4afdb4cd\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::5dbc45bc-72b2-466c-81d3-030d4afdb4cd\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::5dbc45bc-72b2-466c-81d3-030d4afdb4cd\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129618254655135784\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[0003546747094453] 梵高，向日葵 --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129618253845647512\",\n" +
                "                \"filename\": \"oliverdaniel_0003546747094453__72d47c7d-789f-422c-85fb-088fe67b62cf.png\",\n" +
                "                \"size\": 8024813,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129618253845647512/oliverdaniel_0003546747094453__72d47c7d-789f-422c-85fb-088fe67b62cf.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129618253845647512/oliverdaniel_0003546747094453__72d47c7d-789f-422c-85fb-088fe67b62cf.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:39:39.202000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::72d47c7d-789f-422c-85fb-088fe67b62cf\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::72d47c7d-789f-422c-85fb-088fe67b62cf\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::72d47c7d-789f-422c-85fb-088fe67b62cf\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::72d47c7d-789f-422c-85fb-088fe67b62cf\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::72d47c7d-789f-422c-85fb-088fe67b62cf::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::72d47c7d-789f-422c-85fb-088fe67b62cf\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::72d47c7d-789f-422c-85fb-088fe67b62cf\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::72d47c7d-789f-422c-85fb-088fe67b62cf\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::72d47c7d-789f-422c-85fb-088fe67b62cf\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129618199936249886\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[4461412729241137] Vincent van Gogh, Sunflowers --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129618199168688278\",\n" +
                "                \"filename\": \"oliverdaniel_4461412729241137_Vincent_van_Gogh_Sunflowers_bd742d4c-f961-4ded-940b-05629aadeb2f.png\",\n" +
                "                \"size\": 8996675,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129618199168688278/oliverdaniel_4461412729241137_Vincent_van_Gogh_Sunflowers_bd742d4c-f961-4ded-940b-05629aadeb2f.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129618199168688278/oliverdaniel_4461412729241137_Vincent_van_Gogh_Sunflowers_bd742d4c-f961-4ded-940b-05629aadeb2f.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:39:26.156000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::bd742d4c-f961-4ded-940b-05629aadeb2f\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::bd742d4c-f961-4ded-940b-05629aadeb2f\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::bd742d4c-f961-4ded-940b-05629aadeb2f\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::bd742d4c-f961-4ded-940b-05629aadeb2f\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::bd742d4c-f961-4ded-940b-05629aadeb2f::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::bd742d4c-f961-4ded-940b-05629aadeb2f\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::bd742d4c-f961-4ded-940b-05629aadeb2f\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::bd742d4c-f961-4ded-940b-05629aadeb2f\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::bd742d4c-f961-4ded-940b-05629aadeb2f\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129617914979430472\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[8930338666446729] Vincent van Gogh, Sunflowers --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129617914232848504\",\n" +
                "                \"filename\": \"oliverdaniel_8930338666446729_Vincent_van_Gogh_Sunflowers_e279c256-4b05-4b94-9a22-8c54f611e7ec.png\",\n" +
                "                \"size\": 8832716,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129617914232848504/oliverdaniel_8930338666446729_Vincent_van_Gogh_Sunflowers_e279c256-4b05-4b94-9a22-8c54f611e7ec.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129617914232848504/oliverdaniel_8930338666446729_Vincent_van_Gogh_Sunflowers_e279c256-4b05-4b94-9a22-8c54f611e7ec.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:38:18.217000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::e279c256-4b05-4b94-9a22-8c54f611e7ec\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::e279c256-4b05-4b94-9a22-8c54f611e7ec\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::e279c256-4b05-4b94-9a22-8c54f611e7ec\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::e279c256-4b05-4b94-9a22-8c54f611e7ec\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::e279c256-4b05-4b94-9a22-8c54f611e7ec::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::e279c256-4b05-4b94-9a22-8c54f611e7ec\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::e279c256-4b05-4b94-9a22-8c54f611e7ec\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::e279c256-4b05-4b94-9a22-8c54f611e7ec\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::e279c256-4b05-4b94-9a22-8c54f611e7ec\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129613593176985640\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[5252098040755625] Vincent van Gogh, Sunflowers --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129613592631713912\",\n" +
                "                \"filename\": \"oliverdaniel_5252098040755625_Vincent_van_Gogh_Sunflowers_e46afa8d-bd2e-42ee-baf9-310f0d5bb3db.png\",\n" +
                "                \"size\": 8967052,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129613592631713912/oliverdaniel_5252098040755625_Vincent_van_Gogh_Sunflowers_e46afa8d-bd2e-42ee-baf9-310f0d5bb3db.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129613592631713912/oliverdaniel_5252098040755625_Vincent_van_Gogh_Sunflowers_e46afa8d-bd2e-42ee-baf9-310f0d5bb3db.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:21:07.819000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::e46afa8d-bd2e-42ee-baf9-310f0d5bb3db\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::e46afa8d-bd2e-42ee-baf9-310f0d5bb3db\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::e46afa8d-bd2e-42ee-baf9-310f0d5bb3db\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::e46afa8d-bd2e-42ee-baf9-310f0d5bb3db\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::e46afa8d-bd2e-42ee-baf9-310f0d5bb3db::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::e46afa8d-bd2e-42ee-baf9-310f0d5bb3db\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::e46afa8d-bd2e-42ee-baf9-310f0d5bb3db\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::e46afa8d-bd2e-42ee-baf9-310f0d5bb3db\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::e46afa8d-bd2e-42ee-baf9-310f0d5bb3db\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129611848031940619\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[6460302792305210] The little boy is pulling weeds in the garden. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129611847453130752\",\n" +
                "                \"filename\": \"oliverdaniel_6460302792305210_The_little_boy_is_pulling_weeds_i_1c610598-a518-461c-b15f-813d9d967c00.png\",\n" +
                "                \"size\": 7478135,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129611847453130752/oliverdaniel_6460302792305210_The_little_boy_is_pulling_weeds_i_1c610598-a518-461c-b15f-813d9d967c00.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129611847453130752/oliverdaniel_6460302792305210_The_little_boy_is_pulling_weeds_i_1c610598-a518-461c-b15f-813d9d967c00.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:14:11.744000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::1c610598-a518-461c-b15f-813d9d967c00\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::1c610598-a518-461c-b15f-813d9d967c00\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::1c610598-a518-461c-b15f-813d9d967c00\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::1c610598-a518-461c-b15f-813d9d967c00\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::1c610598-a518-461c-b15f-813d9d967c00::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::1c610598-a518-461c-b15f-813d9d967c00\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::1c610598-a518-461c-b15f-813d9d967c00\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::1c610598-a518-461c-b15f-813d9d967c00\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::1c610598-a518-461c-b15f-813d9d967c00\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129609005698920538\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**Content/Story: Step into a cyberpunk dystopia where Werner Herzog is reimagined as a formidable cyborg. Status: His daunting metallic form, a reflection of oil painting with thick brushstrokes, looms large in the city's heart. Perspectives: The narrative unfolds from a low angle view, capturing the raw power and intricate details. Shooting Technique and Elements: A masterful blend of film elements and hyperrealistic details. Special Words: Cinematic, hyper detailed. By Artists: John Waters. Aspect Ratio: --ar 2:3 --s 250 --style raw --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129609005052993567\",\n" +
                "                \"filename\": \"oliverdaniel_ContentStory_Step_into_a_cyberpunk_dystopia_where__69f087e1-e236-4dc3-b6aa-c783b37016e3.png\",\n" +
                "                \"size\": 8881892,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129609005052993567/oliverdaniel_ContentStory_Step_into_a_cyberpunk_dystopia_where__69f087e1-e236-4dc3-b6aa-c783b37016e3.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129609005052993567/oliverdaniel_ContentStory_Step_into_a_cyberpunk_dystopia_where__69f087e1-e236-4dc3-b6aa-c783b37016e3.png\",\n" +
                "                \"width\": 1792,\n" +
                "                \"height\": 2688,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-15T03:02:54.079000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::69f087e1-e236-4dc3-b6aa-c783b37016e3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::69f087e1-e236-4dc3-b6aa-c783b37016e3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::69f087e1-e236-4dc3-b6aa-c783b37016e3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::69f087e1-e236-4dc3-b6aa-c783b37016e3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::69f087e1-e236-4dc3-b6aa-c783b37016e3::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::69f087e1-e236-4dc3-b6aa-c783b37016e3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::69f087e1-e236-4dc3-b6aa-c783b37016e3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::69f087e1-e236-4dc3-b6aa-c783b37016e3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::69f087e1-e236-4dc3-b6aa-c783b37016e3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129312174465101875\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[2652827195710015] Cute little girl drinking cola in the hot summer. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129312173919846450\",\n" +
                "                \"filename\": \"oliverdaniel_2652827195710015_Cute_little_girl_drinking_cola_in_56f6aed4-a822-4799-8da4-f2045b42bae9.png\",\n" +
                "                \"size\": 6309005,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129312173919846450/oliverdaniel_2652827195710015_Cute_little_girl_drinking_cola_in_56f6aed4-a822-4799-8da4-f2045b42bae9.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129312173919846450/oliverdaniel_2652827195710015_Cute_little_girl_drinking_cola_in_56f6aed4-a822-4799-8da4-f2045b42bae9.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-14T07:23:23.998000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::56f6aed4-a822-4799-8da4-f2045b42bae9::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::56f6aed4-a822-4799-8da4-f2045b42bae9\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129311241345703966\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[7569307775639689] Cute little girl drinking cola in the hot summer. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129311240771076277\",\n" +
                "                \"filename\": \"oliverdaniel_7569307775639689_Cute_little_girl_drinking_cola_in_73caa3d3-4736-414d-9c3d-4c392b799813.png\",\n" +
                "                \"size\": 6671458,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129311240771076277/oliverdaniel_7569307775639689_Cute_little_girl_drinking_cola_in_73caa3d3-4736-414d-9c3d-4c392b799813.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129311240771076277/oliverdaniel_7569307775639689_Cute_little_girl_drinking_cola_in_73caa3d3-4736-414d-9c3d-4c392b799813.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-14T07:19:41.525000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::73caa3d3-4736-414d-9c3d-4c392b799813::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::73caa3d3-4736-414d-9c3d-4c392b799813\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1129257193477247016\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[3654696650374942] Puppy --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1129257192885862410\",\n" +
                "                \"filename\": \"oliverdaniel_3654696650374942_Puppy_f0673eb6-49ad-4101-8dd5-1b3851d96f81.png\",\n" +
                "                \"size\": 7577331,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1129257192885862410/oliverdaniel_3654696650374942_Puppy_f0673eb6-49ad-4101-8dd5-1b3851d96f81.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1129257192885862410/oliverdaniel_3654696650374942_Puppy_f0673eb6-49ad-4101-8dd5-1b3851d96f81.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-14T03:44:55.509000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::f0673eb6-49ad-4101-8dd5-1b3851d96f81::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::f0673eb6-49ad-4101-8dd5-1b3851d96f81\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1128641343909007401\",\n" +
                "        \"type\": 19,\n" +
                "        \"content\": \"**[6144234458317582] The spaceship and the airplane collided. --s 750 --v 5.2** - Image #4 <@1012268717368946718>\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1128641343539916850\",\n" +
                "                \"filename\": \"oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c0817eea-4c46-4284-a64a-e36158d72cfe.png\",\n" +
                "                \"size\": 1811877,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128641343539916850/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c0817eea-4c46-4284-a64a-e36158d72cfe.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128641343539916850/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c0817eea-4c46-4284-a64a-e36158d72cfe.png\",\n" +
                "                \"width\": 1024,\n" +
                "                \"height\": 1024,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-12T10:57:45.530000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::high_variation::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"Vary (Strong)\",\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83E\uDE84\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::low_variation::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"Vary (Subtle)\",\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83E\uDE84\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::Outpaint::50::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"Zoom Out 2x\",\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD0D\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::Outpaint::75::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"Zoom Out 1.5x\",\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD0D\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::CustomZoom::c0817eea-4c46-4284-a64a-e36158d72cfe\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"Custom Zoom\",\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD0D\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::pan_left::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"⬅\uFE0F\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::pan_right::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"➡\uFE0F\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::pan_up::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"⬆\uFE0F\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::pan_down::1::c0817eea-4c46-4284-a64a-e36158d72cfe::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"⬇\uFE0F\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::BOOKMARK::c0817eea-4c46-4284-a64a-e36158d72cfe\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"❤\uFE0F\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"style\": 5,\n" +
                "                        \"label\": \"Web\",\n" +
                "                        \"url\": \"https://www.midjourney.com/app/jobs/c0817eea-4c46-4284-a64a-e36158d72cfe/\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"message_reference\": {\n" +
                "            \"channel_id\": \"1120568025993715764\",\n" +
                "            \"message_id\": \"1128599623762714675\",\n" +
                "            \"guild_id\": \"1120568025381355652\"\n" +
                "        },\n" +
                "        \"referenced_message\": {\n" +
                "            \"id\": \"1128599623762714675\",\n" +
                "            \"type\": 0,\n" +
                "            \"content\": \"**[6144234458317582] The spaceship and the airplane collided. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "            \"channel_id\": \"1120568025993715764\",\n" +
                "            \"author\": {\n" +
                "                \"id\": \"936929561302675456\",\n" +
                "                \"username\": \"Midjourney Bot\",\n" +
                "                \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "                \"discriminator\": \"9282\",\n" +
                "                \"public_flags\": 589824,\n" +
                "                \"flags\": 589824,\n" +
                "                \"bot\": true,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": null,\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": null,\n" +
                "                \"banner_color\": null\n" +
                "            },\n" +
                "            \"attachments\": [\n" +
                "                {\n" +
                "                    \"id\": \"1128599623209074708\",\n" +
                "                    \"filename\": \"oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c09a2935-432a-464c-bb31-6250a418a3c1.png\",\n" +
                "                    \"size\": 7812030,\n" +
                "                    \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128599623209074708/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c09a2935-432a-464c-bb31-6250a418a3c1.png\",\n" +
                "                    \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128599623209074708/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c09a2935-432a-464c-bb31-6250a418a3c1.png\",\n" +
                "                    \"width\": 2048,\n" +
                "                    \"height\": 2048,\n" +
                "                    \"content_type\": \"image/png\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"embeds\": [],\n" +
                "            \"mentions\": [\n" +
                "                {\n" +
                "                    \"id\": \"1012268717368946718\",\n" +
                "                    \"username\": \"oliverdaniel\",\n" +
                "                    \"avatar\": null,\n" +
                "                    \"discriminator\": \"0\",\n" +
                "                    \"public_flags\": 0,\n" +
                "                    \"flags\": 0,\n" +
                "                    \"banner\": null,\n" +
                "                    \"accent_color\": null,\n" +
                "                    \"global_name\": \"oliverdaniel\",\n" +
                "                    \"avatar_decoration\": null,\n" +
                "                    \"display_name\": \"oliverdaniel\",\n" +
                "                    \"banner_color\": null\n" +
                "                }\n" +
                "            ],\n" +
                "            \"mention_roles\": [],\n" +
                "            \"pinned\": false,\n" +
                "            \"mention_everyone\": false,\n" +
                "            \"tts\": false,\n" +
                "            \"timestamp\": \"2023-07-12T08:11:58.672000+00:00\",\n" +
                "            \"edited_timestamp\": null,\n" +
                "            \"flags\": 0,\n" +
                "            \"components\": [\n" +
                "                {\n" +
                "                    \"type\": 1,\n" +
                "                    \"components\": [\n" +
                "                        {\n" +
                "                            \"type\": 2,\n" +
                "                            \"custom_id\": \"MJ::JOB::upsample::1::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                            \"style\": 2,\n" +
                "                            \"label\": \"U1\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"type\": 2,\n" +
                "                            \"custom_id\": \"MJ::JOB::upsample::2::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                            \"style\": 2,\n" +
                "                            \"label\": \"U2\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"type\": 2,\n" +
                "                            \"custom_id\": \"MJ::JOB::upsample::3::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                            \"style\": 2,\n" +
                "                            \"label\": \"U3\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"type\": 2,\n" +
                "                            \"custom_id\": \"MJ::JOB::upsample::4::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                            \"style\": 1,\n" +
                "                            \"label\": \"U4\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"type\": 2,\n" +
                "                            \"custom_id\": \"MJ::JOB::reroll::0::c09a2935-432a-464c-bb31-6250a418a3c1::SOLO\",\n" +
                "                            \"style\": 2,\n" +
                "                            \"emoji\": {\n" +
                "                                \"name\": \"\uD83D\uDD04\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"type\": 1,\n" +
                "                    \"components\": [\n" +
                "                        {\n" +
                "                            \"type\": 2,\n" +
                "                            \"custom_id\": \"MJ::JOB::variation::1::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                            \"style\": 2,\n" +
                "                            \"label\": \"V1\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"type\": 2,\n" +
                "                            \"custom_id\": \"MJ::JOB::variation::2::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                            \"style\": 2,\n" +
                "                            \"label\": \"V2\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"type\": 2,\n" +
                "                            \"custom_id\": \"MJ::JOB::variation::3::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                            \"style\": 2,\n" +
                "                            \"label\": \"V3\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"type\": 2,\n" +
                "                            \"custom_id\": \"MJ::JOB::variation::4::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                            \"style\": 2,\n" +
                "                            \"label\": \"V4\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1128599623762714675\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[6144234458317582] The spaceship and the airplane collided. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1128599623209074708\",\n" +
                "                \"filename\": \"oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c09a2935-432a-464c-bb31-6250a418a3c1.png\",\n" +
                "                \"size\": 7812030,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128599623209074708/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c09a2935-432a-464c-bb31-6250a418a3c1.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128599623209074708/oliverdaniel_6144234458317582_The_spaceship_and_the_airplane_co_c09a2935-432a-464c-bb31-6250a418a3c1.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-12T08:11:58.672000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                        \"style\": 1,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::c09a2935-432a-464c-bb31-6250a418a3c1::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::c09a2935-432a-464c-bb31-6250a418a3c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1128599551486459915\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[1907056478711727] 天空出现ufo --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1128599550760849418\",\n" +
                "                \"filename\": \"oliverdaniel_1907056478711727_ufo_be9def17-80f8-4f58-b895-e0faa072f2c5.png\",\n" +
                "                \"size\": 7324641,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128599550760849418/oliverdaniel_1907056478711727_ufo_be9def17-80f8-4f58-b895-e0faa072f2c5.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128599550760849418/oliverdaniel_1907056478711727_ufo_be9def17-80f8-4f58-b895-e0faa072f2c5.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-12T08:11:41.440000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::be9def17-80f8-4f58-b895-e0faa072f2c5\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::be9def17-80f8-4f58-b895-e0faa072f2c5\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::be9def17-80f8-4f58-b895-e0faa072f2c5\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::be9def17-80f8-4f58-b895-e0faa072f2c5\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::be9def17-80f8-4f58-b895-e0faa072f2c5::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::be9def17-80f8-4f58-b895-e0faa072f2c5\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::be9def17-80f8-4f58-b895-e0faa072f2c5\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::be9def17-80f8-4f58-b895-e0faa072f2c5\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::be9def17-80f8-4f58-b895-e0faa072f2c5\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1128586677489774642\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[7278729376429091] Basketball Fire --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1128586676793524295\",\n" +
                "                \"filename\": \"oliverdaniel_7278729376429091_Basketball_Fire_7bbb76e6-885c-4d77-8278-696c3d0973fb.png\",\n" +
                "                \"size\": 7788571,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128586676793524295/oliverdaniel_7278729376429091_Basketball_Fire_7bbb76e6-885c-4d77-8278-696c3d0973fb.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128586676793524295/oliverdaniel_7278729376429091_Basketball_Fire_7bbb76e6-885c-4d77-8278-696c3d0973fb.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-12T07:20:32.040000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::7bbb76e6-885c-4d77-8278-696c3d0973fb\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::7bbb76e6-885c-4d77-8278-696c3d0973fb\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::7bbb76e6-885c-4d77-8278-696c3d0973fb\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::7bbb76e6-885c-4d77-8278-696c3d0973fb\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::7bbb76e6-885c-4d77-8278-696c3d0973fb::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::7bbb76e6-885c-4d77-8278-696c3d0973fb\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::7bbb76e6-885c-4d77-8278-696c3d0973fb\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::7bbb76e6-885c-4d77-8278-696c3d0973fb\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::7bbb76e6-885c-4d77-8278-696c3d0973fb\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1128583362274152509\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[7131861082683404] Beauty and the Beast --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1128583362047643718\",\n" +
                "                \"filename\": \"oliverdaniel_7131861082683404_Beauty_and_the_Beast_4bd56dd8-ba8a-4d72-a463-c93eb259fbaa.png\",\n" +
                "                \"size\": 7690262,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128583362047643718/oliverdaniel_7131861082683404_Beauty_and_the_Beast_4bd56dd8-ba8a-4d72-a463-c93eb259fbaa.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128583362047643718/oliverdaniel_7131861082683404_Beauty_and_the_Beast_4bd56dd8-ba8a-4d72-a463-c93eb259fbaa.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-12T07:07:21.631000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::4bd56dd8-ba8a-4d72-a463-c93eb259fbaa\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::4bd56dd8-ba8a-4d72-a463-c93eb259fbaa\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::4bd56dd8-ba8a-4d72-a463-c93eb259fbaa\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::4bd56dd8-ba8a-4d72-a463-c93eb259fbaa\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::4bd56dd8-ba8a-4d72-a463-c93eb259fbaa::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::4bd56dd8-ba8a-4d72-a463-c93eb259fbaa\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::4bd56dd8-ba8a-4d72-a463-c93eb259fbaa\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::4bd56dd8-ba8a-4d72-a463-c93eb259fbaa\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::4bd56dd8-ba8a-4d72-a463-c93eb259fbaa\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1128244160802074725\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[9637595622096238] The sky is blue. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1128244160533631036\",\n" +
                "                \"filename\": \"oliverdaniel_9637595622096238_The_sky_is_blue._a9db87a0-3400-4170-aaf4-4ccd9ceb404b.png\",\n" +
                "                \"size\": 7761408,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128244160533631036/oliverdaniel_9637595622096238_The_sky_is_blue._a9db87a0-3400-4170-aaf4-4ccd9ceb404b.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128244160533631036/oliverdaniel_9637595622096238_The_sky_is_blue._a9db87a0-3400-4170-aaf4-4ccd9ceb404b.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-11T08:39:29.698000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::a9db87a0-3400-4170-aaf4-4ccd9ceb404b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::a9db87a0-3400-4170-aaf4-4ccd9ceb404b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::a9db87a0-3400-4170-aaf4-4ccd9ceb404b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::a9db87a0-3400-4170-aaf4-4ccd9ceb404b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::a9db87a0-3400-4170-aaf4-4ccd9ceb404b::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::a9db87a0-3400-4170-aaf4-4ccd9ceb404b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::a9db87a0-3400-4170-aaf4-4ccd9ceb404b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::a9db87a0-3400-4170-aaf4-4ccd9ceb404b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::a9db87a0-3400-4170-aaf4-4ccd9ceb404b\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1128129545346756618\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[7584722341995409] The sky is blue. --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1128129544583397416\",\n" +
                "                \"filename\": \"oliverdaniel_7584722341995409_The_sky_is_blue._854d5960-e675-4873-b71f-21cd3298cf07.png\",\n" +
                "                \"size\": 8147073,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1128129544583397416/oliverdaniel_7584722341995409_The_sky_is_blue._854d5960-e675-4873-b71f-21cd3298cf07.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1128129544583397416/oliverdaniel_7584722341995409_The_sky_is_blue._854d5960-e675-4873-b71f-21cd3298cf07.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-11T01:04:03.244000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::854d5960-e675-4873-b71f-21cd3298cf07\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::854d5960-e675-4873-b71f-21cd3298cf07\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::854d5960-e675-4873-b71f-21cd3298cf07\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::854d5960-e675-4873-b71f-21cd3298cf07\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::854d5960-e675-4873-b71f-21cd3298cf07::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::854d5960-e675-4873-b71f-21cd3298cf07\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::854d5960-e675-4873-b71f-21cd3298cf07\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::854d5960-e675-4873-b71f-21cd3298cf07\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::854d5960-e675-4873-b71f-21cd3298cf07\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127971609467170947\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[3670411083712129] Penguins and polar bears are playing together on surfboards in the Pacific Ocean, scanning cyberpunk, 8k. --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127971608963858462\",\n" +
                "                \"filename\": \"oliverdaniel_3670411083712129_Penguins_and_polar_bears_are_play_d93b77f2-14a7-461a-9d79-13275b144044.png\",\n" +
                "                \"size\": 7270840,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127971608963858462/oliverdaniel_3670411083712129_Penguins_and_polar_bears_are_play_d93b77f2-14a7-461a-9d79-13275b144044.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127971608963858462/oliverdaniel_3670411083712129_Penguins_and_polar_bears_are_play_d93b77f2-14a7-461a-9d79-13275b144044.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T14:36:28.396000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::d93b77f2-14a7-461a-9d79-13275b144044\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::d93b77f2-14a7-461a-9d79-13275b144044\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::d93b77f2-14a7-461a-9d79-13275b144044\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::d93b77f2-14a7-461a-9d79-13275b144044\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::d93b77f2-14a7-461a-9d79-13275b144044::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::d93b77f2-14a7-461a-9d79-13275b144044\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::d93b77f2-14a7-461a-9d79-13275b144044\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::d93b77f2-14a7-461a-9d79-13275b144044\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::d93b77f2-14a7-461a-9d79-13275b144044\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127971328100679711\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[9244402853440658] Penguins and polar bears are playing together on surfboards in the Pacific Ocean, scanning cyberpunk, 8k. --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127971327580569670\",\n" +
                "                \"filename\": \"oliverdaniel_9244402853440658_Penguins_and_polar_bears_are_play_e373bf62-84bf-40f4-8078-c4f3ff1b77b2.png\",\n" +
                "                \"size\": 6905779,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127971327580569670/oliverdaniel_9244402853440658_Penguins_and_polar_bears_are_play_e373bf62-84bf-40f4-8078-c4f3ff1b77b2.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127971327580569670/oliverdaniel_9244402853440658_Penguins_and_polar_bears_are_play_e373bf62-84bf-40f4-8078-c4f3ff1b77b2.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T14:35:21.313000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::e373bf62-84bf-40f4-8078-c4f3ff1b77b2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::e373bf62-84bf-40f4-8078-c4f3ff1b77b2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::e373bf62-84bf-40f4-8078-c4f3ff1b77b2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::e373bf62-84bf-40f4-8078-c4f3ff1b77b2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::e373bf62-84bf-40f4-8078-c4f3ff1b77b2::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::e373bf62-84bf-40f4-8078-c4f3ff1b77b2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::e373bf62-84bf-40f4-8078-c4f3ff1b77b2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::e373bf62-84bf-40f4-8078-c4f3ff1b77b2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::e373bf62-84bf-40f4-8078-c4f3ff1b77b2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127969476671983739\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[4268844288346033] tiger --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127969476097351752\",\n" +
                "                \"filename\": \"oliverdaniel_4268844288346033_tiger_fdc5a15a-5012-43ba-98fb-6119f77afef7.png\",\n" +
                "                \"size\": 8060428,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127969476097351752/oliverdaniel_4268844288346033_tiger_fdc5a15a-5012-43ba-98fb-6119f77afef7.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127969476097351752/oliverdaniel_4268844288346033_tiger_fdc5a15a-5012-43ba-98fb-6119f77afef7.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T14:27:59.898000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::fdc5a15a-5012-43ba-98fb-6119f77afef7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::fdc5a15a-5012-43ba-98fb-6119f77afef7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::fdc5a15a-5012-43ba-98fb-6119f77afef7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::fdc5a15a-5012-43ba-98fb-6119f77afef7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::fdc5a15a-5012-43ba-98fb-6119f77afef7::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::fdc5a15a-5012-43ba-98fb-6119f77afef7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::fdc5a15a-5012-43ba-98fb-6119f77afef7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::fdc5a15a-5012-43ba-98fb-6119f77afef7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::fdc5a15a-5012-43ba-98fb-6119f77afef7\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127969380916015205\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[3657560938319833] tiger --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127969380429467739\",\n" +
                "                \"filename\": \"oliverdaniel_3657560938319833_tiger_459258fa-2324-4c47-9aab-49569d289d01.png\",\n" +
                "                \"size\": 8155464,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127969380429467739/oliverdaniel_3657560938319833_tiger_459258fa-2324-4c47-9aab-49569d289d01.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127969380429467739/oliverdaniel_3657560938319833_tiger_459258fa-2324-4c47-9aab-49569d289d01.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T14:27:37.068000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::459258fa-2324-4c47-9aab-49569d289d01\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::459258fa-2324-4c47-9aab-49569d289d01\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::459258fa-2324-4c47-9aab-49569d289d01\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::459258fa-2324-4c47-9aab-49569d289d01\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::459258fa-2324-4c47-9aab-49569d289d01::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::459258fa-2324-4c47-9aab-49569d289d01\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::459258fa-2324-4c47-9aab-49569d289d01\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::459258fa-2324-4c47-9aab-49569d289d01\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::459258fa-2324-4c47-9aab-49569d289d01\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127969348326273144\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[9925263454073932] tiger --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127969347688743003\",\n" +
                "                \"filename\": \"oliverdaniel_9925263454073932_tiger_706106bf-4439-460d-8bb8-473d1bc213c1.png\",\n" +
                "                \"size\": 7604525,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127969347688743003/oliverdaniel_9925263454073932_tiger_706106bf-4439-460d-8bb8-473d1bc213c1.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127969347688743003/oliverdaniel_9925263454073932_tiger_706106bf-4439-460d-8bb8-473d1bc213c1.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T14:27:29.298000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::706106bf-4439-460d-8bb8-473d1bc213c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::706106bf-4439-460d-8bb8-473d1bc213c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::706106bf-4439-460d-8bb8-473d1bc213c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::706106bf-4439-460d-8bb8-473d1bc213c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::706106bf-4439-460d-8bb8-473d1bc213c1::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::706106bf-4439-460d-8bb8-473d1bc213c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::706106bf-4439-460d-8bb8-473d1bc213c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::706106bf-4439-460d-8bb8-473d1bc213c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::706106bf-4439-460d-8bb8-473d1bc213c1\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127967614254194749\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[9329324561381232] In the cold wind, a helpless middle-aged man, holding a cigarette and drinking beer, sheds tears of regret. Bokeh, 8k. --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127967613482438717\",\n" +
                "                \"filename\": \"oliverdaniel_9329324561381232_In_the_cold_wind_a_helpless_middl_49c55d36-b53e-4e5b-ad2b-785ec3d47b56.png\",\n" +
                "                \"size\": 7571668,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127967613482438717/oliverdaniel_9329324561381232_In_the_cold_wind_a_helpless_middl_49c55d36-b53e-4e5b-ad2b-785ec3d47b56.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127967613482438717/oliverdaniel_9329324561381232_In_the_cold_wind_a_helpless_middl_49c55d36-b53e-4e5b-ad2b-785ec3d47b56.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T14:20:35.863000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::49c55d36-b53e-4e5b-ad2b-785ec3d47b56\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::49c55d36-b53e-4e5b-ad2b-785ec3d47b56\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::49c55d36-b53e-4e5b-ad2b-785ec3d47b56\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::49c55d36-b53e-4e5b-ad2b-785ec3d47b56\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::49c55d36-b53e-4e5b-ad2b-785ec3d47b56::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::49c55d36-b53e-4e5b-ad2b-785ec3d47b56\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::49c55d36-b53e-4e5b-ad2b-785ec3d47b56\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::49c55d36-b53e-4e5b-ad2b-785ec3d47b56\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::49c55d36-b53e-4e5b-ad2b-785ec3d47b56\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127967610336718950\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[1358493478465470] In the cold wind, a helpless middle-aged man, holding a cigarette and drinking beer, sheds tears of regret. Bokeh, 8k. --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127967608755474602\",\n" +
                "                \"filename\": \"oliverdaniel_1358493478465470_In_the_cold_wind_a_helpless_middl_e5216074-d3f3-46f4-b5b5-3a59170d285e.png\",\n" +
                "                \"size\": 7367421,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127967608755474602/oliverdaniel_1358493478465470_In_the_cold_wind_a_helpless_middl_e5216074-d3f3-46f4-b5b5-3a59170d285e.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127967608755474602/oliverdaniel_1358493478465470_In_the_cold_wind_a_helpless_middl_e5216074-d3f3-46f4-b5b5-3a59170d285e.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T14:20:34.929000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::e5216074-d3f3-46f4-b5b5-3a59170d285e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::e5216074-d3f3-46f4-b5b5-3a59170d285e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::e5216074-d3f3-46f4-b5b5-3a59170d285e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::e5216074-d3f3-46f4-b5b5-3a59170d285e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::e5216074-d3f3-46f4-b5b5-3a59170d285e::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::e5216074-d3f3-46f4-b5b5-3a59170d285e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::e5216074-d3f3-46f4-b5b5-3a59170d285e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::e5216074-d3f3-46f4-b5b5-3a59170d285e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::e5216074-d3f3-46f4-b5b5-3a59170d285e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127923303567339550\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[0935933750034744] The tiger hunts in the forest. --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127923303030472756\",\n" +
                "                \"filename\": \"oliverdaniel_0935933750034744_The_tiger_hunts_in_the_forest._07bfc435-3978-400c-9ecb-e3a8b9dd70d2.png\",\n" +
                "                \"size\": 7915507,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127923303030472756/oliverdaniel_0935933750034744_The_tiger_hunts_in_the_forest._07bfc435-3978-400c-9ecb-e3a8b9dd70d2.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127923303030472756/oliverdaniel_0935933750034744_The_tiger_hunts_in_the_forest._07bfc435-3978-400c-9ecb-e3a8b9dd70d2.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T11:24:31.372000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::07bfc435-3978-400c-9ecb-e3a8b9dd70d2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::07bfc435-3978-400c-9ecb-e3a8b9dd70d2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::07bfc435-3978-400c-9ecb-e3a8b9dd70d2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::07bfc435-3978-400c-9ecb-e3a8b9dd70d2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::07bfc435-3978-400c-9ecb-e3a8b9dd70d2::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::07bfc435-3978-400c-9ecb-e3a8b9dd70d2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::07bfc435-3978-400c-9ecb-e3a8b9dd70d2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::07bfc435-3978-400c-9ecb-e3a8b9dd70d2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::07bfc435-3978-400c-9ecb-e3a8b9dd70d2\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127910291162804236\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[1187566025347945] Newton stood on the shoulders of giants and became a giant himself. --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127910290839830558\",\n" +
                "                \"filename\": \"oliverdaniel_1187566025347945_Newton_stood_on_the_shoulders_of__40799130-e587-44f7-94a3-ec8e7c09d69e.png\",\n" +
                "                \"size\": 7037558,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127910290839830558/oliverdaniel_1187566025347945_Newton_stood_on_the_shoulders_of__40799130-e587-44f7-94a3-ec8e7c09d69e.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127910290839830558/oliverdaniel_1187566025347945_Newton_stood_on_the_shoulders_of__40799130-e587-44f7-94a3-ec8e7c09d69e.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T10:32:48.973000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::40799130-e587-44f7-94a3-ec8e7c09d69e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::40799130-e587-44f7-94a3-ec8e7c09d69e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::40799130-e587-44f7-94a3-ec8e7c09d69e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::40799130-e587-44f7-94a3-ec8e7c09d69e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::40799130-e587-44f7-94a3-ec8e7c09d69e::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::40799130-e587-44f7-94a3-ec8e7c09d69e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::40799130-e587-44f7-94a3-ec8e7c09d69e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::40799130-e587-44f7-94a3-ec8e7c09d69e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::40799130-e587-44f7-94a3-ec8e7c09d69e\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127907610532782170\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[5762450759044432] 粉色的猪 --s 750 --niji 5** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127907609563889664\",\n" +
                "                \"filename\": \"oliverdaniel_5762450759044432__2d2cece2-2cac-434d-b50c-4e0a838ce91c.png\",\n" +
                "                \"size\": 7682150,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127907609563889664/oliverdaniel_5762450759044432__2d2cece2-2cac-434d-b50c-4e0a838ce91c.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127907609563889664/oliverdaniel_5762450759044432__2d2cece2-2cac-434d-b50c-4e0a838ce91c.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T10:22:09.861000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::2d2cece2-2cac-434d-b50c-4e0a838ce91c\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::2d2cece2-2cac-434d-b50c-4e0a838ce91c\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::2d2cece2-2cac-434d-b50c-4e0a838ce91c\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::2d2cece2-2cac-434d-b50c-4e0a838ce91c\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::2d2cece2-2cac-434d-b50c-4e0a838ce91c::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::2d2cece2-2cac-434d-b50c-4e0a838ce91c\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::2d2cece2-2cac-434d-b50c-4e0a838ce91c\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::2d2cece2-2cac-434d-b50c-4e0a838ce91c\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::2d2cece2-2cac-434d-b50c-4e0a838ce91c\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127899277176360970\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[1974058532033641] Picture a giraffe with cybernetic enhancements gracefully navigating a cityscape of towering skyscrapers, captured in a wide view. The hyper-detailed rendering in 8K resolution creates a cinematic view of this unexpected urban resident by Martin Parr --ar 4:5 --s 250 --style raw --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127899276299743263\",\n" +
                "                \"filename\": \"oliverdaniel_1974058532033641_Picture_a_giraffe_with_cybernetic_0f10a0ba-7c58-4f79-be4f-564269cd0d52.png\",\n" +
                "                \"size\": 8486487,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127899276299743263/oliverdaniel_1974058532033641_Picture_a_giraffe_with_cybernetic_0f10a0ba-7c58-4f79-be4f-564269cd0d52.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127899276299743263/oliverdaniel_1974058532033641_Picture_a_giraffe_with_cybernetic_0f10a0ba-7c58-4f79-be4f-564269cd0d52.png\",\n" +
                "                \"width\": 1920,\n" +
                "                \"height\": 2400,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T09:49:03.034000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::0f10a0ba-7c58-4f79-be4f-564269cd0d52\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::0f10a0ba-7c58-4f79-be4f-564269cd0d52\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::0f10a0ba-7c58-4f79-be4f-564269cd0d52\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::0f10a0ba-7c58-4f79-be4f-564269cd0d52\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::0f10a0ba-7c58-4f79-be4f-564269cd0d52::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::0f10a0ba-7c58-4f79-be4f-564269cd0d52\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::0f10a0ba-7c58-4f79-be4f-564269cd0d52\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::0f10a0ba-7c58-4f79-be4f-564269cd0d52\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::0f10a0ba-7c58-4f79-be4f-564269cd0d52\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127898142604537866\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[8643588946369514] Picture a giraffe with cybernetic enhancements gracefully navigating a cityscape of towering skyscrapers, captured in a wide view. The hyper-detailed rendering in 8K resolution creates a cinematic view of this unexpected urban resident by Martin Parr --ar 4:5 --s 250 --style raw --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127898142034100325\",\n" +
                "                \"filename\": \"oliverdaniel_8643588946369514_Picture_a_giraffe_with_cybernetic_6ebf25bb-93f0-4c90-bd30-71917566d1ed.png\",\n" +
                "                \"size\": 8032333,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127898142034100325/oliverdaniel_8643588946369514_Picture_a_giraffe_with_cybernetic_6ebf25bb-93f0-4c90-bd30-71917566d1ed.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127898142034100325/oliverdaniel_8643588946369514_Picture_a_giraffe_with_cybernetic_6ebf25bb-93f0-4c90-bd30-71917566d1ed.png\",\n" +
                "                \"width\": 1920,\n" +
                "                \"height\": 2400,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T09:44:32.531000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::6ebf25bb-93f0-4c90-bd30-71917566d1ed\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::6ebf25bb-93f0-4c90-bd30-71917566d1ed\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::6ebf25bb-93f0-4c90-bd30-71917566d1ed\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::6ebf25bb-93f0-4c90-bd30-71917566d1ed\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::6ebf25bb-93f0-4c90-bd30-71917566d1ed::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::6ebf25bb-93f0-4c90-bd30-71917566d1ed\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::6ebf25bb-93f0-4c90-bd30-71917566d1ed\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::6ebf25bb-93f0-4c90-bd30-71917566d1ed\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::6ebf25bb-93f0-4c90-bd30-71917566d1ed\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127897147442991114\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[6783450556255966] Big head, big head, not worried about rain. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127897146964844585\",\n" +
                "                \"filename\": \"oliverdaniel_6783450556255966_Big_head_big_head_not_worried_abo_ef3fbe29-0dc8-4a14-a830-16e75ae2fce3.png\",\n" +
                "                \"size\": 7620072,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127897146964844585/oliverdaniel_6783450556255966_Big_head_big_head_not_worried_abo_ef3fbe29-0dc8-4a14-a830-16e75ae2fce3.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127897146964844585/oliverdaniel_6783450556255966_Big_head_big_head_not_worried_abo_ef3fbe29-0dc8-4a14-a830-16e75ae2fce3.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T09:40:35.266000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::ef3fbe29-0dc8-4a14-a830-16e75ae2fce3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::ef3fbe29-0dc8-4a14-a830-16e75ae2fce3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::ef3fbe29-0dc8-4a14-a830-16e75ae2fce3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::ef3fbe29-0dc8-4a14-a830-16e75ae2fce3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::ef3fbe29-0dc8-4a14-a830-16e75ae2fce3::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::ef3fbe29-0dc8-4a14-a830-16e75ae2fce3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::ef3fbe29-0dc8-4a14-a830-16e75ae2fce3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::ef3fbe29-0dc8-4a14-a830-16e75ae2fce3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::ef3fbe29-0dc8-4a14-a830-16e75ae2fce3\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"1127895280684122153\",\n" +
                "        \"type\": 0,\n" +
                "        \"content\": \"**[6195075322852492] A cute little girl was in the forest, catching fireflies. She was running around, and accidentally stepped on a tiger's tail. --s 750 --v 5.2** - <@1012268717368946718> (relaxed)\",\n" +
                "        \"channel_id\": \"1120568025993715764\",\n" +
                "        \"author\": {\n" +
                "            \"id\": \"936929561302675456\",\n" +
                "            \"username\": \"Midjourney Bot\",\n" +
                "            \"avatar\": \"f6ce562a6b4979c4b1cbc5b436d3be76\",\n" +
                "            \"discriminator\": \"9282\",\n" +
                "            \"public_flags\": 589824,\n" +
                "            \"flags\": 589824,\n" +
                "            \"bot\": true,\n" +
                "            \"banner\": null,\n" +
                "            \"accent_color\": null,\n" +
                "            \"global_name\": null,\n" +
                "            \"avatar_decoration\": null,\n" +
                "            \"display_name\": null,\n" +
                "            \"banner_color\": null\n" +
                "        },\n" +
                "        \"attachments\": [\n" +
                "            {\n" +
                "                \"id\": \"1127895280113688596\",\n" +
                "                \"filename\": \"oliverdaniel_6195075322852492_A_cute_little_girl_was_in_the_for_c6029a29-9425-4d9f-b009-91aec58ae0b4.png\",\n" +
                "                \"size\": 6974510,\n" +
                "                \"url\": \"https://cdn.discordapp.com/attachments/1120568025993715764/1127895280113688596/oliverdaniel_6195075322852492_A_cute_little_girl_was_in_the_for_c6029a29-9425-4d9f-b009-91aec58ae0b4.png\",\n" +
                "                \"proxy_url\": \"https://media.discordapp.net/attachments/1120568025993715764/1127895280113688596/oliverdaniel_6195075322852492_A_cute_little_girl_was_in_the_for_c6029a29-9425-4d9f-b009-91aec58ae0b4.png\",\n" +
                "                \"width\": 2048,\n" +
                "                \"height\": 2048,\n" +
                "                \"content_type\": \"image/png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"embeds\": [],\n" +
                "        \"mentions\": [\n" +
                "            {\n" +
                "                \"id\": \"1012268717368946718\",\n" +
                "                \"username\": \"oliverdaniel\",\n" +
                "                \"avatar\": null,\n" +
                "                \"discriminator\": \"0\",\n" +
                "                \"public_flags\": 0,\n" +
                "                \"flags\": 0,\n" +
                "                \"banner\": null,\n" +
                "                \"accent_color\": null,\n" +
                "                \"global_name\": \"oliverdaniel\",\n" +
                "                \"avatar_decoration\": null,\n" +
                "                \"display_name\": \"oliverdaniel\",\n" +
                "                \"banner_color\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"mention_roles\": [],\n" +
                "        \"pinned\": false,\n" +
                "        \"mention_everyone\": false,\n" +
                "        \"tts\": false,\n" +
                "        \"timestamp\": \"2023-07-10T09:33:10.196000+00:00\",\n" +
                "        \"edited_timestamp\": null,\n" +
                "        \"flags\": 0,\n" +
                "        \"components\": [\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::1::c6029a29-9425-4d9f-b009-91aec58ae0b4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::2::c6029a29-9425-4d9f-b009-91aec58ae0b4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::3::c6029a29-9425-4d9f-b009-91aec58ae0b4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::upsample::4::c6029a29-9425-4d9f-b009-91aec58ae0b4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"U4\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::reroll::0::c6029a29-9425-4d9f-b009-91aec58ae0b4::SOLO\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"emoji\": {\n" +
                "                            \"name\": \"\uD83D\uDD04\"\n" +
                "                        }\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"type\": 1,\n" +
                "                \"components\": [\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::1::c6029a29-9425-4d9f-b009-91aec58ae0b4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V1\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::2::c6029a29-9425-4d9f-b009-91aec58ae0b4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V2\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::3::c6029a29-9425-4d9f-b009-91aec58ae0b4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V3\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"type\": 2,\n" +
                "                        \"custom_id\": \"MJ::JOB::variation::4::c6029a29-9425-4d9f-b009-91aec58ae0b4\",\n" +
                "                        \"style\": 2,\n" +
                "                        \"label\": \"V4\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "]";
        JSONArray objects = JSON.parseArray(response);
        if (objects.size() > 0) {
            String nextId = ((JSONObject) objects.get(0)).getString("id");
            System.out.println(nextId);
            for (Object object : objects) {
                for (Object attachments : ((JSONObject) object).getJSONArray("attachments")) {
                    String taskid = ((JSONObject) object).getString("content").split("]")[0].substring(3);
                    boolean isNumeric = taskid.matches("\\d+");
                    System.out.println(isNumeric);
                    if (isNumeric) {
                        String string = ((JSONObject) attachments).getString("proxy_url");
                        String replace = string.replace("https://media.discordapp.net", "http://www.ai-assistant.com.cn/api/cnd-discordapp");
                        String url = replace + "?Authorization=9998@xunshu";
                    } else {
                        System.out.println("非前端生成，跳过存储");
                    }
                }
            }
        }
    }*/
}
