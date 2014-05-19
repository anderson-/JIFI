/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package robotinterface.drawable;

/**
 *
 * @author anderson
 */
public class Layer implements Comparable<Layer>{
    
    private final int layer;

    public Layer(int layer){
        this.layer = layer;
    }
    
    @Override
    public int compareTo(Layer o) {
        return (layer < o.layer) ? -1 : ((layer == o.layer) ? 0 : 1);
    }
    
}
