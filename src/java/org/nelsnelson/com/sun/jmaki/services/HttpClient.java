/* 
 * Copyright 2006 Sun Microsystems, Inc.  All rights reserved.  You may not 
 * modify, use, reproduce, or distribute this software except in compliance 
 * with the terms of the License at: 
 * http://developer.sun.com/berkeley_license.html
 * $Id: HttpClient.java,v 1.4 2006/10/17 03:42:15 gmurray71 Exp $ 
 */

package org.nelsnelson.com.sun.jmaki.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Security;

/**
 * @author Yutaka Yoshida
 *
 * Minimum set of HTTPclient supporting both http and https.
 * It's also capable of POST, but it doesn't provide doGet because
 * the caller can just read the inputstream.
 */
public class HttpClient {

   private String proxyHost = null;
   private int proxyPort = -1;
   private boolean isHttps = false;
   private boolean isProxy = false;
   private URLConnection urlConnection = null;
   
   /**
    * @param url URL string
    */
   public HttpClient(String url) 
       throws MalformedURLException {
       this.urlConnection = getURLConnection(url);
   }
   /**
    * @param phost PROXY host name
    * @param pport PROXY port string
    * @param url URL string
    */
   public HttpClient(String phost, int pport, String url)
       throws MalformedURLException {
       if (phost != null && pport != -1) {
           this.isProxy = true;
       }
       this.proxyHost = phost;
       this.proxyPort = pport;
       if (url.indexOf("https") >= 0) {
           isHttps = true;
       }
       this.urlConnection = getURLConnection(url);
       // set user agent to mimic a common browser
       String ua="Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)";
       this.urlConnection.setRequestProperty("user-agent", ua);
   }
   
   /**
    * private method to get the URLConnection
    * @param str URL string
    */
   private URLConnection getURLConnection(String str) 
       throws MalformedURLException {
       try {
           if (isHttps) {
               if (isProxy) {
                   System.setProperty("https.proxyHost", proxyHost);
                   System.setProperty("https.proxyPort", proxyPort + "");
               }
           } else {
               if (isProxy) {
                   System.setProperty("http.proxyHost", proxyHost);
                   System.setProperty("http.proxyPort", proxyPort  + "");
               }
           }
           URL url = new URL(str);
           return (url.openConnection());
           
       } catch (MalformedURLException me) {
           throw new MalformedURLException(str + " is not a valid URL");
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
   }
   
   /**
    * returns the inputstream from URLConnection
    * @return InputStream
    */
   public InputStream getInputStream() {
       try {
           return (this.urlConnection.getInputStream());
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
   }
   
   /**
    * return the OutputStream from URLConnection
    * @return OutputStream
    */
   public OutputStream getOutputStream() {
       
       try {
           return (this.urlConnection.getOutputStream());
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
   }
   
   /**
    * posts data to the inputstream and returns the InputStream.
    * @param postData data to be posted. must be url-encoded already.
    * @return InputStream input stream from URLConnection
    */
   public InputStream doPost(String postData) {
       this.urlConnection.setDoOutput(true);
       OutputStream os = this.getOutputStream();
       PrintStream ps = new PrintStream(os);
       ps.print(postData);
       ps.close();
       
       return (this.getInputStream());
   }
   
   public String getContentEncoding() {
       if (this.urlConnection == null) return null;
       return (this.urlConnection.getContentEncoding());
   }
   public int getContentLength() {
       if (this.urlConnection == null) return -1;
       return (this.urlConnection.getContentLength());
   }
   public String getContentType() {
       if (this.urlConnection == null) return null;
       return (this.urlConnection.getContentType());
   }
   public long getDate() {
       if (this.urlConnection == null) return -1;
       return (this.urlConnection.getDate());
   }
   public String getHeader(String name) {
       if (this.urlConnection == null) return null;
       return (this.urlConnection.getHeaderField(name));
   }
   public long getIfModifiedSince() {
       if (this.urlConnection == null) return -1;
       return (this.urlConnection.getIfModifiedSince());
   }
}