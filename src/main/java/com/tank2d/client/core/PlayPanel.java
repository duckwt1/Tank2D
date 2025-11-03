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

    private final List<Entity> entities = new ArrayList<>();
    private Player player;
    private AnimationTimer gameLoop;
    private MapLoader mapLoader;

    public PlayPanel() {
        this.canvas = new Canvas(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        init();
        setupControls();

        // Bắt focus mặc định
        setFocusTraversable(true);
        requestFocus();

        // Khi click chuột → focus lại để nhận phím
        canvas.setOnMouseClicked(e -> requestFocus());
    }

    private void init() {
        Polygon solid = new Polygon();
        player = new Player(0, 0, solid, 3, "Pham Ngoc Duc");
        entities.add(player);
        mapLoader = new MapLoader(1); //map id
    }

    /** Lắng nghe phím và chuột */
    private void setupControls() {
        // Chuột: xoay nòng
        canvas.setOnMouseMoved((MouseEvent e) -> player.onMouseMoved(e));

        // Bàn phím: di chuyển
        setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            switch (code) {
                case W, UP -> player.setUp(true);
                case S, DOWN -> player.setDown(true);
                case A, LEFT -> player.setLeft(true);
                case D, RIGHT -> player.setRight(true);
            }
        });

        setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            switch (code) {
                case W, UP -> player.setUp(false);
                case S, DOWN -> player.setDown(false);
                case A, LEFT -> player.setLeft(false);
                case D, RIGHT -> player.setRight(false);
            }
        });
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

    public void update() {
        for (Entity e : entities) e.update();
    }

    public void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // clear old frame first

        mapLoader.draw(gc, player); // draw the map
        for (Entity e : entities) e.draw(gc); // draw tanks, bullets, etc.
    }


    public void stop() {
        if (gameLoop != null) gameLoop.stop();
    }
}
