package com.tank2d.client.entity;

public class ShopItem {
    public int id;
    public String name;
    public String description;
    public int price;
    public double hp;
    public double mp;
    public double spd;
    public double dmg;
    public double discount;
    public int stock;

    public ShopItem(int id, String name, String description, int price,
                    double hp, double mp, double spd, double dmg,
                    double discount, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.hp = hp;
        this.mp = mp;
        this.spd = spd;
        this.dmg = dmg;
        this.discount = discount;
        this.stock = stock;
    }

    public int getFinalPrice() {
        return (int)(price * (1 - discount));
    }
}
