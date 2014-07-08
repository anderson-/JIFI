    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import s3f.core.plugin.Data;
import s3f.core.plugin.EntityManager;
import s3f.core.plugin.Plugabble;
import s3f.core.plugin.PluginManager;
import s3f.core.project.Editor;
import s3f.core.project.Element;
import s3f.core.simulation.Simulator;
import s3f.core.ui.tab.TabProperty;
import s3f.jifi.core.interpreter.Interpreter;
import s3f.jifi.core.parser.decoder.Decoder;
import s3f.jifi.core.parser.decoder.ParseException;
import s3f.jifi.flowchart.Function;

/**
 *
 * @author antunes
 */
public class FlowchartEditorTab implements Editor, PropertyChangeListener {

//    private static final ImageIcon ICON = new ImageIcon(FlowchartEditor.class.getResource("/resources/icons/fugue/block.png"));
    private final Data data;
    private final FlowchartPanel flowchartPanel;
    private Flowchart flowchart;

    public FlowchartEditorTab() {
        data = new Data("editorTab", "s3f.base.code", "Editor Tab");
        flowchartPanel = new FlowchartPanel(new Function());
        TabProperty.put(data, "Editor", null, "Editor de código", flowchartPanel);
    }

    @Override
    public void setContent(Element content) {
        if (content instanceof Flowchart) {
            flowchart = (Flowchart) content;
            content.setCurrentEditor(this);

            Decoder parser;
            try {
                parser = new Decoder(new ByteArrayInputStream(flowchart.getText().getBytes("UTF-8")));
                Function function = parser.decode();
                flowchartPanel.removePropertyChangeListener2(this);
                flowchartPanel.setFunction(function);
                
                EntityManager em = PluginManager.getInstance().createFactoryManager(null);
                Simulator sim = (Simulator) em.getProperty("s3f.core.interpreter.tmp", "interpreter");
                Interpreter i = new Interpreter();
                i.setMainFunction(function);
                sim.add(i);
                
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            flowchartPanel.addPropertyChangeListener2(this);
        }
    }

    @Override
    public Element getContent() {
        return flowchart;
    }

    @Override
    public void update() {

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
    }

}
