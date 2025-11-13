package com.tank2d.client.entity;

import com.tank2d.client.map.MapLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.awt.*;
import java.util.Map;

public abstract class Entity {
    public double x;
    public double y;
    public Polygon solidArea;
    public double speed;
    public Image[] images;
    public int spriteNum = 0;
    public MapLoader mapLoader;
    public Entity(double x, double y, Polygon solidArea, double speed, MapLoader mapLoader) {
        this.x = x;
        this.y = y;
        this.solidArea = solidArea;
        this.speed = speed;
        this.mapLoader = mapLoader;
    }

    public void getImages() {}
    public void update() {}

    public void draw(GraphicsContext gc) {

    }
}
