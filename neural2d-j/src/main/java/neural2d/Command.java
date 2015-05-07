package neural2d;

/**
 * <p>
 * <p>
 * <p>
 ** Copyright Michael C. Whidden 2015
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

    public static class FloatResult implements JoinableResult<Float>
    {
        private float value;

        public FloatResult(float f)
        {
            this.value = f;
        }

        @Override
        public void join(Float f){
            value += f;
        }

        @Override
        public Float getResult()
        {
            return value;
        }
    }
}
