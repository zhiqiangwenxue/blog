package com.iszhouhua.blog.controller.api;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iszhouhua.blog.common.util.Result;
import com.iszhouhua.blog.common.util.ValidatorUtils;
import com.iszhouhua.blog.model.enums.DeleteEnum;
import com.iszhouhua.blog.model.pojo.Tag;
import com.iszhouhua.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 标签管理
 *
 * @author ZhouHua
 * @since 2018-12-01
 */
@RestController
@RequestMapping("api/tag")
public class ApiTagController {
    @Autowired
    private TagService tagService;

    @GetMapping("list")
    public Result list(Page<Tag> page) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", DeleteEnum.NOTDELETE.getValue());
        return Result.success(tagService.page(page, queryWrapper));
    }

    @GetMapping("all")
    public Result all() {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", DeleteEnum.NOTDELETE.getValue());
        return Result.success(tagService.list(queryWrapper));
    }

    @PostMapping
    public Result save(@RequestBody Tag tag) {
        ValidatorUtils.validate(tag);
        tag.setIsDelete(DeleteEnum.NOTDELETE.getValue());
        boolean res = tagService.save(tag);
        tagService.clearCache();
        return res ? Result.success(tag) : Result.fail("保存失败");
    }

    @PutMapping
    public Result update(@RequestBody Tag tag) {
        ValidatorUtils.validate(tag);
        if (Objects.isNull(tag.getId())) {
            return Result.fail("ID不能为空");
        }
        boolean res = tagService.updateById(tag);
        tagService.clearCache();
        return res ? Result.success() : Result.fail("修改失败");
    }

    @GetMapping
    public Result info(Long id) {
        return Result.success(tagService.getById(id));
    }

    @DeleteMapping
    public Result remove(Long id) {
        Tag tag = tagService.getById(id);
        tag.setIsDelete(tag.getIsDelete() == 1 ? 0 : 1);
        boolean res = tagService.updateById(tag);
        tagService.clearCache();
        return res ? Result.success() : Result.fail("删除失败");
    }
}
