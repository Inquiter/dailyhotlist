package com.dailyhotlist.controller;

import com.dailyhotlist.model.HotList;
import com.dailyhotlist.service.HotListService;
import com.dailyhotlist.service.UserService;
import com.dailyhotlist.vo.UserVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@Controller
@RequestMapping("/hotList")
public class HotListController {
    @Resource
    private HotListService hotListService;

    @Resource
    private UserService userService;

    @RequestMapping("/selectHotList")
    public String selectHotList(HttpServletRequest request) {
        request.getSession().setAttribute("hotListVoList", hotListService.selectHotListDataFromDatabase());
        UserVo userVo = (UserVo) request.getSession().getAttribute("userVo");
        if (userVo != null) request.getSession().setAttribute("userVo", userService.selectOneUser(userVo.getUsername()));
        return "index";
    }

    @PostMapping("/addHotList")
    @ResponseBody
    public String addHotList(String hotListName, String hotListUrl, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (hotListName == null || hotListName.trim().length() == 0) return "平台名不能为空!";
        else if (hotListUrl == null || hotListUrl.trim().length() == 0) return "平台地址不能为空!";
        else if (hotListService.selectOneHotList(hotListName.trim()) != null) return "平台名重复!请重新输入!";
        else if (hotListService.addHotList(new HotList(0, hotListName.trim(), hotListUrl.trim(), new ArrayList<>()))) return "添加成功!";
        else return "添加失败!请联系开发人员!";
    }

    @PostMapping("/updateHotList")
    @ResponseBody
    public String updateHotList(int id, String hotListName, String hotListUrl, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (hotListName == null || hotListName.trim().length() == 0) return "平台名不能为空!";
        else if (hotListUrl == null || hotListUrl.trim().length() == 0) return "平台地址不能为空!";
        else if (hotListService.selectOneHotList(hotListName.trim()) != null) return "平台名重复!请重新输入!";
        else if (hotListService.updateHotList(new HotList(id, hotListName.trim(), hotListUrl.trim(), new ArrayList<>()))) return "修改成功!";
        else return "修改失败!请联系开发人员!";
    }

    @PostMapping("/deleteHotList")
    @ResponseBody
    public String deleteHotList(int id, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (hotListService.deleteHotList(id)) return "删除成功!";
        else return "删除失败!请联系开发人员!";
    }
}
