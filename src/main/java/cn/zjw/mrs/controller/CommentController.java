package cn.zjw.mrs.controller;

import cn.zjw.mrs.entity.Comment;
import cn.zjw.mrs.entity.LoginUser;
import cn.zjw.mrs.entity.Result;
import cn.zjw.mrs.service.CommentService;
import cn.zjw.mrs.service.RecommendationService;
import cn.zjw.mrs.vo.comment.CommentMovieVo;
import cn.zjw.mrs.vo.comment.CommentStripVo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author zjw
 * @Classname CommentController
 * @Date 2022/4/14 19:20
 * @Description
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    @Resource
    private RecommendationService recommendationService;

    @PostMapping
    private Result<?> addComment(@RequestBody Comment comment, Principal principal, Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long uid = loginUser.getUser().getId();

        int update = commentService.addComment(comment, principal.getName());
        switch (update) {
            case -1: return Result.error("评价更新失败，请稍后重试(┬┬﹏┬┬)");
            case 1: {
                recommendationService.updateRecommendation(uid);
                return Result.success("评价更新成功(‾◡◝)");
            }
            case -2: return Result.error("评价失败(┬┬﹏┬┬)");
            default: {
                recommendationService.updateRecommendation(uid);
                return Result.success("评价成功(‾◡◝)");
            }
        }
    }

    @GetMapping("/own")
    private Result<?> getOwnComment(@RequestParam Long mid, Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long uid = loginUser.getUser().getId();
        CommentStripVo ownComment = commentService.getOwnComment(uid, mid);
        if (Objects.isNull(ownComment)) {
            return Result.error("该用户还未评论");
        }
        return Result.success(ownComment);
    }

    @GetMapping("/all")
    private Result<?> getMoreCommentsByMovieId(@RequestParam Long mid,
                                               @RequestParam(defaultValue = "0") int currentPage,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        List<CommentStripVo> comments = commentService.getMoreCommentsByMovieId(mid, currentPage, pageSize);
        if (Objects.isNull(comments)) {
            return Result.error("该电影下暂无评论");
        }
        return Result.success(comments);
    }

    @GetMapping("/movie/moments")
    private Result<?> getOwnCommentMovieMoments(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long uid = loginUser.getUser().getId();
        List<CommentMovieVo> moments = commentService.getOwnCommentMovieMoments(uid);
        if (Objects.isNull(moments)) {
            return Result.error("暂无评价动态");
        }
        return Result.success(moments);
    }

    @DeleteMapping("/remove")
    private Result<?> removeOwnComment(@RequestBody Comment comment, Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long uid = loginUser.getUser().getId();
        int delete = commentService.removeOwnComment(uid, comment.getMid());
        if (delete == 0) {
            return Result.error("短评删除失败(┬┬﹏┬┬)");
        }
        return Result.success("短评删除成功(‾◡◝)");
    }
}
