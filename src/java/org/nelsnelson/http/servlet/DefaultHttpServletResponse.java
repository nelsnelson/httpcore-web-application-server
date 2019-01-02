/*
 * DefaultHttpServletResponse.java
 *
 * Created on January 12, 2007, 2:50 PM
 */

package org.nelsnelson.http.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.nelsnelson.http.HttpHeaders;
import org.nelsnelson.http.entity.ServletResponseEntity;

/**
 *
 * @author nelsnelson
 */
public class DefaultHttpServletResponse
    implements HttpServletResponse//, HttpResponse
{
    private ClassicHttpResponse response = null;
    private HttpContext context = null;
    private ServletRequest servletRequest = null;
    private ServletContext servletContext = null;
    private PrintWriter writer = null;
    private ServletOutputStream out = null;
    private Map<String,String> headers = Collections.emptyMap();
    private List<Cookie> cookies = Collections.emptyList();
    
    /** Creates a new instance of DefaultHttpServletResponse */
    public DefaultHttpServletResponse(ClassicHttpResponse response, 
        HttpContext context, ServletRequest servletRequest, 
        ServletContext servletContext) 
    {
        this.servletRequest = servletRequest;
        this.response = response;
        this.context = context;
        this.servletContext = servletContext;
        
        setEntity(new ServletResponseEntity());
    }
    
    public void addCookie(Cookie cookie) {
        if (cookies == null || cookies == Collections.EMPTY_LIST) {
            cookies = new ArrayList<Cookie>();
        }
        
        cookies.add(cookie);
    }

    public void addHeader(String name, String value) {
        if (this.headers == null || headers == Collections.EMPTY_MAP) {
            this.headers = new HashMap<String,String>();
        }
        
        response.addHeader(name, value);
    }

    public void addIntHeader(String name, int value) {
        addHeader(name, Integer.toString(value));
    }
    
    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }
    
    public String encodeRedirectURL(String url) {
        return url;
    }
    
    public String encodeRedirectUrl(String url) {
        return url;
    }
    
    public String encodeURL(String url) {
        return url;
    }
    
    public String encodeUrl(String url) {
        return url;
    }
    
    public void sendError(int errorCode) throws IOException {
        sendError(errorCode, "");
    }
    
    public void sendError(int errorCode, String errorMessage) throws IOException {
        String output = errorCode + " " + errorMessage;
        
        if (servletContext != null && servletRequest != null) {
            RequestDispatcher rd = 
                servletContext.getRequestDispatcher(null);
            
            if (rd != null) {
                try {
                    rd.forward(servletRequest, this);
                    return;
                }
                catch (IllegalStateException ex) {
                    throw ex;
                }
                catch (IOException ex) {
                    throw ex;
                }
                catch (Throwable ex) {
                    return;
                }
            }
        }
        setContentLength(output.getBytes(getCharacterEncoding()).length);
        Writer out = getWriter();
        out.write(output);
        out.flush();
    }
    
    public void sendRedirect(String url) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException();
        }
        
        setHeader(HttpHeaders.LOCATION, url);
        setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    }
    
    /**
     * Adds a response header with the given name and date-value.
     */
    public void addDateHeader(String name, long value) {
        this.setDateHeader(name, value);
    }

    /**
     * Sets a response header with the given name and date-value.
     */
    public void setDateHeader(String name, long value) {
        DateFormat df = 
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        
        this.setHeader(name, df.format(new Date(value)));
    }
    
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    public void setIntHeader(String name, int value) {
        setHeader(name, Integer.toString(value));
    }

    public void setStatus(int code) {
        response.setCode(code);
    }

    public void setStatus(int code, String reason) {
        response.setCode(code);
        response.setReasonPhrase(reason);
    }

    public void flushBuffer() throws IOException {
        ((ServletResponseEntity) getEntity()).flushBuffer();
    }

    public int getBufferSize() {
        return ((ServletResponseEntity) getEntity()).getBufferSize();
    }

    public String getCharacterEncoding() {
        ServletResponseEntity entity = (ServletResponseEntity) getEntity();
        
        String contentEncoding = entity.getContentEncoding();
        
        String contentEncodingHeader = java.nio.charset.StandardCharsets.UTF_8.name();
        
        if (contentEncoding != null) {
            contentEncodingHeader = contentEncoding;
        }
        
        return contentEncodingHeader;
    }

    public String getContentType() {
        ServletResponseEntity entity = (ServletResponseEntity) getEntity();
        
        String contentType = entity.getContentType();
        
        String contentTypeHeader = "text/html";
        
        if (contentType != null) {
            contentTypeHeader = contentType;
        }
        
        return contentTypeHeader;
    }

    public Locale getLocale() {
        return response.getLocale();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("OutputStream is in use");
        }
        
        if (out == null) {
            out = ((ServletResponseEntity) getEntity()).getOutputStream();
        }
        
        return out;
    }

    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(getOutputStream());
        }
        
        return writer;
    }

    public boolean isCommitted() {
        Header contentLengthHeader = getFirstHeader(HttpHeaders.CONTENT_LENGTH);
        int contentLength = Integer.parseInt(contentLengthHeader.getValue());
        int bufferSize = ((ServletResponseEntity) getEntity()).getBufferSize();
        return bufferSize >= contentLength;
    }

    public void reset() {
        resetBuffer();
        setStatus(SC_OK);
        headers.clear();
        cookies.clear();
    }

    public void resetBuffer() {
        ((ServletResponseEntity) getEntity()).resetBuffer();
    }

    public void setBufferSize(int size) {
        ((ServletResponseEntity) getEntity()).setBufferSize(size);
    }

    public void setCharacterEncoding(String ceString) {
        ((ServletResponseEntity) getEntity()).setContentEncoding(ceString);
    }

    public void setContentLength(int len) {
        ((ServletResponseEntity) getEntity()).setContentLength((long) len);
        setIntHeader(HttpHeaders.CONTENT_LENGTH, len);
    }

    public void setContentType(String contentType) {
        ((ServletResponseEntity) getEntity()).setContentType(contentType);
        setHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }

    public void setLocale(Locale loc) {
        response.setLocale(loc);
    }

    public HttpEntity getEntity() {
        return response.getEntity();
    }

    public StatusLine getStatusLine() {
        return new StatusLine(response.getVersion(), response.getCode(), response.getReasonPhrase());
    }

    public void setEntity(HttpEntity entity) {
        response.setEntity(entity);
    }

    public void setReasonPhrase(String reason) throws IllegalStateException {
        response.setReasonPhrase(reason);
    }

    public void setStatusCode(int code) throws IllegalStateException {
        response.setCode(code);
    }

    public void setStatusLine(StatusLine statusline) {
        response.setReasonPhrase(statusline.getReasonPhrase());
    }

    public void setStatusLine(HttpVersion version, int code) {
        ProtocolVersion protocolVersion = new ProtocolVersion(version.getProtocol(), version.getMajor(), version.getMinor());
        response.setVersion(protocolVersion);
        response.setCode(code);
    }

    public void setStatusLine(HttpVersion version, int code, String reason) {
        ProtocolVersion protocolVersion = new ProtocolVersion(version.getProtocol(), version.getMajor(), version.getMinor());
        response.setVersion(protocolVersion);
        response.setCode(code);
        response.setReasonPhrase(reason);
    }

    public void addHeader(Header header) {
        response.addHeader(header);
    }

    public Header[] getAllHeaders() {
        return response.getHeaders();
    }
    
    public Header getFirstHeader(String name) {
        return response.getFirstHeader(name);
    }

    /**
     * Gets the value of the response header with the given name.
     */
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    /**
     * Gets the names of the headers of this response.
     */
    public Collection<String> getHeaderNames() {
        return this.headers.keySet();
    }

    /**
     * Gets the values of the response header with the given name.
     */
    public Collection<String> getHeaders(String name) {
        List<String> headerValues = new ArrayList<String>();
        Header[] headers = this.response.getHeaders(name);
        for (Header header : headers) {
            headerValues.add(header.getValue());
        }
        return headerValues;
    }

    /**
     * Gets the current status code of this response.
     */
    public int getStatus() {
        //return this.response.getCode();
        return 0; // TODO FIXME
    }

    public HttpVersion getHttpVersion() {
        // TODO Test
        ProtocolVersion protocolVersion = response.getVersion();
        int major = protocolVersion.getMajor();
        int minor = protocolVersion.getMinor();
        return new HttpVersion(major, minor);
    }

    public ProtocolVersion getProtocolVersion() {
        // TODO Test
        return response.getVersion();
    }
    
    public Header getLastHeader(String name) {
        return response.getLastHeader(name);
    }
    
    public Iterator<Header> headerIterator() {
        return response.headerIterator();
    }

    public Iterator<Header> headerIterator(String header) {
        // TODO Auto-generated method stub
        return response.headerIterator(header);
    }
    
    public void removeHeader(Header header) {
        response.removeHeader(header);
    }
    
    public void removeHeaders(String header) {
        response.removeHeaders(header);
    }

    /**
     * Sets the length of the content body in the response. In HTTP
     * servlets, this method sets the HTTP Content-Length header.
     */
    public void setContentLengthLong(long len) {
        this.headers.put("Content-length", String.valueOf(len));
    }
    
    public void setHeader(Header header) {
        response.setHeader(header);
    }
    
    public void setHeaders(Header[] headers) {
        response.setHeaders(headers);
    }

    public void setStatusLine(ProtocolVersion ver, int code) {
        // TODO Auto-generated method stub
        
    }

    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        // TODO Auto-generated method stub
        
    }
}
