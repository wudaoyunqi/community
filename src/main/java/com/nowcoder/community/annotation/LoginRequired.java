package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Projectname: community
 * @Filename: LoginRequired
 * @Author: yunqi
 * @Date: 2023/2/25 17:38
 * @Description: TODO
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {


}
