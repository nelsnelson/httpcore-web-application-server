package org.nelsnelson.http.session;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.nelsnelson.http.server.ServletContainer;
import org.nelsnelson.http.server.Webapp;

public class DefaultHttpSession extends Session {
    public static final String SESSION_COOKIE_NAME = "JSESSIONID";
    
    protected int maxInactiveInterval = 0;
    
    private long creationTime = 0l;
    private long lastAccessedTime = 0l;
    
    private String sessionId = null;
    private Map<String, Object> sessionData = null;
    private boolean isNew = true;
    private boolean isInvalidated = false;
    private ServletContext servletContext = null;
    
    public DefaultHttpSession() {
        this.creationTime = System.currentTimeMillis();
    }
    
    public DefaultHttpSession(String sessionId) {
        this.sessionId = sessionId;
        Webapp webapp = ServletContainer.getWebappBySessionKey(sessionId);
        
        this.sessionData = new HashMap<String, Object>();
        this.isNew = true;
        this.isInvalidated = false;
        this.creationTime = System.currentTimeMillis();
        this.servletContext = webapp.getServletContext();
    }
    
    /**
     * Returns the ServletContext to which this session belongs.
     * 
     * @return ServletContext
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }
    
    public boolean isOutdated() {
        long maxInactive = getMaxInactiveInterval() * 1000;
        return maxInactive > 0 && System.currentTimeMillis() - lastAccessedTime > maxInactive;
    }
    
    public boolean isValid() {
        return !isInvalidated;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public String getId() {
        return sessionId;
    }
    
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }
    
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void removeAttribute(String attribute) {
        this.sessionData.remove(attribute);
    }

    /**
     * Returns the object bound with the specified name in this
     * session, or null if no object is bound under the name.
     * 
     * @param attribute
     * @return Object
     */
    public Object getAttribute(String attribute) {
        return this.sessionData.get(attribute);
    }

    public void setAttribute(String attribute, Object value) {
        this.sessionData.put(attribute, value);
    }

    /**
     * Returns an Enumeration of String objects containing the names
     * of all the objects bound to this session.
     * 
     * @return Enumeration<String>
     */
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.sessionData.keySet());
    }

    /**
     * @deprecated
     */
    @Deprecated
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        return new HttpSessionContext() {
            public Enumeration getIds() {
                return SessionManager.getIds(servletContext);
            }
            
            public HttpSession getSession(String sessionId) {
                return SessionManager.getSession(servletContext, sessionId);
            }
        };
    }
    
    public Object getValue(String valueName) {
        return sessionData.get(valueName);
    }
    
    public String[] getValueNames() {
        String[] valueNames = null;
        
        Set keySet = sessionData.keySet();
        
        if (keySet != null) {
            valueNames = (String[]) keySet.toArray(new String[] { });
        }
        
        return valueNames;
    }
    
    public void invalidate() {
        sessionData.clear();
        isInvalidated = true;
    }
    
    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean b) {
        isNew = b;
    }
    
    public void putValue(String valueName, Object value) {
        sessionData.put(valueName, value);
    }
    
    public void removeValue(String valueName) {
        sessionData.remove(valueName);
    }
    
    public void setMaxInactiveInterval(int maxInterval) {
        maxInactiveInterval = maxInterval;
    }
    
    public void setLastAccessedDate(long lastAccessedDate) {
        this.lastAccessedTime = lastAccessedDate;
    }
}
