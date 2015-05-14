package neural2d.config;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public class MatrixConfig
{
    private final double[][] data;

    public static MatrixConfig parse(String data, int rows, int cols) throws ConfigurationException
    {
        MatrixConfig cfg = new MatrixConfig(rows, cols);

        String[] elements = data.split("\\s");
        if (elements.length != rows * cols) {
            throw new ConfigurationException(rows + " x " + cols
                    + " matrix requires " + (rows * cols)
                    + " data elements, but only " + elements.length
                    + " were provided");
        }
        int idx = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cfg.data[i][j] = Double.parseDouble(elements[idx++]);
            }
        }

        return cfg;
    }

    private MatrixConfig(int rows, int cols)
    {
        data = new double[rows][cols];
    }

    public double get(int row, int col)
    {
        return data[row][col];
    }

}
