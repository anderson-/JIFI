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
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import robotinterface.algorithm.parser.parameterparser.Argument;
import robotinterface.algorithm.procedure.Procedure;
import robotinterface.drawable.swing.MutableWidgetContainer;

/**
 *
 * @author antunes2
 */
public abstract class WidgetLine extends Component {

    private boolean onPageEnd = false;

    @Deprecated
    public WidgetLine(int i) {
    }

    @Deprecated
    public WidgetLine(int i, int j) {
    }

    public WidgetLine() {
    }

    public WidgetLine(boolean onPageEnd) {
        this.onPageEnd = onPageEnd;
    }

    public boolean isOnPageEnd() {
        return onPageEnd;
    }

    public abstract void createRow(Collection<Component> components, MutableWidgetContainer container, int index);

    public void toString(StringBuilder sb, ArrayList<Argument> arguments, MutableWidgetContainer container) {

    }

//    protected Widget[] createGenericField(Procedure p, Argument arg, String fieldName, int fieldWidth, int fieldHeight, Collection<Component> components, final MutableWidgetContainer container) {
//        //cria componentes swing
//        JSpinner spinner = new JSpinner();
//        JComboBox combobox = new JComboBox();
//        JTextField textfield = new JTextField();
//        final JButton changeButton = new JButton();
//
//        //habilita o foco
//        spinner.setFocusable(true);
//        combobox.setFocusable(true);
//        textfield.setFocusable(true);
//
//        //define comportamento durante o foco e adiciona valores
//        spinner.setModel(new SpinnerNumberModel(0, -360, 360, 2));
//        MutableWidgetContainer.autoUpdateValue(spinner);
//        MutableWidgetContainer.setAutoFillComboBox(combobox, p);
//
//        //cria widgets e define seus respectivos tamanhos
//        final Widget wspinner = new Widget(spinner, fieldWidth, fieldHeight);
//        final Widget wcombobox = new Widget(combobox, fieldWidth, fieldHeight);
//        final Widget wtextfield = new Widget(textfield, fieldWidth, fieldHeight);
//
//        //aciona a atualização e seleção automática dos componentes de acordo
//        //com o tipo de argumento
//        final Widget chosen = container.entangle(arg, wspinner, wcombobox, wtextfield);
//
//        //obtem o foco para o widget selecionado
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                chosen.getJComponent().requestFocus();
//            }
//        });
//
//        //adiciona widgets criados
//        components.add(new TextLabel(fieldName));
//        components.add(wspinner);
//        components.add(wcombobox);
//        components.add(wtextfield);
//        components.add(new Widget(changeButton, fieldHeight, fieldHeight));
//
//        //define o icone e comportamento do botão
//        final ImageIcon iconcb = new ImageIcon(getClass().getResource("/resources/fugue/ui-combo-box.png"));
//        final ImageIcon iconsp = new ImageIcon(getClass().getResource("/resources/fugue/ui-spin.png"));
//        final ImageIcon icontf = new ImageIcon(getClass().getResource("/resources/fugue/ui-text-field.png"));
//
//        if (container.contains(wspinner)) {
//            changeButton.setIcon(iconcb);
//            changeButton.setToolTipText("Selecionar variável");
//        } else if (container.contains(wcombobox)) {
//            changeButton.setIcon(icontf);
//            changeButton.setToolTipText("Edição livre");
//        } else {
//            changeButton.setIcon(iconsp);
//            changeButton.setToolTipText("Selecionar valor");
//        }
//
//        ActionListener actionListener = new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (container.contains(wspinner)) {
//                    container.removeWidget(wspinner);
//                    container.addWidget(wcombobox);
//                    wcombobox.getJComponent().requestFocusInWindow();
//                    changeButton.setIcon(icontf);
//                    changeButton.setToolTipText("Edição livre");
//                } else if (container.contains(wcombobox)) {
//                    container.removeWidget(wcombobox);
//                    container.addWidget(wtextfield);
//                    wtextfield.getJComponent().requestFocusInWindow();
//                    changeButton.setIcon(iconsp);
//                    changeButton.setToolTipText("Selecionar valor");
//                } else {
//                    container.removeWidget(wtextfield);
//                    container.addWidget(wspinner);
//                    wspinner.getJComponent().requestFocusInWindow();
//                    changeButton.setIcon(iconcb);
//                    changeButton.setToolTipText("Selecionar variável");
//                }
//            }
//        };
//
//        changeButton.addActionListener(actionListener);
////        actionListener.actionPerformed(null);
//
//        return new Widget[]{wspinner, wcombobox, wtextfield};
//    }
    public static final int ARG_SPINNER = 1;
    public static final int ARG_COMBOBOX = 2;
    public static final int ARG_TEXTFIELD = 4;
    public static final int DEFAULT = ARG_SPINNER | ARG_COMBOBOX | ARG_TEXTFIELD;

    protected Widget[] createGenericField(Procedure p, Argument arg, String fieldName, int fieldWidth, int fieldHeight, Collection<Component> components, final MutableWidgetContainer container) {
        return createGenericField(p, arg, fieldName, fieldWidth, fieldHeight, components, container, DEFAULT);
    }

    protected Widget[] createGenericField(Procedure p, Argument arg, String fieldName, int fieldWidth, int fieldHeight, Collection<Component> components, final MutableWidgetContainer container, int type) {

        final ArrayList<Widget> ws = new ArrayList<>();
        final ArrayList<Object[]> infos = new ArrayList<>();

        components.add(new TextLabel(fieldName));

        if ((type & ARG_SPINNER) != 0) {
            JSpinner spinner = new JSpinner();
            spinner.setModel(new SpinnerNumberModel(0, -360, 360, 2));
            spinner.setFocusable(true);
            MutableWidgetContainer.autoUpdateValue(spinner);
            Widget wspinner = new Widget(spinner, fieldWidth, fieldHeight);
            components.add(wspinner);
            ws.add(wspinner);
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/fugue/ui-spin.png"));
            infos.add(new Object[]{"Selecionar valor", icon, wspinner});
        }

        if ((type & ARG_COMBOBOX) != 0) {
            JComboBox combobox = new JComboBox();
            combobox.setFocusable(true);
            MutableWidgetContainer.setAutoFillComboBox(combobox, p);
            Widget wcombobox = new Widget(combobox, fieldWidth, fieldHeight);
            components.add(wcombobox);
            ws.add(wcombobox);
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/fugue/ui-combo-box.png"));
            infos.add(new Object[]{"Selecionar variável", icon, wcombobox});
        }

        if ((type & ARG_TEXTFIELD) != 0) {
            JTextField textfield = new JTextField();
            textfield.setFocusable(true);
            Widget wtextfield = new Widget(textfield, fieldWidth, fieldHeight);
            components.add(wtextfield);
            ws.add(wtextfield);
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/fugue/ui-text-field.png"));
            infos.add(new Object[]{"Edição livre", icon, wtextfield});
        }

        Widget[] aws = new Widget[]{};
        aws = ws.toArray(aws);
        final Widget chosen = container.entangle(arg, aws);

        //obtem o foco para o widget selecionado
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chosen.getJComponent().requestFocus();
            }
        });

        if (infos.size() > 1) {
            final JButton changeButton = new JButton();
            components.add(new Widget(changeButton, fieldHeight, fieldHeight));

            for (int i = 0; i < infos.size(); i++) {
                if (container.contains(ws.get(i))) {
                    int next = (i + 1 >= infos.size()) ? 0 : i + 1;
                    Object[] info = infos.get(next);
                    changeButton.setToolTipText((String) info[0]);
                    changeButton.setIcon((ImageIcon) info[1]);
                    break;
                }
            }

            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < infos.size(); i++) {
                        if (container.contains(ws.get(i))) {
                            container.removeWidget(ws.get(i));
                            int next = (i + 1 >= infos.size()) ? 0 : i + 1;
                            Object[] info = infos.get(next);
                            changeButton.setToolTipText((String) info[0]);
                            changeButton.setIcon((ImageIcon) info[1]);
                            container.addWidget((Widget) info[2]);
                            ((Widget) info[2]).getJComponent().requestFocusInWindow();
                            break;
                        }
                    }
                }
            };

            changeButton.addActionListener(actionListener);
        }

        return aws;

    }

}
