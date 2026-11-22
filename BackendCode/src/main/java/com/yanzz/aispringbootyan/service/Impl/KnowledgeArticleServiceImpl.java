package com.yanzz.aispringbootyan.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanzz.aispringbootyan.DTO.command.KnowledgeArticleCommandDTO;
import com.yanzz.aispringbootyan.DTO.response.KnowledgeArticleVO;
import com.yanzz.aispringbootyan.common.ResultCode;
import com.yanzz.aispringbootyan.entity.KnowledgeArticle;
import com.yanzz.aispringbootyan.entity.KnowledgeCategory;
import com.yanzz.aispringbootyan.entity.User;
import com.yanzz.aispringbootyan.exception.BusinessException;
import com.yanzz.aispringbootyan.mapper.KnowledgeArticleMapper;
import com.yanzz.aispringbootyan.mapper.KnowledgeCategoryMapper;
import com.yanzz.aispringbootyan.mapper.UserMapper;
import com.yanzz.aispringbootyan.service.KnowledgeArticleService;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KnowledgeArticleServiceImpl implements KnowledgeArticleService {

    @Resource
    private KnowledgeArticleMapper knowledgeArticleMapper;

    @Resource
    private KnowledgeCategoryMapper knowledgeCategoryMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Page<KnowledgeArticleVO> pageArticles(boolean admin, long current, long size, String title,
                                                 Long categoryId, Integer status, String sortField, String sortDirection) {
        Page<KnowledgeArticle> page = new Page<>(current, size);
        LambdaQueryWrapper<KnowledgeArticle> queryWrapper = new LambdaQueryWrapper<>();

        if (admin) {
            // 管理员可以按状态筛选，不传则看全部
            queryWrapper.eq(status != null, KnowledgeArticle::getStatus, status);
        } else {
            // 前台只展示已发布文章
            queryWrapper.eq(KnowledgeArticle::getStatus, 1);
        }
        queryWrapper.like(StrUtil.isNotBlank(title), KnowledgeArticle::getTitle, title)
                .eq(categoryId != null, KnowledgeArticle::getCategoryId, categoryId);

        applySort(queryWrapper, sortField, sortDirection);

        Page<KnowledgeArticle> articlePage = knowledgeArticleMapper.selectPage(page, queryWrapper);
        List<KnowledgeArticle> articles = articlePage.getRecords();

        Map<Long, KnowledgeCategory> categoryMap = loadCategories(articles);
        Map<Long, User> authorMap = loadAuthors(articles);

        List<KnowledgeArticleVO> records = articles.stream()
                .map(article -> convertToVO(article,
                        categoryMap.get(article.getCategoryId()),
                        authorMap.get(article.getAuthorId())))
                .collect(Collectors.toList());

        Page<KnowledgeArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public KnowledgeArticleVO getArticle(String id, boolean admin) {
        KnowledgeArticle article = knowledgeArticleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "文章不存在");
        }
        if (!admin && !Integer.valueOf(1).equals(article.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "文章不存在或未发布");
        }

        if (!admin) {
            // 普通用户阅读时累加阅读量；显式写 updated_at 防止 MySQL 的 ON UPDATE 改掉发布时间
            LambdaUpdateWrapper<KnowledgeArticle> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KnowledgeArticle::getId, id)
                    .setSql("read_count = read_count + 1, updated_at = updated_at");
            knowledgeArticleMapper.update(null, updateWrapper);
            article.setReadCount((article.getReadCount() == null ? 0 : article.getReadCount()) + 1);
        }

        return convertToVO(article,
                knowledgeCategoryMapper.selectById(article.getCategoryId()),
                userMapper.selectById(article.getAuthorId()));
    }

    @Override
    public KnowledgeArticleVO createArticle(Long authorId, KnowledgeArticleCommandDTO knowledgeArticleCommandDTO) {
        checkCategoryExists(knowledgeArticleCommandDTO.getCategoryId());

        String id = StrUtil.isBlank(knowledgeArticleCommandDTO.getId())
                ? UUID.randomUUID().toString()
                : knowledgeArticleCommandDTO.getId();
        LocalDateTime now = LocalDateTime.now();

        KnowledgeArticle article = KnowledgeArticle.builder()
                .id(id)
                .categoryId(knowledgeArticleCommandDTO.getCategoryId())
                .title(knowledgeArticleCommandDTO.getTitle())
                .summary(knowledgeArticleCommandDTO.getSummary())
                .content(knowledgeArticleCommandDTO.getContent())
                .coverImage(knowledgeArticleCommandDTO.getCoverImage())
                .tags(knowledgeArticleCommandDTO.getTags())
                .authorId(authorId)
                .readCount(0)
                // 和表默认值保持一致：新增即发布
                .status(1)
                .publishedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            knowledgeArticleMapper.insert(article);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "文章ID已存在");
        }

        return convertToVO(article,
                knowledgeCategoryMapper.selectById(article.getCategoryId()),
                userMapper.selectById(article.getAuthorId()));
    }

    @Override
    public KnowledgeArticleVO updateArticle(String id, KnowledgeArticleCommandDTO knowledgeArticleCommandDTO) {
        KnowledgeArticle existing = knowledgeArticleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "文章不存在");
        }
        checkCategoryExists(knowledgeArticleCommandDTO.getCategoryId());

        // 用 UpdateWrapper 显式 set，允许把摘要、封面、标签清空
        LambdaUpdateWrapper<KnowledgeArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KnowledgeArticle::getId, id)
                .set(KnowledgeArticle::getCategoryId, knowledgeArticleCommandDTO.getCategoryId())
                .set(KnowledgeArticle::getTitle, knowledgeArticleCommandDTO.getTitle())
                .set(KnowledgeArticle::getSummary, knowledgeArticleCommandDTO.getSummary())
                .set(KnowledgeArticle::getContent, knowledgeArticleCommandDTO.getContent())
                .set(KnowledgeArticle::getCoverImage, knowledgeArticleCommandDTO.getCoverImage())
                .set(KnowledgeArticle::getTags, knowledgeArticleCommandDTO.getTags())
                .set(KnowledgeArticle::getUpdatedAt, LocalDateTime.now());
        knowledgeArticleMapper.update(null, updateWrapper);

        KnowledgeArticle updated = knowledgeArticleMapper.selectById(id);
        return convertToVO(updated,
                knowledgeCategoryMapper.selectById(updated.getCategoryId()),
                userMapper.selectById(updated.getAuthorId()));
    }

    @Override
    public void changeStatus(String id, Integer status) {
        KnowledgeArticle existing = knowledgeArticleMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "文章不存在");
        }

        LambdaUpdateWrapper<KnowledgeArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KnowledgeArticle::getId, id)
                .set(KnowledgeArticle::getStatus, status)
                .set(KnowledgeArticle::getUpdatedAt, LocalDateTime.now());
        // 首次发布时补发布时间
        if (Integer.valueOf(1).equals(status) && existing.getPublishedAt() == null) {
            updateWrapper.set(KnowledgeArticle::getPublishedAt, LocalDateTime.now());
        }
        knowledgeArticleMapper.update(null, updateWrapper);
    }

    @Override
    public void deleteArticle(String id) {
        int deleted = knowledgeArticleMapper.deleteById(id);
        if (deleted == 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "文章不存在");
        }
    }

    private void checkCategoryExists(Long categoryId) {
        if (categoryId == null || knowledgeCategoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "文章分类不存在");
        }
    }

    // 排序字段用白名单映射，避免前端传任意列名
    private void applySort(LambdaQueryWrapper<KnowledgeArticle> queryWrapper, String sortField, String sortDirection) {
        SFunction<KnowledgeArticle, ?> column = switch (StrUtil.blankToDefault(sortField, "")) {
            case "readCount" -> KnowledgeArticle::getReadCount;
            case "createdAt" -> KnowledgeArticle::getCreatedAt;
            case "updatedAt" -> KnowledgeArticle::getUpdatedAt;
            default -> KnowledgeArticle::getPublishedAt;
        };
        boolean asc = "asc".equalsIgnoreCase(sortDirection);
        queryWrapper.orderBy(true, asc, column)
                .orderByDesc(KnowledgeArticle::getId);
    }

    private Map<Long, KnowledgeCategory> loadCategories(List<KnowledgeArticle> articles) {
        Set<Long> categoryIds = articles.stream()
                .map(KnowledgeArticle::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return new HashMap<>();
        }
        return knowledgeCategoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(KnowledgeCategory::getId, category -> category, (a, b) -> a));
    }

    private Map<Long, User> loadAuthors(List<KnowledgeArticle> articles) {
        Set<Long> authorIds = articles.stream()
                .map(KnowledgeArticle::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (authorIds.isEmpty()) {
            return new HashMap<>();
        }
        return userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));
    }

    private KnowledgeArticleVO convertToVO(KnowledgeArticle article, KnowledgeCategory category, User author) {
        KnowledgeArticleVO vo = new KnowledgeArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setContent(article.getContent());
        vo.setCoverImage(article.getCoverImage());
        vo.setCategoryId(article.getCategoryId());
        vo.setCategoryName(category != null ? category.getCategoryName() : "");
        vo.setTags(article.getTags());
        vo.setTagArray(splitTags(article.getTags()));
        vo.setAuthorId(article.getAuthorId());
        vo.setAuthorName(author != null ? author.getDisplayName() : "未知作者");
        vo.setReadCount(article.getReadCount());
        vo.setStatus(article.getStatus());
        vo.setPublishedAt(article.getPublishedAt());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());
        return vo;
    }

    private List<String> splitTags(String tags) {
        if (StrUtil.isBlank(tags)) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
