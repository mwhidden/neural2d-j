package neural2d;

/**
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public class Sample
{
    private final Matrix targetVals;
    private Matrix data;
    private final ImageData imageData;

    public static Sample createSample(Matrix input, Matrix target)
    {
        return new Sample(input, target);
    }

    public static Sample createSample(ImageData imageData, Matrix target)
    {
        return new Sample(imageData, target);
    }

    private Sample(Matrix input, Matrix target)
    {
        this.data = input;
        this.targetVals = target;
        this.imageData = null;
    }

    private Sample(ImageData img, Matrix target)
    {
        this.imageData = img;
        this.targetVals = target;
        this.data = null;
    }

    public static class ImageData
    {
        byte[][][] data;
        int rows, cols;
        public ImageData(byte[] bytes, int rows, int cols)
        {
            int idx = 0;
            this.rows = rows;
            this.cols = cols;
            int row = 0;
            data = new byte[rows][cols][3];
            while(idx < bytes.length){
                data[row] = new byte[cols][3];
                for(int col=0; col < cols; col++){
                    data[row][col] = new byte[3];
                    data[row][col][0] = bytes[idx++];
                    data[row][col][1] = bytes[idx++];
                    data[row][col][2] = bytes[idx++];
                }
                row++;
            }
        }

        public int getNumRows()
        {
            return rows;
        }

        public int getNumColumns()
        {
            return cols;
        }

        public float getRed(int row, int col)
        {
            return data[row][col][2];
        }

        public float getGreen(int row, int col)
        {
            return data[row][col][1];
        }

        public float getBlue(int row, int col)
        {
            return data[row][col][0];
        }

        public float getBW(int row, int col)
        {
            // Todo: Document the magic 0.3, 0.6, 0.1 numbers.
             return 0.3f * getRed(row, col) +   // Red
                           0.6f * getGreen(row,col) +   // Green
                           0.1f * getBlue(row,col);    // Blue
        }

    }

    /**
     * Get the pixel data, converted to Floats and flattened to a 1D list.
     *
     * @return the value of data
     */
    public Matrix getData()
    {
        return data;
    }

    private void filterImage(ColorChannel colorChannel)
    {
        data = new Matrix(imageData.getNumRows(),
                    imageData.getNumColumns());

        // BMP pixels are arranged in memory in the order (B, G, R). We'll convert
        // the pixel to a float using one of the conversions below:

        float val = 0.0f;

        for(int row = 0; row < imageData.getNumRows(); row++){
            for (int col = 0; col < imageData.getNumColumns(); ++col) {
                if (colorChannel == ColorChannel.R) {
                    val = imageData.getRed(row,col); // Red
                } else if (colorChannel == ColorChannel.G) {
                    val = imageData.getGreen(row,col); // Red
                } else if (colorChannel == ColorChannel.B) {
                    val = imageData.getBlue(row, col); // Blue
                } else if (colorChannel == ColorChannel.BW) {
                    val =  imageData.getBW(row, col);
                }

                // Convert it to the range 0.0..1.0: this value will be the input to an input neuron:
                data.set(row, col, val / 256.0f);
            }
        }
    }

    public Matrix getData(ColorChannel colorChannel)
    {
        if(data == null && imageData != null){
            filterImage(colorChannel);
        }
        return data;
    }

    public void clearCache()
    {
        data.clear();
    }

    /**
     * Get the value of targetVals
     *
     * @return the value of targetVals
     */
    public Matrix getTargetVals()
    {
        return targetVals;
    }

    public float getTargetVal(int row, int col)
    {
        return targetVals.get(row, col);
    }

}
