package com.dailyhotlist.mapper;

import com.dailyhotlist.model.Feedback;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FeedbackMapper {
    int insertFeedback(String feedbackTitle, String feedbackContent, String userId);

    List<Feedback> selectFeedbackByUserId(String userId);

    List<Feedback> selectAllFeedback();

    int updateOneFeedback(Feedback feedback);

    int deleteOneFeedback(int id);

    int closeForeignKey();

    int resetAutoIncrement();

    int openForeignKey();

}
