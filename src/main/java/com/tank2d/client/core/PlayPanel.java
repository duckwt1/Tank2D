// Pham Ngoc Duc - Lớp 23JIT - Trường VKU - MSSV: 23IT059
package com.tank2d.client.core;

import com.tank2d.client.entity.Entity;
import com.tank2d.client.entity.Player;
import com.tank2d.client.map.MapLoader;
import com.tank2d.shared.Constant;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

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
        // mouse and key handlers (delegate to your methods)
        setOnMouseMoved(this::onMouseMoved);

        // Try to handle keys on the Panel itself
        setOnKeyPressed(e -> {
            System.out.println("KeyPressed on PlayPanel: " + e.getCode());
            onKeyPressed(e);
        });
        setOnKeyReleased(e -> {
            System.out.println("KeyReleased on PlayPanel: " + e.getCode());
            onKeyReleased(e);
        });

        // Make sure PlayPanel is focusable and request focus at right time
        setFocusTraversable(true);
        setOnMouseClicked(e -> {
            requestFocus(); // clicking will give focus
            System.out.println("PlayPanel clicked -> requestFocus()");
        });

        // When scene/window shows, request focus automatically
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.showingProperty().addListener((o3, wasShowing, isShowing) -> {
                            if (isShowing) {
                                javafx.application.Platform.runLater(() -> {
                                    requestFocus();
                                    System.out.println("Requested focus on PlayPanel after show()");
                                });
                            }

                        });
                    }
                });

                // also attach scene-level handlers as fallback
                newScene.setOnKeyPressed(e -> {
                    // scene-level receives even if pane isn't focused
                    System.out.println("Scene key pressed: " + e.getCode());
                    onKeyPressed(e);
                });
                newScene.setOnKeyReleased(e -> {
                    onKeyReleased(e);
                });
            }
        });

        // debug focus changes
        focusedProperty().addListener((obs, oldV, newV) -> System.out.println("PlayPanel focus = " + newV));
    }


    private void onKeyPressed(KeyEvent e) {
        KeyCode code = e.getCode();
        switch (code) {
            case W -> player.setUp(true);
            case S -> player.setDown(true);
            case A -> player.setLeft(true);
            case D -> player.setRight(true);
            case SPACE -> player.setBackward(true);
        }
    }

    private void onKeyReleased(KeyEvent e) {
        KeyCode code = e.getCode();
        switch (code) {
            case W -> player.setUp(false);
            case S -> player.setDown(false);
            case A -> player.setLeft(false);
            case D -> player.setRight(false);
            case SPACE -> player.setBackward(false);
        }
    }

    private void onMouseMoved(MouseEvent e) {
        player.onMouseMoved(e);
    }

    public void setHost(boolean value) {
        this.isHost = value;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    // Called when remote data received from mini server
    public void applyRemoteState(double x, double y, double angle) {
        player.setX(x);
        player.setY(y);
        // we could smooth interpolate here later
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
