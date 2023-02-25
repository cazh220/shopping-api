package com.example.shoppingapi.controller;

import com.example.shoppingapi.dao.AdminDao;
import com.example.shoppingapi.service.PageService;
import com.example.shoppingapi.util.FormatUtil;
import com.example.shoppingapi.util.PageList;
import com.example.shoppingapi.util.R;
import com.example.shoppingapi.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.example.shoppingapi.model.Admin;

@RestController
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    AdminDao adminDao;

    @RequestMapping("/test")
    public String index(@RequestBody Map<String, String> map) {
        String account = map.get("account").toString();
        System.out.println(account);
        String password = map.get("password").toString();
        System.out.println(password);
        String resultString = "{\"code\":200,\"msg\":\"ok\",\"data\":[]}";
        return resultString;
    }

    @RequestMapping("/aa")
    public List<Map<String, Object>> aa() {
        String sql = "select * from admin";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return list;
    }

    @RequestMapping("/test1")
    public void queryUser(HttpServletRequest request, HttpServletResponse response) {
//        try {
//            ResponseUtil.setJsonResponse(response);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @ResponseBody
    @RequestMapping("/map")
    public R testList() {
//        ResponseUtil.setJsonResponse(HttpServletResponse httpServletResponse, "{\"code\":200,\"msg\":\"ok\",\"data\":[]}");
        String sql = "select * from admin";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
//        String json = new ObjectMapper().writeValueAsString(list);
//        ResponseUtil.jsonResponse(list);
        System.out.println(list);
          return R.ok("ok").data("", list);
//        return R.ok().data("items",userService.queryAllUserByMp(pageNum,pageSize));
    }

    @ResponseBody
    @RequestMapping("/admin_list")
    public R adminList(@RequestBody Map<String, String> map) throws IllegalAccessException {
        String page = map.get("page").toString();
        System.out.println(page);
        PageList pageList = new PageList();

        Integer current_page = Integer.parseInt(page);
        Integer page_size = 10;
        Integer start = (current_page-1)*page_size;
        String sql = "select * from admin limit "+ start + "," + page_size;
        System.out.println(sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        pageList.setList(list);
        pageList.setPage(current_page);

        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin", Integer.class);
        System.out.println(count);
        pageList.setTotalRows(count);
        int pages= 0;
        if(count % page_size == 0){ pages = count / page_size;}
        else { pages = count / page_size +1 ;}
        pageList.setPages(pages);
        System.out.println(pageList);
        return R.ok("ok").data(FormatUtil.objectToMap(pageList));
//        return pageList;
    }

    @ResponseBody
    @RequestMapping("/page")
    public String page() {
        PageService pageService = new PageService();
        String sql = "select * from admin";
        String list = pageService.simplePaging(sql);
        return list;
    }

}
