/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi.core;

import java.io.InputStream;
import javax.swing.ImageIcon;
import s3f.core.code.CodeEditorTab;
import s3f.core.plugin.Plugabble;
import s3f.core.project.Editor;
import s3f.core.project.Element;
import s3f.core.project.FileCreator;
import s3f.core.project.SimpleElement;
import s3f.core.project.editormanager.DefaultEditorManager;
import s3f.core.project.editormanager.EditorManager;
import s3f.core.project.editormanager.TextFile;

/**
 *
 * @author antunes
 */
public class Flowchart extends SimpleElement implements TextFile {

    public static final Element FLOWCHART_FILE = new Flowchart();
    public static final Element.CategoryData FLOWCHART_FILES = new Element.CategoryData("flowcht", "jf", new ImageIcon(Flowchart.class.getResource("/resources/icons/fugue/block.png")), FLOWCHART_FILE);
    private static final EditorManager EDITOR_MANAGER = new DefaultEditorManager(new FlowchartEditorTab(), new CodeEditorTab());
    private static final ImageIcon ICON_DEFAULT = new ImageIcon(FlowchartEditorTab.class.getResource("/resources/icons/fugue/block.png"));
    private static final ImageIcon ICON_DEFAULT_ERROR = new ImageIcon(FlowchartEditorTab.class.getResource("/resources/icons/fugue/block--exclamation.png"));
    private static final ImageIcon ICON_TEXT = new ImageIcon(FlowchartEditorTab.class.getResource("/resources/icons/fugue/script-block.png"));
    private static final ImageIcon ICON_TEXT_ERROR = new ImageIcon(FlowchartEditorTab.class.getResource("/resources/icons/fugue/script-block--exclamation.png"));

    private String text = "func f(){\n\t\n}\n\n";
    private Editor editor = null;

    public Flowchart() {
        super("flowchart", ICON_DEFAULT, FLOWCHART_FILES, EDITOR_MANAGER);
    }

    @Override
    public void save(FileCreator fileCreator) {
        StringBuilder sb = new StringBuilder();
        sb.append(getText());
        fileCreator.makeTextFile(getName(), FLOWCHART_FILES.getExtension(), sb);
    }

    @Override
    public Element load(InputStream stream) {
        Flowchart newFlowchart = new Flowchart();
        newFlowchart.setText(FileCreator.convertInputStreamToString(stream));
        return newFlowchart;
    }

    @Override
    public Plugabble createInstance() {
        return new Flowchart();
    }

    @Override
    public final void setText(String text) {
        System.out.println("update text");
        this.text = text;
    }
    
    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setCurrentEditor(Editor editor) {
        this.editor = editor;
    }

}
