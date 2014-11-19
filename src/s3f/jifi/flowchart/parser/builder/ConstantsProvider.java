/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package s3f.jifi.flowchart.parser.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author antunes
 */
public class ConstantsProvider {
    private ArrayList<String> names = new ArrayList<>();

    public ConstantsProvider(String ... ns){
        names.addAll(Arrays.asList(ns));
    }
    
    public List<String> getNames() {
        return names;
    }
    
}
