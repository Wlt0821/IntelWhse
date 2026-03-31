package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.InboundTask;
import com.wms.mapper.InboundTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class InboundTaskService {

    private final InboundTaskMapper inboundTaskMapper;

    public Page<InboundTask> page(Integer pageNum, Integer pageSize, String taskNo, String status) {
        Page<InboundTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InboundTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(taskNo)) {
            wrapper.like(InboundTask::getTaskNo, taskNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(InboundTask::getStatus, status);
        }
        wrapper.orderByDesc(InboundTask::getCreateTime);
        return inboundTaskMapper.selectPage(page, wrapper);
    }

    public InboundTask getById(Long id) {
        return inboundTaskMapper.selectById(id);
    }

    public void save(InboundTask task) {
        if (!StringUtils.hasText(task.getTaskNo())) {
            task.setTaskNo("IT" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(task.getStatus())) {
            task.setStatus("PENDING");
        }
        inboundTaskMapper.insert(task);
    }

    public void update(InboundTask task) {
        inboundTaskMapper.updateById(task);
    }

    public void delete(Long id) {
        inboundTaskMapper.deleteById(id);
    }
}
