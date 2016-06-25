/*
 * Copyright (c) 2009-2016 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2016 Jason Mehrens. All Rights Reserved.
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

import com.sun.mail.util.logging.CollectorFormatter;
import com.sun.mail.util.logging.DurationFilter;
import com.sun.mail.util.logging.MailHandler;
import com.sun.mail.util.logging.SeverityComparator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.logging.*;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

/**
 * Demo for the different configurations for the MailHandler. If the logging
 * properties file or class is not specified then this demo will apply some
 * default settings to store emails in the user's temp directory.
 *
 * @author Jason Mehrens
 */
public class MailHandlerDemo {

    /**
     * This class name.
     */
    private static final String CLASS_NAME = MailHandlerDemo.class.getName();
    /**
     * The logger for this class name.
     */
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Runs the demo.
     *
     * @param args the command line arguments
     * @throws IOException if there is a problem.
     */
    public static void main(String[] args) throws IOException {
        List<String> l = Arrays.asList(args);
        if (l.contains("/?") || l.contains("-?") || l.contains("-help")) {
            LOGGER.info("Usage: java MailHandlerDemo "
                    + "[[-all] | [-body] | [-custom] | [-debug] | [-low] "
                    + "| [-simple] | [-pushlevel] | [-pushfilter] "
                    + "| [-pushnormal] | [-pushonly]] "
                    + "\n\n"
                    + "-all\t\t: Execute all demos.\n"
                    + "-body\t\t: An email with all records and only a body.\n"
                    + "-custom\t\t: An email with attachments and dynamic names.\n"
                    + "-debug\t\t: Output basic debug information about the JVM "
                    + "and log configuration.\n"
                    + "-low\t\t: Generates multiple emails due to low capacity."
                    + "\n"
                    + "-simple\t\t: An email with all records with body and "
                    + "an attachment.\n"
                    + "-pushlevel\t: Generates high priority emails when the"
                    + " push level is triggered and normal priority when "
                    + "flushed.\n"
                    + "-pushFilter\t: Generates high priority emails when the "
                    + "push level and the push filter is triggered and normal "
                    + "priority emails when flushed.\n"
                    + "-pushnormal\t: Generates multiple emails when the "
                    + "MemoryHandler push level is triggered.  All generated "
                    + "email are sent as normal priority.\n"
                    + "-pushonly\t: Generates multiple emails when the "
                    + "MemoryHandler push level is triggered.  Generates high "
                    + "priority emails when the push level is triggered and "
                    + "normal priority when flushed.\n");
        } else {
            final boolean debug = init(l); //may create log messages.
            try {
                LOGGER.log(Level.FINEST, "This is the finest part of the demo.",
                        new MessagingException("Fake JavaMail issue."));
                LOGGER.log(Level.FINER, "This is the finer part of the demo.",
                        new NullPointerException("Fake bug."));
                LOGGER.log(Level.FINE, "This is the fine part of the demo.");
                LOGGER.log(Level.CONFIG, "Logging config file is {0}.",
                        getConfigLocation());
                LOGGER.log(Level.INFO, "Your temp directory is {0}, "
                        + "please wait...", getTempDir());

                try { //Waste some time for the custom formatter.
                    Thread.sleep(3L * 1000L);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                LOGGER.log(Level.WARNING, "This is a warning.",
                        new FileNotFoundException("Fake file chooser issue."));
                LOGGER.log(Level.SEVERE, "The end of the demo.",
                        new IOException("Fake access denied issue."));
            } finally {
                closeHandlers();
            }

            //Force parse errors.  This does have side effects.
            if (debug && getConfigLocation() != null) {
                LogManager.getLogManager().readConfiguration();
            }
        }
    }

    /**
     * Used debug problems with the logging.properties. The system property
     * java.security.debug=access,stack can be used to trace access to the
     * LogManager reset.
     *
     * @param prefix a string to prefix the output.
     * @param err any PrintStream or null for System.out.
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static void checkConfig(String prefix, PrintStream err) {
        if (prefix == null || prefix.trim().length() == 0) {
            prefix = "DEBUG";
        }

        if (err == null) {
            err = System.out;
        }

        try {
            err.println(prefix + ": java.version="
                    + System.getProperty("java.version"));
            err.println(prefix + ": LOGGER=" + LOGGER.getLevel());
            err.println(prefix + ": JVM id "
                    + ManagementFactory.getRuntimeMXBean().getName());
            err.println(prefix + ": java.security.debug="
                    + System.getProperty("java.security.debug"));
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                err.println(prefix + ": SecurityManager.class="
                        + sm.getClass().getName());
                err.println(prefix + ": SecurityManager classLoader="
                        + toString(sm.getClass().getClassLoader()));
                err.println(prefix + ": SecurityManager.toString=" + sm);
            } else {
                err.println(prefix + ": SecurityManager.class=null");
                err.println(prefix + ": SecurityManager.toString=null");
                err.println(prefix + ": SecurityManager classLoader=null");
            }

            String policy = System.getProperty("java.security.policy");
            if (policy != null) {
                File f = new File(policy);
                err.println(prefix + ": AbsolutePath=" + f.getAbsolutePath());
                err.println(prefix + ": CanonicalPath=" + f.getCanonicalPath());
                err.println(prefix + ": length=" + f.length());
                err.println(prefix + ": canRead=" + f.canRead());
                err.println(prefix + ": lastModified="
                        + new java.util.Date(f.lastModified()));
            }

            LogManager manager = LogManager.getLogManager();
            String key = "java.util.logging.config.file";
            String cfg = System.getProperty(key);
            if (cfg != null) {
                err.println(prefix + ": " + cfg);
                File f = new File(cfg);
                err.println(prefix + ": AbsolutePath=" + f.getAbsolutePath());
                err.println(prefix + ": CanonicalPath=" + f.getCanonicalPath());
                err.println(prefix + ": length=" + f.length());
                err.println(prefix + ": canRead=" + f.canRead());
                err.println(prefix + ": lastModified="
                        + new java.util.Date(f.lastModified()));
            } else {
                err.println(prefix + ": " + key
                        + " is not set as a system property.");
            }
            err.println(prefix + ": LogManager.class="
                    + manager.getClass().getName());
            err.println(prefix + ": LogManager classLoader="
                    + toString(manager.getClass().getClassLoader()));
            err.println(prefix + ": LogManager.toString=" + manager);
            err.println(prefix + ": MailHandler classLoader="
                    + toString(MailHandler.class.getClassLoader()));
            err.println(prefix + ": Context ClassLoader="
                    + toString(Thread.currentThread().getContextClassLoader()));
            err.println(prefix + ": Session ClassLoader="
                    + toString(Session.class.getClassLoader()));
            err.println(prefix + ": DataHandler ClassLoader="
                    + toString(DataHandler.class.getClassLoader()));

            final String p = MailHandler.class.getName();
            key = p.concat(".mail.to");
            String to = manager.getProperty(key);
            err.println(prefix + ": TO=" + to);
            if (to != null) {
                err.println(prefix + ": TO="
                        + Arrays.toString(InternetAddress.parse(to, true)));
            }

            key = p.concat(".mail.from");
            String from = manager.getProperty(key);
            if (from == null || from.length() == 0) {
                Session session = Session.getInstance(new Properties());
                InternetAddress local = InternetAddress.getLocalAddress(session);
                err.println(prefix + ": FROM=" + local);
            } else {
                err.println(prefix + ": FROM="
                        + Arrays.asList(InternetAddress.parse(from, false)));
                err.println(prefix + ": FROM="
                        + Arrays.asList(InternetAddress.parse(from, true)));
            }

            synchronized (manager) {
                final Enumeration<String> e = manager.getLoggerNames();
                while (e.hasMoreElements()) {
                    final Logger l = manager.getLogger(e.nextElement());
                    if (l != null) {
                        final Handler[] handlers = l.getHandlers();
                        if (handlers.length > 0) {
                            err.println(prefix + ": " + l.getClass().getName()
                                    + ", " + l.getName());
                            for (Handler h : handlers) {
                                err.println(prefix + ":\t" + toString(prefix, err, h));
                            }
                        }
                    }
                }
            }
        } catch (Throwable error) {
            err.print(prefix + ": ");
            error.printStackTrace(err);
        }
        err.flush();
    }

    /**
     * Gets the class loader list.
     *
     * @param cl the class loader or null.
     * @return the class loader list.
     */
    private static String toString(ClassLoader cl) {
        StringBuilder buf = new StringBuilder();
        buf.append(cl);
        while (cl != null) {
            cl = cl.getParent();
            buf.append("<-").append(cl);
        }
        return buf.toString();
    }

    /**
     * Gets a formatting string describing the given handler.
     *
     * @param prefix the output prefix.
     * @param err the error stream.
     * @param h the handler.
     * @return the formatted string.
     */
    private static String toString(String prefix, PrintStream err, Handler h) {
        StringBuilder buf = new StringBuilder();
        buf.append(h.getClass().getName());
        try {
            if (h instanceof MailHandler) {
                MailHandler mh = (MailHandler) h;
                buf.append(", ").append(mh.getSubject());
            }
        } catch (SecurityException error) {
            err.print(prefix + ": ");
            error.printStackTrace(err);
        }

        try {
            buf.append(", ").append(h.getFormatter());
        } catch (SecurityException error) {
            err.print(prefix + ": ");
            error.printStackTrace(err);
        }

        try {
            if (h instanceof MailHandler) {
                MailHandler mh = (MailHandler) h;
                buf.append(", ").append(Arrays.toString(
                        mh.getAttachmentFormatters()));
            }
        } catch (SecurityException error) {
            err.print(prefix + ": ");
            error.printStackTrace(err);
        }

        try {
            buf.append(", ").append(h.getLevel());
        } catch (SecurityException error) {
            err.print(prefix + ": ");
            error.printStackTrace(err);
        }

        try {
            buf.append(", ").append(h.getFilter());
        } catch (SecurityException error) {
            err.print(prefix + ": ");
            error.printStackTrace(err);
        }

        try {
            buf.append(", ").append(h.getErrorManager());
        } catch (SecurityException error) {
            err.print(prefix + ": ");
            error.printStackTrace(err);
        }

        buf.append(", ").append(toString(h.getClass().getClassLoader()));
        return buf.toString();
    }

    /**
     * Example for body only messages. On close the remaining messages are sent.      <code>
     * ##logging.properties
     * MailHandlerDemo.handlers=com.sun.mail.util.logging.MailHandler
     * com.sun.mail.util.logging.MailHandler.subject=Body only demo
     * ##
     * </code>
     */
    private static void initBodyOnly() {
        MailHandler h = new MailHandler();
        h.setSubject("Body only demo");
        LOGGER.addHandler(h);
    }

    /**
     * Example showing that when the mail handler reaches capacity it will
     * format and send the current records. Capacity is used to roughly limit
     * the size of an outgoing message. On close any remaining messages are
     * sent.      <code>
     * ##logging.properties
     * MailHandlerDemo.handlers=com.sun.mail.util.logging.MailHandler
     * com.sun.mail.util.logging.MailHandler.subject=Low capacity demo
     * com.sun.mail.util.logging.MailHandler.capacity=5
     * ##
     * </code>
     */
    private static void initLowCapacity() {
        MailHandler h = new MailHandler(5);
        h.setSubject("Low capacity demo");
        LOGGER.addHandler(h);
    }

    /**
     * Example for body only messages. On close any remaining messages are sent.      <code>
     * ##logging.properties
     * MailHandlerDemo.handlers=com.sun.mail.util.logging.MailHandler
     * com.sun.mail.util.logging.MailHandler.subject=Body and attachment demo
     * com.sun.mail.util.logging.MailHandler.attachment.formatters=java.util.logging.XMLFormatter
     * com.sun.mail.util.logging.MailHandler.attachment.names=data.xml
     * ##
     * </code>
     */
    private static void initSimpleAttachment() {
        MailHandler h = new MailHandler();
        h.setSubject("Body and attachment demo");
        h.setAttachmentFormatters(new XMLFormatter());
        h.setAttachmentNames("data.xml");
        LOGGER.addHandler(h);
    }

    /**
     * Example setup for priority messages by level. If the push level is
     * triggered the message is high priority. Otherwise, on close any remaining
     * messages are sent.      <code>
     * ##logging.properties
     * MailHandlerDemo.handlers=com.sun.mail.util.logging.MailHandler
     * com.sun.mail.util.logging.MailHandler.subject=Push level demo
     * com.sun.mail.util.logging.MailHandler.pushLevel=WARNING
     * ##
     * </code>
     */
    private static void initWithPushLevel() {
        MailHandler h = new MailHandler();
        h.setSubject("Push level demo");
        h.setPushLevel(Level.WARNING);
        LOGGER.addHandler(h);
    }

    /**
     * Example for priority messages by generation rate. If the push filter is
     * triggered the message is high priority. Otherwise, on close any remaining
     * messages are sent. If the capacity is set to the      <code>
     * ##logging.properties
     * MailHandlerDemo.handlers=com.sun.mail.util.logging.MailHandler
     * com.sun.mail.util.logging.MailHandler.subject=Push filter demo
     * com.sun.mail.util.logging.MailHandler.pushLevel=ALL
     * com.sun.mail.util.logging.MailHandler.pushFilter=com.sun.mail.util.logging.DurationFilter
     * com.sun.mail.util.logging.DurationFilter.records=2
     * com.sun.mail.util.logging.DurationFilter.duration=1 * 60 * 1000
     * ##
     * </code>
     */
    private static void initWithPushFilter() {
        MailHandler h = new MailHandler();
        h.setSubject("Push filter demo");
        h.setPushLevel(Level.ALL);
        h.setPushFilter(new DurationFilter(2, 1L * 60L * 1000L));
        LOGGER.addHandler(h);
    }

    /**
     * Example for circular buffer behavior. The level, push level, and capacity
     * are set the same so that the memory handler push results in a mail
     * handler push. All messages are high priority. On close any remaining
     * records are discarded because they never reach the mail handler.      <code>
     * ##logging.properties
     * MailHandlerDemo.handlers=java.util.logging.MemoryHandler
     * java.util.logging.MemoryHandler.target=com.sun.mail.util.logging.MailHandler
     * com.sun.mail.util.logging.MailHandler.level=ALL
     * java.util.logging.MemoryHandler.level=ALL
     * java.util.logging.MemoryHandler.push=WARNING
     * com.sun.mail.util.logging.MailHandler.subject=Push only demo
     * com.sun.mail.util.logging.MailHandler.pushLevel=WARNING
     * ##
     * </code>
     */
    private static void initPushOnly() {
        final int capacity = 3;
        final Level pushLevel = Level.WARNING;
        final MailHandler h = new MailHandler(capacity);
        h.setPushLevel(pushLevel);
        h.setSubject("Push only demo");
        MemoryHandler m = new MemoryHandler(h, capacity, pushLevel);
        h.setLevel(m.getLevel());
        LOGGER.addHandler(m);
        pushOnlyHandler = h;
    }

    /**
     * Holds on to the push only handler. Only declared here to apply fallback
     * settings.
     */
    private static Handler pushOnlyHandler;

    /**
     * Example for circular buffer behavior as normal priority. The push level,
     * and capacity are set the same so that the memory handler push results in
     * a mail handler push. All messages are normal priority. On close any
     * remaining records are discarded because they never reach the mail
     * handler. Use the LogManager config option or extend the MemoryHandler to
     * emulate this behavior via the logging.properties.
     */
    private static void initPushNormal() {
        final int capacity = 3;
        final MailHandler h = new MailHandler(capacity);
        h.setSubject("Push normal demo");
        MemoryHandler m = new MemoryHandler(h, capacity, Level.WARNING) {

            @Override
            public void push() {
                super.push();  //push to target.
                super.flush(); //make the target send the email.
            }
        };
        LOGGER.addHandler(m);
        pushNormalHandler = h;
    }

    /**
     * Holds on to the push normal handler. Only declared here to apply fallback
     * settings.
     */
    private static Handler pushNormalHandler;

    /**
     * Example for various kinds of custom sorting, formatting, and filtering
     * for multiple attachment messages. The subject will contain the most
     * severe record and a count of remaining records. The log records are
     * ordered from most severe to least severe. The body uses a custom
     * formatter that includes a summary by date and time. The attachment use
     * XML and plain text formats. Each attachment has a different set of
     * filtering. The attachment names are generated from either a fixed name or
     * are built using the number and type of the records formatted. On close
     * any remaining messages are sent. Use the LogManager config option or
     * extend the MemoryHandler to emulate this behavior via the
     * logging.properties.
     */
    private static void initCustomAttachments() {
        MailHandler h = new MailHandler();

        //Sort records by severity keeping the severe messages at the top.
        h.setComparator(Collections.reverseOrder(new SeverityComparator()));

        //Use subject to provide a hint as to what is in the email.
        h.setSubject(new CollectorFormatter());

        //Make the body give a simple summary of what happened.
        h.setFormatter(new SummaryFormatter());

        //Create 3 attachments.
        h.setAttachmentFormatters(new XMLFormatter(),
                new XMLFormatter(), new SimpleFormatter());

        //Filter each attachment differently.
        h.setAttachmentFilters(null,
                new DurationFilter(3L, 1000L),
                new DurationFilter(1L, 15L * 60L * 1000L));

        //Creating the attachment name formatters.
        h.setAttachmentNames(new CollectorFormatter("all.xml"),
                new CollectorFormatter("{3} records and {5} errors.xml"),
                new CollectorFormatter("{5,choice,0#no errors|1#1 error|1<"
                        + "{5,number,integer} errors}.txt"));

        LOGGER.addHandler(h);
    }

    /**
     * Sets up the demos that will run.
     *
     * @param l the list of arguments.
     * @return true if debug is on.
     */
    private static boolean init(List<String> l) {
        l = new ArrayList<String>(l);
        Session session = Session.getInstance(System.getProperties());
        boolean all = l.remove("-all") || l.isEmpty();
        if (l.remove("-body") || all) {
            initBodyOnly();
        }

        if (l.remove("-custom") || all) {
            initCustomAttachments();
        }

        if (l.remove("-low") || all) {
            initLowCapacity();
        }

        if (l.remove("-pushfilter") || all) {
            initWithPushFilter();
        }

        if (l.remove("-pushlevel") || all) {
            initWithPushLevel();
        }

        if (l.remove("-pushnormal") || all) {
            initPushNormal();
        }

        if (l.remove("-pushonly") || all) {
            initPushOnly();
        }

        if (l.remove("-simple") || all) {
            initSimpleAttachment();
        }

        boolean fallback = applyFallbackSettings();
        boolean debug = l.remove("-debug") || session.getDebug();
        if (debug) {
            checkConfig(CLASS_NAME, session.getDebugOut());
        }

        if (!l.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Unknown commands: {0}", l);
        }

        if (fallback) {
            LOGGER.info("Check your user temp dir for output.");
        }
        return debug;
    }

    /**
     * Close and remove all handlers added to the class logger.
     */
    private static void closeHandlers() {
        Handler[] handlers = LOGGER.getHandlers();
        for (Handler h : handlers) {
            h.close();
            LOGGER.removeHandler(h);
        }
    }

    /**
     * Apply some fallback settings if no configuration file was specified.
     *
     * @return true if fallback settings were applied.
     */
    private static boolean applyFallbackSettings() {
        if (getConfigLocation() == null) {
            LOGGER.setLevel(Level.ALL);
            Handler[] handlers = LOGGER.getHandlers();
            for (Handler h : handlers) {
                fallbackSettings(h);
            }
            fallbackSettings(pushOnlyHandler);
            fallbackSettings(pushNormalHandler);
            return true;
        }
        return false;
    }

    /**
     * Common fallback settings for a single handler.
     *
     * @param h the handler.
     */
    private static void fallbackSettings(Handler h) {
        if (h != null) {
            h.setErrorManager(new FileErrorManager());
            h.setLevel(Level.ALL);
        }
    }

    /**
     * Gets the system temp directory.
     *
     * @return the system temp directory.
     */
    private static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Gets the configuration file or class name.
     *
     * @return the file name or class name.
     */
    private static String getConfigLocation() {
        String file = System.getProperty("java.util.logging.config.file");
        if (file == null) {
            return System.getProperty("java.util.logging.config.class");
        }
        return file;
    }

    /**
     * No objects are allowed.
     * @throws IllegalAccessException always.
     */
    private MailHandlerDemo() throws IllegalAccessException {
        throw new IllegalAccessException();
    }
}
