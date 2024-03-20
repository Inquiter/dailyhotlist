package com.dailyhotlist.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FeedbackVo {
    private int id;
    private String feedbackTitle;
    private String feedbackContent;
    private String userId;
}
