/*
 * HttpClassHandler.java
 *
 * Created on December 10, 2006, 10:26 PM
 */

package org.nelsnelson.http;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.nelsnelson.http.server.ServletContainer;
import org.nelsnelson.http.server.ServletContainerConfig;
import org.nelsnelson.http.servlet.ServletConfigurator;
import org.nelsnelson.http.servlet.ServletExecutor;
import org.nelsnelson.http.session.SessionManager;
import org.nelsnelson.http.util.ConnUtils;
import org.nelsnelson.http.util.RefUtils;
import org.nelsnelson.http.util.UriUtils;
import org.nelsnelson.toolbox.util.ClassUtils;
import org.nelsnelson.toolbox.util.FileUtils;

/**
 *
 * @author nelsnelson
 */
public class HttpServletHandler 
    extends HttpFileHandler 
    implements HttpRequestHandler
{
    
    protected final String webRoot;
    protected final String servletRoot;
    protected static ContentType mimeType = FileUtils.getContentType(FileUtils.DEFAULT_MIME_TYPE);
    
    /** Creates a new instance of HttpClassHandler */
    public HttpServletHandler(final String webRoot, final String servletRoot) {
        super(webRoot);
        this.webRoot = webRoot;
        this.servletRoot = servletRoot;
    }
    
    protected static void sendResult(ClassicHttpResponse response, final String result) {
        sendResult(response, result, mimeType);
    }
    
    protected static void sendResult(ClassicHttpResponse response, final String result, final ContentType contentType) {
        response.setCode(HttpStatus.SC_OK);
        StringEntity body = new StringEntity(result);
        if (mimeType != null) {
            body.setContentType(contentType.getMimeType() + "; " + contentType.getCharset());
        }
        else {
            body.setContentType("text/html; charset=UTF-8");
        }
        response.setEntity(body);
    }
    
    protected static void sendError(ClassicHttpResponse response, final int code,
        final String message)
        throws IOException
    {
        response.setCode(code);
        String error =
            "<html><body><h1>Http Error: " + code + "</h1><p>\n" +
        message + "<p>\n" +
        "<pre><hr></pre>\n" +
        "<address>" + ServletContainer.getVersion() + "</address>" +
        "</body></html>";
        StringEntity body = new StringEntity(error);
        body.setContentType("text/html; charset=UTF-8");
        response.setEntity(body);
    }
    
    public void handle(final ClassicHttpRequest request, final ClassicHttpResponse response,
        final HttpContext context)
        throws HttpException, IOException
    {
        String method = request.getMethod().toUpperCase();
        
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported"); 
        }
        String target = UriUtils.getPath(request.getRequestUri());
        
        File parent = new File(this.servletRoot).getCanonicalFile();
        File file = new File(parent, URLDecoder.decode(target, java.nio.charset.StandardCharsets.UTF_8.name())).getCanonicalFile();
        
        if (!file.toString().startsWith(parent.toString())) {
            // Uh-oh, it looks like some lamer is trying to take a peek
            // outside of our class root directory.
            sendError(response, HttpStatus.SC_FORBIDDEN, "Permission Denied.");
        }
        else if (!file.exists()) {
            // The file was not found.
            sendError(response, HttpStatus.SC_NOT_FOUND, "File Not Found.");
        }
        else {
            String packageSpec = ServletContainer.get(webRoot).getClassPackage();
            Class<?> clazz = ClassUtils.loadClass(file, packageSpec);
            
            java.lang.ref.Reference<?> reference = RefUtils.getTransient(clazz);
            
            if (reference.get() instanceof Servlet) {
                HttpConnection conn = ConnUtils.getConnection(context);
                
                ServletContainerConfig serverConfig = 
                    ServletContainerConfig.getInstance(context);
                
                // Servlet configuration
                ServletConfigurator servletConfigurator = 
                    new ServletConfigurator(conn, context);
                
                Servlet servlet = (Servlet) reference.get();
                servlet = servletConfigurator.configure(webRoot, servletRoot, servlet);
                ServletContext servletContext = 
                    servlet.getServletConfig().getServletContext();
                
                // Session manager initialization
                SessionManager.maybeInit(request, servletContext);
                
                // Servlet execution
                ServletExecutor executor = new ServletExecutor();
                
                try {
                    executor.execute(servlet, request, response, context);
                }
                catch (HttpException ex) {
                    ex.printStackTrace();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                } 
                catch (ServletException ex) {
                    ex.printStackTrace();
                }
                catch (NullPointerException ex) {
                    String message = 
                        "Caught NullPointerException from ServletExecutor. Servlet " + 
                        servlet.getClass().getName() + 
                        " is probably malformed.";
                    
                    System.out.println(message);
                    ex.printStackTrace();
                }
            }
            else {
                sendResult(response, reference.get().toString());
            }
        }
    }
}
