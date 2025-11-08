// Pham Ngoc Duc - Lớp 23JIT - Trường VKU - MSSV: 23IT059
package com.tank2d.client.core;

import com.tank2d.client.entity.Entity;
import com.tank2d.client.entity.Player;
import com.tank2d.client.map.MapLoader;
import com.tank2d.shared.Constant;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;
public class PlayPanel extends Pane implements Runnable {
    private final Canvas canvas;
    private final GraphicsContext gc;

    private final List<Entity> entities;
    private final Player player;
    private AnimationTimer gameLoop;
    private MapLoader mapLoader;

    private boolean isHost = false;

    public PlayPanel(int mapId, Player player, List<Entity> entities) {
        this.canvas = new Canvas(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        this.entities = entities;
        this.player = player;
        this.entities.add(player);
        this.mapLoader = new MapLoader(mapId);

        setupControls();
        setFocusTraversable(true);
        requestFocus();
    }

    private void setupControls() {
    }

    public void setHost(boolean value) {
        this.isHost = value;
    }

    public Player getPlayer() { return player; }
    public List<Entity> getEntities() { return entities; }

    // Example hook to receive update from network
    public void applyRemoteState(double x, double y, double angle) {
        player.setX(x);
        player.setY(y);
        // you can later add smooth interpolation here
    }

    @Override
    public void run() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw();
            }
        };
        gameLoop.start();
    }

    private void update() {
        for (Entity e : entities) e.update();
    }

    private void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        mapLoader.draw(gc, this.player);
        for (Entity e : entities) e.draw(gc);
    }
}
