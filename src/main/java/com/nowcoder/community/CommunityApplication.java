package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

    // 指明该方法在构造器之后调用
    @PostConstruct
    private void init() {
        // 解决netty启动冲突问题
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }


    // 会自动扫描启动类所在包以及子包所有的bean装入容器中，能被扫描的类需要加上@Controller、@Service、@Component、@Repository四个注解
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
