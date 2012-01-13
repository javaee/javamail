/*
 * Copyright (c) 2009-2012 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2012 Jason Mehrens. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.*;
import java.security.*;
import java.util.logging.*;

/**
 * An error manager used to store mime messages from the <tt>MailHandler</tt>
 * to the file system when the email server is unavailable or unreachable.
 * The code to manually setup this error manager can be as simple as the
 * following:
 * <tt><pre>
 *      File dir = new File("path to dir");
 *      FileErrorManager em = new FileErrorManager(dir);
 * </pre></tt>
 *
 * <p>
 * <b>Configuration:</b>
 * The code to setup this error manager via the logging properties
 * can be as simple as the following:
 * <tt><pre>
 *      #Default FileErrorManager settings.
 *      FileErrorManager.pattern = path to directory
 * </pre></tt>
 *
 * If properties are not defined, or contain invalid values, then the specified
 * default values are used.
 * <ul>
 * <li>FileErrorManager.pattern the absolute file path to the directory which 
 * will store any failed email messages. (defaults to the value of the system
 * property <tt>java.io.tmpdir</tt>)
 * </ul>
 *
 * @author Jason Mehrens
 */
public class FileErrorManager extends ErrorManager {

    /**
     * Stores the LogManager.
     */
    private static final LogManager manager = LogManager.getLogManager();
    /**
     * Used to report errors that this error manager fails to report.
     */
    private final ErrorManager next = new ErrorManager();
    /**
     * Directory of the email store.
     */
    private final File emailStore;

    /**
     * Creates a new error manager.  Files are stored in the users temp 
     * directory.
     * @exception SecurityException if unable to access system properties or
     * if a security manager is present and unable to read or write to users
     * temp directory.
     */
    public FileErrorManager() {
        this.emailStore = getEmailStore();
        init();
    }

    /**
     * Creates a new error manager.
     * @param dir a directory to store the email files.
     * @throws NullPointerException if <tt>dir</tt> is <tt>null</tt>
     * @throws IllegalArgumentException if <tt>dir</tt> is a
     * <tt>java.io.File</tt> subclass, not a directory, or is not an absolute
     * path.
     * @throws SecurityException if a security manager is present and unable
     * to read or write to a given directory.
     */
    public FileErrorManager(File dir) {
        this.emailStore = dir;
        init();
    }

    /**
     * If the message parameter is a raw email, and passes the store term, then
     * this method will store the email to the file system.  If the message
     * parameter is not a raw email then the message is forwarded to the super
     * class. If an email is written to the file system without error, then the
     * original reported error is ignored.
     * @param msg String raw email or plain error message.
     * @param ex Exception that occurred in the mail handler.
     * @param code int error manager code.
     */
    public void error(String msg, Exception ex, int code) {
        if (isRawEmail(msg, ex, code)) {
            try {
                storeEmail(msg);
            } catch (final IOException IOE) {
                super.error(emailStore.toString(), IOE, ErrorManager.WRITE_FAILURE);
                next.error(msg, ex, code);
            } catch (final RuntimeException RE) {
                super.error(emailStore.toString(), RE, ErrorManager.WRITE_FAILURE);
                next.error(msg, ex, code);
            }
        } else {
            next.error(msg, ex, code);
        }
    }

    private void init() {
        if (next == null) {
            throw new NullPointerException(ErrorManager.class.getName());
        }

        File dir = this.emailStore;
        if (dir.getClass() != File.class) { //for security.
            throw new IllegalArgumentException(dir.getClass().getName());
        }

        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("File must be a directory.");
        }

        //For now, only absolute paths are allowed.
        if (!dir.isAbsolute()) {
            throw new IllegalArgumentException("Only absolute paths are allowed.");
        }

        if (!dir.canRead()) { //Can throw under a security manager.
            super.error(dir.getAbsolutePath(),
                    new SecurityException("read"), ErrorManager.OPEN_FAILURE);
        }

        if (!dir.canWrite()) { //Can throw under a security manager.
            super.error(dir.getAbsolutePath(),
                    new SecurityException("write"), ErrorManager.OPEN_FAILURE);
        }
    }

    private String prefixName() {
        return "FileErrorManager";
    }

    private String suffixName() {
        return ".eml";
    }

    private boolean isRawEmail(String msg, Exception ex, int code) {
        if (msg != null && msg.length() > 0) {
            return !msg.startsWith(Level.SEVERE.getName());
        }
        return false;
    }

    private void storeEmail(String email) throws IOException {
        File tmp = null;
        FileOutputStream out = null;
        for (;;) {
            tmp = File.createTempFile(prefixName(), suffixName(), emailStore);
            try {
                out = new FileOutputStream(tmp);
                break;
            } catch (FileNotFoundException FNFE) {
                if (!tmp.exists()) { //retry if file is locked
                    throw FNFE;
                }
            }
        }

        try { //Raw email is ASCII.
            PrintStream ps = new PrintStream(
                    new NewlineOutputStream(out), false, "US-ASCII");
            ps.print(email);
            ps.flush();
            tmp = null; //Don't delete 'tmp' if all bytes were written.
            ps.close();
        } finally {
            close(out);
            delete(tmp); //Only deletes if not null.
        }
    }

    private void close(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException IOE) {
                super.error(out.toString(), IOE, ErrorManager.CLOSE_FAILURE);
            }
        }
    }

    private void delete(File tmp) {
        if (tmp != null) {
            try {
                if (!tmp.delete() && tmp.exists()) {
                    try {
                        try {
                            tmp.deleteOnExit();
                        } catch (final LinkageError shutdown) {
                            throw new RuntimeException(shutdown);
                        }
                    } catch (final RuntimeException shutdown) {
                        if (!tmp.delete()) {
                            super.error(tmp.getAbsolutePath(), shutdown,
                                    ErrorManager.CLOSE_FAILURE);
                        }
                    }
                }
            } catch (SecurityException SE) {
                super.error(tmp.toString(), SE, ErrorManager.CLOSE_FAILURE);
            }
        }
    }

    /**
     * Gets the location of the email store.
     * @return the File location.
     */
    private File getEmailStore() {
        String dir = manager.getProperty(
                getClass().getName().concat(".pattern"));
        if (dir == null) {
            dir = (String) AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    return System.getProperty("java.io.tmpdir", "");
                }
            });
        }
        return new File(dir);
    }
}
