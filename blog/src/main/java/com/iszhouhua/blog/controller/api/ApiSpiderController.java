package com.iszhouhua.blog.controller.api;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iszhouhua.blog.common.constant.CodeEnum;
import com.iszhouhua.blog.common.exception.BlogException;
import com.iszhouhua.blog.common.util.Result;
import com.iszhouhua.blog.common.util.ValidatorUtils;
import com.iszhouhua.blog.model.enums.DeleteEnum;
import com.iszhouhua.blog.model.pojo.Article;
import com.iszhouhua.blog.model.pojo.Spider;
import com.iszhouhua.blog.service.SpiderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

/**
 * 爬虫规则管理
 *
 * @author ZhouHua
 */
@RestController
@RequestMapping("api/spider")
public class ApiSpiderController {

    @Autowired
    private SpiderService spiderService;

    @GetMapping("list")
    public Result list(Page<Spider> page) {
        QueryWrapper<Spider> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", DeleteEnum.NOTDELETE.getValue());
        return Result.success(spiderService.page(page, queryWrapper));
    }

    @GetMapping("all")
    public Result all() {
        QueryWrapper<Spider> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", DeleteEnum.NOTDELETE.getValue());
        return Result.success(spiderService.list(queryWrapper));
    }

    @PostMapping
    public Result save(@RequestBody Spider spider) {
        ValidatorUtils.validate(spider);
        spider.setIsDelete(DeleteEnum.NOTDELETE.getValue());
        spiderService.save(spider);
        return Result.success(spider);
    }

    @PutMapping
    public Result update(@RequestBody Spider spider) {
        ValidatorUtils.validate(spider);
        if (Objects.isNull(spider.getId())) {
            return Result.fail("ID不能为空");
        }
        spiderService.updateById(spider);
        return Result.success();
    }

    @GetMapping
    public Result info(Long id) {
        return Result.success(spiderService.getById(id));
    }

    @DeleteMapping
    public Result remove(Long id) {
        Spider spider = spiderService.getById(id);
        spider.setIsDelete(DeleteEnum.DELETE.getValue());
        return spiderService.updateById(spider) ? Result.success() : Result.fail("删除失败");
    }

    @PostMapping("spiderArticle")
    public Result spiderArticle(Long id, String url) {
        Spider spider = spiderService.getById(id);
        if (StringUtils.isBlank(url)) {
            return Result.fail("文章链接不能为空");
        }
        if (spider == null) {
            return Result.fail("规则不能为空");
        }
        Article article;
        try {
            article = spiderService.spiderArticle(spider, url);
        } catch (IOException e) {
            throw new BlogException(CodeEnum.SPIDER_ERROR.getValue(), "文章抓取失败", e);
        }
        return article != null ? Result.success(article) : Result.fail("抓取失败");
    }
}

