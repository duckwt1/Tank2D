package com.tank2d.client.utils;

import com.tank2d.client.entity.ShopItem;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ShopItemCell extends ListCell<ShopItem> {

    private HBox container = new HBox();
    private VBox textBox = new VBox();

    private Label lblName = new Label();
    private Label lblStats = new Label();
    private Label lblPrice = new Label();
    private Label lblStock = new Label();

    public ShopItemCell() {
        container.setSpacing(12);
        container.setPadding(new Insets(8));
        container.setStyle("-fx-background-color: #fdf6e3; -fx-background-radius: 6;");

        lblName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #5d4037;");
        lblStats.setStyle("-fx-text-fill: #795548;");
        lblPrice.setStyle("-fx-text-fill: #8d6e63;");
        lblStock.setStyle("-fx-text-fill: #6d4c41;");

        textBox.getChildren().addAll(lblName, lblStats, lblPrice, lblStock);
        container.getChildren().add(textBox);

        // Hover effect
        container.setOnMouseEntered(e ->
                container.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 6;"));
        container.setOnMouseExited(e ->
                container.setStyle("-fx-background-color: #fdf6e3; -fx-background-radius: 6;"));
    }

    @Override
    protected void updateItem(ShopItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        lblName.setText(item.name);

        lblStats.setText(
                String.format("HP: %.0f   MP: %.0f   SPD: %.0f   DMG: %.0f",
                        item.hp, item.mp, item.spd, item.dmg)
        );

        if (item.discount > 0) {
            lblPrice.setText(
                    String.format("Price: %d → %d (-%.0f%%)",
                            item.price, item.getFinalPrice(), item.discount * 100)
            );
        } else {
            lblPrice.setText("Price: " + item.price);
        }

        lblStock.setText("Stock: " + item.stock);

        setGraphic(container);
    }
}
