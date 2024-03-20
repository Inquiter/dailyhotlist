package com.dailyhotlist.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HotListDataVo {
    private int id;
    private String hotListDataId;
    private String hotListDataUrl;
    private String hotListDataTitle;
    private String hotListDataSubTitle;
    private String hotListDataHeat;
    private String hotListDataImageUrl;
    private String hotListName;
}
