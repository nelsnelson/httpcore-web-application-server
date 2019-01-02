package org.nelsnelson.http.session;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.hc.core5.http.HttpRequest;
import org.nelsnelson.http.server.ServletContainer;
import org.nelsnelson.http.server.Webapp;
import org.nelsnelson.http.servlet.ServletSessionInfo;

public class SessionManager {
    private static Map sessionManagers = new HashMap();
    
    public static void maybeInit(HttpRequest request, ServletContext servletContext) {
        //String path = request.getRequestLine().getUri();
        
        HttpSessionManager sessionManager = 
            (HttpSessionManager) sessionManagers.get(servletContext);
        
        if (sessionManager == null) {
            sessionManager = new HttpSessionManager(servletContext);
            sessionManagers.put(servletContext, sessionManager);
        }
    }
    
    public static HttpSession getSession(ServletContext servletContext, String sessionId) {
        HttpSessionManager sessionManager = 
            (HttpSessionManager) sessionManagers.get(servletContext);
        
        return sessionManager.getSession(sessionId);
    }
    
    public static void addSession(ServletContext servletContext, HttpSession session) {
        HttpSessionManager sessionManager = 
            (HttpSessionManager) sessionManagers.get(servletContext);
        
        sessionManager.addSession((Session) session);
    }
    
    public static Enumeration getIds(ServletContext servletContext) {
        HttpSessionManager sessionManager = 
            (HttpSessionManager) sessionManagers.get(servletContext);
        
        return sessionManager.getIds();
    }
    
    private static Map sessionInfo = Collections.EMPTY_MAP;
    
    public static ServletSessionInfo getSessionInfo(HttpServletRequest request) {
        if (sessionInfo == null || sessionInfo == Collections.EMPTY_MAP) {
            sessionInfo = new HashMap();
        }
        
        ServletSessionInfo info = (ServletSessionInfo) sessionInfo.get(request);
        
        if (info == null) {
            info = new ServletSessionInfo();
            sessionInfo.put(request, info);
        }
        
        return info;
    }
    
    public static void manageSessionCookies(HttpServletRequest request, Cookie[] cookies) {
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            
            if (cookie.getName().equals(DefaultHttpSession.SESSION_COOKIE_NAME)) {
                // Get a managing context
                Webapp webapp =
                    ServletContainer.getWebappBySessionKey(cookie.getValue());
                ServletContext managingContext = webapp.getServletContext();
                
                if (managingContext != null) {
                    getSessionInfo(request).putRequestedSessionId(managingContext, cookie.getValue());
                    getSessionInfo(request).putSessionId(managingContext, cookie.getValue());
                }
                else {
                    getSessionInfo(request).setDeadRequestedSessionId(cookie.getValue());
                }
            }
        }
    }
    
    public static ServletSessionInfo discardSessionInfo(HttpServletRequest request) {
        if (sessionInfo == null || sessionInfo == Collections.EMPTY_MAP) {
            sessionInfo = new HashMap();
        }
        
        ServletSessionInfo info = null;
        
        if (sessionInfo.containsKey(request)) {
            info = (ServletSessionInfo) sessionInfo.remove(request);
        }
        
        return info;
    }
    
    public static Object putSessionId(HttpServletRequest request, 
        ServletContext context, String id) 
    {
        return getSessionInfo(request).putSessionId(context, id);
    }
    
    public static String getSessionId(HttpServletRequest request, ServletContext context) {
        return (String) getSessionInfo(request).getSessionId(context);
    }

    public static void debug() {
        System.out.println();
        
        System.out.println("debugging session manager");
        
        for (Iterator i = sessionInfo.keySet().iterator(); i.hasNext(); ) {
            HttpServletRequest servletRequest = (HttpServletRequest) i.next();
            
            String prefix = 
                "SessionInfo(" + servletRequest + ").";
            
            ServletSessionInfo sessionInfo = getSessionInfo(servletRequest);
            
            System.out.println(".  " + prefix + "deadRequestedSessionId=" + 
                sessionInfo.getDeadRequestedSessionId());
            
            System.out.println(".  " + prefix + "requestedSessionIds=");
            for (Iterator j = sessionInfo.getRequestedSessionIds().values().iterator(); j.hasNext(); ) {
                System.out.print(j.next() + ", ");
            }
            System.out.println();
            
            System.out.println(".  " + prefix + "sessionIds=");
            for (Iterator j = sessionInfo.getSessionIds().values().iterator(); j.hasNext(); ) {
                System.out.print(j.next() + ", ");
            }
            System.out.println();
        }
        
        System.out.println();
        
        for (Iterator i = sessionManagers.keySet().iterator(); i.hasNext(); ) {
            ServletContext managingServletContext = (ServletContext) i.next();
            
            String prefix = 
                "SessionManager(" + 
                ((org.nelsnelson.http.server.Webapp) managingServletContext).
                getServletRoot() + ").";
            
            HttpSessionManager sessionManager = 
                (HttpSessionManager) sessionManagers.get(managingServletContext);
            
            for (Enumeration j = sessionManager.getIds(); j.hasMoreElements(); ) {
                String sessionId = (String) j.nextElement();
                System.out.println(".  " + prefix + "sessionId=" + sessionId);
                
                HttpSession session = sessionManager.getSession(sessionId);
                
                String[] valueNames = session.getValueNames();
                for (int k = 0; k < valueNames.length; k++) {
                    String name = valueNames[k];
                    System.out.println(".  " + prefix + name + "=" + session.getValue(name));
                }
            }
        }
    }
}
