package org.nelsnelson.http.session;

import javax.servlet.http.HttpSession;

public abstract class Session implements HttpSession {
    public abstract boolean isNew();
    
    public abstract boolean isValid();
    
    public abstract boolean isOutdated();
    
    public abstract String getId();
    
    public abstract void invalidate();
}
