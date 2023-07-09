package com.github.novicezk.midjourney.controller;

import cn.hutool.core.comparator.CompareUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.novicezk.midjourney.dto.TaskConditionDTO;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskQueueHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "任务查询")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskStoreService taskStoreService;
    private final TaskQueueHelper taskQueueHelper;

    @ApiOperation(value = "查询所有任务")
    @GetMapping("/list")
    public List<Task> list() {
        return this.taskStoreService.list().stream()
                .sorted((t1, t2) -> CompareUtil.compare(t2.getSubmitTime(), t1.getSubmitTime()))
                .toList();
    }

    @ApiOperation(value = "指定ID获取任务")
    @GetMapping("/{id}/fetch")
    public Task fetch(@ApiParam(value = "任务ID") @PathVariable String id) {
        Task task = this.taskStoreService.get(id);
        try {
            String json = new ObjectMapper().writeValueAsString(task);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return task;
    }

    @ApiOperation(value = "查询任务队列")
    @GetMapping("/queue")
    public List<Task> queue() {
        Set<String> queueTaskIds = this.taskQueueHelper.getQueueTaskIds();
        return queueTaskIds.stream().map(this.taskStoreService::get).filter(Objects::nonNull)
                .sorted(Comparator.comparing(Task::getSubmitTime))
                .toList();
    }

    @ApiOperation(value = "根据条件查询任务")
    @PostMapping("/list-by-condition")
    public List<Task> listByCondition(@RequestBody TaskConditionDTO conditionDTO) {
        if (conditionDTO.getIds() == null) {
            return Collections.emptyList();
        }
        return conditionDTO.getIds().stream().map(this.taskStoreService::get).filter(Objects::nonNull).toList();
    }


    @ApiOperation(value = "保存任务")
    @PostMapping("/saveList")
    public boolean saveList(@RequestBody List<Task> taskList) {
        for (Task task : taskList) {
            this.taskStoreService.save(task);
        }
        return true;
    }

//    @ApiOperation(value = "以json字符串导出所有任务")
//    @GetMapping("/exportAll")
//    public String exportAll() throws JsonProcessingException {
//        List<Task> list = list();
//        String json = new ObjectMapper().writeValueAsString(list);
//        return json;
//    }

}
