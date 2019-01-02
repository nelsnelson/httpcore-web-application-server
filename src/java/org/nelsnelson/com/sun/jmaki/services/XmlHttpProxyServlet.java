/*
 * Copyright 2006 Sun Microsystems, Inc.  All rights reserved.  You may not 
 * modify, use, reproduce, or distribute this software except in compliance 
 * with the terms of the License at: 
 * 
 * http://developer.sun.com/berkeley_license.html
 * 
 * $Id: XmlHttpProxyServlet.java,v 1.14 2007/04/03 02:57:14 gmurray71 Exp $ 
 */

package org.nelsnelson.com.sun.jmaki.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class XmlHttpProxyServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static String XHP_LAST_MODIFIED = "xhp_last_modified_key";
    private static String XHP_CONFIG = "/resources/xhp.json";
    private static String XHP_RESOURCES = "/resources/xsl/";
    private static String XHP_CLASSPATH_RESOURCES = "/META-INF/resources/xsl/";

    private static boolean allowXDomain = false;
    private static boolean rDebug = false;
    private Logger logger = null;
    private XmlHttpProxy xhp = null;
    private ServletContext ctx;
    private JsonObject services = null;
    
    /** Creates a new instance of the XmlHttpProxyServlet
     * @author Greg Murray
     */
    public XmlHttpProxyServlet() {
        if (rDebug) {
            logger = getLogger();
        }
    }
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ctx = config.getServletContext();
        String xdomainString = (String) ctx.getAttribute("allowXDomain");
        if (xdomainString != null) {
            if ("true".equals(xdomainString)) {
                allowXDomain = true;
                getLogger().severe("XmlHttpProxyServlet: intialization. xDomain access is enabled.");
            }
        }
        // if there is a proxyHost and proxyPort specified create an HttpClient with the proxy
        String proxyHost = (String) ctx.getAttribute("proxyHost");
        String proxyPortString = (String) ctx.getAttribute("proxyPort");
        if (proxyHost != null && proxyPortString != null) {
            int proxyPort = 8080;
            try {
                proxyPort= new Integer(proxyPortString).intValue();
                xhp = new XmlHttpProxy(proxyHost, proxyPort);
            } catch (NumberFormatException nfe) {
                getLogger().severe("XmlHttpProxyServlet: intialization error. The proxyPort must be a number");
                throw new ServletException("XmlHttpProxyServlet: intialization error. The proxyPort must be a number");
            }
        } else {
            xhp = new XmlHttpProxy();
        }
    }
    
    private void getServices() {
        InputStream is = null;
        try {
            URL url = ctx.getResource(XHP_CONFIG);
            is = url.openStream();
        } catch (Exception ex) {
            getLogger().severe("XmlHttpProxyServlet error getting services:" + ex);
        }
        services = xhp.loadServices(is);
    }
   
    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        if (!allowXDomain) {
            // check to see if there was a session created for this request
            // if not assume it was from another domain and blow up
            // Wrap this to prevent Portlet exeptions
            HttpSession session = req.getSession(false);
            if (session == null) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return; 
            }
        }
        String serviceKey = req.getParameter("id");
        // only to preven regressions - Remove before 1.0
        if (serviceKey == null) serviceKey = req.getParameter("key");
        OutputStream out = null;
        PrintWriter writer = null;
        // check if the services have been loaded or if they need to be reloaded
        if (services == null || configUpdated()) {
            getServices();
        }
        try {           
            String urlString = null;
            String xslURLString = null;
            String format = "json";
            String callback = req.getParameter("callback");
            String urlParams = req.getParameter("urlparams");
            // encode the url to prevent spaces from being passed along
            if (urlParams != null) {
                urlParams = urlParams.replace(' ', '+');
            }
            
            try {
                if (services.containsKey(serviceKey)) {
                    JsonObject service = services.getJsonObject(serviceKey);
                    // default to the service default if no url parameters are specified
                    if (urlParams == null && service.containsKey("defaultURLParams")) {
                        urlParams = service.getString("defaultURLParams");
                    }
                    String serviceURL = service.getString("url");
                    // build the URL
                    if (serviceURL.indexOf("?") == -1){
                        serviceURL += "?";
                    } else {
                        serviceURL += "&";
                    }
                    String apikey = "";
                    if (service.containsKey("apikey")) apikey = service.getString("apikey");
                    urlString = serviceURL + apikey +  "&" + urlParams;
                    if (service.containsKey("xslStyleSheet")) {
                        xslURLString = service.getString("xslStyleSheet");
                    }
                } else {
                    writer = res.getWriter();
                    if (serviceKey == null) writer.write("XmlHttpProxyServlet Error: id parameter specifying serivce required.");
                    else writer.write("XmlHttpProxyServlet Error : service for id '" + serviceKey + "' not  found.");
                    writer.flush();
                    return;  
                }
            } catch (Exception ex) {
                getLogger().severe("XmlHttpProxyServlet Error loading service: " + ex);
            }

            Map paramsMap = new HashMap();
            paramsMap.put("format", format);
            if (callback != null) {
                paramsMap.put("callback", callback);
            }
            
            InputStream xslInputStream = null;
            
            if (urlString == null) {
               writer = res.getWriter();
               writer.write("XmlHttpProxyServlet parameters:  id[Required] urlparams[Optional] format[Optional] callback[Optional]");
               writer.flush();
               return;
            }
            // default to UTF-8
            res.setContentType("text/html;charset=UTF-8");
            out = res.getOutputStream();
            // get the stream for the xsl stylesheet
            if (xslURLString != null) {
                // check the web root for the resource
                URL xslURL = null;
                 xslURL = ctx.getResource(XHP_RESOURCES + xslURLString);
                // if not in the web root check the classpath
                if (xslURL == null) {  
                        xslURL = XmlHttpProxyServlet.class.getResource(XHP_CLASSPATH_RESOURCES + xslURLString);
                }
                if (xslURL != null) {
                    xslInputStream  = xslURL.openStream();
                } else {
                    String message = "Could not locate the XSL stylesheet provided for service id " +  serviceKey + ". Please check the XMLHttpProxy configuration.";
                    getLogger().severe(message);
                    try {
                        out.write(message.getBytes());
                        out.flush();
                    } catch (java.io.IOException iox){
                    }
                }
            }
            xhp.doGet(urlString, out, xslInputStream, paramsMap);
        } catch (java.io.IOException iox) {
            getLogger().severe("XmlHttpProxyServlet: caught " + iox);
            try {
                writer = res.getWriter();
                writer.write("XmlHttpProxyServlet error loading service for " + serviceKey + " . Please notify the administrator.");
                writer.flush();
            } catch (java.io.IOException ix) {
            }
            return;        
        } finally {
             try {
                if (out != null) out.close();
                if (writer != null) writer.close();
            } catch (java.io.IOException iox){
            }
        }
    }

    /**
    * Check to see if the configuration file has been updated so that it may be reloaded.
    */
    private boolean configUpdated() {
        try {
            URL url = ctx.getResource(XHP_CONFIG);
            URLConnection con;
            if (url == null) return false ;
            con = url.openConnection(); 
            long lastModified = con.getLastModified();
            long XHP_LAST_MODIFIEDModified = 0;
            if (ctx.getAttribute(XHP_LAST_MODIFIED) != null) {
                XHP_LAST_MODIFIEDModified = ((Long)ctx.getAttribute(XHP_LAST_MODIFIED)).longValue();
            } else {
                ctx.setAttribute(XHP_LAST_MODIFIED, new Long(lastModified));
                return false;
            }
            if (XHP_LAST_MODIFIEDModified < lastModified) {
                ctx.setAttribute(XHP_LAST_MODIFIED, new Long(lastModified));
                return true;
            }
        } catch (Exception ex) {
            getLogger().severe("XmlHttpProxyServlet error checking configuration: " + ex);
        }
        return false;
    }
         
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("com.sun.jmaki.Log");
        }
        return logger;
    }
    
    private void logMessage(String message) {
        if (rDebug) {
            getLogger().info(message);
        }
    }
}