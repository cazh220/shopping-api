package com.example.shoppingapi.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName: R
 * @Description: TODO
 * @Author: liujianfu
 * @Date: 2022/12/03 15:53:28
 * @Version: V1.0
 **/
public class R {
//    private Boolean success;
    private Integer code;
    private String message;
    private Map<String, Object> data = new LinkedHashMap<String, Object>();
    //把构造方法私有
    private R() {}

    //成功静态方法
    public static R ok(String message) {
        R r = new R();
        r.setCode(ResultCode.SUCCESS);
        if (message != "") {
            r.setMessage(message);
        } else {
            r.setMessage("成功");
        }
        return r;
    }

    //失败静态方法
    public static R error(Integer code, String message) {
        R r = new R();
        if (code != null) {
            r.setCode(code);
        } else {
            r.setCode(ResultCode.ERROR);
        }
        if (message != "") {
            r.setMessage(message);
        } else {
            r.setMessage("失败");
        }
        return r;
    }



//    public R success(Boolean success) {
//        this.setSuccess(success);
//        return this;
//    }



    public R message(String message) {
        this.setMessage(message);
        return this;
    }



    public R code(Integer code) {
        this.setCode(code);
        return this;
    }

    public R data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public R data(Map<String, Object> map) {
        this.setData(map);
        return this;
    }

//    public Boolean getSuccess() {
//        return success;
//    }
//
//    public void setSuccess(Boolean success) {
//        this.success = success;
//    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

}

