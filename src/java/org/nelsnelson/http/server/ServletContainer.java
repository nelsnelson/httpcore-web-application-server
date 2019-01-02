/*
 * ServletContainer.java
 */
package org.nelsnelson.http.server;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.SocketConfig.Builder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.nelsnelson.http.HttpFileHandler;
import org.nelsnelson.http.HttpServletHandler;
import org.nelsnelson.http.session.SessionManager;
import org.nelsnelson.toolbox.util.ClassUtils;
import org.nelsnelson.toolbox.util.JarUtils;
import org.nelsnelson.toolbox.util.Options;

/**
 * 
 * @author nelsnelson
 * 
 */
public class ServletContainer implements Service {
    private static Map<String, Webapp> webapps = null;
    private ServerBootstrap bootstrap = null;
    private HttpServer server = null;
    public final int port = 6500;
    private AbstractService service;
    private ErrorHandler errorHandler;
    
    public ServletContainer() {
        this(new Webapp(new Object()));
    }
    
    public ServletContainer(Webapp webapp) {
        this(new Webapp[] { webapp });
    }
    
    public ServletContainer(Webapp[] webapps) {
        SSLContext sslContext = null;
        
        int bufferSize = Double.valueOf(Math.pow(2,16)).intValue();
        Builder builder = SocketConfig.custom();
        builder.setSoTimeout(15, TimeUnit.SECONDS);
        builder.setRcvBufSize(bufferSize);
        builder.setSndBufSize(bufferSize);
        builder.setSoKeepAlive(false);
        builder.setTcpNoDelay(true);
        
        SocketConfig socketConfig = builder.build();
        
        this.bootstrap = ServerBootstrap.bootstrap();
        this.bootstrap.setListenerPort(port);
        // TODO Remove
        //this.bootstrap.setConnectionReuseStrategy(new UnwiseConnReuseStrategy());
        this.bootstrap.setSocketConfig(socketConfig);
        this.bootstrap.setSslContext(sslContext);
        
        for (Webapp webapp : webapps) {
            ServletContainer.add(webapp);
        }
        
        init();
    }
    
    public ServletContainer(String webRoot) {
        this(new Webapp(new Object(), webRoot));
    }
    
    public ServletContainer(File webappDir) {
        // TODO Implement
    }
    
    /**
     * TODO Remove
     */
    protected class UnwiseConnReuseStrategy implements ConnectionReuseStrategy {
        public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
            return false;
        }
    }
    
    protected class ErrorHandler implements ExceptionListener {
        private Log service = null;
        public ErrorHandler(Log service) {
            this.service = service;
        }

        public void onError(final Exception ex) {
            for (StackTraceElement el : ex.getStackTrace()) {
                service.log(java.util.logging.Level.SEVERE, el.toString());
            }
        }

        public void onError(final HttpConnection conn, final Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                service.log(java.util.logging.Level.SEVERE, "Connection timed out");
                ex.printStackTrace();
            } else if (ex instanceof ConnectionClosedException) {
                service.log(java.util.logging.Level.SEVERE, ex.getMessage());
            } else {
                for (StackTraceElement el : ex.getStackTrace()) {
                    service.log(java.util.logging.Level.SEVERE, el.toString());
                }
            }
        }
    }
    
    protected void init() {
        Collection<Webapp> webapps = getWebapps().values();
        
        for (Webapp webapp : webapps) {
            String webRoot = webapp.getWebRoot();
            String servletRoot = webapp.getServletRoot();
            this.bootstrap = this.bootstrap.setExceptionListener(this.getErrorHandler());
            
            maybeExplodeWebapp(webapp);
            registerHandler("*.class", new HttpServletHandler(webRoot, servletRoot));
            registerHandler("*", new HttpFileHandler(webRoot));
        }

        this.setServer(this.bootstrap.create());
    }
    
    public void start() {
        try {
            this.server.start();
            System.out.println("Listening on port " + this.port);

            this.server.awaitTermination(TimeValue.MAX_VALUE);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void stop() {
        String deployPath = ServletContainer.getProperty(
                ServletContainer.TEMP_DEPLOY_DIR);
        new File(deployPath).delete();

        this.server.close();
    }
    
    private void registerHandler(String extension, HttpRequestHandler handler) {
        if (this.bootstrap != null) {
            this.bootstrap.register(extension, handler);
        }
    }
    
    public static Webapp add(Webapp webapp) {
        String webappPrefix = "";
        
        if (Options.contains(MULTIPLE_APPS)) {
            webappPrefix = ClassUtils.getSimpleName(webapp.getClass());
        }
        
        return getWebapps().put(webappPrefix, webapp);
    }
    
    public static Webapp get(String webRoot) {
        if (Options.contains(MULTIPLE_APPS)) {
            return getWebapps().get(webRoot);
        }
        else {
            return getWebapps().values().iterator().next();
        }
    }

    public static Webapp getWebappBySessionKey(String sessionId) {
        Webapp webappBySessionKey = null;

        for (Webapp webapp : getWebapps().values()) {
            ServletContext servletContext = webapp.getServletContext();
            HttpSession session = SessionManager.getSession(servletContext, sessionId);
            if (session != null) {
                webappBySessionKey = webapp;
                break;
            }
        }

        return webappBySessionKey;
    }

    public static Map<String, Webapp> getWebapps() {
        if (webapps == null) {
            webapps = new HashMap<String, Webapp>();
        }
        
        return webapps;
    }
    
    @SuppressWarnings("unused")
    private void maybeExplodeWebapps() {
        for (Webapp webapp : getWebapps().values()) {
            maybeExplodeWebapp(webapp);
        }
    }
    
    private void maybeExplodeWebapp(Webapp webapp) {
        if (!webapp.isJarred()) {
            return;
        }
        
        if (Options.contains(DEPLOY) || Options.contains(REDEPLOY)) {
            try {
                File destinationDirectory = 
                    new File(ServletContainer.getProperty(ServletContainer.TEMP_DEPLOY_DIR));
                
                if (destinationDirectory.exists() && Options.contains(REDEPLOY)) {
                    destinationDirectory.mkdirs();
                    
                    File jarFile = webapp.getJarFile();
                    
                    JarUtils.extractJar(jarFile, destinationDirectory);
                }
                
                webapp.setWebRoot(new File(destinationDirectory.toString(), 
                    webapp.getDefaultWebRoot()).toString());
                
                webapp.setServletRoot(new File(destinationDirectory.toString(), 
                    webapp.getDefaultServletRoot()).toString());
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static String getVersion() {
        return getProperty(VERSION);
    }

    public static String getDomain() {
        return getProperty(DOMAIN);
    }
    
    public static String getDefaultIndexFile() {
        Webapp webapp = getWebapps().values().iterator().next();
        return webapp.getDefaultIndexFile();
    }
    
    public HttpServer getServer() {
        return server;
    }

    public void setServer(HttpServer server) {
        this.server = server;
    }

    public Service getService() {
        if (this.service == null) {
            this.service = new ContainerService(this);
        }
        return (Service) this.service;
    }

    public Log getLog() {
        return (Log) this.getService();
    }

    public ExceptionListener getErrorHandler() {
        if (this.errorHandler == null) {
            this.errorHandler = new ErrorHandler(this.getLog());
        }
        return this.errorHandler;
    }
    
    /** 
     * Gets the properties for this application
     * 
     * @param property    String is the given key for getting the mapped property value
     * @return String property
     */
    public static String getProperty(String property) {
        return getProperties().getProperty(property);
    }
    
    public static Properties getProperties() {
        return properties;
    }

    public static final String VERSION = "version";
    public static final String DOMAIN = "domain";
    public static final String PREFIX = "prefix";
    public static final String DEPLOY = "deploy";
    public static final String REDEPLOY = "redeploy";
    public static final String MULTIPLE_APPS = "multiple-apps";
    public static final String TEMP_DEPLOY_DIR = "temporary.deploy.directory";
    public static final String DEFAULT_SERVICE_ROOT = "default.service.root";
    
    public static final Properties properties = new Properties();
    
    static {
        properties.put(VERSION, ClassUtils.getSimpleName(ServletContainer.class) + "/1.0 (" +
            System.getProperty("os.name") + ") " + 
            "using Apache httpcore-5.0-beta6");
        properties.put(DOMAIN, "localhost");
        properties.put(TEMP_DEPLOY_DIR, "./tmp/deploy");
    }
    
    public static void main(String[] args) {
        new ServletContainer().getService().start();
    }
}
