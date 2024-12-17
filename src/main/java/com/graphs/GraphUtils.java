package com.graphs;

/**
 * A utility class for graph-related operations.
 * This class provides methods for matrix manipulations commonly used in graph algorithms.
 */
public class GraphUtils {

    /**
     * Multiplies two square matrices.
     * This method performs matrix multiplication of two square matrices of the same size.
     *
     * @param m1 The first matrix to be multiplied.
     * @param m2 The second matrix to be multiplied.
     * @return A new matrix that represents the product of the two input matrices.
     * @throws IllegalArgumentException if the matrices are not of the same size.
     */
    public static int[][] multiplyMatrices(int[][] m1, int[][] m2) {
        int size = m1.length;

        // Ensure the matrices are square and of the same size
        if (m1.length != m2.length) {
            throw new IllegalArgumentException("Matrices must be of the same size.");
        }

        int[][] result = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    result[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }

        return result;
    }
}
