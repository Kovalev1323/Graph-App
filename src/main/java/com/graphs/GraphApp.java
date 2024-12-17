package com.graphs;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of the application for graph generation and cycle calculations.
 * The application uses JavaFX for displaying the graph and interacting with the user.
 */
public class GraphApp extends Application {

    // Window width and height
    private static final int WIDTH = 900;
    private static final int HEIGHT = 700;

    // Logger for event tracking
    private static final Logger logger = Logger.getLogger(GraphApp.class.getName());

    // UI elements
    private TextField nodeCountField;
    private TextField cycleCountField;
    private TextArea resultArea;

    private Button calculateButton;
    private Button resetButton;

    private int nodeCount; // Number of nodes in the graph
    private double scale = 1.0; // Graph scale
    private double translateX = 0; // X-axis translation
    private double translateY = 0; // Y-axis translation
    private double lastMouseX;
    private double lastMouseY;

    private GraphRenderer graphRenderer; // Graph renderer

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        // Setup logger for console output
        ConsoleHandler consoleHandler = new ConsoleHandler();
        logger.addHandler(consoleHandler);
        logger.setLevel(Level.ALL);

        launch(args);
    }

    /**
     * Initializes the graphical interface and the main window.
     *
     * @param primaryStage the primary stage of the application
     */
    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting the GraphApp...");

        primaryStage.setTitle("Graph App");

        BorderPane root = new BorderPane();
        root.setTop(createInputPane());
        root.setCenter(createCanvasContainer());
        root.setBottom(createResultPane());

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
    }

    /**
     * Creates a panel for entering graph parameters (number of nodes and cycles).
     *
     * @return VBox the panel with input fields
     */
    private VBox createInputPane() {
        VBox inputPane = new VBox(10);
        inputPane.setPadding(new Insets(10));
        inputPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        inputPane.getChildren().addAll(createTitleLabel(), createNodeBox(), createCycleBox(), createButtons());
        return inputPane;
    }

    /**
     * Creates a label with the title "Graph Parameters".
     *
     * @return Label the title label
     */
    private Node createTitleLabel() {
        Label title = new Label("Параметры графа");
        title.setFont(Font.font("Arial", 18));
        title.setTextFill(Color.DARKBLUE);
        return title;
    }

    /**
     * Creates a panel for entering the number of nodes in the graph.
     *
     * @return HBox the panel for node count input
     */
    private HBox createNodeBox() {
        HBox nodeBox = new HBox(10);
        Label nodeCountLabel = new Label("Количество узлов:");
        nodeCountField = new TextField();
        nodeCountField.setPrefWidth(60);
        nodeBox.getChildren().addAll(nodeCountLabel, nodeCountField);
        return nodeBox;
    }

    /**
     * Creates a panel for entering the number of cycles in the graph.
     *
     * @return HBox the panel for cycle count input
     */
    private HBox createCycleBox() {
        HBox cycleBox = new HBox(10);
        Label cycleCountLabel = new Label("Количество циклов:");
        cycleCountField = new TextField();
        cycleCountField.setPrefWidth(60);
        cycleBox.getChildren().addAll(cycleCountLabel, cycleCountField);
        return cycleBox;
    }

    /**
     * Creates a panel with buttons for generating the graph, calculating cycles, and resetting.
     *
     * @return HBox the panel with buttons
     */
    private HBox createButtons() {
        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(createGenerateButton(), createCalculateButton(), createResetButton());
        return buttons;
    }

    /**
     * Creates a button for generating the graph.
     *
     * @return Button the generate button
     */
    private Button createGenerateButton() {
        Button generateButton = new Button("Создать граф");
        generateButton.setOnAction(e -> generateGraph());
        return generateButton;
    }

    /**
     * Creates a button for calculating cycles in the graph.
     *
     * @return Button the calculate button
     */
    private Button createCalculateButton() {
        calculateButton = new Button("Посчитать циклы");
        calculateButton.setDisable(true);
        calculateButton.setOnAction(e -> calculateCycles());
        return calculateButton;
    }

    /**
     * Creates a button for resetting the graph.
     *
     * @return Button the reset button
     */
    private Button createResetButton() {
        resetButton = new Button("Сбросить");
        resetButton.setDisable(true);
        resetButton.setOnAction(e -> resetGraph());
        return resetButton;
    }

    /**
     * Creates a container for displaying the canvas (graph).
     *
     * @return Pane the container for the canvas
     */
    private Pane createCanvasContainer() {
        Pane canvasContainer = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT - 200);
        canvasContainer.getChildren().add(canvas);
        canvasContainer.setStyle("-fx-background-color: white; -fx-border-color: lightgray; -fx-border-width: 1");

        canvas.widthProperty().bind(canvasContainer.widthProperty());
        canvas.heightProperty().bind(canvasContainer.heightProperty());

        canvasContainer.setOnScroll(this::handleZoom);
        canvasContainer.setOnMousePressed(this::handleMousePressed);
        canvasContainer.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseClicked(this::handleNodeClick);

        graphRenderer = new GraphRenderer(canvas);
        return canvasContainer;
    }

    /**
     * Creates a panel to display the results of the calculation.
     *
     * @return VBox the panel with results
     */
    private VBox createResultPane() {
        VBox resultPane = new VBox(10);
        resultPane.setPadding(new Insets(10));
        resultPane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        Label resultTitle = new Label("Результаты");
        resultTitle.setFont(Font.font("Arial", 18));
        resultTitle.setTextFill(Color.DARKBLUE);

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(100);

        resultPane.getChildren().addAll(resultTitle, resultArea);
        return resultPane;
    }

    /**
     * Handles the event when a node is clicked on the graph.
     *
     * @param event the mouse click event
     */
    private void handleNodeClick(MouseEvent event) {
        graphRenderer.handleNodeClick(event, nodeCount, translateX, translateY, scale);
    }

    /**
     * Handles the zoom event of the graph using the mouse wheel.
     *
     * @param event the scroll event
     */
    private void handleZoom(ScrollEvent event) {
        scale *= (event.getDeltaY() > 0) ? 1.1 : 0.9;
        graphRenderer.drawGraph(nodeCount, scale, translateX, translateY);
    }

    /**
     * Handles the mouse press event for starting the graph dragging.
     *
     * @param event the mouse press event
     */
    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }

    /**
     * Handles the mouse drag event for moving the graph.
     *
     * @param event the mouse drag event
     */
    private void handleMouseDragged(MouseEvent event) {
        translateX += event.getX() - lastMouseX;
        translateY += event.getY() - lastMouseY;
        lastMouseX = event.getX();
        lastMouseY = event.getY();
        graphRenderer.drawGraph(nodeCount, scale, translateX, translateY);
    }

    /**
     * Generates the graph in the background thread based on the user's input.
     */
    private void generateGraph() {
        try {
            nodeCount = Integer.parseInt(nodeCountField.getText());
            int cycleCount = Integer.parseInt(cycleCountField.getText());

            if (nodeCount > 10000) {
                showError("Ошибка: Количество узлов не может превышать 10,000.");
                return;
            }

            if (nodeCount <= 1 || cycleCount < 0 || cycleCount > nodeCount) {
                showError("Недопустимые данные для создания графа.");
                return;
            }

            logger.info("Generating graph with " + nodeCount + " nodes and " + cycleCount + " cycles.");
            generateGraphInBackground(cycleCount);

        } catch (NumberFormatException e) {
            showError("Ошибка: Пожалуйста, введите допустимые числа.");
            logger.warning("Invalid input for node count or cycle count.");
        } catch (Exception e) {
            showError("Неизвестная ошибка: " + e.getMessage());
            logger.severe("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Displays an error message in the result area.
     *
     * @param message the error message
     */
    private void showError(String message) {
        resultArea.setText(message);
        resetGraph();
        disableButtons();
    }

    /**
     * Disables the buttons for calculating and resetting the graph.
     */
    private void disableButtons() {
        calculateButton.setDisable(true);
        resetButton.setDisable(true);
    }

    /**
     * Generates the graph in a background thread using a task.
     *
     * @param cycleCount the number of cycles in the graph
     */
    private void generateGraphInBackground(int cycleCount) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                graphRenderer.generateGraph(nodeCount, cycleCount);
                return null;
            }

            @Override
            protected void succeeded() {
                resultArea.setText("Граф успешно создан.");
                logger.info("Graph successfully generated.");
                enableButtons();
            }

            @Override
            protected void failed() {
                resultArea.setText("Ошибка создания графа.");
                logger.severe("Graph generation failed.");
                resetGraph();
                disableButtons();
            }
        };
        new Thread(task).start();
    }

    /**
     * Enables the buttons for calculating and resetting the graph after successful generation.
     */
    private void enableButtons() {
        calculateButton.setDisable(false);
        resetButton.setDisable(false);
    }

    /**
     * Calculates the number of cycles in the graph and displays the result.
     */
    private void calculateCycles() {
        if (nodeCount == 0) {
            resultArea.setText("Пожалуйста, сначала создайте граф.");
            return;
        }

        long startTime = System.nanoTime();
        try {
            int[][] adjacencyMatrix = graphRenderer.getAdjacencyMatrix();
            int expectedCycles = Integer.parseInt(cycleCountField.getText());

            int[][] powerMatrix = copyMatrix(adjacencyMatrix);
            int cycleCount = 0;

            for (int step = 1; step <= nodeCount; step++) {
                powerMatrix = GraphUtils.multiplyMatrices(powerMatrix, adjacencyMatrix);
                cycleCount = countCycles(powerMatrix);

                if (cycleCount == expectedCycles) {
                    showCycleCountResult(cycleCount, startTime);
                    return;
                }
            }

            resultArea.setText("Не удалось найти точное количество циклов (" + expectedCycles + ").");
        } catch (NumberFormatException e) {
            resultArea.setText("Ошибка: Пожалуйста, введите допустимое количество циклов.");
        } catch (Exception e) {
            resultArea.setText("Ошибка при расчете циклов: " + e.getMessage());
            logger.severe("Error calculating cycles: " + e.getMessage());
        }
    }

    /**
     * Copies the adjacency matrix for further calculations.
     *
     * @param originalMatrix the original adjacency matrix
     * @return a copy of the adjacency matrix
     */
    private int[][] copyMatrix(int[][] originalMatrix) {
        int[][] newMatrix = new int[nodeCount][nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            System.arraycopy(originalMatrix[i], 0, newMatrix[i], 0, nodeCount);
        }
        return newMatrix;
    }

    /**
     * Counts the number of cycles in the graph.
     *
     * @param matrix the adjacency matrix
     * @return the number of cycles
     */
    private int countCycles(int[][] matrix) {
        int cycleCount = 0;
        for (int i = 0; i < nodeCount; i++) {
            cycleCount += matrix[i][i];
        }
        return cycleCount;
    }

    /**
     * Displays the cycle count result and calculation time.
     *
     * @param cycleCount the number of cycles found
     * @param startTime  the start time of the calculation
     */
    private void showCycleCountResult(int cycleCount, long startTime) {
        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;
        double durationSec = durationMs / 1000.0;

        // Log the result
        logger.info("Cycle count: " + cycleCount);

        // Update the result text area
        resultArea.setText("Общее количество циклов: " + cycleCount + "\n" +
                "Время расчета: " + String.format("%.2f", durationMs) + " мс (" +
                String.format("%.2f", durationSec) + " c)");
    }

    /**
     * Resets the graph to its original state.
     */
    private void resetGraph() {
        try {
            translateX = 0;
            translateY = 0;
            scale = 1.0;
            graphRenderer.clearHighlight();
            graphRenderer.drawGraph(0, scale, translateX, translateY);
            logger.info("Graph reset successfully.");
        } catch (Exception e) {
            resultArea.setText("Ошибка при сбросе графа: " + e.getMessage());
            logger.severe("Error resetting graph: " + e.getMessage());
        }
    }
}
