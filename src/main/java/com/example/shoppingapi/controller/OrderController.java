package com.example.shoppingapi.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.shoppingapi.model.Goods;
import com.example.shoppingapi.model.Order;
import com.example.shoppingapi.model.OrderDetail;
import com.example.shoppingapi.model.User;
import com.example.shoppingapi.util.DateUtil;
import com.example.shoppingapi.util.FormatUtil;
import com.example.shoppingapi.util.PageList;
import com.example.shoppingapi.util.R;
import jdk.internal.org.objectweb.asm.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

@RestController
public class OrderController {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping("/order/list")
    public R orderList(@RequestBody Map<String, String> map) throws IllegalAccessException {
        String order_no = map.get("order_no").toString();
        int state = Integer.parseInt(map.get("state").toString());
        int user_id = Integer.parseInt(map.get("user_id").toString());
        int current_page = Integer.parseInt(map.get("page").toString());

        Integer[] intList = {1, 0, -1, -2};
        boolean contains = Arrays.stream(intList).anyMatch(x -> x == state);
        // 参数校验
        if (!contains) {
            return R.error(-1001, "参数error");
        }

        int page_size = 10;
        int start = (current_page-1)*page_size;
        String sql = "select * from orders ";
        String sql_count = "select count(*) from orders ";

        if (state == -2) {
            // 全部
            sql += " where `state` IN (-1, 0, 1) ";
            sql_count += " where `state` IN (-1, 0, 1) ";
        } else {
            sql += " where state = "+state;
            sql_count += " where state = "+state;
        }

        if (order_no != "") {
            sql += " AND order_no like '%"+order_no+"%'";
            sql_count += " AND order_no like '%"+order_no+"%'";
        }

        if (user_id > 0) {
            sql += " AND user_id ="+user_id;
            sql_count += " AND user_id ="+user_id;
        }

        System.out.println(sql_count);
        int count = jdbcTemplate.queryForObject(sql_count, Integer.class);

        sql += " order by id desc limit "+ start + "," + page_size;
        System.out.println(sql);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        System.out.println(list);

        if (list.size() > 0) {
            // 获取商品信息
            for (int i=0; i<list.size(); i++) {
                List goods = jdbcTemplate.queryForList("select * from orders_detail where order_no = '"+list.get(i).get("order_no")+"'");
                Map<String, Object> obj = list.get(i);
                obj.put("goods", goods);
            }
        }

        PageList pageList = new PageList();
        pageList.setList(list);
        pageList.setPage(current_page);

        pageList.setTotalRows(count);
        int pages= 0;
        if(count % page_size == 0){ pages = count / page_size;}
        else { pages = count / page_size +1 ;}
        pageList.setPages(pages);

        return R.ok("获取订单列表成功").data(FormatUtil.objectToMap(pageList));
    }

    //创建订单
    @RequestMapping("/order/add")
    public R orderCreate(@RequestBody Map<String, Object> map) throws IllegalAccessException {
        Object obj = map.get("goods");
        List<Map<String, Integer>> list = null;
        if(obj instanceof ArrayList){
            list = (ArrayList)map.get("goods");
        }

        int user_id = Integer.parseInt(map.get("user_id").toString());

        // 生成订单号
        Date d = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddkkmmss");
        String order_no = simpleDateFormat.format(d);
        Random random=new Random();
        int number=random.nextInt(90)+10;
        order_no += number;
        String time = DateUtil.getNowTime();

        Goods good;
        int amount = 0;
        List<String> goods_list = new ArrayList<>();
        // 获取商品信息
        for (int i = 0; i< list.size(); i++) {
            int goods_id = list.get(i).get("goods_id");
            String sql = "select a.*, b.name as `category_name` from goods a left join category b on a.category_id = b.id where a.id = "+goods_id;

            good = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Goods.class));
            String name = FormatUtil.objectToMap(good).get("name").toString();
            String category_name = FormatUtil.objectToMap(good).get("category_name").toString();
            String pic = FormatUtil.objectToMap(good).get("pic").toString();
            int price = Integer.parseInt(FormatUtil.objectToMap(good).get("price").toString());
            int left_num = Integer.parseInt(FormatUtil.objectToMap(good).get("num").toString());
            int num = list.get(i).get("num");
            Map g = new HashMap<>();

            g.put("name", name);
            g.put("category_name", category_name);
            g.put("pic", pic);
            g.put("goods_id", goods_id);
            g.put("price", price);
            g.put("num", num);
            int remain_num = left_num - num;
            g.put("remain_num", remain_num);
            goods_list.add(g.toString());

            if (num > left_num) {
                return R.error(-1003, name+"库存不足");
            }

            amount += price*num;
        }

        // 获取用户信息
        User user = jdbcTemplate.queryForObject("select * from users where id = ?", new BeanPropertyRowMapper<>(User.class), user_id);
        String user_name = FormatUtil.objectToMap(user).get("name").toString();
        String address = FormatUtil.objectToMap(user).get("address").toString();
        int money = Integer.parseInt(FormatUtil.objectToMap(user).get("money").toString());
        // 判断余额是否够
        if (money < amount) {
            return R.error(-1001, "余额不足");
        }

        // 减用户余额
        int left_amount = money - amount;
        jdbcTemplate.update("update users set money = ? where id = ?", left_amount, user_id);

        for (int i = 0; i < goods_list.size(); i++) {
            String name = FormatUtil.mapStringToMap(goods_list.get(i)).get("name");
            String category_name = FormatUtil.mapStringToMap(goods_list.get(i)).get("category_name");
            String pic = FormatUtil.mapStringToMap(goods_list.get(i)).get("pic");
            int goods_id = Integer.parseInt(FormatUtil.mapStringToMap(goods_list.get(i)).get("goods_id"));
            int price = Integer.parseInt(FormatUtil.mapStringToMap(goods_list.get(i)).get("price"));
            int num = Integer.parseInt(FormatUtil.mapStringToMap(goods_list.get(i)).get("num"));
            // 入订单明细库
            jdbcTemplate.update("insert into orders_detail(order_no, category_name, goods_id, goods_name, price, num, pic, create_time)values(?, ? ,?, ?, ?, ?, ?, ?)", order_no, category_name, goods_id, name, price, num, pic, time);

            // 减库存
            int remain_num = Integer.parseInt(FormatUtil.mapStringToMap(goods_list.get(i)).get("remain_num"));
            jdbcTemplate.update("update goods set num = ? where id = ?", remain_num, goods_id);
        }

        jdbcTemplate.update("insert into orders(order_no, amount, address, user_id, user_name, create_time)values(?, ?, ?, ?, ?, ?)", order_no, amount, address, user_id, user_name, time);

        return R.ok("创建订单成功");
    }

    // 编辑状态
    @RequestMapping("/order/state")
    public R orderState(@RequestBody Map<String, String> map) throws IllegalAccessException {
        int id = Integer.parseInt(map.get("id").toString());
        int state = Integer.parseInt(map.get("state").toString());

        String sql = "select * from orders where `id` = "+id;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() == 0) {
            return R.error(-1001, "订单不存在");
        }

        jdbcTemplate.update("update orders set state = ? where id = ?", state, id);

        if (state == -1) {
            // 取消订单，返回用户余额
            jdbcTemplate.update("update users set money  =  money + ? where id = ?", list.get(0).get("amount"), list.get(0).get("user_id"));
        }

        return R.ok("修改订单状态成功");
    }

    // 订单统计
    @RequestMapping("/order/count")
    public R countOrders() {
        String sql = "select count(*) as nums,DATE_FORMAT(create_time,'%Y-%m-%d') as date  from orders group by DATE_FORMAT(create_time,'%Y-%m-%d') order by date asc";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        return R.ok("订单统计成功").data("list", list);
    }

    @RequestMapping("/total")
    public R count() {
        int user_num = jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
        int order_num = jdbcTemplate.queryForObject("select count(*) from orders", Integer.class);
        int goods_num = jdbcTemplate.queryForObject("select count(*) from goods", Integer.class);
        String yestoday = DateUtil.getYestoday();
        String sql = "select count(*) from orders where DATE_FORMAT(create_time,'%Y-%m-%d') = '"+yestoday+"'";
        System.out.println(sql);
        int yestoday_num = jdbcTemplate.queryForObject(sql, Integer.class);

        Object[] objects = new Object[4];
        objects[0] = user_num;
        objects[1] = order_num;
        objects[2] = goods_num;
        objects[3] = yestoday_num;

        return R.ok("订单统计成功").data("list", objects);
    }


}
