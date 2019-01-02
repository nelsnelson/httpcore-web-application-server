package org.nelsnelson.http.x.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author nelsnelson
 */
public class SessionProvider extends HttpServlet {
    /**
     * Generated serial version id
     */
    private static final long serialVersionUID = 3983509741780874409L;
    
    public void doGet(HttpServletRequest request, 
        HttpServletResponse response) 
        throws ServletException, IOException 
    {
        HttpSession session = request.getSession();
        Enumeration<String> names = session.getAttributeNames();
        
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();
        
        JsonObject o = JsonObject.EMPTY_JSON_OBJECT;
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        for (String name : Collections.list(names)) {
            String value = (String) session.getAttribute(name);
            
            try {
                builder.add(name, value);
            }
            catch (JsonException e) {
                e.printStackTrace();
            }
        }
        
        o = builder.build();
        
        out.println(o.toString());
    }
    
    public void doPost(HttpServletRequest request, 
        HttpServletResponse response) 
        throws ServletException, IOException 
    {
        doGet(request, response);
    }
}
