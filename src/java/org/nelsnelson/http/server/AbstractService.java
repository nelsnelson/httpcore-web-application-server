package org.nelsnelson.http.server;

public abstract class AbstractService implements Service, Log {
    protected Service service;
    protected ShutdownHook shutdownHook;
    
    abstract public void restart();
}
