package com.graphs;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The GraphRenderer class is responsible for rendering graphs based on an adjacency matrix.
 * It supports different types of graph generation including single-cycle, cycle-free, and multiple-cycles.
 * The graph is drawn on a provided JavaFX Canvas and uses a TextArea for displaying error messages.
 */
public class GraphRenderer {

    private static final int NODE_RADIUS = 20;
    private static final double ARROW_HEAD_SIZE = 10;

    private static final Logger logger = Logger.getLogger(GraphRenderer.class.getName());

    private final Canvas canvas;
    private int[][] adjacencyMatrix;
    private final Set<Integer> highlightedNodes = new HashSet<>();
    private TextArea errorTextArea;

    /**
     * Constructs a GraphRenderer instance.
     *
     * @param canvas The canvas on which the graph will be drawn.
     */
    public GraphRenderer(Canvas canvas) {
        this.canvas = canvas;
        logger.info("GraphRenderer instance created.");
    }

    /**
     * Generates a graph based on the provided number of nodes and cycles.
     * The graph is drawn after generation.
     *
     * @param nodeCountInput  The number of nodes in the graph.
     * @param cycleCountInput The number of cycles in the graph.
     */
    public void generateGraph(int nodeCountInput, int cycleCountInput) {
        try {
            long nodeCount = Long.parseLong(String.valueOf(nodeCountInput));
            int cycleCount = Integer.parseInt(String.valueOf(cycleCountInput));

            if (nodeCount > Integer.MAX_VALUE) {
                handleError("Число узлов слишком велико для обработки.");
                return;
            }

            logger.log(Level.INFO, "Generating graph with {0} nodes and {1} cycles.", new Object[]{nodeCount, cycleCount});
            clearCanvas();
            clearHighlight();
            adjacencyMatrix = new int[(int) nodeCount][(int) nodeCount];

            if (cycleCount == 1) {
                generateSingleCycleGraph((int) nodeCount);
            } else if (cycleCount == 0) {
                generateGraphWithoutCycle((int) nodeCount);
            } else {
                generateMultipleCyclesGraph((int) nodeCount, cycleCount);
            }

            drawGraph((int) nodeCount, 1.0, 0, 0);
        } catch (NumberFormatException e) {
            handleError("Ошибка формата ввода: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating graph", e);
            handleError("Ошибка при генерации графа: " + e.getMessage());
        }
    }

    /**
     * Clears the canvas, removing any drawn content.
     */
    private void clearCanvas() {
        logger.info("Clearing canvas.");
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * Handles errors by logging the error message and setting it in the provided TextArea.
     *
     * @param errorMessage The error message to display.
     */
    private void handleError(String errorMessage) {
        logger.severe(errorMessage);
        if (errorTextArea != null) {
            errorTextArea.setText(errorMessage);
        }
    }

    /**
     * Generates a single-cycle graph where nodes form a cycle.
     *
     * @param nodeCount The number of nodes in the graph.
     */
    private void generateSingleCycleGraph(int nodeCount) {
        logger.info("Generating single-cycle graph.");
        if (nodeCount > 1) {
            for (int i = 1; i < nodeCount; i++) {
                adjacencyMatrix[i][0] = 1;
            }
            adjacencyMatrix[0][0] = 1;
        } else {
            adjacencyMatrix[0][0] = 0;
        }
    }

    /**
     * Generates a graph without any cycles, where nodes are connected linearly.
     *
     * @param nodeCount The number of nodes in the graph.
     */
    private void generateGraphWithoutCycle(int nodeCount) {
        logger.info("Generating graph without cycles.");
        for (int i = 1; i < nodeCount; i++) {
            adjacencyMatrix[i][0] = 1;
        }
        adjacencyMatrix[0][0] = 0;
    }

    /**
     * Generates a graph with multiple cycles.
     *
     * @param nodeCount  The number of nodes in the graph.
     * @param cycleCount The number of cycles to include in the graph.
     */
    private void generateMultipleCyclesGraph(int nodeCount, int cycleCount) {
        logger.info("Generating multiple-cycles graph.");
        Set<Integer> usedNodes = new HashSet<>();
        createCycleEdges(nodeCount, cycleCount, usedNodes);
        addEdgesToNodeZero(nodeCount, usedNodes);
    }

    /**
     * Creates the edges that form the cycles in the graph.
     *
     * @param nodeCount  The number of nodes in the graph.
     * @param cycleCount The number of cycles to create.
     * @param usedNodes  A set of nodes that are part of the cycles.
     */
    private void createCycleEdges(int nodeCount, int cycleCount, Set<Integer> usedNodes) {
        logger.log(Level.INFO, "Creating cycle edges: {0} cycles.", cycleCount);
        for (int i = 0; i < cycleCount; i++) {
            int nextNode = (i + 1) % cycleCount;
            adjacencyMatrix[i][nextNode] = 1;
            usedNodes.add(i);
        }
    }

    /**
     * Adds edges from node 0 to the unused nodes.
     *
     * @param nodeCount The number of nodes in the graph.
     * @param usedNodes A set of nodes that are part of the cycles.
     */
    private void addEdgesToNodeZero(int nodeCount, Set<Integer> usedNodes) {
        logger.info("Adding edges to node zero.");
        for (int i = 0; i < nodeCount; i++) {
            if (!usedNodes.contains(i)) {
                adjacencyMatrix[i][0] = 1;
            }
        }
    }

    /**
     * Draws the graph on the canvas based on the adjacency matrix.
     *
     * @param nodeCount  The number of nodes in the graph.
     * @param scale      The scaling factor for the graph.
     * @param translateX The translation offset in the X direction.
     * @param translateY The translation offset in the Y direction.
     */
    public void drawGraph(int nodeCount, double scale, double translateX, double translateY) {
        System.out.println("Adjacency matrix: " + Arrays.deepToString(this.adjacencyMatrix));
        logger.log(Level.INFO, "Drawing graph with {0} nodes.", nodeCount);
        if (this.adjacencyMatrix == null) {
            System.err.println("Error: adjacencyMatrix is null!");
            return;
        }
        try {
            if (nodeCount > adjacencyMatrix.length) {
                throw new IllegalArgumentException("Число узлов превышает размер матрицы смежности.");
            }

            clearCanvas();
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.save();
            setupGraphicsContext(gc, translateX, translateY, scale);

            double centerX = canvas.getWidth() / 2;
            double centerY = canvas.getHeight() / 2;
            double radius = calculateRadius(nodeCount);
            double angleStep = 2 * Math.PI / nodeCount;

            Point2D[] positions = drawNodes(gc, nodeCount, centerX, centerY, radius, angleStep);
            drawEdges(gc, nodeCount, positions);
            gc.restore();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error drawing graph", e);
            handleError("Ошибка при рисовании графа: " + e.getMessage());
        }
    }

    /**
     * Sets up the graphics context with translation and scaling.
     *
     * @param gc         The graphics context to set up.
     * @param translateX The translation offset in the X direction.
     * @param translateY The translation offset in the Y direction.
     * @param scale      The scaling factor for the graph.
     */
    private void setupGraphicsContext(GraphicsContext gc, double translateX, double translateY, double scale) {
        logger.log(Level.INFO, "Setting up graphics context with scale {0} and translation ({1}, {2}).",
                new Object[]{scale, translateX, translateY});
        gc.translate(translateX, translateY);
        gc.scale(scale, scale);
    }

    /**
     * Calculates the radius for positioning the nodes based on the number of nodes.
     *
     * @param nodeCount The number of nodes in the graph.
     * @return The calculated radius.
     */
    private double calculateRadius(int nodeCount) {
        double adjustedNodeRadius = Math.max(5, NODE_RADIUS - Math.max(0, (nodeCount - 20) / 10.0));
        double radius = Math.max(200, (canvas.getWidth() / 2) - adjustedNodeRadius - 20 + nodeCount * 5);
        logger.log(Level.INFO, "Calculated graph radius: {0}.", radius);
        return radius;
    }

    /**
     * Draws the nodes of the graph on the canvas.
     *
     * @param gc        The graphics context used for drawing.
     * @param nodeCount The number of nodes in the graph.
     * @param centerX   The center X-coordinate for positioning the nodes.
     * @param centerY   The center Y-coordinate for positioning the nodes.
     * @param radius    The radius of the circle used to position the nodes.
     * @param angleStep The angle step for positioning each node.
     * @return An array of Point2D objects representing the positions of the nodes.
     */
    private Point2D[] drawNodes(GraphicsContext gc, int nodeCount, double centerX, double centerY, double radius, double angleStep) {
        logger.info("Drawing nodes.");
        Point2D[] positions = new Point2D[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            double x = centerX + radius * Math.cos(i * angleStep);
            double y = centerY + radius * Math.sin(i * angleStep);
            positions[i] = new Point2D(x, y);
            drawNode(gc, x, y, i);
        }
        return positions;
    }

    /**
     * Draws a single node on the canvas at the specified position.
     *
     * @param gc        The graphics context used for drawing.
     * @param x         The X-coordinate of the node.
     * @param y         The Y-coordinate of the node.
     * @param nodeIndex The index of the node.
     */
    private void drawNode(GraphicsContext gc, double x, double y, int nodeIndex) {
        logger.log(Level.INFO, "Drawing node {0} at position ({1}, {2}).", new Object[]{nodeIndex, x, y});
        if (nodeIndex < 0 || nodeIndex >= adjacencyMatrix.length) {
            logger.warning("Invalid nodeIndex: " + nodeIndex);
            return;
        }

        gc.setFill(Color.DARKORANGE);
        if (adjacencyMatrix[nodeIndex][nodeIndex] == 1 && adjacencyMatrix.length > 1) {
            gc.setFill(Color.RED);
        }

        gc.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(12));
        gc.fillText(String.valueOf(nodeIndex), x - 5, y + 5);
    }

    /**
     * Draws the edges of the graph, iterating over all pairs of nodes.
     * If there is an edge between two nodes, it will invoke the `drawEdge` method to render the edge.
     *
     * @param gc        The GraphicsContext to draw the graph.
     * @param nodeCount The number of nodes in the graph.
     * @param positions An array of Point2D objects representing the positions of the nodes on the canvas.
     */
    private void drawEdges(GraphicsContext gc, int nodeCount, Point2D[] positions) {
        logger.info("Drawing edges.");
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < nodeCount; j++) {
                if (adjacencyMatrix[i][j] == 1) {
                    drawEdge(gc, i, j, positions);
                }
            }
        }
    }

    /**
     * Draws an edge between two nodes, potentially with an arrowhead.
     * If the edge is a loop (i.e., connects a node to itself), a loop will be drawn instead of an arrow.
     *
     * @param gc        The GraphicsContext to draw the edge.
     * @param i         The index of the starting node.
     * @param j         The index of the ending node.
     * @param positions An array of Point2D objects representing the positions of the nodes on the canvas.
     */
    private void drawEdge(GraphicsContext gc, int i, int j, Point2D[] positions) {
        double x1 = positions[i].getX();
        double y1 = positions[i].getY();
        double x2 = positions[j].getX();
        double y2 = positions[j].getY();

        if (i == j) {
            drawLoop(gc, x1, y1);
        } else {
            setEdgeStyle(gc, i);
            drawArrow(gc, x1, y1, x2, y2);
        }
    }

    /**
     * Sets the style for the edge, such as color, based on whether the node is highlighted.
     *
     * @param gc The GraphicsContext to apply the stroke and fill styles.
     * @param i  The index of the node, used to determine if it is highlighted.
     */
    private void setEdgeStyle(GraphicsContext gc, int i) {
        if (highlightedNodes.contains(i)) {
            gc.setStroke(Color.ORANGE);
            gc.setFill(Color.ORANGE);
        } else {
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);
        }
    }

    /**
     * Draws an arrow from one node to another. The arrow is positioned based on the direction of the edge.
     *
     * @param gc The GraphicsContext to draw the arrow.
     * @param x1 The x-coordinate of the starting node.
     * @param y1 The y-coordinate of the starting node.
     * @param x2 The x-coordinate of the ending node.
     * @param y2 The y-coordinate of the ending node.
     */
    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);
        double nx = dx / length;
        double ny = dy / length;

        double startX = x1 + nx * NODE_RADIUS;
        double startY = y1 + ny * NODE_RADIUS;
        double endX = x2 - nx * NODE_RADIUS;
        double endY = y2 - ny * NODE_RADIUS;

        gc.strokeLine(startX, startY, endX, endY);
        drawArrowhead(gc, endX, endY, nx, ny);
    }

    /**
     * Draws an arrowhead at the end of an edge to indicate the direction.
     *
     * @param gc   The GraphicsContext to draw the arrowhead.
     * @param endX The x-coordinate of the end of the arrow.
     * @param endY The y-coordinate of the end of the arrow.
     * @param nx   The normalized direction vector's x component.
     * @param ny   The normalized direction vector's y component.
     */
    private void drawArrowhead(GraphicsContext gc, double endX, double endY, double nx, double ny) {
        double arrowX1 = endX - ARROW_HEAD_SIZE * (nx + ny);
        double arrowY1 = endY - ARROW_HEAD_SIZE * (ny - nx);
        double arrowX2 = endX - ARROW_HEAD_SIZE * (nx - ny);
        double arrowY2 = endY - ARROW_HEAD_SIZE * (ny + nx);

        gc.fillPolygon(new double[]{endX, arrowX1, arrowX2}, new double[]{endY, arrowY1, arrowY2}, 3);
    }

    /**
     * Draws a loop (edge that connects a node to itself) at a given position.
     *
     * @param gc The GraphicsContext to draw the loop.
     * @param x  The x-coordinate of the node.
     * @param y  The y-coordinate of the node.
     */
    private void drawLoop(GraphicsContext gc, double x, double y) {
        double loopRadius = NODE_RADIUS * 2.5;
        gc.strokeArc(x, y - loopRadius, loopRadius * 2, loopRadius * 2, 0, 360, javafx.scene.shape.ArcType.OPEN);

        double arrowAngle = Math.PI / 2;
        double arrowX = x + loopRadius * Math.cos(arrowAngle);
        double arrowY = y - loopRadius + loopRadius * Math.sin(arrowAngle);

        drawArrowhead(gc, arrowX, arrowY, 0, 1);
    }

    /**
     * Handles mouse click events on the canvas. If the click is on a node, it toggles the highlight for that node.
     *
     * @param event      The MouseEvent triggered by the click.
     * @param nodeCount  The number of nodes in the graph.
     * @param translateX The translation of the graph in the x-direction.
     * @param translateY The translation of the graph in the y-direction.
     * @param scale      The scaling factor of the graph.
     */
    public void handleNodeClick(MouseEvent event, int nodeCount, double translateX, double translateY, double scale) {
        logger.info("Handling node click event.");
        try {
            double mouseX = event.getX() / scale - translateX / scale;
            double mouseY = event.getY() / scale - translateY / scale;

            double centerX = canvas.getWidth() / 2;
            double centerY = canvas.getHeight() / 2;
            double radius = calculateRadius(nodeCount);
            double angleStep = 2 * Math.PI / nodeCount;

            for (int i = 0; i < nodeCount; i++) {
                double x = centerX + radius * Math.cos(i * angleStep);
                double y = centerY + radius * Math.sin(i * angleStep);

                if (Math.hypot(mouseX - x, mouseY - y) <= NODE_RADIUS) {
                    toggleNodeHighlight(i);
                    drawGraph(nodeCount, scale, translateX, translateY);
                    return;
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling node click", e);
            handleError("Ошибка при обработке клика по узлу: " + e.getMessage());
        }
    }

    /**
     * Toggles the highlight status of a node (add to highlighted set or remove from it).
     *
     * @param nodeIndex The index of the node to toggle highlight.
     */
    private void toggleNodeHighlight(int nodeIndex) {
        logger.log(Level.INFO, "Toggling highlight for node {0}.", nodeIndex);
        if (highlightedNodes.contains(nodeIndex)) {
            highlightedNodes.remove(nodeIndex);
        } else {
            highlightedNodes.add(nodeIndex);
        }
    }

    /**
     * Clears all highlighted nodes.
     */
    public void clearHighlight() {
        logger.info("Clearing all highlights.");
        highlightedNodes.clear();
    }

    /**
     * Returns the adjacency matrix representing the graph.
     *
     * @return The adjacency matrix of the graph.
     */
    public int[][] getAdjacencyMatrix() {
        logger.info("Returning adjacency matrix.");
        return adjacencyMatrix;
    }
}
