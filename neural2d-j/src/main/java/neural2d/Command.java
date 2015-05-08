package neural2d;

/**
 * <p>
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 * @param <T>
 */
public interface Command<T,V> {
    public JoinableResult<V> execute(T target);
    public boolean canParallelize();

    public static interface JoinableResult<T>
    {
        public void join(T o);
        public T getResult();
    }

    public static class DoubleResult implements JoinableResult<Double>
    {
        private double value;

        public DoubleResult(double f)
        {
            this.value = f;
        }

        @Override
        public void join(Double f){
            value += f;
        }

        @Override
        public Double getResult()
        {
            return value;
        }
    }
}
