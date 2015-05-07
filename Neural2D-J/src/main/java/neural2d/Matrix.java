package neural2d;

import neural2d.config.MatrixConfig;

/**
 * <p>
 *
 * <p>
 *
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public class Matrix
{
    private final float[][] data;
    private final int numCols;
    private final int numRows;

    public Matrix(int rows, int cols)
    {
        data = new float[rows][cols];
        for(int i=0; i < rows; i++){
            data[i] = new float[cols];
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

    public void set(int row, int col, float val)
    {
        data[row][col] = val;
    }

    public float get(int row, int col)
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
