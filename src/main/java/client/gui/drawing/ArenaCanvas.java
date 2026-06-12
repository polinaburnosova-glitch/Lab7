package client.gui.drawing;

import client.gui.localization.LocalizationManager;
import common.model.HumanBeing;
import common.model.User;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

public class ArenaCanvas extends Canvas {

    private static final Color[] OWNER_PALETTE = {
            Color.CORAL, Color.DODGERBLUE, Color.MEDIUMPURPLE, Color.ORANGE,
            Color.TEAL, Color.SLATEBLUE, Color.INDIANRED, Color.SEAGREEN,
            Color.DARKGOLDENROD, Color.STEELBLUE
    };

    private static final double OBJECT_SIZE = 50;

    private final ObservableList<HumanBeing> data;
    private final User currentUser;
    private final Map<Long, Double> lastX = new HashMap<>();
    private final Map<Long, Double> lastY = new HashMap<>();
    private final Map<String, Color> ownerColors = new HashMap<>();

    public ArenaCanvas(double width, double height, ObservableList<HumanBeing> data, User currentUser) {
        super(width, height);
        this.data = data;
        this.currentUser = currentUser;
        redraw();
    }

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        if (data.isEmpty()) {
            gc.setFont(new Font("Arial", 14));
            gc.setFill(Color.GRAY);
            gc.fillText(LocalizationManager.getString("arena.empty"), getWidth() / 2 - 60, getHeight() / 2);
            return;
        }

        double minX = data.stream().mapToDouble(h -> h.getCoordinates().getX()).min().orElse(0);
        double maxX = data.stream().mapToDouble(h -> h.getCoordinates().getX()).max().orElse(100);
        double minY = data.stream().mapToDouble(h -> h.getCoordinates().getY()).min().orElse(0);
        double maxY = data.stream().mapToDouble(h -> h.getCoordinates().getY()).max().orElse(100);

        double rangeX = Math.max(maxX - minX, 1);
        double rangeY = Math.max(maxY - minY, 1);
        double paddingX = rangeX * 0.1;
        double paddingY = rangeY * 0.1;

        double scaleX = (getWidth() - 100) / (rangeX + paddingX * 2);
        double scaleY = (getHeight() - 100) / (rangeY + paddingY * 2);
        double offsetX = 50 - (minX - paddingX) * scaleX;
        double offsetY = 50 - (minY - paddingY) * scaleY;

        drawGrid(gc, minX, maxX, minY, maxY);

        lastX.clear();
        lastY.clear();
        for (HumanBeing human : data) {
            double x = human.getCoordinates().getX() * scaleX + offsetX;
            double y = human.getCoordinates().getY() * scaleY + offsetY;
            drawHuman(gc, human, x, y);
        }
    }

    private void drawGrid(GraphicsContext gc, double minX, double maxX, double minY, double maxY) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        double step = 60;
        for (double x = 0; x < getWidth(); x += step) {
            gc.strokeLine(x, 0, x, getHeight());
        }
        for (double y = 0; y < getHeight(); y += step) {
            gc.strokeLine(0, y, getWidth(), y);
        }

        gc.setFont(new Font("Arial", 10));
        gc.setFill(Color.GRAY);
        gc.fillText("X: " + LocalizationManager.formatNumber(minX) + " — "
                + LocalizationManager.formatNumber(maxX), 10, getHeight() - 20);
        gc.fillText("Y: " + LocalizationManager.formatNumber(minY) + " — "
                + LocalizationManager.formatNumber(maxY), 10, getHeight() - 5);
    }

    private Color colorForOwner(String owner) {
        return ownerColors.computeIfAbsent(owner, key -> {
            int index = Math.floorMod(key.hashCode(), OWNER_PALETTE.length);
            return OWNER_PALETTE[index];
        });
    }

    private void drawHuman(GraphicsContext gc, HumanBeing human, double x, double y) {
        lastX.put(human.getId(), x);
        lastY.put(human.getId(), y);

        gc.setFill(colorForOwner(human.getOwner()));
        gc.fillRoundRect(x, y, OBJECT_SIZE, OBJECT_SIZE, 10, 10);

        boolean own = human.getOwner().equals(currentUser.getUsername());
        gc.setStroke(own ? Color.GOLD : Color.BLACK);
        gc.setLineWidth(own ? 2.5 : 1.5);
        gc.strokeRoundRect(x, y, OBJECT_SIZE, OBJECT_SIZE, 10, 10);

        gc.setFont(new Font("Arial", 10));
        gc.setFill(Color.BLACK);
        gc.fillText(human.getName(), x + 4, y + 14);
        gc.fillText("ID:" + human.getId(), x + 4, y + 28);
        gc.fillText("(" + LocalizationManager.formatNumber(human.getCoordinates().getX()) + ","
                + LocalizationManager.formatNumber(human.getCoordinates().getY()) + ")", x + 4, y + 42);
    }

    public Long getObjectAt(double mouseX, double mouseY) {
        for (Map.Entry<Long, Double> entry : lastX.entrySet()) {
            Long id = entry.getKey();
            double x = entry.getValue();
            double y = lastY.get(id);

            if (mouseX >= x && mouseX <= x + OBJECT_SIZE && mouseY >= y && mouseY <= y + OBJECT_SIZE) {
                return id;
            }
        }
        return null;
    }
}
