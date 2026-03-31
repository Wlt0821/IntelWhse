package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.PickingTask;
import com.wms.mapper.PickingTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PickingTaskService {

    private final PickingTaskMapper pickingTaskMapper;

    public Page<PickingTask> page(Integer pageNum, Integer pageSize, String taskNo, String status) {
        Page<PickingTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PickingTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(taskNo)) {
            wrapper.like(PickingTask::getTaskNo, taskNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PickingTask::getStatus, status);
        }
        wrapper.orderByDesc(PickingTask::getCreateTime);
        return pickingTaskMapper.selectPage(page, wrapper);
    }

    public PickingTask getById(Long id) {
        return pickingTaskMapper.selectById(id);
    }

    public void save(PickingTask task) {
        if (!StringUtils.hasText(task.getTaskNo())) {
            task.setTaskNo("PT" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(task.getStatus())) {
            task.setStatus("PENDING");
        }
        pickingTaskMapper.insert(task);
    }

    public void update(PickingTask task) {
        pickingTaskMapper.updateById(task);
    }

    public void delete(Long id) {
        pickingTaskMapper.deleteById(id);
    }
}
