package org.nelsnelson.http.server;

import java.util.logging.Level;

/**
 * 
 * @author nelsnelson
 *
 */
public class ContainerService extends AbstractService {
    public ContainerService(Service service) {
        this.service = service;
        this.shutdownHook = new ShutdownHook(this.service);
    }
    
    public void start() {
        maybeSetShutdownHook();
        //service.setDaemon(false);
        service.start();
    }
    
    public void restart() {
        stop();
        start();
    }
    
    public void stop() {
        if (service != null) {
            service.stop();
        }
        Thread.yield();
    }
    
    public void log(Level level, String msg) {
        System.out.println(msg);
    }
    
    private void maybeSetShutdownHook() {
        if (this.shutdownHook == null) {
            this.shutdownHook = new ShutdownHook(this.service);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }
}
