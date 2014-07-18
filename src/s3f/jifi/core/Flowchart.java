/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import javax.swing.ImageIcon;
import s3f.core.code.CodeEditorTab;
import s3f.core.plugin.Plugabble;
import s3f.core.plugin.SimulableElement;
import s3f.core.project.ComplexElement;
import s3f.core.project.Editor;
import s3f.core.project.Element;
import s3f.core.project.Resource;
import s3f.core.project.editormanager.DefaultEditorManager;
import s3f.core.project.editormanager.EditorManager;
import s3f.core.project.editormanager.TextFile;
import s3f.jifi.core.interpreter.Interpreter;
import s3f.jifi.core.parser.decoder.Decoder;
import s3f.jifi.core.parser.decoder.ParseException;
import s3f.jifi.flowchart.Function;

/**
 *
 * @author antunes
 */
public class Flowchart extends ComplexElement implements TextFile, SimulableElement {

    public static final Element.CategoryData FLOWCHART_FILES = new Element.CategoryData("flowcht", "jf", new ImageIcon(Flowchart.class.getResource("/resources/icons/fugue/block.png")), new Flowchart());
    private static final EditorManager EDITOR_MANAGER = new DefaultEditorManager(new FlowchartEditorTab(), new CodeEditorTab());
    private static final ImageIcon ICON_DEFAULT_ERROR = new ImageIcon(FlowchartEditorTab.class.getResource("/resources/icons/fugue/block--exclamation.png"));
    private static final ImageIcon ICON_TEXT = new ImageIcon(FlowchartEditorTab.class.getResource("/resources/icons/fugue/script-block.png"));
    private static final ImageIcon ICON_TEXT_ERROR = new ImageIcon(FlowchartEditorTab.class.getResource("/resources/icons/fugue/script-block--exclamation.png"));

    private String text = "func f(){\n\t\n}\n\n";
    private final Interpreter interpreter;

    public Flowchart() {
        super("flowchart", "/resources/icons/fugue/block.png", FLOWCHART_FILES, EDITOR_MANAGER);
        interpreter = new Interpreter();
    }

    @Override
    public Plugabble createInstance() {
        return new Flowchart();
    }

    @Override
    public final void setText(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void addResource(Resource resource) {
        super.addResource(resource);
        if (resource.getPrimary() instanceof Flowchart) {
            Flowchart flowchart = (Flowchart) resource.getPrimary();
            flowchart.addExternalResource(resource.getSecondary());
            Editor currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                currentEditor.update();
            }
        }
    }

    @Override
    public void removeResource(Resource resource) {
        super.removeResource(resource);
        if (resource.getPrimary() instanceof Flowchart) {
            Flowchart flowchart = (Flowchart) resource.getPrimary();
            flowchart.removeExternalResource(resource.getSecondary());
            Editor currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                currentEditor.update();
            }
        }
    }

    public Function getFunction() {
        Decoder parser;
        try {
            parser = new Decoder(new ByteArrayInputStream(getText().getBytes("UTF-8")));
            Function function = parser.decode();
            return function;
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (UnsupportedEncodingException ex) {
        }
        return null;
    }
    
    @Override
    public s3f.core.simulation.System getSystem() {
//        interpreter.setMainFunction(getFunction()); //não é o melhor lugar!
        /*
         cuidado: sempre manter o fluxograma em edição no interpretador!
         TODO: como atualizar o fluxograma do editor de texto??
         */
        for (Object o : getExternalResources()) {
            interpreter.addResource(o);
        }
        return interpreter;
    }
}
