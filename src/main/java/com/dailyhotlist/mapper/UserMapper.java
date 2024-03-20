package com.dailyhotlist.mapper;

import com.dailyhotlist.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectOneUser(String username);

    User selectId(String id);

    int addOneUser(User user);

    int updatePassword(String username, String password);

    List<User> selectAllUser();

    int updateUser(User user);

    int deleteUser(String id);

    int closeForeignKey();

    int resetAutoIncrement();

    int openForeignKey();
}
