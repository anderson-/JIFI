    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.flowchart;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import s3f.core.plugin.Data;
import s3f.core.plugin.Plugabble;
import s3f.core.project.Editor;
import s3f.core.project.Element;
import s3f.core.ui.tab.TabProperty;
import s3f.jifi.core.FlowScript;
import s3f.jifi.flowchart.builder.parser.FlowchartBuilder;
import s3f.jifi.flowchart.builder.parser.ParseException;
import s3f.jifi.flowchart.blocks.ScriptBlock;

/**
 *
 * @author antunes
 */
public class FlowchartEditorTab implements Editor, PropertyChangeListener {

//    private static final ImageIcon ICON = new ImageIcon(FlowchartEditor.class.getResource("/resources/icons/fugue/block.png"));
    private final Data data;
    private final FlowchartPanel flowchartPanel;
    private FlowScript flowchart;

    public FlowchartEditorTab() {
        data = new Data("editorTab", "s3f.core.code", "Editor Tab");
        flowchartPanel = new FlowchartPanel(new ScriptBlock());
        TabProperty.put(data, "Editor", null, "Editor de código", flowchartPanel);
    }

    @Override
    public void setContent(Element content) {
        if (content instanceof FlowScript) {
            flowchart = (FlowScript) content;
            FlowchartBuilder parser;
            ScriptBlock function = null;
            try {
                parser = new FlowchartBuilder(new ByteArrayInputStream(flowchart.getText().getBytes("UTF-8")));
                function = parser.decode();
                System.out.println("Done");
            } catch (ParseException ex) {
                ex.printStackTrace();
            } catch (UnsupportedEncodingException ex) {
            }
            if (function != null) {
                flowchartPanel.removePropertyChangeListener2(this);
                flowchartPanel.setFunction(function);
//                flowchartPanel.setInterpreter((Interpreter) flowchart.getSystem());
                flowchartPanel.addPropertyChangeListener2(this);
            }
            data.setProperty(TabProperty.TITLE, content.getName());
            data.setProperty(TabProperty.ICON, content.getIcon());
        }
    }

    @Override
    public Element getContent() {
        return flowchart;
    }

    @Override
    public void update() {
//        EntityManager em = PluginManager.getInstance().createFactoryManager(null);
//        Simulator sim = (Simulator) em.getProperty("s3f.core.interpreter.tmp", "interpreter");
//        Interpreter i = new Interpreter();
//        for (Object o : flowchart.getExternalResources()) {
//            i.addResource(o);
//        }
//        i.setMainFunction(flowchartPanel.getFunction());
//        sim.clear();
//        sim.add(i);
    }

    @Override
    public void selected() {

    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void init() {

    }

    @Override
    public Plugabble createInstance() {
        return new FlowchartEditorTab();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        StringBuilder sb = new StringBuilder();
        flowchartPanel.getFunction().toString("", sb);
        sb.append("\n");
        flowchart.setText(sb.toString());
        System.out.println("UPDATED");
    }
}
