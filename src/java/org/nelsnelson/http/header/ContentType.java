/*
 * ContentType.java
 *
 * Created on December 13, 2006, 9:28 PM
 */

package org.nelsnelson.http.header;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpHeaders;

/**
 *
 * @author nelsnelson
 */
public class ContentType implements Header {
    public static final String CHARSET = "charset";
    
    private String mimeType = null;
    private Map<String, List<String>> parametersMap = new LinkedHashMap<String, List<String>>();

    private String value = null;
    
    /** Creates a new instance of ContentType */
    public ContentType(Header contentType) {
        this(contentType.getName() + ": " + contentType.getValue());
    }
    
    /** Creates a new instance of ContentType */
    public ContentType(String contentType) {
        if (contentType.indexOf(HttpHeaders.CONTENT_TYPE + ": ") >= 0) {
            contentType = contentType.substring((HttpHeaders.CONTENT_TYPE + ": ").length());
        }
        
        this.value  = contentType;
        
        List<String> parameters = Arrays.asList(contentType.split(";"));
        
        this.mimeType = parameters.iterator().next();
        
        for (String parameter : parameters) {
            Iterator<String> pair = Arrays.asList(parameter.split("=", 1)).iterator();
            String key = pair.next();
            String value = pair.next();
            List<String> values = Arrays.asList(value.split(","));
            
            this.parametersMap.put(key, values);
        }
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getFirstParameter(String key) {
        String parameter = null;
        
        List<String> parameters = this.parametersMap.get(key);
        
        if (parameters != null) {
            parameter = parameters.get(0);
        }
        
        return parameter;
    }
    
    public List<String> getParameters(String key) {
        return this.parametersMap.get(key);
    }
    
    public HeaderElement[] getElements() {
        Collection<List<String>> values = this.parametersMap.values();
        HeaderElement[] elements = new HeaderElement[values.size()];
        
        return elements;
    }
    
    public String getName() {
        return HttpHeaders.CONTENT_TYPE;
    }
    
    public String getValue() {
        return value;
    }

    public void setParameter(String key, String value) {
        parametersMap.put(key, Arrays.asList(value));
    }

    public boolean isSensitive() {
        return false;
    }
}
