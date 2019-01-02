package org.nelsnelson.http.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;

import org.nelsnelson.http.server.ServletContainer;
import org.nelsnelson.http.server.ServletContainerConfig;
import org.nelsnelson.http.util.ServletUtils;
import org.nelsnelson.toolbox.util.ClassUtils;

/**
 *
 * @author nelsnelson
 */
public class HttpServletContext implements ServletContext {
    public static final String HTTP_CONTEXT  = "http.context";
    public static final String INDEX_FILE = "file.index";
    public static final String DEFAULT_WEB_ROOT_DIR = "default.web.root.directory";
    public static final String DEFAULT_SERVLET_ROOT_DIR = "default.servlet.root.directory";
    public static final String WEB_ROOT_DIR = "web.root.directory";
    public static final String SERVLET_ROOT_DIR = "servlet.root.directory";

    private JspConfigDescriptor jspConfigDescriptor = null;
    private Map<String,Object> attributes = Collections.emptyMap();
    private Vector<EventListener> listeners = null;
    private Set<SessionTrackingMode> effectiveSessionTrackingModes = null;
    private Set<SessionTrackingMode> defaultSessionTrackingModes = null;
    private SessionCookieConfig sessionCookieConfig = null;
    private Map<String,FilterRegistration> filterRegistrations = null;
    private Map<String,ServletRegistration> servletRegistrations = null;
    
    public HttpServletContext() {
        this.attributes = new HashMap<String,Object>();
        this.listeners = new Vector<EventListener>();
        this.effectiveSessionTrackingModes = new HashSet<SessionTrackingMode>();
        this.defaultSessionTrackingModes = new HashSet<SessionTrackingMode>();
        this.sessionCookieConfig = new DefaultSessionCookieConfig();
        this.filterRegistrations = new HashMap<String,FilterRegistration>();
        this.servletRegistrations = new HashMap<String,ServletRegistration>();
    }

    public ServletContext getServletContext() {
        return this;
    }

    /**
     * Returns the context path of the web application.
     */
    public String getContextPath() {
        String contextPath = null;
        
        // TODO Implement
        
        return contextPath;
    }
    
    public ServletContext getContext(final String uri) {
        ServletContainerConfig serverConfig = 
            (ServletContainerConfig) this.getAttribute(ServletContainerConfig.SERVER_CONFIG);
        
        return serverConfig.getContext(uri);
    }

    public int getMajorVersion() {
        return 1;
    }
    
    public int getEffectiveMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 0;
    }
    
    public int getEffectiveMinorVersion() {
        return 0;
    }

    public String getMimeType(final String file) {
        String mimeType = "text/html";
        
        return mimeType;
    }

    public URL getResource(final String path) {
        this.getAttribute("resourceManager");
        
        URL resource = null;
        
        try {
            resource = new URL(path);
        }
        catch (Exception e) {
            
        }
        
        return resource;//getResource(realPath + path);
    }

    public InputStream getResourceAsStream(final String path) {
        final URL url = getResource(path);
        
        if (url == null) {
            return null;
        }
        
        try {
            return url.openStream();
        }
        catch (IOException ioe) {
            return null;
        }
    }

    public RequestDispatcher getRequestDispatcher(final String uri) {
        return null;//registrations.getRequestDispatcher(uri);
    }

    public RequestDispatcher getNamedDispatcher(final String name) {
        return null; // NYI: OK, but could be implemented
    }

    public Servlet getServlet(final String name) {
        return null; // deprecated
    }

    public Enumeration getServlets() {
        return ServletUtils.EMPTY_ENUMERATION; // deprecated
    }

    public Enumeration getServletNames() {
        return ServletUtils.EMPTY_ENUMERATION; // deprecated
    }

    public void log(final String message) {
        //if (log.doInfo())
        //    log.info(message);
    }

    public void log(final Exception exception, final String message) {
        log(message, exception); // deprecated
    }

    public void log(final String message, final Throwable throwable) {
        //if (log.doWarn())
        //    log.warn(message, throwable);
    }

    public String getRealPath(final String path) {
        if (path == null) {
            return null;
        }
        else {
            try {
                //File res = new File(webRoot, path);
                String defaultServletRoot = this.getAttribute(DEFAULT_SERVLET_ROOT_DIR).toString();
                String servletRoot = this.getAttribute(SERVLET_ROOT_DIR).toString();
                if (servletRoot == null) {
                    servletRoot = defaultServletRoot;
                }
                File res = new File(servletRoot, path);
                if (res.isDirectory()) {
                    return res.getCanonicalPath() + "/";
                }
                else {
                    return res.getCanonicalPath();
                }
            }
            catch (IOException err) {
                return null;
            }
        }
    }

    public String getServerInfo() {
        return ServletContainer.getProperty(ServletContainer.VERSION);
    }
    
    /**
     * Gets the <jsp-config> related configuration that was aggregated
     * from the web.xml and web-fragment.xml descriptor files of the
     * web application represented by this ServletContext.
     */
    public JspConfigDescriptor getJspConfigDescriptor() {
        if (this.jspConfigDescriptor == null) {
            this.jspConfigDescriptor = new DefaultJspConfigDescriptor();
        }
        return this.jspConfigDescriptor;
    }

    public Object getAttribute(final String name) {
        return this.attributes.get(name);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    public void setAttribute(final String name, final Object value) {
        this.attributes.put(name, value);
    }

    public void removeAttribute(final String name) {
        this.attributes.remove(name);
    }
    
    /**
     * Returns the name of this web application corresponding
     * to this ServletContext as specified in the deployment
     * descriptor for this web application by the display-name
     * element.
     */
    public String getServletContextName() {
        return this.getClass().getName();
    }

    /**
     * Adds the servlet with the given name and class type
     * to this servlet context.
     */
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return this.addServlet(servletName, this.createServlet(servletClass));
    }

    /**
     * Registers the given servlet instance with this
     * ServletContext under the given servletName.
     * 
     * @return ServletRegistration.Dynamic
     */
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        ServletRegistration.Dynamic dynamicServletRegistration = new DefaultServletRegistration(servlet).dynamic();
        this.servletRegistrations.put(servletName, dynamicServletRegistration);
        return dynamicServletRegistration;
    }

    /**
     * Adds the servlet with the given name and class
     * name to this servlet context.
     */
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        Class servletClass = null;
        try {
            servletClass = this.getClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException ex) {
            // TODO FIXME
        }
        return this.addServlet(servletName, servletClass);
    }

    /**
     * Instantiates the given Servlet class.
     */
    public <T extends Servlet> T createServlet(Class<T> clazz) {
        T servlet = null;
        try {
            servlet = (T) clazz.newInstance();
        }
        catch (IllegalAccessException ex) {
            // TODO FIXME
        }
        catch (InstantiationException ex) {
            // TODO FIXME
        }
        return servlet;
    }
    
    /**
     * Gets the ServletRegistration corresponding to the servlet
     * with the given servletName.
     */
    public ServletRegistration getServletRegistration(String servletName) {
        return this.servletRegistrations.get(servletName);
    }

    /**
     * Gets a (possibly empty) Map of the ServletRegistration
     * objects (keyed by servlet name) corresponding to all
     * servlets registered with this ServletContext.
     */
    public Map<String,? extends ServletRegistration> getServletRegistrations() {
        return this.servletRegistrations;
    }

    /**
     * Instantiates the given EventListener class.
     */
    public <T extends EventListener> T createListener(Class<T> clazz) {
        T listener = null;
        try {
            listener = (T) clazz.newInstance();
        }
        catch (IllegalAccessException ex) {
            // TODO FIXME
        }
        catch (InstantiationException ex) {
            // TODO FIXME
        }
        return listener;
    }

    /**
     * Adds the given listener to this ServletContext.
     */
    public void addListener(Class<? extends EventListener> listenerClass) {
        this.addListener(this.createListener(listenerClass));
    }
    
    public <T extends EventListener> void addListener(T listener) {
        this.listeners.add(listener);
    }
    
    public void addListener(String className) {
        Class listenerClass = null;
        try {
            listenerClass = this.getClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException ex) {
            // TODO FIXME
        }
        EventListener listener = this.createListener(listenerClass);
        this.listeners.add(listener);
    }
    
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return this.effectiveSessionTrackingModes;
    }
    
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return this.defaultSessionTrackingModes;
    }
    
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        this.effectiveSessionTrackingModes = sessionTrackingModes;
    }
    
    public SessionCookieConfig getSessionCookieConfig() {
        return this.sessionCookieConfig;
    }

    /**
     * Adds the filter with the given name and class type to this servlet context.
     */
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        Filter filter = this.createFilter(filterClass);
        return this.addFilter(filterName, filter);
    }

    /**
     * Registers the given filter instance with this ServletContext under the given filterName.
     */
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        FilterRegistration.Dynamic dynamicFilterRegistration = new DefaultFilterRegistration(filter).dynamic();
        this.filterRegistrations.put(filterName, dynamicFilterRegistration);
        return dynamicFilterRegistration;
    }

    /**
     * Adds the filter with the given name and class name to this servlet context.
     */
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        Class filterClass = null;
        try {
            filterClass = this.getClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException ex) {
            // TODO FIXME
        }
        return this.addFilter(filterName, filterClass);
    }

    public <T extends Filter> T createFilter(Class<T> clazz) {
        T filter = null;
        try {
            filter = (T) clazz.newInstance();
        }
        catch (IllegalAccessException ex) {
            // TODO FIXME
        }
        catch (InstantiationException ex) {
            // TODO FIXME
        }
        return filter;
    }
    
    public FilterRegistration getFilterRegistration(String registrationName) {
        return this.filterRegistrations.get(registrationName);
    }
    
    public Map<String,? extends FilterRegistration> getFilterRegistrations() {
        return this.filterRegistrations;
    }

    /**
     * Gets the class loader of the web application represented by this
     * ServletContext.
     */
    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }
    
    public String getClassPackage() {
        return ClassUtils.getClassPackage(this.getClass());
    }
    
    // These properties are unique per web application
    public final Properties properties = new Properties();
    
    /** 
     * Gets the properties for this application
     * 
     * @param property    String is the given key for getting the mapped property value
     * @return String property
     */
    public String getProperty(String property) {
        return getProperties().getProperty(property);
    }
    
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Declares role names that are tested using isUserInRole.
     */
    public void declareRoles(String... roles) {
        // TODO Implement
    }

    /**
     * Adds the servlet with the given jsp file to this servlet context.
     */
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        return this.addServlet(servletName, new DynamicServlet(jspFile));
    }
    
    /**
     * Returns the configuration name of the logical host on which
     * the ServletContext is deployed.
     */
    public String getVirtualServerName() {
        return this.attributes.get("virtual.server").toString();
    }

    /**
     * Gets the session timeout in minutes that are supported by
     * default for this ServletContext.
     */
    public int getSessionTimeout() {
        Object value = this.attributes.get("session.timeout");
        Integer sessionTimeout = Integer.parseInt(value.toString());
        return sessionTimeout.intValue();
    }

    /**
     * Sets the session timeout in minutes for this ServletContext.
     */
    public void setSessionTimeout(int sessionTimeout) {
        this.attributes.put("session.timeout", sessionTimeout);
    }

    /**
     * Gets the response character encoding that are supported by
     * default for this ServletContext.
     */
    public String getResponseCharacterEncoding() {
        Object value = this.getAttribute("http.response.encoding");
        return value.toString();
    }
    
    public void setResponseCharacterEncoding(String characterEncoding) {
        this.attributes.put("http.response.encoding", characterEncoding);
    }

    /**
     * Gets the request character encoding that are supported by
     * default for this ServletContext.
     */
    public String getRequestCharacterEncoding() {
        return this.attributes.get("http.request.encoding").toString();
    }
    
    /**
     * Sets the request character encoding for this ServletContext.
     */
    public void setRequestCharacterEncoding(String characterEncoding) {
        this.attributes.put("http.request.encoding", characterEncoding);
    }

    /**
     * Sets the context initialization parameter with the 
     * given name and value on this ServletContext.
     */
    public boolean setInitParameter(String name, String value) {
        return getServletContext().setInitParameter(name, value);
    }

    /**
     * Returns the names of the context's initialization
     * parameters as an Enumeration of String objects, or
     * an empty Enumeration if the context has no
     * initialization parameters.
     */
    public Enumeration<String> getInitParameterNames() {
        return getServletContext().getInitParameterNames();
    }

    /**
     * Returns a String containing the value of the named
     * context-wide initialization parameter, or null if
     * the parameter does not exist.
     */
    public String getInitParameter(String name) {
        return getServletContext().getInitParameter(name);
    }

    /**
     * Returns a directory-like listing of all the paths to
     * resources within the web application whose longest sub
     * path matches the supplied path argument.
     */
    public Set<String> getResourcePaths(String path) {
        return getServletContext().getResourcePaths(path);
    }
}
