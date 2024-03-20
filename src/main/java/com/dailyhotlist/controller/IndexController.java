package com.dailyhotlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class IndexController {
    @RequestMapping("/")
    public String index(HttpServletRequest request, HttpSession session, Model model) {
        if (session.getAttribute("userVo") != null) request.getSession().setAttribute("userVo", session.getAttribute("userVo"));
        if (session.getAttribute("userLoginMsg") != null) {
            model.addAttribute("userLoginMsg", session.getAttribute("userLoginMsg"));
            session.setAttribute("userLoginMsg", null);
        }
        if (session.getAttribute("userRegisterMsg") != null) {
            model.addAttribute("userRegisterMsg", session.getAttribute("userRegisterMsg"));
            session.setAttribute("userRegisterMsg", null);
        }
        return "forward:/hotList/selectHotList";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") != null) {
            request.getSession().removeAttribute("userVo");
            if (request.getSession().getAttribute("userList") != null) request.getSession().removeAttribute("userList");
            if (request.getSession().getAttribute("hotListList") != null) request.getSession().removeAttribute("hotListList");
            if (request.getSession().getAttribute("hotListDataList") != null) request.getSession().removeAttribute("hotListDataList");
            if (request.getSession().getAttribute("subscribeList") != null) request.getSession().removeAttribute("subscribeList");
            if (request.getSession().getAttribute("feedbackList") != null) request.getSession().removeAttribute("feedbackList");
        }
        return "redirect:/";
    }
}
