
package org.nelsnelson.http.servlet;

import javax.servlet.SessionCookieConfig;

public class DefaultSessionCookieConfig
    implements SessionCookieConfig
{
    private String comment = null;
    private String domain = null;
    private int maxAge = -1;
    private String name = null;
    private String path = null;
    private boolean isHttpOnly = false;
    private boolean isSecure = false;

    public DefaultSessionCookieConfig() {
        this.comment = "";
        this.domain = "";
        this.maxAge = 14400;
        this.name = "";
        this.path = "";
        this.isHttpOnly = false;
        this.isSecure = false;
    }

    /**
     * Gets the comment that will be assigned to any session
     * tracking cookies created on behalf of the application
     * represented by the ServletContext from which this
     * SessionCookieConfig was acquired.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Gets the domain name that will be assigned to any session
     * tracking cookies created on behalf of the application
     * represented by the ServletContext from which this
     * SessionCookieConfig was acquired.
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Gets the lifetime (in seconds) of the session tracking
     * cookies created on behalf of the application
     * represented by the ServletContext from which this
     * SessionCookieConfig was acquired.
     */
    public int getMaxAge() {
        return this.maxAge;
    }

    /**
     * Gets the name that will be assigned to any session
     * tracking cookies created on behalf of the application
     * represented by the ServletContext from which this
     * SessionCookieConfig was acquired.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the path that will be assigned to any session
     * tracking cookies created on behalf of the application
     * represented by the ServletContext from which this
     * SessionCookieConfig was acquired.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Checks if the session tracking cookies created on
     * behalf of the application represented by the
     * ServletContext from which this SessionCookieConfig
     * was acquired will be marked as HttpOnly.
     */
    public boolean isHttpOnly() {
        return this.isHttpOnly;
    }

    /**
     * Checks if the session tracking cookies created on
     * behalf of the application represented by the
     * ServletContext from which this SessionCookieConfig
     * was acquired will be marked as secure even if the
     * request that initiated the corresponding session is
     * using plain HTTP instead of HTTPS.
     */
    public boolean isSecure() {
        return this.isSecure;
    }

    /**
     * Sets the comment that will be assigned to any session
     * tracking cookies created on behalf of the application
     * represented by the ServletContext from which this
     * SessionCookieConfig was acquired.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Sets the domain name that will be assigned to any
     * session tracking cookies created on behalf of the
     * application represented by the ServletContext from
     * which this SessionCookieConfig was acquired.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Marks or unmarks the session tracking cookies created
     * on behalf of the application represented by the
     * ServletContext from which this SessionCookieConfig
     * was acquired as HttpOnly.
     */
    public void setHttpOnly(boolean httpOnly) {
        this.isHttpOnly = httpOnly;
    }

    /**
     * Sets the lifetime (in seconds) for the session tracking
     * cookies created on behalf of the application represented
     * by the ServletContext from which this SessionCookieConfig
     * was acquired.
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Sets the name that will be assigned to any session
     * tracking cookies created on behalf of the application
     * represented by the ServletContext from which this
     * SessionCookieConfig was acquired.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the path that will be assigned to any session
     * tracking cookies created on behalf of the application
     * represented by the ServletContext from which this
     * SessionCookieConfig was acquired.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Marks or unmarks the session tracking cookies created
     * on behalf of the application represented by the
     * ServletContext from which this SessionCookieConfig
     * was acquired as secure.
     */
    public void setSecure(boolean secure) {
        this.isSecure = secure;
    }
}
