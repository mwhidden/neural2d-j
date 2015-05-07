package neural2d;

/**
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public interface TransferFunction {
    public float transfer(float val);
    public TransferFunction derivative();

    public static final TransferFunction LOGISTIC = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    return 1.0f/(1.0f+(float)Math.exp(-val));
                }

                @Override
                public TransferFunction derivative()
                {
                    return LOGISTIC_D;
                }
            };

    public static final TransferFunction LOGISTIC_D = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    return (float)Math.exp(-val)/(float)Math.pow((Math.exp(-val) + 1.0), 1.0);
                }

                @Override
                public TransferFunction derivative()
                {
                    return null;
                }

            };

    public static final TransferFunction IDENTITY = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    return val;
                }

                @Override
                public TransferFunction derivative()
                {
                    return IDENTITY_D;
                }
            };

    public static final TransferFunction IDENTITY_D = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    return 1.0f;
                }

                @Override
                public TransferFunction derivative()
                {
                    return null;
                }

            };

    public static final TransferFunction RAMP = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    if(val < -1.0) return -1.0f;
                    if(val > 1.0) return 1.0f;
                    return val;
                }

                @Override
                public TransferFunction derivative()
                {
                    return RAMP_D;
                }

            };

    public static final TransferFunction RAMP_D = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    if(val < -1.0 || val > 1.0){
                        return 0.0f;
                    } else {
                        return 1.0f;
                    }
                }

                @Override
                public TransferFunction derivative()
                {
                    return null;
                }

            };

    public static final TransferFunction GAUSSIAN = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    return (float)Math.exp(-((val * val)/2.0));
                }

                @Override
                public TransferFunction derivative()
                {
                    return GAUSSIAN_D;
                }
            };

    public static final TransferFunction GAUSSIAN_D = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    return -val * (float)Math.exp(-(val*val)/2.0);
                }

                @Override
                public TransferFunction derivative()
                {
                    return null;
                }

            };

    public static final TransferFunction TANH = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    return (float)Math.tanh(val);
                }

                @Override
                public TransferFunction derivative()
                {
                    return TANH_D;
                }
            };

    public static final TransferFunction TANH_D = new TransferFunction()
            {
                @Override
                public float transfer(float val)
                {
                    double t = Math.tanh(val);
                    return 1.0f - (float)(val*val);
                }

                @Override
                public TransferFunction derivative()
                {
                    return null;
                }

            };
}
