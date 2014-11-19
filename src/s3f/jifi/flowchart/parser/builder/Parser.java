/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.flowchart.parser.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import s3f.jifi.flowchart.Command;
import s3f.jifi.flowchart.parser.builder.parser.FlowchartBuilder;
import s3f.jifi.flowchart.parser.builder.parser.ParseException;
import s3f.jifi.flowchart.blocks.Block;
import s3f.jifi.flowchart.blocks.Function;
//import s3f.jifi.interpreter.Interpreter;

/**
 *
 * @author antunes
 */
public class Parser {

    public Command getCommand(String str) {
        return null;
    }

    public static Function decode(String str) throws ParseException {
//        EditorPanel.updateFunctionTokens(); TODO
        try {
            FlowchartBuilder parser = new FlowchartBuilder(new ByteArrayInputStream(str.getBytes("UTF-8")));
            Function f = parser.decode();
            return f;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean tryDecode(String str) {
        try {
            FlowchartBuilder parser = new FlowchartBuilder(new ByteArrayInputStream(str.getBytes("UTF-8")));
            Function f = parser.decode();
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static Function decode(InputStream stream) throws ParseException {
//        EditorPanel.updateFunctionTokens(); TODO
        FlowchartBuilder parser = new FlowchartBuilder(stream);
        Function f = parser.decode();
        return f;
    }

    public static String encode(Block b) {
        StringBuilder sb = new StringBuilder();
        b.toString("", sb);
        sb.append("\n");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
//        Function a = Interpreter.bubbleSort(10, true);
//        String as = encode(a);
//        System.out.println(as);
//        System.out.println("=========== decodificando ===========");
//        Function b = decode(as);
//        String bs = encode(b);
//        System.out.println(bs);
    }
}
