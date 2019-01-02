package org.nelsnelson.http.session;

import javax.servlet.http.HttpSession;

public final class HttpSessionFactory {
    private static HttpSessionFactory instance = null;
    
    private HttpSessionFactory() {
        
    }
    
    private static HttpSessionFactory getInstance() {
        if (instance == null) {
            instance = new HttpSessionFactory();
        }
        
        return instance;
    }
    
    public static HttpSession getHttpSession() {
        return getInstance().createHttpSession();
    }
    
    private HttpSession createHttpSession() {
        return new DefaultHttpSession();
    }
}
