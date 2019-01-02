package org.nelsnelson.http.session;

import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.protocol.HttpContext;

public class HttpSessionContext implements HttpContext {
    public static final String HTTP_SESSION_MANAGER  = "http.session.manager";
    
    private HttpContext context = null;
    
    public HttpSessionContext(HttpContext context) {
        this.context = context;
    }
    
    public Object getAttribute(String id) {
        return context.getAttribute(id);
    }
    
    public Object removeAttribute(String id) {
        return context.removeAttribute(id);
    }

    public ProtocolVersion getProtocolVersion() {
        return context.getProtocolVersion();
    }

    public Object setAttribute(String id, Object obj) {
        return context.setAttribute(id, obj);
    }

    public void setProtocolVersion(ProtocolVersion version) {
        context.setProtocolVersion(version);
    }
}
