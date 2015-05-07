package neural2d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Package-private class for use by Options.
 */
class OptionCfg
{
    boolean required;

    boolean flag;

    String desc;

    OptionCfg(boolean r, boolean f, String d){
        required = r;
        flag = f;
        desc = d;
    }
}

/**
 * Package-private class for use by Options.
 */
class Option
{
    String name;

    int pos;

    String desc;

    Option(String n, int p){
        name = n;
        pos = p;
    }
}

/**
 * Package-private class for use by Options.
 */
class ValuedOption extends Option
{
    String value;

    ValuedOption(String n, String v, int p){
        super(n, p);

        value = v;
    }
}

/**
 * Package-private class for use by Options.
 */
class FileOption extends Option
{
    FileOption(String n, int p){
        super(n, p);
    }
}

/**
 * This class provides a simple interface for processing command-line
 * options, as well as usage information.
 * <p>
 * The parsing allows for a '--' option to cause processing of options
 * to stop and the rest of the command-line is assumed to be a list of
 * file options.
 * <pre>
 * Example: <i>Interpret '-file2' as a file and not an option</i>
 *    java Foo -arg1 value -- file1 -file2
 * </pre>
 * <p>
 * <pre>
 * Example:
 *    public static void main(String args[]) throws Exception {
 *       Options a = new Options();
 *       try {
 *           a.addOption("str",false,"This option takes a string argument");
 *           a.addOption("int",true,"This required option takes an integer parameter");
 *           a.addFlag("flag","If this option is present, bar is enabled.");
 *
 *           a.parseOptions(args);
 *
 *           while(a.next()){
 *               if(a.getName().equals("str")){
 *                   System.out.println("-str is " + a.getString());
 *               } else if (a.getName().equals("int")){
 *                   System.out.println("-int is " + a.getInt());
 *               } else if (a.getName().equals("flag")){
 *                   System.out.println("Found -flag");
 *               }
 *           }
 *
 *           // See if there are any extra arguments on the command line
 *           // In some cases, you may be interested in these, eg,
 *           // grep -l searchString file1 file2 file3
 *           Iterator iter = a.getFileList().listIterator();
 *
 *           while(iter.hasNext()){
 *               throw new OptionException("Extra arguments on command line");
 *           }
 *
 *       } catch (Exception e){
 *           System.out.println(e.getMessage());
 *           System.out.println("Usage: java Options <options>");
 *           System.out.println(a.getUsage());
 *       }
 *   }
 * </pre>
 * <p>
 * <p>
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public class Options
{
    private TreeMap<String, OptionCfg> argConfig;

    private Map<String, Options> argInfo;

    private LinkedList<Option> args;

    private Iterator<Option> argIter;

    private Option curArg;

    private boolean filesStarted;

    /**
     * Create a new Options object.
     */
    public Options(){
        super();

        argConfig = new TreeMap<String, OptionCfg>();

        args = new LinkedList<Option>();

        argInfo = new HashMap<String, Options>();
    }

    /**
     * Add a flag type option to the Options object.  A flag is a lone
     * option like <code>-d</code> that takes no value. It is either there or not.
     * <p>
     * @param name the name of the Option.  In the case of a '-d' flag,
     *             the name would be "d"
     * @param desc the description of the option, as should be displayed in
     *             the usage.
     */
    public void addFlag(String name, String desc) {
        argConfig.put("-"+name, new OptionCfg(false, true, desc));
    }


    /**
     * Add an option to the Options object.  This type of option
     * expects a value, as in <code>-inputfile foo.txt</code>
     * <p>
     * @param name the name of the option.  In the case of a '-d' option,
     *             the name would be "d"
     * @param desc the description of the option, as should be displayed in
     *             the usage.
     */
    public void addOption(String name, boolean required, String desc) {
        argConfig.put("-"+name, new OptionCfg(required, false, desc));
    }

    /**
     * Parse the options, returning an OptionException if the format
     * of the options is wrong or a required option is missing.
     * <p>
     * This method should be called after all options are configured with
     * <code>addFlag</code> or <code>addOption</code>
     * <p>
     * @param argv the options array from the command line.
     * @throws OptionException if a non-flag option is missing a value
     *                           or a required option is missing.
     */
    public void parseOptions(String argv[]) throws OptionException {
        int argCount = argv.length;
        int curArgIdx = 0;

        String argStr;

        OptionCfg cfg;

        Iterator<String> iter;

        // Parse the command line options
        if("on".equals(System.getProperty("com.fahnestock.util.Options.debug")))
        {
            for(int i=0; i < argCount; i++){
                System.out.println("Arg " + i + "= \"" + argv[i] + "\"");
            }
        }

        while (curArgIdx < argCount) {
            argStr = argv[curArgIdx];
            cfg = argConfig.get(argStr);

            if(argStr.equals("--")){
                filesStarted = true;
            } else if(cfg == null || filesStarted) {
                filesStarted = true;
                args.addLast(new FileOption(argStr, curArgIdx));
            } else {
                if(!cfg.flag){
                    if(curArgIdx + 1 < argCount){
                        args.addLast(new ValuedOption(argStr,
                                                        argv[curArgIdx+1],
                                                        curArgIdx++));
                    } else {
                        throw new OptionException("Expected option parameter to follow '" +
                                                    argStr + "'");
                    }
                } else {
                    args.addLast(new Option(argStr,
                                              curArgIdx));
                }
                argInfo.put(argStr,this);
            }
            curArgIdx++;
        }

        iter = argConfig.keySet().iterator();

        while(iter.hasNext()){
            String key = iter.next();

            cfg = argConfig.get(key);

            if(cfg.required && !argInfo.containsKey(key)){
                throw new OptionException("Missing required option '" +
                                            key + "'");
            }
        }

        argIter = args.listIterator();
    }

    /**
     * After calling parseOptions, call next() repeatedly until it returns
     * false to advance to the next Option.
     * <p>
     * @return true if there is a 'next' option.  false otherwise.
     */
    public boolean next(){
        if(argIter == null){
            return false;
        }
        if(argIter.hasNext()){
            curArg = argIter.next();
            if(curArg == null || curArg instanceof FileOption){
                argIter = null;
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieve the name of the current Option, as selected by
     * <code>next()</code>.
     * <p>
     * @return the name of the option, without the leading '-'.
     */
    public String getName() {
        if(curArg != null){
            return curArg.name.substring(1);
        } else {
            return null;
        }
    }

    /**
     * Return the value of the current option, if there is one.
     * In the case of '-f foo.txt', if the current option is "f",
     * then getString will return "foo.txt".
     * <p>
     * @return the value of the option
     * @throws IllegalArgumentException if the option takes no argument (a flag)
     */
    public String getString(){
        if(curArg == null){
            return null;
        } else if(curArg instanceof ValuedOption) {
            return ((ValuedOption)curArg).value;
        } else {
            throw new IllegalArgumentException("Option '" + curArg.name +
                                               "' takes no argument");
        }
    }

    /**
     * Return the value of the current option as an int, if there is one.
     * In the case of '-f 123', if the current option is "f",
     * then getString will return 123.
     * <p>
     * @return the value of the option as an int.
     * @throws NumberFormatException if the option value is not a valid
     *                               integer
     * @throws IllegalArgumentException if the option takes no argument (a flag)
     */
    public int getInt() throws NumberFormatException {
        if(curArg instanceof ValuedOption) {
            try {
                return Integer.parseInt(((ValuedOption)curArg).value);
            } catch (NumberFormatException e){
                throw new NumberFormatException("Parameter to '" +
                                                curArg.name + "' is not a " +
                                                " number (" +
                                                ((ValuedOption)curArg).value +
                                                ")");
            }
        } else {
            throw new IllegalArgumentException("Option '" + curArg.name +
                                            "' takes no argument");
        }
    }

    /**
     * Return a string formatted to describe the usage of the command.
     * Each option is described, in arbitrary order, in the text.
     * <p>
     * @return a string formatted to describe the usage of the command.
     */
    public String getUsage(){

        StringBuilder usage = new StringBuilder();
        Iterator<String> iter;
        String key;
        OptionCfg cfg;

        iter = argConfig.keySet().iterator();

        while(iter.hasNext()){
            key = iter.next();

            cfg = argConfig.get(key);

            usage.append("  ").append(key).append(" ");
            if(cfg.required){
                usage.append("(required)");
            }
            if(cfg.desc != null){
                usage.append(" ").append(cfg.desc);
            }
            usage.append("\n");
        }

        return usage.toString();
    }

    /**
     * Return a linked list of any command-line arguments that
     * follow the last valid command-line option, or all the arguments
     * that follow the special option '--'.
     * <p>
     * For Example:
     * If the command line were:
     * <code>java MyProgram -d foo.txt -i 120 input.dat output.dat</code>
     * then this method might return the list ("input.dat", "output.dat")
     * <code>java MyProgram -d foo.txt -i 120 -- -file1 -file2
     * would return ("-file1", "-file2")
     * <p>
     * @return the LinkedList of non-option argument on the command-line.
     */
    public LinkedList<String> getFileList(){
        LinkedList<String> l = new LinkedList<String>();
        Iterator<Option> fileIter = args.listIterator();

        while(fileIter.hasNext()){
            Option arg = fileIter.next();

            if(arg instanceof FileOption){
                l.addLast((arg).name);
            }
        }

        return l;
    }
}
