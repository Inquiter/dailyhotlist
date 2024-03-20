package com.dailyhotlist.controller;

import com.dailyhotlist.model.Feedback;
import com.dailyhotlist.service.FeedbackService;
import com.dailyhotlist.vo.FeedbackVo;
import com.dailyhotlist.vo.UserVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {
    @Resource
    private FeedbackService feedbackService;

    @PostMapping("/selectFeedback")
    public String selectFeedback() {
        return "userMessage";
    }

    @PostMapping("/insertFeedback")
    @ResponseBody
    public String insertFeedback(String feedbackTitle, String feedbackContent, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if ((feedbackTitle == null || feedbackTitle.trim().length() == 0) && (feedbackContent == null || feedbackContent.trim().length() == 0))
            return "请输入反馈标题和反馈内容!";
        else if (feedbackTitle == null || feedbackTitle.trim().length() == 0) return "请输入反馈标题!";
        else if (feedbackContent == null || feedbackContent.trim().length() == 0) return "请输入反馈内容!";
        else {
            UserVo userVo = (UserVo) request.getSession().getAttribute("userVo");
            if (feedbackService.addFeedback(feedbackTitle, feedbackContent, userVo.getId())) {
                userVo.setFeedbackVoList(feedbackService.selectFeedbackByUserId(userVo.getId()));
                return "反馈提交成功!";
            } else return "反馈提交失败!请联系开发人员!";
        }
    }

    @PostMapping("/detail")
    public String detail(@RequestParam("id") int id, Model model, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) {
            model.addAttribute("userLoginMsg", "用户未登录!请登录后重试!");
            return "redirect:/";
        } else {
            UserVo userVo = (UserVo) request.getSession().getAttribute("userVo");
            for (FeedbackVo feedbackVo : userVo.getFeedbackVoList())
                if (feedbackVo.getId() == id) {
                    model.addAttribute("feedbackVo", feedbackVo);
                    model.addAttribute("feedbackMsg", "反馈信息获取成功!");
                    return "feedback";
                }
            model.addAttribute("feedbackMsg", "反馈信息未找到!请联系开发人员!");
            return "feedback";
        }
    }

    @PostMapping("/addFeedback")
    @ResponseBody
    public String addFeedback(String feedbackTitle, String feedbackContent, String userId, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (feedbackTitle == null || feedbackTitle.trim().length() == 0) return "反馈标题不能为空!";
        else if (feedbackContent == null || feedbackContent.trim().length() == 0) return "反馈内容不能为空!";
        else if (userId == null || userId.trim().length() == 0) return "反馈用户ID不能为空!";
        else if (feedbackService.addFeedback(feedbackTitle.trim(), feedbackContent.trim(), userId.trim())) return "添加成功!";
        else return "添加失败!请联系开发人员!";
    }

    @PostMapping("/updateFeedback")
    @ResponseBody
    public String updateFeedback(int id, String feedbackTitle, String feedbackContent, String userId, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (feedbackTitle == null || feedbackTitle.trim().length() == 0) return "反馈标题不能为空!";
        else if (feedbackContent == null || feedbackContent.trim().length() == 0) return "反馈内容不能为空!";
        else if (userId == null || userId.trim().length() == 0) return "反馈用户ID不能为空!";
        else if (feedbackService.updateOneFeedback(new Feedback(id, feedbackTitle.trim(), feedbackContent.trim(), userId.trim()))) return "修改成功!";
        else return "修改失败!请联系开发人员!";
    }

    @PostMapping("/deleteFeedback")
    @ResponseBody
    public String deleteFeedback(int id, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (feedbackService.deleteOneFeedback(id)) return "删除成功!";
        else return "删除失败!请联系开发人员!";
    }
}
