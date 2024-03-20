package com.dailyhotlist.mapper;

import com.dailyhotlist.model.HotListData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HotListDataMapper {
    List<HotListData> selectAllHotListData();

    List<HotListData> selectHotListDataByHotListName(String hotListName);

    HotListData selectOneHotListData(String hotListDataId, String hotListName);

    int insertHotListData(HotListData hotListData);

    int updateHotListData(HotListData hotListData);

    int deleteOneHotListData(int id);

    int deleteHotListData();

    int closeForeignKey();

    int resetAutoIncrement();

    int openForeignKey();
}
