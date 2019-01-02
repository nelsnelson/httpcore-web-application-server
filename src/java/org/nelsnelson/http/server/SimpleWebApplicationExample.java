package org.nelsnelson.http.server;

import java.util.HashMap;
import java.util.Map;

import org.nelsnelson.toolbox.util.Options;

/**
 * 
 * @author nelsnelson
 *
 */
public class SimpleWebApplicationExample {
    private static Map<String,String> defaults = new HashMap<String,String>();
    
    static {
        defaults.put("test", "test");
    }
    
    /** 
     * Creates a new instance of SimpleWebApplicationExample
     */
    public SimpleWebApplicationExample() {
        new ServletContainer(new Webapp(this)).getService().start();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Options(defaults, args);
        
        new SimpleWebApplicationExample();
    }
}
