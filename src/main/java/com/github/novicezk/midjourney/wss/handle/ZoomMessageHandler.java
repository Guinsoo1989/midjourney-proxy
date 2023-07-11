package com.github.novicezk.midjourney.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ContentParseData;
import com.github.novicezk.midjourney.util.UVContentParseData;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ZoomMessageHandler extends MessageHandler {
    private static final String START_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - <@\\d+> \\((.*?)\\)";
    private static final String OLD_CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Zoom Out by <@\\d+> \\((.*?)\\)";
    private static final String CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Zoom Out \\(Strong\\) by <@\\d+> \\((.*?)\\)";

    public static boolean isZoom(String msgStr) {
        JSONObject msg = JSONUtil.parseObj(msgStr);
        String type = msg.get("t").toString();
        JSONObject data = (JSONObject) msg.get("d");
        if (!data.containsKey("content")) {
            return false;
        }
        String content = data.get("content").toString();
        // update和结束的create消息都包含Zoom Out
        if ("MESSAGE_UPDATE".equals(type)) {
            return content.contains("Zoom Out");
        }
        if ("MESSAGE_CREATE".equals(type)) {
            // zoom开始的create消息的content和imagine一样，难以区分
            // imagine开始会有interaction
            if (data.containsKey("interaction")) {
                return false;
            }
            return content.contains("Zoom Out");
        }
        return false;
    }

    @Override
    public void handle(MessageType messageType, DataObject message) {
        String content = getMessageContent(message);
        if (MessageType.CREATE.equals(messageType)) {
            ContentParseData start = parseStart(content);
            if (start != null) {
                // 开始
                TaskCondition condition = new TaskCondition()
                        .setFinalPromptEn(start.getPrompt())
                        .setActionSet(Set.of(TaskAction.ZOOM))
                        .setStatusSet(Set.of(TaskStatus.SUBMITTED));
                Task task = this.taskQueueHelper.findRunningTask(condition)
                        .min(Comparator.comparing(Task::getSubmitTime))
                        .orElse(null);
                if (task == null) {
                    return;
                }
                task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, message.getString("id"));
                task.setStatus(TaskStatus.IN_PROGRESS);
                task.awake();
                return;
            }
            UVContentParseData end = parse(content);
            if (end == null) {
                return;
            }
            TaskCondition condition = new TaskCondition()
                    .setFinalPromptEn(end.getPrompt())
                    .setActionSet(Set.of(TaskAction.ZOOM))
                    .setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
            Task task = this.taskQueueHelper.findRunningTask(condition)
                    .max(Comparator.comparing(Task::getProgress))
                    .orElse(null);
            if (task == null) {
                return;
            }
            finishTask(task, message);
            task.awake();
        } else if (MessageType.UPDATE == messageType) {
            UVContentParseData parseData = parse(content);
            if (parseData == null || CharSequenceUtil.equalsAny(parseData.getStatus(), "relaxed", "fast")) {
                return;
            }
            TaskCondition condition = new TaskCondition()
                    .setProgressMessageId(message.getString("id"))
                    .setActionSet(Set.of(TaskAction.ZOOM))
                    .setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
            Task task = this.taskQueueHelper.findRunningTask(condition)
                    .findFirst().orElse(null);
            if (task == null) {
                return;
            }
            task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, message.getString("id"));
            task.setStatus(TaskStatus.IN_PROGRESS);
            task.setProgress(parseData.getStatus());
            task.setImageUrl(getImageUrl(message));
            task.awake();
        }
    }

    /**
     * bot-wss模式，取不到执行进度; todo: 同个任务不同变换对应不上.
     *
     * @param messageType messageType
     * @param message     message
     */
    @Override
    public void handle(MessageType messageType, Message message) {
        String content = message.getContentRaw();
        if (MessageType.CREATE.equals(messageType)) {
            UVContentParseData parseData = parse(content);
            if (parseData == null) {
                return;
            }
            TaskCondition condition = new TaskCondition()
                    .setFinalPromptEn(parseData.getPrompt())
                    .setActionSet(Set.of(TaskAction.ZOOM))
                    .setStatusSet(Set.of(TaskStatus.SUBMITTED, TaskStatus.IN_PROGRESS));
            Task task = this.taskQueueHelper.findRunningTask(condition)
                    .min(Comparator.comparing(Task::getSubmitTime))
                    .orElse(null);
            if (task == null) {
                return;
            }
            finishTask(task, message);
            task.awake();
        }
    }

    private static ContentParseData parseStart(String content) {
        Matcher matcher = Pattern.compile(START_CONTENT_REGEX).matcher(content);
        if (!matcher.find()) {
            return null;
        }
        ContentParseData parseData = new ContentParseData();
        parseData.setPrompt(matcher.group(1));
        parseData.setStatus(matcher.group(2));
        return parseData;
    }

    private UVContentParseData parse(String content) {
        UVContentParseData data = parse(content, CONTENT_REGEX);
        if (data == null) {
            return parse(content, OLD_CONTENT_REGEX);
        }
        return data;
    }

    private UVContentParseData parse(String content, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(content);
        if (!matcher.find()) {
            return null;
        }
        UVContentParseData parseData = new UVContentParseData();
        parseData.setPrompt(matcher.group(1));
        parseData.setStatus(matcher.group(2));
        return parseData;
    }
}
