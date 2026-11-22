package com.yanzz.aispringbootyan.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanzz.aispringbootyan.DTO.command.KnowledgeArticleCommandDTO;
import com.yanzz.aispringbootyan.DTO.command.KnowledgeArticleStatusDTO;
import com.yanzz.aispringbootyan.DTO.response.KnowledgeArticleVO;
import com.yanzz.aispringbootyan.DTO.response.KnowledgeCategoryVO;
import com.yanzz.aispringbootyan.common.Result;
import com.yanzz.aispringbootyan.service.KnowledgeArticleService;
import com.yanzz.aispringbootyan.service.KnowledgeCategoryService;
import com.yanzz.aispringbootyan.utils.CurrentUserUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Resource
    private KnowledgeArticleService knowledgeArticleService;

    @Resource
    private KnowledgeCategoryService knowledgeCategoryService;

    // 分类树：目前返回启用的扁平分类列表，前端按下拉选项使用
    @GetMapping("/category/tree")
    public Result<List<KnowledgeCategoryVO>> categoryTree() {
        return Result.ok(knowledgeCategoryService.listEnabledCategories());
    }

    // 文章分页：管理员看全部，普通用户只看已发布
    @GetMapping("/article/page")
    public Result<Page<KnowledgeArticleVO>> pageArticles(
            @RequestParam(value = "currentPage", required = false) Long currentPage,
            @RequestParam(value = "pageNum", required = false) Long pageNum,
            @RequestParam(value = "size", required = false) Long size,
            @RequestParam(value = "pageSize", required = false) Long pageSize,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortDirection", required = false) String sortDirection) {
        long current = currentPage != null ? currentPage : (pageNum != null ? pageNum : 1L);
        long pageSizeValue = size != null ? size : (pageSize != null ? pageSize : 10L);
        current = Math.max(current, 1L);
        pageSizeValue = pageSizeValue < 1L || pageSizeValue > 100L ? 10L : pageSizeValue;

        return Result.ok(knowledgeArticleService.pageArticles(
                CurrentUserUtil.isAdmin(), current, pageSizeValue,
                title, categoryId, status, sortField, sortDirection));
    }

    // 文章详情：普通用户只能看已发布文章
    @GetMapping("/article/{id}")
    public Result<KnowledgeArticleVO> getArticle(@PathVariable String id) {
        return Result.ok(knowledgeArticleService.getArticle(id, CurrentUserUtil.isAdmin()));
    }

    // 新增文章（管理员）
    @PostMapping("/article")
    public Result<KnowledgeArticleVO> createArticle(@Valid @RequestBody KnowledgeArticleCommandDTO knowledgeArticleCommandDTO) {
        CurrentUserUtil.requireAdmin();
        return Result.ok(knowledgeArticleService.createArticle(
                CurrentUserUtil.getUserId(), knowledgeArticleCommandDTO));
    }

    // 编辑文章（管理员）
    @PutMapping("/article/{id}")
    public Result<KnowledgeArticleVO> updateArticle(@PathVariable String id,
                                                    @Valid @RequestBody KnowledgeArticleCommandDTO knowledgeArticleCommandDTO) {
        CurrentUserUtil.requireAdmin();
        return Result.ok(knowledgeArticleService.updateArticle(id, knowledgeArticleCommandDTO));
    }

    // 发布 / 下线 / 草稿（管理员）
    @PutMapping("/article/{id}/status")
    public Result<Void> changeStatus(@PathVariable String id,
                                     @Valid @RequestBody KnowledgeArticleStatusDTO knowledgeArticleStatusDTO) {
        CurrentUserUtil.requireAdmin();
        knowledgeArticleService.changeStatus(id, knowledgeArticleStatusDTO.getStatus());
        return Result.ok();
    }

    // 删除文章（管理员）
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable String id) {
        CurrentUserUtil.requireAdmin();
        knowledgeArticleService.deleteArticle(id);
        return Result.ok();
    }
}
