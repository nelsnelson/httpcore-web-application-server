package org.nelsnelson.http.servlet;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.nelsnelson.http.HttpHeaders;
import org.nelsnelson.http.entity.ServletResponseEntity;
import org.nelsnelson.http.header.ContentType;
import org.nelsnelson.http.server.ServletContainer;
import org.nelsnelson.http.server.Webapp;
import org.nelsnelson.http.session.DefaultHttpSession;
import org.nelsnelson.http.session.SessionManager;
import org.nelsnelson.http.util.RequestUtils;
import org.nelsnelson.http.util.ResponseUtils;
import org.nelsnelson.org.apache.commons.httpclient.Cookie;

public class ServletExecutor {
    private static String versionString = "Servlet/4.0.2 (HttpCore Web Application Server/1.0)";

    public ServletExecutor() {
        
    }
    
    public void execute(Servlet servlet, ClassicHttpRequest request, 
        ClassicHttpResponse response, HttpContext httpContext) 
        throws HttpException, IOException, ServletException, 
        NullPointerException
    {
        ServletContext servletContext = 
            servlet.getServletConfig().getServletContext();
        
        HttpServletRequest servletRequest = 
            RequestUtils.getServletRequest(request, httpContext, servletContext);
        
        HttpServletResponse servletResponse = 
            ResponseUtils.getServletResponse(response, httpContext, servletRequest, servletContext);
        
        servlet.init(servlet.getServletConfig());
        servlet.service(servletRequest, servletResponse);
        servlet.destroy();
        
        validate(request, response, servletContext, servletRequest, servletResponse);
        
        close(servletRequest, servletResponse);
    }

    /**
     * Validate that the outgoing response has all its required headers
     */
    public void validate(ClassicHttpRequest request, ClassicHttpResponse response, 
        ServletContext servletContext, HttpServletRequest servletRequest, 
        HttpServletResponse servletResponse) 
    {
        int statusCode = response.getCode();
        
        // Ensure Content-Length header is included
        String lengthHeader = null;
        Header contentLength = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
        if (contentLength != null) {
            lengthHeader = contentLength.getValue();
        }
        
        if (lengthHeader == null && statusCode >= 300) {
            ServletResponseEntity entity = (ServletResponseEntity) response.getEntity();
            long bodyBytes = entity.getContentLength();
            
            if (entity.getBufferSize() > bodyBytes) {
                servletResponse.setHeader(HttpHeaders.CONTENT_LENGTH, "" + bodyBytes);
                contentLength = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
                lengthHeader = contentLength.getValue();
            }
        }
        
        if (shouldKeepAlive(request, response)) {
            // Ensure Keep-Alive header is included
            servletResponse.setHeader(HttpHeaders.CONNECTION, HttpHeaders.KEEP_ALIVE);
        }
        
        // Ensure Content-Type header is included
        if (statusCode != HttpServletResponse.SC_MOVED_TEMPORARILY) {
            String characterEncoding = servletResponse.getCharacterEncoding();
            
            Header contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
            
            if (contentType == null) {
                // use servlet-specifed encoding with default mime-type
                response.setHeader(HttpHeaders.CONTENT_TYPE, 
                    "text/html;charset=" + characterEncoding);
            }
            else {
                ContentType contentTypeHeader = new ContentType(contentType);
                
                if (contentTypeHeader.getMimeType().startsWith("text/")) {
                	if (characterEncoding == null) {
                		characterEncoding = contentTypeHeader.getFirstParameter(ContentType.CHARSET);
                	}
                    
                    contentTypeHeader.setParameter(ContentType.CHARSET, characterEncoding);
                    
                    response.setHeader(contentTypeHeader);
                }
            }
        }
        
        // Set Date header
        if (response.getHeaders(HttpHeaders.DATE).length == 0) {
            servletResponse.setDateHeader(HttpHeaders.DATE, new Date().getTime());
        }
        
        // Set X-Powered-By header
        if (response.getFirstHeader("X-Powered-By") == null) {
            response.setHeader("X-Powered-By", ServletExecutor.getVersion());
        }
        
        // Set Content-Language header
        Locale locale = response.getLocale();
        
        if (locale != null) {
            String lang = locale.getLanguage();
            String country = locale.getCountry();
            
            if (country != null && !country.equals("")) {
                lang = lang + "-" + country;
            }
            
            servletResponse.setHeader(HttpHeaders.CONTENT_LANGUAGE, lang);
        }
        
        // No context? No session.
        if (servletContext == null) {
            return;
        }
        
        // Handle session information
        ServletSessionInfo info = SessionManager.getSessionInfo(servletRequest);
        Map sessionIds = info.getSessionIds();
        Map requestedSessionIds = info.getRequestedSessionIds();
        
        // Add a new cookie for the session, if there is a session
        for (Iterator i = sessionIds.keySet().iterator(); i.hasNext(); ) {
            String prefix = (String) i.next();
            String sessionId = (String) sessionIds.get(prefix);
            Webapp ownerContext = ServletContainer.get(prefix);
            
            if (ownerContext != null) {
                HttpSession session = SessionManager.getSession(servletContext, sessionId);
                
                if (session != null && session.isNew()) {
                    if (session instanceof DefaultHttpSession) {
                        ((DefaultHttpSession) session).setIsNew(false);
                    }
                    
                    Cookie cookie = 
                        new Cookie(ServletContainer.getDomain(), 
                        DefaultHttpSession.SESSION_COOKIE_NAME, session.getId());
                    
                    cookie.setMaxAge(-1);
                    cookie.setSecure(false);//servletRequest.isSecure());
                    cookie.setVersion(0);
                    cookie.setPath(servletContext.getRealPath("").equals("") ? 
                        "/" : servletContext.getRealPath(""));
                    
                    response.addHeader(HttpHeaders.SET_COOKIE, 
                        CookieHandler.getInstance().formatCookie(cookie));
                }
            }
        }
        
        // Remove expired sessions. Expired sessions are those that are requested, but 
        // aren't being tracked. Tell the requester that the cookie being used has 
        // expired.
        for (Iterator i = requestedSessionIds.keySet().iterator(); i.hasNext(); ) {
            String prefix = (String) i.next();
            String sessionId = (String) requestedSessionIds.get(prefix);
            
            if (!requestedSessionIds.containsKey(prefix)) {
                Cookie cookie = 
                    new Cookie(ServletContainer.getDomain(), 
                    DefaultHttpSession.SESSION_COOKIE_NAME, sessionId);
                
                cookie.setMaxAge(0); // explicitly expire this cookie
                cookie.setSecure(false); //servletRequest.isSecure());
                cookie.setVersion(0); //request.isSecure() ? 1 : 0);
                cookie.setPath(prefix.equals("") ? "/" : prefix);
                
                response.addHeader(HttpHeaders.SET_COOKIE, 
                    CookieHandler.getInstance().formatCookie(cookie));
            }
        }
    }
    
    private static Object getVersion() {
        return ServletExecutor.versionString ;
    }

    /**
     * Based on request/response headers and the protocol, determine whether or
     * not this connection should operate in keep-alive mode.
     */
    public boolean shouldKeepAlive(HttpRequest request, HttpResponse response) {
        boolean shouldKeepAlive = false;
        
        Header requestKeepAlive = request.getFirstHeader(HttpHeaders.CONNECTION);
        Header responseKeepAlive = response.getFirstHeader(HttpHeaders.CONNECTION);
        
        String requestKeepAliveHeader = null;
        String responseKeepAliveHeader = null;
        
        if (requestKeepAlive != null) {
            requestKeepAliveHeader = requestKeepAlive.getValue();
        }
        
        if (responseKeepAlive != null) {
            responseKeepAliveHeader = responseKeepAlive.getValue();
        }
        
        Header contentLength = response.getFirstHeader(HttpHeaders.CONTENT_LENGTH);
        
        ProtocolVersion version = response.getVersion();
        
        if (version.lessEquals(HttpVersion.HTTP_0_9)) {
            shouldKeepAlive = true;
        }
        else if (requestKeepAliveHeader == null && responseKeepAliveHeader == null) {
            if (version.equals(HttpVersion.HTTP_1_0) || contentLength == null) {
                shouldKeepAlive = true;
            }
        }
        else if (responseKeepAliveHeader != null) {
            if (contentLength == null) {
                shouldKeepAlive = true;
            }
        }
        else if (requestKeepAliveHeader != null) {
            if (contentLength == null) {
                shouldKeepAlive = true;
            }
        }
        
        return shouldKeepAlive;
    }
    
    public void close(HttpServletRequest request, 
        HttpServletResponse response) 
        throws IOException 
    {
        Writer writer = response.getWriter();
        
        if (writer != null) {
            writer.flush();
            writer.close();
        }
        
        Reader reader = request.getReader();
        
        if (reader != null) {
            reader.close();
        }
        
        SessionManager.discardSessionInfo(request);
    }
}
