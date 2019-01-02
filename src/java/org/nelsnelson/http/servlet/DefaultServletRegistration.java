package org.nelsnelson.http.servlet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.HttpMethodConstraintElement;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity;

public class DefaultServletRegistration
    implements ServletRegistration, ServletRegistration.Dynamic
{
    private String name = null;
    private Servlet servlet = null;
    private Collection<String> mappings = null;
    private String runAsRole = null;
    private Map<String,String> initParameters = null;
    private boolean isAsyncSupported = false;
    private int loadOnStartup = -1;
    private MultipartConfigElement multipartConfig = null;
    private Set<String> servletSecurity = null;

    public DefaultServletRegistration(Servlet servlet) {
        this.name = "";
        this.servlet = servlet;
        this.mappings = new HashSet<String>();
        this.runAsRole = "";
        this.initParameters = new HashMap<String,String>();
        this.isAsyncSupported = false;

        String location = "";
        long maxFileSize = -1;
        long maxRequestSize = -1;
        int fileSizeThreshold = -1;

        this.multipartConfig = new MultipartConfigElement(
                location, maxFileSize, maxRequestSize, fileSizeThreshold);
        this.servletSecurity = new HashSet<String>();
    }
    
    public ServletRegistration.Dynamic dynamic() {
        return (ServletRegistration.Dynamic) this;
    }

    /**
     * Adds a servlet mapping with the given URL patterns for
     * the Servlet represented by this ServletRegistration.
     */
    public Set<String> addMapping(String... urlPatterns) {
        this.mappings = Arrays.asList(urlPatterns);
        
        return new HashSet(this.mappings);
    }

    /**
     * Gets the currently available mappings of the Servlet
     * represented by this ServletRegistration.
     */
    public Collection<String> getMappings() {
        return this.mappings;
    }

    /**
     * Gets the name of the runAs role of the Servlet represented
     * by this ServletRegistration.
     */
    public String getRunAsRole() {
        return this.runAsRole;
    }

    /**
     * Configures the Servlet or Filter represented by this dynamic
     * Registration as supporting asynchronous operations or not.
     */
    public void setAsyncSupported(boolean isAsyncSupported) {
        this.isAsyncSupported = isAsyncSupported;
    }

    /**
     * Sets the loadOnStartup priority on the Servlet represented
     * by this dynamic ServletRegistration.
     */
    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    /**
     * Sets the MultipartConfigElement to be applied to the mappings
     * defined for this ServletRegistration.
     */
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        this.multipartConfig = multipartConfig;
    }

    /**
     * Sets the name of the runAs role for this ServletRegistration.
     */
    public void setRunAsRole(String roleName) {
        this.runAsRole = roleName;
    }

    /**
     * Sets the ServletSecurityElement to be applied to the mappings
     * defined for this ServletRegistration.
     */
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        String[] rolesAllowed = constraint.getRolesAllowed();
        ServletSecurity.TransportGuarantee transportGuarantee = constraint.getTransportGuarantee();
        Collection<String> methodNames = constraint.getMethodNames();
        Collection<HttpMethodConstraintElement> httpMethodConstraints = constraint.getHttpMethodConstraints();
        return this.servletSecurity;
    }


    /**
     * Gets the fully qualified class name of the Servlet or
     * Filter that is represented by this Registration.
     */
    public String getClassName() {
        return this.getClass().getName();
    }

    /**
     * Gets the value of the initialization parameter with the
     * given name that will be used to initialize the Servlet or
     * Filter represented by this Registration object.
     */
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    /**
     * Gets an immutable (and possibly empty) Map containing the
     * currently available initialization parameters that will be
     * used to initialize the Servlet or Filter represented by
     * this Registration object.
     */
    public Map<String,String> getInitParameters() {
        return this.initParameters;
    }

    /**
     * Gets the name of the Servlet or Filter that is represented
     * by this Registration.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the initialization parameter with the given name and
     * value on the Servlet or Filter that is represented by this
     * Registration.
     */
    public boolean setInitParameter(String name, String value) {
        this.initParameters.put(name, value);
        return this.initParameters.containsKey(name);
    }

    /**
     * Sets the given initialization parameters on the Servlet or
     * Filter that is represented by this Registration.
     */
    public Set<String> setInitParameters(Map<String,String> initParameters) {
        this.initParameters = initParameters;
        
        return this.initParameters.keySet();
    }
}
