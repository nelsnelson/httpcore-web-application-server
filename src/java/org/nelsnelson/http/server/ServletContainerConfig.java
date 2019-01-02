package org.nelsnelson.http.server;

import javax.servlet.ServletContext;

import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;

public class ServletContainerConfig extends BasicHttpContext {
    public static final String SERVER_CONFIG = "server.config";
    
    public ServletContainerConfig(HttpContext httpContext) {
        super(httpContext);
    }
    
    public ServletContext getContext(String uri) {
        ServletContext servletContext = null;
        
        if (uri == null) {
            return servletContext;
        }
        
        int lastSlashPos = uri.lastIndexOf('/');
        
        if (lastSlashPos >= 0 && uri.length() > lastSlashPos) {
            uri = uri.substring(0, lastSlashPos);
        }
        
        String url = "";
        
        if (uri.startsWith("/")) {
            String noLeadingSlash = url.substring(1);
            int slashPos = noLeadingSlash.indexOf("/");
            
            if (slashPos >= 0) {
                url = url.substring(0, slashPos + 1);
            }
        }
        
        servletContext = (ServletContext) ServletContainer.get(url);
        
        return servletContext;
    }
    
    public static ServletContainerConfig getInstance(HttpContext context) {
        ServletContainerConfig serverConfig = 
            (ServletContainerConfig) context.
            getAttribute(ServletContainerConfig.SERVER_CONFIG);
        
        if (serverConfig == null) {
            serverConfig = new ServletContainerConfig(context);
            context.setAttribute(ServletContainerConfig.SERVER_CONFIG, 
                serverConfig);
        }
        
        return serverConfig;
    }
}
