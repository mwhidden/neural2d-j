package neural2d;

/**
 * A class of exception thrown by Options to indicate an error in
 * parsing the options.
 *
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 * @see Options
 */
public class OptionException extends Exception
{
    public OptionException(String arg){
        super(arg);
    }
}