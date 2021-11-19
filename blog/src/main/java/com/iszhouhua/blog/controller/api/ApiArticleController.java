package com.iszhouhua.blog.controller.api;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iszhouhua.blog.common.constant.CodeEnum;
import com.iszhouhua.blog.common.exception.BlogException;
import com.iszhouhua.blog.common.util.Result;
import com.iszhouhua.blog.common.util.ValidatorUtils;
import com.iszhouhua.blog.model.enums.ArticleStatusEnum;
import com.iszhouhua.blog.model.enums.DeleteEnum;
import com.iszhouhua.blog.model.pojo.Article;
import com.iszhouhua.blog.model.pojo.Comment;
import com.iszhouhua.blog.model.pojo.Tag;
import com.iszhouhua.blog.service.ArticleService;
import com.iszhouhua.blog.service.ArticleTagService;
import com.iszhouhua.blog.service.CommentService;
import com.iszhouhua.blog.service.TagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章管理
 *
 * @author ZhouHua
 * @since 2018-12-20
 */
@RestController
@RequestMapping("api/article")
public class ApiArticleController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private TagService tagService;
    @Autowired
    private ArticleTagService articleTagService;

    @Autowired
    private CommentService commentService;

    @GetMapping("list")
    public Result list(Page<Article> page, Article article) {
        //title需使用模糊查询，单独处理
        String title = article.getTitle();
        article.setTitle(null);
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>(article);
        if (article.getStatus() == null) {
            queryWrapper.in("status", ArticleStatusEnum.DRAFT.getValue(), ArticleStatusEnum.PUBLISHED.getValue());
        }
        queryWrapper.eq("is_delete", 0);
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(true, "title", title);
        }
        IPage<Article> articlePage = articleService.page(page, queryWrapper);
        articlePage.getRecords().forEach(post -> post.setTags(tagService.findTagsByArticleId(post.getId())));
        return Result.success(articlePage);
    }

    @PostMapping
    public Result save(@RequestBody Article article) {
        ValidatorUtils.validate(article);
        if (StringUtils.isBlank(article.getUrl())) {
            article.setUrl(article.getTitle());
        }
        article.setIsDelete(DeleteEnum.NOTDELETE.getValue());
        article.setUpdateTime(new Date());
        boolean res = articleService.saveOrUpdate(article);
        if (!res) {
            return Result.fail("保存失败");
        }
        if (!CollectionUtils.isEmpty(article.getTags())) {
            Long articleId = article.getId() != null ? article.getId() : articleService.findArticleByUrl(article.getUrl()).getId();
            List<Long> tagIds = article.getTags().stream().map(Tag::getId).collect(Collectors.toList());
            res = articleTagService.saveBatch(articleId, tagIds);
            if (!res) {
                return Result.fail("文章已成功保存，但关联标签保存失败");
            }
        }
        articleService.clearCache();
        return Result.success(article);
    }

    /**
     * 修改部分参数时使用，如状态、是否置顶等。
     *
     * @param article
     * @return
     */
    @PostMapping("modify")
    public Result modify(@RequestBody Article article) {
        if (article.getId() == null) {
            throw new BlogException(CodeEnum.VALIDATION_ERROR.getValue(), "修改数据时ID不能为空");
        }
        article.setUpdateTime(new Date());
        boolean res = articleService.updateById(article);
        if (!res) {
            return Result.fail("保存失败");
        }
        articleService.clearCache();
        return Result.success(article);
    }

    @GetMapping
    public Result info(Long id) {
        Article article = articleService.getById(id);
        article.setTags(tagService.findTagsByArticleId(article.getId()));
        return Result.success(article);
    }

    @DeleteMapping
    public Result remove(Long[] ids) {
        Article article = new Article();
        List<Comment> commentList = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        boolean res = false;
        for (int i = 0; i < ids.length; i++) {
            article = articleService.getById(ids[i]);
            commentList = commentService.findLatestComments(article.getId());
            article.setIsDelete(article.getIsDelete() == 0 ? 1 : 0);
            res = articleService.updateById(article);
        }

        for (int i = 0; i < commentList.size(); i++) {
            getList(commentList, commentList.get(i).getId());
        }

        articleService.clearCache();
        commentService.clearCache();
        return res ? Result.success() : Result.fail("删除失败");
    }

    public Boolean getList(List<Comment> list, Long id) {
        Boolean ok = false;
        List<Comment> commentList = new ArrayList<>();
        for (Comment comment : list) {
            if (id != null && id != 0) {
                if (comment.getParentId() == null) {
                    comment.setIsDelete(comment.getIsDelete() == 0 ? 1 : 0);
                    ok = commentService.updateById(comment);
                }
                if (comment.getParentId() == id) {
                    commentList.add(comment);
                    if (comment.getIsDelete() != 1) {
                        comment.setIsDelete(comment.getIsDelete() == 0 ? 1 : 0);
                        ok = commentService.updateById(comment);
                    }

                }
            }

        }
        for (Comment c : commentList) {
            getList(list, c.getId());
        }
        if (commentList.size() == 0) {
            ok = true;
            return ok;
        }
        return ok;
    }


    /**
     * 查询最新的文章
     *
     * @param number 需要查询的条数
     * @return
     */
    @GetMapping("latest")
    public Result latest(int number) {
        List<Article> articleList = articleService.findLatestArticle(number);
        return Result.success(articleList);
    }

}
