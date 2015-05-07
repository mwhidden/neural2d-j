package neural2d;

/**
 * <p>
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
class Util
{

    static String prepend(String string, String toString)
    {
        String[] a = toString.split("\\n");
        StringBuilder buff = new StringBuilder();
        for(String s: a){
            buff.append(string);
            buff.append(s);
            buff.append("\n");
        }
        return buff.toString();
    }

}
