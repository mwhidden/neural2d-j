package neural2d.config;

/**
 * <p>
 * <p>
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public class MatrixConfig {
    private float[][] data;

    public static MatrixConfig parse(String data, int rows, int cols) throws ConfigurationException
    {
        MatrixConfig cfg = new MatrixConfig();
        cfg.data = new float[rows][cols];

        String[] elements = data.split("\\s");
        if(elements.length != rows * cols){
            throw new ConfigurationException(rows + " x " + cols +
                    " matrix requires " + (rows * cols) +
                    " data elements, but only " + elements.length +
                    " were provided");
        }
        int idx = 0;
        for(int i=0; i < rows; i++){
            for(int j=0; j < cols; j++){
                cfg.data[i][j] = Float.parseFloat(elements[idx++]);
            }
        }

        return cfg;
    }

    public float get(int row, int col)
    {
        return data[row][col];
    }

}
