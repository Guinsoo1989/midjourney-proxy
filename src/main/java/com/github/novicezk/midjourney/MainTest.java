package com.github.novicezk.midjourney;

import com.github.novicezk.midjourney.util.ContentParseData;
import com.github.novicezk.midjourney.wss.handle.ZoomMessageHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainTest {

    private static final String START_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - <@\\d+> \\((.*?)\\)";

    public static void main(String[] args) {
        ZoomMessageHandler.isZoom("{\"op\":0,\"s\":26,\"t\":\"MESSAGE_CREATE\",\"d\":{\"mention_everyone\":false,\"pinned\":false,\"components\":[{\"components\":[{\"custom_id\":\"MJ::CancelJob::ByJobid::875fbd3a-ef43-4134-a4e8-cd853565cbfd\",\"style\":4,\"label\":\"Cancel Job\",\"type\":2}],\"type\":1}],\"attachments\":[],\"author\":{\"global_name\":null,\"bot\":true,\"public_flags\":589824,\"id\":\"936929561302675456\",\"avatar\":\"f6ce562a6b4979c4b1cbc5b436d3be76\",\"username\":\"Midjourney Bot\",\"discriminator\":\"9282\",\"avatar_decoration\":null},\"flags\":64,\"type\":19,\"mention_roles\":[],\"application_id\":\"936929561302675456\",\"edited_timestamp\":null,\"content\":\"**rain** - <@1127612768489578560> (Waiting to start)\",\"tts\":false,\"webhook_id\":\"936929561302675456\",\"mentions\":[{\"global_name\":null,\"public_flags\":0,\"id\":\"1127612768489578560\",\"avatar\":null,\"username\":\"fengye\",\"discriminator\":\"7973\",\"avatar_decoration\":null}],\"message_reference\":{\"guild_id\":\"1127614059156602984\",\"message_id\":\"1127962442635419768\",\"channel_id\":\"1127614059894820930\"},\"id\":\"1127962511753363537\",\"embeds\":[],\"channel_id\":\"1127614059894820930\",\"timestamp\":\"2023-07-10T14:00:19.332000+00:00\"}}");
    }

//    private static boolean isZoom(String msgStr) {
//        JSONObject msg = JSONUtil.parseObj(msgStr);
//        String type = msg.get("t").toString();
//        JSONObject data = (JSONObject) msg.get("d");
//        if ("MESSAGE_CREATE".equals(type)) {
//            // zoom的create消息的content和imagine一样
//            if (data.containsKey("interaction")) {
//                return false;
//            }
//
//            String content = data.get("content").toString();
//            System.out.println(content);
//            ContentParseData parse = parseStart(content);
//            return Objects.nonNull(parse);
//        } else if ("MESSAGE_UPDATE".equals(type)) {
//            String content = data.get("content").toString();
//            return content.contains("Zoom Out");
//        }
//
//        return false;
//
//    }

//    private static ContentParseData parseStart(String content) {
//        Matcher matcher = Pattern.compile(START_CONTENT_REGEX).matcher(content);
//        if (!matcher.find()) {
//            return null;
//        }
//        ContentParseData parseData = new ContentParseData();
//        parseData.setPrompt(matcher.group(1));
//        parseData.setStatus(matcher.group(2));
//        return parseData;
//    }
}
