package com.dailyhotlist.service;

import com.dailyhotlist.mapper.SubscribeMapper;
import com.dailyhotlist.model.HotListData;
import com.dailyhotlist.model.Subscribe;
import com.dailyhotlist.vo.HotListDataVo;
import com.dailyhotlist.vo.SubscribeVo;
import com.dailyhotlist.vo.UserVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class SubscribeService {
    @Resource
    private SubscribeMapper subscribeMapper;

    public List<SubscribeVo> updateSubscribes(List<String> hotListNameList, UserVo userVo) {
        List<String> oldHotListNameList = new ArrayList<>();
        for (SubscribeVo subscribeVo : userVo.getSubscribeVoList()) oldHotListNameList.add(subscribeVo.getHotListName());
        if (oldHotListNameList.size() == hotListNameList.size() && oldHotListNameList.containsAll(hotListNameList)) return new ArrayList<>();
        else {
            for (String hotListName : hotListNameList)
                if (!oldHotListNameList.contains(hotListName) && subscribeMapper.insertSubscribe(encode(hotListName), encode(userVo.getId())) < 1)
                    return null;
            oldHotListNameList.removeAll(hotListNameList);
            for (String hotListName : oldHotListNameList)
                subscribeMapper.deleteSubscribeByHotListNameAndUserId(encode(hotListName), encode(userVo.getId()));
            if (subscribeMapper.resetAutoIncrement() < 0) return null;
            else {
                List<SubscribeVo> subscribeVoList = new ArrayList<>();
                for (Subscribe subscribe : subscribeMapper.selectSubscribeByUserId(encode(userVo.getId()))) {
                    List<HotListDataVo> hotListDataVoList = new ArrayList<>();
                    for (HotListData hotListData : subscribe.getHotListDataList())
                        hotListDataVoList.add(new HotListDataVo(hotListData.getId(), decrypt(hotListData.getHotListDataId()), decrypt(hotListData.getHotListDataUrl()), decrypt(hotListData.getHotListDataTitle()), decrypt(hotListData.getHotListDataSubTitle()), decrypt(hotListData.getHotListDataHeat()), decrypt(hotListData.getHotListDataImageUrl()), decrypt(hotListData.getHotListName())));
                    subscribeVoList.add(new SubscribeVo(subscribe.getId(), decrypt(subscribe.getHotListName()), decrypt(subscribe.getUserId()), hotListDataVoList));
                }
                if (subscribeVoList.size() > 0) showAllSubscribeData(subscribeVoList);
                return subscribeVoList;
            }
        }
    }

    //展示全部订阅数据。
    private void showAllSubscribeData(List<SubscribeVo> subscribeVoList) {
        List<String> hotListNameList = new ArrayList<>();
        hotListNameList.add("知乎");
        hotListNameList.add("微博");
        hotListNameList.add("虎扑");
        hotListNameList.add("贴吧");
        hotListNameList.add("百度");
        hotListNameList.add("豆瓣");
        hotListNameList.add("今日头条");
        hotListNameList.add("网易新闻");
        hotListNameList.add("澎湃新闻");
        hotListNameList.add("观风闻");
        subscribeVoList.add(0, new SubscribeVo(0, "全站", "http://localhost:8080/", new ArrayList<>()));
        List<HotListDataVo> hotListDataVoList = subscribeVoList.get(0).getHotListDataVoList();
        for (SubscribeVo subscribeVo : subscribeVoList)
            for (int i = 0; i < 100 / (subscribeVoList.size() - 1) && i < subscribeVo.getHotListDataVoList().size(); i++)
                if (hotListNameList.contains(subscribeVo.getHotListName()))
                    hotListDataVoList.add(new HotListDataVo(subscribeVo.getHotListDataVoList().get(i).getId(), subscribeVo.getHotListDataVoList().get(i).getHotListDataId(), subscribeVo.getHotListDataVoList().get(i).getHotListDataUrl(), subscribeVo.getHotListDataVoList().get(i).getHotListDataTitle(), subscribeVo.getHotListDataVoList().get(i).getHotListDataSubTitle(), subscribeVo.getHotListDataVoList().get(i).getHotListDataHeat(), subscribeVo.getHotListDataVoList().get(i).getHotListDataImageUrl(), subscribeVo.getHotListDataVoList().get(i).getHotListName()));
        for (int i = hotListDataVoList.size() - 1; i > 0; i--)
            for (int j = 0; j < i; j++) {
                String heat1 = hotListDataVoList.get(j).getHotListDataHeat();
                String heat2 = hotListDataVoList.get(j + 1).getHotListDataHeat();
                heat1 = heat1.substring(0, heat1.indexOf("热"));
                heat2 = heat2.substring(0, heat2.indexOf("热"));
                if (heat1.trim().length() == 0) heat1 = "0";
                if (heat2.trim().length() == 0) heat2 = "0";
                if ((heat1.contains("万") ? (Float.parseFloat(heat1.substring(0, heat1.indexOf("万")).trim()) * 10000) : Float.parseFloat(heat1.trim())) < (heat2.contains("万") ? (Float.parseFloat(heat2.substring(0, heat2.indexOf("万")).trim()) * 10000) : Float.parseFloat(heat2.trim()))) {
                    HotListDataVo hotListDataVo = hotListDataVoList.get(j);
                    hotListDataVoList.set(j, hotListDataVoList.get(j + 1));
                    hotListDataVoList.set(j + 1, hotListDataVo);
                }
            }
        for (int i = 0; i < hotListDataVoList.size(); i++) hotListDataVoList.get(i).setHotListDataId(String.valueOf(i + 1));
    }

    public List<SubscribeVo> selectAllSubscribe() {
        List<SubscribeVo> subscribeVoList = new ArrayList<>();
        for (Subscribe subscribe : subscribeMapper.selectAllSubscribe())
            subscribeVoList.add(new SubscribeVo(subscribe.getId(), decrypt(subscribe.getHotListName()), decrypt(subscribe.getUserId()), new ArrayList<>()));
        return subscribeVoList;
    }

    public SubscribeVo selectOneSubscribe(String hotListName, String userId) {
        Subscribe subscribe = subscribeMapper.selectOneSubscribe(encode(hotListName), encode(userId));
        return subscribe == null ? null : new SubscribeVo(subscribe.getId(), decrypt(subscribe.getHotListName()), decrypt(subscribe.getUserId()), new ArrayList<>());
    }

    public boolean addSubscribe(String hotListName, String userId) {
        return subscribeMapper.insertSubscribe(encode(hotListName), encode(userId)) == 1;
    }

    public boolean updateOneSubscribe(Subscribe subscribe) {
        subscribe.setHotListName(encode(subscribe.getHotListName()));
        subscribe.setUserId(encode(subscribe.getUserId()));
        return subscribeMapper.updateOneSubscribe(subscribe) == 1;
    }

    public boolean deleteOneSubscribe(int id) {
        int count = subscribeMapper.deleteOneSubscribe(id);
        subscribeMapper.resetAutoIncrement();
        return count == 1;
    }

    public String encode(String origin) {
        return Base64.getEncoder().encodeToString(origin.getBytes());
    }

    public String decrypt(String encode) {
        return new String(Base64.getDecoder().decode(encode));
    }
}
