package com.example.shoppingapi.controller;

import com.example.shoppingapi.model.User;
import com.example.shoppingapi.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UsersController {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // 获取用列表
    @RequestMapping("/user/list")
    public R userList(@RequestBody Map<String, String> map) throws IllegalAccessException {
        String phone = map.get("phone").toString();
        String page = map.get("page").toString();
        Integer current_page = Integer.parseInt(page);
        Integer page_size = 10;
        Integer start = (current_page-1)*page_size;
        String sql = "select id, phone, name, money, address, create_time from users";
        String sql_count = "select count(*) from users";
        if (phone != "") {
            sql += " where phone like '%"+phone+"%'";
            sql_count += " where phone like '%"+phone+"%'";
        }

        int count = jdbcTemplate.queryForObject(sql_count, Integer.class);

        sql += " limit "+ start + "," + page_size;

//        String sql = "select id, phone, name, money, address, create_time from users limit "+ start + "," + page_size;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        PageList pageList = new PageList();
        pageList.setList(list);
        pageList.setPage(current_page);
//        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        pageList.setTotalRows(count);
        int pages= 0;
        if(count % page_size == 0){ pages = count / page_size;}
        else { pages = count / page_size +1 ;}
        pageList.setPages(pages);

        return R.ok("获取用户列表成功").data(FormatUtil.objectToMap(pageList));
    }

    // 添加用户
    @RequestMapping("/user/add")
    public R addUser(@RequestBody Map<String, String> map) {
        String phone = map.get("phone").toString();
        String name = map.get("name").toString();
        String password = map.get("password").toString();
        String address = map.get("address").toString();

        // TODO 接收参数校验, 比如phone或password空等，密码限制位数, phone格式等

        password = MD5Util.MD5Encode(password, "UTF-8");
        String time = DateUtil.getNowTime();
        // 判断是否已存在
        String sql = "select * from users where `phone` = '"+phone+"'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() > 0) {
            return R.error(-1001, "手机号已存在");
        }
        sql = "insert into users(`phone`,`password`, `name`, `address`, `create_time`)values('"+phone+"','"+password+"','"+name+"','"+address+"','"+time+"')";
        jdbcTemplate.update(sql);
        return R.ok("添加成功");
    }

    // 编辑用户
    @RequestMapping("/user/edit")
    public R editUser(@RequestBody Map<String, String> map) {
        String phone = map.get("phone").toString();
        String name = map.get("name").toString();
        String password = map.get("password").toString();
        String address = map.get("address").toString();
        int money = Integer.parseInt(map.get("money").toString());

        // TODO 接收参数校验, 比如phone或password空等，密码限制位数, phone格式等

        String sql = "select * from users where `phone` = '"+phone+"'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() == 0) {
            return R.error(-1001, "用户不存在");
        }

        password = MD5Util.MD5Encode(password, "UTF-8");
        sql = "update users set password = ?, name = ?, address = ?, money = ? where phone = '"+phone+"'";
        Object[] objects = new Object[4];
        objects[0] = password;
        objects[1] = name;
        objects[2] = address;
        objects[3] = money;
        System.out.println(sql);
        try{
            jdbcTemplate.update(sql, objects);
        }catch (InvalidResultSetAccessException e) {
            // 异常处理
            throw new RuntimeException(e);
        }

        return R.ok("编辑用户成功");
    }

    // 修改密码
    @RequestMapping("/user/editpwd")
    public R editPassword(@RequestBody Map<String, String> map) {
        String phone = map.get("phone").toString();
        String password = map.get("password").toString();

        // TODO 接收参数校验, 比如phone或password空等，密码限制位数, phone格式等

        String sql = "select * from users where `phone` = '"+phone+"'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() == 0) {
            return R.error(-1001, "用户不存在");
        }

        password = MD5Util.MD5Encode(password, "UTF-8");
        sql = "update users set password = ? where phone = '"+phone+"'";
        Object[] objects = new Object[1];
        objects[0] = password;
        try{
            jdbcTemplate.update(sql, objects);
        }catch (InvalidResultSetAccessException e) {
            // 异常处理
            throw new RuntimeException(e);
        }

        return R.ok("修改用户密码成功");
    }

    // 删除用户
    @RequestMapping("/user/del")
    public R delUser(@RequestBody Map<String, String> map) {
        String phone = map.get("phone").toString();
        // TODO 接收参数校验, 比如phone是否存在，phone格式等

        String sql = "select * from users where `phone` = '"+phone+"'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() == 0) {
            return R.error(-1001, "用户不存在");
        }

        sql = "delete from users where phone = '"+phone+"'";
        try{
            jdbcTemplate.update(sql);
        }catch (InvalidResultSetAccessException e) {
            // 异常处理
            throw new RuntimeException(e);
        }
        return R.ok("删除成功");

    }

    // 获取用户详情
    @RequestMapping("/user/detail")
    public R userDetail(@RequestBody Map<String, String> map) throws IllegalAccessException {
        String phone = map.get("phone").toString();
        User user;
        try {
            user =  jdbcTemplate.queryForObject("select * from users where phone = ?", new BeanPropertyRowMapper<>(User.class), phone);
        } catch (EmptyResultDataAccessException e) {
            return R.ok("获取成功");
        }

        return R.ok("获取成功").data(FormatUtil.objectToMap(user));
    }

    // 登录
    @RequestMapping("/login")
    public R login(@RequestBody Map<String, String> map) throws IllegalAccessException {
        String phone = map.get("phone").toString();
        String password = map.get("password").toString();

        password = MD5Util.MD5Encode(password, "UTF-8");

        User user;
        try {
            user =  jdbcTemplate.queryForObject("select id, name, money, address, phone from users where phone = ? and password = ?", new BeanPropertyRowMapper<>(User.class), phone, password);
        } catch (EmptyResultDataAccessException e) {
            return R.error(-1,"登录失败");
        }

        if (user.getId() > 0) {
            return R.ok("登录成功").data(FormatUtil.objectToMap(user));
        } else {
            return R.error(-1,"登录失败");
        }

    }
}
