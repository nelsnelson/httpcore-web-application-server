package org.nelsnelson.http.entity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;

public class ServletRequestEntity extends AbstractHttpEntity {
    private HttpEntity entity = null;
    private ServletInputStream in = null;
    private InputStream entityContent = null;
    private ContentType contentType = null;
    private String contentEncoding = null;
    
    public ServletRequestEntity(HttpEntity entity) {
        this.entity = entity;
        this.contentType = ContentType.create(entity.getContentType());
        this.contentEncoding = entity.getContentEncoding();
        
        try {
            this.entityContent = entity.getContent();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public ServletInputStream getInputStream() {
        in = new BufferedServletInputStream();
        
        return in;
    }
    public InputStream getContent() throws IOException, IllegalStateException {
        return getInputStream();
    }

    public long getContentLength() {
        long contentLength = 0l;
        if (entity == null) {
            if (this.entityContent == null) {
                contentLength = entity.getContentLength();
            }
            else {
                int count = 0;
                try {
                    count = this.entityContent.available();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                contentLength = Integer.valueOf(count).longValue();
            }
        }
        return contentLength;
    }

    public boolean isRepeatable() {
        // This is true by definition because this is a ServletRequestEntity 
        // and contains data from a POST request so there will be more than 
        // one interested party.
        return true;
    }

    public boolean isStreaming() {
        return entity.isStreaming();
    }

    public void writeTo(OutputStream outstream) throws IOException {
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
    
    class BufferedServletInputStream extends ServletInputStream {
        private InputStream buffer = null;
        private ReadListener readListener = null;
        
        public BufferedServletInputStream() {
            try {
                buffer = new BufferedInputStream(entity.getContent());
            }
            catch (UnsupportedOperationException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void setReadListener(ReadListener readListener) {
            this.readListener = readListener;
        }
        
        public boolean isReady() {
            int avail = -1;
            try {
                avail = this.buffer.available();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return (avail > 0);
        }
        
        public boolean isFinished() {
            int avail = -1;
            try {
                avail = this.buffer.available();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return (avail == 0);
        }
        
        public int read() throws IOException {
            if (this.buffer.available() == 0) {
                this.buffer.close();
                return -1;
            }
            else {
                return this.buffer.read();
            }
        }
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        this.entityContent.close();
    }
}
