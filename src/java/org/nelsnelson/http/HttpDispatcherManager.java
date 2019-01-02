/*
 * HttpResponseDispatcherManager.java
 *
 * Created on January 3, 2007, 12:32 PM
 */

package org.nelsnelson.http;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.impl.nio.DefaultHttpRequestFactory;
import org.apache.hc.core5.http.io.HttpServerConnection;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

/**
 *
 * @author nelsnelson
 */
public class HttpDispatcherManager {
    private static final int PROCESSOR = 0;
    private static final int SERVICE = 1;
    private static final int CONNECTION = 2;
    
    private static HttpDispatcherManager instance = null;
    private static Map services = null;
    
    /**
     * Creates a new instance of HttpDispatcherManager
     */
    private HttpDispatcherManager() {
        services = new LinkedHashMap();
    }
    
    private static synchronized HttpDispatcherManager getInstance() {
        if (instance == null) {
            instance = new HttpDispatcherManager();
        }
        
        return instance;
    }
    
    private static HttpService getService() {
        HttpService service = null;
        
        Object item1 = getInstance().services.get(Thread.currentThread());
        
        if (item1 instanceof List) {
            List servicesList = (List) item1;
            
            Object item2 = servicesList.get(SERVICE);
            
            if (item2 instanceof HttpService) {
                service = (HttpService) item2;
            }
        }
        
        return service;
    }
    
    private static HttpServerConnection getConnection() {
        HttpServerConnection connection = null;
        
        Object item1 = getInstance().services.get(Thread.currentThread());
        
        if (item1 instanceof List) {
            List servicesList = (List) item1;
            
            Object item2 = servicesList.get(CONNECTION);
            
            if (item2 instanceof HttpService) {
                connection = (HttpServerConnection) item2;
            }
        }
        
        return connection;
    }
    
    public static synchronized void registerService(HttpService service, 
        HttpServerConnection connection)
    {
        List servicesList = new ArrayList();
        servicesList.add(service);
        servicesList.add(connection);
        
        getInstance().services.put(Thread.currentThread(), servicesList);
    }
    
    /**
     * This is for executing a resource fetch in an external domain.
     * 
     * @param resourcePath
     */
    public static synchronized void getRequestDispatcher(String resourcePath) {
        HttpService httpservice = getService();
        HttpServerConnection conn = getConnection();
        
        HttpContext context = new BasicHttpContext(null);
        
        try {
            DefaultHttpRequestFactory requestFactory = new DefaultHttpRequestFactory();
            HttpRequest request = requestFactory.newHttpRequest("GET", resourcePath);
            context.setAttribute(HttpCoreContext.HTTP_REQUEST, request);
            httpservice.handleRequest(conn, context);
        }
        catch (ConnectionClosedException ex) {
            System.err.println("Client closed connection");
        }
        catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
        }
        catch (HttpException ex) {
            System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
        }
        finally {
            try {
                conn.close();
            }
            catch (IOException ignore) {
                
            }
        }
    }
    
    public static synchronized void getRequestDispatcher(URL resourceUrl) {
        
    }
    
    public static synchronized void getResponseDispatcher(String resourcePath) {
        
    }
    
    public static synchronized void getResponseDispatcher(URL resourceUrl) {
        
    }
}
