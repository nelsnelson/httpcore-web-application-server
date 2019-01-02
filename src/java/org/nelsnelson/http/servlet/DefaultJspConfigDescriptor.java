package org.nelsnelson.http.servlet;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;

/**
 * 
 * @author nelsnelson
 *
 */
public class DefaultJspConfigDescriptor implements JspConfigDescriptor {
    java.util.Collection<JspPropertyGroupDescriptor> jspPropertyGroups = null; 
    java.util.Collection<TaglibDescriptor> taglibs = null;
    
    public DefaultJspConfigDescriptor() {
        this.jspPropertyGroups = new ArrayList<JspPropertyGroupDescriptor>();
        this.taglibs = new ArrayList<TaglibDescriptor>();
    }

    public Collection<TaglibDescriptor> getTaglibs() {
        return this.taglibs;
    }

    public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
        return this.jspPropertyGroups;
    }
}
