package com.yanzz.aispringbootyan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanzz.aispringbootyan.DTO.command.KnowledgeArticleCommandDTO;
import com.yanzz.aispringbootyan.DTO.response.KnowledgeArticleVO;

public interface KnowledgeArticleService {

    // 分页查询：管理员可看全部状态并筛选，普通用户只能看已发布
    Page<KnowledgeArticleVO> pageArticles(boolean admin, long current, long size, String title,
                                          Long categoryId, Integer status, String sortField, String sortDirection);

    // 文章详情：普通用户只能看已发布，且会累加阅读量
    KnowledgeArticleVO getArticle(String id, boolean admin);

    // 新增文章
    KnowledgeArticleVO createArticle(Long authorId, KnowledgeArticleCommandDTO knowledgeArticleCommandDTO);

    // 编辑文章
    KnowledgeArticleVO updateArticle(String id, KnowledgeArticleCommandDTO knowledgeArticleCommandDTO);

    // 修改发布状态
    void changeStatus(String id, Integer status);

    // 删除文章
    void deleteArticle(String id);
}
