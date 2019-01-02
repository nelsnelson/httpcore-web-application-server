package org.nelsnelson.http.session;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

public class SessionFactory {
    private static SessionFactory instance = null;
    
    private static MessageDigest md5Digester = null;
    protected static DateFormat headerDF = 
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    
    protected static Random rnd = null;
    static {
        headerDF.setTimeZone(TimeZone.getTimeZone("GMT"));
        rnd = new Random(System.currentTimeMillis());
    }
    
    private static int sessionTimeout = 1440;
    
    private SessionFactory() {
        try {
            md5Digester = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
    public static SessionFactory getInstance() {
        if (instance == null) {
            instance = new SessionFactory();
        }
        
        return instance;
    }
    
    public String generateSessionId() {
        String cookieValue = 
            "Rummage_" + "remoteIP" + "_" + "serverPort" + "_" + 
            System.currentTimeMillis() + rnd.nextLong();
        
        byte digestBytes[] = md5Digester.digest(cookieValue.getBytes());
        
        // Write out in hex format
        char outArray[] = new char[32];
        for (int n = 0; n < digestBytes.length; n++) {
            int hiNibble = (digestBytes[n] & 0xFF) >> 4;
            int loNibble = (digestBytes[n] & 0xF);
            
            outArray[2 * n] = 
                (hiNibble > 9 ? (char) (hiNibble + 87) : (char) (hiNibble + 48));
            
            outArray[2 * n + 1] = 
                (loNibble > 9 ? (char) (loNibble + 87) : (char) (loNibble + 48));
        }
        
        String newSessionId = new String(outArray);
        
        return newSessionId;
    }
    
    public static HttpSession createSession() {
        String sessionId = getInstance().generateSessionId();
        
        DefaultHttpSession session = new DefaultHttpSession(sessionId);
        setSessionListeners(session);
        
        if (sessionTimeout > 0) {
            session.setMaxInactiveInterval(sessionTimeout * 60);
        }
        else {
            session.setMaxInactiveInterval(-1);
        }
        
        session.setLastAccessedDate(System.currentTimeMillis());
        
        return session;
    }
    
    public static void setSessionListeners(DefaultHttpSession session) {
        //session.setSessionActivationListeners(sessionActivationListeners);
        //session.setSessionAttributeListeners(sessionAttributeListeners);
        //session.setSessionListeners(sessionListeners);
    }
}
