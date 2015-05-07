package neural2d.config;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>
 *
 * <p>
 *
 * <p>
 ** Copyright Michael C. Whidden 2015
 * @author Michael C. Whidden
 */
public class SAXErrorHandler implements ErrorHandler
{
    SAXException exception = null;
    String warning = null;

    @Override
    public void warning(SAXParseException exception) throws SAXException
    {
        if(this.warning != null){
            this.warning += "\n";
        }
        this.warning += exception.getMessage();
    }

    @Override
    public void error(SAXParseException exception) throws SAXException
    {
        this.exception = exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException
    {
        this.exception = exception;
    }

}
