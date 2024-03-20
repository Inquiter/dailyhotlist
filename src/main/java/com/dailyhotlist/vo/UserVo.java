package com.dailyhotlist.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserVo {
    private String id;
    private String username;
    private String password;
    private String accountType;
    private List<SubscribeVo> subscribeVoList = new ArrayList<>();
    private List<FeedbackVo> feedbackVoList = new ArrayList<>();
    //默认id长度
    private final int idLen = 15;
}
