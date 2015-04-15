/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.algorithm.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import jifi.algorithm.Command;
import static jifi.algorithm.Command.identChar;
import jifi.algorithm.parser.decoder.Decoder;
import jifi.algorithm.parser.decoder.ParseException;
import jifi.algorithm.procedure.Block;
import jifi.algorithm.procedure.Function;
import jifi.gui.panels.editor.EditorPanel;
import jifi.interpreter.Interpreter;

/**
 *
 * @author antunes
 */
public class Parser {

    public Command getCommand(String str) {
        return null;
    }

    public static Function decode(String str) throws ParseException {
        EditorPanel.updateFunctionTokens();
        try {
            Decoder parser = new Decoder(new ByteArrayInputStream(str.getBytes("UTF-8")));
            Function f = parser.decode();
            return f;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Function decode(InputStream stream) throws ParseException {
        EditorPanel.updateFunctionTokens();
        Decoder parser = new Decoder(stream);
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
        Function a = Interpreter.bubbleSort(10, true);
        String as = encode(a);
        System.out.println(as);
        System.out.println("=========== decodificando ===========");
        Function b = decode(as);
        String bs = encode(b);
        System.out.println(bs);
    }
}
