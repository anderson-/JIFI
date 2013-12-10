/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robotinterface.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author antunes2
 */
public final class ResourceManager {

    private final Map<Class, Object> map = new HashMap<>();

    public void setResource(Object resource){
        map.remove(resource.getClass());
        map.put(resource.getClass(), resource);
    }
    
    public <T> T getResource(Class c) throws ResourceNotFoundException {
        T t = (T) map.get(c);
        if (t != null) {
            return t;
        } else {
            throw new ResourceNotFoundException(c);
        }
    }
    
    public void clear(){
        map.clear();
    }

}