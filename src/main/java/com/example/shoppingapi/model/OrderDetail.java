package com.example.shoppingapi.model;

public class OrderDetail {
    private int id;
    private String order_no;
    private String category_name;
    private int goods_id;
    private String goods_name;
    private String pic;
    private int price;
    private int num;
    private String create_time;
    private String update_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderNo() { return order_no; }

    public void setOrderNo(String order_no) {
        this.order_no = order_no;
    }

    public String getCategoryName() {
        return category_name;
    }

    public void setCategoryName(String category_name) {
        this.category_name = category_name;
    }

    public int getGoodsId() { return goods_id; }

    public void setGoodsId(int goods_id) {
        this.goods_id = goods_id;
    }

    public String getGoodsName() { return goods_name; }

    public void setGoodsName(String goods_name) {
        this.goods_name = goods_name;
    }
    public String getPic() { return pic; }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getNum() { return num; }

    public void setNum(int num) {
        this.num = num;
    }

    public int getPrice() { return price; }

    public void setPrice(int price) {
        this.price = price;
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
