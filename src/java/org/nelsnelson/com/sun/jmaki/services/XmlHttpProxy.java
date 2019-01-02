/* 
 * Copyright 2006 Sun Microsystems, Inc.  All rights reserved.  You may not 
 * modify, use, reproduce, or distribute this software except in compliance 
 * with the terms of the License at: 
 * 
 * http://developer.sun.com/berkeley_license.html
 *
 * $Id: XmlHttpProxy.java,v 1.9 2006/10/18 22:16:23 gmurray71 Exp $ 
 */

package org.nelsnelson.com.sun.jmaki.services;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XmlHttpProxy {

    private static Logger logger;
    private String proxyHost = "";
    int proxyPort = -1;
    private JsonObject config;
    private static String USAGE = "Usage:  -url service_URL  -key service_key [-url or -key required] -xslurl xsl_url [optional] -format json|xml [optional] -callback[optional] -config [optional] -resources base_directory_containing XSL stylesheets [optional]";

    public XmlHttpProxy() {}

    public XmlHttpProxy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    /**
     * This method will go out and make the call and it will apply an XSLT Transformation with the
     * set of parameters provided.
     *
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     *
     */

    public void doGet(String urlString,
        OutputStream out)
    throws IOException, MalformedURLException {
        doGet(urlString, out, null, null);
    }

    /**
     * This method will go out and make the call and it will apply an XSLT Transformation with the
     * set of parameters provided.
     *
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     * @param xslInputStream - An input Stream to an XSL style sheet that is provided to the XSLT processor. If set to null there will be no transformation 
     * @paramsMap - A Map of parameters that are feed to the XSLT Processor. These params may be used when generating content. This may be set to null if no parameters are necessary.
     *
     */
    public void doGet(String urlString,
        OutputStream out,
        InputStream xslInputStream,
        Map paramsMap) throws IOException, MalformedURLException {

        if (paramsMap == null) {
            paramsMap = new HashMap();
        }

        String format = (String)paramsMap.get("format");
        if (format == null) {
            format = "xml";
        }

        InputStream in = null;
        BufferedOutputStream os = null;

        HttpClient httpclient = new HttpClient(proxyHost, proxyPort, urlString);
        in = httpclient.getInputStream();
        // read the encoding from the incoming document and default to 8859-1
        // if an encoding is not provided
        String ce = httpclient.getContentEncoding();
        if (ce == null) {
            String ct = httpclient.getContentType();
            if (ct != null) {
                int idx = ct.lastIndexOf("charset=");
                if (idx >= 0) {
                    ce = ct.substring(idx+8);
                } else {
                    ce = "iso-8859-1";
                }
            } else {
                ce = "iso-8859-1";
            }
        } 

        try {
            byte[] buffer = new byte[1024];
            int read = 0;
            String cType = null;
            // write out hte content type
            if (format.equals("json")) {
                cType = "text/javascript;charset="+ce;
            } else {
                cType = "text/xml;charset="+ce;
            } 
            if (xslInputStream == null) {
                while (true) {
                    read = in.read(buffer);
                    if (read <= 0) break;
                    out.write(buffer, 0, read );
                }
            } else {
                transform(in, xslInputStream, paramsMap, out, ce);
            }
        } catch (Exception e) {
            getLogger().severe("XmlHttpProxy transformation error: " + e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    /**
     * Do the XSLT transformation
     */
    public void transform( InputStream xmlIS,
        InputStream xslIS,
        Map params,
        OutputStream result,
        String encoding) {
        try {
            TransformerFactory trFac = TransformerFactory.newInstance();
            Transformer transformer = trFac.newTransformer(new StreamSource(xslIS));
            Iterator it = params.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                transformer.setParameter(key, (String)params.get(key));
            }
            transformer.setOutputProperty("encoding", encoding);
            transformer.transform(new StreamSource(xmlIS), new StreamResult(result));
        } catch (Exception e) {
            getLogger().severe("XmlHttpProxy: Exception with xslt " + e);
        }
    }

    /**
     *
     * CLI to the XmlHttpProxy
     */   
    public static void main(String[] args)
    throws IOException, MalformedURLException {

        getLogger().info("XmlHttpProxy 1.1");
        XmlHttpProxy xhp = new XmlHttpProxy();

        if (args.length == 0) {
            System.out.println(USAGE);
        }

        InputStream xslInputStream = null;
        String serviceKey = null;
        String urlString = null;
        String xslURLString = null;
        String format = "xml";
        String callback = null;
        String urlParams = null;
        String configURLString = "xhp.json";
        String resourceBase = "file:src/conf/META-INF/resources/xsl/";

        // read in the arguments
        int index = 0;
        while (index < args.length) {
            if (args[index].toLowerCase().equals("-url") && index + 1 < args.length) {
                urlString = args[++index];
            } else if (args[index].toLowerCase().equals("-key") && index + 1 < args.length) {
                serviceKey = args[++index];
            } else if (args[index].toLowerCase().equals("-callback") && index + 1 < args.length) {
                callback = args[++index];
            }  else if (args[index].toLowerCase().equals("-xslurl") && index + 1 < args.length) {
                xslURLString = args[++index];
            } else if (args[index].toLowerCase().equals("-urlparams") && index + 1 < args.length) {
                urlParams = args[++index];
            } else if (args[index].toLowerCase().equals("-config") && index + 1 < args.length) {
                configURLString = args[++index];
            } else if (args[index].toLowerCase().equals("-resources") && index + 1 < args.length) {
                resourceBase = args[++index];
            }
            index++;
        }

        if (serviceKey != null) {
            try {
                InputStream is = (new URL(configURLString)).openStream();
                JsonObject services = loadServices(is);
                JsonObject service = services.getJsonObject(serviceKey);
                // default to the service default if no url parameters are specified
                if (urlParams == null && service.containsKey("defaultURLParams")) {
                    urlParams = service.getString("defaultURLParams");
                }
                String serviceURL = service.getString("url");
                // build the URL properly
                if (serviceURL.indexOf("?") == -1){
                    serviceURL += "?";
                } else {
                    serviceURL += "&";
                }
                urlString = serviceURL + service.getString("apikey") +  "&" + urlParams;
                if (service.containsKey("xslStyleSheet")) {
                    xslURLString = service.getString("xslStyleSheet");
                    // check if the url is correct of if to load from the classpath

                }
            } catch (Exception ex) {
                getLogger().severe("XmlHttpProxy Error loading service: " + ex);
                System.exit(1);
            }
        } else if (urlString == null) {
            System.out.println(USAGE);
            System.exit(1);
        }
        // The parameters are feed to the XSL Stylsheet during transformation.
        // These parameters can provided data or conditional information.
        Map paramsMap = new HashMap();
        if (format != null) {
            paramsMap.put("format", format);
        }
        if (callback != null) {
            System.out.println("callback=" + callback);
            paramsMap.put("callback", callback);
        }

        if (xslURLString != null) {
            URL xslURL = new URL(xslURLString);
            if (xslURL != null) {
                xslInputStream  = xslURL.openStream();
            } else {
                getLogger().severe("Error: Unable to locate XSL at URL " + xslURLString);
            }
        }       
        xhp.doGet(urlString, System.out, xslInputStream, paramsMap);
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("com.sun.jmaki.Log");
        }
        return logger;
    }

    public static JsonObject loadServices(InputStream is) {
        JsonObject config = null;
        JsonObject services = JsonObject.EMPTY_JSON_OBJECT;
        try {
            config = loadJsonObject(is).getJsonObject("xhp");
            JsonArray servicesArray = config.getJsonArray("services");
            servicesArray.forEach((value)->{
                String key = ((JsonObject) value).getString("id");
                services.put(key,value);
            });
            //for (int l=0; l < sA.size(); l++) {
            //    JsonObject value = sA.getJsonObject(l);
            //    String key = value.getString("id");
            //    services.put(key,value);
            // }
        } catch (Exception ex) {
            getLogger().severe("XmlHttpProxy error loading services." + ex);
        }
        return services;
    }

    public static JsonObject loadJsonObject(InputStream in) {
        JsonReader jsonReader = Json.createReader(in);
        try {
            return jsonReader.readObject();
        }
        finally {
            jsonReader.close();
        }
    }
}