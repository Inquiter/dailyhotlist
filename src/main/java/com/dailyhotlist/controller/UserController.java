package com.dailyhotlist.controller;

import com.dailyhotlist.model.User;
import com.dailyhotlist.service.*;
import com.dailyhotlist.vo.UserVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@Controller
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private HotListService hotListService;

    @Resource
    private HotListDataService hotListDataService;

    @Resource
    private SubscribeService subscribeService;

    @Resource
    private FeedbackService feedbackService;

    @RequestMapping("/login")
    public String login(HttpServletRequest request, HttpSession session, Model model) {
        if (session.getAttribute("userVo") != null) request.getSession().setAttribute("userVo", session.getAttribute("userVo"));
        if (session.getAttribute("userLoginMsg") != null) {
            model.addAttribute("userLoginMsg", session.getAttribute("userLoginMsg"));
            session.setAttribute("userLoginMsg", null);
        }
        return "login";
    }

    @PostMapping("/userLogin")
    public String userLogin(@RequestParam("username") String username, @RequestParam("password") String password, HttpSession session) {
        if ((username == null || username.trim().length() == 0) && (password == null || password.trim().length() == 0))
            session.setAttribute("userLoginMsg", "请输入用户名和密码!");
        else if (username == null || username.trim().length() == 0) session.setAttribute("userLoginMsg", "请输入用户名!");
        else if (password == null || password.trim().length() == 0) session.setAttribute("userLoginMsg", "请输入密码!");
        else {
            UserVo userVo = userService.selectOneUser(username.trim());
            if (userVo == null) session.setAttribute("userLoginMsg", "用户不存在!请先注册!");
            else if (!userService.decrypt(userVo.getPassword()).equals(password.trim())) session.setAttribute("userLoginMsg", "用户名或密码错误!");
            else {
                session.setAttribute("userLoginMsg", "登录成功!");
                session.setAttribute("userVo", userVo);
                return "redirect:/";
            }
        }
        return "redirect:/user/login";
    }

    @RequestMapping("/register")
    public String register(HttpServletRequest request, HttpSession session, Model model) {
        if (session.getAttribute("userVo") != null) request.getSession().setAttribute("userVo", session.getAttribute("userVo"));
        if (session.getAttribute("userRegisterMsg") != null) {
            model.addAttribute("userRegisterMsg", session.getAttribute("userRegisterMsg"));
            session.setAttribute("userRegisterMsg", null);
        }
        return "register";
    }

    @PostMapping("/userRegister")
    public String userRegister(UserVo userVo, HttpSession session) {
        if (userVo.getUsername() == null || userVo.getUsername().trim().length() == 0) session.setAttribute("userRegisterMsg", "用户名不能为空!");
        else if (userVo.getPassword() == null || userVo.getPassword().trim().length() == 0) session.setAttribute("userRegisterMsg", "密码不能为空!");
        else if (userService.selectOneUser(userVo.getUsername()) != null) session.setAttribute("userRegisterMsg", "用户名重复!请重新输入!");
        else {
            if (userService.addOneUser(userVo)) {
                session.setAttribute("userRegisterMsg", "注册成功!");
                return "redirect:/";
            } else session.setAttribute("userRegisterMsg", "注册失败!请联系开发人员!");
        }
        return "redirect:/user/register";
    }

    @RequestMapping("/userMessage")
    public String userMessage(Model model, HttpServletRequest request, HttpSession session) {
        if (session.getAttribute("userVo") != null) request.getSession().setAttribute("userVo", session.getAttribute("userVo"));
        if (session.getAttribute("userUpdateMsg") != null) {
            model.addAttribute("userUpdateMsg", session.getAttribute("userUpdateMsg"));
            session.setAttribute("userUpdateMsg", null);
        }
        if (request.getSession().getAttribute("userVo") == null) {
            model.addAttribute("userLoginMsg", "用户未登录!请登录后重试!");
            return "redirect:/";
        } else return "userMessage";
    }

    @PostMapping("/updatePassword")
    public String updatePassword(@RequestParam("password") String password, HttpServletRequest request, HttpSession session) {
        UserVo userVo = (UserVo) request.getSession().getAttribute("userVo");
        if (userVo == null) {
            session.setAttribute("userLoginMsg", "用户未登录!请登录后重试!");
            return "redirect:/";
        } else if (password == null || password.trim().length() == 0) session.setAttribute("userUpdateMsg", "请输入新密码!");
        else if (userService.decrypt(userVo.getPassword()).equals(password.trim())) session.setAttribute("userUpdateMsg", "新旧密码相同!请重新输入!");
        else if (userService.updatePassword(userVo.getUsername(), password.trim())) session.setAttribute("userUpdateMsg", "密码修改成功!");
        else session.setAttribute("userUpdateMsg", "密码修改异常!请联系开发人员!");
        return "redirect:/user/userMessage";
    }

    @PostMapping("/manage")
    public String manage(@RequestParam("id") String id, HttpSession session, HttpServletRequest request, Model model) {
        if (request.getSession().getAttribute("userVo") == null) {
            model.addAttribute("userLoginMsg", "用户未登录!请登录后重试!");
            return "redirect:/";
        } else {
            if (session.getAttribute("userUpdateMsg") != null) {
                model.addAttribute("userUpdateMsg", session.getAttribute("userUpdateMsg"));
                session.setAttribute("userUpdateMsg", null);
            } else {
                request.getSession().setAttribute("userVo", session.getAttribute("userVo"));
                request.getSession().setAttribute("userList", userService.selectAllUser());
                request.getSession().setAttribute("hotListList", hotListService.selectAllHotList());
                request.getSession().setAttribute("hotListDataList", hotListDataService.selectAllHotListData());
                request.getSession().setAttribute("subscribeList", subscribeService.selectAllSubscribe());
                request.getSession().setAttribute("feedbackList", feedbackService.selectAllFeedback());
                model.addAttribute("id", id);
            }
            return "manage";
        }
    }

    @PostMapping("/addUser")
    @ResponseBody
    public String addUser(String username, String password, String accountType, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (username == null || username.trim().length() == 0) return "用户名不能为空!";
        else if (password == null || password.trim().length() == 0) return "密码不能为空!";
        else if (userService.selectOneUser(username.trim()) != null) return "用户名重复!请重新输入!";
        else if (userService.addOneUser(new UserVo(null, username.trim(), password.trim(), accountType.trim(), new ArrayList<>(), new ArrayList<>())))
            return "添加成功!";
        else return "添加失败!请联系开发人员!";
    }

    @PostMapping("/updateUser")
    @ResponseBody
    public String updateUser(String id, String username, String password, String accountType, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (username == null || username.trim().length() == 0) return "平台名不能为空!";
        else if (password == null || password.trim().length() == 0) return "平台地址不能为空!";
        else if (accountType == null || accountType.trim().length() == 0) return "平台地址不能为空!";
        else if (userService.updateUser(new User(id.trim(), username.trim(), password.trim(), accountType.trim()))) return "修改成功!";
        else return "修改失败!请联系开发人员!";
    }

    @PostMapping("/deleteUser")
    @ResponseBody
    public String deleteUser(String id, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (userService.deleteUser(id)) return "删除成功!";
        else return "删除失败!请联系开发人员!";
    }
}
