package org.nelsnelson.http.session;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingListener;



/**
 * Minimal HttpSessionContext implementation
 * This session manager is simple enough... it is the foundation for all session
 * managers, including distributed ones. To provide different session management
 * overload this class with the required functionality, create an instance of the
 * session manager and supply it to the @code{Service} on creation.
 *
 * @heading HttpSession Collection
 * A collection process is defined in this class to call the method
 * @code{doCollection()} which operates on the local session table.
 *
 * @see #newSessionId for details of how we create session ids
 * @author Paul Siegmann
 * @author Nic Ferrier - Tapsell-Ferrier Limited, nferrier@tfltd.net
 */
public class HttpSessionManager {
    // The debug pragma
    public final static boolean __DEBUG = false;

    /** HttpSession table scan interval.
     * This is the period used by the session collector to fire off a check
     * for invalid sessions.
     *
     * By default this value is set to 1 minute.
     */
    static long s_scaninterval;

    /** The invalid session collector
     * @code{.wait()}s on itself for @code{m_scaninterval} delay.
     * This allows us to @code{notify()} it into doing a session collection.
     */
    static private Collector s_collector;

    static {
        s_scaninterval = 60000L;
        s_collector = new Collector();
        s_collector.setPriority(10);
        s_collector.setDaemon(true);
        s_collector.start();
    }


    /**
     * The associated servlet context.
     */
    ServletContext application;

    /**
     * The default timeout.
     */
    int defaultInactivePeriod = 600000;

    /**
     * The sessions table
     */
    private Hashtable m_sessiontable;

    /**
     * The listeners.
     */
    private Vector m_listeners;

    /**
     * This is used to synchronize access to the accumulator
     * @see #newSessionId() the function that uses this variable
     */
    Object m_accumsync = new Object();

    /**
     * This is the session id accumulator.
     * We increment this each time we need a new session id.
     * When it gets to @code{Integer.MAX} we zero it and go round again.
     */
    private int m_accumulator = 0;
    
    
    // Constructors and other methods
    
    /**
     * A new session manager
     */
    public HttpSessionManager(ServletContext context) {
        application = context;
        m_sessiontable = new Hashtable();
        m_listeners = new Vector();
    }
    
    /**
     * Initialise the session manager.
     * This is called by the @code{Service} when the instance of the
     * session manager is created.
     */
    public void init()
    {
        Collector.addSessionManager(this);
    }

    /**
     * Set the default timeout for sessions.
     */
    public void setDefaultTimeout (int timeout)
    {
        defaultInactivePeriod = timeout;
    }

    /** Add a listener that will recieve session creation events.
     * This allows an application to monitor it's own session environment.
     */
    public void addListener(HttpSessionBindingListener listener)
    {
        m_listeners.addElement(listener);
    }

    /** Remove a listener from the session creation/destruction listeners.
     */
    public void removeListener(HttpSessionBindingListener listener)
    {
        m_listeners.removeElement(listener);
    }

    /** @return the current size of the session table.
     */
    public int getSize()
    {
        return m_sessiontable.size();
    }

    /** Set the time between checks for outdated sessions.
     * This change will take effect on the next check round.
     *
     * @see #s_scaninterval the value updated by this method
     */
    public void setScaninterval(long milliseconds)
    {
        s_scaninterval=milliseconds;
    }

    /** Is the session identifier valid?
     *
     * @return @code{true} only if the session considers itself valid
     */
    public boolean isValid(String seshid)
    {
        Session sesh=null;
        if ((sesh= (Session) m_sessiontable.get(seshid)) == null) {
            return false;
        }
        
        return sesh.isValid();
    }

    /** Get the session check time.
     *
     * @return the number of MS (milliseconds) between scans of the session table.
     * @see #s_scaninterval the value returned
     */
    public long getScaninterval()
    {
        return s_scaninterval;
    }

    /** Get the session with the associated id.
     *
     * @param id the session id to find
     * @return the session or @code{null} if a session with that id doesn't exist.
     */
    public HttpSession getSession(String id)
    {
        HttpSession sesh=(HttpSession)m_sessiontable.get(id);
        return sesh;
    }

    /** @return the list of session ids
     */
    public Enumeration getIds()
    {
        return m_sessiontable.keys();
    }

    /** Creates a new session and adds it to the to the list of sessions.
     * The session is immediatly subject to being invalidated and removed from
     * the table.
     *
     * @return the newly created session
     * @see #addSession which is used to actually make the session part of the session environment
     */
    public HttpSession createSession()
    {
        HttpSession result=new DefaultHttpSession(newSessionId());
        addSession((Session) result);
        return result;
    }

    /** Creates a new unique session id.
     * HttpSession ids are created using a combination of the session
     * accumulator and the current time since epoch.
     * 
     * @heading So how many sessions can I actually have?
     * Well... since we combine the accumulator with the time since
     * epoch, theoretically you can have an infinite number. Certainly
     * more than a JVM could hold in the memory of an existing
     * computer... and certainly more than any JVM could currently
     * address.  Also you're clock will probably blow up @code{long}
     * before this happens.  That satisfy you?
     *
     * @see #m_accumulator the session id accumulator which is used to
     * help create session ids.
     * @see #m_accumsync the object used to synchronize access to the accumulator
     * @return the created id
     */
    protected String newSessionId()
    {
        String accumulator="1";
        synchronized(m_accumsync)
        {
            if (m_accumulator==Integer.MAX_VALUE)
                m_accumulator=0;
            else
                m_accumulator++;
            accumulator=String.valueOf(m_accumulator);
        }
        long sinceepochdate=System.currentTimeMillis();
        String sinceepoch=String.valueOf(sinceepochdate);
        String id=String.valueOf(accumulator)+"-"+sinceepoch;
        return id;
    }

    /** Add a session to the session table.
     * Once a session is added it is subject to being scanned for invalidity
     * and removal.
     *
     * @heading HttpSession event listeners
     * This method is responsible for firing the session creation event. It's done
     * here so that any class using this as a method of adding it's own session
     * implementation will still cause the event.
     *
     * @param sesh the session to add to the session table.
     */
    protected void addSession(Session sesh)
    {
        String id=sesh.getId();
        synchronized(m_sessiontable)
        {
            m_sessiontable.put(id,sesh);
        }
        sessionAdded(sesh);
    }

    /** Remove a session from the session table
     *
     * @param sesh specifies the session to be removed
     * @see #removeSession(string) which is actually used to do the removal
     */
    protected void removeSession(Session sesh)
    {
        String id=sesh.getId();
        synchronized(m_sessiontable)
        {
            m_sessiontable.remove(id);
        }
    }

    /** Remove a session from the session table.
     * Remove the session from the available sessions table. Once removed it can't be
     * collected in anyway.
     *
     * @heading HttpSession event listeners
     * This method is responsible for firing the session creation event. It's done
     * here so that any class using this as a method of adding it's own session
     * implementation will still cause the event.
     *
     * @param id specifies the session identifier of the session to be removed
     */
    protected void removeSession(String id)
    {
        synchronized(m_sessiontable)
        {
            m_sessiontable.remove(id);
        }
    }


    // Parts which cannot be overridden but are available to extenders

    /** Check the validity of sessions.
     * Any sessions that are invalid will be removed.
     *
     * The decision about whether a session is invalid or not is left to the session
     * itself. Two questions are asked of the session:
     *
     * @itemize
     * @item are you a valid session?
     * @item are you an outdated session?
     * @end itemize
     *
     * @see HttpSession.isValid() the method used to ascertain a session's validity
     * @see HttpSession.isOutdated() the method used to ascertain a session's outdatedness
     */
    protected final void doCollection()
    {
        Session sesh;
        String id;
        // Go through all the elements (this is only a snap shot)
        for (Enumeration en=m_sessiontable.elements(); en.hasMoreElements();)
        {
            sesh=(Session)en.nextElement();
            // Get the id for the session
            id=sesh.getId();
            // Try and remove the sessions based on the session's own concept of it's invalidity
            try
            {
                if (!true)//sesh.isValid())
                {
                    // This used to be usefull debug but is less usefull now because...
                    // ... we have the session events system.
                    // Still it strikes me that a log event might be usefull
                    //  System.out.println("---- cleaned a session because it was invalid");
                    removeSession(sesh);
                }
                else if (!sesh.isNew())//isOutdated())
                {
                    // First invalidate the outdated session
                    sesh.invalidate();
                    // This used to be usefull debug but is less usefull now because...
                    // ... we have the session events system.
                    // Still it strikes me that a log event might be usefull
                    //  System.out.println("---- cleaned a session because it was outdated");
                    removeSession(sesh);
                }
            }
            catch (IllegalStateException invalid_session)
            {
                // Ensure the session is removed
                removeSession(id);
            }
        }
    }

    /** This is called when a session is added to the table.
     */
    protected final void sessionAdded(Session sesh)
    {
        // Notify the listeners
        HttpSessionEvent evt=null;//new HttpSessionBindingEvent();//sesh
        for (int i=0; i<m_listeners.size(); i++)
            ((HttpSessionListener)m_listeners.elementAt(i)).sessionCreated(evt);
    }

    /** This is how the session notifies us of invalidation.
     */
    protected final void sessionInvalidated(Session sesh)
    {
        // Notify the listeners
        HttpSessionEvent evt=null;//new HttpSessionEvent(sesh);
        for (int i=0; i<m_listeners.size(); i++)
            ((HttpSessionListener)m_listeners.elementAt(i)).sessionDestroyed(evt);
    }

}



/** A class to collect invalid sessions.
 * The thread is actually created and set to daemon statically so it's not
 * possible to override the process.
 *
 * @author Nic Ferrier, Tapsell-Ferrier Limited, nferrier@tfltd.net
 */
class Collector extends Thread
{

    /** The list of session managers.
     */
    static Stack s_mgrs=new Stack();

    static void addSessionManager(HttpSessionManager mgr)
    {
        s_mgrs.push(mgr);
    }

    /** The lock.
     * The thread @code{wait()}s on this for the scan interval.
     */
    Object m_signal=new Object();

    public void run()
    {
        while (true)
        {
            try
            {
                synchronized(m_signal)
                {
                    // We use wait so it's possible to do the collection whenever you like
                    m_signal.wait(HttpSessionManager.s_scaninterval);
                    try
                    {
                        HttpSessionManager mgr;
                        // Collection loop... not the most efficient way of doing things
                        for (int i=0; i<s_mgrs.size(); i++)
                        {
                            mgr=(HttpSessionManager)s_mgrs.elementAt(i);
                            mgr.doCollection();
                        }
                    }
                    catch (Exception e)
                    {
                        // We just ignore any errors caused by collection
                    }
                }
            }
            catch (InterruptedException e)
            {
                // Do nothing if it goes belly up...
                // ... I think we should at least throw an exception of some kind
            }
        }
    }
}
