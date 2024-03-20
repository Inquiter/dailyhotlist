package com.dailyhotlist.mapper;

import com.dailyhotlist.model.HotList;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HotListMapper {
    List<HotList> selectHotList();

    int insertHotList(HotList hotList);

    int deleteHotList();

    int resetAutoIncrement();

    List<HotList> selectAllHotList();

    HotList selectOneHotList(String hotListName);

    int updateHotList(HotList hotList);

    int closeForeignKey();

    int deleteOneHotList(int id);

    int openForeignKey();
}
