package org.nelsnelson.http.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DynamicServlet 
    extends HttpServlet
{
    private File jspFile = null;
    public DynamicServlet(String jspFile) {
        this.jspFile = new File(jspFile);
    }

    public void doGet(HttpServletRequest request, 
        HttpServletResponse response) 
        throws ServletException, IOException 
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(this.toString());
    }

    public void doPost(HttpServletRequest request, 
        HttpServletResponse response) 
        throws ServletException, IOException 
    {
        doGet(request, response);
    }
}
