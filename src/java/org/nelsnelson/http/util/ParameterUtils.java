package org.nelsnelson.http.util;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.nelsnelson.com.oreilly.servlet.MultipartRequest;
import org.nelsnelson.org.apache.excalibur.source.SourceParameters;

public class ParameterUtils {
    public static Map getParameterMap(String parameters) {
        return HttpParameterMapFactory.getInstance().createHttpParameterMap(parameters);
    }

    public static Map getParameterMap(MultipartRequest request) {
        Map parameters = new LinkedHashMap();

        Enumeration parameterNames = request.getParameterNames();
        
        while (parameterNames.hasMoreElements()) {
            Object element = parameterNames.nextElement();
            
            if (element instanceof String) {
                String name = (String) element;
                String value = request.getParameter(name);
                
                parameters.put(name, value);
            }
        }
        
        return parameters;
    }
    
    public static final class HttpParameterMapFactory {
        private static HttpParameterMapFactory instance = null;
        
        private HttpParameterMapFactory() {
            
        }
        
        public static HttpParameterMapFactory getInstance() {
            if (instance == null) {
                instance = new HttpParameterMapFactory();
            }
            
            return instance;
        }
        
        public HttpParameterMap createHttpParameterMap(String queryString) {
            return new HttpParameterMap(queryString);
        }
    }
    
    private static class HttpParameterMap extends ParameterMap {
        public HttpParameterMap(String parameters) {
            super(new SourceParameters(parameters).getParameterMap());
        }
    }
    
    private static class ParameterMap extends LinkedHashMap {
        public ParameterMap(Map m) {
            super(m);
        }
    }

    public static Map parseParameters(String parameterLine) {
        Map parameters = new LinkedHashMap();
        
        String[] args = parameterLine.split("&");
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if (arg != null) {
                String[] pair = arg.split("=");
                
                if (pair != null) {
                    String key = pair.length > 0 ? pair[0] : "";
                    String value = pair.length > 1 ? pair[1] : "";
                    
                    if (key.length() > 0) {
                        parameters.put(key, value);
                    }
                }
            }
        }
        
        return parameters;
    }
}
