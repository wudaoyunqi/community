package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @Projectname: community
 * @Filename: SensitiveTests
 * @Author: yunqi
 * @Date: 2023/2/26 10:29
 * @Description: TODO
 */

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String text = "/f&a*a%b^c+d-e";
        System.out.println(sensitiveFilter.filter(text));
    }
}
