package org.nelsnelson.http.servlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;

public class DefaultFilterRegistration
    implements FilterRegistration, FilterRegistration.Dynamic
{
    private Filter filter = null;
    private boolean isAsyncSupported = false;
    private Map<String,String> initParameters = null;
    private List<String> urlPatternMappings = null;
    private List<String> servletNameMappings = null;

    public DefaultFilterRegistration(Filter filter) {
        this.filter = filter;
        this.initParameters = new HashMap<String,String>();
        this.urlPatternMappings = new ArrayList<String>();
        this.servletNameMappings = new ArrayList<String>();
    }

    public FilterRegistration.Dynamic dynamic() {
        return (FilterRegistration.Dynamic) this;
    }

    /**
     * Configures the Servlet or Filter represented by this dynamic
     * Registration as supporting asynchronous operations or not.
     */
    public void setAsyncSupported(boolean isAsyncSupported) {
        this.isAsyncSupported = isAsyncSupported;
    }

    /**
     * Gets the name of the Servlet or Filter that is represented by
     * this Registration.
     * 
     * @return String
     */
    public String getName() {
        return this.filter.toString();
    }

    /**
     * Gets the fully qualified class name of the Servlet or
     * Filter that is represented by this Registration.
     * 
     * @return String
     */
    public String getClassName() {
        return this.getClass().getName();
    }

    /**
     * Gets the value of the initialization parameter with the given name
     * that will be used to initialize the Servlet or Filter represented
     * by this Registration object.
     */
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    /**
     * Sets the initialization parameter with the given name and value on
     * the Servlet or Filter that is represented by this Registration.
     */
    public boolean setInitParameter(String name, String value) {
        this.initParameters.put(name, value);
        return this.initParameters.containsKey(name);
    }

    /**
     * Gets an immutable (and possibly empty) Map containing the currently
     * available initialization parameters that will be used to initialize
     * the Servlet or Filter represented by this Registration object.
     */
    public Map<String,String> getInitParameters() {
        return this.initParameters;
    }

    /**
     * Sets the given initialization parameters on the Servlet or Filter
     * that is represented by this Registration.
     */
    public Set<String> setInitParameters(Map<String,String> initParameters) {
        this.initParameters = initParameters;
        
        return this.initParameters.keySet();
    }

    /**
     * Adds a filter mapping with the given servlet names and dispatcher
     * types for the Filter represented by this FilterRegistration.
     */
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames)
        throws IllegalArgumentException, IllegalStateException
    {
        // TODO Implement
    }
    
    /**
     * Adds a filter mapping with the given url patterns and dispatcher
     * types for the Filter represented by this FilterRegistration.
     */
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        // TODO Implement
    }

    /**
     * Gets the currently available URL pattern mappings of the
     * Filter represented by this FilterRegistration.
     */
    public Collection<String> getUrlPatternMappings() {
        return this.urlPatternMappings;
    }

    /**
     * Gets the currently available servlet name mappings of the
     * Filter represented by this FilterRegistration.
     */
    public Collection<String> getServletNameMappings() {
        return this.servletNameMappings;
    }
}
