package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Packing;
import com.wms.mapper.PackingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PackingService {

    private final PackingMapper packingMapper;

    public Page<Packing> page(Integer pageNum, Integer pageSize, String packingNo, String status) {
        Page<Packing> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Packing> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(packingNo)) {
            wrapper.like(Packing::getPackingNo, packingNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Packing::getStatus, status);
        }
        wrapper.orderByDesc(Packing::getCreateTime);
        return packingMapper.selectPage(page, wrapper);
    }

    public Packing getById(Long id) {
        return packingMapper.selectById(id);
    }

    public void save(Packing packing) {
        if (!StringUtils.hasText(packing.getPackingNo())) {
            packing.setPackingNo("PK" + System.currentTimeMillis());
        }
        if (!StringUtils.hasText(packing.getStatus())) {
            packing.setStatus("DRAFT");
        }
        packingMapper.insert(packing);
    }

    public void update(Packing packing) {
        packingMapper.updateById(packing);
    }

    public void delete(Long id) {
        packingMapper.deleteById(id);
    }
}
