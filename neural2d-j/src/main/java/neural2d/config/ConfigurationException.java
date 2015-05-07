package neural2d.config;

/**
 * <p>
 * 
 * <p>
 * 
 * <p>
 * Copyright (c) 2015 Michael C. Whidden
 * @author Michael C. Whidden
 */
public class ConfigurationException extends Exception {

    /**
     * Creates a new instance of <code>ConfigurationException</code> without detail message.
     */
    public ConfigurationException() {
    }


    /**
     * Constructs an instance of <code>ConfigurationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ConfigurationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>ConfigurationException</code> with the specified detail message,
     * and underlying cause.
     * @param msg the detail message.
     * @param cause the cause.
     */
    public ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
