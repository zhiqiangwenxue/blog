package com.iszhouhua.blog.controller.api;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iszhouhua.blog.common.util.Result;
import com.iszhouhua.blog.common.util.ValidatorUtils;
import com.iszhouhua.blog.model.enums.DeleteEnum;
import com.iszhouhua.blog.model.pojo.Link;
import com.iszhouhua.blog.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 链接管理
 *
 * @author ZhouHua
 * @since 2018-04-13
 */
@RestController
@RequestMapping("api/link")
public class ApLinkController {
    @Autowired
    private LinkService linkService;

    @GetMapping("list")
    public Result list(Page<Link> page) {
        QueryWrapper<Link> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", DeleteEnum.NOTDELETE.getValue());
        return Result.success(linkService.page(page, queryWrapper));
    }

    @PostMapping
    public Result save(@RequestBody Link link) {
        ValidatorUtils.validate(link);
        link.setIsDelete(DeleteEnum.NOTDELETE.getValue());
        boolean res = linkService.save(link);
        linkService.clearCache();
        return res ? Result.success(link) : Result.fail("保存失败");
    }

    @PutMapping
    public Result update(@RequestBody Link link) {
        ValidatorUtils.validate(link);
        if (Objects.isNull(link.getId())) {
            return Result.fail("ID不能为空");
        }
        boolean res = linkService.updateById(link);
        linkService.clearCache();
        return res ? Result.success() : Result.fail("修改失败");
    }

    @GetMapping
    public Result info(Long id) {
        return Result.success(linkService.getById(id));
    }

    @DeleteMapping
    public Result remove(Long id) {
        Link link = linkService.getById(id);
        link.setIsDelete(DeleteEnum.DELETE.getValue());
        boolean res = linkService.updateById(link);
        linkService.clearCache();
        return res ? Result.success() : Result.fail("删除失败");
    }
}
