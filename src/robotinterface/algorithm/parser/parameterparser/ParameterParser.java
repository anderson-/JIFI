/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.parser.parameterparser;

import java.util.ArrayList;
import robotinterface.algorithm.parser.decoder.ParseException;
import robotinterface.algorithm.parser.decoder.Token;

/**
 *
 * @author antunes2
 */
public class ParameterParser {

    private static final ArrayList<Argument> args = new ArrayList();

    public static Argument[] parse(String functionName, String strArgs, int nParameters, Token token) throws ParseException {
        args.clear();

        for (String arg : strArgs.split(",")) {
            args.add(new Argument(arg));
        }
        
        if (nParameters >= 0 && args.size() != nParameters){
            throw new ParseException(token, new int[][]{}, new String[]{
                "Numero de parametros na função \"" + functionName + "\": esperado " + nParameters + ", encontrado " +  args.size() + "."
            });
        }
        
        int minParameterNumber = -nParameters -1;
        
        if (minParameterNumber > 0 && args.size() < minParameterNumber){
            throw new ParseException(token, new int[][]{}, new String[]{
                "Numero de parametros na função \"" + functionName + "\": esperado ao menos " + minParameterNumber + ", encontrado " +  args.size() + "."
            });
        }

        return args.toArray(new Argument[args.size()]);
    }

//    {
//        //em Function.jj substituir:
//        parameters = parameters.trim();
//        block.add(ftoken.createInstance(parameters));
//        //por:
//        block.add(ftoken.createInstance(ParameterParser.parse(functionID.toString(), parameters, ftoken.getParameters(), token)));
//    }
}
