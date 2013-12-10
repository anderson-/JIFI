/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.parser;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import robotinterface.algorithm.Command;
import robotinterface.algorithm.parser.decoder.ParseException;
import robotinterface.algorithm.parser.parameterparser.Argument;

/**
 *
 * @author antunes
 */
public interface FunctionToken <T extends Command> {

    public String getToken();
    
    public Completion getInfo(CompletionProvider provider);
    
    public T createInstance(String args) throws ParseException;
    
//    public int getParameters ();
//    
//    public T createInstance(Argument [] args);
    
}
