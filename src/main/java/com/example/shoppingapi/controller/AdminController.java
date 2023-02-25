package com.example.shoppingapi.controller;

import com.example.shoppingapi.service.AdminService;
import com.example.shoppingapi.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 管理员模块
 */
@RestController
public class AdminController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 管理员列表
    @RequestMapping(value = "/admin/list")
    public R adminList(@RequestBody Map<String, String> map) throws IllegalAccessException {
        String page = map.get("page").toString();
        Integer current_page = Integer.parseInt(page);
        Integer page_size = 10;
        Integer start = (current_page-1)*page_size;
        String sql = "select id, account, last_login_time, create_time from admin limit "+ start + "," + page_size;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        PageList pageList = new PageList();
        pageList.setList(list);
        pageList.setPage(current_page);
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin", Integer.class);
        pageList.setTotalRows(count);
        int pages= 0;
        if(count % page_size == 0){ pages = count / page_size;}
        else { pages = count / page_size +1 ;}
        pageList.setPages(pages);

        return R.ok("获取列表成功").data(FormatUtil.objectToMap(pageList));
    }

    // 查看管理详情
    @GetMapping("/admin/detail")
    public List<Map<String, Object>> adminDetail(@RequestParam("account") String account) {
        String sql = "select * from admin where account = '"+account+"'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return list;
    }

    // 添加管理员
    @PostMapping("/admin/add")
    public R addAdmin(@RequestBody Map<String, String> map) {
        String account = map.get("account").toString();
        String password = map.get("password").toString();
        // TODO 接收参数校验, 比如account或password空等，密码限制位数等

        password = MD5Util.MD5Encode(password, "UTF-8");
        String time = DateUtil.getNowTime();
        // 判断是否已存在
        String sql = "select * from admin where `account` = '"+account+"'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() > 0) {
            return R.error(-1001, "账号已存在");
        }
        sql = "insert into admin(`account`,`password`, `create_time`)values('"+account+"','"+password+"','"+time+"')";
        jdbcTemplate.update(sql);
        return R.ok("添加成功");
    }

    // 修改密码
    @PostMapping("/admin/edit")
    public R editPwd(@RequestBody Map<String, String> map) {
        String account = map.get("account").toString();
        String password = map.get("password").toString();
        // TODO 接收参数校验, 比如account或password空等，密码限制位数, 新密码是否与原密码相同（可选）等

        // 首先判断account 是否存在
        String sql = "select * from admin where `account` = '"+account+"'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() == 0) {
            return R.error(-1001, "未知账号");
        }
        password = MD5Util.MD5Encode(password, "UTF-8");
        sql = "update admin set password = ? where account = '"+account+"'";
        Object[] objects = new Object[1];
        objects[0] = password;
        System.out.println(sql);
        try{
            jdbcTemplate.update(sql, objects);
        }catch (InvalidResultSetAccessException e) {
            // 异常处理
            throw new RuntimeException(e);
        }

        return R.ok("编辑成功");
    }

    // 删除管理员
    @RequestMapping("/admin/del")
    public R delAdmin(@RequestBody Map<String, String> map) {
        String account = map.get("account").toString();
        // TODO 数据校验

        // 首先判断account 是否存在
        String sql = "select * from admin where `account` = '"+account+"'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() == 0) {
            return R.error(-1001, "未知账号");
        }
        sql = "delete from admin where account = '"+account+"'";
        try{
            jdbcTemplate.update(sql);
        }catch (InvalidResultSetAccessException e) {
            // 异常处理
            throw new RuntimeException(e);
        }
        return R.ok("删除成功");
    }

    @RequestMapping("/admin/login")
    public R login(@RequestBody Map<String, String> map) {
        String account = map.get("account").toString();
        String password = map.get("password").toString();

        password = MD5Util.MD5Encode(password, "UTF-8");

        int count;
        try {
            count =  jdbcTemplate.queryForObject("select count(*) from admin where account = ? and password = ?", Integer.class, account, password);
        } catch (EmptyResultDataAccessException e) {
            return R.error(-1,"登录失败");
        }

        if (count > 0) {
            String time = DateUtil.getNowTime();
            // 更新最后登录时间
            jdbcTemplate.update("update admin set last_login_time = ? where account = ?", time, account);
            return R.ok("登录成功");
        } else {
            return R.error(-1,"登录失败");
        }

    }
}
