/*
 * StreamUtilities.java
 *
 * Created on November 25, 2006, 10:59 AM
 */

package org.nelsnelson.toolbox.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nelsnelson
 */
public class StreamUtils {
    public static final String UTF_8 = "UTF-8";
    private StreamUtils() {
        
    }
    
    public static void stream(InputStream in, File file) {
        int data = -1;
        
        try {
            FileOutputStream out = new FileOutputStream(file);
            
            while((data = in.read()) > -1) {
                out.write(data);
            }
            
            in.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public static byte[] stream(InputStream in) {
        int data = -1;
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            while((data = in.read()) > -1) {
                out.write(data);
            }
            
            in.close();
        }
        catch (IOException ex) {
            
        }
        
        return out.toByteArray();
    }

    /**
     * Reads data from the input and writes it to the output, until the end of the input
     * stream.
     * 
     * @param in
     * @param out
     * @param bufSizeHint
     * @throws IOException
     */
    public static void copyPipe(InputStream in, OutputStream out, int bufSizeHint)
            throws IOException 
    {
        int read = -1;
        byte[] buf = new byte[bufSizeHint];
        while ((read = in.read(buf, 0, bufSizeHint)) >= 0) {
            out.write(buf, 0, read);
        }
        out.flush();
    }
    
    public static boolean pipe(URL source, File destination) {
        BufferedInputStream fin = null;
        BufferedOutputStream fout = null;
        try {
            int bufSize = 8 * 1024;
            fin = new BufferedInputStream(source.openConnection().getInputStream(), bufSize);
            fout = new BufferedOutputStream(new FileOutputStream(destination), bufSize);
            StreamUtils.copyPipe(fin, fout, bufSize);
        }
        catch (IOException ioex) {
            return false;
        }
        catch (SecurityException sx) {
            return false;
        }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                }
                catch (IOException cioex) {
                }
            }
            if (fout != null) {
                try {
                    fout.close();
                }
                catch (IOException cioex) {
                }
            }
        }
        return true;
    }
    
    public static boolean pipe(File source, File destination) {
        BufferedInputStream fin = null;
        BufferedOutputStream fout = null;
        try {
            int bufSize = 8 * 1024;
            fin = new BufferedInputStream(new FileInputStream(source), bufSize);
            fout = new BufferedOutputStream(new FileOutputStream(destination), bufSize);
            StreamUtils.copyPipe(fin, fout, bufSize);
        }
        catch (IOException ioex) {
            return false;
        }
        catch (SecurityException sx) {
            return false;
        }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                }
                catch (IOException cioex) {
                }
            }
            if (fout != null) {
                try {
                    fout.close();
                }
                catch (IOException cioex) {
                }
            }
        }
        return true;
    }
    
    public static String getCharset(String locale) {
        String charset = null;
        Map charsets = getCharsets();
        
        if (charsets.containsKey(locale)) {
            Object item = charsets.get(locale);
            
            if (item instanceof String) {
                charset = (String) item;
            }
        }
        else {
            Object item = charsets.get("");
            
            if (item instanceof String) {
                charset = (String) item;
            }
        }
        
        return charset;
    }

    // TODO Get these from a charset manager that is linked to the 
    // generic application properties
    public static Map getCharsets() {
        return charsets;
    }
    
    public static final String DEFAULT_CHARSET = "default.charset";
    
    public static final Map charsets = new LinkedHashMap();
    
    static {
        charsets.put(DEFAULT_CHARSET, UTF_8);
    }

    /**
     * The name of this method is inspired by the Unix command line utility
     * <code>cat</code>.
     *
     * @see File#cat(InputStream, OutputStream)
     */
    public static void cat(final InputStream in, final OutputStream out)
    throws IOException {
        if (in == null || out == null)
            throw new NullPointerException();

        // Note that we do not use PipedInput/OutputStream because these
        // classes are slooowww. This is partially because they are using
        // Object.wait()/notify() in a suboptimal way and partially because
        // they copy data to and from an additional buffer byte array, which
        // is redundant if the data to be transferred is already held in
        // another byte array.
        // As an implication of the latter reason, although the idea of
        // adopting the pipe concept to threads looks tempting it is actually
        // bad design: Pipes are a good means of interprocess communication,
        // where processes cannot access each others data directly without
        // using an external data structure like the pipe as a commonly shared
        // FIFO buffer.
        // However, threads are different: They share the same memory and thus
        // we can use much more elaborated algorithms for data transfer.

        // Finally, in this case we will simply cycle through an array of
        // byte buffers, where an additionally created reader executor will fill
        // the buffers with data from the input and the current executor will
        // flush the filled buffers to the output.

        final Buffer[] buffers = allocateBuffers();

        /*
         * The task that cycles through the buffers in order to fill them
         * with input.
         */
        class Reader implements Runnable {
            /** The index of the next buffer to be written. */
            int off;

            /** The number of buffers filled with data to be written. */
            int len;

            /** The IOException that happened in this task, if any. */
            volatile IOException exception;

            public void run() {
                // Cache some data for better performance.
                final InputStream _in = in;
                final Buffer[] _buffers = buffers;
                final int _buffersLen = buffers.length;

                // The writer executor interrupts this executor to signal
                // that it cannot handle more input because there has been
                // an IOException during writing.
                // We stop processing in this case.
                int read;
                do {
                    // Wait until a buffer is available.
                    final Buffer buffer;
                    synchronized (this) {
                        while (len >= _buffersLen) {
                            try {
                                wait();
                            } catch (InterruptedException interrupted) {
                                return;
                            }
                        }
                        buffer = _buffers[(off + len) % _buffersLen];
                    }

                    // Fill buffer until end of file or buffer.
                    // This should normally complete in one loop cycle, but
                    // we do not depend on this as it would be a violation
                    // of InputStream's contract.
                    final byte[] buf = buffer.buf;
                    try {
                        read = _in.read(buf, 0, buf.length);
                    } catch (IOException ex) {
                        read = -1;
                        exception = ex;
                    }
                    if (Thread.interrupted())
                        read = -1; // throws away buf - OK in this context
                    buffer.read = read;

                    // Advance head and notify writer.
                    synchronized (this) {
                        len++;
                        notify(); // only the writer could be waiting now!
                    }
                } while (read != -1);
            }
        } // class Reader

        try {
            final Reader reader = new Reader();
            final Task task = getTask(reader);

            // Cache some data for better performance.
            final int buffersLen = buffers.length;

            int write;
            while (true) {
                // Wait until a buffer is available.
                final int off;
                final Buffer buffer;
                synchronized (reader) {
                    while (reader.len <= 0) {
                        try {
                            reader.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                    off = reader.off;
                    buffer = buffers[off];
                }

                // Stop on last buffer.
                write = buffer.read;
                if (write == -1)
                    break; // reader has terminated because of EOF or exception

                // Process buffer.
                final byte[] buf = buffer.buf;
                try {
                    out.write(buf, 0, write);
                } catch (IOException ex) {
                    // Cancel reader thread synchronously.
                    // Cancellation of the reader thread is required
                    // so that a re-entry to the cat(...) method by the same
                    // thread cannot not reuse the same shared buffers that
                    // an unfinished reader thread of a previous call is
                    // still using.
                    task.cancel();
                    throw ex;
                }

                // Advance tail and notify reader.
                synchronized (reader) {
                    reader.off = (off + 1) % buffersLen;
                    reader.len--;
                    reader.notify(); // only the reader could be waiting now!
                }
            }

            if (reader.exception != null)
                throw reader.exception;
        } finally {
            releaseBuffers(buffers);
        }
    }

    private static final Buffer[] allocateBuffers() {
        synchronized (Buffer.list) {
            Buffer[] buffers;
            for (Iterator i = Buffer.list.iterator(); i.hasNext(); ) {
                buffers = (Buffer[]) ((Reference) i.next()).get();
                i.remove();
                if (buffers != null)
                    return buffers;
            }
        }

        // A minimum of two buffers is required.
        // The actual number is optimized to compensate for oscillating
        // I/O bandwidths like e.g. with network shares.
        final Buffer[] buffers = new Buffer[4];
        for (int i = buffers.length; --i >= 0; )
            buffers[i] = new Buffer();
        return buffers;
    }

    private static final void releaseBuffers(Buffer[] buffers) {
        synchronized (Buffer.list) {
            Buffer.list.add(new SoftReference(buffers));
        }
    }

    //
    // Static member classes and interfaces.
    //

    private static class Buffer {
        /**
         * Each entry in this list holds a soft reference to an array
         * initialized with instances of this class.
         */
        static final List list = new LinkedList();

        /** The byte buffer used for asynchronous reading and writing. */
        byte[] buf = new byte[64 * 1024]; // TODO: Reuse FLATER_BUF_LENGTH of org.nelsnelson.toolbox.util.jar.de.schlichtherle.util.zip.ZipConstants

        /** The actual number of bytes read into the buffer. */
        int read;
    }

    public static Task getTask(final Runnable target) {
        assert target != null;
        return new Task(target);
    }

    private static final class Task {
        private final Thread thread;

        private Task(final Runnable target) {
            assert target != null;
            thread = new Thread(target, target.toString());
            thread.start();
        }

        public void cancel() {
            thread.interrupt();
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
