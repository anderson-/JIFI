/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.algorithm.parser.parameterparser;

import java.util.ArrayList;
import java.util.List;
import robotinterface.algorithm.parser.decoder.DecoderConstants;
import robotinterface.algorithm.parser.decoder.ParseException;
import robotinterface.algorithm.parser.decoder.Token;

/**
 *
 * @author antunes2
 */
public class ParameterParser {

    private static final ArrayList<Argument> args = new ArrayList();

    public static Argument[] parse(String functionName, List<Integer> argType, List<String> argEx, int nParameters, Token token) throws ParseException {
        if (nParameters >= 0 && argEx.size() != nParameters) {
            throw new ParseException(token, new int[][]{}, new String[]{
                "Numero de parametros na função \"" + functionName + "\": esperado " + nParameters + ", encontrado " + argEx.size() + "."
            });
        }

        int minParameterNumber = -nParameters - 1;

        if (minParameterNumber > 0 && argEx.size() < minParameterNumber) {
            throw new ParseException(token, new int[][]{}, new String[]{
                "Numero de parametros na função \"" + functionName + "\": esperado ao menos " + minParameterNumber + ", encontrado " + argEx.size() + "."
            });
        }

//        System.out.println(">>" + functionName);
//
//        for (int i = 0; i < argEx.size(); i++) {
//            System.out.println("p" + i + ": '" + argEx.get(i) + "'::" + argType.get(i));
//        }
        args.clear();
        for (int i = 0; i < argEx.size(); i++) {
            int type = argType.get(i);
            String str = argEx.get(i);
            switch (type) {
                case DecoderConstants.INTEGER_LITERAL:
                case DecoderConstants.DECIMAL_LITERAL:
                case DecoderConstants.HEX_LITERAL:
                case DecoderConstants.OCTAL_LITERAL:
                case DecoderConstants.FLOATING_POINT_LITERAL:
                    type = Argument.NUMBER_LITERAL;
                    break;
                case DecoderConstants.STRING_LITERAL:
                    type = Argument.STRING_LITERAL;
                    str = str.replace("\"", "");
                    break;
                case DecoderConstants.IDENTIFIER:
                    type = Argument.SINGLE_VARIABLE;
                    break;
                default:
                    type = Argument.EXPRESSION;
            }

            args.add(new Argument(str, type));
        }

        return args.toArray(new Argument[args.size()]);
    }
}
