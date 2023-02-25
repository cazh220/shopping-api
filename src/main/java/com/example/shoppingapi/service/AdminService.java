package com.example.shoppingapi.service;

import com.example.shoppingapi.model.Admin;
import com.example.shoppingapi.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class AdminService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public AdminService addAdmin(String account, String password) {
        String pwd = MD5Util.MD5Encode(password, "UTF-8");
        System.out.println(pwd);
        return null;
    }

}
