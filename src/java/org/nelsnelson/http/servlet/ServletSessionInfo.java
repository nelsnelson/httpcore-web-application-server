package org.nelsnelson.http.servlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.nelsnelson.http.server.ServletContainer;

public class ServletSessionInfo {
    private Map sessionIds = Collections.EMPTY_MAP;
    private Map requestedSessionIds = Collections.EMPTY_MAP;
    private String deadRequestedSessionId = null;
    
    public ServletSessionInfo() {
        sessionIds = new HashMap();
        requestedSessionIds = new HashMap();
    }
    
    public Object putSessionId(ServletContext context, String sessionId) {
        String contextPrefix = 
            (String) context.getAttribute(ServletContainer.PREFIX);
        
        return sessionIds.put(contextPrefix, sessionId);
    }
    
    public String getSessionId(ServletContext context) {
        String contextPrefix = 
            (String) context.getAttribute(ServletContainer.PREFIX);
        
        return (String) sessionIds.get(contextPrefix);
    }
    
    public Map getSessionIds() {
        return sessionIds;
    }

    public Object putRequestedSessionId(ServletContext context, String sessionId) {
        String contextPrefix = 
            (String) context.getAttribute(ServletContainer.PREFIX);
        
        return requestedSessionIds.put(contextPrefix, sessionId);
    }
    
    public String getRequestedSessionId(ServletContext context) {
        String contextPrefix = 
            (String) context.getAttribute(ServletContainer.PREFIX);
        
        return (String) requestedSessionIds.get(contextPrefix);
    }
    
    public Map getRequestedSessionIds() {
        return requestedSessionIds;
    }
    
    public String getDeadRequestedSessionId() {
        return deadRequestedSessionId;
    }
    
    public void setDeadRequestedSessionId(String deadRequestedSessionId) {
        this.deadRequestedSessionId = deadRequestedSessionId;
    }
}
