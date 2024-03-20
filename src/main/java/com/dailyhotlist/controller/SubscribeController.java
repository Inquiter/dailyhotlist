package com.dailyhotlist.controller;

import com.dailyhotlist.model.Subscribe;
import com.dailyhotlist.service.SubscribeService;
import com.dailyhotlist.vo.SubscribeVo;
import com.dailyhotlist.vo.UserVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/subscribe")
public class SubscribeController {
    @Resource
    private SubscribeService subscribeService;

    @PostMapping("/updateSubscribes")
    @ResponseBody
    public String updateSubscribes(String[] subscribeList, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else {
            UserVo userVo = (UserVo) request.getSession().getAttribute("userVo");
            List<SubscribeVo> subscribeVoList = subscribeService.updateSubscribes(Arrays.asList(subscribeList == null ? new String[0] : subscribeList), userVo);
            if (subscribeVoList != null) {
                if (subscribeVoList.size() > 0) {
                    userVo.setSubscribeVoList(subscribeVoList);
                    request.getSession().setAttribute("userVo", userVo);
                }
                return "订阅修改成功!";
            } else return "订阅修改失败!";
        }
    }

    @PostMapping("/addSubscribe")
    @ResponseBody
    public String addSubscribe(String hotListName, String userId, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (hotListName == null || hotListName.trim().length() == 0) return "平台名不能为空!";
        else if (subscribeService.selectOneSubscribe(hotListName.trim(), userId.trim()) != null) return "订阅已存在!请重新输入!";
        else if (subscribeService.addSubscribe(hotListName.trim(), userId.trim())) return "添加成功!";
        else return "添加失败!请联系开发人员!";
    }

    @PostMapping("/updateSubscribe")
    @ResponseBody
    public String updateSubscribe(int id, String hotListName, String userId, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (hotListName == null || hotListName.trim().length() == 0) return "平台名不能为空!";
        else if (subscribeService.selectOneSubscribe(hotListName.trim(), userId.trim()) != null) return "订阅已存在!请重新输入!";
        else if (subscribeService.updateOneSubscribe(new Subscribe(id, hotListName.trim(), userId.trim(), new ArrayList<>()))) return "修改成功!";
        else return "修改失败!请联系开发人员!";
    }

    @PostMapping("/deleteSubscribe")
    @ResponseBody
    public String deleteSubscribe(int id, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (subscribeService.deleteOneSubscribe(id)) return "删除成功!";
        else return "删除失败!请联系开发人员!";
    }
}
