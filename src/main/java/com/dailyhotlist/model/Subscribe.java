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
public class Subscribe {
    private int id;
    private String hotListName;
    private String userId;
    private List<HotListData> hotListDataList;
}
