
package org.nelsnelson.http.servlet;

import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;

public class DefaultHttpUpgradeHandler
    implements HttpUpgradeHandler
{
    private Class<HttpUpgradeHandler> handlerClass = null;

    public DefaultHttpUpgradeHandler(Class<HttpUpgradeHandler> handlerClass) {
        this.handlerClass = handlerClass;
    }

    /**
     * It is called when the client is disconnected.
     */
    public void destroy() {
        // TODO Implemet
    }

    /**
     * It is called once the HTTP Upgrade process has been completed
     * and the upgraded connection is ready to start using the new
     * protocol.
     */
    public void init(WebConnection wc) {
        // TODO Implement
    }
}
