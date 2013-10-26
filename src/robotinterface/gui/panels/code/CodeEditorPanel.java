/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.gui.panels.code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import robotinterface.algorithm.parser.Parser;
import robotinterface.algorithm.parser.decoder.ParseException;
import robotinterface.algorithm.procedure.Function;
import robotinterface.gui.GUI;
import robotinterface.gui.panels.FlowchartPanel;
import robotinterface.gui.panels.code.jedit.*;

/**
 *
 * @author antunes
 */
public class CodeEditorPanel extends JPanel {

    public CodeEditorPanel(Function f) {
        super(new BorderLayout());

        final JEditTextArea textArea = new JEditTextArea();
        textArea.setTokenMarker(new FunctionTokenMarker());
        textArea.setText(Parser.encode(f));
        textArea.getPainter().setFont(UIManager.getDefaults().getFont("TextPane.font"));
        textArea.recalculateVisibleLines();
        textArea.setFirstLine(0);
        textArea.setElectricScroll(0);
        textArea.getPainter().setSelectionColor(
                UIManager.getColor("TextArea.selectionBackground"));

        SyntaxStyle[] styles = SyntaxUtilities.getDefaultSyntaxStyles();
        styles[Token.COMMENT1] = new SyntaxStyle(Color.GRAY, true, false);
        styles[Token.KEYWORD1] = new SyntaxStyle(Color.blue, false, false);
        styles[Token.KEYWORD2] = new SyntaxStyle(new Color(0x100080), false, true);
        styles[Token.KEYWORD3] = new SyntaxStyle(Color.CYAN.darker().darker(), false, false);

        styles[Token.LITERAL1] = new SyntaxStyle(Color.orange, false, false);
        styles[Token.LITERAL2] = new SyntaxStyle(Color.CYAN, false, false);

        textArea.getPainter().setStyles(styles);

        JScrollPane jsp = new JScrollPane(textArea);

        jsp.getVerticalScrollBar().setUnitIncrement(10);

        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        JButton convert = new JButton("Com");
        convert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String str = textArea.getText();
                    GUI.getInstance().add(
                            new FlowchartPanel(Parser.decode(str)),
                            new ImageIcon(getClass().getResource("/resources/tango/16x16/categories/applications-other.png")));
                } catch (ParseException ex) {
                    int pos = textArea.getLineEndOffset(ex.currentToken.next.endLine);
                    pos += ex.currentToken.next.beginColumn;
                    textArea.select(pos, pos+1);
                } catch (Exception ex) {
                }
            }
        });

        tb.add(convert);
        add(tb, BorderLayout.PAGE_START);
//        add(new JButton("converter"));
//        add(new JButton("sei lá..."));
        add(jsp);

    }
}
