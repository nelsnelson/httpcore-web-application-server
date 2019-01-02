package org.nelsnelson.http.util;

import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.protocol.HttpContext;

public class ConnUtils {
    public static HttpConnection getConnection(HttpContext context) {
        HttpConnection conn = null;
        
        Object o = null; // TODO FIXME
        
        if (o instanceof HttpConnection) {
            conn = (HttpConnection) o;
        }
        
        return conn;
    }
}
