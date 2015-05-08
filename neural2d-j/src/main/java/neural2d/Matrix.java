package neural2d;

import neural2d.config.MatrixConfig;

/**
 * <p>
 *
 * <p>
 *
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class Matrix
{
    private final double[][] data;
    private final int numCols;
    private final int numRows;

    public Matrix(int rows, int cols)
    {
        data = new double[rows][cols];
        for(int i=0; i < rows; i++){
            data[i] = new double[cols];
        }
        this.numRows = rows;
        this.numCols = cols;
    }

    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append("{");
        for(int row = 0; row < numRows; row++){
            if(row > 0){
                buff.append(",");
            }
            buff.append("{");
            for(int col = 0; col < numCols; col++){
                if(col > 0){
                    buff.append(",");
                }
                buff.append(data[row][col]);
            }
            buff.append("}");
        }
        buff.append("}");
        return buff.toString();
    }

    public void clear()
    {
        for(int row = 0; row < numRows; row++){
            for(int col = 0; col < numCols; col++){
                data[row][col] = 0;
            }
        }

    }

    public void set(int row, int col, double val)
    {
        data[row][col] = val;
    }

    public double get(int row, int col)
    {
        return data[row][col];
    }

    public void load(MatrixConfig cfg)
    {
        for(int row = 0; row < numRows; row++){
            for(int col = 0; col < numCols; col++){
                data[row][col] = cfg.get(row, col);
            }
        }
    }

    public int getNumColumns()
    {
        return numCols;
    }

    public int getNumRows()
    {
        return numRows;
    }
}
