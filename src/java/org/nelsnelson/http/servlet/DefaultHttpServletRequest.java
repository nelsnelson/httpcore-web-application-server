/*
 * DefaultHttpServletRequest.java
 *
 * Created on January 12, 2007, 2:50 PM
 */

package org.nelsnelson.http.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.nelsnelson.com.oreilly.servlet.MultipartRequest;
import org.nelsnelson.http.HttpHeaders;
import org.nelsnelson.http.entity.ServletRequestEntity;
import org.nelsnelson.http.server.ServletContainerConfig;
import org.nelsnelson.http.session.SessionFactory;
import org.nelsnelson.http.session.SessionManager;
import org.nelsnelson.http.util.ParameterUtils;
import org.nelsnelson.http.util.QueryUtils;
import org.nelsnelson.http.util.RequestUtils;

/**
 *
 * @author nelsnelson
 */
public class DefaultHttpServletRequest
    implements HttpServletRequest//, HttpRequest
{
    private HttpRequest request = null;
    private HttpContext context = null;
    private ServletContext servletContext = null;
    private ServletContainerConfig serverConfig = null;
    private BufferedReader reader = null;
    private ServletInputStream in = null;
    private HttpEntity entity = null;
    private String requestMethod = null;
    private String requestUri = null;
    private String queryString = null;
    private Map parameters = Collections.EMPTY_MAP;
    private Enumeration<String> headerNames = null;
    private String characterEncoding = "";
    private Cookie[] cookies = null;
    
    /** Creates a new instance of DefaultHttpServletRequest */
    public DefaultHttpServletRequest(HttpRequest request, HttpContext context, ServletContext servletContext) {
        this.request = request;
        this.context = context;
        this.servletContext = servletContext;

        requestMethod = request.getMethod();
        requestUri = request.getRequestUri();
        
        // this should only be an HttpEntityEnclosingRequest if the request 
        // has a POST command
        if (request instanceof ClassicHttpRequest) {
            ClassicHttpRequest enclosingRequest = 
                (ClassicHttpRequest) request;
            
            entity = new ServletRequestEntity(enclosingRequest.getEntity());
        }
        
        serverConfig = (ServletContainerConfig) context.getAttribute(ServletContainerConfig.SERVER_CONFIG);
        
        // Initialize active sessions used by requester
        SessionManager.manageSessionCookies(this, getCookies());
    }
    
    /**
     * Use the container login mechanism configured for the ServletContext
     * to authenticate the user making this request.
     */
    public boolean authenticate(HttpServletResponse response) {
        // TODO Implement
        return true;
    }

    /**
     * Change the session id of the current session associated with this
     * request and return the new session id.
     */
    public String changeSessionId() {
        String sessionId = null;

        // TODO Implement

        return sessionId;
    }
    
    /**
     * Gets the AsyncContext that was created or reinitialized by the
     * most recent invocation of startAsync() or
     * startAsync(ServletRequest,ServletResponse) on this request.
     */
    public AsyncContext getAsyncContext() {
        AsyncContext asyncContext = null;
        
        // TODO Implement
        
        return asyncContext;
    }

    /**
     * Returns the name of the authentication scheme used to protect the
     * servlet.
     */
    public String getAuthType() {
        String authType = null;
        
        // TODO Implement
        
        return authType;
    }

    /**
     * Returns the portion of the request URI that indicates the context
     * of the request.
     */
    public String getContextPath() {
        String contextPath = null;
        
        // TODO Implement
        
        return contextPath;
    }

    /**
     * Gets the dispatcher type of this request.
     */
    public DispatcherType getDispatcherType() {
        DispatcherType dispatcherType = null;
        
        // TODO Implement
        
        return dispatcherType;
    }

    public HttpEntity getEntity() {
        return this.entity;
    }

    public Cookie[] getCookies() {
        if (this.cookies == null) {
            Header[] headers = this.request.getHeaders(HttpHeaders.COOKIE);
            this.cookies = CookieHandler.getInstance().getCookiesFromHeaders(headers);
        }
        
        return this.cookies;
    }

    public long getDateHeader(String name) {
        long date = 0l;
        String header = this.getHeader(name);
        
        try {
            DateFormat format = 
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            Date d = format.parse(header);
            date = d.getTime();
        }
        catch (java.text.ParseException pe) {
            throw new IllegalArgumentException();
        }
        
        return date;
    }
    
    public String getHeader(String name) {
        String header = "";
        
        Header[] headers = request.getHeaders(name);
        
        if (headers != null && headers.length > 0) {
            header = headers[0].getValue();
        }
        
        return header;
    }
    
    public Enumeration<String> getHeaders(String name) {
        List<String> headers = new ArrayList<String>();
        for (Header header : this.request.getHeaders(name)) {
            headers.add(header.toString());
        }
        return (Enumeration<String>) Collections.enumeration(headers);
    }
    
    public Enumeration<String> getHeaderNames() {
        if (this.headerNames == null) {
            Header[] headers = request.getHeaders();
            
            List<String> names = new ArrayList<String>();
            
            for (int i = 0; i < headers.length; i++) {
                Header header = headers[i];
                
                names.add(header.getName());
            }
            
            headerNames = Collections.enumeration(names);
        }
        
        return headerNames;
    }
    
    public int getIntHeader(String headerName) {
        return Integer.parseInt(getHeader(headerName));
    }
    
    public String getMethod() {
        return this.requestMethod;
    }

    /**
     * Gets the Part with the given name.
     */
    public Part getPart(String name) {
        return null;
    }
    
    /**
     * Gets all the Part components of this request, provided that it is
     * of type multipart/form-data.
     */
    public Collection<Part> getParts() {
        return null;
    }

    public String getPathInfo() {
        return null;
    }
    
    public String getPathTranslated() {
        return null;
    }
    
    public String getQueryString() {
        String uri = this.requestUri;
        
        if (uri == null) {
            queryString = "";
        }
        else if (queryString == null) {
            queryString = QueryUtils.getQuery(uri);
            
            if (queryString == null) {
                queryString = "";
            }
        }
        
        return queryString;
    }
    
    public String getRemoteUser() {
        return null;
    }
    
    public boolean isUserInRole(String string) {
        return false;
    }
    
    public Principal getUserPrincipal() {
        return null;
    }
    
    public String getRequestedSessionId() {
        String actualSessionId = 
            (String) SessionManager.getSessionInfo(this).
            getRequestedSessionIds().get(servletContext);
        
        if (actualSessionId != null) {
            return actualSessionId;
        }
        else {
            return 
                (String) SessionManager.getSessionInfo(this).
                getDeadRequestedSessionId();
        }
    }
    
    public String getRequestURI() {
        String uri = this.requestUri;
        
        if (uri == null) {
            return "";
        }
        
        return uri;
    }
    
    public StringBuffer getRequestURL() {
        return new StringBuffer(this.requestUri);
    }
    
    public String getServletPath() {
        return null;
    }
    
    public HttpSession getSession(boolean shouldAutomaticallyCreateNewSession) {
        String cookieValue = SessionManager.getSessionId(this, servletContext);
        
        if (cookieValue == null) {
            if (shouldAutomaticallyCreateNewSession) {
                HttpSession session = SessionFactory.createSession();
                SessionManager.putSessionId(this, servletContext, session.getId());
                SessionManager.addSession(servletContext, session);
                cookieValue = session.getId();
            }
            else {
                return null;
            }
        }
        
        HttpSession session = SessionManager.getSession(servletContext, cookieValue);
        
        if (session == null && shouldAutomaticallyCreateNewSession) {
            session = SessionFactory.createSession();
            SessionManager.putSessionId(this, servletContext, session.getId());
            SessionManager.addSession(servletContext, session);
        }
        
        return session;
    }
    
    public HttpSession getSession() {
        return getSession(true);
    }

    /**
     * Checks if this request has been put into asynchronous mode.
     */
    public boolean isAsyncStarted() {
        return false;
    }

    /**
     * Checks if this request supports asynchronous operation.
     */
    public boolean isAsyncSupported() {
        return true;
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    /**
     * Puts this request into asynchronous mode, and initializes its
     * AsyncContext with the original (unwrapped) ServletRequest and
     * ServletResponse objects.
     */
    public AsyncContext startAsync() {
        ServletRequest servletRequest = null;
        ServletResponse servletResponse = null;
        
        // TODO Implement

        return startAsync(servletRequest, servletResponse);
    }

    /**
     * Puts this request into asynchronous mode, and initializes its
     * AsyncContext with the given request and response objects.
     */
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        AsyncContext asyncContext = null;
        
        // TODO Implement
        
        return asyncContext;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }
    
    public Object getAttribute(String string) {
        return null;
    }
    
    public Enumeration getAttributeNames() {
        return null;
    }
    
    public String getCharacterEncoding() {
        if (entity == null) {
            return getHeader(HttpHeaders.CONTENT_ENCODING);
        }
        
        return entity.getContentEncoding();
    }

    /**
     * Returns the length, in bytes, of the request body and made
     * available by the input stream, or -1 if the length is not known
     * or is greater than Integer.MAX_VALUE.
     */
    public int getContentLength() {
        return Long.valueOf(getContentLengthLong()).intValue();
    }

    /**
     * Returns the length, in bytes, of the request body and made
     * available by the input stream, or -1 if the length is not known.
     */
    public long getContentLengthLong() {
        long contentLength = 0l;
        if (this.entity != null && this.entity instanceof EntityDetails) {
            EntityDetails entityDetails = (EntityDetails) this.entity;
            contentLength = entityDetails.getContentLength();
        }
        return contentLength;
    }
    
    public String getContentType() {
        String contentType = getHeader(HttpHeaders.CONTENT_TYPE);
        
        if (contentType == null || entity == null) {
            contentType = "";
        }
        else {
            contentType = entity.getContentType();
        }
        
        return contentType;
    }
    
    public ServletInputStream getInputStream() throws IOException {
        if (entity == null) {
            return in; // TODO Maybe throw an exception
        }
        
        if (in == null || entity.isRepeatable()) {
            in = ((ServletRequestEntity) getEntity()).getInputStream();
        }
        
        return in;
    }
    
    public String getParameter(String name) {
        String value = null;
        
        Object o = getParameterMap().get(name);
        
        if (o instanceof String) {
            value = (String) o;
        }
        else {
            value = o == null ? "" : o.toString();
        }
        
        try {
            value = URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        
        return value;
    }
    
    public Enumeration getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }
    
    public String[] getParameterValues(String name) {
        String[] values = null;
        
        String parameter = getParameter(name);
        
        if (parameter != null) {
            // TODO figure this out
            values = parameter.split(";");
        }
        
        return values;
    }
    
    public Map getParameterMap() {
        if (parameters == null || parameters == Collections.EMPTY_MAP) {
            try {
                parameters = getParameterMap(this);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return parameters;
    }
    
    private Map getParameterMap(HttpServletRequest request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("request argument must not be null");
        }
        
        Map parameters = new LinkedHashMap();
        
        String method = request.getMethod();
        
        if (method.equals("GET") || method.equals("HEAD") || method.equals("POST")) {
            String queryString = request.getQueryString();
            
            if (queryString != null && queryString.length() > 0) {
                parameters.putAll(ParameterUtils.parseParameters(queryString));
            }
        }
        
        if (method.equals("POST")) {
            if (RequestUtils.isMultiPart(this)) {
                MultipartRequest multipartRequest = RequestUtils.getMultiPartRequest(request);
                parameters.putAll(ParameterUtils.
                    getParameterMap(multipartRequest));
            }
            else {
                int contentLength = request.getContentLength();
                byte[] b = new byte[contentLength];
                request.getInputStream().read(b);
                String parameterLine = new String(b);

                if (parameterLine != null && parameterLine.length() > 0) {
                    parameters.putAll(ParameterUtils.parseParameters(parameterLine));
                }
            }
        }
        
        if (shouldOverwriteParameters()) {
            this.parameters = parameters;
        }
        else {
            this.parameters.putAll(parameters);
        }
        
        return this.parameters;
    }
    
    private boolean shouldOverwriteParameters() {
        return true; // TODO make configurable
    }
    
    public String getProtocol() {
        return null;
    }
    
    public String getScheme() {
        return null;
    }
    
    public String getServerName() {
        return null;
    }
    
    public int getServerPort() {
        return -1;
    }

    /**
     * Gets the servlet context to which this ServletRequest was last dispatched.
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    public String getLocalName() {
        return null;
    }
    
    public String getLocalAddr() {
        return null;
    }
    
    public int getLocalPort() {
        return -1;
    }
    
    public BufferedReader getReader() throws IOException {
        InputStream in = getInputStream();
        
        if (in != null) {
            reader = new BufferedReader(new InputStreamReader(in));
        }
        
        return reader;
    }
    
    public String getRemoteAddr() {
        return null;
    }
    
    public String getRemoteHost() {
        return null;
    }
    
    public int getRemotePort() {
        return -1;
    }
    
    public void removeAttribute(String string) {
        return;
    }
    
    public Locale getLocale() {
        return null;
    }
    
    public Enumeration getLocales() {
        return null;
    }
    
    public boolean isSecure() {
        return getAttribute("javax.net.ssl.session") != null;
    }
    
    public RequestDispatcher getRequestDispatcher(String string) {
        return null;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }

    public RequestLine getRequestLine() {
        // TODO Test
        return new RequestLine(this.request);
    }

    public void addHeader(Header header) {
        // TODO Test
        this.request.addHeader(header);
    }

    public void addHeader(String name, String value) {
        // TODO Test
        this.request.addHeader(name, value);
    }

    public boolean containsHeader(String name) {
        // TODO Test
        return request.containsHeader(name);
    }

    public Header[] getAllHeaders() {
        // TODO Test
        return request.getHeaders();
    }

    public Header getFirstHeader(String name) {
        // TODO Test
        return request.getFirstHeader(name);
    }

    public HttpVersion getHttpVersion() {
        // TODO Test
        ProtocolVersion protocolVersion = request.getVersion();
        int major = protocolVersion.getMajor();
        int minor = protocolVersion.getMinor();
        return new HttpVersion(major, minor);
    }

    public Header getLastHeader(String name) {
        // TODO Test
        return request.getLastHeader(name);
    }
    
    /**
     * Returns an iterator of all the headers.
     * 
     * @return Iterator that returns Header objects in the sequence they are
     *         sent over a connection.
     */
    public Iterator<Header> headerIterator() {
        // TODO Test
        return request.headerIterator();
    }

    /**
     * Returns an iterator of the headers with a given name.
     *
     * @param name      the name of the headers over which to iterate, or
     *                  <code>null</code> for all headers
     *
     * @return Iterator that returns Header objects with the argument name
     *         in the sequence they are sent over a connection.
     */
    public Iterator<Header> headerIterator(String name) {
        // TODO Test
        return request.headerIterator(name);
    }

    public void removeHeader(Header header) {
        // TODO Test
        request.removeHeader(header);
    }

    public void removeHeaders(String name) {
        // TODO Test
        request.removeHeaders(name);
    }
    
    public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
        return;
    }
    
    public void setAttribute(String string, Object object) {
        return;
    }

    public void setHeader(Header header) {
        // TODO Test
        request.setHeader(header);
    }

    public void setHeader(String name, String value) {
        // TODO Test
        request.setHeader(name, value);
    }

    public void setHeaders(Header[] headers) {
        // TODO Test
        request.setHeaders(headers);
    }

    public ProtocolVersion getProtocolVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Validate the provided username and password in the password
     * validation realm used by the web container login mechanism
     * configured for the ServletContext.
     */
    public void login(String username, String password) {
        // TODO Implement
    }

    /**
     * Establish null as the value returned when getUserPrincipal,
     * getRemoteUser, and getAuthType is called on the request.
     */
    public void logout() {
        // TODO Implement
    }

    /**
     * Creates an instance of HttpUpgradeHandler for a given class
     * and uses it for the http protocol upgrade processing.
     */
    public HttpUpgradeHandler upgrade(Class handlerClass) {
        return (HttpUpgradeHandler) new DefaultHttpUpgradeHandler(handlerClass);
    }
}
