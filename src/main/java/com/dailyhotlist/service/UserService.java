package com.dailyhotlist.service;

import com.dailyhotlist.mapper.FeedbackMapper;
import com.dailyhotlist.mapper.SubscribeMapper;
import com.dailyhotlist.mapper.UserMapper;
import com.dailyhotlist.model.Feedback;
import com.dailyhotlist.model.HotListData;
import com.dailyhotlist.model.Subscribe;
import com.dailyhotlist.model.User;
import com.dailyhotlist.vo.FeedbackVo;
import com.dailyhotlist.vo.HotListDataVo;
import com.dailyhotlist.vo.SubscribeVo;
import com.dailyhotlist.vo.UserVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class UserService {
    @Resource
    public UserMapper userMapper;

    @Resource
    public SubscribeMapper subscribeMapper;

    @Resource
    public FeedbackMapper feedbackMapper;

    public UserVo selectOneUser(String username) {
        User user = userMapper.selectOneUser(encode(username));
        if (user != null) {
            List<SubscribeVo> subscribeVoList = new ArrayList<>();
            for (Subscribe subscribe : subscribeMapper.selectSubscribeByUserId(user.getId())) {
                List<HotListDataVo> hotListDataVoList = new ArrayList<>();
                for (HotListData hotListData : subscribe.getHotListDataList())
                    hotListDataVoList.add(new HotListDataVo(hotListData.getId(), decrypt(hotListData.getHotListDataId()), decrypt(hotListData.getHotListDataUrl()), decrypt(hotListData.getHotListDataTitle()), decrypt(hotListData.getHotListDataSubTitle()), decrypt(hotListData.getHotListDataHeat()), decrypt(hotListData.getHotListDataImageUrl()), decrypt(hotListData.getHotListName())));
                subscribeVoList.add(new SubscribeVo(subscribe.getId(), decrypt(subscribe.getHotListName()), decrypt(subscribe.getUserId()), hotListDataVoList));
            }
            if (subscribeVoList.size() > 0) showAllSubscribeData(subscribeVoList);
            List<FeedbackVo> feedbackVoList = new ArrayList<>();
            for (Feedback feedback : feedbackMapper.selectFeedbackByUserId(user.getId()))
                feedbackVoList.add(new FeedbackVo(feedback.getId(), decrypt(feedback.getFeedbackTitle()), decrypt(feedback.getFeedbackContent()), decrypt(feedback.getUserId())));
            return new UserVo(decrypt(user.getId()), decrypt(user.getUsername()), user.getPassword(), decrypt(user.getAccountType()), subscribeVoList, feedbackVoList);
        } else return null;
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
        subscribeVoList.add(0, new SubscribeVo(0, "全部", "http://localhost:8080/", new ArrayList<>()));
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

    public boolean addOneUser(UserVo userVo) {
        StringBuilder id = new StringBuilder();
        do {
            int idLen = userVo.getIdLen();
            char[] nums = new char[3];
            while (idLen-- > 0) {
                nums[0] = (char) ((int) (Math.random() * 10) + 48);
                nums[1] = (char) ((int) (Math.random() * 26) + 65);
                nums[2] = (char) ((int) (Math.random() * 26) + 97);
                id.append(nums[(int) (Math.random() * 3)]);
            }
            id = new StringBuilder(encode(id.toString()));
        } while (userMapper.selectId(id.toString()) != null);
        return userMapper.addOneUser(new User(id.toString(), encode(userVo.getUsername()), encode(userVo.getPassword()), encode(userVo.getAccountType()))) == 1;
    }

    public boolean updatePassword(String username, String password) {
        return userMapper.updatePassword(encode(username), encode(password)) == 1;
    }

    public List<UserVo> selectAllUser() {
        List<UserVo> userVoList = new ArrayList<>();
        for (User user : userMapper.selectAllUser())
            userVoList.add(new UserVo(decrypt(user.getId()), decrypt(user.getUsername()), user.getPassword(), decrypt(user.getAccountType()), new ArrayList<>(), new ArrayList<>()));
        return userVoList;
    }

    public boolean updateUser(User user) {
        user.setId(encode(user.getId()));
        user.setUsername(encode(user.getUsername()));
        user.setPassword(encode(user.getPassword()));
        user.setAccountType(encode(user.getAccountType()));
        return userMapper.updateUser(user) == 1;
    }

    public boolean deleteUser(String id) {
        userMapper.closeForeignKey();
        int count = userMapper.deleteUser(encode(id));
        userMapper.resetAutoIncrement();
        userMapper.openForeignKey();
        return count == 1;
    }

    public String encode(String origin) {
        return Base64.getEncoder().encodeToString(origin.getBytes());
    }

    public String decrypt(String encode) {
        return new String(Base64.getDecoder().decode(encode));
    }
}
