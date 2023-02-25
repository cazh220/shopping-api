package com.example.shoppingapi.controller;

import com.example.shoppingapi.model.Cart;
import com.example.shoppingapi.util.DateUtil;
import com.example.shoppingapi.util.FormatUtil;
import com.example.shoppingapi.util.PageList;
import com.example.shoppingapi.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CartController {
    @Autowired
    JdbcTemplate jdbcTemplate;

    // 购物车详情
    @RequestMapping("/cart/detail")
    public R carts(@RequestBody Map<String, String> map) throws IllegalAccessException {
        int user_id = Integer.parseInt(map.get("user_id").toString());
        System.out.println(user_id);
        List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from cart where user_id = "+user_id);
        System.out.println(list);
        return R.ok("获取购物车成功").data("list", list);
    }

    // 添加购物车
    @RequestMapping("/cart/add")
    public R addCart(@RequestBody Map<String, String> map) {
        int user_id = Integer.parseInt(map.get("user_id").toString());
        int goods_id = Integer.parseInt(map.get("goods_id").toString());
        String goods_name = map.get("goods_name").toString();
        int num = Integer.parseInt(map.get("num").toString());

        // TODO 接收参数校验, 必传等

        String time = DateUtil.getNowTime();
        String sql = "insert into cart(`goods_name`,`goods_id`, `user_id`, `num`, `create_time`, `update_time`)values('"+goods_name+"',"+goods_id+","+user_id+","+num+",'"+time+"','"+time+"')";
        jdbcTemplate.update(sql);
        return R.ok("添加购物车成功");
    }

    // 清空购物车
    @RequestMapping("/cart/clear")
    public R clearCart(@RequestBody Map<String, String> map) {
        int user_id = Integer.parseInt(map.get("user_id").toString());

        jdbcTemplate.update("delete from cart where user_id = ?", user_id);
        return R.ok("清空购物车成功");
    }
}
