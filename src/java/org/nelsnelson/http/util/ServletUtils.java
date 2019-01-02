/*
 * CgiUtils.java
 *
 * Created on December 13, 2006, 3:47 PM
 */

package org.nelsnelson.http.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 *
 * @author nelsnelson
 */
public class ServletUtils {
    private ServletUtils() {
        
    }
    
    public final static Enumeration EMPTY_ENUMERATION = new Enumeration() {
        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            throw new NoSuchElementException();
        }
    };
}
