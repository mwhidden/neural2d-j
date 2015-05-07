package neural2d;

/**
 * <p>
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
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
