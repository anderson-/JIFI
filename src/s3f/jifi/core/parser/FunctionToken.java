/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core.parser;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import s3f.jifi.core.Command;
import s3f.jifi.core.parser.parameterparser.Argument;

/**
 *
 * @author antunes
 */
public interface FunctionToken <T extends Command> { //TODO: substituir por TokenProperty

    public String getToken();
    
    public Completion getInfo(CompletionProvider provider);
    
//    public T createInstance(String args) throws ParseException;
    
    public int getParameters ();
    
    public T createInstance(Argument [] args);
    
}
