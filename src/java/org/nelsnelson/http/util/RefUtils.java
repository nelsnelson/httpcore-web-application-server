/*
 * RefUtils.java
 *
 * Created on December 13, 2006, 3:58 PM
 */

package org.nelsnelson.http.util;

import java.lang.ref.WeakReference;

/**
 *
 * @author nelsnelson
 */
public class RefUtils {
    /** Creates a new instance of RefUtils */
    public RefUtils() {
        
    }
    
    public static WeakReference getTransient(Class clazz) {
        WeakReference reference = null;
        
        try {
            reference = new WeakReference(clazz.newInstance());
        }
        catch (InstantiationException ex) {
            ex.printStackTrace();
        }
        catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        
        return reference;
    }
}
