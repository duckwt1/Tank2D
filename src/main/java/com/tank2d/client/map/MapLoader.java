package com.tank2d.client.map;

// Pham Ngoc Duc - Lớp 23JIT - Trường VKU - MSSV: 23IT059

import com.tank2d.client.entity.Player;
import com.tank2d.shared.Constant;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Area;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Comparator;

public class MapLoader {

    public int id;
    public String name;
    public ArrayList<Layer> layers = new ArrayList<>();
    public ArrayList<TileSet> tileSets = new ArrayList<>();
    public Tile[] tiles;
    public int width;
    public int height;

    public MapLoader(int id) {
        this.id = id;
        loadMap("res/map/map" + id + ".tmj");
    }

    private void loadMap(String mapPath) {
        try {
            System.out.println("🗺️ Loading map: " + mapPath);
            File mapFile = new File(mapPath);
            if (!mapFile.exists()) {
                System.err.println("❌ Map file not found: " + mapPath);
                return;
            }

            String jsonString = new String(new FileInputStream(mapPath).readAllBytes());
            JSONObject json = new JSONObject(jsonString);

            int width = json.getInt("width");
            int height = json.getInt("height");
            this.width = width;
            this.height = height;

            // ===== Load layers =====
            JSONArray jsonLayers = json.getJSONArray("layers");
            for (int i = 0; i < jsonLayers.length(); i++) {
                JSONObject jLayer = jsonLayers.getJSONObject(i);
                if (!jLayer.getString("type").equals("tilelayer")) continue;

                Layer layer = new Layer();
                layer.id = jLayer.getInt("id");
                layer.visible = jLayer.optBoolean("visible", true);
                JSONArray data = jLayer.getJSONArray("data");

                layer.data = new int[height][width];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int idx = y * width + x;
                        layer.data[y][x] = data.getInt(idx);
                    }
                }
                layers.add(layer);
            }
            layers.sort(Comparator.comparingInt(l -> l.id));
            System.out.println("✅ Loaded " + layers.size() + " layers.");

            // ===== Load tilesets =====
            JSONArray jsonTilesets = json.getJSONArray("tilesets");
            int maxGid = 0;

            for (int i = 0; i < jsonTilesets.length(); i++) {
                JSONObject jTs = jsonTilesets.getJSONObject(i);

                TileSet ts = new TileSet();
                ts.firstgid = jTs.getInt("firstgid");

                File mapDir = mapFile.getParentFile();
                String rawSource = jTs.getString("source");
                File tsxFile = new File(mapDir, rawSource);
                if (!tsxFile.exists()) tsxFile = new File("res/tileset/" + rawSource);

                if (!tsxFile.exists()) {
                    System.err.println("❌ Tileset not found: " + tsxFile.getAbsolutePath());
                    continue;
                }

                // Parse .tsx
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(tsxFile);
                doc.getDocumentElement().normalize();

                Element tilesetElement = doc.getDocumentElement();
                ts.tileWidth = Integer.parseInt(tilesetElement.getAttribute("tilewidth"));
                ts.tileHeight = Integer.parseInt(tilesetElement.getAttribute("tileheight"));
                ts.columns = Integer.parseInt(tilesetElement.getAttribute("columns"));
                ts.tileCount = Integer.parseInt(tilesetElement.getAttribute("tilecount"));

                NodeList imageNodes = tilesetElement.getElementsByTagName("image");
                if (imageNodes.getLength() > 0) {
                    Element imageElement = (Element) imageNodes.item(0);
                    String imageSource = imageElement.getAttribute("source");
                    String imageFileName = new File(imageSource).getName();

                    ts.image = new Image("file:res/tile/" + imageFileName);
                    tileSets.add(ts);
                    maxGid = Math.max(maxGid, ts.firstgid + ts.tileCount - 1);

                    System.out.println("🧩 Loaded tileset: " + imageFileName);
                }

                // ===== Load collision polygons =====
                NodeList tileCollisionList = tilesetElement.getElementsByTagName("tile");
                ArrayList<Tile> collisionTiles = new ArrayList<>();

                for (int k = 0; k < tileCollisionList.getLength(); k++) {
                    Element tileElement = (Element) tileCollisionList.item(k);
                    int tileId = Integer.parseInt(tileElement.getAttribute("id"));

                    NodeList objectGroupList = tileElement.getElementsByTagName("objectgroup");
                    if (objectGroupList.getLength() == 0) continue;

                    Element objectGroup = (Element) objectGroupList.item(0);
                    NodeList objectList = objectGroup.getElementsByTagName("object");
                    if (objectList.getLength() == 0) continue;

                    Element objectElement = (Element) objectList.item(0);
                    float offsetX = Float.parseFloat(objectElement.getAttribute("x"));
                    float offsetY = Float.parseFloat(objectElement.getAttribute("y"));

                    NodeList polygonList = objectElement.getElementsByTagName("polygon");
                    if (polygonList.getLength() == 0) continue;

                    Element polygonElement = (Element) polygonList.item(0);
                    String pointsString = polygonElement.getAttribute("points");
                    String[] pointPairs = pointsString.trim().split(" ");

                    int[] xPoints = new int[pointPairs.length];
                    int[] yPoints = new int[pointPairs.length];
                    for (int l = 0; l < pointPairs.length; l++) {
                        String[] xy = pointPairs[l].split(",");
                        float px = Float.parseFloat(xy[0]);
                        float py = Float.parseFloat(xy[1]);
                        xPoints[l] = Math.round(offsetX + px);
                        yPoints[l] = Math.round(offsetY + py);
                    }

                    Polygon polygon = new Polygon(xPoints, yPoints, pointPairs.length);

                    Tile tile = new Tile();
                    tile.gid = tileId;
                    tile.collision = true;
                    tile.solidPolygon = polygon;
                    collisionTiles.add(tile);
                }

                ts.collisionTile = collisionTiles;
                System.out.println("⚙️ Collision tiles in tileset: " + collisionTiles.size());
            }

            // ===== Build GID-indexed tiles =====
            tiles = new Tile[maxGid + 1];
            for (TileSet ts : tileSets) {
                int tilesPerRow = ts.columns;
                for (int i = 0; i < ts.tileCount; i++) {
                    int gid = ts.firstgid + i;
                    int sx = (i % tilesPerRow) * ts.tileWidth;
                    int sy = (i / tilesPerRow) * ts.tileHeight;

                    Tile tile = new Tile();
                    tile.gid = gid;

                    // crop from tileset
                    tile.image = new WritableImage(
                            ts.image.getPixelReader(),
                            sx, sy,
                            ts.tileWidth, ts.tileHeight
                    );

                    Tile check = exist(gid - 1, ts.collisionTile);
                    if (check != null) {
                        tile.collision = true;
                        tile.solidPolygon = check.solidPolygon;
                    }
                    tiles[gid] = tile;
                }
            }

            System.out.println("✅ Map " + id + " loaded successfully with " + tileSets.size() + " tilesets.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Tile exist(int id, ArrayList<Tile> list) {
        for (Tile tile : list) {
            if (tile.gid == id) return tile;
        }
        return null;
    }

    public void draw(GraphicsContext gc, Player player) {
        if (tiles == null || layers.isEmpty()) return;

        int playerTileX = (int) (player.x / Constant.TILESIZE);
        int playerTileY = (int) (player.y / Constant.TILESIZE);

        int halfCols = Constant.SCREEN_COL / 2;
        int halfRows = Constant.SCREEN_ROW / 2;

        for (Layer layer : layers) {
            if (!layer.visible) continue;

            for (int y = playerTileY - halfRows; y <= playerTileY + halfRows; y++) {
                for (int x = playerTileX - halfCols; x <= playerTileX + halfCols; x++) {
                    if (y < 0 || y >= layer.data.length) continue;
                    if (x < 0 || x >= layer.data[0].length) continue;

                    int gid = layer.data[y][x];
                    if (gid == 0 || tiles[gid] == null || tiles[gid].image == null) continue;

                    double screenX = (x * Constant.TILESIZE) - player.x + Constant.SCREEN_WIDTH / 2.0;
                    double screenY = (y * Constant.TILESIZE) - player.y + Constant.SCREEN_HEIGHT / 2.0;

                    gc.drawImage(tiles[gid].image, screenX, screenY, Constant.TILESIZE, Constant.TILESIZE);
                }
            }
        }

        // Debug player info
        gc.setFill(Color.WHITE);
        gc.fillText("Player: (" + player.x + ", " + player.y + ")", 10, 20);
    }
    // In MapLoader.java
    public boolean checkCollision(double x, double y, Polygon solidArea) {
        // Create player bounding box (based on position)
        Rectangle playerRect = new Rectangle(
                (int) (x + 8),  // offset (same as solidAreaX)
                (int) (y + 16), // offset (same as solidAreaY)
                (int) (Constant.TILESIZE * Constant.CHAR_SCALE - 16),
                (int) (Constant.TILESIZE * Constant.CHAR_SCALE - 16)
        );

        int minTileX = playerRect.x / Constant.TILESIZE;
        int maxTileX = (playerRect.x + playerRect.width) / Constant.TILESIZE;
        int minTileY = playerRect.y / Constant.TILESIZE;
        int maxTileY = (playerRect.y + playerRect.height) / Constant.TILESIZE;

        for (var layer : layers) {
            for (int ty = minTileY; ty <= maxTileY; ty++) {
                for (int tx = minTileX; tx <= maxTileX; tx++) {
                    // Bounds safety
                    if (ty < 0 || ty >= layer.data.length || tx < 0 || tx >= layer.data[0].length)
                        continue;

                    int tileId = layer.data[ty][tx];
                    if (tileId == 0) continue;

                    var tile = tiles[tileId];
                    if (tile == null || !tile.collision || tile.solidPolygon == null)
                        continue;

                    // Build world-space polygon
                    Polygon poly = new Polygon(tile.solidPolygon.xpoints, tile.solidPolygon.ypoints, tile.solidPolygon.npoints);
                    poly.translate(tx * Constant.TILESIZE, ty * Constant.TILESIZE);

                    // Check intersection
                    Area tileArea = new Area(poly);
                    Area playerArea = new Area(playerRect);
                    tileArea.intersect(playerArea);

                    if (!tileArea.isEmpty()) {
                        return true; // Collision!
                    }
                }
            }
        }

        return false; // No collision
    }


    private javafx.scene.shape.Polygon toFxPolygon(Polygon awtPoly) {
        javafx.scene.shape.Polygon fx = new javafx.scene.shape.Polygon();
        for (int i = 0; i < awtPoly.npoints; i++) {
            fx.getPoints().addAll(
                    (double) awtPoly.xpoints[i],
                    (double) awtPoly.ypoints[i]
            );
        }
        return fx;
    }

}
