package com.nowcoder.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @Projectname: community
 * @Filename: AlphaDaoMyBatisImpl
 * @Author: yunqi
 * @Date: 2023/2/23 13:00
 * @Description: TODO
 */

// @Primary具有较高优先级
@Repository
@Primary
public class AlphaDaoMyBatisImpl implements AlphaDAO {
    @Override
    public String select() {
        return "MyBatis";
    }
}
