package com.tank2d.client.map;

import javafx.scene.image.Image;

import java.util.ArrayList;

public class TileSet {
    public int firstgid;
    public String source;
    public Image image;
    public int tileWidth, tileHeight, columns, tileCount;
    public ArrayList<Tile> collisionTile;
}