package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Projectname: community
 * @Filename: ExceptionAdvice
 * @Author: yunqi
 * @Date: 2023/2/27 20:50
 * @Description: TODO
 */

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        // 获取请求的方式
        String xRequestWith = request.getHeader("x-requested-with");
        if (xRequestWith.equals("XMLHttpRequest")) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器发生异常"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");  // 重定向回错误页面
        }
    }

}
