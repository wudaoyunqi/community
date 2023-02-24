package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {

    // 会自动扫描启动类所在包以及子包所有的bean装入容器中，能被扫描的类需要加上@Controller、@Service、@Component、@Repository四个注解
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
