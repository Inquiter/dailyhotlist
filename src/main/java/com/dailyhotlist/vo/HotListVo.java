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
public class HotListVo {
    private int id;
    private String hotListName;
    private String hotListUrl;
    private List<HotListDataVo> hotListDataVoList = new ArrayList<>();
}
