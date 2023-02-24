package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @Projectname: community
 * @Filename: AlphaController
 * @Author: yunqi
 * @Date: 2023/2/23 13:31
 * @Description: TODO
 */

@Controller
@RequestMapping("alpha")
public class AlphaController {

    @Autowired
    AlphaService alphaService;

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code"));

        // 返回响应数据
        response.setContentType("text/html;charset=utf-8");
        // response的输出流向浏览器输出
        try (
                PrintWriter writer = response.getWriter();
        ) {
            writer.write("<h1>牛客</h1>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // GET请求：查询所有学生   /students?current=1&limit=20
    @RequestMapping(path = {"/students"}, method = {RequestMethod.GET})
    @ResponseBody
    public String getStudents(@RequestParam(name = "current", required = false, defaultValue = "1") int current,
                              @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    // /student/123
    @RequestMapping(path = {"/student/{id}"}, method = {RequestMethod.GET})
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }

    // POST请求
    // 如何获取POST请求的参数？ 直接声明参数，参数名与表单中数据的name一致即可
    @RequestMapping(path = {"/student"}, method = {RequestMethod.POST})
    @ResponseBody
    public String saveStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    // 响应html数据：response.ContentType = "text/html"
    @RequestMapping(path = {"/teacher"}, method = {RequestMethod.GET})
    public ModelAndView getTeacher() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "张三");
        modelAndView.addObject("age", 30);
        // 模板文件存放的地址，默认在/resources/templates文件夹下
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    // 一样的写法，更加简洁
    @RequestMapping(path = {"/school"}, method = {RequestMethod.GET})
    public String getSchool(Model model) {   // model对象是DispatcherServlet传来的，其引用DispatcherServlet能得到，所以直接往model写入数据即可
        model.addAttribute("name", "北大");
        model.addAttribute("age", 100);
        return "/demo/view";
    }

    // 响应json数据(异步请求，比如注册时输入用户名页面显示“该名称已被占用”，页面不刷新，但已经向服务器发送了一次请求，查询注册名是否有效，返回的是json数据)
    // Java对象 -> JSON字符串 -> JS对象
    // @ResponseBody注解表明该方法返回的是json/xml数据（response.ContentType = "application/json"），DispatcherServlet会自动把返回的map对象包装成json字符串发送给浏览器
    @RequestMapping(path = {"/emp"}, method = {RequestMethod.GET})
    @ResponseBody
    public Map<String, Object> getEmp() {
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "zhangsan");
        emp.put("age", 23);
        emp.put("salary", 8000.00);
        return emp;
        // 返回的是{"name":"zhangsan","salary":8000.0,"age":23}
    }

    @RequestMapping(path = {"/emps"}, method = {RequestMethod.GET})
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("name", "zhangsan");
        emp.put("age", 23);
        emp.put("salary", 8000.00);
        list.add(emp);

        emp.put("name", "lisi");
        emp.put("age", 28);
        emp.put("salary", 10000.00);
        list.add(emp);

        emp.put("name", "wangwu");
        emp.put("age", 35);
        emp.put("salary", 26000.00);
        list.add(emp);
        return list;
        // 返回[{"name":"wangwu","salary":26000.0,"age":35},{"name":"wangwu","salary":26000.0,"age":35},{"name":"wangwu","salary":26000.0,"age":35}]
    }










}


