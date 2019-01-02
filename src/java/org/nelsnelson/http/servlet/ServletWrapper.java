package org.nelsnelson.http.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @author nelsnelson
 */
public class ServletWrapper implements Servlet {
    private Servlet servlet = null;
    private ServletConfig servletConfig = null;
    private String servletInfo = null;
    
    public ServletWrapper(Servlet servlet) {
        this.servlet = servlet;
    }
    
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }
    
    public void setServletInfo(String servletInfo) {
        this.servletInfo = servletInfo;
    }
    
    public void destroy() {
        servlet.destroy();
    }
    
    public ServletConfig getServletConfig() {
        return servletConfig;
    }
    
    public String getServletInfo() {
        return servletInfo;
    }
    
    public void init(ServletConfig servletConfig) throws ServletException {
        servlet.init(servletConfig);
    }
    
    public void service(ServletRequest request, ServletResponse response) 
        throws ServletException, IOException 
    {
        servlet.service(request, response);
    }
}
