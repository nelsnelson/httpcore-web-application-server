package org.nelsnelson.http.servlet;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.nelsnelson.http.server.ServletContainer;
import org.nelsnelson.toolbox.util.ClassUtils;

public class ServletConfigurator {
    private Dictionary<String,String> parameters = new Hashtable<String,String>();
    private String servletInfo = null;
    private ServletConfig servletConfig = null;
    private ServletContext servletContext = null;
    private HttpConnection conn = null;
    private HttpContext context = null;
    
    public ServletConfigurator(HttpConnection conn, HttpContext context) {
        this.conn = conn;
        this.context = context;
    }
    
    public Servlet configure(String webRoot, String servletRoot, Servlet servlet) {
        servletContext = (ServletContext) ServletContainer.get(webRoot);
        servletConfig = servlet.getServletConfig();
        servletInfo = servletRoot + ClassUtils.getSimpleName(servlet.getClass());
        
        if (servletConfig == null) {
            servletConfig = new HttpServletConfig(parameters, servletContext);
        }
        
        ServletWrapper servletWrapper = new ServletWrapper(servlet);
        servletWrapper.setServletConfig(servletConfig);
        servletWrapper.setServletInfo(servletInfo);
        
        return servletWrapper;
    }
}
