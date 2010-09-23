/*
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2010 Jason Mehrens. All Rights Reserved.
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

import com.sun.mail.util.logging.MailHandler;
import java.util.logging.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.io.*;

/**
 * Demo for the different configurations for the MailHandler.
 * If the logging properties file or class is not specified then this
 * demo will apply some default settings to store emails in the users temp dir.
 * @author Jason Mehrens
 */
public class MailHandlerDemo {

    private static final String CLASS_NAME = MailHandlerDemo.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        init(); //may create log messages.
        try {
            LOGGER.log(Level.FINEST, "This is the finest part of the demo.",
                    new MessagingException("Fake"));
            LOGGER.log(Level.FINER, "This is the finer part of the demo.",
                    new NullPointerException("Fake"));
            LOGGER.log(Level.FINE, "This is the fine part of the demo.");
            LOGGER.log(Level.CONFIG, "Logging config file is {0}.", getConfigLocation());
            LOGGER.log(Level.INFO, "Your temp directory is {0}, please wait...", getTempDir());

            try { //waste some time for the custom formatter.
                Thread.sleep(3L * 1000L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            LOGGER.log(Level.WARNING, "This is a warning.", new FileNotFoundException("Fake"));
            LOGGER.log(Level.SEVERE, "The end of the demo.", new IOException("Fake"));
        } finally {
            closeHandlers();
        }
    }

    /**
     * Used debug problems with the logging.properties.
     * @param prefix a string to prefix the output.
     * @param err any PrintStream or null for System.out.
     */
    private static void checkConfig(String prefix, PrintStream err) {
        if (prefix == null || prefix.trim().length() == 0) {
            prefix = "DEBUG";
        }

        if (err == null) {
            err = System.out;
        }

        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                err.println(prefix + ": SecurityManager.class=" + sm.getClass().getName());
                err.println(prefix + ": SecurityManager.toString=" + sm);
            } else {
                err.println(prefix + ": SecurityManager.class=" + null);
                err.println(prefix + ": SecurityManager.toString=" + null);
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
                //force any errors, only safe is key is present.
                manager.readConfiguration();
            } else {
                err.println(prefix + ": " + key + " is not set as a system property.");
            }
            err.println(prefix + ": LogManager.class=" + manager.getClass().getName());
            err.println(prefix + ": LogManager.toString=" + manager);

            final String p = MailHandler.class.getName();
            key = p.concat(".mail.to");
            String to = manager.getProperty(key);
            err.println(prefix + ": TO=" + to);
            err.println(prefix + ": TO="
                    + Arrays.toString(InternetAddress.parse(to, false)));
            err.println(prefix + ": TO="
                    + Arrays.toString(InternetAddress.parse(to, true)));

            key = p.concat(".mail.from");
            String from = manager.getProperty(key);
            if (from == null || from.length() == 0) {
                Session session = Session.getInstance(new Properties());
                InternetAddress local = InternetAddress.getLocalAddress(session);
                err.println(prefix + ": FROM=" + local);
            } else {
                err.println(prefix + ": FROM="
                        + Arrays.toString(InternetAddress.parse(from, false)));
                err.println(prefix + ": FROM="
                        + Arrays.toString(InternetAddress.parse(from, true)));
            }
        } catch (Throwable error) {
            err.print(prefix + ": ");
            error.printStackTrace(err);
        }
    }

    /**
     * Example for body only messages.
     * On close the remaining messages are sent.
     */
    private static void initBodyOnly() {
        MailHandler h = new MailHandler();
        h.setSubject("Body only demo");
        LOGGER.addHandler(h);
    }

    /**
     * Example showing that when the mail handler reaches capacity it
     * will format and send the current records.  Capacity is used to roughly 
     * limit the size of an outgoing message.
     * On close any remaining messages are sent.
     */
    private static void initLowCapacity() {
        MailHandler h = new MailHandler(5);
        h.setSubject("Low capacity demo");
        LOGGER.addHandler(h);
    }

    /**
     * Example for body only messages.
     * On close any remaining messages are sent.
     */
    private static void initSimpleAttachment() {
        MailHandler h = new MailHandler();
        h.setSubject("Body and attachment demo");
        h.setAttachmentFormatters(new Formatter[]{new XMLFormatter()});
        h.setAttachmentNames(new String[]{"data.xml"});
        LOGGER.addHandler(h);
    }

    /**
     * Example setup for priority messages by level.
     * If the push level is triggered the message is high priority.
     * Otherwise, on close any remaining messages are sent.
     */
    private static void initWithPushLevel() {
        MailHandler h = new MailHandler();
        h.setSubject("Push level demo");
        h.setPushLevel(Level.WARNING);
        LOGGER.addHandler(h);
    }

    /**
     * Example for priority messages by custom trigger.
     * If the push filter is triggered the message is high priority.
     * Otherwise, on close any remaining messages are sent.
     */
    private static void initWithPushFilter() {
        MailHandler h = new MailHandler();
        h.setSubject("Push on MessagingException demo");
        h.setPushLevel(Level.ALL);
        h.setPushFilter(new MessageErrorsFilter(true));
        LOGGER.addHandler(h);
    }

    /**
     * Example for circular buffer behavior.  The level, push level, and
     * capacity are set the same so that the memory handler push results
     * in a mail handler push.  All messages are high priority.
     * On close any remaining records are discarded because they never reach
     * the mail handler.
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
    private static Handler pushOnlyHandler;

    /**
     * Example for circular buffer behavior as normal priority.  The push level,
     * and capacity are set the same so that the memory handler push results
     * in a mail handler push.  All messages are normal priority.
     * On close any remaining records are discarded because they never reach
     * the mail handler.
     */
    private static void initPushNormal() {
        final int capacity = 3;
        final MailHandler h = new MailHandler(capacity);
        h.setSubject("Push normal demo");
        MemoryHandler m = new MemoryHandler(h, capacity, Level.WARNING) {

            public void push() {
                super.push();  //push to target.
                super.flush(); //make the target send the email.
            }
        };
        LOGGER.addHandler(m);
        pushNormalHandler = h;
    }
    private static Handler pushNormalHandler;

    /**
     *  Example for various kinds of custom sorting, formatting, and filtering
     *  for multiple attachment messages.
     *  On close any remaining messages are sent.
     */
    private static void initCustomAttachments() {
        MailHandler h = new MailHandler();

        //Sort records by level keeping the severe messages at the top.
        h.setComparator(new LevelAndSeqComparator(true));

        //Use subject to provide a hint as to what is in the email.
        h.setSubject(new SummaryNameFormatter("Log containing {0} records with {1} errors"));

        //Make the body give a simple summary of what happened.
        h.setFormatter(new SummaryFormatter());

        //Create 3 attachments.
        h.setAttachmentFormatters(new Formatter[]{new XMLFormatter(), new XMLFormatter(), new SimpleFormatter()});

        //filter each attachment differently.
        h.setAttachmentFilters(new Filter[]{null, new MessageErrorsFilter(false),
                    new MessageErrorsFilter(true)});


        //create simple names.
        h.setAttachmentNames(new String[]{"all.xml", "errors.xml", "errors.txt"});

        //extract simple name, replace the rest with formatters.
        h.setAttachmentNames(new Formatter[]{h.getAttachmentNames()[0],
                    new SummaryNameFormatter("{0} records and {1} errors"),
                    new SummaryNameFormatter("{0,choice,0#no records|1#1 record|"
                    + "1<{0,number,integer} records} and "
                    + "{1,choice,0#no errors|1#1 error|1<"
                    + "{1,number,integer} errors}")});

        LOGGER.addHandler(h);
    }

    /**
     * Sets up the demos that will run.
     */
    private static void init() {
        Session session = Session.getInstance(System.getProperties());
        if (session.getDebug()) {
            checkConfig(CLASS_NAME, session.getDebugOut());
        }

        initBodyOnly();
        initLowCapacity();
        initSimpleAttachment();
        initWithPushLevel();
        initWithPushFilter();
        initCustomAttachments();
        initPushOnly();
        initPushNormal();
        applyFallbackSettings();
    }

    private static void closeHandlers() {
        Handler[] handlers = LOGGER.getHandlers();
        for (int i = 0; i < handlers.length; i++) {
            Handler h = handlers[i];
            h.close();
            LOGGER.removeHandler(h);
        }
    }

    private static void applyFallbackSettings() {
        if (getConfigLocation() == null) {
            LOGGER.setLevel(Level.ALL);
            LOGGER.info("Check your user temp dir for output.");
            Handler[] handlers = LOGGER.getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                Handler h = handlers[i];
                fallbackSettings(h);
            }
            fallbackSettings(pushOnlyHandler);
            fallbackSettings(pushNormalHandler);
        }
    }

    private static void fallbackSettings(Handler h) {
        if (h != null) {
            h.setErrorManager(new FileErrorManager());
            h.setLevel(Level.ALL);
        }
    }

    private static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    private static String getConfigLocation() {
        String file = System.getProperty("java.util.logging.config.file");
        if (file == null) {
            return System.getProperty("java.util.logging.config.class");
        }
        return file;
    }

    private static final class MessageErrorsFilter implements Filter {

        private final boolean complement;

        MessageErrorsFilter(final boolean complement) {
            this.complement = complement;
        }

        public boolean isLoggable(LogRecord r) {
            return r.getThrown() instanceof MessagingException == complement;
        }
    }

    /**
     * Orders log records by level then sequence number.
     */
    private static final class LevelAndSeqComparator
            implements Comparator, java.io.Serializable {

        private static final long serialVersionUID = 6269562326337300267L;
        private final boolean reverse;

        LevelAndSeqComparator() {
            this(false);
        }

        LevelAndSeqComparator(final boolean reverse) {
            this.reverse = reverse;
        }

        public int compare(Object o1, Object o2) {
            LogRecord r1 = (LogRecord) o1;
            LogRecord r2 = (LogRecord) o2;
            final int first = r1.getLevel().intValue();
            final int second = r2.getLevel().intValue();
            if (first < second) {
                return reverse ? 1 : -1;
            } else if (first > second) {
                return reverse ? -1 : 1;
            } else {
                return compareSeq(r1, r2);
            }
        }

        private int compareSeq(LogRecord r1, LogRecord r2) {
            final long first = r1.getSequenceNumber();
            final long second = r2.getSequenceNumber();
            if (first < second) {
                return reverse ? 1 : -1;
            } else if (first > second) {
                return reverse ? -1 : 1;
            } else {
                return 0;
            }
        }
    }
}
