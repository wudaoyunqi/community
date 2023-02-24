package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Projectname: community
 * @Filename: UserService
 * @Author: yunqi
 * @Date: 2023/2/23 17:36
 * @Description: TODO
 */
@Service
public class UserService {

    @Autowired
    UserMapper userMapper;

    public User getUserById(int id) {
        return userMapper.selectById(id);
    }
}
