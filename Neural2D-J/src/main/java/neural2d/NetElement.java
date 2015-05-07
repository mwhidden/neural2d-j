package neural2d;

/**
 * <p>
 * <p>
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public interface NetElement
{
    public void accept(NetElementVisitor v);

}
