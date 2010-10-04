/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2010 Jason Mehrens. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.mail.util.logging;

import java.lang.reflect.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test case for the MailHandler spec.
 * @author Jason Mehrens
 */
public class MailHandlerTest {

    private static final String LOG_CFG_KEY = "java.util.logging.config.file";
    /**
     * Used to prevent G.C. of loggers.
     */
    private Object hardRef;

    @BeforeClass
    public static void setUpClass() throws Exception {
        checkJVMOptions();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        checkJVMOptions();
    }

    private static void checkJVMOptions() {
        assertTrue(MailHandlerTest.class.desiredAssertionStatus());
        assertNull(System.getProperty("java.util.logging.manager"));
        assertNull(System.getProperty("java.util.logging.config.class"));
        assertNull(System.getProperty(LOG_CFG_KEY));
        assertEquals(LogManager.class, LogManager.getLogManager().getClass());
        assertTrue(LOW_CAPACITY < NUM_RUNS);
        //Try to hold MAX_CAPACITY array with log records.
        assertTrue((60L * 1024L * 1024L) <= Runtime.getRuntime().maxMemory());
    }

    @Test
    public void testIsLoggable() {
        Level[] lvls = getAllLevels();
        if (lvls.length > 0) {
            LogRecord record = new LogRecord(Level.INFO, "");
            for (int i = 0; i < lvls.length; i++) {
                testLoggable(lvls[i], null);
                testLoggable(lvls[i], record);
            }
        } else {
            fail("No predefined levels.");
        }
    }

    private void testLoggable(Level lvl, LogRecord record) {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        instance.setLevel(lvl);
        MemoryHandler mem = null;
        boolean result = false;
        boolean expect = true;
        try {
            result = instance.isLoggable(record);
            mem = new MemoryHandler(new ConsoleHandler(), 100, Level.OFF);
            mem.setErrorManager(em);
            mem.setLevel(lvl);
            expect = mem.isLoggable(record);
        } catch (RuntimeException mailEx) {
            try {
                if (mem != null) {
                    fail("MemoryHandler threw and exception: " + mailEx);
                } else {
                    mem = new MemoryHandler(new ConsoleHandler(), 100, Level.OFF);
                    mem.setErrorManager(em);
                    mem.setLevel(lvl);
                    expect = mem.isLoggable(record);
                    fail("MailHandler threw and exception: " + mailEx);
                }
            } catch (RuntimeException memEx) {
                assertEquals(memEx.getClass(), mailEx.getClass());
                result = false;
                expect = false;
            }
        }
        assertEquals(expect, result);

        instance.setLevel(Level.INFO);
        instance.setFilter(BooleanFilter.FALSE);
        instance.setAttachmentFormatters(
                new Formatter[]{new SimpleFormatter(), new XMLFormatter()});
        //null filter makes all records INFO and above loggable.
        instance.setAttachmentFilters(new Filter[]{BooleanFilter.FALSE, null});
        assertEquals(false, instance.isLoggable(new LogRecord(Level.FINEST, "")));
        assertEquals(true, instance.isLoggable(new LogRecord(Level.INFO, "")));
        assertEquals(true, instance.isLoggable(new LogRecord(Level.WARNING, "")));
        assertEquals(true, instance.isLoggable(new LogRecord(Level.SEVERE, "")));

        assertEquals(em.exceptions.isEmpty(), true);
    }

    @Test
    public void testPublish() {
        MailHandler instance = createHandlerWithRecords();
        InternalErrorManager em =
                (InternalErrorManager) instance.getErrorManager();
        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();

        assertEquals(1, em.exceptions.size());
        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);

        //Test for valid message.
        instance = createHandlerWithRecords();
        instance.setErrorManager(new FlushErrorManager(instance));

        Level[] lvls = this.getAllLevels();
        String SOURCE_CLASS = MailHandlerTest.class.getName();
        String SOURCE_METHOD = "testPublish";
        for (int i = 0; i < lvls.length; i++) {
            LogRecord r = new LogRecord(lvls[i], "");
            r.setSourceClassName(SOURCE_CLASS);
            r.setSourceMethodName(SOURCE_METHOD);
            instance.publish(r);
        }

        instance.close();
    }

    @Test
    public void testPublishDuringClose() {
        Level[] lvls = getAllLevels();
        for (int levelIndex = 0; levelIndex < lvls.length; levelIndex++) {
            MailHandler instance = new MailHandler(lvls.length + 2);
            InternalErrorManager em = new InternalErrorManager();
            instance.setErrorManager(em);
            Properties props = new Properties();
            props.put("mail.smtp.host", "bad-host-name");
            props.put("mail.host", "bad-host-name");
            instance.setMailProperties(props);

            Authenticator auth = new EmptyAuthenticator();
            Filter filter = new BooleanFilter(true);
            Formatter formatter = new SimpleFormatter();
            instance.setSubject("publishDuringClose");
            Formatter subject = instance.getSubject();

            instance.setAuthenticator(auth);
            instance.setLevel(Level.ALL);
            instance.setFormatter(formatter);
            instance.setFilter(filter);
            instance.setPushLevel(Level.OFF);
            instance.setPushFilter(filter);
            instance.setAttachmentFormatters(new Formatter[]{formatter});
            instance.setAttachmentFilters(new Filter[]{filter});
            instance.setAttachmentNames(new Formatter[]{subject});

            assertTrue(em.exceptions.isEmpty());

            final String msg = instance.toString();
            for (int j = 0; j < lvls.length; j++) {
                Level oldLevel = instance.getLevel();
                Level lvl = lvls[(levelIndex + j) % lvls.length];
                CloseLogRecord r = new CloseLogRecord(lvl, msg, instance);
                assertFalse(r.isClosed());
                instance.publish(r);
                if (!oldLevel.equals(Level.OFF)) {
                    assertEquals(Level.OFF, instance.getLevel());
                    assertTrue(r.isClosed());
                }
            }

            //Close is not allowed to change any settings.
            assertEquals(Level.OFF, instance.getLevel());
            assertEquals(props, instance.getMailProperties());
            assertEquals(auth, instance.getAuthenticator());
            assertEquals(subject, instance.getSubject());
            assertEquals(filter, instance.getFilter());
            assertEquals(formatter, instance.getFormatter());
            assertEquals(Level.OFF, instance.getPushLevel());
            assertEquals(filter, instance.getPushFilter());
            assertEquals(formatter, instance.getAttachmentFormatters()[0]);
            assertEquals(filter, instance.getAttachmentFilters()[0]);
            assertEquals(subject, instance.getAttachmentNames()[0]);

            //ensure one transport error.
            assertEquals(1, em.exceptions.size());
            assertTrue(em.exceptions.get(0) instanceof MessagingException);
        }
    }

    private MailHandler createHandlerWithRecords() {
        Level[] lvls = getAllLevels();

        MailHandler instance = new MailHandler(lvls.length + 2);
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        instance.setMailProperties(props);
        instance.setLevel(Level.ALL);
        instance.setFilter(null);
        instance.setPushLevel(Level.OFF);
        instance.setPushFilter(null);

        final String msg = instance.toString();
        for (int i = 0; i < lvls.length; i++) {
            LogRecord r = new LogRecord(lvls[i], msg);
            r.setSourceClassName(MailHandlerTest.class.getName());
            r.setLoggerName(r.getSourceClassName());
            r.setSourceMethodName("createHandlerWithRecords");
            instance.publish(r);
        }
        return instance;
    }

    @Test
    public void testErrorSubjectFormatter() {
        MailHandler instance = new MailHandler(2);
        instance.setLevel(Level.ALL);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        instance.setMailProperties(props);

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setSubject(new ErrorFormatter());

        LogRecord record = new LogRecord(Level.INFO, "");
        instance.publish(record);
        try {
            instance.push();
            fail("Error didn't escape push.");
        } catch (Error expected) {
            if (expected.getClass() != Error.class) {
                throw expected;
            } else {
                assertEquals(Level.ALL, instance.getLevel());
            }
        }

        instance.publish(record);
        try {
            instance.flush();
            fail("Error didn't escape flush.");
        } catch (Error expected) {
            if (expected.getClass() != Error.class) {
                throw expected;
            } else {
                assertEquals(Level.ALL, instance.getLevel());
            }
        }

        instance.publish(record);
        record = new LogRecord(Level.INFO, "");
        try {
            instance.publish(record);
            fail("Error didn't escape publish at full capacity.");
        } catch (Error expected) {
            if (expected.getClass() != Error.class) {
                throw expected;
            } else {
                assertEquals(Level.ALL, instance.getLevel());
            }
        }

        instance.publish(record);
        try {
            instance.close();
            fail("Error didn't escape close.");
        } catch (Error expected) {
            if (expected.getClass() != Error.class) {
                throw expected;
            } else {
                assertEquals(Level.OFF, instance.getLevel());
            }
        }

        instance.close();
        final int size = em.exceptions.size();
        if (size > 0) {
            fail(em.exceptions.toString());
        }
    }

    @Test
    public void testThrowFormatters() {
        MailHandler instance = new MailHandler();
        instance.setLevel(Level.ALL);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        instance.setMailProperties(props);

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setComparator(new ThrowComparator());
        instance.setFormatter(new ThrowFormatter());
        instance.setSubject(new ThrowFormatter());
        instance.setAttachmentFormatters(new Formatter[]{new ThrowFormatter()});
        instance.setAttachmentNames(new Formatter[]{new ThrowFormatter()});

        LogRecord record = new LogRecord(Level.INFO, "");
        instance.publish(record);
        instance.close();

        final int size = em.exceptions.size();
        if (size > 0) {
            for (int i = 0; i < em.exceptions.size() - 1; i++) {
                assertEquals(true, em.exceptions.get(i) instanceof RuntimeException);
            }
            assertEquals(true,
                    em.exceptions.get(size - 1) instanceof MessagingException);
            return;
        }
        fail("No runtime exceptions reported");
    }

    @Test
    public void testErrorFormatters() {
        MailHandler instance = new MailHandler();
        instance.setLevel(Level.ALL);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        instance.setMailProperties(props);

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setComparator(new ErrorComparator());
        instance.setFormatter(new ErrorFormatter());
        instance.setSubject(new ErrorFormatter());
        instance.setAttachmentFormatters(new Formatter[]{new ErrorFormatter()});
        instance.setAttachmentNames(new Formatter[]{new ErrorFormatter()});

        LogRecord record = new LogRecord(Level.INFO, "");
        instance.publish(record);
        try {
            instance.close();
            fail("Error was swallowed.");
        } catch (Error expect) {
            if (expect.getClass() != Error.class) {
                throw expect;
            }
        }
    }

    @Test
    public void testErrorFilters() {
        LogRecord record = new LogRecord(Level.INFO, "");
        MemoryHandler mh = new MemoryHandler(new ConsoleHandler(), 100, Level.OFF);
        mh.setFilter(new ErrorFilter());
        MailHandler instance = null;
        try {
            boolean expect = mh.isLoggable(record);
            instance = new MailHandler();
            instance.setLevel(Level.ALL);
            instance.setFilter(new ErrorFilter());
            boolean result = instance.isLoggable(record);
            assertEquals(expect, result);
        } catch (Error expectEx) {
            if (instance == null) {
                try {
                    instance = new MailHandler();
                    instance.setLevel(Level.ALL);
                    instance.setFilter(new ErrorFilter());
                    instance.isLoggable(record);
                    fail("Doesn't match the memory handler.");
                } catch (Error resultEx) {
                    assertEquals(expectEx.getClass(), resultEx.getClass());
                }
            } else {
                fail("Doesn't match the memory handler.");
            }
        }
        instance.setFilter(null);


        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        instance.setMailProperties(props);

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        instance.setAttachmentFormatters(new Formatter[]{new SimpleFormatter()});
        instance.setAttachmentFilters(new Filter[]{new ErrorFilter()});
        instance.setAttachmentNames(new String[]{"test.txt"});

        instance.publish(record);
        try {
            instance.close();
            fail("Error was swallowed.");
        } catch (Error expect) {
            if (expect.getClass() != Error.class) {
                throw expect;
            }
        }

        assertEquals(true, em.exceptions.isEmpty());
    }

    @Test
    public void testThrowFilters() {
        LogRecord record = new LogRecord(Level.INFO, "");
        MemoryHandler mh = new MemoryHandler(new ConsoleHandler(), 100, Level.OFF);
        mh.setFilter(new ThrowFilter());
        MailHandler instance = null;
        try {
            boolean expect = mh.isLoggable(record);
            instance = new MailHandler();
            instance.setLevel(Level.ALL);
            instance.setFilter(new ThrowFilter());
            boolean result = instance.isLoggable(record);
            assertEquals(expect, result);
        } catch (RuntimeException expectEx) {
            if (instance == null) {
                try {
                    instance = new MailHandler();
                    instance.setLevel(Level.ALL);
                    instance.setFilter(new ThrowFilter());
                    instance.isLoggable(record);
                    fail("Doesn't match the memory handler.");
                } catch (RuntimeException resultEx) {
                    assertEquals(expectEx.getClass(), resultEx.getClass());
                }
            } else {
                fail("Doesn't match the memory handler.");
            }
        }
        instance.setFilter(null);


        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        instance.setMailProperties(props);

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        instance.setAttachmentFormatters(new Formatter[]{new SimpleFormatter()});
        instance.setAttachmentFilters(new Filter[]{new ThrowFilter()});
        instance.setAttachmentNames(new String[]{"test.txt"});

        instance.publish(record);
        instance.close();

        assertEquals(true, !em.exceptions.isEmpty());
    }

    @Test
    public void testEmpty() {
        MailHandler instance = createHandlerWithRecords();
        instance.setFormatter(new SimpleFormatter());
        instance.setAttachmentFormatters(new Formatter[]{
                    new EmptyFormatter(), new SimpleFormatter(), new SimpleFormatter()});
        testEmpty(instance);

        instance = createHandlerWithRecords();
        instance.setFormatter(new SimpleFormatter());
        instance.setAttachmentFormatters(new Formatter[]{
                    new SimpleFormatter(), new EmptyFormatter(), new SimpleFormatter()});
        testEmpty(instance);

        instance = createHandlerWithRecords();
        instance.setFormatter(new SimpleFormatter());
        instance.setAttachmentFormatters(new Formatter[]{
                    new SimpleFormatter(), new SimpleFormatter(), new EmptyFormatter()});
        testEmpty(instance);

        instance = createHandlerWithRecords();
        instance.setFormatter(new EmptyFormatter());
        instance.setAttachmentFormatters(new Formatter[]{
                    new SimpleFormatter(), new SimpleFormatter(), new SimpleFormatter()});
        testEmpty(instance);


        instance = createHandlerWithRecords();
        instance.setFormatter(new EmptyFormatter());
        instance.setAttachmentFormatters(new Formatter[]{
                    new SimpleFormatter(), new EmptyFormatter(), new SimpleFormatter()});
        testEmpty(instance);

        instance = createHandlerWithRecords();
        instance.setFormatter(new SimpleFormatter());
        instance.setAttachmentFormatters(new Formatter[]{new EmptyFormatter()});
        testEmpty(instance);

        instance = createHandlerWithRecords();
        instance.setFormatter(new EmptyFormatter());
        instance.setAttachmentFormatters(new Formatter[]{new SimpleFormatter()});
        testEmpty(instance);

        instance = createHandlerWithRecords();
        instance.setFormatter(new EmptyFormatter());
        instance.setAttachmentFormatters(new Formatter[]{new EmptyFormatter()});
        testEmpty(instance);

        instance = createHandlerWithRecords();
        instance.setFormatter(new EmptyFormatter());
        testEmpty(instance);
    }

    private void testEmpty(MailHandler instance) {
        Properties props = instance.getMailProperties();
        props.setProperty("mail.from", "localhost@localdomain");
        props.setProperty("mail.to", "localhost@localdomain");
        instance.setMailProperties(props);

        MessageErrorManager empty = new MessageErrorManager(instance) {

            @Override
            public void error(MimeMessage msg, Throwable t, int code) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    msg.saveChanges();
                    msg.writeTo(out);
                } catch (Throwable ex) {
                    fail(ex.toString());
                }
            }
        };
        instance.setErrorManager(empty);
        instance.close();
    }

    @Test
    public void testEncoding() throws Exception {
        final String enc = "iso-8859-1";
        LogManager manager = LogManager.getLogManager();
        final MailHandler instance = new MailHandler();
        MessageErrorManager em = new MessageErrorManager(instance) {

            protected void error(MimeMessage msg, Throwable t, int code) {
                try {
                    MimeMultipart multi = (MimeMultipart) msg.getContent();
                    BodyPart body = multi.getBodyPart(0);
                    assertEquals(Part.INLINE, body.getDisposition());
                    ContentType ct = new ContentType(body.getContentType());
                    assertEquals(enc, ct.getParameter("charset"));

                    BodyPart attach = multi.getBodyPart(1);
                    ct = new ContentType(attach.getContentType());
                    assertEquals(enc, ct.getParameter("charset"));
                } catch (Throwable E) {
                    E.printStackTrace();
                    fail(E.toString());
                }
            }
        };

        instance.setErrorManager(em);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        props.put("mail.to", "localhost@localdomain");
        instance.setMailProperties(props);
        instance.setAttachmentFormatters(new Formatter[]{new XMLFormatter()});
        instance.setAttachmentNames(new String[]{"all.xml"});
        String p = instance.getClass().getName();

        assertEquals(manager.getProperty(p.concat(".encoding")), instance.getEncoding());
        try {
            instance.setEncoding("unsupported encoding exception");
            fail("Missing encoding check.");
        } catch (UnsupportedEncodingException expect) {
        }

        assertTrue(em.exceptions.isEmpty());

        instance.setEncoding(enc);
        instance.setSubject("ORA-17043=Ung\u00FCltige maximale Stream-Gr\u00F6\u00DFe");
        LogRecord record = new LogRecord(Level.SEVERE, "Zeit\u00FCberschreitung bei Anweisung");
        instance.publish(record);
        instance.close();
    }

    @Test
    public void testPushInsidePush() {
        Level[] lvls = getAllLevels();

        MailHandler instance = new MailHandler(lvls.length + 2);
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        instance.setMailProperties(props);
        instance.setLevel(Level.ALL);
        instance.setFilter(null);
        instance.setPushLevel(Level.OFF);
        instance.setPushFilter(null);

        instance.setFormatter(new SimpleFormatter() {

            @Override
            public String getHead(Handler h) {
                assert h instanceof MailHandler : h;
                try {
                    h.flush();
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            @Override
            public String getTail(Handler h) {
                assert h instanceof MailHandler : h;
                final Filter filter = h.getFilter();
                try {
                    h.setFilter(filter);
                } catch (Throwable T) {
                    fail(T.toString());
                }

                final Level lvl = h.getLevel();
                try {
                    h.setLevel(lvl);
                } catch (Throwable T) {
                    fail(T.toString());
                }

                final String enc = h.getEncoding();
                try {
                    h.setEncoding(enc);
                } catch (Throwable T) {
                    fail(T.toString());
                }


                try {
                    h.setFormatter(new SimpleFormatter());
                } catch (Throwable T) {
                    fail(T.toString());
                }

                try {
                    h.close();
                    assertEquals(h.getLevel(), Level.OFF);
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getTail(h);
            }
        });


        Formatter push = new SimpleFormatter() {

            @Override
            public String getHead(Handler h) {
                assert h instanceof MailHandler : h;
                try {
                    ((MailHandler) h).push();
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            @Override
            public String getTail(Handler h) {
                assert h instanceof MailHandler : h;
                try {
                    ((MailHandler) h).push();
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getTail(h);
            }
        };

        Formatter atFor = new SimpleFormatter() {

            @Override
            public String getHead(Handler h) {
                assert h instanceof MailHandler : h;
                MailHandler mh = (MailHandler) h;
                Formatter[] f = mh.getAttachmentFormatters();
                try {
                    mh.setAttachmentFormatters(f);
                    fail("Mutable formatter.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            @Override
            public String getTail(Handler h) {
                getHead(h);
                return super.getTail(h);
            }
        };

        Formatter atName = new SimpleFormatter() {

            @Override
            public String getHead(Handler h) {
                assert h instanceof MailHandler : h;
                MailHandler mh = (MailHandler) h;
                Formatter[] f = mh.getAttachmentNames();
                try {
                    mh.setAttachmentNames(f);
                    fail("Mutable formatter.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            @Override
            public String getTail(Handler h) {
                getHead(h);
                return super.getTail(h);
            }
        };

        Formatter atFilter = new SimpleFormatter() {

            @Override
            public String getHead(Handler h) {
                assert h instanceof MailHandler;
                MailHandler mh = (MailHandler) h;
                Filter[] f = mh.getAttachmentFilters();
                try {
                    mh.setAttachmentFilters(f);
                    fail("Mutable filters.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            @Override
            public String getTail(Handler h) {
                getHead(h);
                return super.getTail(h);
            }
        };

        Formatter nameComp = new Formatter() {

            @Override
            public String getHead(Handler h) {
                assert h instanceof MailHandler : h;
                MailHandler mh = (MailHandler) h;
                Comparator c = mh.getComparator();
                try {
                    mh.setComparator(c);
                    fail("Mutable comparator.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            @Override
            public String format(LogRecord r) {
                return "";
            }

            @Override
            public String getTail(Handler h) {
                getHead(h);
                return "name.txt";
            }
        };

        Formatter nameMail = new Formatter() {

            @Override
            public String getHead(Handler h) {
                assert h instanceof MailHandler : h;
                MailHandler mh = (MailHandler) h;
                Properties props = mh.getMailProperties();
                try {
                    mh.setMailProperties(props);
                    fail("Mutable props.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            @Override
            public String format(LogRecord r) {
                return "";
            }

            @Override
            public String getTail(Handler h) {
                getHead(h);
                return "name.txt";
            }
        };

        Formatter nameSub = new Formatter() {

            @Override
            public String getHead(Handler h) {
                assert h instanceof MailHandler : h;
                MailHandler mh = (MailHandler) h;
                Formatter f = mh.getSubject();
                try {
                    mh.setSubject(f);
                    fail("Mutable subject.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            @Override
            public String format(LogRecord r) {
                return "";
            }

            @Override
            public String getTail(Handler h) {
                getHead(h);
                return "name.txt";
            }
        };

        Formatter nameAuth = new Formatter() {

            @Override
            public String getHead(Handler h) {
                assert h instanceof MailHandler : h;
                MailHandler mh = (MailHandler) h;
                Authenticator a = mh.getAuthenticator();
                try {
                    mh.setAuthenticator(a);
                    fail("Mutable Authenticator.");
                } catch (IllegalStateException pass) {
                } catch (Throwable T) {
                    fail(T.toString());
                }
                return super.getHead(h);
            }

            @Override
            public String format(LogRecord r) {
                return "";
            }

            @Override
            public String getTail(Handler h) {
                getHead(h);
                return "name.txt";
            }
        };

        instance.setAttachmentFormatters(
                new Formatter[]{push, atFor, atName, atFilter});
        instance.setAttachmentNames(
                new Formatter[]{nameComp, nameMail, nameSub, nameAuth});

        String SOURCE_CLASS = MailHandlerTest.class.getName();
        String SOURCE_METHOD = "testPushInsidePush";
        for (int i = 0; i < lvls.length; i++) {
            LogRecord r = new LogRecord(lvls[i], "");
            r.setSourceClassName(SOURCE_CLASS);
            r.setSourceMethodName(SOURCE_METHOD);
            instance.publish(r);
        }
        instance.flush();

        for (int i = 0; i < em.exceptions.size(); i++) {
            assertEquals(false, em.exceptions.get(i) instanceof RuntimeException);
        }
    }

    @Test
    public void testPush() {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.push();
        assertEquals(true, em.exceptions.isEmpty());
        instance.close();

        instance = createHandlerWithRecords();
        em = (InternalErrorManager) instance.getErrorManager();
        instance.push();

        assertEquals(1, em.exceptions.size());
        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);
        instance.close();

        //Test for valid message.
        instance = createHandlerWithRecords();
        instance.setErrorManager(new PushErrorManager(instance));
        instance.push();
        instance.close();


        instance = new MailHandler(1);
        instance.setLevel(Level.ALL);
        instance.setErrorManager(new PushErrorManager(instance));
        instance.setPushFilter(null);
        instance.setPushLevel(Level.INFO);
        LogRecord record = new LogRecord(Level.SEVERE, "");
        instance.publish(record); //should push.
        instance.close(); //cause a flush if publish didn't push.
    }

    @Test
    public void testFlush() {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.flush();

        assertEquals(true, em.exceptions.isEmpty());
        instance.close();

        instance = createHandlerWithRecords();
        em = (InternalErrorManager) instance.getErrorManager();
        instance.flush();

        assertEquals(1, em.exceptions.size());
        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);
        instance.close();

        //Test for valid message.
        instance = createHandlerWithRecords();
        instance.setErrorManager(new FlushErrorManager(instance));
        instance.flush();
        instance.close();

        instance = new MailHandler(1);
        instance.setLevel(Level.ALL);
        instance.setErrorManager(new FlushErrorManager(instance));
        instance.setPushFilter(null);
        instance.setPushLevel(Level.SEVERE);
        LogRecord record = new LogRecord(Level.INFO, "");
        instance.publish(record); //should flush.
        instance.push(); //make FlushErrorManager fail if handler didn't flush.
        instance.close();
    }

    @Test
    public void testClose() {
        LogRecord record = new LogRecord(Level.INFO, "");
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        int capacity = instance.getCapacity();

        assertNotNull(instance.getLevel());

        instance.setLevel(Level.ALL);
        assertEquals(true, instance.isLoggable(record));

        instance.close();

        assertEquals(false, instance.isLoggable(record));
        assertEquals(Level.OFF, instance.getLevel());

        instance.setLevel(Level.ALL);
        assertEquals(Level.OFF, instance.getLevel());

        assertEquals(capacity, instance.getCapacity());
        assertEquals(true, em.exceptions.isEmpty());

        instance = createHandlerWithRecords();
        em = (InternalErrorManager) instance.getErrorManager();
        instance.close();

        assertEquals(1, em.exceptions.size());
        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);

        //Test for valid message.
        instance = createHandlerWithRecords();
        instance.setErrorManager(new FlushErrorManager(instance));
        instance.close();
    }

    @Test
    public void testLevel() {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getLevel());

        try {
            instance.setLevel(null);
            fail("Null level was allowed");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        Level[] lvls = getAllLevels();
        for (int i = 0; i < lvls.length; i++) {
            instance.setLevel(lvls[i]);
            assertEquals(instance.getLevel(), lvls[i]);
        }

        instance.close();
        for (int i = 0; i < lvls.length; i++) {
            instance.setLevel(lvls[i]);
            assertEquals(Level.OFF, instance.getLevel());
        }
        assertEquals(true, em.exceptions.isEmpty());
    }

    @Test
    public void testLogManagerReset() throws IOException {
        LogManager manager = LogManager.getLogManager();
        assertEquals(LogManager.class, manager.getClass());
        MailHandler instance = startLogManagerReset("remote");
        InternalErrorManager em =
                (InternalErrorManager) instance.getErrorManager();

        manager.reset();

        for (int i = 0; i < em.exceptions.size(); i++) {
            Throwable t = em.exceptions.get(i);
            if (t instanceof MessagingException) {
                if (!isConnectOrTimeout(t)) {
                    t.printStackTrace();
                    fail(t.toString());
                }
            } else {
                t.printStackTrace();
                fail(t.toString());
            }
        }

        instance = startLogManagerReset("local");
        em = (InternalErrorManager) instance.getErrorManager();

        for (int i = 0; i < em.exceptions.size(); i++) {
            Throwable t = em.exceptions.get(i);
            if (t instanceof MessagingException) {
                if (!isConnectOrTimeout(t)) {
                    t.printStackTrace();
                    fail(t.toString());
                }
            } else {
                t.printStackTrace();
                fail(t.toString());
            }
        }

        manager.reset();

        for (int i = 0; i < em.exceptions.size(); i++) {
            Throwable t = em.exceptions.get(i);
            if (t instanceof MessagingException) {
                if (!isConnectOrTimeout(t)) {
                    t.printStackTrace();
                    fail(t.toString());
                }
            } else {
                t.printStackTrace();
                fail(t.toString());
            }
        }

        String[] noVerify = new String[]{null, "", "null"};
        for (int v = 0; v < noVerify.length; v++) {
            instance = startLogManagerReset(noVerify[v]);
            em = (InternalErrorManager) instance.getErrorManager();

            for (int i = 0; i < em.exceptions.size(); i++) {
                Throwable t = em.exceptions.get(i);
                System.err.println("Verify index=" + v);
                t.printStackTrace();
                fail(t.toString());
            }

            manager.reset();

            //No verify results in failed send.
            for (int i = 0; i < em.exceptions.size(); i++) {
                Throwable t = em.exceptions.get(i);
                if (t instanceof SendFailedException == false) {
                    System.err.println("Verify index=" + v);
                    t.printStackTrace();
                    fail(t.toString());
                }
            }
        }

        instance = startLogManagerReset("bad-enum-name");
        em = (InternalErrorManager) instance.getErrorManager();

        manager.reset();

        //Allow the LogManagerProperties to copy on a bad enum type.
        boolean foundIllegalArg = false;
        for (int i = 0; i < em.exceptions.size(); i++) {
            Throwable t = em.exceptions.get(i);
            if (t instanceof IllegalArgumentException) {
                foundIllegalArg = true;
            } else if (t instanceof RuntimeException) {
                t.printStackTrace();
                fail(t.toString());
            }
        }

        assertTrue(foundIllegalArg);
        assertFalse(em.exceptions.isEmpty());
        hardRef = null;
    }

    /**
     * Setup and load the standard properties.
     * @param verify the value of verify enum.
     * @return a MailHandler
     * @throws IOException if there is a problem.
     */
    private MailHandler startLogManagerReset(String verify) throws IOException {
        LogManager manager = LogManager.getLogManager();
        manager.reset();

        final String p = MailHandler.class.getName();
        Properties props = new Properties();
        props.put(p.concat(".mail.host"), "localhost");
        props.put(p.concat(".mail.smtp.host"), "localhost");
        props.put(p.concat(".mail.smtp.port"), "80"); //bad port.
        props.put(p.concat(".mail.to"), "localhost@localdomain");
        props.put(p.concat(".mail.cc"), "localhost@localdomain");
        props.put(p.concat(".mail.subject"), p.concat(" test"));
        props.put(p.concat(".mail.from"), "localhost@localdomain");
        props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
        if (verify != null) {
            props.put(p.concat(".verify"), verify);
        }
        props.put(p.concat(".mail.smtp.connectiontimeout"), "1");
        props.put(p.concat(".mail.smtp.timeout"), "1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.store(out, "No comment");

        manager.readConfiguration(new ByteArrayInputStream(out.toByteArray()));

        assertNotNull(manager.getProperty(p.concat(".mail.host")));
        assertNotNull(manager.getProperty(p.concat(".mail.smtp.host")));
        assertNotNull(manager.getProperty(p.concat(".mail.smtp.port")));
        assertNotNull(manager.getProperty(p.concat(".mail.to")));
        assertNotNull(manager.getProperty(p.concat(".mail.cc")));
        assertNotNull(manager.getProperty(p.concat(".mail.subject")));
        assertNotNull(manager.getProperty(p.concat(".mail.from")));
        assertEquals(verify, manager.getProperty(p.concat(".verify")));
        assertNotNull(manager.getProperty(p.concat(".mail.smtp.connectiontimeout")));
        assertNotNull(manager.getProperty(p.concat(".mail.smtp.timeout")));

        MailHandler instance = new MailHandler(10);
        instance.setLevel(Level.ALL);

        assertEquals(InternalErrorManager.class, instance.getErrorManager().getClass());

        final String CLASS_NAME = MailHandlerTest.class.getName();
        Logger logger = Logger.getLogger(CLASS_NAME);
        hardRef = logger;
        logger.setUseParentHandlers(false);
        logger.addHandler(instance);

        logger.log(Level.SEVERE, "");
        logger.log(Level.SEVERE, "");
        return instance;
    }

    private static boolean isConnectOrTimeout(Throwable t) {
        if (t instanceof MessagingException) {
            Throwable cause = ((MessagingException) t).getCause();
            if (cause != null) {
                return isConnectOrTimeout(cause);
            } else {
                String msg = t.getMessage();
                return msg != null && (msg.indexOf("connect") > -1 ||
                            msg.indexOf("80") > -1);
            }
        } else {
            return t instanceof java.net.ConnectException
                    || t instanceof java.net.SocketTimeoutException;
        }
    }

    @Test
    public void testPushLevel() {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getPushLevel());

        try {
            instance.setPushLevel(null);
            fail("Null level was allowed");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        Level[] lvls = getAllLevels();
        for (int i = 0; i < lvls.length; i++) {
            instance.setPushLevel(lvls[i]);
            assertEquals(instance.getPushLevel(), lvls[i]);
        }

        instance.close();
        for (int i = 0; i < lvls.length; i++) {
            instance.setPushLevel(lvls[i]);
            assertEquals(instance.getPushLevel(), lvls[i]);
        }
        assertEquals(true, em.exceptions.isEmpty());
    }

    @Test
    public void testPushFilter() {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        try {
            instance.setPushFilter(null);
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }
        assertNull(instance.getPushFilter());

        instance.setPushFilter(BooleanFilter.TRUE);
        assertEquals(BooleanFilter.TRUE, instance.getPushFilter());

        assertEquals(true, em.exceptions.isEmpty());

        instance = createHandlerWithRecords();
        instance.setErrorManager(new PushErrorManager(instance));
        instance.setPushFilter(BooleanFilter.TRUE);
        instance.setLevel(Level.ALL);
        instance.setPushLevel(Level.WARNING);
        instance.publish(new LogRecord(Level.SEVERE, ""));
        instance.close();
    }

    @Test
    public void testContentTypeOf() throws IOException {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setEncoding(null);
        String head = instance.contentTypeOf(new XMLFormatter().getHead(instance));
        assertEquals("application/xml", head);
        instance.setEncoding("US-ASCII");

        head = instance.contentTypeOf(new XMLFormatter().getHead(instance));
        assertEquals("application/xml", head);

        instance.setEncoding(null);
        head = instance.contentTypeOf(new SimpleFormatter().getHead(instance));
        assertNull(head);

        instance.setEncoding("US-ASCII");
        head = instance.contentTypeOf(new SimpleFormatter().getHead(instance));
        assertNull(head);

        instance.setEncoding(null);
        head = instance.contentTypeOf(new HeadFormatter("<HTML><BODY>").getHead(instance));
        assertEquals("text/html", head);

        instance.setEncoding(null);
        head = instance.contentTypeOf(new HeadFormatter("<html><body>").getHead(instance));
        assertEquals("text/html", head);

        instance.setEncoding("US-ASCII");
        head = instance.contentTypeOf(new HeadFormatter("<HTML><BODY>").getHead(instance));
        assertEquals("text/html", head);

        instance.setEncoding("US-ASCII");
        head = instance.contentTypeOf(new HeadFormatter("<HTML><HEAD></HEAD>"
                + "<BODY></BODY></HTML>").getHead(instance));
        assertEquals("text/html", head);

        instance.setEncoding(null);
        head = instance.contentTypeOf(new HeadFormatter("Head").getHead(instance));
        if (head != null) {//null is assumed to be plain text.
            assertEquals("text/plain", head);
        }

        instance.setEncoding("US-ASCII");
        head = instance.contentTypeOf(new HeadFormatter("Head").getHead(instance));
        if (head != null) { //null is assumed to be plain text.
            assertEquals("text/plain", head);
        }

        instance.setEncoding("US-ASCII");
        head = instance.contentTypeOf(new HeadFormatter("Head.......Neck.......Body").getHead(instance));
        if (head != null) { //null is assumed to be plain text.
            assertEquals("text/plain", head);
        }
        instance.close();

        for (int i = 0; i < em.exceptions.size(); i++) {
            fail(em.exceptions.get(i).toString());
        }
    }

    @Test
    public void testContentTypeOverride() throws Exception {
        String expected = "application/xml; charset=us-ascii";
        String type = getInlineContentType();
        assertEquals(expected, type);

        MimetypesFileTypeMap m = new MimetypesFileTypeMap();
        m.addMimeTypes("text/plain txt TXT XMLFormatter");
        final FileTypeMap old = FileTypeMap.getDefaultFileTypeMap();
        FileTypeMap.setDefaultFileTypeMap(m);
        try {
            type = getInlineContentType();
            assertEquals("text/plain; charset=us-ascii", type);
        } finally {
            FileTypeMap.setDefaultFileTypeMap(old);
        }

        type = getInlineContentType();
        assertEquals(expected, type);
    }

    private String getInlineContentType() throws Exception {
        final String[] value = new String[1];
        MailHandler instance = new MailHandler();
        instance.setEncoding("us-ascii");
        MessageErrorManager em = new MessageErrorManager(instance) {

            protected void error(MimeMessage msg, Throwable t, int code) {
                try {
                    MimeMultipart multi = (MimeMultipart) msg.getContent();
                    BodyPart body = multi.getBodyPart(0);
                    assertEquals(Part.INLINE, body.getDisposition());
                    value[0] = body.getContentType();
                } catch (Throwable E) {
                    E.printStackTrace();
                    fail(E.toString());
                }
            }
        };
        instance.setErrorManager(em);
        Properties props = new Properties();
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.host", "bad-host-name");
        props.put("mail.to", "localhost@localdomain");
        instance.setMailProperties(props);
        instance.setFormatter(new XMLFormatter());
        instance.publish(new LogRecord(Level.SEVERE, "test"));
        instance.close();

        return value[0];
    }

    @Test
    public void testComparator() {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        try {
            instance.setComparator(null);
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }
        assertNull(instance.getComparator());

        Comparator uselessComparator = new UselessComparator();
        Comparator result = instance.getComparator();
        assertEquals(false, uselessComparator.equals(result));

        instance.setComparator(uselessComparator);
        result = instance.getComparator();

        assertEquals(true, uselessComparator.equals(result));

        assertEquals(true, em.exceptions.isEmpty());
        instance.close();
    }

    @Test
    public void testCapacity() {
        try {
            MailHandler h = new MailHandler(-1);
            h.getCapacity();
            fail("Negative capacity was allowed.");
        } catch (IllegalArgumentException pass) {
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        try {
            MailHandler h = new MailHandler(0);
            h.getCapacity();
            fail("Zero capacity was allowed.");
        } catch (IllegalArgumentException pass) {
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        try {
            MailHandler h = new MailHandler(1);
            h.getCapacity();
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        final int expResult = 20;
        MailHandler instance = new MailHandler(20);
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        int result = instance.getCapacity();
        assertEquals(expResult, result);
        instance.close();

        result = instance.getCapacity();
        assertEquals(expResult, result);
        assertEquals(true, em.exceptions.isEmpty());
        instance.close();

        String SOURCE_CLASS = MailHandlerTest.class.getName();
        String SOURCE_METHOD = "testCapacity";
        for (int i = 0; i <= NUM_RUNS; i++) {
            instance = new MailHandler(nextCapacity(i));
            instance.setLevel(Level.ALL);
            instance.setPushLevel(Level.OFF);
            em = new InternalErrorManager();
            instance.setErrorManager(em);
            CountingFormatter formatter = new CountingFormatter();
            instance.setFormatter(formatter);
            Properties props = new Properties();
            props.put("mail.smtp.host", "bad-host-name");
            props.put("mail.host", "bad-host-name");
            instance.setMailProperties(props);
            for (int j = 0; j < instance.getCapacity(); j++) {
                LogRecord r = new LogRecord(Level.INFO, "");
                r.setSourceClassName(SOURCE_CLASS);
                r.setSourceMethodName(SOURCE_METHOD);
                instance.publish(r);
            }
            assertEquals(instance.getCapacity(), formatter.format);
            assertEquals(1, formatter.head);
            assertEquals(1, formatter.tail);
            assertEquals(1, em.exceptions.size());
            assertTrue(em.exceptions.get(0) instanceof MessagingException);
            instance.close();
        }
    }
    private static final int LOW_CAPACITY = 1000;
    private static final int MAX_CAPACITY = 1 << 18;
    private static final int NUM_RUNS = LOW_CAPACITY + 42;
    private static final Random RANDOM = new Random();

    /**
     * Test all numbers between 1 and low capacity.
     * @param capacity
     * @return
     */
    private int nextCapacity(int capacity) {
        if (capacity <= LOW_CAPACITY) {
            return ++capacity;
        } else {
            if (capacity < NUM_RUNS) {
                int next;
                do {
                    next = RANDOM.nextInt(MAX_CAPACITY);
                } while (next <= LOW_CAPACITY);
                return next;
            } else {
                return MAX_CAPACITY;
            }
        }
    }

    @Test
    public void testAuthenticator() {
        Authenticator auth = new EmptyAuthenticator();

        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        try {
            instance.setAuthenticator(null);
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        try {
            instance.setAuthenticator(instance.getAuthenticator());
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        try {
            instance.setAuthenticator(auth);
            assertEquals(auth, instance.getAuthenticator());
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        assertEquals(true, em.exceptions.isEmpty());

        instance = createHandlerWithRecords();
        instance.setAuthenticator(new ThrowAuthenticator());
        em = (InternalErrorManager) instance.getErrorManager();
        instance.close();

        assertEquals(1, em.exceptions.size());
        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);
    }

    @Test
    public void testMailProperties() {
        Properties props = new Properties();
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getMailProperties());
        assertEquals(Properties.class, instance.getMailProperties().getClass());

        try {
            instance.setMailProperties(null);
            fail("Null was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        instance.setMailProperties(props);
        Properties stored = instance.getMailProperties();

        assertNotNull(stored);
        assertEquals(false, props == stored);
        assertEquals(Properties.class, stored.getClass());

        assertEquals(true, em.exceptions.isEmpty());
        instance.close();

        final String p = MailHandler.class.getName();
        instance = createHandlerWithRecords();
        props = instance.getMailProperties();
        em = new InternalErrorManager();
        instance.setErrorManager(em);
        props.setProperty(p.concat(".mail.from"), "::1");
        props.setProperty(p.concat(".mail.to"), "::1");
        props.setProperty(p.concat(".mail.sender"), "::1");
        props.setProperty(p.concat(".mail.cc"), "::1");
        props.setProperty(p.concat(".mail.bcc"), "::1");
        props.setProperty(p.concat(".mail.reply.to"), "::1");
        instance.setMailProperties(props);
        instance.close();
        assertEquals(false, em.exceptions.isEmpty());
    }

    @Test
    public void testAttachmentFilters() {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        Filter[] result = instance.getAttachmentFilters();
        assertNotNull(result);
        assertEquals(result.length, instance.getAttachmentFormatters().length);


        assertEquals(false, instance.getAttachmentFilters() == result);

        if (instance.getAttachmentFormatters().length != 0) {
            instance.setAttachmentFormatters(new Formatter[0]);
        }

        try {
            instance.setAttachmentFilters(null);
            fail("Null allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            instance.setAttachmentFilters(new Filter[0]);
        } catch (RuntimeException re) {
            fail(re.toString());
        }


        try {
            assertEquals(0, instance.getAttachmentFormatters().length);

            instance.setAttachmentFilters(new Filter[]{BooleanFilter.TRUE});
            fail("Filter to formatter mismatch.");
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setAttachmentFormatters(
                new Formatter[]{new SimpleFormatter(), new XMLFormatter()});

        try {
            assertEquals(2, instance.getAttachmentFormatters().length);

            instance.setAttachmentFilters(new Filter[]{BooleanFilter.TRUE});
            fail("Filter to formatter mismatch.");
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            assertEquals(2, instance.getAttachmentFormatters().length);
            Filter[] filters = new Filter[]{BooleanFilter.TRUE, BooleanFilter.TRUE};
            assertEquals(instance.getAttachmentFormatters().length, filters.length);
            instance.setAttachmentFilters(filters);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            assertEquals(2, instance.getAttachmentFormatters().length);
            Filter[] filters = new Filter[]{null, null};
            assertEquals(instance.getAttachmentFormatters().length, filters.length);
            instance.setAttachmentFilters(filters);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            assertEquals(2, instance.getAttachmentFormatters().length);
            instance.setAttachmentFilters(new Filter[0]);
            fail("Filter to formatter mismatch.");
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            assertEquals(instance.getAttachmentFormatters().length, 2);
            Filter[] filters = new Filter[]{null, null};
            instance.setAttachmentFilters(filters);
            filters[0] = BooleanFilter.TRUE;
            assertEquals(filters[0], filters[0]);
            assertEquals(filters[0].equals(instance.getAttachmentFilters()[0]), false);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();
    }

    @Test
    public void testAttachmentFormatters() {
        MailHandler instance = new MailHandler();

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        Formatter[] result = instance.getAttachmentFormatters();
        assertNotNull(result);
        assertEquals(result == instance.getAttachmentFormatters(), false);

        assertEquals(result.length, instance.getAttachmentFilters().length);
        assertEquals(result.length, instance.getAttachmentNames().length);

        result = new Formatter[]{new SimpleFormatter(), new XMLFormatter()};
        instance.setAttachmentFormatters(result);

        assertEquals(result.length, instance.getAttachmentFilters().length);
        assertEquals(result.length, instance.getAttachmentNames().length);

        result[0] = new XMLFormatter();
        result[1] = new SimpleFormatter();
        assertEquals(result[1].getClass(),
                instance.getAttachmentFormatters()[0].getClass());
        assertEquals(result[0].getClass(),
                instance.getAttachmentFormatters()[1].getClass());

        try {
            instance.setAttachmentFormatters(null);
            fail("Null was allowed.");
        } catch (NullPointerException NPE) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        result[0] = null;
        try {
            instance.setAttachmentFormatters(result);
            fail("Null index was allowed.");
        } catch (NullPointerException NPE) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }


        result = new Formatter[0];
        try {
            instance.setAttachmentFormatters(result);
            assertEquals(result.length, instance.getAttachmentFilters().length);
            assertEquals(result.length, instance.getAttachmentNames().length);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();
    }

    @Test
    public void testAttachmentNames_StringArr() {
        Formatter[] names = null;
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        names = instance.getAttachmentNames();
        assertNotNull(names);

        try {
            instance.setAttachmentNames((String[]) null);
            fail("Null was allowed.");
        } catch (RuntimeException re) {
            assertEquals(NullPointerException.class, re.getClass());
        }

        if (instance.getAttachmentFormatters().length > 0) {
            instance.setAttachmentFormatters(new Formatter[0]);
        }

        try {
            instance.setAttachmentNames(new String[0]);
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        try {
            instance.setAttachmentNames(new String[1]);
            fail("Mismatch with attachment formatters.");
        } catch (NullPointerException pass) {
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setAttachmentFormatters(
                new Formatter[]{new SimpleFormatter(), new XMLFormatter()});
        try {
            instance.setAttachmentNames(new String[2]);
            fail("Null index was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        Formatter[] formatters = instance.getAttachmentFormatters();
        names = instance.getAttachmentNames();

        assertEquals(names[0].toString(), String.valueOf(formatters[0]));
        assertEquals(names[1].toString(), String.valueOf(formatters[1]));

        String[] stringNames = new String[]{"error.txt", "error.xml"};
        instance.setAttachmentNames(stringNames);
        assertEquals(stringNames[0], instance.getAttachmentNames()[0].toString());
        assertEquals(stringNames[1], instance.getAttachmentNames()[1].toString());

        stringNames[0] = "info.txt";
        assertEquals(stringNames[0].equals(
                instance.getAttachmentNames()[0].toString()), false);

        try {
            instance.setAttachmentNames(new String[0]);
            fail("Names mismatch formatters.");
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        assertEquals(true, em.exceptions.isEmpty());
    }

    @Test
    public void testAttachmentNames_FormatterArr() {
        Formatter[] formatters = null;
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getAttachmentNames());

        try {
            instance.setAttachmentNames((Formatter[]) null);
            fail("Null was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        if (instance.getAttachmentFormatters().length > 0) {
            instance.setAttachmentFormatters(new Formatter[0]);
        }

        try {
            instance.setAttachmentNames(new Formatter[2]);
            fail("formatter mismatch.");
        } catch (NullPointerException pass) {
        } catch (IndexOutOfBoundsException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setAttachmentFormatters(
                new Formatter[]{new SimpleFormatter(), new XMLFormatter()});

        assertEquals(instance.getAttachmentFormatters().length,
                instance.getAttachmentNames().length);

        formatters = new Formatter[]{new SimpleFormatter(), new XMLFormatter()};
        instance.setAttachmentNames(formatters);
        formatters[0] = new XMLFormatter();
        assertEquals(formatters[0].equals(instance.getAttachmentNames()[0]), false);

        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();
    }

    @Test
    public void testSubject_String() {
        String subject = "Test subject.";
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getSubject());

        try {
            instance.setSubject((String) null);
            fail("Null subject was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setSubject(subject);
        assertEquals(subject, instance.getSubject().toString());

        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();
    }

    @Test
    public void testTailFormatters() {
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        instance.setSubject(instance.toString());
        Formatter f1 = instance.getSubject();
        assertEquals(f1, f1);
        assertEquals(f1.hashCode(), f1.hashCode());
        assertEquals(f1.toString(), f1.toString());

        instance.setSubject(instance.toString());
        Formatter f2 = instance.getSubject();
        assertEquals(f2, f2);
        assertEquals(f2.hashCode(), f2.hashCode());
        assertEquals(f2.toString(), f2.toString());

        assertEquals(f1.getClass(), f2.getClass());
        assertEquals(f1.toString(), f2.toString());

        Formatter same = new XMLFormatter();
        instance.setAttachmentFormatters(
                new Formatter[]{same, same});
        Formatter[] formatters = instance.getAttachmentNames();
        f1 = formatters[0];
        f2 = formatters[1];

        assertEquals(f1, f1);
        assertEquals(f1.hashCode(), f1.hashCode());
        assertEquals(f1.toString(), f1.toString());

        assertEquals(f2, f2);
        assertEquals(f2.hashCode(), f2.hashCode());
        assertEquals(f2.toString(), f2.toString());

        assertEquals(f1.getClass(), f2.getClass());
        assertEquals(f1.toString(), f2.toString());

        assertFalse(f1.equals(new SimpleFormatter()));
        assertFalse(new SimpleFormatter().equals(f1));
        assertFalse(f2.equals(new SimpleFormatter()));
        assertFalse(new SimpleFormatter().equals(f2));

        //New in JavaMail 1.4.4.
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());

        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();
    }

    @Test
    public void testSubject_Formatter() {
        Formatter format = new SimpleFormatter();
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getSubject());

        try {
            instance.setSubject((Formatter) null);
            fail("Null subject was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        instance.setSubject(format);
        assertEquals(format, instance.getSubject());


        assertEquals(true, em.exceptions.isEmpty());
        instance.close();
    }

    @Test
    public void testReportError() {
        MailHandler instance = new MailHandler();
        instance.setErrorManager(new ErrorManager() {

            @Override
            public void error(String msg, Exception ex, int code) {
                assertNull(msg);
            }
        });

        instance.reportError(null, null, ErrorManager.GENERIC_FAILURE);



        instance.setErrorManager(new ErrorManager() {

            @Override
            public void error(String msg, Exception ex, int code) {
                assertEquals(msg.indexOf(Level.SEVERE.getName()), 0);
            }
        });

        instance.reportError("simple message.", null, ErrorManager.GENERIC_FAILURE);
        instance.close();

        //Test for valid message.
        instance = createHandlerWithRecords();
        instance.setErrorManager(new MessageErrorManager(instance) {

            protected void error(MimeMessage message, Throwable t, int code) {
                try {
                    assertTrue(message.getHeader("X-Mailer")[0].startsWith(MailHandler.class.getName()));
                    assertTrue(null != message.getSentDate());
                    message.saveChanges();
                } catch (MessagingException ME) {
                    fail(ME.toString());
                }
            }
        });
        instance.close();
    }

    @Test
    public void testSecurityManager() {
        InternalErrorManager em = null;
        MailHandler h = null;
        final ThrowSecurityManager manager = new ThrowSecurityManager();
        System.setSecurityManager(manager);
        try {
            manager.secure = false;
            h = new MailHandler();
            em = new InternalErrorManager();
            h.setErrorManager(em);
            Properties props = new Properties();
            props.put("mail.user", "bad-user");
            props.put("mail.smtp.host", "bad-host-name");
            props.put("mail.host", "bad-host-name");
            manager.secure = true;
            assertEquals(manager, System.getSecurityManager());

            try {
                assertEquals(0, h.getAttachmentFormatters().length);
                h.setAttachmentNames(new String[]{"error.txt"});
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertEquals(0, h.getAttachmentFormatters().length);
                h.setAttachmentNames(new Formatter[]{new ThrowFormatter()});
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertEquals(0, h.getAttachmentFormatters().length);
                h.setAttachmentFilters(new Filter[]{new ThrowFilter()});
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setAttachmentFormatters(new Formatter[]{new ThrowFormatter()});
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            manager.secure = false;
            try {
                h.setAttachmentFormatters(new Formatter[]{new ThrowFormatter()});
            } catch (SecurityException fail) {
                fail("Unexpected secure check.");
            } catch (Exception fail) {
                fail(fail.toString());
            } finally {
                manager.secure = true;
            }

            try {
                assertEquals(1, h.getAttachmentFormatters().length);
                h.setAttachmentFilters(new Filter[]{new ThrowFilter()});
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertEquals(1, h.getAttachmentFormatters().length);
                h.setAttachmentFilters((Filter[]) null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertEquals(1, h.getAttachmentFormatters().length);
                h.setAttachmentNames(new String[]{"error.txt"});
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertEquals(1, h.getAttachmentFormatters().length);
                h.setAttachmentNames((String[]) null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertEquals(1, h.getAttachmentFormatters().length);
                h.setAttachmentNames((Formatter[]) null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertEquals(1, h.getAttachmentFormatters().length);
                h.setAttachmentNames(new Formatter[]{new ThrowFormatter()});
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            manager.secure = false;
            try {
                assertEquals(1, h.getAttachmentFormatters().length);
                h.setAttachmentFormatters(new Formatter[0]);
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            } finally {
                manager.secure = true;
            }

            try {
                assertEquals(0, h.getAttachmentFormatters().length);
                assertEquals(0, h.getAttachmentFilters().length);
                assertEquals(0, h.getAttachmentNames().length);
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setAuthenticator(null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setComparator(null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getComparator();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setLevel(Level.ALL);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setLevel((Level) null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getLevel();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setFilter(BooleanFilter.FALSE);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setFilter(null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getFilter();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setFormatter(new EmptyFormatter());
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setFormatter(null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getFormatter();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertNotNull(h.getErrorManager());
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setErrorManager(new ErrorManager());
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setErrorManager(null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setEncoding(null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getEncoding();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.flush();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.push();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setMailProperties(new Properties());
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setMailProperties((Properties) null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setPushFilter(null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getPushFilter();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setPushLevel(Level.OFF);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setPushLevel((Level) null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getPushLevel();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setSubject((Formatter) null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setSubject((String) null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setSubject(new ThrowFormatter());
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setSubject("test");
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getSubject();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertTrue(h.getCapacity() > 0);
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getAuthenticator();
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.getMailProperties();
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.close();
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.publish(new LogRecord(Level.SEVERE, ""));
                h.flush();
            } catch (SecurityException fail) {
                fail(fail.toString());
            } catch (Exception fail) {
                fail(fail.toString());
            }

            //check for internal security exceptions
            for (Exception e : em.exceptions) {
                for (Throwable t = e; t != null; t = t.getCause()) {
                    if (t instanceof SecurityException) {
                        throw (SecurityException) t; //fail
                    }
                }
            }
            em.exceptions.clear();

            try {
                new MailHandler();
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                new MailHandler(100);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                new MailHandler(new Properties());
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                new MailHandler(-100);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (IllegalArgumentException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                new MailHandler((Properties) null);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }
        } finally {
            manager.secure = false;
            System.setSecurityManager(null);
            if (h != null) {
                h.close();
            }
        }
    }

    @Test
    public void testVerifyLogManager() throws Exception {
        LogManager manager = LogManager.getLogManager();
        try {
            manager.reset();

            final String p = MailHandler.class.getName();
            Properties props = new Properties();
            props.put(p.concat(".mail.host"), "bad-host-name");
            props.put(p.concat(".mail.smtp.host"), "bad-host-name");
            props.put(p.concat(".mail.smtp.port"), "80"); //bad port.
            props.put(p.concat(".mail.to"), "badAddress");
            props.put(p.concat(".mail.cc"), "badAddress");
            props.put(p.concat(".mail.subject"), p.concat(" test"));
            props.put(p.concat(".mail.from"), "badAddress");
            props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
            props.put(p.concat(".mail.smtp.connectiontimeout"), "1");
            props.put(p.concat(".mail.smtp.timeout"), "1");
            props.put(p.concat(".verify"), "local");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            props.store(out, "No comment");

            manager.readConfiguration(new ByteArrayInputStream(out.toByteArray()));
            out = null;

            MailHandler instance = new MailHandler();
            InternalErrorManager em =
                    (InternalErrorManager) instance.getErrorManager();

            assertFalse(em.exceptions.isEmpty());

            for (int i = 0; i < em.exceptions.size(); i++) {
                final Throwable t = em.exceptions.get(i);
                if (t instanceof AddressException == false) {
                    t.printStackTrace();
                    fail(t.toString());
                }
            }

            instance.close();

            props.put(p.concat(".verify"), "remote");
            out = new ByteArrayOutputStream();
            props.store(out, "No comment");
            manager.readConfiguration(new ByteArrayInputStream(out.toByteArray()));
            out = null;


            instance = new MailHandler();
            em = (InternalErrorManager) instance.getErrorManager();

            assertFalse(em.exceptions.isEmpty());

            for (int i = 0; i < em.exceptions.size(); i++) {
                final Throwable t = em.exceptions.get(i);
                if (t instanceof AddressException) {
                    continue;
                } else if (t.getMessage().indexOf("bad-host-name") > -1) {
                    continue;
                } else {
                    t.printStackTrace();
                    fail(t.toString());
                }
            }
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testVerifyProperties() throws Exception {
        Properties props = new Properties();
        props.put("mail.host", "bad-host-name");
        props.put("mail.smtp.host", "bad-host-name");
        props.put("mail.smtp.port", "80"); //bad port.
        props.put("mail.to", "badAddress");
        props.put("mail.cc", "badAddress");
        props.put("mail.subject", "test");
        props.put("mail.from", "badAddress");
        props.put("mail.smtp.connectiontimeout", "1");
        props.put("mail.smtp.timeout", "1");
        props.put("verify", "local");
        
        InternalErrorManager em = new InternalErrorManager();
        MailHandler instance = new MailHandler();
        try {
            instance.setErrorManager(em);
            instance.setMailProperties(props);

            for (int i = 0; i < em.exceptions.size(); i++) {
                final Throwable t = em.exceptions.get(i);
                if (t instanceof AddressException == false) {
                    t.printStackTrace();
                    fail(t.toString());
                }
            }
        } finally {
            instance.close();
        }

        props.put("verify", "remote");
        instance = new MailHandler();
        try {
            em = new InternalErrorManager();
            instance.setErrorManager(em);
            instance.setMailProperties(props);

            assertFalse(em.exceptions.isEmpty());

            for (int i = 0; i < em.exceptions.size(); i++) {
                final Throwable t = em.exceptions.get(i);
                if (t instanceof AddressException) {
                    continue;
                } else if (t.getMessage().indexOf("bad-host-name") > -1) {
                    continue;
                } else {
                    t.printStackTrace();
                    fail(t.toString());
                }
            }
        } finally {
            instance.close();
        }
    }

    /**
     * Test must run last.
     */
    @Test
    public void testZInit() {
        String tmp = System.getProperty("java.io.tmpdir");
        if (tmp == null) {
            tmp = System.getProperty("user.home");
        }

        File dir = new File(tmp);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        try {
            File cfg = File.createTempFile("mailhandler_test", ".properties", dir);
            cfg.deleteOnExit();
            System.setProperty(LOG_CFG_KEY, cfg.getAbsolutePath());
            try {
                initGoodTest(cfg, MailHandler.class,
                        new Class[0], new Object[0]);
                initBadTest(cfg, MailHandler.class,
                        new Class[0], new Object[0]);

                initGoodTest(cfg, MailHandler.class,
                        new Class[]{Integer.TYPE}, new Object[]{10});
                initBadTest(cfg, MailHandler.class,
                        new Class[]{Integer.TYPE}, new Object[]{100});
                initGoodTest(cfg, MailHandler.class,
                        new Class[]{Properties.class},
                        new Object[]{new Properties()});
                initBadTest(cfg, MailHandler.class,
                        new Class[]{Properties.class},
                        new Object[]{new Properties()});


                //Test subclass properties.
                initGoodTest(cfg, MailHandlerExt.class,
                        new Class[0], new Object[0]);
                initBadTest(cfg, MailHandlerExt.class,
                        new Class[0], new Object[0]);

                initGoodTest(cfg, MailHandlerExt.class,
                        new Class[]{Integer.TYPE}, new Object[]{10});
                initBadTest(cfg, MailHandlerExt.class,
                        new Class[]{Integer.TYPE}, new Object[]{100});

                initGoodTest(cfg, MailHandlerExt.class,
                        new Class[]{Properties.class},
                        new Object[]{new Properties()});
                initBadTest(cfg, MailHandlerExt.class,
                        new Class[]{Properties.class},
                        new Object[]{new Properties()});
            } finally {
                boolean v;
                v = cfg.delete();
                assertTrue(v || !cfg.exists());

                System.clearProperty(LOG_CFG_KEY);
                LogManager.getLogManager().readConfiguration();
            }
        } catch (Exception E) {
            E.printStackTrace();
            fail(E.toString());
        }
    }

    private static String freeTextSubject() {
        String name = "Mail Handler test subject";
        try {
            Class.forName(name); //ensure this can't be loaded.
            fail("Invalid subject: " + name);
        } catch (AssertionError fail) {
            throw fail;
        } catch (Throwable expected) {
        }
        return name;
    }

    private void initGoodTest(File cfg, Class<? extends MailHandler> type,
            Class[] types, Object[] params) throws Exception {

        final String p = type.getName();
        Properties props = new Properties();
        FileOutputStream out = new FileOutputStream(cfg);
        try {
            props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
            props.put(p.concat(".capacity"), "10");
            props.put(p.concat(".level"), "ALL");
            props.put(p.concat(".formatter"), XMLFormatter.class.getName());
            props.put(p.concat(".filter"), ThrowFilter.class.getName());
            props.put(p.concat(".authenticator"), EmptyAuthenticator.class.getName());
            props.put(p.concat(".pushLevel"), "WARNING");
            props.put(p.concat(".pushFilter"), ThrowFilter.class.getName());
            props.put(p.concat(".comparator"), ThrowComparator.class.getName());
            props.put(p.concat(".encoding"), "UTF-8");
            props.put(p.concat(".subject"), EmptyFormatter.class.getName());

            props.put(p.concat(".attachment.filters"),
                    "null, " + ThrowFilter.class.getName() + ", "
                    + ThrowFilter.class.getName());

            props.put(p.concat(".attachment.formatters"),
                    SimpleFormatter.class.getName() + ", "
                    + XMLFormatter.class.getName() + ", "
                    + SimpleFormatter.class.getName());

            props.put(p.concat(".attachment.names"), "msg.txt, "
                    + SimpleFormatter.class.getName() + ", error.txt");

            props.store(out, p);
        } finally {
            out.close();
        }

        LogManager.getLogManager().readConfiguration();
        MailHandler h = type.getConstructor(types).newInstance(params);
        assertEquals(10, h.getCapacity());
        assertEquals(Level.ALL, h.getLevel());
        assertEquals(ThrowFilter.class, h.getFilter().getClass());
        assertEquals(XMLFormatter.class, h.getFormatter().getClass());
        assertEquals(Level.WARNING, h.getPushLevel());
        assertEquals(ThrowFilter.class, h.getPushFilter().getClass());
        assertEquals("UTF-8", h.getEncoding());
        assertEquals(EmptyFormatter.class, h.getSubject().getClass());
        assertEquals(EmptyAuthenticator.class, h.getAuthenticator().getClass());
        assertEquals(3, h.getAttachmentFormatters().length);
        assertTrue(null != h.getAttachmentFormatters()[0]);
        assertTrue(null != h.getAttachmentFormatters()[1]);
        assertTrue(null != h.getAttachmentFormatters()[2]);
        assertEquals(3, h.getAttachmentFilters().length);
        assertEquals(null, h.getAttachmentFilters()[0]);
        assertEquals(ThrowFilter.class, h.getAttachmentFilters()[1].getClass());
        assertEquals(ThrowFilter.class, h.getAttachmentFilters()[2].getClass());
        assertEquals(3, h.getAttachmentNames().length);
        assertTrue(null != h.getAttachmentNames()[0]);
        assertTrue(null != h.getAttachmentNames()[1]);
        assertTrue(null != h.getAttachmentNames()[2]);

        InternalErrorManager em = (InternalErrorManager) h.getErrorManager();
        assertTrue(em.exceptions.isEmpty());

        for (int i = 0; i < em.exceptions.size(); i++) {
            fail(String.valueOf(em.exceptions.get(i)));
        }

        h.close();
        assertEquals(em.exceptions.isEmpty(), true);

        props.put(p.concat(".subject"), freeTextSubject());

        out = new FileOutputStream(cfg);
        try {
            props.store(out, p);
        } finally {
            out.close();
        }
        LogManager.getLogManager().readConfiguration();


        h = type.getConstructor(types).newInstance(params);
        em = (InternalErrorManager) h.getErrorManager();
        assertTrue(em.exceptions.isEmpty());
        assertEquals(freeTextSubject(), h.getSubject().toString());

        props.remove(p.concat(".attachment.filters"));
        out = new FileOutputStream(cfg);
        try {
            props.store(out, p);
        } finally {
            out.close();
        }
        LogManager.getLogManager().readConfiguration();

        h = type.getConstructor(types).newInstance(params);
        em = (InternalErrorManager) h.getErrorManager();
        assertTrue(em.exceptions.isEmpty());
        assertEquals(3, h.getAttachmentFormatters().length);
        h.close();

        props.remove(p.concat(".attachment.names"));
        out = new FileOutputStream(cfg);
        try {
            props.store(out, p);
        } finally {
            out.close();
        }
        LogManager.getLogManager().readConfiguration();

        h = type.getConstructor(types).newInstance(params);
        em = (InternalErrorManager) h.getErrorManager();
        assertTrue(em.exceptions.isEmpty());
        assertEquals(h.getAttachmentFormatters().length, 3);
        h.close();
    }

    private void initBadTest(File cfg, Class<? extends MailHandler> type,
            Class[] types, Object[] params) throws Exception {
        final PrintStream err = System.err;
        ByteArrayOutputStream oldErrors = new ByteArrayOutputStream();

        final String p = type.getName();
        Properties props = new Properties();
        FileOutputStream out = new FileOutputStream(cfg);
        try {
            props.put(p.concat(".errorManager"), "InvalidErrorManager");
            props.put(p.concat(".capacity"), "-10");
            props.put(p.concat(".level"), "BAD");
            props.put(p.concat(".formatter"), "InvalidFormatter");
            props.put(p.concat(".filter"), "InvalidFilter");
            props.put(p.concat(".authenticator"), ThrowAuthenticator.class.getName());
            props.put(p.concat(".pushLevel"), "PUSHBAD");
            props.put(p.concat(".pushFilter"), "InvalidPushFilter");
            props.put(p.concat(".comparator"), "InvalidComparator");
            props.put(p.concat(".encoding"), "MailHandler-ENC");
            props.put(p.concat(".subject"), ThrowFilter.class.getName());
            props.put(p.concat(".attachment.filters"), "null, "
                    + "InvalidAttachFilter1, " + ThrowFilter.class.getName());

            props.put(p.concat(".attachment.formatters"),
                    "InvalidAttachFormatter0, "
                    + ThrowComparator.class.getName() + ", "
                    + XMLFormatter.class.getName());

            props.put(p.concat(".attachment.names"), "msg.txt, "
                    + ThrowComparator.class.getName() + ", "
                    + XMLFormatter.class.getName());
            props.store(out, "Mail handler test file.");
        } finally {
            out.close();
        }

        MailHandler h = null;
        oldErrors.reset();
        System.setErr(new PrintStream(oldErrors));
        try {
            /**
             * Bad level value for property: com.sun.mail.util.logging.MailHandler.level
             * The LogManager.setLevelsOnExistingLoggers triggers an error.
             * This code swallows that error message.
             */
            LogManager.getLogManager().readConfiguration();
            System.err.flush();
            String result = oldErrors.toString().trim();
            oldErrors.reset();
            if (result.length() > 0) {
                final String expect = "Bad level value for property: " + p + ".level";
                //if (result.length() > expect.length()) {
                //    result = result.substring(0, expect.length());
                //}
                assertEquals(expect, result);
            }

            /**
             * The default error manager writes to System.err.
             * Since this test is trying to install an invalid ErrorManager
             * we can only capture the error by capturing System.err.
             */
            h = type.getConstructor(types).newInstance(params);
            System.err.flush();
            result = oldErrors.toString().trim();
            int index = result.indexOf(ErrorManager.class.getName() + ": "
                    + ErrorManager.OPEN_FAILURE + ": " + Level.SEVERE.getName()
                    + ": InvalidErrorManager");
            assertTrue(index > -1);
            assertTrue(result.indexOf(
                    "java.lang.ClassNotFoundException: InvalidErrorManager") > index);
            oldErrors.reset();
        } finally {
            System.setErr(err);
        }

        assertEquals(ErrorManager.class, h.getErrorManager().getClass());
        assertTrue(h.getCapacity() != 10);
        assertTrue(h.getCapacity() != -10);
        assertEquals(Level.WARNING, h.getLevel());
        assertEquals(null, h.getFilter());
        assertEquals(SimpleFormatter.class, h.getFormatter().getClass());
        assertEquals(Level.OFF, h.getPushLevel());
        assertEquals(null, h.getPushFilter());
        assertEquals(null, h.getEncoding());
        assertEquals(ThrowFilter.class.getName(), h.getSubject().toString());
        assertEquals(ThrowAuthenticator.class, h.getAuthenticator().getClass());
        assertEquals(3, h.getAttachmentFormatters().length);
        assertTrue(null != h.getAttachmentFormatters()[0]);
        assertTrue(null != h.getAttachmentFormatters()[1]);
        assertTrue(null != h.getAttachmentFormatters()[2]);
        assertEquals(3, h.getAttachmentFilters().length);
        assertTrue(null == h.getAttachmentFilters()[0]);
        assertTrue(null == h.getAttachmentFilters()[1]);
        assertTrue(null != h.getAttachmentFilters()[2]);
        assertEquals(ThrowFilter.class, h.getAttachmentFilters()[2].getClass());
        assertEquals(3, h.getAttachmentNames().length);
        assertTrue(null != h.getAttachmentNames()[0]);
        assertTrue(null != h.getAttachmentNames()[1]);
        assertTrue(null != h.getAttachmentNames()[2]);
        assertEquals(XMLFormatter.class, h.getAttachmentNames()[2].getClass());
        h.close();
    }

    private Level[] getAllLevels() {
        Field[] fields = Level.class.getFields();
        List a = new ArrayList(fields.length);
        for (int i = 0; i < fields.length; i++) {
            if (Modifier.isStatic(fields[i].getModifiers())
                    && Level.class.isAssignableFrom(fields[i].getType())) {
                try {
                    a.add(fields[i].get(null));
                } catch (IllegalArgumentException ex) {
                    fail(ex.toString());
                } catch (IllegalAccessException ex) {
                    fail(ex.toString());
                }
            }
        }
        return (Level[]) a.toArray(new Level[a.size()]);
    }

    private static abstract class MessageErrorManager extends InternalErrorManager {

        private final MailHandler h;

        protected MessageErrorManager(final MailHandler h) {
            if (h == null) {
                throw new NullPointerException();
            }
            this.h = h;
        }

        @Override
        public final void error(String msg, Exception ex, int code) {
            super.error(msg, ex, code);
            if (msg != null && msg.length() > 0
                    && !msg.startsWith(Level.SEVERE.getName())) {
                MimeMessage message = null;
                try {
                    byte[] b = msg.getBytes();
                    assertTrue(b.length > 0);

                    ByteArrayInputStream in = new ByteArrayInputStream(b);
                    Session session = Session.getInstance(h.getMailProperties());
                    message = new MimeMessage(session, in);
                    error(message, ex, code);
                } catch (Throwable T) {
                    fail(T.toString());
                }
            } else {
                new ErrorManager().error(msg, ex, code);
                fail("Message.writeTo failed.");
            }
        }

        protected abstract void error(MimeMessage msg, Throwable t, int code);
    }

    public static final class PushErrorManager extends MessageErrorManager {

        public PushErrorManager(MailHandler h) {
            super(h);
        }

        protected void error(MimeMessage message, Throwable t, int code) {
            try {
                assertTrue(null != message.getSentDate());
                assertNotNull(message.getHeader("X-Priority"));
                assertEquals("2", message.getHeader("X-Priority")[0]);
                assertNotNull(message.getHeader("Importance"));
                assertEquals("High", message.getHeader("Importance")[0]);
                assertNotNull(message.getHeader("Priority"));
                assertEquals("urgent", message.getHeader("Priority")[0]);
                message.saveChanges();
            } catch (RuntimeException RE) {
                fail(RE.toString());
            } catch (MessagingException ME) {
                fail(ME.toString());
            }
        }
    }

    public static final class FlushErrorManager extends MessageErrorManager {

        public FlushErrorManager(MailHandler h) {
            super(h);
        }

        protected void error(MimeMessage message, Throwable t, int code) {
            try {
                assertTrue(null != message.getSentDate());
                assertNull(message.getHeader("X-Priority"));
                assertNull(message.getHeader("Importance"));
                assertNull(message.getHeader("Priority"));
                message.saveChanges();
            } catch (RuntimeException RE) {
                fail(RE.toString());
            } catch (MessagingException ME) {
                fail(ME.toString());
            }
        }
    }

    public static class ThrowFilter implements Filter {

        public boolean isLoggable(LogRecord record) {
            throw new RuntimeException(record.toString());
        }
    }

    public static final class ThrowComparator implements Comparator, Serializable {

        private static final long serialVersionUID = 8493707928829966353L;

        public int compare(Object o1, Object o2) {
            throw new RuntimeException();
        }
    }

    public static final class ThrowFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            throw new RuntimeException("format");
        }

        @Override
        public String getHead(Handler h) {
            throw new RuntimeException("head");
        }

        @Override
        public String getTail(Handler h) {
            throw new RuntimeException("tail");
        }
    }

    public static class UselessComparator implements Comparator, Serializable {

        private static final long serialVersionUID = 7973575043680596722L;

        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };

    public static final class BooleanFilter implements Filter {

        static final BooleanFilter TRUE = new BooleanFilter(true);
        static final BooleanFilter FALSE = new BooleanFilter(false);
        private final boolean value;

        public BooleanFilter() {
            this(false);
        }

        private BooleanFilter(boolean v) {
            this.value = v;
        }

        public boolean isLoggable(LogRecord r) {
            return value;
        }
    }

    public static final class CountingFormatter extends Formatter {

        int head;
        int tail;
        int format;

        public String getHead(Handler h) {
            ++head;
            return "";
        }

        public String format(LogRecord record) {
            ++format;
            return String.valueOf(record.getMessage());
        }

        public String getTail(Handler h) {
            ++tail;
            return "";
        }
    }

    public static final class HeadFormatter extends Formatter {

        private final String name;

        public HeadFormatter() {
            this(null);
        }

        public HeadFormatter(final String name) {
            this.name = name;
        }

        public String getHead(Handler h) {
            return name;
        }

        public String format(LogRecord record) {
            return "";
        }
    }

    public static class InternalErrorManager extends ErrorManager {

        protected final List<Exception> exceptions = new ArrayList();

        @Override
        public void error(String msg, Exception ex, int code) {
            exceptions.add(ex);
        }
    }

    public static final class ThrowAuthenticator extends javax.mail.Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            throw new RuntimeException();
        }
    }

    public static final class EmptyAuthenticator extends javax.mail.Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("", "");
        }
    }

    public static final class EmptyFormatter extends Formatter {

        @Override
        public String format(LogRecord r) {
            return "";
        }
    }

    public static final class ThrowSecurityManager extends SecurityManager {

        boolean secure = false;

        @Override
        public void checkPermission(java.security.Permission perm) {
            if (secure) {
                super.checkPermission(perm);
                throw new SecurityException(perm.toString());
            }
        }

        @Override
        public void checkPermission(java.security.Permission perm, Object context) {
            if (secure) {
                super.checkPermission(perm, context);
                throw new SecurityException(perm.toString());
            }
        }
    }

    public static class ErrorFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            throw new Error("format");
        }

        @Override
        public String getHead(Handler h) {
            throw new Error("head");
        }

        @Override
        public String getTail(Handler h) {
            throw new Error("tail");
        }
    }

    public static class ErrorComparator implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = 1L;

        public int compare(LogRecord r1, LogRecord r2) {
            throw new Error("");
        }
    }

    public static class ErrorFilter implements Filter {

        public boolean isLoggable(LogRecord record) {
            throw new Error("");
        }
    }

    public final static class MailHandlerExt extends MailHandler {

        public MailHandlerExt() {
            super();
        }

        public MailHandlerExt(Properties props) {
            super(props);
        }

        public MailHandlerExt(int capacity) {
            super(capacity);
        }
    }

    private final static class CloseLogRecord extends LogRecord {

        private static final long serialVersionUID = 1L;
        private transient volatile Handler target;

        CloseLogRecord(Level level, String msg, final Handler target) {
            super(level, msg);
            this.target = target;
        }

        public String getSourceMethodName() {
            close();
            return super.getSourceMethodName();
        }

        public String getSourceClassName() {
            close();
            return super.getSourceClassName();
        }

        public boolean isClosed() {
            return this.target == null;
        }

        private void close() {
            final Handler h = this.target;
            if (h != null) {
                h.close();
                this.target = null;
            }
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.target = null;
        }
    }
}
