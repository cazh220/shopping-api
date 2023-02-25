package com.example.shoppingapi.model;

public class Cart {
    private int id;
    private int user_id;
    private int goods_id;
    private String goods_name;
    private int num;
    private String create_time;
    private String update_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() { return user_id; }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public int getGoodsId() { return goods_id; }

    public void setGoodsId(int goods_id) {
        this.goods_id = goods_id;
    }

    public String getGoodsName() { return goods_name; }

    public void setGoodsName(String goods_name) {
        this.goods_name = goods_name;
    }

    public int getNum() { return num; }

    public void setNum(int num) {
        this.num = num;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }
}
