package com.example.shoppingapi.controller;

import com.example.shoppingapi.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CategoryController {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // 品类列表
    @RequestMapping("/category/list")
    public R categoryList(@RequestBody Map<String, String> map) throws IllegalAccessException {
        String page = map.get("page").toString();
        Integer current_page = Integer.parseInt(page);
        Integer page_size = 10;
        Integer start = (current_page - 1) * page_size;
        String sql = "select * from category limit " + start + "," + page_size;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        PageList pageList = new PageList();
        pageList.setList(list);
        pageList.setPage(current_page);
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM category", Integer.class);
        pageList.setTotalRows(count);
        int pages = 0;
        if (count % page_size == 0) {
            pages = count / page_size;
        } else {
            pages = count / page_size + 1;
        }
        pageList.setPages(pages);

        return R.ok("获取品类列表成功").data(FormatUtil.objectToMap(pageList));
    }

    // 获取所有品类
    @RequestMapping("category/all")
    public R selectCategory() throws IllegalAccessException {
        String sql = "select * from category";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        return R.ok("获取所有品类成功").data("list", list);
    }

    // 添加品类
    @RequestMapping("/category/add")
    public R addCategory(@RequestBody Map<String, String> map) {
        String name = map.get("name").toString();
        String time = DateUtil.getNowTime();

        // 判断是否已存在
        String sql = "select * from category where `name` = '"+name+"'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() > 0) {
            return R.error(-1001, "此品类已存在");
        }
        sql = "insert into category(`name`, `create_time`)values('"+name+"','"+time+"')";
        jdbcTemplate.update(sql);

        return R.ok("添加品类成功");
    }

    @RequestMapping("/category/edit")
    public R editCategory(@RequestBody Map<String, String> map) {
        Integer id = Integer.parseInt(map.get("id").toString());
        String name = map.get("name").toString();

        String sql = "select * from category where `id` = "+id;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() == 0) {
            return R.error(-1001, "品类不存在");
        }

        sql = "update category set name = ? where id = "+id;
        Object[] objects = new Object[1];
        objects[0] = name;
        try{
            jdbcTemplate.update(sql, objects);
        }catch (InvalidResultSetAccessException e) {
            // 异常处理
            throw new RuntimeException(e);
        }

        return R.ok("编辑成功");
    }

    // 删除品类
    @RequestMapping("/category/del")
    public R delCategory(@RequestBody Map<String, String> map) {
        Integer id = Integer.parseInt(map.get("id").toString());

        String sql = "delete from category where id = "+id;
        try{
            jdbcTemplate.update(sql);
        }catch (InvalidResultSetAccessException e) {
            // 异常处理
            throw new RuntimeException(e);
        }

        return R.ok("删除成功");
    }
}
