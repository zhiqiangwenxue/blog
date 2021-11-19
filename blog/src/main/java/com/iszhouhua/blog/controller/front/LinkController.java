package com.iszhouhua.blog.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iszhouhua.blog.model.enums.DeleteEnum;
import com.iszhouhua.blog.model.enums.LinkTypeEnum;
import com.iszhouhua.blog.model.pojo.Link;
import com.iszhouhua.blog.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author Yzq
 * @date 2021/11/16
 */
@Controller
@RequestMapping("link")
public class LinkController extends BaseController {

    @Autowired
    private LinkService linkService;

    @GetMapping("/")
    public String link(Model model) {
        QueryWrapper<Link> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", DeleteEnum.NOTDELETE.getValue());
        queryWrapper.eq("type", LinkTypeEnum.FRIEND_LINK.getValue());
        List<Link> links = linkService.list(queryWrapper);
        model.addAttribute("list", links);
        return "links";
    }
}
