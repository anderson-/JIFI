/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.flowchart.parser.parameterparser;

import java.util.ArrayList;
import java.util.List;
import s3f.jifi.flowchart.parser.builder.parser.FlowchartBuilderConstants;
import s3f.jifi.flowchart.parser.builder.parser.ParseException;
import s3f.jifi.flowchart.parser.builder.parser.Token;

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
                case FlowchartBuilderConstants.INTEGER_LITERAL:
                case FlowchartBuilderConstants.DECIMAL_LITERAL:
                case FlowchartBuilderConstants.HEX_LITERAL:
                case FlowchartBuilderConstants.OCTAL_LITERAL:
                case FlowchartBuilderConstants.FLOATING_POINT_LITERAL:
                    type = Argument.NUMBER_LITERAL;
                    break;
                case FlowchartBuilderConstants.STRING_LITERAL:
                    str = str.replace("\"", "");
                    if (str.contains(" ")){
                        type = Argument.TEXT;
                    } else {
                        type = Argument.STRING_LITERAL;
                    }
                    break;
                case FlowchartBuilderConstants.IDENTIFIER:
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
