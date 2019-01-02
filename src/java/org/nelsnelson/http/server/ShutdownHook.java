package org.nelsnelson.http.server;

import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;

/**
 * 
 * @author nelsnelson
 */
public class ShutdownHook extends Thread {
    private Service service;

    public ShutdownHook(Service service) {
        this.service = service;
    }

    public void run() {
        if (service != null) {
            service.stop();
            if (service instanceof ModalCloseable) {
                ModalCloseable closeable = (ModalCloseable) service;
                closeable.close(CloseMode.GRACEFUL);
            }
        }
    }
}
