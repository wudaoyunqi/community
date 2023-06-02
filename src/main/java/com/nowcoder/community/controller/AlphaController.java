package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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

    private static final ThreadLocal<Integer> currentUser = ThreadLocal.withInitial(() -> null);

    private static final Logger logger = LoggerFactory.getLogger(AlphaController.class);

    @Autowired
    AlphaService alphaService;


    @GetMapping("/wrong")
    @ResponseBody
    public Map<String, String> wrong(@RequestParam("userId") int userId) {
        String before = Thread.currentThread().getName() + ":" + currentUser.get();
        currentUser.set(userId);
        String after = Thread.currentThread().getName() + ":" + currentUser.get();
        Map<String, String> result = new HashMap<>();
        result.put("before", before);
        result.put("after", after);
        return result;
    }

    //线程个数
    private static int THREAD_COUNT = 10;
    //总元素数量
    private static int ITEM_COUNT = 1000;

    //帮助方法，用来获得一个指定元素数量模拟数据的ConcurrentHashMap
    private ConcurrentHashMap<String, Long> getData(int count) {
        return LongStream.rangeClosed(1, count)
                .boxed()
                .collect(Collectors.toConcurrentMap(i -> UUID.randomUUID().toString(), Function.identity(),
                        (o1, o2) -> o1, ConcurrentHashMap::new));
    }

    @GetMapping("wrong2")
    @ResponseBody
    public String wrong2() throws InterruptedException {
        //初始900个元素
        ConcurrentHashMap<String, Long> concurrentHashMap = getData(ITEM_COUNT - 100);
        logger.info("init size:{}", concurrentHashMap.size());

        ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        //使用线程池并发处理逻辑
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, 10).parallel().forEach(i -> {
            // keypoint 对下面匿名代码块加锁就保证线程安全了
            synchronized (concurrentHashMap) {
                //查询还需要补充多少个元素
                int gap = ITEM_COUNT - concurrentHashMap.size();
                logger.info("gap size:{}", gap);
                //补充元素
                concurrentHashMap.putAll(getData(gap));
            }
        }));

        //等待所有任务完成
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);

        //最后元素个数会是1000吗？
        logger.info("finish size:{}", concurrentHashMap.size());
        return "OK";
    }

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


    // Cookie示例，在response header里查看是否有cookie
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        // 创建Cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        // 设置Cookie生效的范围
        cookie.setPath("/community/alpha");
        // 设置cookie的生存时间
        cookie.setMaxAge(60 * 10);   // 单位是s
        // 发送cookie
        response.addCookie(cookie);
        return "set cookie";

    }

    // 获取cookie，在request header里查看
    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) {
        System.out.println(code);
        return "get cookie";
    }

    // session示例，在response header里查看cookie里是否有sessionid
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) {
        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set session";
    }

    // 获取session，在request header里查看cookie是否携带sessionid
    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

    // ajax示例（异步请求不返回html页面，而是返回json字符串）
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0, "操作成功！");
    }


}


