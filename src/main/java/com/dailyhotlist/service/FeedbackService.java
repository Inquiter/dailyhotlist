package com.dailyhotlist.service;

import com.dailyhotlist.mapper.FeedbackMapper;
import com.dailyhotlist.model.Feedback;
import com.dailyhotlist.vo.FeedbackVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class FeedbackService {
    @Resource
    private FeedbackMapper feedbackMapper;

    public boolean addFeedback(String feedbackTitle, String feedbackContent, String userId) {
        return feedbackMapper.insertFeedback(encode(feedbackTitle), encode(feedbackContent), encode(userId)) == 1;
    }

    public List<FeedbackVo> selectFeedbackByUserId(String userId) {
        List<FeedbackVo> feedbackVoList = new ArrayList<>();
        for (Feedback feedback : feedbackMapper.selectFeedbackByUserId(encode(userId)))
            feedbackVoList.add(new FeedbackVo(feedback.getId(), decrypt(feedback.getFeedbackTitle()), decrypt(feedback.getFeedbackContent()), decrypt(feedback.getUserId())));
        return feedbackVoList;
    }

    public List<FeedbackVo> selectAllFeedback() {
        List<FeedbackVo> feedbackVoList = new ArrayList<>();
        for (Feedback feedback : feedbackMapper.selectAllFeedback())
            feedbackVoList.add(new FeedbackVo(feedback.getId(), decrypt(feedback.getFeedbackTitle()), decrypt(feedback.getFeedbackContent()), decrypt(feedback.getUserId())));
        return feedbackVoList;
    }

    public boolean updateOneFeedback(Feedback feedback) {
        feedback.setFeedbackTitle(encode(feedback.getFeedbackTitle()));
        feedback.setFeedbackContent(encode(feedback.getFeedbackContent()));
        feedback.setUserId(encode(feedback.getUserId()));
        return feedbackMapper.updateOneFeedback(feedback) == 1;
    }

    public boolean deleteOneFeedback(int id) {
        int count = feedbackMapper.deleteOneFeedback(id);
        feedbackMapper.resetAutoIncrement();
        return count == 1;
    }

    public String encode(String origin) {
        return Base64.getEncoder().encodeToString(origin.getBytes());
    }

    public String decrypt(String encode) {
        return new String(Base64.getDecoder().decode(encode));
    }
}
