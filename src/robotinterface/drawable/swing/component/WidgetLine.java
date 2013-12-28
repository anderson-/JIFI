/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.drawable.swing.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.swing.MutableWidgetContainer;

/**
 *
 * @author antunes2
 */
public abstract class WidgetLine extends Component {

    private boolean onPageEnd = false;

    public WidgetLine() {
    }

    @Deprecated
    public WidgetLine(int height) {
//            this.height = height;
    }

    @Deprecated
    public WidgetLine(int width, int height) {
//            this.width = width;
//            this.height = height;
    }

    public WidgetLine(boolean onPageEnd) {
        this.onPageEnd = onPageEnd;
    }

    public boolean isOnPageEnd() {
        return onPageEnd;
    }

    @Deprecated
    public void createRow(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container, Object data) {

    }

    @Deprecated
    public String getString(Collection<Widget> widgets, Collection<TextLabel> labels, MutableWidgetContainer container) {
        return "";
    }

    //abstract
    public void createRow(Collection<Component> components, MutableWidgetContainer container, int index) {

    }

    @Deprecated
    public String getString(Collection<Component> components, MutableWidgetContainer container) {
        return "";
    }

    public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {

    }

    protected void createGenericField(Procedure p, Argument arg, String fieldName, int fieldWidth, int fieldHeight, Collection<Component> components, final MutableWidgetContainer container) {

        final JSpinner spinner1 = new JSpinner();
        spinner1.setModel(new SpinnerNumberModel(0, -360, 360, 2));
        JComboBox combobox1 = new JComboBox();
//                boolean num1 = true;

        MutableWidgetContainer.autoUpdateValue(spinner1);
        MutableWidgetContainer.setAutoFillComboBox(combobox1, p);

//                if (data != null) {
//                    if (data instanceof Rotate) {
//                        Rotate m = (Rotate) data;
//
//                        if (r.arg0.isVariable()) {
//                            combobox1.setSelectedItem(r.arg0.toString());
//                            num1 = false;
//                        } else {
//                            spinner1.setValue((int) r.arg0.getDoubleValue());
//                        }
//                    }
//                }
        final JButton changeButton1 = new JButton();
        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/tango/16x16/actions/system-search.png"));
        changeButton1.setIcon(icon);
        changeButton1.setToolTipText("Selecionar variável");

        components.add(new TextLabel(fieldName));

        final Widget wspinner1 = new Widget(spinner1, fieldWidth, fieldHeight);
        final Widget wcombobox1 = new Widget(combobox1, fieldWidth, fieldHeight);
        components.add(wspinner1);
        components.add(wcombobox1);

        container.entangle(arg, wspinner1, wcombobox1);

        components.add(new Widget(changeButton1, fieldHeight, fieldHeight));

        changeButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (container.contains(wspinner1)) {
                    container.removeWidget(wspinner1);
                    container.addWidget(wcombobox1);
                } else {
                    container.removeWidget(wcombobox1);
                    container.addWidget(wspinner1);
                }
            }
        });

//                if (num1) {
//                    container.addWidget(wspinner1);
//                } else {
//                    container.addWidget(wcombobox1);
//                }
    }

}
