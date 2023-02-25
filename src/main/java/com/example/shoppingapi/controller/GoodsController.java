package com.example.shoppingapi.controller;

import com.example.shoppingapi.model.Goods;
import com.example.shoppingapi.model.User;
import com.example.shoppingapi.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // 商品列表
    @RequestMapping("/goods/list")
    public R goodsList(@RequestBody Map<String, String> map) throws IllegalAccessException {
        String name = map.get("name").toString();
        int state = Integer.parseInt(map.get("state").toString());
        int current_page = Integer.parseInt(map.get("page").toString());
        int page_size = 10;
        int start = (current_page-1)*page_size;
        String sql = "select a.*, b.name as `category_name` from goods a left join category b on a.category_id = b.id where a.state <> -1";
        String sql_count = "select count(*) from goods a left join category b on a.category_id = b.id where a.state <> -1";
        if (name != "") {
            sql += " AND a.name like '%"+name+"%'";
            sql_count += " AND a.name like '%"+name+"%'";
        }
        if (state != 0) {
            sql += " AND a.state ="+state;
            sql_count += " AND a.state ="+state;
        }
        System.out.println(sql);
        int count = jdbcTemplate.queryForObject(sql_count, Integer.class);

        sql += " order by id desc limit "+ start + "," + page_size;

        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        PageList pageList = new PageList();
        pageList.setList(list);
        pageList.setPage(current_page);

        pageList.setTotalRows(count);
        int pages= 0;
        if(count % page_size == 0){ pages = count / page_size;}
        else { pages = count / page_size +1 ;}
        pageList.setPages(pages);

        return R.ok("获取商品列表成功").data(FormatUtil.objectToMap(pageList));
    }

    // 添加商品
    @RequestMapping("/goods/add")
    public R addGoods(@RequestBody Map<String, String> map) {
        String name = map.get("name").toString();
        String pic = map.get("pic").toString();
        String desc = map.get("desc").toString();
        int category_id = Integer.parseInt(map.get("category_id").toString());
        int price = Integer.parseInt(map.get("price").toString());
        int num = Integer.parseInt(map.get("num").toString());

        // TODO 接收参数校验, 必传等

        String time = DateUtil.getNowTime();
        // 判断此商品是否已存在
        String sql = "select * from goods where state <> -1 and `name` = '"+name+"' and category_id = "+ category_id;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() > 0) {
            return R.error(-1001, "此商品已存在");
        }
        sql = "insert into goods(`name`,`category_id`, `price`, `num`, `pic`, `desc`, `create_time`)values('"+name+"',"+category_id+","+price+","+num+",'"+pic+"','"+desc+"','"+time+"')";
        jdbcTemplate.update(sql);
        return R.ok("添加商品成功");
    }

    // 商品编辑
    @RequestMapping("/goods/edit")
    public R editGoods(@RequestBody Map<String, String> map) {
        Integer id = Integer.parseInt(map.get("id").toString());
        String name = map.get("name").toString();
        int category_id = Integer.parseInt(map.get("category_id").toString());
        int price = Integer.parseInt(map.get("price").toString());
        int num = Integer.parseInt(map.get("num").toString());
        String desc = map.get("desc").toString();

        String sql = "select * from goods where `id` = "+id;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() == 0) {
            return R.error(-1001, "商品不存在");
        }

        sql = "update goods set `name` = ?, `category_id` = ?, `price` = ?, `num` = ?, `desc` = ? where id = "+id;
        Object[] objects = new Object[5];
        objects[0] = name;
        objects[1] = category_id;
        objects[2] = price;
        objects[3] = num;
        objects[4] = desc;

        System.out.println(sql);
        try{
            jdbcTemplate.update(sql, objects);
        }catch (InvalidResultSetAccessException e) {
            // 异常处理
            throw new RuntimeException(e);
        }

        return R.ok("商品编辑成功");
    }

    // 更新商品状态
    @RequestMapping("/goods/state")
    public R stateGoods(@RequestBody Map<String, String> map) {
        Integer id = Integer.parseInt(map.get("id").toString());
        Integer state = Integer.parseInt(map.get("state").toString());

        String sql = "update goods set state = ? where id = "+id;
        Object[] objects = new Object[1];
        objects[0] = state;
        try{
            jdbcTemplate.update(sql, objects);
        }catch (InvalidResultSetAccessException e) {
            // 异常处理
            throw new RuntimeException(e);
        }

        String msg = "商品状态修改";
        if (state == -1) {
            msg = "商品删除成功";
        } else if (state == 1) {
            msg = "商品上架";
        }

        return R.ok(msg);
    }

    @RequestMapping("/goods/detail")
    public R userDetail(@RequestBody Map<String, String> map) throws IllegalAccessException {
        Integer id = Integer.parseInt(map.get("id").toString());

        Goods goods;
        try {
            goods =  jdbcTemplate.queryForObject("select a.*,b.`name` as `category_name` from goods a left join  category b on a.category_id = b.id where a.id = ?", new BeanPropertyRowMapper<>(Goods.class), id);
        } catch (EmptyResultDataAccessException e) {
            return R.ok("获取成功");
        }

        return R.ok("获取成功").data(FormatUtil.objectToMap(goods));

    }

    @RequestMapping("/upload")
    public R uploadPic(HttpServletRequest req, @RequestParam("pic") MultipartFile file, Model m) {
        String fileName;
        try {
            //2.根据时间戳创建新的文件名，这样即便是第二次上传相同名称的文件，也不会把第一次的文件覆盖了
            fileName = System.currentTimeMillis() + file.getOriginalFilename();
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
            fileName = MD5Util.MD5Encode(String.valueOf(System.currentTimeMillis()), "UTF-8") + "." + ext;
            System.out.println(fileName);
            //3.通过req.getServletContext().getRealPath("") 获取当前项目的真实路径，然后拼接前面的文件名
//            String destFileName = this.getClass().getClassLoader().getResource("").getPath()+ "uploaded" + File.separator + fileName;
            File directory = new File("../shoppingapi/src/main/resources");
            String reportPath = directory.getCanonicalPath();
            String destFileName = reportPath + "/upload" + File.separator + fileName;
//            String destFileName = req.getServletContext().getRealPath("") + "uploaded" + File.separator + fileName;
            System.out.println(destFileName);
            //4.第一次运行的时候，这个文件所在的目录往往是不存在的，这里需要创建一下目录（创建到了webapp下uploaded文件夹下）
            File destFile = new File(destFileName);
            destFile.getParentFile().mkdirs();
            //5.把浏览器上传的文件复制到希望的位置
            file.transferTo(destFile);
            //6.把文件名放在model里，以便后续显示用
            m.addAttribute("fileName", fileName);
            System.out.println(m.getAttribute("fileName"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return R.error(-1002, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return R.error(-1002, e.getMessage());
        }

        return R.ok("上传成功").data("pic", fileName);
    }

    @RequestMapping("/showpic")
    public R getPic() throws IOException {
        InputStream pngInStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("upload/c34aa65f602f0b62ef2ef61ecf608472.PNG");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n;
        while ((n = pngInStream.read(buffer)) != -1) {
            out.write(buffer,0,n);
        }
        System.out.println(out.toByteArray());
        return R.ok("获取成功");
    }

}
