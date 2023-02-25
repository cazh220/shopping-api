package com.example.shoppingapi.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.shoppingapi.model.Admin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

public class ResponseUtil {

    public static void setJsonResponse(HttpServletResponse httpServletResponse, String data) throws IOException {

        //Map map = Maps.newHashMap();
        Map<String, Object>map = new HashMap<String, Object>();

        map.put("code", 200);
        map.put("msg", "ok");
        map.put("data", data);
        httpServletResponse.addHeader("Content-Type", "application/json;chaset=UTF-8");
        httpServletResponse.getOutputStream().write(JSON.toJSONBytes(map));
    }

    /**
     * 创建日期:2018年4月6日<br/>
     * 代码创建:黄聪<br/>
     * 功能描述:通过request的方式来获取到json数据<br/>
     * @param jsonobject 这个是阿里的 fastjson对象
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/json/data", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String getByJSON(@RequestBody JSONObject jsonParam) {
        // 直接将json信息打印出来
        System.out.println(jsonParam.toJSONString());

        // 将获取的json数据封装一层，然后在给返回
        JSONObject result = new JSONObject();
        result.put("msg", "ok");
        result.put("method", "json");
        result.put("data", jsonParam);

        return result.toJSONString();
    }

    public static String jsonResponse(JSONObject jsonParam) {
        System.out.println(jsonParam.toJSONString());

        // 将获取的json数据封装一层，然后在给返回
        JSONObject result = new JSONObject();
        result.put("code", 200);
        result.put("msg", "ok");
        result.put("data", jsonParam);

        return result.toJSONString();
    }
}
