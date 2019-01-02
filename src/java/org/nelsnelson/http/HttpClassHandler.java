/*
 * HttpClassHandler.java
 *
 * Created on December 10, 2006, 10:26 PM
 */

package org.nelsnelson.http;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.nelsnelson.http.server.ServletContainer;
import org.nelsnelson.http.util.RefUtils;
import org.nelsnelson.http.util.RequestUtils;
import org.nelsnelson.toolbox.util.ClassUtils;

/**
 *
 * @author nelsnelson
 */
public class HttpClassHandler
     implements HttpRequestHandler
{
    private final List<String> validMethods = Arrays.asList(new String[] {
            "GET",
            "POST",
            "HEAD",
    });
    private final File classRoot;
    private final String classRootPackage;
    
    /** Creates a new instance of HttpClassHandler */
    public HttpClassHandler(final File classRoot, String classRootPackage) {
        super();
        this.classRoot = classRoot;
        this.classRootPackage = classRootPackage;
    }
    
    private static void sendError(ClassicHttpResponse response, int code,
        final String message)
        throws IOException
    {
        response.setCode(code);
        
        String error =
            "<html><body><h1>" +
            message + "<hr>" +
            ServletContainer.getProperty(ServletContainer.VERSION) +
            "</h1></body></html>";
        StringEntity body = new StringEntity(error, java.nio.charset.StandardCharsets.UTF_8);
        body.setContentType("text/html; charset=UTF-8");
        response.setEntity(body);
    }
    
    public void handle(final ClassicHttpRequest request, final ClassicHttpResponse response,
        final HttpContext context)
        throws HttpException, IOException
    {
        String method = request.getMethod().toUpperCase();
        
        if (!validMethods.contains(method)) {
            throw new MethodNotSupportedException(method + " method not supported"); 
        }
        String target = request.getRequestUri();
        List<File> files = null;
        List data = null;
        
        if (request instanceof ClassicHttpRequest) {
            ClassicHttpRequest enclosingRequest =
                (ClassicHttpRequest) request;
            HttpEntity entity = enclosingRequest.getEntity();
            byte[] entityContent = EntityUtils.toByteArray(entity);
            System.out.println("Incoming entity content (bytes): " + entityContent.length);
            
            files = RequestUtils.getPostedFiles(enclosingRequest, entityContent);
            data = null;
        }
        
        String encoding = java.nio.charset.StandardCharsets.UTF_8.name();
        File file = new File(this.classRoot, URLDecoder.decode(target, encoding)).getCanonicalFile();
        
        if (!file.toString().startsWith(this.classRoot.toString())) {
            // Uh-oh, it looks like some lamer is trying to take a peek
            // outside of our class root directory.
            sendError(response, HttpStatus.SC_FORBIDDEN, "Permission Denied.");
        }
        else if (!file.exists()) {
            // The file was not found.
            sendError(response, HttpStatus.SC_NOT_FOUND, "File Not Found.");
        }
        else {
            Class<?> clazz = ClassUtils.loadClass(file, classRootPackage);
            
            // Create a transient reference for easy disposal and tight
            // software-managed memory management.
            java.lang.ref.Reference<?> reference = RefUtils.getTransient(clazz);

            HttpConnection conn = null;
            Object o = null;
            //o = context.getAttribute(ExecutionContext.HTTP_CONNECTION);
            
            if (o instanceof HttpConnection) {
                conn = (HttpConnection) o;
            }
            
            //TODO
            //ServletUtils.execute(reference, request, response, conn);
            
            // Eliminate the instance of the requested class from memory.
            reference.clear();
        }
    }
}
