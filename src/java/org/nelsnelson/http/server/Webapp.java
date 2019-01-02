package org.nelsnelson.http.server;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.hc.core5.http.ExceptionListener;
import org.nelsnelson.http.server.ServletContainer.ErrorHandler;
import org.nelsnelson.http.servlet.HttpServletContext;
import org.nelsnelson.toolbox.util.ClassUtils;
import org.nelsnelson.toolbox.util.JarUtils;

/**
 *
 * @author nelsnelson
 */
public class Webapp {
    private Object app = null;
    private String webRoot = null;
    private String servletRoot = null;
    private ServletContext context = null;
    private File jarFile = null;

    public Webapp() {
        this(new Object());
    }

    public Webapp(Object o) {
        this.app = o;
        
        // Set default properties
        getServletContext().setAttribute(HttpServletContext.INDEX_FILE, "index.html");
        getServletContext().setAttribute(HttpServletContext.DEFAULT_WEB_ROOT_DIR, "web/");
        getServletContext().setAttribute(HttpServletContext.DEFAULT_SERVLET_ROOT_DIR, "/" + ClassUtils.getPackagePath(app.getClass()));
    }

    public Webapp(Object o, String webRoot) {
        this(o);
        getServletContext().setAttribute(HttpServletContext.WEB_ROOT_DIR, webRoot);
        getServletContext().setAttribute(HttpServletContext.SERVLET_ROOT_DIR, "/" + ClassUtils.getPackagePath(app.getClass()));
    }

    public ServletContext getServletContext() {
        if (this.context == null) {
            this.context = new HttpServletContext();
        }

        return this.context;
    }

    public String getServletRoot() {
        if (servletRoot == null) {
            servletRoot = getDefaultServletRoot();
            
            if (isJarred()) {
                servletRoot = 
                    JarUtils.getResourcePath(app.getClass(), servletRoot);
            }
            else {
                servletRoot = "./" + servletRoot;
            }
        }
        
        return servletRoot;
    }
    
    public void setServletRoot(String servletRoot) {
        this.servletRoot = servletRoot;
    }
    
    public String getWebRoot() {
        if (webRoot == null) {
            Class<?> clazz = this.app.getClass();
            webRoot = getDefaultWebRoot();
            
            if (isJarred()) {
                webRoot = JarUtils.getResourcePath(clazz, webRoot);
            }
            else {
                webRoot = "./" + webRoot;
            }
        }
        
        return webRoot;
    }
    
    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }
    
    public String getDefaultServletRoot() {
        return getServletContext().getAttribute(HttpServletContext.DEFAULT_SERVLET_ROOT_DIR).toString();
    }
    
    public String getDefaultWebRoot() {
        return getServletContext().getAttribute(HttpServletContext.DEFAULT_WEB_ROOT_DIR).toString();
    }
    
    public String getDefaultIndexFile() {
        return getServletContext().getAttribute(HttpServletContext.INDEX_FILE).toString();
    }
    
    public boolean isJarred() {
        return getJarFile() != null;
    }
    
    public File getJarFile() {
        if (jarFile == null) {
            Class<?> clazz = this.app.getClass();
            jarFile = JarUtils.getJarFile(clazz);
        }
        
        return jarFile;
    }

    /**
     * Gets the class loader of the web application represented by this
     * ServletContext.
     */
    public ClassLoader getClassLoader() {
        return this.app.getClass().getClassLoader();
    }

    public String getClassPackage() {
        return ClassUtils.getClassPackage(this.app.getClass());
    }
}
