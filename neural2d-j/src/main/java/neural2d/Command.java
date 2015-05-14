package neural2d;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 *
 * @author Michael C. Whidden
 * @param <T> the class of the object the command will execute on.
 * @param <V> the class of the result of the command execution.
 */
public interface Command<T, V>
{

    public JoinableResult<V> execute(T target);

    public boolean canParallelize();

    public static interface JoinableResult<T>
    {

        public void join(JoinableResult<T> o);

        public T getResult();
    }

    public static class NoOpCommand<U, W> implements Command<U, W>
    {

        @Override
        public JoinableResult<W> execute(U target)
        {
            return null;
        }

        @Override
        public boolean canParallelize()
        {
            return false;
        }

    }

    public static class DoubleResult implements JoinableResult<Double>
    {

        private double value;

        public DoubleResult(double f)
        {
            this.value = f;
        }

        @Override
        public void join(JoinableResult<Double> o)
        {
            if (o != null) {
                value += o.getResult();
            }
        }

        @Override
        public Double getResult()
        {
            return value;
        }
    }
}
