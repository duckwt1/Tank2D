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
    private Image bodyImage;
    private Image gunImage;

    // Góc thân (hiện tại) và góc mục tiêu (khi nhấn phím)
    private double bodyAngle = 0;
    private double targetAngle = 0;
    private double gunAngle = 0;
    private boolean backward; // trạng thái phím lùi


    // Trạng thái phím
    private boolean up, down, left, right;

    // Tốc độ di chuyển và tốc độ xoay
    private final double moveSpeed = 2.5;
    private final double rotateSpeed = 2.0; // độ/khung hình

    // Pivot của nòng
    private double gunPivotX;
    private double gunPivotY;

    public Player(double x, double y, Polygon solidArea, double speed, String playerName) {
        super(x, y, solidArea, speed);
        this.playerName = playerName;
        getImages();
    }

    @Override
    public void getImages() {
        try {
            bodyImage = new Image("file:res/tank/tank2.png");
            gunImage = new Image("file:res/gun/gun1.png");

            gunPivotX = gunImage.getWidth() / 4; // vị trí xoay nòng
            gunPivotY = gunImage.getHeight() / 2;

            System.out.println(" Loaded Player images successfully.");
        } catch (Exception e) {
            System.out.println(" Error loading images for Player:");
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        double dx = 0, dy = 0;

        if (up) dy -= moveSpeed;
        if (down) dy += moveSpeed;
        if (left) dx -= moveSpeed;
        if (right) dx += moveSpeed;

        if (dx != 0 || dy != 0) {
            // Góc đích (mục tiêu) theo hướng phím nhấn
            targetAngle = Math.toDegrees(Math.atan2(dy, dx));
            double diff = normalizeAngle(targetAngle - bodyAngle);

            if (Math.abs(diff) < rotateSpeed) {
                bodyAngle = targetAngle;
            } else {
                bodyAngle += Math.signum(diff) * rotateSpeed;
            }

            // Tiến theo hướng thân
            double rad = Math.toRadians(bodyAngle);
            x += Math.cos(rad) * moveSpeed;
            y += Math.sin(rad) * moveSpeed;
        } else if (backward) {
            // Nếu nhấn SPACE → lùi
            double rad = Math.toRadians(bodyAngle);
            x -= Math.cos(rad) * moveSpeed;
            y -= Math.sin(rad) * moveSpeed;
        }

    }

    @Override
    public void draw(GraphicsContext gc) {
        if (bodyImage == null || gunImage == null) return;

        double centerX = Constant.SCREEN_WIDTH / 2.0;
        double centerY = Constant.SCREEN_HEIGHT / 2.0;

        double bodyW = bodyImage.getWidth();
        double bodyH = bodyImage.getHeight();

        // --- Vẽ thân (xoay mượt theo hướng di chuyển) ---
        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(bodyAngle);
        gc.drawImage(bodyImage, -bodyW / 2, -bodyH / 2);
        gc.restore();

        // --- Vẽ nòng ---
        gc.save();
        Affine transform = new Affine();
        transform.appendTranslation(centerX, centerY);
        transform.appendRotation(Math.toDegrees(gunAngle));
        gc.setTransform(transform);
        gc.drawImage(gunImage, -gunPivotX, -gunPivotY);
        gc.restore();

        // --- Debug ---
        gc.setFill(Color.WHITE);
        gc.fillText(playerName, centerX - 20, centerY - bodyH / 2 - 5);
        gc.setFill(Color.RED);
        gc.fillText(String.format("x: %.1f, y: %.1f  angle: %.1f°", x, y, bodyAngle), 10, 20);
    }

    /** Giúp giữ góc trong khoảng [-180, 180] */
    private double normalizeAngle(double angle) {
        angle %= 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }

    /** Xoay nòng theo chuột */
    public void onMouseMoved(MouseEvent e) {
        double centerX = Constant.SCREEN_WIDTH / 2.0;
        double centerY = Constant.SCREEN_HEIGHT / 2.0;
        double dx = e.getX() - centerX;
        double dy = e.getY() - centerY;
        gunAngle = Math.atan2(dy, dx);
    }

    // ---- Phím điều khiển ----
    public void setUp(boolean value) { up = value;
        System.out.println("hello");}
    public void setDown(boolean value) { down = value; }
    public void setLeft(boolean value) { left = value; }
    public void setRight(boolean value) { right = value; }
    public void setBackward(boolean value) { backward = value; }
    public void setX(double x)
    {
        this.x = x;
    }
    public double getGunAngle() {
        return gunAngle;
    }

    public void setGunAngle(double angle) {
        this.gunAngle = angle;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getX() {
        return this.x;
    }
    public double getY()
    {
        return this.y;
    }

    public double getBodyAngle() {
        return bodyAngle;
    }
}
