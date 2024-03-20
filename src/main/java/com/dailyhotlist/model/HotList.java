package com.dailyhotlist.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HotList {
    private int id;
    private String hotListName;
    private String hotListUrl;
    private List<HotListData> hotListDataList;
}
