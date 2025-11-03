package com.tank2d.client.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Polygon;

public abstract class Entity {
    public double x;
    public double y;
    public Polygon solidArea;
    public double speed;
    public Image[] images;
    public int spriteNum = 0;

    public Entity(double x, double y, Polygon solidArea, double speed) {
        this.x = x;
        this.y = y;
        this.solidArea = solidArea;
        this.speed = speed;
    }

    public void getImages() {}
    public void update() {}

    public void draw(GraphicsContext gc) {

    }
}
