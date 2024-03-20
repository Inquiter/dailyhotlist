package com.dailyhotlist.controller;

import com.dailyhotlist.model.HotListData;
import com.dailyhotlist.service.HotListDataService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/hotListData")
public class HotListDataController {
    @Resource
    private HotListDataService hotListDataService;

    @PostMapping("/addHotListData")
    @ResponseBody
    public String addHotListData(String hotListDataId, String hotListDataUrl, String hotListDataTitle, String hotListDataSubTitle, String hotListDataHeat, String hotListDataImageUrl, String hotListName, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (hotListDataId == null || hotListDataId.trim().length() == 0) return "热点ID不能为空!";
        else if (hotListDataUrl == null || hotListDataUrl.trim().length() == 0) return "热点地址不能为空!";
        else if (hotListDataTitle == null || hotListDataTitle.trim().length() == 0) return "热点标题不能为空!";
        else if (hotListDataService.selectOneHotListData(hotListDataId.trim(), hotListName.trim()) != null) return "热点ID重复!请重新输入!";
        else if (hotListDataService.addHotListData(new HotListData(0, hotListDataId.trim(), hotListDataUrl.trim(), hotListDataTitle.trim(), hotListDataSubTitle.trim(), hotListDataHeat.trim(), hotListDataImageUrl.trim(), hotListName.trim())))
            return "添加成功!";
        else return "添加失败!请联系开发人员!";
    }

    @PostMapping("/updateHotListData")
    @ResponseBody
    public String updateHotListData(int id, String hotListDataId, String hotListDataUrl, String hotListDataTitle, String hotListDataSubTitle, String hotListDataHeat, String hotListDataImageUrl, String hotListName, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (hotListDataId == null || hotListDataId.trim().length() == 0) return "热点ID不能为空!";
        else if (hotListDataUrl == null || hotListDataUrl.trim().length() == 0) return "热点地址不能为空!";
        else if (hotListDataTitle == null || hotListDataTitle.trim().length() == 0) return "热点标题不能为空!";
        else if (hotListDataService.updateHotListData(new HotListData(id, hotListDataId.trim(), hotListDataUrl.trim(), hotListDataTitle.trim(), hotListDataSubTitle.trim(), hotListDataHeat.trim(), hotListDataImageUrl.trim(), hotListName.trim())))
            return "修改成功!";
        else return "修改失败!请联系开发人员!";
    }

    @PostMapping("/deleteHotListData")
    @ResponseBody
    public String deleteHotListData(int id, HttpServletRequest request) {
        if (request.getSession().getAttribute("userVo") == null) return "用户未登录!请登录后重试!";
        else if (hotListDataService.deleteOneHotListData(id)) return "删除成功!";
        else return "删除失败!请联系开发人员!";
    }
}
