/*
 * RequestUtils.java
 *
 * Created on December 13, 2006, 2:53 AM
 */

package org.nelsnelson.http.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.nelsnelson.com.oreilly.servlet.MultipartRequest;
import org.nelsnelson.http.servlet.DefaultHttpServletRequest;

/**
 *
 * @author nelsnelson
 */
public class RequestUtils {
    private static int maxSizeInBytes = 10 * 1024 * 1024; // (10 MegaBytes)
    
    /** Creates a new instance of RequestUtils */
    public RequestUtils() {
        
    }
    
    public static FileItemFactory createDiskFileItemFactory() {
        return
            createDiskFileItemFactory(DiskFileItemFactory.
            DEFAULT_SIZE_THRESHOLD,
            new File(System.getProperty("java.io.tmpdir")));
    }
    
    public static FileItemFactory createDiskFileItemFactory(int sizeThreshold,
        java.io.File repository)
    {
        return new DiskFileItemFactory(sizeThreshold, repository);
    }
    
    public static List getPostedFiles(ClassicHttpRequest enclosingRequest) {
        List files = new ArrayList();
        FileUpload upload = new FileUpload();
        upload.setFileItemFactory(createDiskFileItemFactory());
        
        RequestContextFactory factory = RequestContextFactory.getInstance();
        factory.setRequest(enclosingRequest);
        RequestContext ctx = factory.createRequestContext();
        
        try {
            files = (List) upload.parseRequest(ctx);
        }
        catch (FileUploadException ex) {
            ex.printStackTrace();
        }
        
        return processDiskFileItems(files);
    }
    
    public static List<File> getPostedFiles(ClassicHttpRequest enclosingRequest,
        byte[] entityContent)
    {
        List<?> files = new ArrayList();
        FileUpload upload = new FileUpload();
        upload.setFileItemFactory(createDiskFileItemFactory());
        
        RequestContextFactory factory = RequestContextFactory.getInstance();
        factory.createRequestContext(enclosingRequest);
        factory.setEntityContent(entityContent);
        RequestContext ctx = factory.createRequestContext();
        
        try {
            files = (List<?>) upload.parseRequest(ctx);
        }
        catch (FileUploadBase.InvalidContentTypeException ex) {
            ex.printStackTrace();
        }
        catch (FileUploadException ex) {
            ex.printStackTrace();
        }
        
        return processDiskFileItems(files);
    }
    
    private static List processDiskFileItems(List fileItems) {
        List files = new ArrayList();
        
        for (int i = 0; i < fileItems.size(); i++) {
            Object item = fileItems.get(i);
            
            if (item instanceof DiskFileItem) {
                DiskFileItem fileItem = (DiskFileItem) item;
                
                try {
                    File file = new File(fileItem.getName());
                    
                    fileItem.write(file);
                    
                    files.add(file);
                    fileItem.delete();
                } 
                catch (Exception ex) {
                    //ex.printStackTrace();
                }
            }
        }
        
        return files;
    }

    public static HttpServletRequest getServletRequest(HttpRequest request, 
        HttpContext context, ServletContext servletContext) 
        throws HttpException, IOException 
    {
        return new DefaultHttpServletRequest(request, context, servletContext);
    }
    
    static class RequestContextFactory {
        private static RequestContextFactory instance = null;
        
        // to do: these need to be configurable using a configurator
        private static ClassicHttpRequest request = null;
        private static byte[] content = null;
        
        private RequestContextFactory() {
            
        }
        
        public static RequestContextFactory getInstance() {
            if (instance == null) {
                instance = new RequestContextFactory();
            }
            
            return instance;
        }
    
        /**
         * Returns a request context for an instance of the
         * <code>HttpEntityEnclosingRequest</code> class.
         *
         * This method creates a new instance of the <code>HttpRequestContext</code> class
         * which performs a transform on the request and the content of the
         * enclosed entity.
         *
         * The <code>RequestContext</code> class is related to Java's J2EE
         * <code>javax.servlet.http.HttpServletRequest</code>, and is a
         * completely different animal.
         */
        public RequestContext createRequestContext(ClassicHttpRequest request) {
            return new HttpRequestContext(request);
        }

        public static RequestContext createRequestContext(ClassicHttpRequest request,
            byte[] entityContent)
        {
            return new HttpRequestContext(request, entityContent);
        }

        public static RequestContext createRequestContext() {
            return createRequestContext(request, content);
        }

        public static void setRequest(ClassicHttpRequest enclosingRequest) {
            request = enclosingRequest;
        }

        public static void setEntityContent(byte[] entityContent) {
            content = entityContent;
        }
    }
    
    static class HttpRequestContext
        implements RequestContext
    {
        private ClassicHttpRequest request = null;
        private HttpEntity entity = null;
        private byte[] entityContent = null;
        
        public HttpRequestContext(ClassicHttpRequest request) {
            this.request = request;
            this.entity = request.getEntity();
        }
        
        public HttpRequestContext(ClassicHttpRequest request,
            byte[] entityContent)
        {
            this.request = request;
            this.entity = request.getEntity();
            this.entityContent = entityContent;
        }
        
        public String getCharacterEncoding() {
            if (entity == null) {
                return null;
            }
            
            return this.entity.getContentEncoding();
        }

        public String getContentType() {
            if (entity == null) {
                return null;
            }
            
            return entity.getContentType();
        }

        public int getContentLength() {
            return entity == null ? 0 : (int) entity.getContentLength();
        }

        public InputStream getInputStream() throws IOException {
            if (entity.isRepeatable()) {
                return entity.getContent();
            }
            else if (entityContent == null) {
                throw new IOException("Entity content is not repeatable. " +
                    "HttpRequestContext must be instantiated with byte " +
                    "array of entity content.");
            }
            else {
                return new ByteArrayInputStream(entityContent);
            }
        }
    }
    
    static class HttpContextTransformer {
        private ClassicHttpRequest request = null;
        
        public HttpContextTransformer(ClassicHttpRequest request) {
            this.request = request;
        }
        
        public RequestContext transform(byte[] entityContent) {
            return new HttpRequestContext(request, entityContent);
        }
    }
    
    static class HttpContextTransformerFactory {
        private ClassicHttpRequest request = null;
        
        public HttpContextTransformerFactory() {
            
        }
        
        public void setRequest(ClassicHttpRequest request) {
            this.request = request;
        }
        
        public HttpContextTransformer createHttpContextTransformer() {
            return new HttpContextTransformer(request);
        }
    }

    public static boolean isMultiPart(ServletRequest request) {
        boolean isMultiPart = false;
        
        String contentType = request.getContentType();
        System.out.println("ServletRequest.contentType=" + contentType);
        
        if (contentType.equals("multipart/form-data") || 
            contentType.startsWith("multipart/form-data" + "; ")) 
        {
            isMultiPart = true;
        }
        
        return isMultiPart;
    }

    public static MultipartRequest getMultiPartRequest(ServletRequest request) {
        MultipartRequest multipartRequest = null;
        
        try {
            multipartRequest = new MultipartRequest(request, "../", maxSizeInBytes);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return multipartRequest;
    }
}
