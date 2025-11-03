// Pham Ngoc Duc - Lớp 23JIT - Trường VKU - MSSV: 23IT059
package com.tank2d.client.entity;

import com.tank2d.shared.Constant;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Affine;

public class Player extends Entity {
    private final String playerName;
    private GraphicsContext gc;
    private Image bodyImage;
    private Image gunImage;

    private double gunAngle = 0;

    // Tâm màn hình (thân cố định ở giữa)
    private double screenCenterX;
    private double screenCenterY;

    // Pivot của nòng
    private double gunPivotX;
    private double gunPivotY;

    // Trạng thái phím
    private boolean up, down, left, right;

    public Player(double x, double y, Polygon solidArea, double speed, String playerName) {
        super(x, y, solidArea, speed);
        this.playerName = playerName;
        getImages();
        this.gc = gc;
    }

    @Override
    public void getImages() {
        try {
            bodyImage = new Image("file:res/tank/tank2.png");
            gunImage = new Image("file:res/gun/gun1.png");

            gunPivotX = 32;
            gunPivotY = gunImage.getHeight() / 2;

            System.out.println("✅ Loaded Player images successfully.");
        } catch (Exception e) {
            System.out.println("❌ Error loading images for Player:");
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        // Di chuyển theo phím
        double dx = 0, dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;
        move(dx, dy);

        // Thân luôn ở giữa màn hình
        screenCenterX = Constant.SCREEN_WIDTH / 2.0;
        screenCenterY = Constant.SCREEN_HEIGHT / 2.0;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (bodyImage == null || gunImage == null) return;

        double bodyW = bodyImage.getWidth();
        double bodyH = bodyImage.getHeight();

        // Vẽ thân ở giữa
        gc.drawImage(bodyImage, screenCenterX - bodyW / 2, screenCenterY - bodyH / 2);

        // --- Vẽ nòng ---
        gc.save();
        Affine transform = new Affine();
        transform.appendTranslation(screenCenterX, screenCenterY);
        transform.appendRotation(Math.toDegrees(gunAngle));
        gc.setTransform(transform);
        gc.drawImage(gunImage, -gunPivotX, -gunPivotY);
        gc.restore();

        // --- Tên + debug tọa độ ---
        gc.setFill(Color.BLACK);
        gc.fillText(playerName, screenCenterX - 20, screenCenterY - bodyH / 2 - 5);

        gc.setFill(Color.RED);
        gc.fillText(String.format("x: %.1f, y: %.1f", x, y), 10, 20);

    }

    /** Xoay nòng theo chuột */
    public void onMouseMoved(MouseEvent e) {
        double dx = e.getX() - screenCenterX;
        double dy = e.getY() - screenCenterY;
        gunAngle = Math.atan2(dy, dx);
    }

    /** Di chuyển player */
    public void move(double dx, double dy) {
        x += dx * speed;
        y += dy * speed;
    }

    // ---- Phím điều khiển ----
    public void setUp(boolean value) { up = value;
    }
    public void setDown(boolean value) { down = value; }
    public void setLeft(boolean value) { left = value; }
    public void setRight(boolean value) { right = value; }
}
