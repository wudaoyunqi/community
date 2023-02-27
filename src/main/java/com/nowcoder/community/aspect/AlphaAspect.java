package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @Projectname: community
 * @Filename: AlphaAspect
 * @Author: yunqi
 * @Date: 2023/2/27 21:58
 * @Description: TODO
 */

//@Component
//@Aspect
public class AlphaAspect {

    // 所有在service包下的类的所有方法的所有参数，以及所有返回值
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before() {
        System.out.println("before");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("after");
    }

    @AfterReturning("pointcut()")
    public void afterreturning() {
        System.out.println("after returning");
    }

    @AfterThrowing("pointcut()")
    public void afterthrowing() {
        System.out.println("after throwing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint JoinPoint) throws Throwable {
        System.out.println("around before");
        Object obj = JoinPoint.proceed();
        System.out.println("around after");
        return obj;
    }

}
