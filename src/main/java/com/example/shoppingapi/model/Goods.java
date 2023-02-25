package com.example.shoppingapi.model;

public class Goods {
    private int id;
    private String name;
    private int category_id;
    private int price;
    private int num;
    private String pic;
    private String desc;
    private int state;
    private String category_name;
    private String create_time;
    private String update_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() { return category_id; }

    public void setCategoryId(int category_id) {
        this.category_id = category_id;
    }

    public int getPrice() { return price; }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNum() { return num; }

    public void setNum(int num) {
        this.num = num;
    }

    public int getState() { return state; }

    public void setState(int state) {
        this.state = state;
    }

    public String getCategoryName() {
        return category_name;
    }

    public void setCategoryName(String category_name) {
        this.category_name = category_name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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
