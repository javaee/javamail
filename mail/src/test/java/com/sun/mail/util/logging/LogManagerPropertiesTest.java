/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2015 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2015 Jason Mehrens. All rights reserved.
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

import java.io.*;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import junit.framework.Assert;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test case for the LogManagerProperties spec.
 *
 * @author Jason Mehrens
 */
public class LogManagerPropertiesTest {

    /**
     * Holder used to inject Throwables into other APIs.
     */
    private final static ThreadLocal<Throwable> PENDING = new ThreadLocal<Throwable>();

    @BeforeClass
    public static void setUpClass() throws Exception {
        Assert.assertNull(System.getSecurityManager());
    }

    private static void fullFence() {
        LogManager.getLogManager().getProperty("");
    }

    private static void assumeNoJit() {
        CompilationMXBean c = ManagementFactory.getCompilationMXBean();
        if (c != null) { //-Xint
            Assume.assumeNoException(new IllegalArgumentException(
                    c.getName() + " must be disabled."));
        }
    }

    @Before
    public void setUp() {
        fullFence();
    }

    @After
    public void tearDown() {
        fullFence();
    }

    @Test
    public void testCheckAccessPresent() {
        LogManager m = LogManager.getLogManager();
        m.checkAccess();
        LogManagerProperties.checkLogManagerAccess();

        LogPermSecurityManager sm = new LogPermSecurityManager();
        sm.secure = false;
        System.setSecurityManager(sm);
        try {
            sm.secure = true;
            try {
                m.checkAccess();
                fail(m.toString());
            } catch (SecurityException expect) {
            }

            try {
                LogManagerProperties.checkLogManagerAccess();
                fail(LogManagerProperties.class.getName());
            } catch (SecurityException expect) {
            }
        } finally {
            sm.secure = false;
            System.setSecurityManager((SecurityManager) null);
        }
    }

    @Ignore
    public void testCheckAccessAbsent() throws Exception {
        assumeNoJit();
        final Class<?> k = LogManagerProperties.class;
        final Field f = k.getDeclaredField("LOG_MANAGER");
        Field mod = setAccessible(f);
        try {
            final Object lm = f.get(null);
            f.set(null, null);
            try {
                fullFence();
                LogManagerProperties.checkLogManagerAccess();

                LogPermSecurityManager sm = new LogPermSecurityManager();
                sm.secure = false;
                System.setSecurityManager(sm);
                try {
                    sm.secure = true;
                    try {
                        LogManagerProperties.checkLogManagerAccess();
                        fail(LogManagerProperties.class.getName());
                    } catch (SecurityException expect) {
                    }
                } finally {
                    sm.secure = false;
                    System.setSecurityManager((SecurityManager) null);
                }
            } finally {
                f.set(null, lm);
                fullFence();
            }
        } finally {
            mod.setInt(f, f.getModifiers() | Modifier.FINAL);
        }
    }

    @Test
    public void testFromLogManagerPresent() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        LogManager manager = LogManager.getLogManager();
        try {
            String key = prefix.concat(".dummy");
            String value = "value";
            String emptyValue = "empty";
            Properties parent = new Properties();
            parent.put(key, value);
            parent.put("", emptyValue);

            read(manager, parent);
            assertTrue(LogManagerProperties.hasLogManager());
            assertEquals(value, LogManagerProperties.fromLogManager(key));
            assertEquals(emptyValue, LogManagerProperties.fromLogManager(""));
        } finally {
            manager.reset();
        }

        try {
            LogManagerProperties.fromLogManager((String) null);
            fail("");
        } catch (NullPointerException expect) {
        }
    }

    @Ignore
    public void testFromLogManagerNull() throws Exception {
        assumeNoJit();
        testFromLogManager((Properties) null);
    }

    @Ignore
    public void testFromLogManagerAbsent() throws Exception {
        assumeNoJit();
        final String cfgKey = "java.util.logging.config.file";
        final Class<?> k = LogManagerProperties.class;
        String old = System.getProperty(cfgKey);
        try {
            Properties props = new Properties();
            props.put("", "empty");
            props.put(k.getName().concat(".dummy"), "value");
            final File f = File.createTempFile(k.getName(), ".properties");
            try {
                final FileOutputStream out = new FileOutputStream(f);
                try {
                    props.store(out, "testFromLogManagerAbsent");
                } finally {
                    out.close();
                }
                System.setProperty(cfgKey, f.getAbsolutePath());
                final Method m = k.getDeclaredMethod("readConfiguration");
                assertTrue(Modifier.isPrivate(m.getModifiers()));
                m.setAccessible(true);
                props = (Properties) m.invoke(null);
                testFromLogManager(props);
            } finally {
                assertTrue(f.toString(), f.delete() || !f.exists());
            }
        } finally {
            if (old != null) {
                System.setProperty(cfgKey, old);
            } else {
                System.clearProperty(cfgKey);
            }
        }
    }

    private void testFromLogManager(Properties parent) throws Exception {
        assertTrue(LogManagerProperties.hasLogManager());
        final Class<?> k = LogManagerProperties.class;
        final Field f = k.getDeclaredField("LOG_MANAGER");
        Field mod = setAccessible(f);
        try {
            fullFence();
            final Object lm = f.get(null);
            f.set(null, parent);
            try {
                fullFence();
                assertFalse(LogManagerProperties.hasLogManager());
                if (parent != null) {
                    assertFalse(parent.isEmpty());
                    for (Map.Entry<Object, Object> e : parent.entrySet()) {
                        String key = e.getKey().toString();
                        String val = LogManagerProperties.fromLogManager(key);
                        assertEquals(e.getValue(), val);
                    }
                } else {
                    assertNull(LogManagerProperties.fromLogManager(""));
                    assertNull(LogManagerProperties.fromLogManager("val"));
                }

                try {
                    LogManagerProperties.fromLogManager((String) null);
                    fail("");
                } catch (NullPointerException expect) {
                }
            } finally {
                f.set(null, lm);
                fullFence();
            }
        } finally {
            mod.setInt(f, f.getModifiers() | Modifier.FINAL);
            fullFence();
        }
    }

    @Test
    public void testJavaMailLinkage() throws Exception {
        for (Method m : LogManagerProperties.class.getDeclaredMethods()) {
            assertFalse(m.getReturnType().getName(),
                    isFromJavaMail(m.getReturnType()));
            for (Class<?> p : m.getParameterTypes()) {
                assertFalse(p.getName(), isFromJavaMail(p));
            }

            for (Class<?> e : m.getExceptionTypes()) {
                assertFalse(e.getName(), isFromJavaMail(e));
            }
        }

        for (Constructor<?> c : LogManagerProperties.class.getDeclaredConstructors()) {
            for (Class<?> p : c.getParameterTypes()) {
                assertFalse(p.getName(), isFromJavaMail(p));
            }

            for (Class<?> e : c.getExceptionTypes()) {
                assertFalse(e.getName(), isFromJavaMail(e));
            }
        }

        for (Field f : LogManagerProperties.class.getDeclaredFields()) {
            assertFalse(f.getName(), isFromJavaMail(f.getType()));
        }
    }

    private boolean isFromJavaMail(Class<?> k) throws Exception {
        for (Class<?> t = k; t != null; t = t.getSuperclass()) {
            final String n = t.getName();
            if (n.startsWith("javax.mail.")) {
                return true;
            }

            //Not included with logging-mailhandler.jar.
            if (n.startsWith("com.sun.mail.")
                    && !n.startsWith("com.sun.mail.util.logging.")) {
                return true;
            }
        }
        return false;
    }

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
    public void testIsReflection() throws Exception {
        assertTrue(LogManagerProperties.isReflectionClass(Constructor.class.getName()));
        assertTrue(LogManagerProperties.isReflectionClass(Method.class.getName()));
    }

    @Test
    public void testIsStaticUtilityClass() throws Exception {
        boolean nullCheck;
        try {
            LogManagerProperties.isStaticUtilityClass((String) null);
            nullCheck = false;
        } catch (NullPointerException expect) {
            nullCheck = true;
        }

        if (!nullCheck) {
            fail("Null check");
        }

        String[] utils = {
            "java.lang.System",
            "java.nio.channels.Channels",
            "java.util.Collections",
            "javax.mail.internet.MimeUtility",
            "org.junit.Assert"
        };

        testIsStaticUtilityClass(utils, true);

        String[] obj = {
            "java.lang.Exception",
            "java.lang.Object",
            "java.lang.Runtime",
            "java.io.Serializable"
        };
        testIsStaticUtilityClass(obj, false);

        String[] enumerations = {
            "java.util.concurrent.TimeUnit"
        };
        testIsStaticUtilityClass(enumerations, false);

        String[] fail = {
            "badClassName"
        };
        for (String name : fail) {
            boolean pass;
            try {
                LogManagerProperties.isStaticUtilityClass(name);
                pass = false;
            } catch (ClassNotFoundException expect) {
                pass = true;
            }

            if (!pass) {
                fail(name);
            }
        }
    }

    private void testIsStaticUtilityClass(String[] names, boolean complement) throws Exception {
        assertFalse(names.length == 0);

        if (complement) {
            for (String name : names) {
                assertTrue(name, LogManagerProperties.isStaticUtilityClass(name));
            }
        } else {
            for (String name : names) {
                assertFalse(name, LogManagerProperties.isStaticUtilityClass(name));
            }
        }
    }

    @Test
    public void testGetLocalHost() throws Exception {
        String host = LogManagerPropertiesTest.class.getName();
        Properties p = new Properties();
        p.setProperty("mail.smtp.localhost", host);
        Session s = Session.getInstance(p);
        Transport t = s.getTransport(InternetAddress.getLocalAddress(s));
        String h = LogManagerProperties.getLocalHost(t);
        Assert.assertEquals(host, h);
    }

    @Test
    public void testGetLocalHostMissing() throws Exception {
        Session session = Session.getInstance(new Properties());
        Service svc = new NoHostService(session);
        try {
            LogManagerProperties.getLocalHost(svc);
            fail("");
        } catch (NoSuchMethodException expect) {
        }
    }

    @Test
    public void testGetLocalHostSecure() throws Exception {
        Session session = Session.getInstance(new Properties());
        Service svc = new NotAllowedService(session);
        try {
            LogManagerProperties.getLocalHost(svc);
        } catch (SecurityException allowed) {
        } catch (InvocationTargetException expect) {
            Assert.assertTrue(expect.getCause() instanceof SecurityException);
        }
    }

    @Test
    public void testGetLocalHostNull() throws Exception {
        boolean fail = true;
        try {
            LogManagerProperties.getLocalHost((Service) null);
        } catch (NullPointerException expected) {
            fail = false;
        }
        Assert.assertFalse(fail);
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
    public void testGetObject() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        Properties parent = new Properties();
        LogManagerProperties mp = new LogManagerProperties(parent, prefix);
        String key = "key";
        Object value = new Object();
        parent.put(key, value);

        assertEquals(parent.get(key), mp.get(key));
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
    public void testContainsKeyObject() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        Properties parent = new Properties();
        LogManagerProperties mp = new LogManagerProperties(parent, prefix);
        String key = "key";
        Object value = new Object();
        parent.put(key, value);
        assertEquals(parent.containsKey(key), mp.containsKey(key));
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
    public void testRemoveObject() {
        String prefix = LogManagerPropertiesTest.class.getName();
        Properties parent = new Properties();
        LogManagerProperties mp = new LogManagerProperties(parent, prefix);
        String key = "key";
        Object value = new Object();
        parent.put(key, value);
        assertEquals(value, parent.remove(key));
        assertEquals(parent.containsKey(key), mp.containsKey(key));
        assertNull(parent.remove(key));
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
    public void testPutObject() throws Exception {
        String prefix = LogManagerPropertiesTest.class.getName();
        Properties parent = new Properties();
        LogManagerProperties mp = new LogManagerProperties(parent, prefix);
        String key = "key";
        Object value = TimeUnit.MILLISECONDS;
        assertNull(mp.put(key, value));
        Object newValue = TimeUnit.NANOSECONDS;
        assertEquals(value, mp.put(key, newValue));
        assertEquals(newValue, mp.get(key));
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
    public void testNewObjectFrom() throws Exception {
        try {
            LogManagerProperties.newObjectFrom((String) null, Object.class);
            fail("Null name was allowed.");
        } catch (NullPointerException expect) {
        }

        try {
            LogManagerProperties.newObjectFrom(Object.class.getName(),
                    (Class<Object>) null);
            fail("Null class was allowed.");
        } catch (NullPointerException expect) {
        }

        try {
            LogManagerProperties.newObjectFrom((String) null,
                    (Class<Object>) null);
            fail("Null was allowed.");
        } catch (NullPointerException expect) {
        }

        try {
            LogManagerProperties.newObjectFrom("", Object.class);
            fail("Empty class was allowed.");
        } catch (ClassNotFoundException expect) {
        }

        try {
            LogManagerProperties.newObjectFrom(Object.class.getName(),
                    String.class);
            fail("Wrong type was allowed.");
        } catch (ClassCastException expect) {
        }

        Object o = LogManagerProperties.
                newObjectFrom(String.class.getName(), Object.class);
        assertEquals(String.class, o.getClass());

        String n = LogManagerProperties.
                newObjectFrom(String.class.getName(), String.class);
        assertEquals(String.class, n.getClass());
    }

    @Test
    public void testNewAuthenticator() throws Exception {
        Authenticator a = LogManagerProperties.newObjectFrom(
                EmptyAuthenticator.class.getName(),
                Authenticator.class);
        assertEquals(EmptyAuthenticator.class, a.getClass());

        final Class<?> type = ErrorAuthenticator.class;
        a = LogManagerProperties.newObjectFrom(
                type.getName(), Authenticator.class);
        assertEquals(type, a.getClass());

        setPending(new RuntimeException());
        try {
            LogManagerProperties.newObjectFrom(type.getName(),
                    Authenticator.class);
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
        final Comparator<? super LogRecord> c
                = LogManagerProperties.newComparator(type.getName());
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

            a = LogManagerProperties.newObjectFrom(k.getName(), Authenticator.class);
            assertEquals(k, a.getClass());

            setPending(new ThreadDeath());
            try {
                a = LogManagerProperties.newObjectFrom(k.getName(), Authenticator.class);
                fail(String.valueOf(a));
            } catch (ThreadDeath expect) {
            }

            setPending(new OutOfMemoryError());
            try {
                a = LogManagerProperties.newObjectFrom(k.getName(), Authenticator.class);
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
            Comparator<? super LogRecord> c;

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
        Throwable t = PENDING.get();
        if (t != null) {
            if (t instanceof Error) {
                t = t.fillInStackTrace();
                assert t instanceof Error : t;
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                t = t.fillInStackTrace();
                assert t instanceof RuntimeException : t;
                throw (RuntimeException) t;
            } else {
                throw new AssertionError(t);
            }
        }
    }

    private static Field setAccessible(Field f) {
        f.setAccessible(true);
        try {
            assumeNoJit();
            if (Modifier.isFinal(f.getModifiers())) {
                Field mod = Field.class.getDeclaredField("modifiers");
                mod.setAccessible(true);
                mod.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                return mod;
            }
        } catch (RuntimeException re) {
            Assume.assumeNoException(re);
        } catch (Exception e) {
            Assume.assumeNoException(e);
        }
        throw new AssertionError();
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

    public static final class EmptyAuthenticator extends javax.mail.Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return null;
        }
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

        public Comparator<LogRecord> reversed() {
            return new DescComparator();
        }
    }

    public static class DescComparator implements Comparator<LogRecord>,
            Serializable {

        private static final long serialVersionUID = 1L;

        public int compare(LogRecord r1, LogRecord r2) {
            throw new UnsupportedOperationException();
        }

        public Comparator<LogRecord> reversed() {
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

    private static final class NoHostService extends Service {

        public NoHostService(Session session) {
            super(session, new URLName("test://somehost"));
        }
    }

    private static final class NotAllowedService extends Service {

        public NotAllowedService(Session session) {
            super(session, new URLName("test://somehost"));
        }

        public String getLocalHost() {
            throw new SecurityException();
        }
    }

    private static final class LogPermSecurityManager extends SecurityManager {

        volatile boolean secure = false;

        LogPermSecurityManager() {
        }

        @Override
        public void checkPermission(java.security.Permission perm) {
            try { //Call super class always for java.security.debug tracing.
                super.checkPermission(perm);
                checkPermission(perm, new SecurityException(perm.toString()));
            } catch (SecurityException se) {
                checkPermission(perm, se);
            }
        }

        @Override
        public void checkPermission(java.security.Permission perm, Object context) {
            try { //Call super class always for java.security.debug tracing.
                super.checkPermission(perm, context);
                checkPermission(perm, new SecurityException(perm.toString()));
            } catch (SecurityException se) {
                checkPermission(perm, se);
            }
        }

        private void checkPermission(java.security.Permission perm, SecurityException se) {
            if (secure && perm instanceof LoggingPermission) {
                throw se;
            }
        }
    }
}
