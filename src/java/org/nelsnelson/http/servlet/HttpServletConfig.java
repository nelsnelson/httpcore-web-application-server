package org.nelsnelson.http.servlet;

import java.util.Dictionary;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class HttpServletConfig implements ServletConfig {
    private final Dictionary<String,String> parameters;
    private final ServletContext context;

    private final String servletName;

    public HttpServletConfig(final Dictionary<String,String> parameters,
            final ServletContext context) {

        this.parameters = parameters;
        this.context = context;
        this.servletName = (String) parameters.get("");
    }

    public String getInitParameter(final String name) {
        return parameters.get(name);
    }

    public Enumeration<String> getInitParameterNames() {
        return parameters.keys();
    }

    public ServletContext getServletContext() {
        return context;
    }

    public String getServletName() {
        return servletName;
    }
}