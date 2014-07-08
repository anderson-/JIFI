/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3f.jifi;

import s3f.jifi.core.FlowchartPanel;
import s3f.core.plugin.Data;
import s3f.core.plugin.EntityManager;
import s3f.core.plugin.Extensible;
import s3f.core.ui.tab.Tab;
import s3f.core.ui.tab.TabProperty;
import s3f.jifi.flowchart.Function;

/**
 *
 * @author antunes
 */
public class TesteTab implements Tab, Extensible {

    private Data data;

    public TesteTab() {
        data = new Data("uia", "s3f.jifi", "asda asdas");
        TabProperty.put(data, "Teste", null, "...", new FlowchartPanel(new Function()));
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
    public void loadModulesFrom(EntityManager em) {

    }

}
