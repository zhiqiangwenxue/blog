package com.iszhouhua.blog.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iszhouhua.blog.common.annotation.CurrentUser;
import com.iszhouhua.blog.common.constant.CodeEnum;
import com.iszhouhua.blog.common.constant.ConfigConst;
import com.iszhouhua.blog.common.util.IPUtils;
import com.iszhouhua.blog.common.util.Result;
import com.iszhouhua.blog.common.util.ValidatorUtils;
import com.iszhouhua.blog.model.enums.CommentStatusEnum;
import com.iszhouhua.blog.model.enums.CommentTargetTypeEnum;
import com.iszhouhua.blog.model.enums.DeleteEnum;
import com.iszhouhua.blog.model.pojo.Comment;
import com.iszhouhua.blog.model.pojo.User;
import com.iszhouhua.blog.service.CommentService;
import com.iszhouhua.blog.service.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 评论管理
 *
 * @author ZhouHua
 * @since 2018-12-01
 */
@RestController
@RequestMapping("api/comment")
public class ApiCommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private ConfigService configService;

    @GetMapping("list")
    public Result list(Page<Comment> page, Comment comment) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>(comment);
        String content = comment.getContent();
        comment.setContent(null);
        if (StringUtils.isNotBlank(content)) {
            queryWrapper.like("content", content);
        }
        if (Objects.isNull(comment.getIsDelete())) {
            queryWrapper.eq("is_delete", DeleteEnum.NOTDELETE.getValue());
        }
        IPage<Comment> commentPage = commentService.findCommentsByPage(page, queryWrapper);
        return Result.success(commentPage);
    }

    /**
     * 加载更多评论
     *
     * @param current   需要加载的页数
     * @param size      需要加载的数量
     * @param articleId 文章ID
     * @return
     */
    @GetMapping("more")
    public List<Comment> commentPage(int current, int size, long articleId) {
        return commentService.findPageByArticleId(new Page<>(current, size, false), articleId).getRecords();
    }

    @PostMapping(value = {"", "save"})
    public Result save(@RequestBody Comment comment, HttpServletRequest request, @CurrentUser User currentUser) {
        ValidatorUtils.validate(comment);
        if (comment.getTargetType().equals(CommentTargetTypeEnum.COMMENT.getValue())) {
            if (Objects.isNull(comment.getParentId())) {
                return Result.fail(CodeEnum.VALIDATION_ERROR.getValue(), "父级评论不能为空");
            }
            if (Objects.isNull(comment.getReplyUserId())) {
                return Result.fail(CodeEnum.VALIDATION_ERROR.getValue(), "回复的人不能为空");
            }
        }
//        User user = (User) request.getSession().getAttribute(Const.USER_SESSION_KEY);
        comment.setUserId(currentUser.getId());
        comment.setIsDelete(DeleteEnum.NOTDELETE.getValue());
        if (currentUser.getIsAdmin() == 1) {
            comment.setStatus(CommentStatusEnum.PUBLISHED.getValue());
        } else {
            Boolean isCheck = configService.getConfigObject(ConfigConst.COMMENT_CHECK, Boolean.class);
            comment.setStatus(isCheck ? CommentStatusEnum.CHECKING.getValue() : CommentStatusEnum.PUBLISHED.getValue());
        }
        comment.setUserAgent(request.getHeader("user-agent"));
        comment.setIp(IPUtils.getIpAddr(request));
        commentService.save(comment);
        commentService.clearCache();
        return Result.success(comment);
    }

    @PutMapping
    public Result update(@RequestBody Comment comment) {
        Boolean res = false;
        Comment commen = new Comment();
        if (Objects.isNull(comment.getId())) {
            return Result.fail(CodeEnum.VALIDATION_ERROR.getValue(), "评论ID不能为空");
        }
        Comment com = commentService.getById(comment);
        List<Comment> comments = commentService.getCommentByParentId(comment.getId());
        if (comments.size() > 0) {
            for (Comment comme : comments) {
                comme.setIsDelete(comme.getIsDelete() == 0 ? 1 : 0);
                commentService.updateById(comme);
            }
            com.setIsDelete(com.getIsDelete() == 0 ? 1 : 0);
            res = commentService.updateById(com);
        } else {
            com.setIsDelete(com.getIsDelete() == 0 ? 1 : 0);
            res = commentService.updateById(com);
        }
        commentService.clearCache();
        return res ? Result.success() : Result.fail("删除失败");
    }


    /**
     * 审核
     *
     * @param commentr
     * @return
     */
    @PostMapping("review")
    public Result review(@RequestBody Comment commentr) {
        Comment comment = commentService.getById(commentr.getId());
        BeanUtils.copyProperties(commentr, comment);
        boolean res = commentService.updateById(comment);
        commentService.clearCache();
        return res ? Result.success() : Result.fail("审核失败");
    }


    @GetMapping
    public Result info(Long id) {
        return Result.success(commentService.findCommentById(id));
    }

    /**
     * 获得最近一周评论数和总评论数
     *
     * @return
     */
    @GetMapping("commentCount")
    public Result commentCount() {
        Map<String, Integer> data = new HashMap<>();
        int totalComment = commentService.count(new QueryWrapper<Comment>().eq("is_delete", DeleteEnum.NOTDELETE.getValue()));
        data.put("totalComment", totalComment);
        int latestComment = commentService.count(new QueryWrapper<Comment>().eq("is_delete", DeleteEnum.NOTDELETE.getValue()).apply("create_time > DATE_SUB(CURDATE(), INTERVAL 1 WEEK)"));
        data.put("latestComment", latestComment);
        return Result.success(data);
    }

    /**
     * 获得最新的n条评论
     *
     * @param number 获取的数量
     * @return
     */
    @GetMapping("latest")
    public Result latest(int number) {
        List<Comment> commentList = commentService.findLatestComments(number, true);
        return Result.success(commentList);
    }
}
