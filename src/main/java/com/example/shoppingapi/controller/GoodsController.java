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

    // ๅๅๅ่กจ
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

        return R.ok("่ทๅๅๅๅ่กจๆๅ").data(FormatUtil.objectToMap(pageList));
    }

    // ๆทปๅ?ๅๅ
    @RequestMapping("/goods/add")
    public R addGoods(@RequestBody Map<String, String> map) {
        String name = map.get("name").toString();
        String pic = map.get("pic").toString();
        String desc = map.get("desc").toString();
        int category_id = Integer.parseInt(map.get("category_id").toString());
        int price = Integer.parseInt(map.get("price").toString());
        int num = Integer.parseInt(map.get("num").toString());

        // TODO ๆฅๆถๅๆฐๆ?ก้ช, ๅฟไผ?็ญ

        String time = DateUtil.getNowTime();
        // ๅคๆญๆญคๅๅๆฏๅฆๅทฒๅญๅจ
        String sql = "select * from goods where state <> -1 and `name` = '"+name+"' and category_id = "+ category_id;
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() > 0) {
            return R.error(-1001, "ๆญคๅๅๅทฒๅญๅจ");
        }
        sql = "insert into goods(`name`,`category_id`, `price`, `num`, `pic`, `desc`, `create_time`)values('"+name+"',"+category_id+","+price+","+num+",'"+pic+"','"+desc+"','"+time+"')";
        jdbcTemplate.update(sql);
        return R.ok("ๆทปๅ?ๅๅๆๅ");
    }

    // ๅๅ็ผ่พ
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
            return R.error(-1001, "ๅๅไธๅญๅจ");
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
            // ๅผๅธธๅค็
            throw new RuntimeException(e);
        }

        return R.ok("ๅๅ็ผ่พๆๅ");
    }

    // ๆดๆฐๅๅ็ถๆ
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
            // ๅผๅธธๅค็
            throw new RuntimeException(e);
        }

        String msg = "ๅๅ็ถๆไฟฎๆน";
        if (state == -1) {
            msg = "ๅๅๅ?้คๆๅ";
        } else if (state == 1) {
            msg = "ๅๅไธๆถ";
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
            return R.ok("่ทๅๆๅ");
        }

        return R.ok("่ทๅๆๅ").data(FormatUtil.objectToMap(goods));

    }

    @RequestMapping("/upload")
    public R uploadPic(HttpServletRequest req, @RequestParam("pic") MultipartFile file, Model m) {
        String fileName;
        try {
            //2.ๆ?นๆฎๆถ้ดๆณๅๅปบๆฐ็ๆไปถๅ๏ผ่ฟๆ?ทๅณไพฟๆฏ็ฌฌไบๆฌกไธไผ?็ธๅๅ็งฐ็ๆไปถ๏ผไนไธไผๆ็ฌฌไธๆฌก็ๆไปถ่ฆ็ไบ
            fileName = System.currentTimeMillis() + file.getOriginalFilename();
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
            fileName = MD5Util.MD5Encode(String.valueOf(System.currentTimeMillis()), "UTF-8") + "." + ext;
            System.out.println(fileName);
            //3.้่ฟreq.getServletContext().getRealPath("") ่ทๅๅฝๅ้กน็ฎ็็ๅฎ่ทฏๅพ๏ผ็ถๅๆผๆฅๅ้ข็ๆไปถๅ
//            String destFileName = this.getClass().getClassLoader().getResource("").getPath()+ "uploaded" + File.separator + fileName;
            File directory = new File("../shoppingapi/src/main/resources");
            String reportPath = directory.getCanonicalPath();
            String destFileName = reportPath + "/upload" + File.separator + fileName;
//            String destFileName = req.getServletContext().getRealPath("") + "uploaded" + File.separator + fileName;
            System.out.println(destFileName);
            //4.็ฌฌไธๆฌก่ฟ่ก็ๆถๅ๏ผ่ฟไธชๆไปถๆๅจ็็ฎๅฝๅพๅพๆฏไธๅญๅจ็๏ผ่ฟ้้่ฆๅๅปบไธไธ็ฎๅฝ๏ผๅๅปบๅฐไบwebappไธuploadedๆไปถๅคนไธ๏ผ
            File destFile = new File(destFileName);
            destFile.getParentFile().mkdirs();
            //5.ๆๆต่งๅจไธไผ?็ๆไปถๅคๅถๅฐๅธๆ็ไฝ็ฝฎ
            file.transferTo(destFile);
            //6.ๆๆไปถๅๆพๅจmodel้๏ผไปฅไพฟๅ็ปญๆพ็คบ็จ
            m.addAttribute("fileName", fileName);
            System.out.println(m.getAttribute("fileName"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return R.error(-1002, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return R.error(-1002, e.getMessage());
        }

        return R.ok("ไธไผ?ๆๅ").data("pic", fileName);
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
        return R.ok("่ทๅๆๅ");
    }

}
