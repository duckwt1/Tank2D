package com.tank2d.client.map;

import javafx.scene.image.Image;

import java.awt.*;

public class Tile {
    public int gid;
    public Image image;
    public boolean collision = false;
    public Polygon solidPolygon;
}