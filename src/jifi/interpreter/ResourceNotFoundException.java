/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.interpreter;

/**
 *
 * @author antunes2
 */
public final class ResourceNotFoundException extends ExecutionException {

    public ResourceNotFoundException(Class c) {
        super("The resource " + c + " is missing.");
    }
}