package com.yanzz.aispringbootyan.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanzz.aispringbootyan.DTO.response.KnowledgeCategoryVO;
import com.yanzz.aispringbootyan.entity.KnowledgeCategory;
import com.yanzz.aispringbootyan.mapper.KnowledgeCategoryMapper;
import com.yanzz.aispringbootyan.service.KnowledgeCategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeCategoryServiceImpl implements KnowledgeCategoryService {

    @Resource
    private KnowledgeCategoryMapper knowledgeCategoryMapper;

    @Override
    public List<KnowledgeCategoryVO> listEnabledCategories() {
        LambdaQueryWrapper<KnowledgeCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeCategory::getStatus, 1)
                .orderByAsc(KnowledgeCategory::getSortOrder)
                .orderByAsc(KnowledgeCategory::getId);
        return knowledgeCategoryMapper.selectList(queryWrapper)
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private KnowledgeCategoryVO convertToVO(KnowledgeCategory category) {
        KnowledgeCategoryVO vo = new KnowledgeCategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setCategoryName(category.getCategoryName());
        vo.setCategoryCode(category.getCategoryCode());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());
        return vo;
    }
}
