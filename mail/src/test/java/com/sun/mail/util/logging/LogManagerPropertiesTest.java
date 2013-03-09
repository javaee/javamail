/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2013 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2013 Jason Mehrens. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.*;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test case for the LogManagerProperties spec.
 * @author Jason Mehrens
 */
public class LogManagerPropertiesTest {

    /**
     * Holder used to inject Throwables into other APIs.
     */
    private final static ThreadLocal<Throwable> PENDING = new ThreadLocal<Throwable>();

    @Test
    public void testClone() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        Properties parent;
        LogManagerProperties mp;
        LogManager manager = LogManager.getLogManager();
        try {
            String key = prefix.concat(".dummy");
            parent = new Properties();
            parent.put(key, "value");

            read(manager, parent);

            parent = new Properties();
            mp = new LogManagerProperties(parent, prefix);

            assertFalse(contains(mp, key, null));
            assertEquals("value", mp.getProperty(key));
            assertTrue(contains(mp, key, "value")); //ensure copy worked.
        } finally {
            manager.reset();
        }

        Properties clone = (Properties) mp.clone();
        assertFalse(clone instanceof LogManagerProperties);
        assertEquals(Properties.class, clone.getClass());
        assertNotSame(clone, parent);
        assertNotSame(clone, mp);
        assertEquals(mp.size(), clone.size());
        assertTrue(clone.equals(mp)); //don't call mp.equals.
    }

    @Test
    public void testGetProperty_String() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        LogManager manager = LogManager.getLogManager();
        try {
            String key = prefix.concat(".dummy");
            Properties parent = new Properties();
            parent.put(key, "value");
            parent.put("", "empty");

            read(manager, parent);

            parent = new Properties();
            LogManagerProperties mp = new LogManagerProperties(parent, prefix);

            assertFalse(contains(mp, key, null));
            assertEquals("value", mp.getProperty(key));
            assertTrue(contains(mp, key, "value")); //ensure copy worked.
            parent.put(key, "newValue");
            assertEquals("newValue", mp.getProperty(key));
            assertEquals("empty", mp.getProperty(""));
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testGetProperty_String_String() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        LogManager manager = LogManager.getLogManager();
        try {
            String key = prefix.concat(".dummy");
            Properties parent = new Properties();
            parent.put(key, "value");
            parent.put("", "empty");

            read(manager, parent);

            parent = new Properties();
            LogManagerProperties mp = new LogManagerProperties(parent, prefix);

            assertFalse(contains(mp, key, null));
            assertEquals("value", mp.getProperty(key, null));
            assertTrue(contains(mp, key, "value")); //ensure copy worked.
            parent.put(key, "newValue");
            assertEquals("newValue", mp.getProperty(key, null));
            assertEquals("default", mp.getProperty("unknown", "default"));
            assertEquals("empty", mp.getProperty("", null));
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testGet() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        LogManager manager = LogManager.getLogManager();
        try {
            String key = prefix.concat(".dummy");
            Properties parent = new Properties();
            parent.put(key, "value");
            parent.put("", "empty");

            read(manager, parent);

            parent = new Properties();
            LogManagerProperties mp = new LogManagerProperties(parent, prefix);

            assertFalse(contains(mp, key, null));
            assertEquals("value", mp.get(key));
            assertTrue(contains(mp, key, "value")); //ensure copy worked.
            parent.put(key, "newValue");
            assertEquals("newValue", mp.get(key));
            assertEquals("empty", mp.get(""));
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testContainsKey() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        LogManager manager = LogManager.getLogManager();
        try {
            String key = prefix.concat(".dummy");
            Properties parent = new Properties();
            parent.put(key, "value");
            parent.put("", "empty");

            read(manager, parent);

            parent = new Properties();
            LogManagerProperties mp = new LogManagerProperties(parent, prefix);

            assertFalse(contains(mp, key, null));
            assertTrue(mp.containsKey(key));
            assertTrue(contains(mp, key, "value")); //ensure copy worked.
            parent.put(key, "newValue");
            assertEquals("newValue", mp.get(key));
            assertTrue(mp.containsKey(""));
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testRemove() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        LogManager manager = LogManager.getLogManager();
        try {
            String key = prefix.concat(".dummy");
            Properties parent = new Properties();
            parent.put(key, "value");
            parent.put("", "empty");

            read(manager, parent);

            parent = new Properties();
            LogManagerProperties mp = new LogManagerProperties(parent, prefix);

            assertFalse(contains(mp, key, null));
            assertEquals("value", mp.remove(key));
            assertFalse(contains(mp, key, "value")); //ensure copy worked.
            parent.put(key, "newValue");
            assertEquals("newValue", mp.remove(key));
            assertEquals("empty", mp.remove(""));
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testPut() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        LogManager manager = LogManager.getLogManager();
        try {
            String key = prefix.concat(".dummy");
            Properties parent = new Properties();
            parent.put(key, "value");
            parent.put("", "empty");

            read(manager, parent);

            parent = new Properties();
            LogManagerProperties mp = new LogManagerProperties(parent, prefix);

            assertFalse(contains(mp, key, null));
            assertEquals("value", mp.put(key, "newValue"));
            assertFalse(contains(mp, key, "value")); //ensure copy worked.
            assertTrue(contains(mp, key, "newValue")); //ensure copy worked.
            parent.put(key, "defValue");
            assertEquals("newValue", mp.remove(key));
            assertEquals("defValue", mp.remove(key));
            assertEquals("empty", mp.put("", ""));
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testSetProperty() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        LogManager manager = LogManager.getLogManager();
        try {
            String key = prefix.concat(".dummy");
            Properties parent = new Properties();
            parent.put(key, "value");
            parent.put("", "empty");

            read(manager, parent);

            parent = new Properties();
            LogManagerProperties mp = new LogManagerProperties(parent, prefix);

            assertFalse(contains(mp, key, null));
            assertEquals("value", mp.setProperty(key, "newValue"));
            assertFalse(contains(mp, key, "value")); //ensure copy worked.
            assertTrue(contains(mp, key, "newValue")); //ensure copy worked.
            parent.put(key, "defValue");
            assertEquals("newValue", mp.remove(key));
            assertEquals("defValue", mp.remove(key));
            assertEquals("empty", mp.setProperty("", ""));
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testPropUtil() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        LogManager manager = LogManager.getLogManager();
        try {
            String keyShort = "mail.smtp.reportsuccess";
            String key = prefix + '.' + keyShort;
            Properties parent = new Properties();
            parent.put(key, "true");

            read(manager, parent);

            parent = new Properties();
            LogManagerProperties mp = new LogManagerProperties(parent, prefix);
            assertFalse(contains(mp, keyShort, null));

            final Session session = Session.getInstance(mp);
            final Object t = session.getTransport("smtp");
            final String clazzName = "com.sun.mail.smtp.SMTPTransport";
            assertEquals(clazzName, t.getClass().getName());
            assertTrue(contains(mp, keyShort, "true"));
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testGetLogManager() throws Exception {
        LogManager manager = LogManagerProperties.getLogManager();
        assertNotNull(manager);
    }

    @Test
    public void testToLanguageTag() throws Exception {
        assertEquals("en-US", LogManagerProperties.toLanguageTag(Locale.US));
        assertEquals("en", LogManagerProperties.toLanguageTag(Locale.ENGLISH));
        assertEquals("", LogManagerProperties.toLanguageTag(new Locale("", "", "")));
        Locale l = new Locale("en", "US", "slang");
        assertEquals("en-US-slang", LogManagerProperties.toLanguageTag(l));
        l = new Locale("en", "", "slang");
        assertEquals("en--slang", LogManagerProperties.toLanguageTag(l));

        try {
            LogManagerProperties.toLanguageTag(null);
            fail("Null was allowed.");
        } catch (NullPointerException expect) {
        }
    }

    @Test
    public void testNewAuthenticator() throws Exception {
        try {
            LogManagerProperties.newAuthenticator(null);
            fail("Null was allowed.");
        } catch (NullPointerException expect) {
        }

        try {
            LogManagerProperties.newAuthenticator("");
            fail("Empty class was allowed.");
        } catch (ClassNotFoundException expect) {
        }

        try {
            LogManagerProperties.newAuthenticator(Object.class.getName());
            fail("Wrong type was allowed.");
        } catch (ClassCastException expect) {
        }

        final Class<?> type = ErrorAuthenticator.class;
        final javax.mail.Authenticator a =
                LogManagerProperties.newAuthenticator(type.getName());
        assertEquals(type, a.getClass());


        setPending(new RuntimeException());
        try {
            LogManagerProperties.newAuthenticator(type.getName());
            fail("Exception was not thrown.");
        } catch (InvocationTargetException expect) {
            assertEquals(RuntimeException.class, expect.getCause().getClass());
        } finally {
            setPending(null);
        }
    }

    @Test
    public void testNewComparator() throws Exception {
        try {
            LogManagerProperties.newComparator(null);
            fail("Null was allowed.");
        } catch (NullPointerException expect) {
        }

        try {
            LogManagerProperties.newComparator("");
            fail("Empty class was allowed.");
        } catch (ClassNotFoundException expect) {
        }

        try {
            LogManagerProperties.newComparator(Object.class.getName());
            fail("Wrong type was allowed.");
        } catch (ClassCastException expect) {
        }

        final Class<?> type = ErrorComparator.class;
        final Comparator c = LogManagerProperties.newComparator(type.getName());
        assertEquals(type, c.getClass());

        setPending(new RuntimeException());
        try {
            LogManagerProperties.newComparator(type.getName());
            fail("Exception was not thrown.");
        } catch (InvocationTargetException expect) {
            assertEquals(RuntimeException.class, expect.getCause().getClass());
        } finally {
            setPending(null);
        }
    }

    @Test
    public void testReverseOrder() throws Exception {
        try {
            LogManagerProperties.reverseOrder(null);
            fail("Null was allowed.");
        } catch (NullPointerException expect) {
        }

        Comparator<LogRecord> c = new ErrorComparator();
        Comparator<LogRecord> r = LogManagerProperties.reverseOrder(c);
        assertTrue(c.getClass() != r.getClass());
        assertFalse(r instanceof ErrorComparator);
        assertFalse(r instanceof AscComparator);
        assertFalse(r instanceof DescComparator);

        c = new AscComparator();
        r = LogManagerProperties.reverseOrder(c);
        assertTrue(r instanceof DescComparator);

        c = new AscComparator();
        r = LogManagerProperties.reverseOrder(c);
        assertTrue(r instanceof DescComparator);
    }


    @Test
    public void testNewErrorManager() throws Exception {
        try {
            LogManagerProperties.newErrorManager(null);
            fail("Null was allowed.");
        } catch (NullPointerException expect) {
        }

        try {
            LogManagerProperties.newErrorManager("");
            fail("Empty class was allowed.");
        } catch (ClassNotFoundException expect) {
        }

        try {
            LogManagerProperties.newErrorManager(Object.class.getName());
            fail("Wrong type was allowed.");
        } catch (ClassCastException expect) {
        }

        final Class<?> type = ErrorManager.class;
        ErrorManager f = LogManagerProperties.newErrorManager(type.getName());
        assertEquals(type, f.getClass());

        setPending(new RuntimeException());
        try {
            final String name = ErrorErrorManager.class.getName();
            LogManagerProperties.newErrorManager(name);
            fail("Exception was not thrown.");
        } catch (InvocationTargetException expect) {
            assertEquals(RuntimeException.class, expect.getCause().getClass());
        } finally {
            setPending(null);
        }
    }

    @Test
    public void testNewFilter() throws Exception {
        try {
            LogManagerProperties.newFilter(null);
            fail("Null was allowed.");
        } catch (NullPointerException expect) {
        }

        try {
            LogManagerProperties.newFilter("");
            fail("Empty class was allowed.");
        } catch (ClassNotFoundException expect) {
        }

        try {
            LogManagerProperties.newFilter(Object.class.getName());
            fail("Wrong type was allowed.");
        } catch (ClassCastException expect) {
        }

        final Class<?> type = ErrorFilter.class;
        final Filter f = LogManagerProperties.newFilter(type.getName());
        assertEquals(type, f.getClass());

        setPending(new RuntimeException());
        try {
            LogManagerProperties.newFilter(type.getName());
            fail("Exception was not thrown.");
        } catch (InvocationTargetException expect) {
            assertEquals(RuntimeException.class, expect.getCause().getClass());
        } finally {
            setPending(null);
        }
    }

    @Test
    public void testNewFormatter() throws Exception {
        try {
            LogManagerProperties.newFormatter(null);
            fail("Null was allowed.");
        } catch (NullPointerException expect) {
        }

        try {
            LogManagerProperties.newFormatter("");
            fail("Empty class was allowed.");
        } catch (ClassNotFoundException expect) {
        }

        try {
            LogManagerProperties.newFormatter(Object.class.getName());
            fail("Wrong type was allowed.");
        } catch (ClassCastException expect) {
        }

        final Class<?> type = SimpleFormatter.class;
        final Formatter f = LogManagerProperties.newFormatter(type.getName());
        assertEquals(type, f.getClass());

        setPending(new RuntimeException());
        try {
            final String name = ErrorFormatter.class.getName();
            LogManagerProperties.newFormatter(name);
            fail("Exception was not thrown.");
        } catch (InvocationTargetException expect) {
            assertEquals(RuntimeException.class, expect.getCause().getClass());
        } finally {
            setPending(null);
        }
    }

    @Test
    public void testEscapingAuthenticator() throws Exception {
        try {
            Class<?> k = ErrorAuthenticator.class;
            javax.mail.Authenticator a;

            a = LogManagerProperties.newAuthenticator(k.getName());
            assertEquals(k, a.getClass());

            setPending(new ThreadDeath());
            try {
                a = LogManagerProperties.newAuthenticator(k.getName());
                fail(String.valueOf(a));
            } catch (ThreadDeath expect) {
            }

            setPending(new OutOfMemoryError());
            try {
                a = LogManagerProperties.newAuthenticator(k.getName());
                fail(String.valueOf(a));
            } catch (OutOfMemoryError expect) {
            }
        } finally {
            setPending(null);
        }
    }

    @Test
    public void testEscapingComparator() throws Exception {
        try {
            Class<?> k = ErrorComparator.class;
            Comparator c;

            c = LogManagerProperties.newComparator(k.getName());
            assertEquals(k, c.getClass());

            setPending(new ThreadDeath());
            try {
                c = LogManagerProperties.newComparator(k.getName());
                fail(String.valueOf(c));
            } catch (ThreadDeath expect) {
            }

            setPending(new OutOfMemoryError());
            try {
                c = LogManagerProperties.newComparator(k.getName());
                fail(String.valueOf(c));
            } catch (OutOfMemoryError expect) {
            }
        } finally {
            setPending(null);
        }
    }

    @Test
    public void testEscapingErrorErrorManager() throws Exception {
        try {
            Class<?> k = ErrorErrorManager.class;
            ErrorManager f;

            f = LogManagerProperties.newErrorManager(k.getName());
            assertEquals(k, f.getClass());

            setPending(new ThreadDeath());
            try {
                f = LogManagerProperties.newErrorManager(k.getName());
                fail(String.valueOf(f));
            } catch (ThreadDeath expect) {
            }

            setPending(new OutOfMemoryError());
            try {
                f = LogManagerProperties.newErrorManager(k.getName());
                fail(String.valueOf(f));
            } catch (OutOfMemoryError expect) {
            }
        } finally {
            setPending(null);
        }
    }

    @Test
    public void testEscapingFilter() throws Exception {
        try {
            Class<?> k = ErrorFilter.class;
            Filter f;

            f = LogManagerProperties.newFilter(k.getName());
            assertEquals(k, f.getClass());

            setPending(new ThreadDeath());
            try {
                f = LogManagerProperties.newFilter(k.getName());
                fail(String.valueOf(f));
            } catch (ThreadDeath expect) {
            }

            setPending(new OutOfMemoryError());
            try {
                f = LogManagerProperties.newFilter(k.getName());
                fail(String.valueOf(f));
            } catch (OutOfMemoryError expect) {
            }
        } finally {
            setPending(null);
        }
    }

    @Test
    public void testEscapingFormatter() throws Exception {
        try {
            Class<?> k = ErrorFormatter.class;
            Formatter f;

            f = LogManagerProperties.newFormatter(k.getName());
            assertEquals(k, f.getClass());

            setPending(new ThreadDeath());
            try {
                f = LogManagerProperties.newFormatter(k.getName());
                fail(String.valueOf(f));
            } catch (ThreadDeath expect) {
            }

            setPending(new OutOfMemoryError());
            try {
                f = LogManagerProperties.newFormatter(k.getName());
                fail(String.valueOf(f));
            } catch (OutOfMemoryError expect) {
            }
        } finally {
            setPending(null);
        }
    }

    private static void setPending(final Throwable t) {
        if (t != null) {
            PENDING.set(t);
        } else {
            PENDING.remove();
        }
    }

    static void throwPendingIfSet() {
        final Throwable t = PENDING.get();
        if (t != null) {
            if (t instanceof Error) {
                t.fillInStackTrace();
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                t.fillInStackTrace();
                throw (RuntimeException) t;
            } else {
                throw new AssertionError(t);
            }
        }
    }

    private void read(LogManager manager, Properties props) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        props.store(out, "No comment");
        manager.readConfiguration(new ByteArrayInputStream(out.toByteArray()));
    }

    private boolean contains(Properties props, String key, String value) {
        if (key == null) {
            throw new NullPointerException();
        }

        //walk the entry set so we don't preload a key from the manager.
        for (Map.Entry<?, ?> e : props.entrySet()) {
            if (key.equals(e.getKey())) {
                return value.equals(e.getValue());
            }
        }
        return false;
    }

    public static final class ErrorAuthenticator extends javax.mail.Authenticator {

        public ErrorAuthenticator() {
            throwPendingIfSet();
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            throw new Error("");
        }
    }

    public static class ErrorComparator implements Comparator<LogRecord>,
                                                                Serializable {

        private static final long serialVersionUID = 1L;

        public ErrorComparator() {
            throwPendingIfSet();
        }

        public int compare(LogRecord r1, LogRecord r2) {
            throw new Error("");
        }
    }

    public static class AscComparator implements Comparator<LogRecord>,
                                                                Serializable {
        private static final long serialVersionUID = 1L;

        public int compare(LogRecord r1, LogRecord r2) {
            throw new UnsupportedOperationException();
        }

        public Comparator<LogRecord> reverseOrder() {
            return new DescComparator();
        }
    }

    public static class DescComparator implements Comparator<LogRecord>,
                                                                Serializable {
        private static final long serialVersionUID = 1L;

        public int compare(LogRecord r1, LogRecord r2) {
            throw new UnsupportedOperationException();
        }

        public Comparator<LogRecord> reverseOrder() {
            return new AscComparator();
        }
    }

    public static class ErrorFilter implements Filter {

        public ErrorFilter() {
            throwPendingIfSet();
        }

        public boolean isLoggable(LogRecord record) {
            throw new Error("");
        }
    }

    public static class ErrorFormatter extends Formatter {

        public ErrorFormatter() {
            throwPendingIfSet();
        }

        public String format(LogRecord record) {
            throw new Error("");
        }
    }

    public static class ErrorErrorManager extends ErrorManager {

        public ErrorErrorManager() {
            throwPendingIfSet();
        }

        @Override
        public void error(String msg, Exception ex, int code) {
            throw new Error("");
        }
    }
}
