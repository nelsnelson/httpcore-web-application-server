package org.nelsnelson.http.util;

import javax.servlet.http.HttpSession;

public class SessionUtils {
    public static void append(HttpSession session, String name, String s) {
        String value = (String) session.getValue(name);
        value = (value == null ? "" : value) + s;
        session.putValue(name, value);
    }
}
