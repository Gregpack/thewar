package ru.gregpack.thewar.view;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.gregpack.thewar.ClientDelegate;
import ru.gregpack.thewar.model.entities.basic.Coordinate;
import ru.gregpack.thewar.network.messages.dto.GameStateDto;
import ru.gregpack.thewar.network.messages.dto.PlayerDto;
import ru.gregpack.thewar.view.canvas.GameStatusPanel;
import ru.gregpack.thewar.view.canvas.PannableCanvas;
import ru.gregpack.thewar.view.canvas.SceneGestures;

import java.util.ArrayList;
import java.util.List;


public class Visualiser extends Application {

    private static ClientDelegate clientDelegate;
    private VisualiserCanvas canvas;
    private GameStatusPanel gameStatusPanel;

    public static void start(String[] args) {
        launch(args);
    }

    public static void setClientDelegate(ClientDelegate gameStateProducer) {
        Visualiser.clientDelegate = gameStateProducer;
    }

    @Override
    public void start(Stage primaryStage) {
        launchFrame(primaryStage);
    }

    private void launchFrame(Stage primaryStage) {
        Pane root = new Pane();
        List<String> params = getParameters().getRaw();
        if (params.size() < 5) {
            System.err.println("No length or height!");
        }
        int width = Integer.parseInt(params.get(0));
        int height = Integer.parseInt(params.get(1));
        String goldToWin = params.get(2);
        int baseLength = Integer.parseInt(params.get(3));
        int baseHeight = Integer.parseInt(params.get(4));
        int tickrate = Integer.parseInt(params.get(5));
        List<Coordinate> bases = new ArrayList<>();
        for (int i = 6; i < params.size(); i++) {
            String[] param = params.get(i).split(";");
            bases.add(new Coordinate(Integer.parseInt(param[0]), Integer.parseInt(param[1])));
        }

        canvas = new VisualiserCanvas(width, height, baseLength, baseHeight, bases, tickrate);
        canvas.initCanvas();

        gameStatusPanel = new GameStatusPanel(goldToWin);

        VBox vbox = new VBox();
        vbox.getChildren().add(gameStatusPanel.getPanel());
        vbox.getChildren().add(canvas.getPannableCanvas());
        root.getChildren().add(vbox);

        Scene scene = new Scene(root, canvas.getWidth(), canvas.getHeight() + GameStatusPanel.HEIGHT, Color.WHITESMOKE);
        setupCanvas(scene, canvas.getPannableCanvas());
        clientDelegate.startListening(this::drawGameStateThreadSafe, this::finishViewer, this::endGame);

        primaryStage.setTitle("TheWar v1.0");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> finishViewer());
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                canvas.drawGameFrame();
            }
        };
        timer.start();
        primaryStage.show();
    }

    private void drawGameStateThreadSafe(GameStateDto gameStateDto) {
        Platform.runLater(() -> {
            canvas.updateGameState(gameStateDto);
            gameStatusPanel.drawGameState(gameStateDto);
        });
    }

    private void endGame(PlayerDto winner) {
        Platform.runLater(() -> {
            gameStatusPanel.drawWinner(winner);
        });

    }

    private void finishViewer() {
        Platform.exit();
        clientDelegate.shutdown();
    }

    private void setupCanvas(Scene scene, PannableCanvas canvas) {
        SceneGestures sceneGestures = new SceneGestures(canvas);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures::onMousePressedEventHandler);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures::onMouseDraggedEventHandler);
        scene.addEventFilter(ScrollEvent.ANY, sceneGestures::onScrollEventHandle);
    }

}
