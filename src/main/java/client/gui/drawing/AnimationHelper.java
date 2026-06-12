package client.gui.drawing;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AnimationHelper {

    /**
     * Пульсация при добавлении нового объекта
     */
    public static void animateAdd(Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), node);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(1.15);
        scale.setToY(1.15);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();
    }

    /**
     * Мигание при обновлении объекта
     */
    public static void animateUpdate(Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(4);
        scale.play();
    }

    /**
     * Эффект удара при битве
     */
    public static void animateHit(Canvas canvas, double x, double y, Runnable onFinished) {
        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.setFill(Color.rgb(255, 255, 255, 0.7));
                    gc.fillRect(x, y, 50, 50);
                }),
                new KeyFrame(Duration.millis(100), e -> {
                    if (onFinished != null) {
                        onFinished.run();
                    }
                })
        );
        flash.setCycleCount(3);
        flash.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        flash.play();

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), canvas);
        shake.setFromX(0);
        shake.setToX(5);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);
        shake.setOnFinished(e -> canvas.setTranslateX(0));
        shake.play();
    }

    /**
     * Плавное появление объекта (Fade-in)
     */
    public static void animateFadeIn(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(500), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Плавное исчезновение объекта (Fade-out) при удалении
     */
    public static void animateFadeOut(Node node, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            node.setOpacity(1);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        fade.play();
    }

    /**
     * Эффект "прилёта" объекта на арену
     */
    public static void animateFlyIn(Node node, double startX, double startY, double endX, double endY) {
        node.setTranslateX(startX);
        node.setTranslateY(startY);
        node.setOpacity(0);

        TranslateTransition move = new TranslateTransition(Duration.millis(500), node);
        move.setFromX(startX);
        move.setFromY(startY);
        move.setToX(endX);
        move.setToY(endY);

        FadeTransition fade = new FadeTransition(Duration.millis(500), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(move, fade);
        parallel.setOnFinished(e -> {
            node.setTranslateX(0);
            node.setTranslateY(0);
        });
        parallel.play();
    }

    /**
     * Вращение при наведении (hover)
     */
    public static void animateHover(Node node, boolean enter) {
        RotateTransition rotate = new RotateTransition(Duration.millis(200), node);
        if (enter) {
            rotate.setByAngle(10);
        } else {
            rotate.setByAngle(-10);
        }
        rotate.play();
    }

    /**
     * Эффект пульсации для выделения топ-3 героев
     */
    public static void animatePulse(Node node) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    ScaleTransition scale = new ScaleTransition(Duration.millis(500), node);
                    scale.setFromX(1);
                    scale.setFromY(1);
                    scale.setToX(1.05);
                    scale.setToY(1.05);
                    scale.setAutoReverse(true);
                    scale.setCycleCount(Animation.INDEFINITE);
                    scale.play();
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Остановка пульсации
     */
    public static void stopPulse(Node node) {
        node.setScaleX(1);
        node.setScaleY(1);
    }
}