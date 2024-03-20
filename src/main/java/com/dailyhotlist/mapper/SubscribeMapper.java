package com.dailyhotlist.mapper;

import com.dailyhotlist.model.Subscribe;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SubscribeMapper {
    List<Subscribe> selectSubscribeByUserId(String userId);

    int insertSubscribe(String hotListName, String userId);

    int closeForeignKey();

    int openForeignKey();

    int deleteSubscribeByHotListNameAndUserId(String hotListName, String userId);

    int resetAutoIncrement();

    List<Subscribe> selectAllSubscribe();

    Subscribe selectOneSubscribe(String hotListName, String userId);

    int updateOneSubscribe(Subscribe subscribe);

    int deleteOneSubscribe(int id);
}
