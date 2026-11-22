package com.yanzz.aispringbootyan.service;

import com.yanzz.aispringbootyan.DTO.response.KnowledgeCategoryVO;

import java.util.List;

public interface KnowledgeCategoryService {

    // 查询启用的分类，按排序号升序；前端目前按扁平列表使用
    List<KnowledgeCategoryVO> listEnabledCategories();
}
