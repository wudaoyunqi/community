package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDAO;
import com.nowcoder.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CommunityApplicationTests implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    /**
     * @param applicationContext Bean容器，实现了BeanFactory接口
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext() {
        System.out.println(applicationContext);

        AlphaDAO alphaDAO = applicationContext.getBean(AlphaDAO.class);   // 通过Bean容器主动获取Bean
        System.out.println(alphaDAO.select());

        alphaDAO = applicationContext.getBean("alphaHibernate", AlphaDAO.class);
        System.out.println(alphaDAO.select());
    }

    @Test
    public void testBeanManagement() {
        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);

        alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);
    }

    @Test
    public void testBeanConfig() {
        SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    // 常用依赖注入@Autowired获取Bean
    // Qualifier指明获取的Bean
    @Autowired
    @Qualifier("alphaHibernate")
    private AlphaDAO alphaDAO;

    @Autowired
    private AlphaService alphaService;

    @Autowired
    SimpleDateFormat simpleDateFormat;

    @Test
    public void testDI() {
        System.out.println(alphaDAO);
        System.out.println(alphaService);
        System.out.println(simpleDateFormat);
    }


}




