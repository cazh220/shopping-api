package com.example.shoppingapi.service;

import com.example.shoppingapi.util.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class PageService {

    @Autowired
    @Qualifier("jdbcTemplate")
    JdbcTemplate jdbcTemplate;

    public String simplePaging(String sql){
        System.out.println(sql);
        jdbcTemplate.queryForList(sql);
        return sql;
    }
}
