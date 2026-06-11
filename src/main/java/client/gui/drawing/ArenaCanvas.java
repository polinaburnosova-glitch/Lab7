package client.gui.drawing;

import common.model.HumanBeing;
import common.model.User;
import javafx.animation.ScaleTransition;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class ArenaCanvas extends Canvas {

    private ObservableList<HumanBeing> data;
    private User currentUser;
    private Map<Long, Double> lastX;
    private Map<Long, Double> lastY;

    public ArenaCanvas(double width, double height, ObservableList<HumanBeing> data, User currentUser) {
        super(width, height);
        this.data = data;
        this.currentUser = currentUser;
        this.lastX = new HashMap<>();
        this.lastY = new HashMap<>();

        setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
        });

        redraw();
    }

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        drawGrid(gc);

        for (HumanBeing human : data) {
            drawHuman(gc, human);
        }
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        double step = 50;
        for (double x = 0; x < getWidth(); x += step) {
            gc.strokeLine(x, 0, x, getHeight());
        }
        for (double y = 0; y < getHeight(); y += step) {
            gc.strokeLine(0, y, getWidth(), y);
        }
    }

    private void drawHuman(GraphicsContext gc, HumanBeing human) {
        double x = human.getCoordinates().getX() * 50;
        double y = human.getCoordinates().getY() * 50;

        lastX.put(human.getId(), x);
        lastY.put(human.getId(), y);

        Color color;
        if (human.getOwner().equals(currentUser.getUsername())) {
            color = Color.GREEN;
        } else if (isTop3(human)) {
            color = Color.GOLD;
        } else {
            color = Color.RED;
        }
        gc.setFill(color);

        gc.fillRoundRect(x, y, 40, 40, 10, 10);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, 40, 40, 10, 10);

        gc.setFill(Color.BLACK);
        gc.fillText(human.getName(), x + 5, y + 15);

        gc.fillText("ID:" + human.getId(), x + 5, y + 28);

        gc.fillText(String.valueOf((int) human.getImpactSpeed()), x + 25, y + 38);
    }

    private boolean isTop3(HumanBeing human) {
        return data.stream()
                .sorted((a, b) -> Float.compare(b.getImpactSpeed(), a.getImpactSpeed()))
                .limit(3)
                .anyMatch(top -> top.getId().equals(human.getId()));
    }

    public Long getObjectAt(double mouseX, double mouseY) {
        for (Map.Entry<Long, Double> entry : lastX.entrySet()) {
            Long id = entry.getKey();
            Double x = entry.getValue();
            Double y = lastY.get(id);

            if (mouseX >= x && mouseX <= x + 40 && mouseY >= y && mouseY <= y + 40) {
                return id;
            }
        }
        return null;
    }

    public void animateAdd(HumanBeing human) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), this);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.1);
        st.setToY(1.1);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        redraw();
    }
}
