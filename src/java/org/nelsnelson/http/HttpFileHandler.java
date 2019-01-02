/*
 * HttpFileHandler.java
 *
 * Created on December 10, 2006, 10:26 PM
 */

package org.nelsnelson.http;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collections;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.nelsnelson.http.server.ServletContainer;
import org.nelsnelson.toolbox.util.FileUtils;

/**
 *
 * @author nelsnelson
 */
public class HttpFileHandler implements HttpRequestHandler {
    
    protected final String docRoot;
    
    /** Creates a new instance of HttpFileHandler */
    public HttpFileHandler(final String docRoot) {
        super();
        this.docRoot = docRoot;
    }
    
    protected static void sendFile(ClassicHttpResponse response, final File file) {
        response.setCode(HttpStatus.SC_OK);
        FileEntity body = new FileEntity(file, FileUtils.getContentType(file));
        
        response.setEntity(body);
        try {
            response.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected static void sendDirectoryListing(ClassicHttpResponse response,
        final File file, final String path)
    {
        final File[] files = file.listFiles();
        final String unixPath = path.length() == 0 ? "/" : path.replace('\\', '/');
        
        response.setCode(HttpStatus.SC_OK);
        String title = "Index of " + unixPath;
        String directoryIndex =
            "<html><head><title>" + title + "</title>" +
            "<link rel=\"shortcut icon\" href=\"/favicon.ico\" />" +
            "</head><body><h3>" + title + "</h3><p>\n" +
            "<pre>&nbsp;&nbsp;<a href=\"#\">Name</a>\n" +
            "<hr><a href=\"..\">Parent Directory</a>\n";

        for (File f : files) {
            String filename = f.getName();

            if (file.isDirectory()) {
                filename = filename + "/";
            }

            directoryIndex += "<a href=\"" + filename + "\">" + filename + "</a>\n";
        }

        directoryIndex += "<hr></pre>\n" +
            "<address>" + ServletContainer.getVersion() + "</address>" +
            "</body><html>";

        StringEntity body = new StringEntity(directoryIndex, java.nio.charset.Charset.forName("UTF-8"));
        body.setContentType("text/html; charset=UTF-8");
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

        StringEntity body = new StringEntity(error, java.nio.charset.Charset.forName("UTF-8"));
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
        String target = request.getRequestUri();

        byte[] entityContent = new byte[0];
        HttpEntity entity = request.getEntity();
        if (entity != null) {
            entityContent = EntityUtils.toByteArray(entity);
            System.out.println("Incoming entity content (bytes): " + entityContent.length);
        }
        
        String fileName = URLDecoder.decode(target, java.nio.charset.Charset.forName("UTF-8").name());
        File parent = new File(this.docRoot).getCanonicalFile();
        File file = new File(parent, fileName).getCanonicalFile();
        String path = file.getAbsolutePath();
        
        if (file.isDirectory()) {
            // Check to see if there is an index file in the directory.
            File indexFile = new File(file, ServletContainer.getDefaultIndexFile());
            
            if (indexFile.exists() && !indexFile.isDirectory()) {
                file = indexFile;
            }
        }
        
        if (!file.toString().startsWith(parent.toString())) {
            // Uh-oh, it looks like some lamer is trying to take a peek
            // outside of our web root directory.
            sendError(response, HttpStatus.SC_FORBIDDEN, "Permission Denied.");
        }
        else if (!file.exists()) {
            // The file was not found.
            sendError(response, HttpStatus.SC_NOT_FOUND, "File Not Found.");
        }
        else if (file.isDirectory()) {
            // print directory listing
            sendDirectoryListing(response, file, 
                path.substring(parent.toString().length()));
        }
        else {
            sendFile(response, file);
        }
    }
}
