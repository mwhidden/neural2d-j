package neural2d;

/**
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 */
public interface TransferFunction
{

    public double transfer(double val);

    public TransferFunction derivative();

    public static final TransferFunction LOGISTIC = new TransferFunction()
    {
        @Override
        public double transfer(double val)
        {
            return 1.0 / (1.0 + Math.exp(-val));
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
        public double transfer(double val)
        {
            return Math.exp(-val) / Math.pow((Math.exp(-val) + 1.0), 2.0);
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
        public double transfer(double val)
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
        public double transfer(double val)
        {
            return 1.0;
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
        public double transfer(double val)
        {
            if (val < -1.0) {
                return -1.0;
            }
            if (val > 1.0) {
                return 1.0;
            }
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
        public double transfer(double val)
        {
            if (val < -1.0 || val > 1.0) {
                return 0.0;
            } else {
                return 1.0;
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
        public double transfer(double val)
        {
            return Math.exp(-((val * val) / 2.0));
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
        public double transfer(double val)
        {
            return -val * Math.exp(-(val * val) / 2.0);
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
        public double transfer(double val)
        {
            return Math.tanh(val);
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
        public double transfer(double val)
        {
            double t = Math.tanh(val);
            return 1.0 - (t * t);
        }

        @Override
        public TransferFunction derivative()
        {
            return null;
        }

    };
}
