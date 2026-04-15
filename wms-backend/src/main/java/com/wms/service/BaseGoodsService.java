package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.BaseGoods;
import com.wms.mapper.BaseGoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseGoodsService {

    private final BaseGoodsMapper baseGoodsMapper;

    public List<BaseGoods> list(LambdaQueryWrapper<BaseGoods> wrapper) {
        return baseGoodsMapper.selectList(wrapper);
    }

    public Page<BaseGoods> page(Integer pageNum, Integer pageSize, String goodsName, Integer status) {
        Page<BaseGoods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BaseGoods> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(goodsName)) {
            wrapper.like(BaseGoods::getGoodsName, goodsName);
        }
        if (status != null) {
            wrapper.eq(BaseGoods::getStatus, status);
        }
        wrapper.orderByDesc(BaseGoods::getCreateTime);
        return baseGoodsMapper.selectPage(page, wrapper);
    }

    public BaseGoods getById(Long id) {
        return baseGoodsMapper.selectById(id);
    }

    public void save(BaseGoods goods) {
        baseGoodsMapper.insert(goods);
    }

    public void update(BaseGoods goods) {
        baseGoodsMapper.updateById(goods);
    }

    public void delete(Long id) {
        baseGoodsMapper.deleteById(id);
    }
}
