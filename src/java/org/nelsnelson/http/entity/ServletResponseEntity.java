package org.nelsnelson.http.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;

public class ServletResponseEntity extends AbstractHttpEntity {
    private ServletOutputStream out = null;
    private java.io.ByteArrayOutputStream buffer = null;
    private InputStream content;
    private boolean contentObtained;
    private long length;
    
    /**
     * Creates a new servlet response entity.
     * The content is initially missing, the content length
     * is set to a negative number.
     */
    public ServletResponseEntity() {
        super();
        this.length = -1;
    }
    
    public void flushBuffer() throws IOException {
        getBuffer().flush();
    }
    
    public void resetBuffer() {
        getBuffer().reset();
    }
    
    public int getBufferSize() {
        return getBuffer().size();
    }
    
    public void setBufferSize(int size) {
        //byte[] b = getBuffer().toByteArray();
        //buffer = new java.io.ByteArrayOutputStream(size);
        //buffer.write(b, 0, size);
    }
    
    public ServletOutputStream getOutputStream() {
        if (out == null) {
            out = new BufferedServletOutputStream();
        }
        
        return out;
    }
    
    private java.io.ByteArrayOutputStream getBuffer() {
        if (buffer == null) {
            buffer = new java.io.ByteArrayOutputStream();
        }
        
        return buffer;
    }
    
    public InputStream getContent() throws IllegalStateException {
        try {
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        if (!contentObtained) {
            try {
                getBuffer().flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            
            byte[] buf = getBuffer().toByteArray();
            this.content = new ByteArrayInputStream(buf, 0, buf.length);
            this.contentObtained = true;
        }
        
        return content;
    }
    
    // non-javadoc, see interface HttpEntity
    public long getContentLength() {
        int size = getBuffer().size();
        
        if (this.length > 0) {
            size = (int) Math.min((long) size, this.length);
        }
        
        this.length = size;
        
        return size;
    }
    
    /**
     * Tells that this entity is repeatable.
     *
     * @return <code>false</code>
     */
    public boolean isRepeatable() {
        return true;
    }
    
    /**
     * Specifies the length of the content.
     *
     * @param len       the number of bytes in the content, or
     *                  a negative number to indicate an unknown length
     */
    public void setContentLength(long len) {
        this.length = len;
    }
    
    /**
     * Specifies the content.
     *
     * @param instream          the stream to return with the next call to
     *                          {@link #getContent getContent}
     */
    public void setContent(final InputStream instream) {
        this.content = instream;
        this.contentObtained = false; 
    }
    
    // non-javadoc, see interface HttpEntity
    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        InputStream instream = getContent();
        int l;
        byte[] tmp = new byte[2048];
        while ((l = instream.read(tmp)) != -1) {
            outstream.write(tmp, 0, l);
        }
    }
    
    // non-javadoc, see interface HttpEntity
    public boolean isStreaming() {
        return !this.contentObtained && this.content != null;
    }
    
    // non-javadoc, see interface HttpEntity
    public void consumeContent() throws IOException {
        if (content != null) {
            content.close(); // reads to the end of the entity
        }
    }
    
    class BufferedServletOutputStream extends ServletOutputStream {
        private WriteListener writeListener = null;

        public void write(int b) throws IOException {
            getBuffer().write(b);
        }

        public void setWriteListener(WriteListener writeListener) {
            this.writeListener = writeListener;
        }
        
        public boolean isReady() {
            int avail = getBuffer().size();
            return (avail > 0);
        }
        
        public boolean isFinished() {
            int avail = getBuffer().size();
            return (avail == 0);
        }
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        this.content.close();
    }
}
