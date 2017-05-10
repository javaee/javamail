/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2009-2017 Jason Mehrens. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;
import javax.activation.*;
import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test case for the MailHandler spec.
 *
 * @author Jason Mehrens
 */
public class MailHandlerTest extends AbstractLogging {

    /**
     * See LogManager.
     */
    private static final String LOG_CFG_KEY = "java.util.logging.config.file";
    /**
     * Holder used to inject Throwables into other APIs.
     */
    private static final ThreadLocal<Throwable> PENDING = new ThreadLocal<>();
    /**
     * Holder used to inject ClassLoaders into other APIs.
     */
    private static final ThreadLocal<ClassLoader> LOADER = new ThreadLocal<>();
    /**
     * Stores the value of a port that is not used on the local machine.
     */
    private static volatile int OPEN_PORT = Integer.MIN_VALUE;
    /**
     * A host name that can not be resolved.
     */
    private static final String UNKNOWN_HOST = "bad-host-name";
    /**
     * Stores a writable directory that is in the class path and visible to the
     * context class loader.
     */
    private static volatile File anyClassPathDir = null;
    /**
     * Used to prevent G.C. of loggers.
     */
    private volatile Object hardRef;

    @BeforeClass
    public static void setUpClass() throws Exception {
        checkJVMOptions();
        OPEN_PORT = findOpenPort();
        checkUnknownHost();
        assertTrue(findClassPathDir().isDirectory());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        checkJVMOptions();
        assertTrue(checkUnusedPort(OPEN_PORT));
        OPEN_PORT = Integer.MIN_VALUE;
        checkUnknownHost();
        anyClassPathDir = null;
    }

    private static void assumeNoJit() {
        CompilationMXBean c = ManagementFactory.getCompilationMXBean();
        if (c != null) { //-Xint
            Assume.assumeNoException(new IllegalArgumentException(
                    c.getName() + " must be disabled."));
        }
    }

    private static void fullFence() {
        LogManager.getLogManager().getProperty("");
    }

    @Before
    public void setUp() {
        fullFence();
        assertNull(hardRef);
    }

    @After
    public void tearDown() {
        fullFence();
        hardRef = null;
    }

    private static void checkUnknownHost() throws Exception {
        try {
            InetAddress.getByName(UNKNOWN_HOST);
            throw new AssertionError(UNKNOWN_HOST);
        } catch (UnknownHostException expect) {
        }
    }

    private static void checkJVMOptions() throws Exception {
        assertTrue(MailHandlerTest.class.desiredAssertionStatus());
        assertNull(System.getProperty("java.util.logging.manager"));
        assertNull(System.getProperty("java.util.logging.config.class"));
        assertNull(System.getProperty(LOG_CFG_KEY));
        assertEquals(LogManager.class, LogManager.getLogManager().getClass());
        assertTrue(LOW_CAPACITY < NUM_RUNS);
        //Try to hold MAX_CAPACITY array with log records.
        assertTrue((60L * 1024L * 1024L) <= Runtime.getRuntime().maxMemory());
        try {
            if (InetAddress.getLocalHost().getHostName().length() == 0) {
                throw new UnknownHostException();
            }
        } catch (UnknownHostException UHE) {
            throw new AssertionError(UHE);
        }
    }

    private static Throwable getPending() {
        return PENDING.get();
    }

    private static void setPending(final Throwable t) {
        if (t != null) {
            PENDING.set(t);
        } else {
            PENDING.remove();
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

    private static void set(ClassLoader expect) {
        if (expect == null) {
            LOADER.remove();
        } else {
            LOADER.set(expect);
        }
    }

    static void throwPending() {
        Throwable t = PENDING.get();
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

    static boolean isSecurityDebug() {
        boolean debug;
        final String value = System.getProperty("java.security.debug");
        if (value != null) {
            debug = value.contains("all")
                    || value.contains("access")
                    || value.contains("stack");
        } else {
            debug = false;
        }
        return debug;
    }

    static void securityDebugPrint(Throwable se) {
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        final PrintStream err = System.err;
        err.println("Suppressed security exception to allow access:");
        se.printStackTrace(err);
    }

    static void checkContextClassLoader(ClassLoader expect) {
        Object ccl = Thread.currentThread().getContextClassLoader();
        if (expect != ccl) {
            AssertionError ae = new AssertionError(expect + " != " + ccl
                    + ", sm=" + System.getSecurityManager());
            dump(ae);
            throw ae;
        }
    }

    @Test
    public void testChildClassLoader() {
        assertNull(System.getSecurityManager());
        final Thread thread = Thread.currentThread();
        final ClassLoader ccl = thread.getContextClassLoader();
        try {
            URLClassLoader child = new URLClassLoader(new URL[0], ccl);

            thread.setContextClassLoader(child);
            testCallingClassLoader((ClassLoaderSecurityManager) null, child);

            thread.setContextClassLoader(child);
            testCallingClassLoader(new ClassLoaderSecurityManager(), child);
        } finally {
            thread.setContextClassLoader(ccl);
        }
        assertNull(System.getSecurityManager());
    }

    private void testCallingClassLoader(
            ClassLoaderSecurityManager sm, ClassLoader expect) {
        InternalErrorManager em = new ClassLoaderErrorManager(expect);
        try {
            MailHandler instance = new MailHandler(createInitProperties(""));
            try {
                if (sm != null) {
                    System.setSecurityManager(sm);
                    sm.secure = true;
                }
                instance.setErrorManager(em);
                instance.setLevel(Level.ALL);
                instance.setPushLevel(Level.SEVERE);
                instance.setComparator(new ClassLoaderComparator(expect));
                instance.setFilter(new ClassLoaderFilterFormatter(expect));
                instance.setPushFilter(new ClassLoaderFilterFormatter(expect));
                instance.setSubject(new ClassLoaderFilterFormatter(expect));
                instance.setFormatter(new ClassLoaderFilterFormatter(expect));
                instance.setAttachmentFormatters(
                        new ClassLoaderFilterFormatter(expect, "testCCL"));
                instance.setAttachmentFilters(new ClassLoaderFilterFormatter(expect));
                instance.setAttachmentNames(new ClassLoaderFilterFormatter(expect));
                instance.publish(new LogRecord(Level.WARNING, ""));
                instance.publish(new LogRecord(Level.SEVERE, ""));
            } finally {
                instance.close();
            }
        } finally {
            if (sm != null) {
                sm.secure = false;
                System.setSecurityManager((SecurityManager) null);
            }
        }

        assert em != null;
        for (Exception exception : em.exceptions) {
            Throwable t = exception;
            if (t instanceof MessagingException == false) {
                dump(t);
                fail(t.toString());
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testVerifyClassLoader() throws Exception {
        assertNull(System.getSecurityManager());
        final Thread thread = Thread.currentThread();
        final ClassLoader ccl = thread.getContextClassLoader();
        try {
            URLClassLoader child = new URLClassLoader(new URL[0], ccl);

            thread.setContextClassLoader(child);
            testVerify((ClassLoaderSecurityManager) null, child);

            thread.setContextClassLoader(child);
            testVerify(new ClassLoaderSecurityManager(), child);
        } finally {
            thread.setContextClassLoader(ccl);
        }
        assertNull(System.getSecurityManager());
    }

    @Test
    public void testJavaMailLinkage() throws Exception {
        /**
         * The MailHandler has to depend on the official JavaMail spec classes.
         * The logging-mailhandler.jar needs to be portable to other platforms
         * so it doesn't depend on reference implementation classes directly.
         */
        testJavaMailLinkage(MailHandler.class, false);
    }

    @Test
    public void testLogManagerModifiers() throws Exception {
        testLogManagerModifiers(MailHandler.class);
    }

    @Test
    public void testWebappClassLoaderFieldNames() throws Exception {
        testWebappClassLoaderFieldNames(MailHandler.class);
    }

    private void testVerify(ClassLoaderSecurityManager sm, ClassLoader expect) throws Exception {
        final LogManager manager = LogManager.getLogManager();
        InternalErrorManager em = null;
        set(expect);
        try {
            String p = MailHandler.class.getName();
            Properties props = createInitProperties(p);
            props.put(p.concat(".verify"), "local");
            props.put(p.concat(".comparator"), ClassLoaderComparator.class.getName());
            props.put(p.concat(".pushFilter"), ClassLoaderFilterFormatter.class.getName());
            props.put(p.concat(".subject"), ClassLoaderFilterFormatter.class.getName());
            props.put(p.concat(".errorManager"), ClassLoaderErrorManager.class.getName());

            read(manager, props);

            if (sm != null) {
                System.setSecurityManager(sm);
                sm.secure = true;
            }

            MailHandler instance = new MailHandler();
            try {
                em = internalErrorManagerFrom(instance);
            } finally {
                instance.close();
            }
        } finally {
            if (sm != null) {
                sm.secure = false;
                System.setSecurityManager((SecurityManager) null);
            }
            set((ClassLoader) null);
            manager.reset();
        }

        assert em != null;
        for (Exception exception : em.exceptions) {
            Throwable t = exception;
            if (t instanceof MessagingException == false) {
                dump(t);
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testSetMailPropertiesClassLoader() throws Exception {
        assertNull(System.getSecurityManager());
        final Thread thread = Thread.currentThread();
        final ClassLoader ccl = thread.getContextClassLoader();
        try {
            URLClassLoader child = new URLClassLoader(new URL[0], ccl);

            thread.setContextClassLoader(child);
            testSetMailProperties((ClassLoaderSecurityManager) null, child);

            thread.setContextClassLoader(child);
            testSetMailProperties(new ClassLoaderSecurityManager(), child);
        } finally {
            thread.setContextClassLoader(ccl);
        }
        assertNull(System.getSecurityManager());
    }

    private void testSetMailProperties(ClassLoaderSecurityManager sm, ClassLoader expect) throws Exception {
        InternalErrorManager em = new ClassLoaderErrorManager(expect);
        try {
            Properties props = createInitProperties("");
            props.put("verify", "local");

            if (sm != null) {
                System.setSecurityManager(sm);
                sm.secure = true;
            }

            MailHandler instance = new MailHandler();
            try {
                instance.setErrorManager(em);
                instance.setComparator(new ClassLoaderComparator(expect));
                instance.setFilter(new ClassLoaderFilterFormatter(expect));
                instance.setPushFilter(new ClassLoaderFilterFormatter(expect));
                instance.setSubject(new ClassLoaderFilterFormatter(expect));
                instance.setFormatter(new ClassLoaderFilterFormatter(expect));

                instance.setMailProperties(props);
            } finally {
                instance.close();
            }
        } finally {
            if (sm != null) {
                sm.secure = false;
                System.setSecurityManager((SecurityManager) null);
            }
        }

        assert em != null;
        for (Exception exception : em.exceptions) {
            Throwable t = exception;
            if (t instanceof MessagingException == false) {
                dump(t);
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testIsLoggable() {
        final Level[] lvls = getAllLevels();
        if (lvls.length > 0) {
            LogRecord record = new LogRecord(Level.INFO, "");
            for (Level lvl : lvls) {
                testLoggable(lvl, null);
                testLoggable(lvl, record);
            }
        } else {
            fail("No predefined levels.");
        }
    }

    private void testLoggable(Level lvl, LogRecord record) {
        MailHandler instance = new MailHandler(createInitProperties(""));
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
    public void testPostConstruct() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.postConstruct();

        assertEquals(true, em.exceptions.isEmpty());
        instance.close();
    }

    @Test
    public void testPreDestroy() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.preDestroy();

        assertEquals(true, em.exceptions.isEmpty());
        instance.close();

        instance = createHandlerWithRecords();
        em = internalErrorManagerFrom(instance);
        instance.preDestroy();

        assertEquals(1, em.exceptions.size());
        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);
        instance.close();

        //Test for valid message.
        instance = createHandlerWithRecords();
        instance.setErrorManager(new FlushErrorManager(instance));
        instance.preDestroy();
        instance.close();

        //Test that preDestroy is normal priority.
        instance = new MailHandler(2);
        instance.setMailProperties(createInitProperties(""));
        instance.setLevel(Level.ALL);
        instance.setErrorManager(new FlushErrorManager(instance));
        instance.setPushFilter((Filter) null);
        instance.setPushLevel(Level.OFF);
        LogRecord record = new LogRecord(Level.INFO, "");
        instance.publish(record); //should flush.
        instance.preDestroy();
        instance.push(); //Trigger an error if data is present here.
        instance.close();
    }

    @Test
    public void testReflectiveLifeCycleAccess() throws Exception {
        final Class<?> k = MailHandler.class;
        final Method construct = k.getDeclaredMethod("postConstruct");
        final Method destory = k.getDeclaredMethod("preDestroy");
        testLifeCycleProtypes(construct);
        testLifeCycleProtypes(destory);

        MailHandler h = new MailHandler();
        try {
            h.setMailProperties(createInitProperties(""));
            InternalErrorManager em = new InternalErrorManager();
            h.setErrorManager(em);

            construct.invoke(h);
            destory.invoke(h);

            for (Throwable t : em.exceptions) {
                dump(t);
            }
            assertEquals(true, em.exceptions.isEmpty());
        } finally {
            h.close();
        }
    }

    private void testLifeCycleProtypes(Method m) throws Exception {
        final String s = m.toString();
        assertTrue(s, MailHandler.class.equals(m.getDeclaringClass()));
        assertTrue(s, Modifier.isPublic(m.getModifiers()));
        assertFalse(s, Modifier.isStatic(m.getModifiers()));
        assertTrue(s, Void.TYPE.equals(m.getReturnType()));
        assertTrue(s, m.getParameterTypes().length == 0);
        assertTrue(s, m.getExceptionTypes().length == 0);
    }

    @Test
    public void testNoDependencyOnJavaxAnnotations() throws Exception {
        testNoDependencyOnJavaxAnnotations(MailHandler.class);
    }

    @Test
    public void testPreDestroyLinkageError() throws Exception {
        testLinkageErrorWithStack("preDestroy");
    }

    @Test
    public void testPreDestroyLinkageErrorEmpty() throws Exception {
        testLinkageErrorEmptyStack("preDestroy");
    }

    @Test
    public void testPublish() {
        MailHandler instance = createHandlerWithRecords();
        InternalErrorManager em = internalErrorManagerFrom(instance);
        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();

        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);
        assertEquals(1, em.exceptions.size());

        //Test for valid message.
        instance = createHandlerWithRecords();
        instance.setErrorManager(new FlushErrorManager(instance));

        final Level[] lvls = getAllLevels();
        String SOURCE_CLASS = MailHandlerTest.class.getName();
        String SOURCE_METHOD = "testPublish";
        for (Level lvl : lvls) {
            LogRecord r = new LogRecord(lvl, "");
            r.setSourceClassName(SOURCE_CLASS);
            r.setSourceMethodName(SOURCE_METHOD);
            instance.publish(r);
        }

        instance.close();
    }

    @Test
    public void testPublishLinkageError() throws Exception {
        testLinkageErrorWithStack("publish");
    }

    @Test
    public void testPublishLinkageErrorEmpty() throws Exception {
        testLinkageErrorEmptyStack("publish");
    }

    @Test
    public void testPublishDuringClose() {
        final Level[] lvls = getAllLevels();
        for (int levelIndex = 0; levelIndex < lvls.length; levelIndex++) {
            MailHandler instance = new MailHandler(lvls.length + 2);
            InternalErrorManager em = new InternalErrorManager();
            instance.setErrorManager(em);
            Properties props = createInitProperties("");
            instance.setMailProperties(props);

            Authenticator auth = new EmptyAuthenticator();
            Filter filter = BooleanFilter.TRUE;
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

    @Test
    public void testPublishDuringCloseLinkageError() throws Exception {
        testLinkageErrorWithStack("publishDuringClose");
    }

    @Test
    public void testPublishDuringCloseLinkageErrorEmpty() throws Exception {
        testLinkageErrorEmptyStack("publishDuringClose");
    }

    private MailHandler createHandlerWithRecords() {
        final Level[] lvls = getAllLevels();

        MailHandler instance = new MailHandler(lvls.length + 2);
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        Properties props = createInitProperties("");
        instance.setMailProperties(props);
        instance.setLevel(Level.ALL);
        instance.setFilter((Filter) null);
        instance.setPushLevel(Level.OFF);
        instance.setPushFilter((Filter) null);

        final String msg = instance.toString();
        for (Level lvl : lvls) {
            LogRecord r = new LogRecord(lvl, msg);
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
        Properties props = createInitProperties("");
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
    public void testErrorComparator() {
        testErrorComparator(0);
        testErrorComparator(1);
        testErrorComparator(2);
        testErrorComparator(3);
        testErrorComparator(10);
        testErrorComparator(999);
        testErrorComparator(1000);
        testErrorComparator(1001);
    }

    private void testErrorComparator(int records) {
        assertTrue("Invalid argument.", records >= 0);
        Properties props = createInitProperties("");
        MailHandler instance = new MailHandler(props);
        instance.setComparator(new ErrorComparator());
        instance.setErrorManager(new InternalErrorManager());
        boolean normal = false;
        try {
            try {
                for (int i = 0; i < records; ++i) {
                    instance.publish(new LogRecord(Level.SEVERE, ""));
                }
            } finally {
                instance.close();
            }
            normal = true;
        } catch (Error e) {
            if (records == 0 || e.getClass() != Error.class) {
                throw e;
            } else {
                InternalErrorManager em = internalErrorManagerFrom(instance);
                for (Throwable t : em.exceptions) {
                    dump(t);
                }
                assertEquals(true, em.exceptions.isEmpty());
            }
        }

        if (normal) {
            assertTrue(records == 0);
        }
    }

    @Test
    public void testThrowFormatters() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        instance.setLevel(Level.ALL);
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
        MailHandler instance = new MailHandler(createInitProperties(""));
        instance.setLevel(Level.ALL);
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
            instance = new MailHandler(createInitProperties(""));
            instance.setLevel(Level.ALL);
            instance.setFilter(new ErrorFilter());
            boolean result = instance.isLoggable(record);
            assertEquals(expect, result);
        } catch (Error expectEx) {
            if (instance == null) {
                try {
                    instance = new MailHandler(createInitProperties(""));
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

        assert instance != null;
        instance.setFilter((Filter) null);

        Properties props = new Properties();
        props.put("mail.smtp.host", UNKNOWN_HOST);
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
    public void testThrowComparator() {
        testThrowComparator(0);
        testThrowComparator(1);
        testThrowComparator(2);
        testThrowComparator(3);
        testThrowComparator(10);
        testThrowComparator(999);
        testThrowComparator(1000);
        testThrowComparator(1001);
    }

    private void testThrowComparator(int records) {
        assertTrue(records >= 0);
        MailHandler instance = new MailHandler(createInitProperties(""));
        instance.setComparator(new ThrowComparator());
        instance.setErrorManager(new InternalErrorManager());
        try {
            for (int i = 0; i < records; ++i) {
                instance.publish(new LogRecord(Level.SEVERE, ""));
            }
        } finally {
            instance.close();
        }

        InternalErrorManager em = internalErrorManagerFrom(instance);
        boolean seenError = false;
        for (Throwable t : em.exceptions) {
            if (isConnectOrTimeout(t)) {
                continue;
            } else if (t.getClass() == RuntimeException.class) {
                seenError = true;
                continue; //expect.
            } else {
                dump(t);
                fail(t.toString());
            }
        }

        if (records == 0) {
            assertEquals(true, em.exceptions.isEmpty());
        } else {
            assertTrue("Exception was not thrown.", seenError);
            assertEquals(true, !em.exceptions.isEmpty());
        }
    }

    @Test
    public void testThrowFilters() {
        LogRecord record = new LogRecord(Level.INFO, "");
        MemoryHandler mh = new MemoryHandler(new ConsoleHandler(), 100, Level.OFF);
        mh.setFilter(new ThrowFilter());
        MailHandler instance = null;
        try {
            boolean expect = mh.isLoggable(record);
            instance = new MailHandler(createInitProperties(""));
            instance.setLevel(Level.ALL);
            instance.setFilter(new ThrowFilter());
            boolean result = instance.isLoggable(record);
            assertEquals(expect, result);
        } catch (RuntimeException expectEx) {
            if (instance == null) {
                try {
                    instance = new MailHandler(createInitProperties(""));
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

        assert instance != null;
        instance.setFilter((Filter) null);

        Properties props = new Properties();
        props.put("mail.smtp.host", UNKNOWN_HOST);
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

        MessageErrorManager empty = new MessageErrorManager(instance.getMailProperties()) {

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

    private void testAttachmentInvariants(boolean error) throws Exception {
        MailHandler target = new MailHandler(createInitProperties(""));
        try {
            InternalErrorManager em = internalErrorManagerFrom(target);
            if (error) {
                assertFalse(em.exceptions.isEmpty());
                boolean unexpected = false;
                for (Exception e : em.exceptions) {
                    if (e instanceof IndexOutOfBoundsException == false) {
                        dump(e);
                        unexpected = true;
                    }
                }
                assertFalse(unexpected);
            } else {
                for (Exception e : em.exceptions) {
                    dump(e);
                }
                assertTrue(em.exceptions.isEmpty());
            }
            int len = target.getAttachmentFormatters().length;
            assertTrue(String.valueOf(len), len > 0);
            assertEquals(len, target.getAttachmentFilters().length);
            assertEquals(len, target.getAttachmentNames().length);
        } finally {
            target.close();
        }
    }

    @Test
    public void testAlignEmptyFilter() throws Exception {
        String p = MailHandler.class.getName();
        Properties props = createInitProperties(p);
        props.put(p.concat(".attachment.formatters"), SimpleFormatter.class.getName());
        props.put(p.concat(".attachment.names"), "att.txt");
        final LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            testAttachmentInvariants(false);
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testAlignEmptyNames() throws Exception {
        String p = MailHandler.class.getName();
        Properties props = createInitProperties(p);
        props.put(p.concat(".attachment.formatters"), SimpleFormatter.class.getName());
        props.put(p.concat(".attachment.filters"), ErrorFilter.class.getName());
        final LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            testAttachmentInvariants(false);
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testAlignEmptyFilterAndNames() throws Exception {
        String p = MailHandler.class.getName();
        Properties props = createInitProperties(p);
        props.put(p.concat(".attachment.formatters"), SimpleFormatter.class.getName());
        final LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            testAttachmentInvariants(false);
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testAlignErrorFilter() throws Exception {
        String p = MailHandler.class.getName();
        Properties props = createInitProperties(p);
        props.put(p.concat(".attachment.formatters"),
                SimpleFormatter.class.getName() + ", " + SimpleFormatter.class.getName());
        props.put(p.concat(".attachment.filters"), ErrorFilter.class.getName());
        final LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            testAttachmentInvariants(true);
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testAlignErrorNames() throws Exception {
        String p = MailHandler.class.getName();
        Properties props = createInitProperties(p);
        props.put(p.concat(".attachment.formatters"), SimpleFormatter.class.getName());
        props.put(p.concat(".attachment.names"), "att.txt, extra.txt");
        final LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            testAttachmentInvariants(true);
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testAlignErrorFilterAndNames() throws Exception {
        String p = MailHandler.class.getName();
        Properties props = createInitProperties(p);
        props.put(p.concat(".attachment.formatters"),
                SimpleFormatter.class.getName() + ", " + SimpleFormatter.class.getName());
        props.put(p.concat(".attachment.filters"),
                ErrorFilter.class.getName() + "," + ErrorFilter.class.getName()
                + "," + ErrorFilter.class.getName());
        props.put(p.concat(".attachment.names"), "att.txt, next.txt, extra.txt");
        final LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            testAttachmentInvariants(true);
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testEncoding() throws Exception {
        final String enc = "iso8859_1";
        //names are different but equal encodings.
        final String found = MimeUtility.mimeCharset(enc);
        Class<?> k = Session.getInstance(new Properties())
                .getTransport("smtp").getClass();
        if (isPrivateSpec(k)) {
            assertFalse(enc + "==" + found, enc.equals(found));
        }

        LogManager manager = LogManager.getLogManager();
        final MailHandler instance = new MailHandler(createInitProperties(""));
        MessageErrorManager em = new MessageErrorManager(instance.getMailProperties()) {

            @Override
            protected void error(MimeMessage msg, Throwable t, int code) {
                try {
                    MimeMultipart multi = (MimeMultipart) msg.getContent();
                    BodyPart body = multi.getBodyPart(0);
                    assertEquals(Part.INLINE, body.getDisposition());
                    ContentType ct = new ContentType(body.getContentType());
                    assertEquals(MimeUtility.mimeCharset(enc), ct.getParameter("charset"));

                    BodyPart attach = multi.getBodyPart(1);
                    ct = new ContentType(attach.getContentType());
                    assertEquals(MimeUtility.mimeCharset(enc), ct.getParameter("charset"));
                } catch (Throwable E) {
                    dump(E);
                    fail(E.toString());
                }
            }
        };

        instance.setErrorManager(em);
        Properties props = createInitProperties("");
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
        assertEquals(manager.getProperty(p.concat(".encoding")), instance.getEncoding());

        assertTrue(em.exceptions.isEmpty());

        instance.setEncoding(enc);
        assertEquals(enc, instance.getEncoding());
        instance.setSubject("ORA-17043=Ung\u00FCltige maximale Stream-Gr\u00F6\u00DFe");
        LogRecord record = new LogRecord(Level.SEVERE, "Zeit\u00FCberschreitung bei Anweisung");
        instance.publish(record);
        instance.close();
    }

    @Test
    public void testErrorManager() {
        MailHandler h = new MailHandler();
        assertNotNull(h.getErrorManager());
        try {
            h.setErrorManager((ErrorManager) null);
        } catch (NullPointerException expect) {
            assertNotNull(h);
        }
        assertNotNull(h.getErrorManager());

        ErrorManager em = new ErrorManager();
        h.setErrorManager(em);
        assertSame(em, h.getErrorManager());
    }

    @Test
    public void testFormatter() {
        MailHandler h = new MailHandler();
        assertNotNull(h.getFormatter());
        try {
            h.setFormatter((Formatter) null);
        } catch (NullPointerException expect) {
            assertNotNull(h);
        }
        assertNotNull(h.getFormatter());

        SimpleFormatter f = new SimpleFormatter();
        h.setFormatter(f);
        assertSame(f, h.getFormatter());
    }

    @Test
    public void testFilter() {
        MailHandler h = new MailHandler();
        assertNull(h.getFilter());
        h.setFilter((Filter) null);
        assertNull(h.getFilter());

        BooleanFilter f = new BooleanFilter();
        h.setFilter(f);
        assertSame(f, h.getFilter());

        h.setFilter((Filter) null);
        assertNull(h.getFilter());
    }

    @Test
    public void testStatefulFilter() {
        MailHandler h = new MailHandler();
        h.setMailProperties(createInitProperties(""));
        InternalErrorManager em = new FlushErrorManager(h);
        h.setErrorManager(em);
        CountingFilter cf = new CountingFilter();
        h.setFilter(cf);
        int MAX_RECORDS = 100;
        for (int i = 0; i < MAX_RECORDS; i++) {
            LogRecord r = new LogRecord(Level.SEVERE, "");
            h.publish(r);
        }
        h.close();
        assertEquals(MAX_RECORDS, cf.count);
        for (Exception exception : em.exceptions) {
            if (!isConnectOrTimeout(exception)) {
                dump(exception);
                fail(String.valueOf(exception));
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testStatefulAttachmentFilter() {
        MailHandler h = new MailHandler();
        h.setMailProperties(createInitProperties(""));
        InternalErrorManager em = new FlushErrorManager(h);
        h.setErrorManager(em);
        CountingFilter negativeOne = new CountingFilter(BooleanFilter.FALSE);
        h.setFilter(negativeOne);
        h.setAttachmentFormatters(new SimpleFormatter(), new SimpleFormatter(),
                new SimpleFormatter());
        CountingFilter one = new CountingFilter(BooleanFilter.FALSE);
        CountingFilter two = new CountingFilter();
        h.setAttachmentFilters(BooleanFilter.FALSE, one, two);
        int MAX_RECORDS = 100;
        for (int i = 0; i < MAX_RECORDS; i++) {
            LogRecord r = new LogRecord(Level.SEVERE, "");
            h.publish(r);
        }
        h.close();

        assertEquals(MAX_RECORDS, negativeOne.count);
        assertEquals(MAX_RECORDS, one.count);
        assertEquals(MAX_RECORDS, two.count);
        for (Exception exception : em.exceptions) {
            if (!isConnectOrTimeout(exception)) {
                dump(exception);
                fail(String.valueOf(exception));
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testStatefulInternAttachmentFilter() {
        testStatefulAttachmentFilter(false);
    }

    private void testStatefulAttachmentFilter(boolean clear) {
        MailHandler h = new MailHandler();
        h.setMailProperties(createInitProperties(""));
        InternalErrorManager em = new FlushErrorManager(h);
        h.setErrorManager(em);
        CountingFilter cf = new CountingFilter(BooleanFilter.TRUE);
        h.setFilter(cf);
        h.setAttachmentFormatters(new SimpleFormatter(), new SimpleFormatter());
        CountingFilter one = new CountingFilter();
        h.setAttachmentFilters(cf, one);
        int MAX_RECORDS = 100;
        for (int i = 0; i < MAX_RECORDS; i++) {
            LogRecord r = new LogRecord(Level.SEVERE, "");
            h.publish(r);
        }

        if (clear) {
            Filter[] af = h.getAttachmentFilters();
            h.setAttachmentFormatters();
            h.setAttachmentFormatters(new SimpleFormatter(),
                    new SimpleFormatter());
            h.setAttachmentFilters(af);
        }
        h.close();

        assertEquals(MAX_RECORDS, cf.count);
        assertEquals(MAX_RECORDS, one.count);
        for (Exception exception : em.exceptions) {
            if (!isConnectOrTimeout(exception)) {
                dump(exception);
                fail(String.valueOf(exception));
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testStatefulAttachmentFilterClearMatches() {
        testStatefulAttachmentFilter(true);
    }

    @Test
    public void testStatefulPushFilter() {
        MailHandler h = new MailHandler();
        h.setMailProperties(createInitProperties(""));
        InternalErrorManager em = new PushErrorManager(h);
        h.setErrorManager(em);
        CountingFilter cf = new CountingFilter();
        h.setFilter(cf);
        h.setPushLevel(Level.ALL);
        h.setPushFilter(cf);
        LogRecord r = new LogRecord(Level.SEVERE, "");
        h.publish(r);
        h.close();
        assertEquals(1, cf.count);
        for (Exception exception : em.exceptions) {
            if (!isConnectOrTimeout(exception)) {
                dump(exception);
                fail(String.valueOf(exception));
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    private void testStatefulPushAttachmentFilter(boolean clear) {
        final MailHandler h = new MailHandler();
        h.setMailProperties(createInitProperties(""));
        final InternalErrorManager em = new PushErrorManager(h);
        h.setErrorManager(em);
        final CountingFilter cf = new CountingFilter(BooleanFilter.FALSE);
        h.setFilter(cf);
        h.setPushLevel(Level.ALL);
        final CountingFilter push = new CountingFilter();
        h.setPushFilter(push);
        h.setAttachmentFormatters(new SimpleFormatter(), new SimpleFormatter(), new SimpleFormatter());
        final CountingFilter one = new CountingFilter(BooleanFilter.FALSE);
        final CountingFilter two = new CountingFilter(BooleanFilter.FALSE);

        if (clear) {
            h.setAttachmentFilters(one, two,
                    new Filter() {
			@Override
                        public boolean isLoggable(LogRecord record) {
                            h.setAttachmentFormatters(new SimpleFormatter(),
                                    new SimpleFormatter());
                            h.setAttachmentFilters(one, push);
                            return push.isLoggable(record);
                        }

                    });
        } else {
            h.setAttachmentFilters(one, two, push);
        }

        LogRecord r = new LogRecord(Level.SEVERE, "");
        h.publish(r);
        h.close();

        if (clear) {
            assertEquals(2, cf.count);
            assertEquals(2, one.count);
            assertEquals(1, two.count);
            assertEquals(3, push.count);
        } else {
            assertEquals(1, cf.count);
            assertEquals(1, one.count);
            assertEquals(1, two.count);
            assertEquals(1, push.count);
        }
        for (Exception exception : em.exceptions) {
            if (!isConnectOrTimeout(exception)) {
                dump(exception);
                fail(String.valueOf(exception));
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testStatefulPushAttachmentFilter() {
        testStatefulPushAttachmentFilter(false);
    }

    @Test
    public void testStatefulPushFilterClearMatches() {
        testStatefulPushAttachmentFilter(true);
    }

    @Test
    public void testPushInsidePush() {
        final Level[] lvls = getAllLevels();

        MailHandler instance = new MailHandler(lvls.length + 2);
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setMailProperties(createInitProperties(""));
        instance.setLevel(Level.ALL);
        instance.setFilter((Filter) null);
        instance.setPushLevel(Level.OFF);
        instance.setPushFilter((Filter) null);

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
                Comparator<? super LogRecord> c = mh.getComparator();
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
        for (Level lvl : lvls) {
            LogRecord r = new LogRecord(lvl, "");
            r.setSourceClassName(SOURCE_CLASS);
            r.setSourceMethodName(SOURCE_METHOD);
            instance.publish(r);
        }
        instance.flush();

        for (Exception exception : em.exceptions) {
            Throwable t = exception;
            if ((t instanceof MessagingException == false)
                    && (t instanceof IllegalStateException == false)) {
                dump(t);
                fail(String.valueOf(t));
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testPush() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.push();
        assertEquals(true, em.exceptions.isEmpty());
        instance.close();

        instance = createHandlerWithRecords();
        em = internalErrorManagerFrom(instance);
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
        instance.setMailProperties(createInitProperties(""));
        instance.setLevel(Level.ALL);
        instance.setErrorManager(new PushErrorManager(instance));
        instance.setPushFilter((Filter) null);
        instance.setPushLevel(Level.INFO);
        LogRecord record = new LogRecord(Level.SEVERE, "");
        instance.publish(record); //should push.
        instance.close(); //cause a flush if publish didn't push.
    }

    @Test
    public void testFlush() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.flush();

        assertEquals(true, em.exceptions.isEmpty());
        instance.close();

        instance = createHandlerWithRecords();
        em = internalErrorManagerFrom(instance);
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
        instance.setMailProperties(createInitProperties(""));
        instance.setLevel(Level.ALL);
        instance.setErrorManager(new FlushErrorManager(instance));
        instance.setPushFilter((Filter) null);
        instance.setPushLevel(Level.SEVERE);
        LogRecord record = new LogRecord(Level.INFO, "");
        instance.publish(record); //should flush.
        instance.push(); //make FlushErrorManager fail if handler didn't flush.
        instance.close();
    }

    @Test
    public void testFlushLinkageError() throws Exception {
        testLinkageErrorWithStack("flush");
    }

    @Test
    public void testFlushLinkageErrorEmpty() throws Exception {
        testLinkageErrorEmptyStack("flush");
    }

    @Test
    public void testClose() {
        LogRecord record = new LogRecord(Level.INFO, "");
        MailHandler instance = new MailHandler(createInitProperties(""));
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
        em = internalErrorManagerFrom(instance);
        instance.close();

        assertEquals(1, em.exceptions.size());
        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);

        //Test for valid message.
        instance = createHandlerWithRecords();
        instance.setErrorManager(new FlushErrorManager(instance));
        instance.close();
    }

    @Test
    public void testCloseLinkageError() throws Exception {
        testLinkageErrorWithStack("close");
    }

    @Test
    public void testCloseLinkageErrorEmpty() throws Exception {
        testLinkageErrorEmptyStack("close");
    }

    private void testLinkageErrorWithStack(String method) throws IOException {
        PrintStream ls = new LinkageErrorStream();
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        final PrintStream err = System.err;
        try {
            System.setErr(new LinkageErrorStream());
            boolean linkageErrorEscapes = false;
            try {
                ErrorManager em = new ErrorManager();
                em.error(null, null, ErrorManager.GENERIC_FAILURE);
            } catch (LinkageError expect) {
                linkageErrorEscapes = expect.getStackTrace().length != 0;
            }
            Assume.assumeTrue(linkageErrorEscapes);

            System.setErr(ls);
            MailHandler instance = new MailHandler(createInitProperties(""));
            try {
                assertEquals(ErrorManager.class,
                        instance.getErrorManager().getClass());
                instance.publish(new LogRecord(Level.SEVERE, ""));
                if ("preDestroy".equals(method)) {
                    instance.preDestroy();
                } else if ("publish".equals(method)) {
                    instance.setPushLevel(Level.ALL);
                    instance.publish(new LogRecord(Level.SEVERE, ""));
                } else if ("publishDuringClose".equals(method)) {
                    CloseLogRecord r
                            = new CloseLogRecord(Level.SEVERE, "", instance);
                    instance.publish(r);
                } else if ("flush".equals(method)) {
                    instance.flush();
                } else if ("push".equals(method)) {
                    instance.push();
                } else if ("close".equals(method)) {
                    instance.close();
                } else {
                    fail(method);
                }
                assertTrue(ls.checkError());
            } finally {
                instance.close();
            }
            assertTrue(ls.checkError());
        } finally {
            System.setErr(err);
        }
    }

    private void testLinkageErrorEmptyStack(String method) throws IOException {
        PrintStream ls = new LinkageErrorStream(new StackTraceElement[0]);
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        final PrintStream err = System.err;
        final Thread.UncaughtExceptionHandler ueh
                = Thread.currentThread().getUncaughtExceptionHandler();
        try {
            CountingUncaughtExceptionHandler cueh
                    = new CountingUncaughtExceptionHandler();
            System.setErr(new LinkageErrorStream(new StackTraceElement[0]));
            Thread.currentThread().setUncaughtExceptionHandler(cueh);
            boolean linkageErrorEscapes = false;
            try {
                ErrorManager em = new ErrorManager();
                em.error(null, null, ErrorManager.GENERIC_FAILURE);
            } catch (LinkageError expect) {
                linkageErrorEscapes = true;
            }
            Assume.assumeTrue(linkageErrorEscapes);

            System.setErr(ls);
            MailHandler instance = new MailHandler(createInitProperties(""));
            try {
                assertEquals(ErrorManager.class,
                        instance.getErrorManager().getClass());
                instance.publish(new LogRecord(Level.SEVERE, ""));
                if ("preDestroy".equals(method)) {
                    instance.preDestroy();
                } else if ("publish".equals(method)) {
                    instance.setPushLevel(Level.ALL);
                    instance.publish(new LogRecord(Level.SEVERE, ""));
                } else if ("publishDuringClose".equals(method)) {
                    CloseLogRecord r
                            = new CloseLogRecord(Level.SEVERE, "", instance);
                    instance.publish(r);
                } else if ("flush".equals(method)) {
                    instance.flush();
                } else if ("push".equals(method)) {
                    instance.push();
                } else if ("close".equals(method)) {
                    instance.close();
                } else {
                    fail(method);
                }
                assertTrue(ls.checkError());
            } finally {
                instance.close();
            }
            assertTrue(ls.checkError());
            assertEquals(1, cueh.count);
        } finally {
            System.setErr(err);
            Thread.currentThread().setUncaughtExceptionHandler(ueh);
        }
    }

    @Test
    public void testCloseContextClassLoader() {
        assertNull(System.getSecurityManager());
        final Thread thread = Thread.currentThread();
        final ClassLoader ccl = thread.getContextClassLoader();
        try {
            testCloseContextClassLoader0();
        } finally {
            thread.setContextClassLoader(ccl);
        }
        assertNull(System.getSecurityManager());
    }

    private void testCloseContextClassLoader0() {

        InternalErrorManager em = new ActivationErrorManager();
        MailHandler instance = createHandlerWithRecords();
        try {
            instance.setErrorManager(em);
            ClassLoader expect = instance.getClass().getClassLoader();
            assertNotNull(expect);
            instance.setAuthenticator(new ClassLoaderAuthenticator(expect));

            /**
             * java.util.logging.LogManager$Cleaner has a null CCL.
             */
            Thread.currentThread().setContextClassLoader(null);
        } finally {
            instance.close();
        }

        for (Exception exception : em.exceptions) {
            Throwable t = exception;
            if (!isConnectOrTimeout(t)) {
                dump(t);
                fail(t.toString());
            }
        }
    }

    @Test
    public void testLevel() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getLevel());
        try {
            instance.setLevel((Level) null);
            fail("Null level was allowed");
        } catch (NullPointerException pass) {
            assertNotNull(instance);
        } catch (RuntimeException re) {
            fail(re.toString());
        }
        assertNotNull(instance.getLevel());

        final Level[] lvls = getAllLevels();
        for (Level lvl : lvls) {
            instance.setLevel(lvl);
            assertEquals(instance.getLevel(), lvl);
        }

        instance.setLevel(Level.WARNING);
        instance.close();
        assertEquals(Level.OFF, instance.getLevel());
        for (Level lvl : lvls) {
            instance.setLevel(lvl);
            assertEquals(Level.OFF, instance.getLevel());
        }
        assertEquals(true, em.exceptions.isEmpty());
    }

    @Test
    public void testLevelBeforeClose() {
        MailHandler instance = this.createHandlerWithRecords();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        final Level expect = Level.WARNING;
        instance.setLevel(expect);

        instance.setFormatter(new LevelCheckingFormatter(expect));
        instance.close();

        for (Exception exception : em.exceptions) {
            Throwable t = exception;
            if (t instanceof MessagingException) {
                if (!isConnectOrTimeout(t)) {
                    dump(t);
                    fail(t.toString());
                }
            } else {
                dump(t);
                fail(t.toString());
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testLevelAfterClose() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        instance.setLevel(Level.WARNING);
        instance.setFormatter(new LevelCheckingFormatter(Level.OFF));
        instance.publish(new CloseLogRecord(Level.SEVERE, "", instance));
        assertEquals(Level.OFF, instance.getLevel());

        instance.close();
        for (Exception exception : em.exceptions) {
            Throwable t = exception;
            if (t instanceof MessagingException) {
                if (!isConnectOrTimeout(t)) {
                    dump(t);
                    fail(t.toString());
                }
            } else {
                dump(t);
                fail(t.toString());
            }
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testLogManagerReset() throws IOException {
        LogManager manager = LogManager.getLogManager();
        try {
            assertEquals(LogManager.class, manager.getClass());
            MailHandler instance = startLogManagerReset("remote");
            InternalErrorManager em = internalErrorManagerFrom(instance);

            manager.reset();

            for (Exception exception : em.exceptions) {
                Throwable t = exception;
                if (t instanceof MessagingException) {
                    if (isNoRecipientAddress(t)) {
                        continue;
                    }
                    if (!isConnectOrTimeout(t)) {
                        dump(t);
                        fail(t.toString());
                    }
                } else {
                    dump(t);
                    fail(t.toString());
                }
            }

            instance = startLogManagerReset("local");
            em = internalErrorManagerFrom(instance);

            for (Exception exception : em.exceptions) {
                Throwable t = exception;
                if (t instanceof MessagingException) {
                    if (isNoRecipientAddress(t)) {
                        continue;
                    }
                    if (!isConnectOrTimeout(t)) {
                        dump(t);
                        fail(t.toString());
                    }
                } else {
                    dump(t);
                    fail(t.toString());
                }
            }

            manager.reset();

            for (Exception exception : em.exceptions) {
                Throwable t = exception;
                if (t instanceof MessagingException) {
                    if (isNoRecipientAddress(t)) {
                        continue;
                    }
                    if (!isConnectOrTimeout(t)) {
                        dump(t);
                        fail(t.toString());
                    }
                } else {
                    dump(t);
                    fail(t.toString());
                }
            }

            String[] noVerify = new String[]{null, "", "null"};
            for (int v = 0; v < noVerify.length; v++) {
                instance = startLogManagerReset(noVerify[v]);
                em = internalErrorManagerFrom(instance);

                for (Exception exception : em.exceptions) {
                    Throwable t = exception;
                    dump(t);
                    fail("Verify index=" + v);
                }

                manager.reset();

                //No verify results in failed send.
                for (Exception exception : em.exceptions) {
                    Throwable t = exception;
                    if (t instanceof MessagingException) {
                        if (isNoRecipientAddress(t)) {
                            continue;
                        }
                        if (!isConnectOrTimeout(t)) {
                            dump(t);
                            fail("Verify index=" + v);
                        }
                    } else {
                        dump(t);
                        fail("Verify index=" + v);
                    }
                }
            }

            instance = startLogManagerReset("bad-enum-name");
            em = internalErrorManagerFrom(instance);

            manager.reset();

            //Allow the LogManagerProperties to copy on a bad enum type.
            boolean foundIllegalArg = false;
            for (Exception exception : em.exceptions) {
                Throwable t = exception;
                if (t instanceof IllegalArgumentException) {
                    foundIllegalArg = true;
                } else if (t instanceof RuntimeException) {
                    dump(t);
                    fail(t.toString());
                }
            }

            assertTrue(foundIllegalArg);
            assertFalse(em.exceptions.isEmpty());
        } finally {
            hardRef = null;
            manager.reset();
        }
    }

    /**
     * Setup and load the standard properties.
     *
     * @param verify the value of verify enum.
     * @return a MailHandler
     * @throws IOException if there is a problem.
     */
    private MailHandler startLogManagerReset(String verify) throws IOException {
        LogManager manager = LogManager.getLogManager();
        manager.reset();

        final String p = MailHandler.class.getName();
        Properties props = createInitProperties(p);
        props.put(p.concat(".mail.from"), "localhost@localdomain");
        props.put(p.concat(".mail.to"), "");
        props.put(p.concat(".mail.cc"), "");
        props.put(p.concat(".mail.bcc"), "");
        props.put(p.concat(".subject"), p.concat(" test"));
        props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
        if (verify != null) {
            props.put(p.concat(".verify"), verify);
        }
        read(manager, props);

        assertNotNull(manager.getProperty(p.concat(".mail.host")));
        assertNotNull(manager.getProperty(p.concat(".mail.smtp.host")));
        assertNotNull(manager.getProperty(p.concat(".mail.smtp.port")));
        assertNotNull(manager.getProperty(p.concat(".mail.to")));
        assertNotNull(manager.getProperty(p.concat(".mail.cc")));
        assertNotNull(manager.getProperty(p.concat(".subject")));
        assertNotNull(manager.getProperty(p.concat(".mail.from")));
        assertEquals(verify, manager.getProperty(p.concat(".verify")));
        assertNotNull(manager.getProperty(p.concat(".mail.smtp.connectiontimeout")));
        assertNotNull(manager.getProperty(p.concat(".mail.smtp.timeout")));

        MailHandler instance = new MailHandler(10);
        instance.setLevel(Level.ALL);

        //Don't auto compute a default recipient.
        Properties bug7092981 = createInitProperties("");
        bug7092981.setProperty("mail.to", "");
        bug7092981.setProperty("mail.cc", "");
        bug7092981.setProperty("mail.bcc", "");
        instance.setMailProperties(bug7092981);

        assertEquals(InternalErrorManager.class, instance.getErrorManager().getClass());

        final String CLASS_NAME = MailHandlerTest.class.getName();
        Logger logger = Logger.getLogger(CLASS_NAME);
        hardRef = logger;
        logger.setUseParentHandlers(false);
        logger.addHandler(instance);

        logger.log(Level.SEVERE, "Verify is {0}.", verify);
        logger.log(Level.SEVERE, "Verify is {0}.", verify);
        return instance;
    }

    @Test
    public void testPushLevel() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getPushLevel());

        try {
            instance.setPushLevel((Level) null);
            fail("Null level was allowed");
        } catch (NullPointerException pass) {
        } catch (RuntimeException re) {
            fail(re.toString());
        }

        final Level[] lvls = getAllLevels();
        for (Level lvl : lvls) {
            instance.setPushLevel(lvl);
            assertEquals(instance.getPushLevel(), lvl);
        }

        instance.close();
        for (Level lvl : lvls) {
            instance.setPushLevel(lvl);
            assertEquals(instance.getPushLevel(), lvl);
        }
        assertEquals(true, em.exceptions.isEmpty());
    }

    @Test
    public void testPushLinkageError() throws Exception {
        testLinkageErrorWithStack("push");
    }

    @Test
    public void testPushLinkageErrorEmpty() throws Exception {
        testLinkageErrorEmptyStack("push");
    }

    @Test
    public void testPushFilter() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        try {
            instance.setPushFilter((Filter) null);
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
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setEncoding((String) null);
        String head = instance.contentTypeOf(new XMLFormatter().getHead(instance));
        assertEquals("application/xml", head);
        instance.setEncoding("US-ASCII");

        head = instance.contentTypeOf(new XMLFormatter().getHead(instance));
        assertEquals("application/xml", head);

        instance.setEncoding((String) null);
        head = instance.contentTypeOf(new SimpleFormatter().getHead(instance));
        assertNull(head);

        instance.setEncoding("US-ASCII");
        head = instance.contentTypeOf(new SimpleFormatter().getHead(instance));
        assertNull(head);

        instance.setEncoding((String) null);
        head = instance.contentTypeOf(new HeadFormatter("<HTML><BODY>").getHead(instance));
        assertEquals("text/html", head);

        instance.setEncoding((String) null);
        head = instance.contentTypeOf(new HeadFormatter("<html><body>").getHead(instance));
        assertEquals("text/html", head);

        instance.setEncoding("US-ASCII");
        head = instance.contentTypeOf(new HeadFormatter("<HTML><BODY>").getHead(instance));
        assertEquals("text/html", head);

        instance.setEncoding("US-ASCII");
        head = instance.contentTypeOf(new HeadFormatter("<HTML><HEAD></HEAD>"
                + "<BODY></BODY></HTML>").getHead(instance));
        assertEquals("text/html", head);

        instance.setEncoding((String) null);
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

        for (Exception exception : em.exceptions) {
            fail(exception.toString());
        }
    }

    @Test
    public void testContentTypeOfFormatter() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        synchronized (instance) {
            assertNull(instance.contentTypeOf(new SimpleFormatter()));
            assertNull(instance.contentTypeOf(new SimpleFormatter(){}));

            assertEquals("application/xml", instance.contentTypeOf(new XMLFormatter()));
            assertEquals("application/xml", instance.contentTypeOf(new XMLFormatter(){}));
        }

        /**
         * None of the Formatter methods that can generate content should be
         * invoked during a verify as that could lead to poor startup times.
         */
        class UnsupportedHTML extends Formatter {

            @Override
            public String getHead(Handler h) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String format(LogRecord record) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getTail(Handler h) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                throw new UnsupportedOperationException();
            }
        }

        synchronized (instance) {
            assertEquals("text/html", instance.contentTypeOf(new UnsupportedHTML()));
            assertEquals("text/html", instance.contentTypeOf(new UnsupportedHTML(){}));
        }

        instance.close();
        for (Exception exception : em.exceptions) {
            fail(exception.toString());
        }
    }

    @Test
    public void testGuessContentTypeReadlimit() throws Exception {
        class LastMarkInputStream extends ByteArrayInputStream {

            private int lastReadLimit;

            LastMarkInputStream() {
                super(new byte[1024]);
            }

            @Override
            public synchronized void mark(int readlimit) {
                this.lastReadLimit = readlimit;
                super.mark(readlimit);
            }

            public synchronized int getLastReadLimit() {
                return lastReadLimit;
            }
        }

        LastMarkInputStream in = new LastMarkInputStream();
        URLConnection.guessContentTypeFromStream(in);

        //See MAX_CHARS in MailHandler.contentTypeOf
        final int lastLimit = in.getLastReadLimit();
        assertTrue(String.valueOf(lastLimit), 25 >= lastLimit);
    }

    @Test
    public void testContentTypeNestedFormatter() throws Exception {
        String expected = "application/xml; charset=us-ascii";
        String type = getInlineContentType(new CollectorFormatter("{0}{1}{2}",
                new XMLFormatter(), new SeverityComparator()));
        assertEquals(expected, type);


        expected = "text/plain; charset=us-ascii";
        type = getInlineContentType(new CollectorFormatter("{0}{1}{2}",
                new CompactFormatter(), new SeverityComparator()));
        assertEquals(expected, type);
    }

    @Test
    public void testContentTypeNestedMultiFormatter() throws Exception {
        String expected = "application/xml; charset=us-ascii";
        String type = getInlineMultiContentType(new CollectorFormatter("{0}{1}{2}",
                new XMLFormatter(), new SeverityComparator()));
        assertEquals(expected, type);


        expected = "text/plain; charset=us-ascii";
        type = getInlineMultiContentType(new CollectorFormatter("{0}{1}{2}",
                new CompactFormatter(), new SeverityComparator()));
        assertEquals(expected, type);
    }

    @Test
    public void testContentTypeMultiOverride() throws Exception {
        String expected = "application/xml; charset=us-ascii";
        String type = getInlineMultiContentType(new XMLFormatter());
        assertEquals(expected, type);

        MimetypesFileTypeMap m = new MimetypesFileTypeMap();
        m.addMimeTypes("text/plain txt TXT XMLFormatter");
        final FileTypeMap old = FileTypeMap.getDefaultFileTypeMap();
        FileTypeMap.setDefaultFileTypeMap(m);
        try {
            type = getInlineMultiContentType(new XMLFormatter());
            assertEquals("text/plain; charset=us-ascii", type);
        } finally {
            FileTypeMap.setDefaultFileTypeMap(old);
        }

        type = getInlineMultiContentType(new XMLFormatter());
        assertEquals(expected, type);
    }

    private String getInlineMultiContentType(Formatter f) throws Exception {
        final String[] value = new String[1];
        MailHandler instance = new MailHandler(createInitProperties(""));
        instance.setAttachmentFormatters(new SimpleFormatter());
        instance.setAttachmentFilters(BooleanFilter.FALSE);
        instance.setEncoding("us-ascii");

        MessageErrorManager em = new MessageErrorManager(instance.getMailProperties()) {

            @Override
            protected void error(MimeMessage msg, Throwable t, int code) {
                try {
                    MimeMultipart multi = (MimeMultipart) msg.getContent();
                    BodyPart body = multi.getBodyPart(0);
                    assertEquals(Part.INLINE, body.getDisposition());
                    String desc = msg.getDescription();
                    assertTrue(desc.contains("Sorted using"));
                    assertTrue(desc.contains("pushed when"));
                    value[0] = body.getContentType();
                } catch (Throwable E) {
                    dump(E);
                    fail(E.toString());
                }
            }
        };
        instance.setErrorManager(em);
        Properties props = createInitProperties("");
        props.put("mail.to", "localhost@localdomain");
        instance.setMailProperties(props);
        instance.setFormatter(f);
        instance.publish(new LogRecord(Level.SEVERE, "test"));
        instance.close();

        return value[0];
    }

    @Test
    public void testContentTypeOverride() throws Exception {
        String expected = "application/xml; charset=us-ascii";
        String type = getInlineContentType(new XMLFormatter());
        assertEquals(expected, type);

        MimetypesFileTypeMap m = new MimetypesFileTypeMap();
        m.addMimeTypes("text/plain txt TXT XMLFormatter");
        final FileTypeMap old = FileTypeMap.getDefaultFileTypeMap();
        FileTypeMap.setDefaultFileTypeMap(m);
        try {
            type = getInlineContentType(new XMLFormatter());
            assertEquals("text/plain; charset=us-ascii", type);
        } finally {
            FileTypeMap.setDefaultFileTypeMap(old);
        }

        type = getInlineContentType(new XMLFormatter());
        assertEquals(expected, type);
    }

    private ErrorManager getSuperErrorManager(MailHandler h) throws Exception {
        Method hem = MailHandler.class.getDeclaredMethod("defaultErrorManager");
        hem.setAccessible(true);
        return (ErrorManager) hem.invoke(h);
    }

    private String getInlineContentType(Formatter f) throws Exception {
        final String[] value = new String[1];
        MailHandler instance = new MailHandler(createInitProperties(""));
        instance.setEncoding("us-ascii");
        MessageErrorManager em = new MessageErrorManager(instance.getMailProperties()) {

            @Override
            protected void error(MimeMessage msg, Throwable t, int code) {
                try {
                    String desc = msg.getDescription();
                    assertTrue(desc.contains("filtered with"));
                    assertTrue(desc.contains("named by"));
                    value[0] = msg.getContentType();
                } catch (Throwable E) {
                    dump(E);
                    fail(E.toString());
                }
            }
        };
        instance.setErrorManager(em);
        Properties props = createInitProperties("");
        props.put("mail.to", "localhost@localdomain");
        instance.setMailProperties(props);
        instance.setFormatter(f);
        instance.publish(new LogRecord(Level.SEVERE, "test"));
        instance.close();

        return value[0];
    }

    @Test
    public void testAcceptLang() throws Exception {
        class LangManager extends MessageErrorManager {

            LangManager(final Properties props) {
                super(props);
            }

            @Override
            protected void error(MimeMessage msg, Throwable t, int code) {
                try {
                    final Locale locale = Locale.getDefault();
                    String lang = LogManagerProperties.toLanguageTag(locale);
                    if (lang.length() != 0) {
                        assertEquals(lang, msg.getHeader("Accept-Language", null));
                    } else {
                        assertEquals("", locale.getLanguage());
                    }

                    MimeMultipart mp = (MimeMultipart) msg.getContent();
                    assertTrue(mp.getCount() > 0);
                    for (int i = 0; i < mp.getCount(); i++) {
                        MimePart part = (MimePart) mp.getBodyPart(i);
                        if (lang.length() != 0) {
                            assertEquals(lang, part.getHeader("Accept-Language", null));
                        } else {
                            assertEquals("", locale.getLanguage());
                        }
                    }
                } catch (RuntimeException re) {
                    dump(re);
                    throw new AssertionError(re);
                } catch (Exception ex) {
                    dump(ex);
                    throw new AssertionError(ex);
                }
            }
        }

        Formatter[] formatters = new Formatter[]{new SimpleFormatter(), new SimpleFormatter()};
        InternalErrorManager em;
        MailHandler target;
        Locale locale = Locale.getDefault();
        try {
            target = new MailHandler(createInitProperties(""));
            try {
                em = new LangManager(target.getMailProperties());
                target.setErrorManager(em);
                target.setAttachmentFormatters(formatters);

                Locale.setDefault(new Locale("", "", ""));
                target.publish(new LogRecord(Level.SEVERE, ""));
                target.flush();

                Locale.setDefault(Locale.ENGLISH);
                target.publish(new LogRecord(Level.SEVERE, ""));
                target.flush();

                Locale.setDefault(Locale.GERMAN);
                target.publish(new LogRecord(Level.SEVERE, ""));
                target.flush();

                Locale.setDefault(Locale.FRANCE);
                target.publish(new LogRecord(Level.SEVERE, ""));
                target.flush();
            } finally {
                target.close();
            }
        } finally {
            Locale.setDefault(locale);
        }
    }

    @Test
    public void testContentLangBase() throws Exception {

        class Base extends MessageErrorManager {

            private final String bundleName;

            Base(Properties props, final String bundleName) {
                super(props);
                this.bundleName = bundleName;
            }

            @Override
            protected void error(MimeMessage msg, Throwable t, int code) {
                try {
                    assertNotNull(bundleName);
                    MimeMultipart mp = (MimeMultipart) msg.getContent();
                    Locale l = Locale.getDefault();
                    assertEquals(LogManagerProperties.toLanguageTag(l), msg.getHeader("Accept-Language", null));
                    String lang[] = msg.getContentLanguage();
                    assertNotNull(lang);
                    assertEquals(LogManagerProperties.toLanguageTag(l), lang[0]);
                    assertEquals(1, mp.getCount());
                    MimePart part;

                    part = (MimePart) mp.getBodyPart(0);
                    lang = part.getContentLanguage();
                    assertNotNull(lang);
                    assertEquals(LogManagerProperties.toLanguageTag(l), lang[0]);
                    assertEquals(LogManagerProperties.toLanguageTag(l), part.getHeader("Accept-Language", null));
                } catch (RuntimeException re) {
                    dump(re);
                    throw new AssertionError(re);
                } catch (Exception ex) {
                    dump(ex);
                    throw new AssertionError(ex);
                }
            }
        }

        MailHandler target = new MailHandler(createInitProperties(""));
        target.setAttachmentFormatters(new SimpleFormatter());
        target.setAttachmentFilters(BooleanFilter.FALSE);

        Properties props = new Properties();
        props.put("motd", "Hello MailHandler!");
        final String p = MailHandler.class.getName();
        final Locale l = Locale.getDefault();
        final String name = MailHandler.class.getSimpleName().concat("base");
        final File f = File.createTempFile(name, ".properties", findClassPathDir());
        Locale.setDefault(Locale.US);
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) {
                props.store(fos, "No Comment");
            }

            String bundleName = f.getName().substring(0, f.getName().lastIndexOf('.'));
            target.setErrorManager(new Base(target.getMailProperties(), bundleName));
            final Logger log = Logger.getLogger(p + '.' + f.getName(), bundleName);
            hardRef = log;
            try {
                assertNotNull(log.getResourceBundle());
                assertNotNull(log.getResourceBundleName());

                log.addHandler(target);
                try {
                    log.setUseParentHandlers(false);
                    log.log(Level.SEVERE, "motd");
                } finally {
                    log.removeHandler(target);
                }
            } finally {
                hardRef = null;
            }

            target.close();

            InternalErrorManager em = internalErrorManagerFrom(target);
            for (Exception t : em.exceptions) {
                if (isConnectOrTimeout(t)) {
                    continue;
                }
                dump(t);
                fail(t.toString());
            }
            assertFalse(em.exceptions.isEmpty());
        } finally {
            Locale.setDefault(l);
            if (!f.delete() && f.exists()) {
                f.deleteOnExit();
            }
        }
    }

    @Test
    public void testContentLangInfer() throws Exception {

        class Infer extends MessageErrorManager {

            private final Locale expect;

            Infer(Properties props, final Locale expect) {
                super(props);
                this.expect = expect;
            }

            @Override
            protected void error(MimeMessage msg, Throwable t, int code) {
                try {
                    MimeMultipart mp = (MimeMultipart) msg.getContent();
                    Locale l = Locale.getDefault();
                    assertFalse(l.getCountry().equals(expect.getCountry()));

                    assertEquals(LogManagerProperties.toLanguageTag(l), msg.getHeader("Accept-Language", null));
                    String lang[] = msg.getContentLanguage();
                    assertEquals(1, lang.length);
                    assertEquals(LogManagerProperties.toLanguageTag(expect), lang[0]);
                    assertEquals(1, mp.getCount());
                    MimePart part;

                    part = (MimePart) mp.getBodyPart(0);
                    lang = part.getContentLanguage();
                    assertEquals(1, lang.length);
                    assertEquals(LogManagerProperties.toLanguageTag(expect), lang[0]);
                    assertEquals(LogManagerProperties.toLanguageTag(l), part.getHeader("Accept-Language", null));
                } catch (RuntimeException re) {
                    dump(re);
                    throw new AssertionError(re);
                } catch (Exception ex) {
                    dump(ex);
                    throw new AssertionError(ex);
                }
            }
        }

        MailHandler target;
        Locale cl;
        String logPrefix;
        Properties props = new Properties();
        props.put("motd", "Hello MailHandler!");
        final String p = MailHandler.class.getName();
        final Locale l = Locale.getDefault();
        final String name = MailHandler.class.getSimpleName().concat("infer");
        final File f = File.createTempFile(name, "_"
                + Locale.ENGLISH.getLanguage() + ".properties", findClassPathDir());
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) {
                props.store(fos, "No Comment");
            }

            String bundleName = f.getName().substring(0, f.getName().lastIndexOf('_'));
            assertTrue(!bundleName.contains(Locale.ENGLISH.getLanguage()));

            cl = Locale.US;
            target = new MailHandler(createInitProperties(""));
            target.setAttachmentFormatters(new SimpleFormatter());
            target.setAttachmentFilters(BooleanFilter.FALSE);

            target.setErrorManager(new Infer(target.getMailProperties(), Locale.ENGLISH));
            logPrefix = p + '.' + f.getName() + cl;
            testContentLangInfer(target, logPrefix, bundleName, cl);

            cl = Locale.UK;
            target = new MailHandler(createInitProperties(""));
            target.setAttachmentFormatters(new SimpleFormatter());
            target.setAttachmentFilters(BooleanFilter.FALSE);
            target.setErrorManager(new Infer(target.getMailProperties(), Locale.ENGLISH));
            logPrefix = p + '.' + f.getName() + cl;
            testContentLangInfer(target, logPrefix, bundleName, cl);
        } finally {
            Locale.setDefault(l);
            if (!f.delete() && f.exists()) {
                f.deleteOnExit();
            }
        }
    }

    private void testContentLangInfer(MailHandler target, String logPrefix, String bundleName, Locale cl) {
        Locale.setDefault(cl);
        Logger log = Logger.getLogger(logPrefix + cl, bundleName);
        hardRef = log;
        try {
            assertNotNull(log.getResourceBundle());
            assertNotNull(log.getResourceBundleName());

            log.addHandler(target);
            try {
                log.setUseParentHandlers(false);
                log.log(Level.SEVERE, "motd");
            } finally {
                log.removeHandler(target);
            }
        } finally {
            hardRef = null;
        }

        target.close();

        InternalErrorManager em = internalErrorManagerFrom(target);
        for (Exception t : em.exceptions) {
            if (isConnectOrTimeout(t)) {
                continue;
            }
            dump(t);
            fail(t.toString());
        }
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testContentLangExact() throws Exception {
        MailHandler target = new MailHandler(createInitProperties(""));
        target.setErrorManager(new MessageErrorManager(target.getMailProperties()) {

            @Override
            protected void error(MimeMessage msg, Throwable t, int code) {
                try {
                    MimeMultipart mp = (MimeMultipart) msg.getContent();
                    Locale l = Locale.getDefault();
                    assertEquals(LogManagerProperties.toLanguageTag(l), msg.getHeader("Accept-Language", null));
                    String lang[] = msg.getContentLanguage();
                    assertEquals(LogManagerProperties.toLanguageTag(Locale.ENGLISH), lang[0]);
                    assertEquals(LogManagerProperties.toLanguageTag(Locale.GERMAN), lang[1]);
                    assertEquals(LogManagerProperties.toLanguageTag(Locale.FRANCE), lang[2]);
                    assertEquals(4, mp.getCount());
                    MimePart part;

                    part = (MimePart) mp.getBodyPart(0);
                    assertEquals(LogManagerProperties.toLanguageTag(l), part.getHeader("Accept-Language", null));
                    assertNull(part.getHeader("Content-Language", ","));

                    part = (MimePart) mp.getBodyPart(1);
                    assertEquals(LogManagerProperties.toLanguageTag(l), part.getHeader("Accept-Language", null));
                    assertEquals(LogManagerProperties.toLanguageTag(Locale.ENGLISH), part.getHeader("Content-Language", ","));

                    part = (MimePart) mp.getBodyPart(2);
                    assertEquals(LogManagerProperties.toLanguageTag(l), part.getHeader("Accept-Language", null));
                    assertEquals(LogManagerProperties.toLanguageTag(Locale.GERMAN), part.getHeader("Content-Language", ","));

                    part = (MimePart) mp.getBodyPart(3);
                    assertEquals(LogManagerProperties.toLanguageTag(l), part.getHeader("Accept-Language", null));
                    assertEquals(LogManagerProperties.toLanguageTag(Locale.FRANCE), part.getHeader("Content-Language", ","));
                } catch (RuntimeException re) {
                    dump(re);
                    throw new AssertionError(re);
                } catch (Exception ex) {
                    dump(ex);
                    throw new AssertionError(ex);
                }
            }
        });

        target.setLevel(Level.ALL);
        target.setFilter(new LocaleFilter(Locale.JAPANESE, true));
        target.setPushLevel(Level.OFF);
        target.setAttachmentFormatters(new Formatter[]{
            new SimpleFormatter(), new SimpleFormatter(), new SimpleFormatter()});
        target.setAttachmentFilters(new Filter[]{
            new LocaleFilter(Locale.ENGLISH, false),
            new LocaleFilter(Locale.GERMAN, false),
            new LocaleFilter(Locale.FRANCE, false)}); //just the language.

        assertEquals(3, target.getAttachmentFormatters().length);
        assertEquals(3, target.getAttachmentFilters().length);

        final List<File> files = new ArrayList<>();
        final Properties props = new Properties();
        final Locale current = Locale.getDefault();
        try {
            File f;
            Locale.setDefault(new Locale("", "", ""));
            f = testContentLangExact(target, props, "_");
            files.add(f);

            props.put("motd", "Hello MailHandler!");
            Locale.setDefault(Locale.ENGLISH);
            f = testContentLangExact(target, props, "_");
            files.add(f);

            props.put("motd", "Hallo MailHandler!");
            Locale.setDefault(Locale.GERMAN);
            f = testContentLangExact(target, props, "_");
            files.add(f);

            props.put("motd", "Bonjour MailHandler!");
            Locale.setDefault(Locale.FRANCE); //just the language.
            f = testContentLangExact(target, props, "_");
            files.add(f);

            Locale.setDefault(new Locale("", "", ""));
            f = testContentLangExact(target, props, "_");
            files.add(f);

            Locale.setDefault(new Locale("", "", ""));
            f = testContentLangExact(target, props, ".");
            files.add(f);

            props.put("motd", "Hello MailHandler!");
            Locale.setDefault(Locale.ENGLISH);
            f = testContentLangExact(target, props, ".");
            files.add(f);

            props.put("motd", "Hallo MailHandler!");
            Locale.setDefault(Locale.GERMAN);
            f = testContentLangExact(target, props, ".");
            files.add(f);

            props.put("motd", "Bonjour MailHandler!");
            Locale.setDefault(Locale.FRANCE); //just the language.
            f = testContentLangExact(target, props, ".");
            files.add(f);

            Locale.setDefault(new Locale("", "", ""));
            f = testContentLangExact(target, props, ".");
            files.add(f);
        } finally {
            Locale.setDefault(current);
            for (File f : files) {
                if (!f.delete() && f.exists()) {
                    f.deleteOnExit();
                }
            }
        }

        target.close();

        InternalErrorManager em = internalErrorManagerFrom(target);
        for (Exception t : em.exceptions) {
            if (isConnectOrTimeout(t)) {
                continue;
            }
            dump(t);
            fail(t.toString());
        }
        assertFalse(em.exceptions.isEmpty());
    }

    private File testContentLangExact(MailHandler target, Properties props, String exact) throws Exception {
        final String p = MailHandler.class.getName();
        final Locale l = Locale.getDefault();
        boolean fail = true;
        final String name = MailHandler.class.getSimpleName().concat("motd");
        assertTrue(name, name.indexOf(exact) < 1);

        String prefix;
        if (l.getLanguage().length() != 0) {
            prefix = "_" + l;
        } else {
            prefix = "";
        }
        final File f = File.createTempFile(name, prefix + ".properties", findClassPathDir());
        try {
            try (FileOutputStream fos = new FileOutputStream(f)) {
                props.store(fos, "No Comment");
            }

            Logger log;
            if (l.getLanguage().length() == 0) {
                log = Logger.getLogger(p + '.' + f.getName());
                assertNull(log.getResourceBundle());
            } else {
                final String loggerName = p + '.' + f.getName() + '.' + l;
                if (".".equals(exact)) {
                    log = Logger.getLogger(loggerName,
                            f.getName().substring(0, f.getName().lastIndexOf(exact)));
                } else if ("_".equals(exact)) {
                    log = Logger.getLogger(loggerName,
                            f.getName().substring(0, f.getName().indexOf(exact)));
                } else {
                    throw new IllegalArgumentException(exact);
                }
                assertNotNull(log.getResourceBundle());
                assertNotNull(log.getResourceBundleName());
            }

            hardRef = log;
            try {
                log.setUseParentHandlers(false);
                try {
                    log.addHandler(target);
                    log.log(Level.INFO, "motd");
                    fail = false;
                } finally {
                    log.removeHandler(target);
                }
            } finally {
                hardRef = null;
            }
        } finally {
            if (fail) {
                if (!f.delete() && f.exists()) {
                    f.deleteOnExit();
                }
            }
        }
        return f;
    }

    /**
     * Find a writable directory that is in the class path.
     *
     * @return a File directory.
     * @throws IOException if there is a problem.
     * @throws FileNotFoundException if there are no directories in class path.
     */
    @SuppressWarnings("ThrowableInitCause")
    private static File findClassPathDir() throws IOException {
        File f = anyClassPathDir;
        if (f != null) {
            return f;
        }

        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl == null) {
            ccl = ClassLoader.getSystemClassLoader();
        }

        if (ccl == null) {
            throw new IllegalStateException("Missing classloader.");
        }

        String path = System.getProperty("java.class.path");
        String[] dirs = path.split(System.getProperty("path.separator"));
        IOException fail = null;
        for (String dir : dirs) {
            f = new File(dir.trim());
            if (f.isFile()) {
                f = f.getParentFile();
                if (f == null) {
                    continue;
                }
            }

            try {
                if (f.isDirectory()) {
                    final String name = MailHandlerTest.class.getName();
                    final File tmp = File.createTempFile(name, ".tmp", f);
                    final URL url = ccl.getResource(tmp.getName());
                    if (!tmp.delete() && tmp.exists()) {
                        IOException ioe = new IOException(tmp.toString());
                        dump(ioe);
                        throw ioe;
                    }

                    if (url == null || !tmp.equals(new File(url.toURI()))) {
                        throw new FileNotFoundException(tmp + "not visible from " + ccl);
                    }
                    anyClassPathDir = f;
                    return f;
                } else {
                    fail = new FileNotFoundException(f.toString());
                }
            } catch (final IOException ioe) {
                fail = ioe;
            } catch (final URISyntaxException | IllegalArgumentException use) {
                fail = (IOException) new IOException(use.toString()).initCause(use);
            }
        }

        if (fail != null) {
            throw fail;
        }

        //modify the classpath to include a writable directory.
        throw new FileNotFoundException(path);
    }

    @Test
    public void testComparator() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        try {
            instance.setComparator((Comparator<LogRecord>) null);
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }
        assertNull(instance.getComparator());

        UselessComparator uselessComparator = new UselessComparator();
        Comparator<? super LogRecord> result = instance.getComparator();
        assertEquals(false, uselessComparator.equals(result));

        instance.setComparator(uselessComparator);
        result = instance.getComparator();

        assertTrue(uselessComparator.equals(result));

        RawTypeComparator raw = new RawTypeComparator();
        instance.setComparator(raw);
        assertTrue(raw.equals(instance.getComparator()));

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
        instance.setMailProperties(createInitProperties(""));
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
            Properties props = createInitProperties("");
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
     *
     * @param capacity the current capacity.
     * @return the next random capacity.
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
    public void testAuthenticator_Authenticator_Arg() {
        Authenticator auth = new EmptyAuthenticator();

        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        try {
            instance.setAuthenticator((Authenticator) null);
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
        em = internalErrorManagerFrom(instance);
        instance.close();

        assertEquals(1, em.exceptions.size());
        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);
    }

    @Test
    public void testAuthenticator_Char_Array_Arg() {
        PasswordAuthentication pa;
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        //Null literal means actual password value here.
        instance.setAuthenticator("null".toCharArray());
        pa = passwordAuthentication(instance.getAuthenticator(), "user");
        assertEquals("user", pa.getUserName());
        assertEquals("null", pa.getPassword());

        instance.setAuthenticator("Null".toCharArray());
        pa = passwordAuthentication(instance.getAuthenticator(), "user");
        assertEquals("user", pa.getUserName());
        assertEquals("Null", pa.getPassword());

        instance.setAuthenticator("NULL".toCharArray());
        pa = passwordAuthentication(instance.getAuthenticator(), "user");
        assertEquals("user", pa.getUserName());
        assertEquals("NULL", pa.getPassword());

        try {
            instance.setAuthenticator((char[]) null);
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        try {
            instance.setAuthenticator(instance.getAuthenticator());
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        try {
            instance.setAuthenticator("password".toCharArray());
            pa = passwordAuthentication(
                    instance.getAuthenticator(), "user");
            assertEquals("user", pa.getUserName());
            assertEquals("password", pa.getPassword());
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        assertEquals(true, em.exceptions.isEmpty());

        instance = createHandlerWithRecords();
        instance.setAuthenticator("password".toCharArray());
        em = internalErrorManagerFrom(instance);
        instance.close();

        assertEquals(1, em.exceptions.size());
        assertEquals(true, em.exceptions.get(0) instanceof MessagingException);
    }

    @Test
    public void testMailProperties() throws Exception {
        Properties props = new Properties();
        MailHandler instance = new MailHandler();
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getMailProperties());
        assertEquals(Properties.class, instance.getMailProperties().getClass());

        try {
            instance.setMailProperties((Properties) null);
            fail("Null was allowed.");
        } catch (NullPointerException pass) {
        } catch (RuntimeException RE) {
            fail(RE.toString());
        }

        instance.setMailProperties(props);
        Properties stored = instance.getMailProperties();

        assertNotNull(stored);
        assertNotSame(props, stored);
        assertEquals(props.getClass(), stored.getClass());

        assertEquals(true, em.exceptions.isEmpty());
        instance.close();

        instance = createHandlerWithRecords();
        props = instance.getMailProperties();
        em = new InternalErrorManager();
        instance.setErrorManager(em);

        props.setProperty("mail.from", "localhost@localdomain");
        props.setProperty("mail.to", "localhost@localdomain");
        instance.setMailProperties(props);
        instance.flush();
        for (Exception exception : em.exceptions) {
            final Throwable t = exception;
            if (isConnectOrTimeout(t)) {
                continue;
            } else {
                dump(t);
                fail(t.toString());
            }
        }
        assertFalse(em.exceptions.isEmpty());

        props.setProperty("mail.from", "localhost@localdomain");
        props.setProperty("mail.to", "::1@@");
        instance.setMailProperties(props);

        em = new InternalErrorManager();
        instance.setErrorManager(em);

        instance.publish(new LogRecord(Level.SEVERE, "test"));
        instance.close();
        int failed = 0;
        for (Exception exception : em.exceptions) {
            final Throwable t = exception;
            if (t instanceof AddressException || isConnectOrTimeout(t)) {
                continue;
            } else {
                dump(t);
                failed++;
            }
        }
        assertEquals(0, failed);
        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testEmptyAddressParse() throws Exception {
        //Assumed to never return null in the MailHandler.
        InternetAddress[] a = InternetAddress.parse("", false);
        assertTrue(a.length == 0);
    }

    @Test
    public void testDefaultRecipient() throws Exception {
        Properties props = createInitProperties("");
        props.remove("mail.from");
        props.remove("mail.to");
        props.remove("mail.cc");
        props.remove("mail.bcc");

        //User didn't specify so auto compute addresses.
        assertNull(props.getProperty("mail.from"));
        assertNull(props.get("mail.from"));

        assertNull(props.getProperty("mail.to"));
        assertNull(props.get("mail.to"));
        testDefaultRecipient(props);

        //User override of TO and FROM addresses.
        props.setProperty("mail.from", "");
        props.setProperty("mail.to", "");
        testDefaultRecipient(props);

        //Fixed TO and FROM.
        props.setProperty("mail.from", "localhost@localdomain");
        props.setProperty("mail.to", "otherhost@localdomain");
        testDefaultRecipient(props);

        //Compute TO and FROM with a fixed CC and BCC.
        props.remove("mail.from");
        props.remove("mail.to");
        assertNull(props.getProperty("mail.to"));
        assertNull(props.get("mail.to"));
        props.setProperty("mail.cc", "localhost@localdomain");
        props.setProperty("mail.bcc", "otherhost@localdomain");
        testDefaultRecipient(props);
    }

    private void testDefaultRecipient(Properties addresses) throws Exception {

        class DefaultRecipient extends MessageErrorManager {

            DefaultRecipient(Properties props) {
                super(props);
            }

            private Address[] parseKey(Session s, String key) throws Exception {
                final String v = s.getProperty(key);
                if (v == null) {
                    return null; //value not present for key.
                } else if (v.length() == 0) {
                    return new Address[0]; //value empty for key.
                } else {
                    return InternetAddress.parse(v, true);
                }
            }

            @Override
            protected void error(MimeMessage msg, Throwable t, int code) {
                Session s = new MessageContext(msg).getSession();
                try {
                    Address[] local = new Address[]{
                        InternetAddress.getLocalAddress(s)};
                    Address[] expectFrom = parseKey(s, "mail.from");
                    Address[] expectTo = parseKey(s, "mail.to");
                    Address[] expectCc = parseKey(s, "mail.cc");
                    Address[] expectBcc = parseKey(s, "mail.bcc");

                    checkAddress(expectFrom == null ? local : expectFrom,
                            msg.getFrom());
                    checkAddress(expectTo == null ? local : expectTo,
                            msg.getRecipients(Message.RecipientType.TO));

                    assertArrayEquals(expectCc,
                            msg.getRecipients(Message.RecipientType.CC));
                    assertArrayEquals(expectBcc,
                            msg.getRecipients(Message.RecipientType.BCC));

                    List<Address> all = asList(msg.getAllRecipients());
                    assertTrue(all.containsAll(asList(expectTo)));
                    assertTrue(all.containsAll(asList(expectCc)));
                    assertTrue(all.containsAll(asList(expectBcc)));
                } catch (Throwable fail) {
                    dump(fail);
                    fail(fail.toString());
                }
            }

            private void checkAddress(Address[] expect, Address[] found) {
                if (expect.length == 0) {
                    assertTrue(found == null || found.length == 0);
                } else {
                    assertArrayEquals(expect, found);
                }
            }

            private List<Address> asList(Address... a) {
                return Arrays.asList(a == null ? new Address[0] : a);
            }
        }
        MailHandler instance = createHandlerWithRecords();
        Properties props = instance.getMailProperties();
        props.putAll(addresses);
        InternalErrorManager em = new DefaultRecipient(props);
        instance.setErrorManager(em);

        assertNotNull(instance.getMailProperties());
        assertEquals(Properties.class, instance.getMailProperties().getClass());

        instance.setMailProperties(props);
        instance.close();
        for (Exception exception : em.exceptions) {
            final Throwable t = exception;
            if (isConnectOrTimeout(t) || t instanceof SendFailedException) {
                continue;
            } else {
                dump(t);
                fail(t.toString());
            }
        }

        assertFalse(em.exceptions.isEmpty());
    }

    @Test
    public void testDefaultUrlName() throws Exception {
        Properties props = createInitProperties("");
        props.put("mail.transport.protocol", "smtp");
        Session s = Session.getInstance(props);
        Transport t = s.getTransport();
        if (isPrivateSpec(t.getClass())) {
            assertEquals(UNKNOWN_HOST, t.getURLName().getHost());
        }
    }

    @Test
    public void testAttachmentFilters() {
        MailHandler instance = new MailHandler(createInitProperties(""));
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
            instance.setAttachmentFilters((Filter[]) null);
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
            assert filters != null; //Suppress broken NPE hint with assert.
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

        assertEquals(instance.getAttachmentFormatters().length, 2);
        //Force a subclass array.
        instance.setAttachmentFilters(new ThrowFilter[]{new ThrowFilter(), new ThrowFilter()});
        assertEquals(Filter[].class, instance.getAttachmentFilters().getClass());

        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();
    }

    @Test
    public void testAttachmentFiltersDefaults() {
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setFilter(new ErrorFilter());
        final Formatter f = new SimpleFormatter();
        instance.setAttachmentFormatters(f, f, f, f);

        for (Exception exception : em.exceptions) {
            dump(exception);
        }
        assertTrue(em.exceptions.isEmpty());

        assertEquals(ErrorFilter.class, instance.getFilter().getClass());
        assertEquals(instance.getFilter(), instance.getAttachmentFilters()[0]);
        assertEquals(instance.getFilter(), instance.getAttachmentFilters()[1]);
        assertEquals(instance.getFilter(), instance.getAttachmentFilters()[2]);
        assertEquals(instance.getFilter(), instance.getAttachmentFilters()[3]);

        instance.setAttachmentFilters(null, null, null, null);
        assertEquals(ErrorFilter.class, instance.getFilter().getClass());
        assertNull(instance.getAttachmentFilters()[0]);
        assertNull(instance.getAttachmentFilters()[1]);
        assertNull(instance.getAttachmentFilters()[2]);
        assertNull(instance.getAttachmentFilters()[3]);
    }

    @Test
    public void testAttachmentFormatters() {
        MailHandler instance = new MailHandler(createInitProperties(""));

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
            instance.setAttachmentFormatters((Formatter[]) null);
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

        instance.setAttachmentFormatters(new ThrowFormatter[]{new ThrowFormatter()});
        assertEquals(Formatter[].class, instance.getAttachmentFormatters().getClass());
        assertEquals(Filter[].class, instance.getAttachmentFilters().getClass());
        assertEquals(Formatter[].class, instance.getAttachmentNames().getClass());

        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();
    }

    @Test
    public void testAttachmentNames_StringArr() {
        Formatter[] names;
        MailHandler instance = new MailHandler(createInitProperties(""));
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
        } catch (NullPointerException | IndexOutOfBoundsException pass) {
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
        Formatter[] formatters;
        MailHandler instance = new MailHandler(createInitProperties(""));
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
        } catch (NullPointerException | IndexOutOfBoundsException pass) {
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

        instance.setAttachmentNames(new ThrowFormatter[]{new ThrowFormatter(), new ThrowFormatter()});
        assertEquals(Formatter[].class, instance.getAttachmentNames().getClass());
        assertEquals(em.exceptions.isEmpty(), true);
        instance.close();
    }

    @Test
    public void testSubject_String() {
        String subject = "Test subject.";
        MailHandler instance = new MailHandler(createInitProperties(""));
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
        MailHandler instance = new MailHandler(createInitProperties(""));
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
        MailHandler instance = new MailHandler(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        assertNotNull(instance.getSubject());
        assertEquals(CollectorFormatter.class, instance.getSubject().getClass());

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
    public void testAttachmentFilterSwapBeforePush() {
        MailHandler instance = new MailHandler(10);
        instance.setMailProperties(createInitProperties(""));
        instance.setLevel(Level.ALL);
        instance.setPushLevel(Level.OFF);
        instance.setPushFilter((Filter) null);
        instance.setFilter(BooleanFilter.FALSE);
        instance.setAttachmentFormatters(new Formatter[]{new XMLFormatter()});
        instance.setAttachmentFilters(new Filter[]{null});
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        LogRecord record = new LogRecord(Level.SEVERE, "lost record");
        assertTrue(instance.isLoggable(record));

        instance.publish(record);
        instance.setAttachmentFilters(new Filter[]{BooleanFilter.FALSE});
        assertFalse(instance.isLoggable(record));
        instance.close();

        int seenFormat = 0;
        for (Exception exception : em.exceptions) {
            if (exception instanceof MessagingException) {
                continue;
            } else if (exception instanceof RuntimeException
                    && exception.getMessage().contains(instance.getFilter().toString())
                    && exception.getMessage().contains(Arrays.asList(instance.getAttachmentFilters()).toString())) {
                seenFormat++;
                continue; //expected.
            } else {
                fail(String.valueOf(exception));
            }
        }
        assertTrue("No format error", seenFormat > 0);
    }

    @Test
    public void testFilterSwapBeforePush() {
        MailHandler instance = new MailHandler(10);
        instance.setMailProperties(createInitProperties(""));
        instance.setLevel(Level.ALL);
        instance.setPushLevel(Level.OFF);
        instance.setPushFilter((Filter) null);
        instance.setAttachmentFormatters(new SimpleFormatter());
        instance.setAttachmentFilters(BooleanFilter.FALSE);
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        LogRecord record = new LogRecord(Level.SEVERE, "lost record");
        assertTrue(instance.isLoggable(record));

        instance.publish(record);
        instance.setFilter(BooleanFilter.FALSE);
        assertFalse(instance.isLoggable(record));
        instance.close();

        int seenFormat = 0;
        for (Exception exception : em.exceptions) {
            if (exception instanceof MessagingException) {
                continue;
            } else if (exception instanceof RuntimeException
                    && exception.getMessage().contains(instance.getFilter().toString())) {
                seenFormat++;
                continue; //expected.
            } else {
                fail(String.valueOf(exception));
            }
        }
        assertTrue("No format error", seenFormat > 0);
    }

    @Test
    public void testFilterFlipFlop() {
        MailHandler instance = new MailHandlerOverride(10);
        instance.setMailProperties(createInitProperties(""));
        instance.setLevel(Level.ALL);
        instance.setPushLevel(Level.OFF);
        instance.setPushFilter((Filter) null);
        FlipFlopFilter badFilter = new FlipFlopFilter();
        instance.setFilter(badFilter);
        instance.setAttachmentFormatters(new SimpleFormatter());

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);

        LogRecord record = new LogRecord(Level.SEVERE, "lost record");

        assertSame(badFilter, instance.getFilter());
        badFilter.value = true;
        assertSame(badFilter, instance.getFilter());

        assertTrue(instance.isLoggable(record));
        instance.publish(record);
        badFilter.value = false;

        assertSame(badFilter, instance.getFilter());
        assertFalse(instance.isLoggable(record));
        instance.close();
        assertSame(badFilter, instance.getFilter());

        int seenFormat = 0;
        for (Exception exception : em.exceptions) {
            if (exception instanceof MessagingException) {
                continue;
            } else if (exception instanceof RuntimeException
                    && exception.getMessage().contains(instance.getFilter().toString())) {
                seenFormat++;
                continue; //expected.
            } else {
                fail(String.valueOf(exception));
            }
        }
        assertTrue("No format error", seenFormat > 0);
    }

    @Test
    public void testFilterReentrance() {
        Logger logger = Logger.getLogger("testFilterReentrance");

        MailHandler instance = new MailHandler(2);
        instance.setMailProperties(createInitProperties(""));
        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setFilter(new ReentranceFilter());

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(instance);
        hardRef = logger;
        try {
            assertNotNull(hardRef);
            logger.logp(Level.SEVERE, MailHandlerTest.class.getName(), "testFilterReentrance", "test");

            int seenIse = 0;
            for (Exception exception : em.exceptions) {
                if (exception instanceof MessagingException) {
                    continue;
                } else if (exception instanceof IllegalStateException) {
                    seenIse++;
                    continue; //expected.
                } else {
                    fail(String.valueOf(exception));
                }
            }

            assertTrue("No IllegalStateException", seenIse > 0);
        } finally {
            logger.removeHandler(instance);
            logger.setLevel((Level) null);
            logger.setUseParentHandlers(true);
            hardRef = null;
        }
    }

    @Test
    public void testPushFilterReentrance() {
        testPushFilterReentrance(1, 1);
        testPushFilterReentrance(1, 2);
        testPushFilterReentrance(1, 1000);
        testPushFilterReentrance(500, 1000);
        testPushFilterReentrance(1000, 1000);
    }

    private void testPushFilterReentrance(int records, int cap) {
        assert records <= cap : records;
        Logger logger = Logger.getLogger("testPushFilterReentrance");

        MailHandler instance = new MailHandler(cap);

        Properties props = createInitProperties("");
        instance.setMailProperties(props);

        InternalErrorManager em = new InternalErrorManager();
        instance.setErrorManager(em);
        instance.setPushLevel(Level.ALL);
        instance.setPushFilter(new ReentranceFilter());

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(instance);
        hardRef = logger;
        try {
            assertNotNull(hardRef);

            while (records-- > 0) {
                logger.logp(Level.SEVERE, MailHandlerTest.class.getName(), "testPushFilterReentrance", "test");
            }
            instance.close();

            for (Exception exception : em.exceptions) {
                Throwable t = exception;
                if ((t instanceof MessagingException == false)
                        && (t instanceof IllegalStateException == false)) {
                    dump(t);
                    fail(String.valueOf(t));
                }
            }
            assertFalse(em.exceptions.isEmpty());
        } finally {
            logger.removeHandler(instance);
            logger.setLevel((Level) null);
            logger.setUseParentHandlers(true);
            hardRef = null;
        }
    }

    @Test
    public void testMailDebugLowCap() throws Exception {
        MailHandler instance = new MailHandler(1);
        try {
            testMailDebug(instance, 2);
        } finally {
            instance.close();
        }
    }

    @Test
    public void testMailDebugPushLevel() throws Exception {
        MailHandler instance = new MailHandler(1000);
        try {
            instance.setPushLevel(Level.ALL);
            testMailDebug(instance, 2);
        } finally {
            instance.close();
        }
    }

    @Test
    public void testMailDebugPush() throws Exception {
        MailHandler instance = new MailHandler(4);
        try {
            testMailDebug(instance, -3);
        } finally {
            instance.close();
        }
    }

    @Test
    public void testMailDebugFlush() throws Exception {
        MailHandler instance = new MailHandler(3);
        try {
            testMailDebug(instance, -2);
        } finally {
            instance.close();
        }
    }

    @Test
    public void testMailDebugClose() throws Exception {
        MailHandler instance = new MailHandler(1000);
        try {
            testMailDebug(instance, -1);
        } finally {
            instance.close();
        }
    }

    @Test
    public void testMailDebugErrorManager() throws Exception {
        MailHandler instance = new MailHandler(1);
        try {
            instance.setErrorManager(new MailDebugErrorManager());
            testMailDebug(instance, 2);
        } finally {
            instance.close();
        }
    }

    private void testMailDebug(MailHandler instance, int records) throws Exception {
        final Properties props = createInitProperties("");
        props.put("mail.debug", "true");
        props.put("verify", "local");

        instance.setLevel(Level.ALL);
        testMailDebugQuietLog(instance, props, records);
    }

    private void testMailDebugQuietLog(MailHandler instance, Properties props, int records) throws Exception {
        final Logger root = Logger.getLogger("");
        hardRef = root;
        try {
            final Handler[] handlers = root.getHandlers();
            for (Handler h : handlers) {
                root.removeHandler(h);
            }
            try {
                root.setLevel(Level.ALL);
                root.addHandler(instance);
                try {
                    testMailDebugQuietStreams(instance, props, records);
                } finally {
                    root.setLevel(null);
                    root.removeHandler(instance);
                }
            } finally {
                for (Handler h : handlers) {
                    root.addHandler(h);
                }
            }
        } finally {
            hardRef = null;
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private void testMailDebugQuietStreams(MailHandler instance, Properties props, int records) throws Exception {
        final PrintStream out = System.out;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos, true, "ISO-8859-1"));
            final PrintStream err = System.err;
            try {
                System.setErr(new PrintStream(baos, true, "ISO-8859-1"));
                instance.setMailProperties(props);

                if (records > 0) {
                    for (int i = 0; i < records; i++) {
                        baos.reset();
                        instance.publish(new LogRecord(Level.SEVERE, ""));
                    }
                } else {
                    records = -records;
                    for (int i = 0; i < records; i++) {
                        baos.reset();
                        instance.publish(new LogRecord(Level.SEVERE, ""));
                    }

                    if (records == 1) {
                        instance.close();
                    } else if (records == 2) {
                        instance.flush();
                    } else if (records == 3) {
                        instance.push();
                    }
                }
            } finally {
                System.setErr(err);
            }
        } finally {
            System.setOut(out);
        }
    }

    @Test
    public void testReportError() {
        MailHandler instance = new MailHandler(createInitProperties(""));
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
        instance.setErrorManager(new MessageErrorManager(instance.getMailProperties()) {

            @Override
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
    public void testReportErrorUtf8Addresses() throws Exception {
        final String test = "test\u03b1";
        final String saddr = test + '@' + UNKNOWN_HOST;
        final String sender = test + saddr;
        Properties props = createInitProperties("");
        props.put("mail.to", saddr);
        props.put("mail.cc", saddr);
        props.put("mail.bcc", saddr);
        props.put("mail.from", saddr);
        props.put("mail.sender", sender);
        props.put("mail.mime.allowutf8",  "true");
        MailHandler instance = new MailHandler(props);
        instance.setEncoding("UTF-8");
        instance.setFormatter(new SimpleFormatter());
        instance.setErrorManager(new MessageErrorManager(instance.getMailProperties()) {

            @Override
            protected void error(MimeMessage message, Throwable t, int code) {
                try {
                    assertEquals(saddr, toString(message.getFrom()[0]));
                    assertEquals(saddr, toString(message.getRecipients(Message.RecipientType.TO)[0]));
                    assertEquals(saddr, toString(message.getRecipients(Message.RecipientType.CC)[0]));
                    assertEquals(saddr, toString(message.getRecipients(Message.RecipientType.BCC)[0]));

                    assertEquals(sender, toString(message.getSender()));
                    assertTrue(String.valueOf(message.getContent()).contains(sender));

                    message.saveChanges();
                } catch (MessagingException | IOException E) {
                    fail(E.toString());
                }
            }

            private String toString(Address o) {
                return ((InternetAddress)o).toUnicodeString();
            }
        });
        instance.publish(new LogRecord(Level.SEVERE, sender));
        instance.close();
    }

    @Test
    public void testReportErrorSuper() throws Exception {
        assertNull(System.getSecurityManager());
        Field mhem = MailHandler.class.getDeclaredField("errorManager");
        mhem.setAccessible(true);

        InternalErrorManager superEm = new InternalErrorManager();
        InternalErrorManager em = new InternalErrorManager();
        MailHandler h = new MailHandler();
        try {
            Exception tester = new Exception();
            synchronized (h) {
                assertSame(h.getErrorManager(), getSuperErrorManager(h));

                h.setErrorManager(superEm);
                assertSame(superEm, getSuperErrorManager(h));
                assertSame(superEm, mhem.get(h));

                mhem.set(h, em);
                assertSame(em, h.getErrorManager());
                assertSame(superEm, getSuperErrorManager(h));
                assertNotSame(h.getErrorManager(), getSuperErrorManager(h));
                h.reportError("", tester, ErrorManager.GENERIC_FAILURE);
            }

            assertEquals(1, em.exceptions.size());
            assertSame(tester, em.exceptions.get(0));
            assertTrue(superEm.exceptions.toString(),
                    superEm.exceptions.isEmpty());
        } finally {
            h.close();
        }
    }

    @Test
    public void testGaeReportErrorSuper() throws Exception {
        Field mhem = MailHandler.class.getDeclaredField("errorManager");
        mhem.setAccessible(true);

        InternalErrorManager em = new InternalErrorManager();
        GaeSecurityManager sm = new GaeSecurityManager();
        System.setSecurityManager(sm);
        sm.secure = true;
        try {
            MailHandler h = new MailHandler();
            try {
                Exception tester = new Exception();
                synchronized (h) {
                    sm.secure = false;
                    final Object def = getSuperErrorManager(h);
                    assertSame(def, getSuperErrorManager(h));
                    sm.secure = true;

                    assertEquals(h.getErrorManager().getClass(), def.getClass());
                    assertNotSame(h.getErrorManager(), def);

                    h.setErrorManager(em);
                    sm.secure = false;
                    final Object sem = getSuperErrorManager(h);
                    sm.secure = true;
                    assertSame(def, sem);
                    assertNotSame(h.getErrorManager(), def);
                    assertNotSame(h.getErrorManager(), sem);
                    assertSame(h.getErrorManager(), em);

                    h.reportError("", tester, ErrorManager.GENERIC_FAILURE);
                }

                assertEquals(1, em.exceptions.size());
                assertSame(tester, em.exceptions.get(0));
            } finally {
                h.close();
            }
        } finally {
            sm.secure = false;
            System.setSecurityManager((SecurityManager) null);
        }
    }

    @Test
    public void testReportErrorLinkageWithStack() throws Exception {
        testReportErrorLinkageWithStack(new LinkageErrorStream());
    }

    @Test
    public void testReportErrorRuntimeWithStack() throws Exception {
        testReportErrorLinkageWithStack(new RuntimeErrorStream());
    }

    private void testReportErrorLinkageWithStack(PrintStream ps) throws Exception {
        MailHandler instance = new MailHandler(createInitProperties(""));

        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        final PrintStream err = System.err;
        System.setErr(ps);
        try {
            try {
                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.CLOSE_FAILURE);

                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.FLUSH_FAILURE);

                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.GENERIC_FAILURE);

                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.OPEN_FAILURE);

                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.WRITE_FAILURE);
            } finally {
                System.setErr(err);
            }
        } catch (PrintThrowsRuntimeException unexpected) {
            unexpected.dump();
            fail(unexpected.getMessage());
        }
    }

    @Test
    public void testReportErrorLinkageEmptyStack() throws Throwable {
        testReportErrorLinkageEmptyStack(
                new LinkageErrorStream(new StackTraceElement[0]));
    }

    @Test
    public void testReportErrorLinkageShortStack() throws Throwable {
        testReportErrorLinkageEmptyStack(
                new LinkageErrorStream(new StackTraceElement[]{
                    new StackTraceElement("", "", "", -1)}));
    }

    @Test
    public void testReportErrorRuntimeEmptyStack() throws Throwable {
        testReportErrorLinkageEmptyStack(
                new RuntimeErrorStream(new StackTraceElement[0]));
    }

    @Test
    public void testReportErrorRuntimeShortStack() throws Throwable {
        testReportErrorLinkageEmptyStack(
                new RuntimeErrorStream(new StackTraceElement[]{
                    new StackTraceElement("", "", "", -1)}));
    }

    private void testReportErrorLinkageEmptyStack(PrintStream ps) throws Throwable {
        MailHandler instance = new MailHandler(createInitProperties(""));

        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        final PrintStream err = System.err;
        System.setErr(ps);
        try {
            try {
                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.FLUSH_FAILURE);

                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.CLOSE_FAILURE);

                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.GENERIC_FAILURE);

                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.OPEN_FAILURE);

                instance.setErrorManager(new ErrorManager());
                instance.reportError(null, null, ErrorManager.WRITE_FAILURE);
            } finally {
                System.setErr(err);
            }
        } catch (PrintThrowsRuntimeException unexpected) {
            unexpected.dump();
            fail(unexpected.getMessage());
        }
    }

    @Ignore
    public void testGaeForbiddenHeaders() throws Exception {
        assumeNoJit();
        assertNull(System.getSecurityManager());
        assertTrue(LogManagerProperties.hasLogManager());
        final Class<?> k = LogManagerProperties.class;
        final Field f = k.getDeclaredField("LOG_MANAGER");
        final Field mod = setAccessible(f);
        try {
            final Object lm = f.get(null);
            f.set(null, new Properties());
            try {
                fullFence();
                assertFalse(LogManagerProperties.hasLogManager());
                MailHandler instance = createHandlerWithRecords();
                instance.setErrorManager(new GaeErrorManager(instance));
                instance.close();
            } finally {
                f.set(null, lm);
                fullFence();
            }
        } finally {
            mod.setInt(f, f.getModifiers() | Modifier.FINAL);
        }
        assertTrue(LogManagerProperties.hasLogManager());
    }

    @Ignore
    public void testGaeSecurityManager() throws Exception {
        assumeNoJit();
        InternalErrorManager em;
        MailHandler h = null;
        final GaeSecurityManager manager = new GaeSecurityManager();
        System.setSecurityManager(manager);
        try {
            manager.secure = false;
            h = new MailHandler(createInitProperties(""));
            em = new InternalErrorManager();
            h.setErrorManager(em);
            manager.secure = true;
            assertEquals(manager, System.getSecurityManager());

            //GAE allows access to loggers.
            Logger global = Logger.getLogger("global");
            hardRef = global;
            global.addHandler(h);
            global.removeHandler(h);
            global.removeHandler((Handler) null);
            hardRef = null;

            h.postConstruct();
            h.setAttachmentFormatters(new Formatter[]{new ThrowFormatter()});
            assertEquals(1, h.getAttachmentFormatters().length);

            h.setAttachmentFilters(new Filter[]{new ThrowFilter()});
            assertEquals(1, h.getAttachmentFormatters().length);

            assertEquals(1, h.getAttachmentFormatters().length);
            h.setAttachmentNames(new String[]{"error.txt"});

            assertEquals(1, h.getAttachmentFormatters().length);
            h.setAttachmentNames(new Formatter[]{new ThrowFormatter()});

            h.setAuthenticator((Authenticator) null);
            h.setComparator((Comparator<? super LogRecord>) null);

            h.setLevel(Level.ALL);
            h.setFilter(BooleanFilter.FALSE);
            h.setFilter((Filter) null);
            h.setFormatter(new EmptyFormatter());

            assertNotNull(h.getErrorManager());
            h.setErrorManager(new ErrorManager());

            h.setEncoding((String) null);

            h.flush();
            h.push();

            h.setMailProperties(new Properties());

            h.setPushFilter((Filter) null);
            h.setPushLevel(Level.OFF);

            h.setSubject(new ThrowFormatter());
            h.setSubject("test");

            h.getAuthenticator();
            h.getMailProperties();

            h.preDestroy();
            h.close();

            //check for internal exceptions caused by security manager.
            for (Exception e : em.exceptions) {
                dump(e);
            }
            assertTrue(em.exceptions.isEmpty());

            hardRef = h = new MailHandler();
            h.close();

            hardRef = h = new MailHandler(100);
            assertEquals(100, h.getCapacity());
            h.close();

            Properties props = new Properties();
            props.put("test", "test");
            hardRef = h = new MailHandler(props);
            assertEquals(props, h.getMailProperties());
        } finally {
            hardRef = null;
            manager.secure = false;
            System.setSecurityManager((SecurityManager) null);
            if (h != null) {
                h.close();
            }
        }
    }

    /**
     * Test logging permissions of the MailHandler. Must run by itself or run in
     * isolated VM. Use system property java.security.debug=all to troubleshoot
     * failures.
     */
    @Test
    public void testSecurityManager() {
        InternalErrorManager em;
        MailHandler h = null;
        final ThrowSecurityManager manager = new ThrowSecurityManager();
        System.setSecurityManager(manager);
        try {
            manager.secure = false;
            h = new MailHandler(createInitProperties(""));
            em = new InternalErrorManager();
            h.setErrorManager(em);
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
            } catch (SecurityException | NullPointerException pass) {
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
            } catch (SecurityException | NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                assertEquals(1, h.getAttachmentFormatters().length);
                h.setAttachmentNames((Formatter[]) null);
                fail("Missing secure check.");
            } catch (SecurityException | NullPointerException pass) {
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
                h.setAuthenticator((Authenticator) null);
                fail("Missing secure check.");
            } catch (SecurityException | NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setComparator((Comparator<? super LogRecord>) null);
                fail("Missing secure check.");
            } catch (SecurityException | NullPointerException pass) {
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
            } catch (SecurityException | NullPointerException pass) {
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
                h.setFilter((Filter) null);
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
                h.setFormatter((Formatter) null);
                fail("Missing secure check.");
            } catch (SecurityException | NullPointerException pass) {
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
                h.setErrorManager((ErrorManager) null);
                fail("Missing secure check.");
            } catch (SecurityException | NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setEncoding((String) null);
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
            } catch (SecurityException | NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setPushFilter((Filter) null);
                fail("Missing secure check.");
            } catch (SecurityException | NullPointerException pass) {
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
            } catch (SecurityException | NullPointerException pass) {
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
            } catch (SecurityException | NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                h.setSubject((String) null);
                fail("Missing secure check.");
            } catch (SecurityException | NullPointerException pass) {
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

            //check for internal exceptions caused by security manager.
            next:
            for (Exception e : em.exceptions) {
                for (Throwable t = e; t != null; t = t.getCause()) {
                    if (t instanceof SecurityException) {
                        continue next; //expected
                    } else if (t instanceof RuntimeException) {
                        throw (RuntimeException) t; //fail
                    }
                }
            }
            em.exceptions.clear();

            try {
                hardRef = new MailHandler();
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                hardRef = new MailHandler(100);
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                hardRef = new MailHandler(new Properties());
                fail("Missing secure check.");
            } catch (SecurityException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                hardRef = new MailHandler(-100);
                fail("Missing secure check.");
            } catch (SecurityException | IllegalArgumentException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }

            try {
                hardRef = new MailHandler((Properties) null);
                fail("Missing secure check.");
            } catch (SecurityException | NullPointerException pass) {
            } catch (Exception fail) {
                fail(fail.toString());
            }
        } finally {
            hardRef = null;
            manager.secure = false;
            System.setSecurityManager((SecurityManager) null);
            if (h != null) {
                h.close();
            }
        }
    }

    @Test
    public void testVerifyErrorManager() throws Exception {
        LogManager manager = LogManager.getLogManager();
        try {
            manager.reset();

            final String p = MailHandler.class.getName();
            Properties props = createInitProperties(p);
            props.put(p.concat(".encoding"), "us-ascii");
            props.put(p.concat(".mail.to"), "foo@bar.com");
            props.put(p.concat(".mail.cc"), "fizz@buzz.com");
            props.put(p.concat(".mail.bcc"), "baz@bar.com");
            props.put(p.concat(".subject"), p.concat(" test"));
            props.put(p.concat(".mail.from"), "localhost@localdomain");
            props.put(p.concat(".mail.sender"), "mail@handler");
            props.put(p.concat(".errorManager"), VerifyErrorManager.class.getName());
            props.put(p.concat(".verify"), "remote");

            read(manager, props);

            MailHandler instance = new MailHandler();
            InternalErrorManager em = internalErrorManagerFrom(instance);

            //ensure VerifyErrorManager was installed.
            assertEquals(VerifyErrorManager.class, em.getClass());

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (!isConnectOrTimeout(t)) {
                    dump(t);
                    fail(t.toString());
                }
            }

            assertFalse(em.exceptions.isEmpty());
            instance.close();
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testVerifyNoContent() throws Exception {
        Properties props = createInitProperties("");
        Session session = Session.getInstance(props);
        MimeMessage msg = new MimeMessage(session);
        Address[] from = InternetAddress.parse("me@localhost", false);
        msg.addFrom(from);
        msg.setRecipients(Message.RecipientType.TO, from);
        ByteArrayOutputStream out = new ByteArrayOutputStream(384);
        msg.setHeader("Content-Transfer-Encoding", "base64");
        msg.saveChanges();
        try {
            msg.writeTo(out);
            fail("Verify type 'remote' may send a message with no content.");
        } catch (MessagingException | IOException expect) {
            msg.setContent("", "text/plain");
            msg.saveChanges();
            msg.writeTo(out);
        } finally {
            out.close();
        }
    }

    @Test
    public void testIsMissingContent() throws Exception {
        Properties props = createInitProperties("");

        MailHandler target = new MailHandler(props);
        Session session = Session.getInstance(props);
        MimeMessage msg = new MimeMessage(session);
        Address[] from = InternetAddress.parse("me@localhost", false);
        msg.addFrom(from);
        msg.setRecipients(Message.RecipientType.TO, from);
        msg.setHeader("Content-Transfer-Encoding", "base64");
        msg.saveChanges();
        try {
            msg.writeTo(new ByteArrayOutputStream(384));
            fail("Verify type 'remote' may hide remote exceptions.");
        } catch (RuntimeException re) {
            throw re; //Avoid catch all.
        } catch (Exception expect) {
            assertNotNull(expect.getMessage());
            assertTrue(expect.getMessage().length() != 0);
            assertTrue(target.isMissingContent(msg, expect));
            assertTrue(target.isMissingContent(msg, new Exception(expect)));
            assertTrue(target.isMissingContent(msg, new MessagingException("", expect)));
            assertFalse(target.isMissingContent(msg, new Exception()));
            assertFalse(target.isMissingContent(msg, new RuntimeException()));
        }
    }

    @Test
    public void testIntern() throws Exception {
        assertNull(System.getSecurityManager());
        final String p = MailHandler.class.getName();
        Properties props = createInitProperties(p);
        props.put(p.concat(".errorManager"),
                InternFilterErrorManager.class.getName());
        props.put(p.concat(".comparator"),
                InternFilterFormatterComparator.class.getName());
        props.put(p.concat(".filter"), InternFilter.class.getName());
        props.put(p.concat(".pushFilter"),
                InternFilterErrorManager.class.getName());

        props.put(p.concat(".attachment.formatters"),
                SimpleFormatter.class.getName() + ", "
                + InternFilterFormatter.class.getName() + ", "
                + InternFormatter.class.getName() + ", "
                + XMLFormatter.class.getName() + ", "
                + InternFormatter.class.getName() + ", "
                + SimpleFormatter.class.getName() + ", "
                + SimpleFormatter.class.getName());

        props.put(p.concat(".attachment.filters"),
                null + ", "
                + InternFilterFormatter.class.getName() + ", "
                + InternFilterFormatter.class.getName() + ", "
                + InternFilter.class.getName() + ", "
                + InternFilter.class.getName() + ", "
                + InternBadSubFilter.class.getName() + ", "
                + InternBadFilter.class.getName());

        final String txt = "Intern test";
        props.put(p.concat(".attachment.names"),
                txt + ", "
                + InternFilterFormatter.class.getName() + ", "
                + InternFilterFormatter.class.getName() + ", "
                + txt + ", "
                + InternFormatter.class.getName() + ", "
                + InternFilterFormatterComparator.class.getName() + ", "
                + InternFilterFormatterComparator.class.getName());
        props.put(p.concat(".subject"), txt);

        MailHandler instance = testIntern(p, props);
        instance.close();

        Formatter[] formatter = instance.getAttachmentFormatters();
        Filter[] filter = instance.getAttachmentFilters();
        Formatter[] names = instance.getAttachmentNames();

        assertSame(instance.getErrorManager(), instance.getPushFilter());
        assertNull(filter[0]);
        assertSame(filter[1], filter[2]);
        assertSame(filter[1], formatter[1]);
        assertSame(filter[2], formatter[1]);
        assertSame(names[1], filter[1]);
        assertSame(names[1], formatter[1]);
        assertSame(names[2], filter[2]);
        assertSame(names[2], formatter[1]);
        assertSame(names[1], filter[2]);
        assertNotNull(instance.getSubject());
        assertSame(instance.getSubject(), names[0]);
        assertSame(instance.getSubject(), names[3]);
        assertSame(names[0], names[3]);
        assertNotNull(instance.getFilter());
        assertSame(instance.getFilter(), filter[3]);
        assertSame(instance.getFilter(), filter[4]);
        assertSame(filter[3], filter[4]);
        assertNotSame(filter[5], filter[6]); //Bad equals method.
        assertNotNull(instance.getComparator());  //Comparator is not interned.
        assertNotSame(instance.getComparator(), names[5]);
        assertNotSame(instance.getComparator(), names[6]);
        assertSame(names[5], names[6]);

        InternalErrorManager em = internalErrorManagerFrom(instance);
        for (Exception exception : em.exceptions) {
            final Throwable t = exception;
            if (t instanceof IllegalArgumentException
                    && String.valueOf(t.getMessage()).contains("equal")) {
                continue;
            }
            dump(t);
        }
        assertFalse(em.exceptions.isEmpty());
    }

    private MailHandler testIntern(String p, Properties props) throws Exception {
        props.put(p.concat(".mail.to"), "badAddress");
        props.put(p.concat(".mail.cc"), "badAddress");
        props.put(p.concat(".mail.from"), "badAddress");

        final LogManager manager = LogManager.getLogManager();
        read(manager, props);
        try {
            return new MailHandler();
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testComparatorReverse() throws Exception {
        final String p = MailHandler.class.getName();
        Properties props = createInitProperties(p);
        props.put(p.concat(".comparator"), SequenceComparator.class.getName());
        props.put(p.concat(".comparator.reverse"), "false");
        props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());

        LogRecord low = new LogRecord(Level.INFO, "");
        LogRecord high = new LogRecord(Level.INFO, "");
        final LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            MailHandler instance = new MailHandler();
            instance.close();
            InternalErrorManager em = internalErrorManagerFrom(instance);
            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                dump(t);
                fail(t.toString());
            }
            assertTrue(em.exceptions.isEmpty());

            assertEquals(SequenceComparator.class,
                    instance.getComparator().getClass());

            assertTrue(instance.getComparator().compare(low, high) < 0);
            assertTrue(instance.getComparator().compare(high, low) > 0);

            props.put(p.concat(".comparator.reverse"), "true");
            read(manager, props);
            instance = new MailHandler();
            instance.close();
            em = internalErrorManagerFrom(instance);
            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                dump(t);
                fail(t.toString());
            }
            assertTrue(em.exceptions.isEmpty());

            Comparator<? super LogRecord> c = instance.getComparator();
            assertTrue(SequenceComparator.class != c.getClass());
            assertFalse(instance.getComparator().compare(low, high) < 0);
            assertFalse(instance.getComparator().compare(high, low) > 0);

            props.put(p.concat(".comparator"),
                    SequenceComparatorWithReverse.class.getName());
            read(manager, props);
            instance = new MailHandler();
            instance.close();
            em = internalErrorManagerFrom(instance);
            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                dump(t);
                fail(t.toString());
            }
            assertTrue(em.exceptions.isEmpty());

            c = instance.getComparator();
            assertTrue(SequenceDescComparator.class == c.getClass());
            assertFalse(instance.getComparator().compare(low, high) < 0);
            assertFalse(instance.getComparator().compare(high, low) > 0);

            props.put(p.concat(".comparator"), "");
            read(manager, props);
            instance = new MailHandler();
            instance.close();
            em = internalErrorManagerFrom(instance);
            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (t instanceof IllegalArgumentException) {
                    continue;
                }
                dump(t);
                fail(t.toString());
            }

            assertFalse(IllegalArgumentException.class.getName(),
                    em.exceptions.isEmpty());

            props.put(p.concat(".comparator"), "null");
            read(manager, props);
            instance = new MailHandler();
            instance.close();
            em = internalErrorManagerFrom(instance);
            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (t instanceof IllegalArgumentException) {
                    continue;
                }
                dump(t);
                fail(t.toString());
            }

            assertFalse(IllegalArgumentException.class.getName(),
                    em.exceptions.isEmpty());

            props.remove(p.concat(".comparator"));
            read(manager, props);
            instance = new MailHandler();
            instance.close();
            em = internalErrorManagerFrom(instance);
            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (t instanceof IllegalArgumentException) {
                    continue;
                }
                dump(t);
                fail(t.toString());
            }

            assertFalse(IllegalArgumentException.class.getName(),
                    em.exceptions.isEmpty());
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testVerifyLogManager() throws Exception {
        LogManager manager = LogManager.getLogManager();
        try {
            manager.reset();

            final String p = MailHandler.class.getName();
            Properties props = createInitProperties(p);
            props.put(p.concat(".subject"), p.concat(" test"));
            props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
            props.put(p.concat(".verify"), "limited");

            read(manager, props);

            MailHandler instance = new MailHandler();
            InternalErrorManager em = internalErrorManagerFrom(instance);

            assertEquals(InternalErrorManager.class, em.getClass());

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (t instanceof AddressException == false) {
                    dump(t);
                    fail(t.toString());
                }
            }
            assertFalse(em.exceptions.isEmpty());

            instance.close();

            props.put(p.concat(".verify"), "local");

            read(manager, props);

            instance = new MailHandler();
            em = internalErrorManagerFrom(instance);

            assertEquals(InternalErrorManager.class, em.getClass());

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (t instanceof AddressException == false) {
                    dump(t);
                    fail(t.toString());
                }
            }
            assertFalse(em.exceptions.isEmpty());

            instance.close();

            props.put(p.concat(".verify"), "resolve");

            read(manager, props);

            instance = new MailHandler();
            em = internalErrorManagerFrom(instance);

            assertEquals(InternalErrorManager.class, em.getClass());

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (isConnectOrTimeout(t)) {
                    continue;
                }
                if (t instanceof AddressException == false) {
                    dump(t);
                    fail(t.toString());
                }
            }
            assertFalse(em.exceptions.isEmpty());

            instance.close();

            props.put(p.concat(".verify"), "remote");
            read(manager, props);

            instance = new MailHandler();
            em = internalErrorManagerFrom(instance);

            assertEquals(InternalErrorManager.class, em.getClass());

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (t instanceof AddressException) {
                    continue;
                } else if (isConnectOrTimeout(t)) {
                    continue;
                } else {
                    dump(t);
                    fail(t.toString());
                }
            }
            assertFalse(em.exceptions.isEmpty());
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testVerifyProperties() throws Exception {
        Properties props = createInitProperties("");
        props.put("subject", "test");
        props.put("verify", "limited");

        InternalErrorManager em = new InternalErrorManager();
        MailHandler instance = new MailHandler();
        try {
            instance.setErrorManager(em);
            instance.setMailProperties(props);

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (t instanceof AddressException == false) {
                    dump(t);
                    fail(t.toString());
                }
            }
        } finally {
            instance.close();
        }

        props.put("verify", "local");
        instance = new MailHandler();
        try {
            em = new InternalErrorManager();
            instance.setErrorManager(em);
            instance.setMailProperties(props);

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (t instanceof AddressException == false) {
                    dump(t);
                    fail(t.toString());
                }
            }
        } finally {
            instance.close();
        }

        props.put("verify", "resolve");
        instance = new MailHandler();
        try {
            em = new InternalErrorManager();
            instance.setErrorManager(em);
            instance.setMailProperties(props);

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (isConnectOrTimeout(t)) {
                    continue;
                }
                if (t instanceof AddressException == false) {
                    dump(t);
                    fail(t.toString());
                }
            }
        } finally {
            instance.close();
        }

        props.put("verify", "login");
        instance = new MailHandler();
        try {
            em = new InternalErrorManager();
            instance.setErrorManager(em);
            instance.setMailProperties(props);

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (isConnectOrTimeout(t)) {
                    continue;
                }
                if (t instanceof AddressException == false) {
                    dump(t);
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

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                if (t instanceof AddressException) {
                    continue;
                } else if (isConnectOrTimeout(t)) {
                    continue;
                } else {
                    dump(t);
                    fail(t.toString());
                }
            }
        } finally {
            instance.close();
        }
    }

    @Test
    public void testVerifyPropertiesConstructor() throws Exception {
        LogManager manager = LogManager.getLogManager();
        try {
            manager.reset();

            final String p = MailHandler.class.getName();
            Properties props = createInitProperties(p);
            props.put(p.concat(".subject"), p.concat(" test"));
            props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
            props.put(p.concat(".formatter"), XMLFormatter.class.getName());
            read(manager, props);

            props = createInitProperties("");
            props.put("subject", "test");
            props.put("verify", "limited");

            MailHandler instance = new MailHandler(props);
            try {
                InternalErrorManager em = internalErrorManagerFrom(instance);

                for (Exception exception : em.exceptions) {
                    final Throwable t = exception;
                    if (t instanceof AddressException == false) {
                        dump(t);
                        fail(t.toString());
                    }
                }
                assertFalse(em.exceptions.isEmpty());
            } finally {
                instance.close();
            }

            props = createInitProperties("");
            props.put("mail.to", "badAddress");
            props.put("mail.cc", "badAddress");
            props.put("subject", "test");
            props.put("mail.from", "badAddress");
            props.put("verify", "local");

            instance = new MailHandler(props);
            try {
                InternalErrorManager em = internalErrorManagerFrom(instance);

                for (Exception exception : em.exceptions) {
                    final Throwable t = exception;
                    if (t instanceof AddressException == false) {
                        dump(t);
                        fail(t.toString());
                    }
                }
                assertFalse(em.exceptions.isEmpty());
            } finally {
                instance.close();
            }

            props.put("verify", "resolve");

            instance = new MailHandler(props);
            try {
                InternalErrorManager em = internalErrorManagerFrom(instance);

                for (Exception exception : em.exceptions) {
                    final Throwable t = exception;
                    if (isConnectOrTimeout(t)) {
                        continue;
                    }
                    if (t instanceof AddressException == false) {
                        dump(t);
                        fail(t.toString());
                    }
                }
                assertFalse(em.exceptions.isEmpty());
            } finally {
                instance.close();
            }

            props.put("verify", "remote");
            instance = new MailHandler(props);
            try {
                InternalErrorManager em = internalErrorManagerFrom(instance);

                for (Exception exception : em.exceptions) {
                    final Throwable t = exception;
                    if (t instanceof AddressException) {
                        continue;
                    } else if (isConnectOrTimeout(t)) {
                        continue;
                    } else {
                        dump(t);
                        fail(t.toString());
                    }
                }
                assertFalse(em.exceptions.isEmpty());
            } finally {
                instance.close();
            }
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testNoVerifyReplacedProperties() throws Exception {
        LogManager manager = LogManager.getLogManager();
        try {
            manager.reset();

            final String p = MailHandler.class.getName();
            Properties props = createInitProperties(p);
            props.put(p.concat(".subject"), p.concat(" test"));
            props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
            props.put(p.concat(".verify"), "remote");

            read(manager, props);

            //Use empty properties to prove fallback to LogManager.
            MailHandler instance = new MailHandler(new Properties());
            InternalErrorManager em = internalErrorManagerFrom(instance);
            assertEquals(InternalErrorManager.class, em.getClass());
            instance.close();

            for (Exception exception : em.exceptions) {
                final Throwable t = exception;
                dump(t);
                fail(t.toString());
            }
            assertTrue(em.exceptions.isEmpty());
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testInitSubject() throws Exception {
        InternalErrorManager em;
        MailHandler target;
        final String p = MailHandler.class.getName();
        final LogManager manager = LogManager.getLogManager();
        Properties props = createInitProperties(p);
        props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());

        //test class cast.
        props.put(p.concat(".subject"), Properties.class.getName());

        read(manager, props);

        try {
            target = new MailHandler();
            try {
                em = internalErrorManagerFrom(target);
                for (Exception exception : em.exceptions) {
                    dump(exception);
                }
                assertTrue(em.exceptions.isEmpty());
            } finally {
                target.close();
            }

            //test linkage error.
            props.put(p.concat(".subject"), ThrowFormatter.class.getName().toUpperCase(Locale.US));
            read(manager, props);

            target = new MailHandler();
            try {
                em = internalErrorManagerFrom(target);
                for (Exception exception : em.exceptions) {
                    dump(exception);
                }
                assertTrue(em.exceptions.isEmpty());
            } finally {
                target.close();
            }

            //test mixed linkage error.
            props.put(p.concat(".subject"), Properties.class.getName().toUpperCase(Locale.US));
            read(manager, props);

            target = new MailHandler();
            try {
                em = internalErrorManagerFrom(target);
                for (Exception exception : em.exceptions) {
                    dump(exception);
                }
                assertTrue(em.exceptions.isEmpty());
            } finally {
                target.close();
            }
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testInitAuthenticator() throws Exception {
        //Liternal null and null reference mean no Authenticator
        //when dealing with a properties file.
        testInitAuthenticator("user", null);
        testInitAuthenticator("user", "null");
        testInitAuthenticator("user", "NULL");
        testInitAuthenticator("user", "Null");
        testInitAuthenticator("user", "");
        testInitAuthenticator("user", "somepassword");
    }

    private void testInitAuthenticator(String user, String pass) throws Exception {
        InternalErrorManager em;
        MailHandler target;
        final String p = MailHandler.class.getName();
        final LogManager manager = LogManager.getLogManager();
        final Properties props = createInitProperties(p);
        props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
        if (pass != null) {
            props.put(p.concat(".authenticator"), pass);
        }
        props.put(p.concat(".mail.transport.protocol"), "smtp");

        read(manager, props);
        try {
            target = new MailHandler();
            try {
                em = internalErrorManagerFrom(target);
                for (Exception exception : em.exceptions) {
                    dump(exception);
                }
                assertTrue(em.exceptions.isEmpty());
            } finally {
                target.close();
            }

            if (pass != null && !"null".equalsIgnoreCase(pass)) {
                assertNotNull(target.getAuthenticator());
                PasswordAuthentication initPa = passwordAuthentication(
                        target.getAuthenticator(), user);
                assertEquals(user, initPa.getUserName());
                assertEquals(pass, initPa.getPassword());

                target.setAuthenticator(pass.toCharArray());
                PasswordAuthentication setPa = passwordAuthentication(
                        target.getAuthenticator(), user);
                assertEquals(setPa.getUserName(), initPa.getUserName());
                assertEquals(setPa.getPassword(), initPa.getPassword());
            } else {
                assertNull(target.getAuthenticator());
                target.setAuthenticator((char[]) null);
                assertNull(target.getAuthenticator());
            }
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testInitAttachmentFilters() throws Exception {
        InternalErrorManager em;
        MailHandler target;
        final String p = MailHandler.class.getName();
        final LogManager manager = LogManager.getLogManager();
        final Properties props = createInitProperties(p);
        props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());
        props.put(p.concat(".filter"), ErrorFilter.class.getName());
        props.put(p.concat(".attachment.formatters"), SimpleFormatter.class.getName());
        props.put(p.concat(".attachment.names"), Properties.class.getName());
        assertNull(props.getProperty(p.concat(".attachment.filters")));

        read(manager, props);
        try {
            target = new MailHandler();
            try {
                em = internalErrorManagerFrom(target);
                for (Exception exception : em.exceptions) {
                    dump(exception);
                }
                assertTrue(em.exceptions.isEmpty());
            } finally {
                target.close();
            }
        } finally {
            manager.reset();
        }

        assertEquals(ErrorFilter.class, target.getFilter().getClass());
        assertEquals(target.getFilter(), target.getAttachmentFilters()[0]);

        props.put(p.concat(".attachment.formatters"),
                SimpleFormatter.class.getName() + ", "
                + SimpleFormatter.class.getName() + ", "
                + SimpleFormatter.class.getName() + ", "
                + SimpleFormatter.class.getName());
        props.put(p.concat(".attachment.names"), "a.txt, b.txt, c.txt, d.txt");
        props.put(p.concat(".attachment.filters"), "null, "
                + ThrowFilter.class.getName());

        read(manager, props);
        try {
            target = new MailHandler();
            try {
                em = internalErrorManagerFrom(target);
                for (Exception exception : em.exceptions) {
                    final Throwable t = exception;
                    if (t instanceof IndexOutOfBoundsException) {
                        continue;
                    }
                    dump(t);
                }
                assertFalse(em.exceptions.isEmpty());
            } finally {
                target.close();
            }
        } finally {
            manager.reset();
        }

        assertEquals(ErrorFilter.class, target.getFilter().getClass());
        assertNull(target.getAttachmentFilters()[0]);
        assertEquals(ThrowFilter.class,
                target.getAttachmentFilters()[1].getClass());
        assertEquals(target.getFilter(), target.getAttachmentFilters()[2]);
        assertEquals(target.getFilter(), target.getAttachmentFilters()[3]);
    }

    @Test
    public void testInitAttachmentNames() throws Exception {
        InternalErrorManager em;
        MailHandler target;
        final String p = MailHandler.class.getName();
        final LogManager manager = LogManager.getLogManager();
        Properties props = createInitProperties(p);
        props.put(p.concat(".errorManager"), InternalErrorManager.class.getName());

        //test class cast.
        props.put(p.concat(".attachment.formatters"), SimpleFormatter.class.getName());
        props.put(p.concat(".attachment.names"), Properties.class.getName());

        read(manager, props);

        try {
            target = new MailHandler();
            try {
                em = internalErrorManagerFrom(target);
                for (Exception exception : em.exceptions) {
                    dump(exception);
                }
                assertTrue(em.exceptions.isEmpty());
            } finally {
                target.close();
            }

            //test linkage error.
            props.put(p.concat(".attachment.formatters"), SimpleFormatter.class.getName());
            props.put(p.concat(".attachment.names"), SimpleFormatter.class.getName().toUpperCase(Locale.US));

            read(manager, props);

            target = new MailHandler();
            try {
                em = internalErrorManagerFrom(target);
                for (Exception exception : em.exceptions) {
                    dump(exception);
                }
                assertTrue(em.exceptions.isEmpty());
            } finally {
                target.close();
            }

            //test mixed linkage error.
            props.put(p.concat(".attachment.formatters"), SimpleFormatter.class.getName());
            props.put(p.concat(".attachment.names"), Properties.class.getName().toUpperCase(Locale.US));
            read(manager, props);

            target = new MailHandler();
            try {
                em = internalErrorManagerFrom(target);
                for (Exception exception : em.exceptions) {
                    dump(exception);
                }
                assertTrue(em.exceptions.isEmpty());
            } finally {
                target.close();
            }
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testInitErrorManagerException() throws Exception {
        final String encoding = System.getProperty("file.encoding", "8859_1");
        final String p = MailHandler.class.getName();
        final Properties props = createInitProperties(p);
        String key;

        setPending(new RuntimeException());
        try {
            key = p.concat(".errorManager");
            props.put(key, InitErrorManager.class.getName());

            final LogManager manager = LogManager.getLogManager();
            try {
                read(manager, props);
                ByteArrayOutputStream oldErrors = new ByteArrayOutputStream();
                PrintStream newErr = new PrintStream(oldErrors, false, encoding);
                @SuppressWarnings("UseOfSystemOutOrSystemErr")
                final PrintStream err = System.err;
                System.setErr(newErr);
                try {
                    final MailHandler target = new MailHandler();
                    try {
                        System.setErr(err);
                        target.setErrorManager(new ErrorManager());
                    } finally {
                        target.close();
                    }
                } finally {
                    System.setErr(err);
                }

                //java.util.logging.ErrorManager: 4
                //java.lang.reflect.InvocationTargetException
                // at...
                //Caused by: java.lang.RuntimeException
                final String data = oldErrors.toString(encoding);
                assertTrue(data, data.contains(ErrorManager.class.getName()));
                int ite, re;
                ite = data.indexOf(InvocationTargetException.class.getName());
                re = data.indexOf(RuntimeException.class.getName(), ite);
                assertTrue(data, ite < re);
            } finally {
                manager.reset();
            }
        } finally {
            setPending((Throwable) null);
        }
    }

    @Test
    public void testInitErrorManagerError() throws Exception {
        final String encoding = System.getProperty("file.encoding", "8859_1");
        final String p = MailHandler.class.getName();
        final Properties props = createInitProperties(p);
        String key;

        setPending(new Error());
        try {
            key = p.concat(".errorManager");
            props.put(key, InitErrorManager.class.getName());

            final LogManager manager = LogManager.getLogManager();
            try {
                read(manager, props);
                ByteArrayOutputStream oldErrors = new ByteArrayOutputStream();
                PrintStream newErr = new PrintStream(oldErrors, false, encoding);
                @SuppressWarnings("UseOfSystemOutOrSystemErr")
                final PrintStream err = System.err;
                System.setErr(newErr);
                try {
                    final MailHandler target = new MailHandler();
                    try {
                        System.setErr(err);
                        target.setErrorManager(new ErrorManager());
                    } finally {
                        target.close();
                    }
                } finally {
                    System.setErr(err);
                }

                //java.util.logging.ErrorManager: 4
                //java.lang.reflect.InvocationTargetException
                // at...
                //Caused by: java.lang.Error
                final String data = oldErrors.toString(encoding);
                assertTrue(data, data.contains(ErrorManager.class.getName()));
                int ite, re;
                ite = data.indexOf(InvocationTargetException.class.getName());
                re = data.indexOf(Error.class.getName(), ite);
                assertTrue(data, ite < re);
            } finally {
                manager.reset();
            }
        } finally {
            setPending((Throwable) null);
        }
    }

    @Test
    public void testInitError() throws Exception {
        final String p = MailHandler.class.getName();
        final Properties props = createInitProperties(p);
        String filter;
        String name;
        String key;

        setPending(new Error());
        try {
            key = p.concat(".authenticator");
            props.put(key, InitAuthenticator.class.getName());
            testInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".comparator");
            props.put(key, InitComparator.class.getName());
            testInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".filter");
            props.put(key, InitFilter.class.getName());
            testInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".formatter");
            props.put(key, InitFormatter.class.getName());
            testInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".subject");
            props.put(key, InitFormatter.class.getName());
            testInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".attachment.formatters");
            props.put(key, InitFormatter.class.getName());
            testInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".attachment.formatters");
            props.put(key, ThrowFormatter.class.getName());
            filter = p.concat(".attachment.filters");
            props.put(filter, InitFilter.class.getName());
            name = p.concat(".attachment.names");
            props.put(name, "test.txt");
            testInitError(props);
            assertNotNull(props.remove(key));
            assertNotNull(props.remove(name));
            assertNotNull(props.remove(filter));

            key = p.concat(".attachment.formatters");
            props.put(key, ThrowFormatter.class.getName());
            name = p.concat(".attachment.names");
            props.put(name, InitFormatter.class.getName());
            testInitError(props);
            assertNotNull(props.remove(key));
            assertNotNull(props.remove(name));
        } finally {
            setPending((Throwable) null);
        }
    }

    private void testInitError(Properties props) throws Exception {
        testInitException(props);
    }

    @Test
    public void testInitException() throws Exception {
        final String p = MailHandler.class.getName();
        final Properties props = createInitProperties(p);
        String filter;
        String name;
        String key;

        setPending(new RuntimeException());
        try {
            key = p.concat(".authenticator");
            props.put(key, InitAuthenticator.class.getName());
            testInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".comparator");
            props.put(key, InitComparator.class.getName());
            testInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".filter");
            props.put(key, InitFilter.class.getName());
            testInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".formatter");
            props.put(key, InitFormatter.class.getName());
            testInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".subject");
            props.put(key, InitFormatter.class.getName());
            testInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".attachment.formatters");
            props.put(key, InitFormatter.class.getName());
            testInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".attachment.formatters");
            props.put(key, ThrowFormatter.class.getName());
            filter = p.concat(".attachment.filters");
            props.put(filter, InitFilter.class.getName());
            name = p.concat(".attachment.names");
            props.put(name, "test.txt");
            testInitException(props);
            assertNotNull(props.remove(key));
            assertNotNull(props.remove(name));
            assertNotNull(props.remove(filter));

            key = p.concat(".attachment.formatters");
            props.put(key, ThrowFormatter.class.getName());
            name = p.concat(".attachment.names");
            props.put(name, InitFormatter.class.getName());
            testInitException(props);
            assertNotNull(props.remove(key));
            assertNotNull(props.remove(name));
        } finally {
            setPending((Throwable) null);
        }
    }

    private void testInitException(Properties props) throws Exception {
        final LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            final MailHandler target = new MailHandler();
            try {
                InternalErrorManager em = internalErrorManagerFrom(target);
                next:
                for (Exception t : em.exceptions) {
                    for (Throwable cause = t; cause != null; cause = cause.getCause()) {
                        if (cause == getPending()) {
                            continue next;
                        }
                    }
                    dump(t);
                    fail(t.toString());
                }
                assertFalse(em.exceptions.isEmpty());
            } finally {
                target.close();
            }
        } finally {
            manager.reset();
        }
    }

    @Test
    public void testStaticInitErrorManagerException() throws Exception {
        final String encoding = System.getProperty("file.encoding", "8859_1");
        final String test = MailHandlerTest.class.getName();
        final String p = MailHandler.class.getName();
        final Properties props = createInitProperties(p);
        String key;

        setPending(new RuntimeException());
        try {
            key = p.concat(".errorManager");
            props.put(key, test.concat("$StaticInitReErrorManager"));

            final LogManager manager = LogManager.getLogManager();
            try {
                read(manager, props);
                ByteArrayOutputStream oldErrors = new ByteArrayOutputStream();
                PrintStream newErr = new PrintStream(oldErrors, false, encoding);
                @SuppressWarnings("UseOfSystemOutOrSystemErr")
                final PrintStream err = System.err;
                System.setErr(newErr);
                try {
                    final MailHandler target = new MailHandler();
                    try {
                        System.setErr(err);
                        target.setErrorManager(new ErrorManager());
                    } finally {
                        target.close();
                    }
                } finally {
                    System.setErr(err);
                }

                //java.util.logging.ErrorManager: 4
                //java.lang.reflect.InvocationTargetException
                // at ....
                //Caused by: java.lang.ExceptionInInitializerError
                // at...
                //Caused by: java.lang.RuntimeException
                final String data = oldErrors.toString(encoding);
                assertTrue(data, data.contains(ErrorManager.class.getName()));
                int ite, eiie, re;
                ite = data.indexOf(InvocationTargetException.class.getName());
                eiie = data.indexOf(ExceptionInInitializerError.class.getName(), ite);
                if (eiie < 0) {
                    re = data.indexOf(RuntimeException.class.getName(), ite);
                    assertTrue(data, ite < re);
                } else {
                    re = data.indexOf(RuntimeException.class.getName(), eiie);
                    assertTrue(data, ite < eiie);
                    assertTrue(data, eiie < re);
                }
            } finally {
                manager.reset();
            }
            assertNotNull(props.remove(key));
        } finally {
            setPending((Throwable) null);
        }
    }

    @Test
    public void testStaticInitException() throws Exception {
        final String test = MailHandlerTest.class.getName();
        final String p = MailHandler.class.getName();
        final Properties props = createInitProperties(p);
        String filter;
        String name;
        String key;

        setPending(new RuntimeException());
        try {
            key = p.concat(".authenticator");
            props.put(key, test.concat("$StaticInitReAuthenticator"));
            testStaticInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".comparator");
            props.put(key, test.concat("$StaticInitReComparator"));
            testStaticInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".filter");
            props.put(key, test.concat("$StaticInitReFilter"));
            testStaticInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".formatter");
            props.put(key, test.concat("$StaticInitReFormatter"));
            testStaticInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".subject");
            props.put(key, test.concat("$StaticInitReSubjectFormatter"));
            testStaticInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".attachment.formatters");
            props.put(key, test.concat("$StaticInitReAttachFormatter"));
            testStaticInitException(props);
            assertNotNull(props.remove(key));

            key = p.concat(".attachment.formatters");
            props.put(key, ThrowFormatter.class.getName());
            filter = p.concat(".attachment.filters");
            props.put(filter, test.concat("$StaticInitReAttachFilter"));
            name = p.concat(".attachment.names");
            props.put(name, "test.txt");
            testStaticInitException(props);
            assertNotNull(props.remove(key));
            assertNotNull(props.remove(name));
            assertNotNull(props.remove(filter));

            key = p.concat(".attachment.formatters");
            props.put(key, ThrowFormatter.class.getName());
            name = p.concat(".attachment.names");
            props.put(name, test.concat("$StaticInitReNameFormatter"));
            testStaticInitException(props);
            assertNotNull(props.remove(key));
            assertNotNull(props.remove(name));
        } finally {
            setPending((Throwable) null);
        }
    }

    private void testStaticInitException(Properties props) throws Exception {
        testInitException(props);
    }

    @Test
    public void testStaticInitError() throws Exception {
        final String test = MailHandlerTest.class.getName();
        final String p = MailHandler.class.getName();
        final Properties props = createInitProperties(p);
        String filter;
        String name;
        String key;

        setPending(new Error());
        try {
            key = p.concat(".authenticator");
            props.put(key, test.concat("$StaticInitErAuthenticator"));
            testStaticInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".comparator");
            props.put(key, test.concat("$StaticInitErComparator"));
            testStaticInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".errorManager");
            props.put(key, test.concat("$StaticInitErErrorManager"));
            testStaticInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".filter");
            props.put(key, test.concat("$StaticInitErFilter"));
            testStaticInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".formatter");
            props.put(key, test.concat("$StaticInitErFormatter"));
            testStaticInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".subject");
            props.put(key, test.concat("$StaticInitErSubjectFormatter"));
            testStaticInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".attachment.formatters");
            props.put(key, test.concat("$StaticInitErAttachFormatter"));
            testStaticInitError(props);
            assertNotNull(props.remove(key));

            key = p.concat(".attachment.formatters");
            props.put(key, ThrowFormatter.class.getName());
            filter = p.concat(".attachment.filters");
            props.put(filter, test.concat("$StaticInitErAttachFilter"));
            name = p.concat(".attachment.names");
            props.put(name, "test.txt");
            testStaticInitError(props);
            assertNotNull(props.remove(key));
            assertNotNull(props.remove(name));
            assertNotNull(props.remove(filter));

            key = p.concat(".attachment.formatters");
            props.put(key, ThrowFormatter.class.getName());
            name = p.concat(".attachment.names");
            props.put(name, test.concat("$StaticInitErNameFormatter"));
            testStaticInitError(props);
            assertNotNull(props.remove(key));
            assertNotNull(props.remove(name));
        } finally {
            setPending((Throwable) null);
        }
    }

    @SuppressWarnings("ThrowableInitCause")
    private void testStaticInitError(Properties props) throws Exception {
        final LogManager manager = LogManager.getLogManager();
        try {
            read(manager, props);
            MailHandler target = null;
            try {
                target = new MailHandler();
                AssertionError AE = new AssertionError(props.toString());
                AE.initCause(getPending());
                throw AE;
            } catch (AssertionError e) {
                throw e; //avoid catch all.
            } catch (Error expect) {
                assertEquals(Error.class, expect.getClass());
            } finally {
                if (target != null) {
                    target.close();
                }
            }
        } finally {
            manager.reset();
        }
    }

    static Properties createInitProperties(String p) {
        final Properties props = new Properties();
        if (p.length() != 0) {
            p = p.concat(".");
        }
        props.put(p.concat("mail.host"), UNKNOWN_HOST);
        props.put(p.concat("mail.smtp.host"), UNKNOWN_HOST);
        props.put(p.concat("mail.smtp.port"), Integer.toString(OPEN_PORT));
        props.put(p.concat("mail.to"), "");
        props.put(p.concat("mail.cc"), "badAddress");
        props.put(p.concat("mail.from"), "");
        props.put(p.concat("mail.smtp.connectiontimeout"), "1");
        props.put(p.concat("mail.smtp.timeout"), "1");
        props.put(p.concat("errorManager"), InternalErrorManager.class.getName());
        return props;
    }

    @Test
    public void testInitFromLogManager() throws Exception {
        final LogManager manager = LogManager.getLogManager();
        synchronized (manager) {
            try {
                initGoodTest(MailHandler.class, new Class<?>[0], new Object[0]);
                initBadTest(MailHandler.class, new Class<?>[0], new Object[0]);
                initGoodTest(MailHandler.class,
                        new Class<?>[]{Integer.TYPE}, new Object[]{10});
                initBadTest(MailHandler.class,
                        new Class<?>[]{Integer.TYPE}, new Object[]{100});
                initGoodTest(MailHandler.class,
                        new Class<?>[]{Properties.class},
                        new Object[]{new Properties()});
                initBadTest(MailHandler.class,
                        new Class<?>[]{Properties.class},
                        new Object[]{new Properties()});

                //Test subclass properties.
                initGoodTest(MailHandlerExt.class,
                        new Class<?>[0], new Object[0]);
                initBadTest(MailHandlerExt.class,
                        new Class<?>[0], new Object[0]);

                initGoodTest(MailHandlerExt.class,
                        new Class<?>[]{Integer.TYPE}, new Object[]{10});
                initBadTest(MailHandlerExt.class,
                        new Class<?>[]{Integer.TYPE}, new Object[]{100});

                initGoodTest(MailHandlerExt.class,
                        new Class<?>[]{Properties.class},
                        new Object[]{new Properties()});
                initBadTest(MailHandlerExt.class,
                        new Class<?>[]{Properties.class},
                        new Object[]{new Properties()});
            } finally {
                manager.reset();
            }
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

    private void initGoodTest(Class<? extends MailHandler> type,
            Class<?>[] types, Object[] params) throws Exception {

        final String p = type.getName();
        Properties props = createInitProperties(p);
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

        read(LogManager.getLogManager(), props);

        MailHandler h = type.getConstructor(types).newInstance(params);
        assertEquals(10, h.getCapacity());
        assertEquals(Level.ALL, h.getLevel());
        assertEquals(ThrowFilter.class, h.getFilter().getClass());
        assertEquals(XMLFormatter.class, h.getFormatter().getClass());
        assertEquals(Level.WARNING, h.getPushLevel());
        assertEquals(ThrowFilter.class, h.getPushFilter().getClass());
        assertEquals(ThrowComparator.class, h.getComparator().getClass());
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

        InternalErrorManager em = internalErrorManagerFrom(h);
        for (Exception exception : em.exceptions) {
            fail(String.valueOf(exception));
        }
        assertTrue(em.exceptions.isEmpty());

        h.setComparator(null);
        h.close();
        assertEquals(em.exceptions.isEmpty(), true);

        props.put(p.concat(".subject"), freeTextSubject());

        read(LogManager.getLogManager(), props);

        h = type.getConstructor(types).newInstance(params);
        em = internalErrorManagerFrom(h);
        assertTrue(em.exceptions.isEmpty());
        assertEquals(freeTextSubject(), h.getSubject().toString());

        props.remove(p.concat(".attachment.filters"));
        read(LogManager.getLogManager(), props);

        h = type.getConstructor(types).newInstance(params);
        em = internalErrorManagerFrom(h);
        assertTrue(em.exceptions.isEmpty());
        assertEquals(3, h.getAttachmentFormatters().length);
        h.close();

        props.remove(p.concat(".attachment.names"));
        read(LogManager.getLogManager(), props);

        h = type.getConstructor(types).newInstance(params);
        em = internalErrorManagerFrom(h);
        assertTrue(em.exceptions.isEmpty());
        assertEquals(h.getAttachmentFormatters().length, 3);
        h.close();
    }

    private PasswordAuthentication passwordAuthentication(javax.mail.Authenticator auth, String user) {
        final Session s = Session.getInstance(new Properties(), auth);
        return s.requestPasswordAuthentication(null, 25, "SMTP", "", user);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private void initBadTest(Class<? extends MailHandler> type,
            Class<?>[] types, Object[] params) throws Exception {
        final String encoding = System.getProperty("file.encoding", "8859_1");
        final PrintStream err = System.err;
        ByteArrayOutputStream oldErrors = new ByteArrayOutputStream();

        final String p = type.getName();
        Properties props = createInitProperties(p);

        props.put(p.concat(".errorManager"), "InvalidErrorManager");
        props.put(p.concat(".capacity"), "-10");
        props.put(p.concat(".level"), "BAD");
        props.put(p.concat(".formatter"), "InvalidFormatter");
        props.put(p.concat(".filter"), "InvalidFilter");
        props.put(p.concat(".authenticator"), "password");
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

        MailHandler h = null;
        oldErrors.reset();
        System.setErr(new PrintStream(oldErrors, false, encoding));
        try {
            /**
             * Bad level value for property:
             * com.sun.mail.util.logging.MailHandler.level The
             * LogManager.setLevelsOnExistingLoggers triggers an error. This
             * code swallows that error message.
             */
            read(LogManager.getLogManager(), props);
            System.err.print(""); //flushBuffer.
            System.err.flush();
            String result = oldErrors.toString(encoding).trim();
            oldErrors.reset();
            if (result.length() > 0) {
                final String expect = "Bad level value for property: " + p + ".level";
                //if (result.length() > expect.length()) {
                //    result = result.substring(0, expect.length());
                //}
                assertEquals(expect, result);
            }

            /**
             * The default error manager writes to System.err. Since this test
             * is trying to install an invalid ErrorManager we can only capture
             * the error by capturing System.err.
             */
            h = type.getConstructor(types).newInstance(params);
            System.err.flush();
            result = oldErrors.toString(encoding).trim();
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

        assert h != null;
        assertEquals(ErrorManager.class, h.getErrorManager().getClass());
        assertTrue(h.getCapacity() != 10);
        assertTrue(h.getCapacity() != -10);
        assertEquals(Level.WARNING, h.getLevel());
        assertEquals(null, h.getFilter());
        assertEquals(SimpleFormatter.class, h.getFormatter().getClass());
        assertEquals(Level.OFF, h.getPushLevel());
        assertEquals(null, h.getPushFilter());
        assertNull(h.getComparator());
        assertEquals(null, h.getEncoding());
        assertEquals(ThrowFilter.class.getName(), h.getSubject().toString());
        PasswordAuthentication pa = passwordAuthentication(h.getAuthenticator(), "user");
        assertEquals("user", pa.getUserName());
        assertEquals("password", pa.getPassword());
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

    private static boolean isConnectOrTimeout(Throwable t) {
        if (t instanceof MessagingException) {
            Throwable cause = t.getCause();
            if (cause == null) { //GNU JavaMail doesn't support 1.4 chaining.
               cause = ((MessagingException) t).getNextException();
            }
            return isConnectOrTimeout(cause);
        } else if (isInstanceof(t, "com.sun.mail.util.SocketConnectException")) {
            return isConnectOrTimeout(t.getCause());
        } else {
            return t instanceof java.net.ConnectException
                    || t instanceof java.net.UnknownHostException
                    || t instanceof java.net.SocketTimeoutException;
        }
    }

    private static boolean isInstanceof(Object o, String s) {
        if (s == null) {
           throw new NullPointerException();
        }

        if (o != null) {
            for (Class<?> k = o.getClass(); k != null; k = k.getSuperclass()) {
                if (s.equals(k.getClass().getName())) {
                   return true;
                }
            }
        }
        return false;
    }

    private static boolean isNoRecipientAddress(Throwable t) {
        if (t instanceof MessagingException) {
            return String.valueOf(t).contains("No recipient addresses");
        }
        return false;
    }

    private static InternalErrorManager internalErrorManagerFrom(Handler h) {
        final ErrorManager em = h.getErrorManager();
        if (em instanceof InternalErrorManager) {
            return (InternalErrorManager) em;
        }
        throw new ClassCastException(String.valueOf(em));
    }

    /**
     * http://www.iana.org/assignments/port-numbers
     *
     * @return a open dynamic port.
     */
    private static int findOpenPort() {
        final int MAX_PORT = 65535;
        for (int i = 49152; i <= MAX_PORT; ++i) {
            if (checkUnusedPort(i)) {
                return i;
            }
        }

        try {
            close(new Socket("localhost", MAX_PORT));
            return MAX_PORT;
        } catch (Throwable t) { //Config error or fix isConnectOrTimeout method.
            throw new Error("Can't find open port.", t);
        }
    }

    private static boolean checkUnusedPort(int port) {
        try {
            close(new Socket("localhost", port));
        } catch (UnknownHostException UHE) {
            throw new AssertionError(UHE);
        } catch (IOException IOE) {
            return isConnectOrTimeout(IOE);
        }
        return false; //listening.
    }

    private static void close(Socket s) {
        try {
            s.close();
        } catch (IOException ignore) {
        }
    }

    private static abstract class MessageErrorManager extends InternalErrorManager {

        private final Properties props;

        protected MessageErrorManager(final Properties props) {
            if (props == null) {
                throw new NullPointerException();
            }
            this.props = props;
        }

        @Override
        public final void error(String msg, Exception ex, int code) {
            super.error(msg, ex, code);
            if (msg != null && msg.length() > 0
                    && !msg.startsWith(Level.SEVERE.getName())) {
                MimeMessage message;
                try { //Headers can be UTF-8 or US-ASCII.
                    byte[] b = msg.getBytes("UTF-8");
                    assertTrue(b.length > 0);

                    ByteArrayInputStream in = new ByteArrayInputStream(b);
                    Session session = Session.getInstance(props);
                    message = new MimeMessage(session, in);
                    error(message, ex, code);
                } catch (Error e) {
                    throw e;
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

    public static class PushErrorManager extends MessageErrorManager {

        public PushErrorManager(MailHandler h) {
            super(h.getMailProperties());
        }

        protected PushErrorManager(Properties p) {
            super(p);
        }

        @Override
        protected void error(MimeMessage message, Throwable t, int code) {
            try {
                assertNotNull(message.getSentDate());
                assertNotNull(message.getDescription());
                assertNotNull(message.getHeader("X-Priority"));
                assertEquals("2", message.getHeader("X-Priority")[0]);
                assertNotNull(message.getHeader("Importance"));
                assertEquals("High", message.getHeader("Importance")[0]);
                assertNotNull(message.getHeader("Priority"));
                assertEquals("urgent", message.getHeader("Priority")[0]);
                assertEquals("auto-generated", message.getHeader("auto-submitted")[0]);
                message.saveChanges();
            } catch (RuntimeException | MessagingException RE) {
                dump(RE);
                fail(RE.toString());
            }
        }
    }

    public static final class VerifyErrorManager extends PushErrorManager {

        public VerifyErrorManager() {
            super(new Properties());
        }

        @Override
        protected void error(MimeMessage message, Throwable t, int code) {
            super.error(message, t, code);
            try {
                final Locale locale = Locale.getDefault();
                String lang = LogManagerProperties.toLanguageTag(locale);
                if (lang.length() != 0) {
                    assertEquals(lang, message.getHeader("Accept-Language", null));
                } else {
                    assertEquals("", locale.getLanguage());
                }

                Address[] a = message.getRecipients(Message.RecipientType.TO);
                assertEquals(InternetAddress.parse("foo@bar.com")[0], a[0]);
                assertEquals(1, a.length);

                a = message.getRecipients(Message.RecipientType.CC);
                assertEquals(InternetAddress.parse("fizz@buzz.com")[0], a[0]);
                assertEquals(1, a.length);

                a = message.getRecipients(Message.RecipientType.BCC);
                assertEquals(InternetAddress.parse("baz@bar.com")[0], a[0]);
                assertEquals(1, a.length);

                a = message.getFrom();
                assertEquals(InternetAddress.parse("localhost@localdomain")[0], a[0]);
                assertEquals(1, a.length);

                a = new Address[]{message.getSender()};
                assertEquals(InternetAddress.parse("mail@handler")[0], a[0]);

                assertEquals(MailHandler.class.getName() + " test", message.getSubject());

                assertNotNull(message.getHeader("Incomplete-Copy", null));

                assertTrue(message.getContentType(), message.isMimeType("multipart/mixed"));
                Multipart multipart = (Multipart) message.getContent();
                MimePart body = (MimePart) multipart.getBodyPart(0);
                ContentType type = new ContentType(body.getContentType());
                assertEquals("text/plain", type.getBaseType());
                assertEquals("us-ascii", type.getParameter("charset").toLowerCase(Locale.US));
                assertEquals("auto-generated", message.getHeader("auto-submitted")[0]);

                if (lang.length() != 0) {
                    assertEquals(lang, body.getHeader("Accept-Language", null));
                } else {
                    assertEquals("", locale.getLanguage());
                }
            } catch (MessagingException | IOException me) {
                dump(me);
                fail(me.toString());
            }
        }
    }

    public static final class FlushErrorManager extends MessageErrorManager {

        public FlushErrorManager(MailHandler h) {
            super(h.getMailProperties());
        }

        @Override
        protected void error(MimeMessage message, Throwable t, int code) {
            try {
                assertTrue(null != message.getSentDate());
                assertNotNull(message.getDescription());
                assertNull(message.getHeader("X-Priority"));
                assertNull(message.getHeader("Importance"));
                assertNull(message.getHeader("Priority"));
                assertEquals("auto-generated", message.getHeader("auto-submitted")[0]);
                message.saveChanges();
            } catch (RuntimeException | MessagingException RE) {
                dump(RE);
                fail(RE.toString());
            }
        }
    }

    public static class ThrowFilter implements Filter {

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            throw new RuntimeException(record.toString());
        }
    }

    public static final class ThrowComparator
            implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = 8493707928829966353L;

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
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

    public static class UselessComparator
            implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = 7973575043680596722L;

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };

    public static class SequenceComparator
            implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
            long s1 = o1.getSequenceNumber();
            long s2 = o2.getSequenceNumber();
            return s1 < s2 ? -1 : s1 > s2 ? 1 : 0;
        }
    }

    public static class SequenceDescComparator
            implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
            long s1 = o1.getSequenceNumber();
            long s2 = o2.getSequenceNumber();
            return s1 < s2 ? 1 : s1 > s2 ? -1 : 0;
        }
    }

    public static final class SequenceComparatorWithReverse
            implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
            long s1 = o1.getSequenceNumber();
            long s2 = o2.getSequenceNumber();
            return s1 < s2 ? -1 : s1 > s2 ? 1 : 0;
        }

        @SuppressWarnings("override")
        public Comparator<LogRecord> reversed() {
            return new SequenceDescComparator();
        }
    }

    public static class RawTypeComparator
            implements Comparator<Object>, Serializable {

        private static final long serialVersionUID = -6539179106541617400L;

        @SuppressWarnings("override") //JDK-6954234
        public int compare(Object o1, Object o2) {
            long s1 = LogRecord.class.cast(o1).getSequenceNumber();
            long s2 = LogRecord.class.cast(o2).getSequenceNumber();
            return s1 < s2 ? -1 : s1 > s2 ? 1 : 0;
        }
    }

    public static final class ActivationErrorManager extends InternalErrorManager {

        @Override
        public void error(String msg, Exception ex, int code) {
            if (isDataTypeError(msg)) {
                Exception e = new UnsupportedDataTypeException(msg);
                super.error(msg, e, code);
            } else {
                if (!isConnectOrTimeout(ex)) {
                    super.error(msg, ex, code);
                } else {
                    for (Throwable t = ex; t != null; t = t.getCause()) {
                        if (isDataTypeError(t.getMessage())
                                || isDataTypeError(t.getClass().getName())) {
                            super.error(msg, ex, code);
                        }
                    }
                }
            }
        }

        private boolean isDataTypeError(String m) {
            if (m != null) {
                return m.contains("javax.activation.UnsupportedDataTypeException");
            }
            return false;
        }
    }

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

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord r) {
            return value;
        }
    }

    public static final class CountingFilter implements Filter {

        private final Filter result;
        int count;

        public CountingFilter() {
            this.result = BooleanFilter.TRUE;
        }

        public CountingFilter(Filter f) {
            this.result = f;
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord r) {
            ++count;
            return result.isLoggable(r);
        }
    }

    public static final class CountingFormatter extends Formatter {

        int head;
        int tail;
        int format;

        @Override
        public String getHead(Handler h) {
            ++head;
            return "";
        }

        @Override
        public String format(LogRecord record) {
            ++format;
            return String.valueOf(record.getMessage());
        }

        @Override
        public String getTail(Handler h) {
            ++tail;
            return "";
        }
    }

    public static final class HeadFormatter extends Formatter {

        private final String name;

        public HeadFormatter() {
            this((String) null);
        }

        public HeadFormatter(final String name) {
            this.name = name;
        }

        @Override
        public String getHead(Handler h) {
            return name;
        }

        @Override
        public String format(LogRecord record) {
            return "";
        }
    }

    public static class InternalErrorManager extends ErrorManager {

        protected final List<Exception> exceptions = new ArrayList<>();

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
        private final boolean debug;

        public ThrowSecurityManager() {
            debug = isSecurityDebug();
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
            } else {
                if (debug) {
                    securityDebugPrint(se);
                }
            }
        }
    }

    public static final class GaeErrorManager extends MessageErrorManager {

        public GaeErrorManager(MailHandler h) {
            super(h.getMailProperties());
        }

        @Override
        protected void error(MimeMessage message, Throwable t, int code) {
            try {
                assertFalse(LogManagerProperties.hasLogManager());
                String[] a = message.getHeader("auto-submitted");
                assertTrue(Arrays.toString(a), a == null || a.length == 0);
                message.saveChanges();
            } catch (RuntimeException RE) {
                dump(RE);
                fail(RE.toString());
            } catch (Exception ME) {
                dump(ME);
                fail(ME.toString());
            }
        }
    }

    public static final class GaeSecurityManager extends SecurityManager {

        boolean secure = false;
        private final boolean debug;

        public GaeSecurityManager() {
            debug = isSecurityDebug();
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
                final StackTraceElement[] stack = se.getStackTrace();
                if (stack.length == 0) {
                    Assume.assumeNoException(se);
                }
                for (StackTraceElement e : stack) {
                    if (Handler.class.getName().equals(e.getClassName())) {
                        throw se;
                    }
                }
            }
            if (debug) {
                securityDebugPrint(se);
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

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord r1, LogRecord r2) {
            throw new Error("");
        }
    }

    public static class ReentranceFilter implements Filter {

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            if (!getClass().getName().equals(record.getSourceClassName())) {
                final Logger logger = Logger.getLogger(record.getLoggerName());
                logger.logp(Level.SEVERE, getClass().getName(), "isLoggable", toString());
            }
            return true;
        }
    }

    public static class ErrorFilter implements Filter {

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            throw new Error("");
        }
    }

    public static class FlipFlopFilter implements Filter {

        volatile boolean value;

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            return value;
        }
    }

    public static final class InitAuthenticator extends javax.mail.Authenticator {

        public InitAuthenticator() {
            throwPending();
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            throw new NoSuchMethodError();
        }
    }

    public final static class InitFilter implements Filter {

        public InitFilter() {
            throwPending();
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class InitFormatter extends Formatter {

        public InitFormatter() {
            throwPending();
        }

        @Override
        public String format(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class InitComparator
            implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = 1L;

        public InitComparator() {
            throwPending();
        }

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
            throw new NoSuchMethodError();
        }
    }

    public final static class InitErrorManager extends ErrorManager {

        public InitErrorManager() {
            throwPending();
        }
    }

    public final static class InternFilterFormatterComparator extends Formatter
            implements Comparator<LogRecord>, Filter,
            Serializable {

        private static final long serialVersionUID = -7282673499043066003L;

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord lr) {
            return true;
        }

        @Override
        public String format(LogRecord lr) {
            return "";
        }

        @Override
        public boolean equals(Object o) {
            return o == null ? false : getClass().equals(o.getClass());
        }

        @Override
        public int hashCode() {
            return 31 * getClass().hashCode();
        }
    }

    public static class InternBadFilter implements Filter {

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof InternBadFilter; //Not safe.
        }

        @Override
        public int hashCode() {
            return 31 * InternBadFilter.class.hashCode(); //Not safe.
        }
    }

    public final static class InternBadSubFilter extends InternBadFilter {

        @Override
        public boolean isLoggable(LogRecord record) {
            return false;
        }
    }

    public final static class InternFilter implements Filter {

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord lr) {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return o == null ? false : getClass().equals(o.getClass());
        }

        @Override
        public int hashCode() {
            return 31 * getClass().hashCode();
        }
    }

    public final static class InternFilterErrorManager
            extends InternalErrorManager implements Filter {

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord lr) {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return o == null ? false : getClass().equals(o.getClass());
        }

        @Override
        public int hashCode() {
            return 31 * getClass().hashCode();
        }
    }

    public final static class InternFilterFormatter
            extends Formatter implements Filter {

        @Override
        public String format(LogRecord lr) {
            return "";
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord lr) {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            return o == null ? false : getClass().equals(o.getClass());
        }

        @Override
        public int hashCode() {
            return 31 * getClass().hashCode();
        }
    }

    public final static class InternFormatter extends Formatter {

        @Override
        public boolean equals(Object o) {
            return o == null ? false : getClass().equals(o.getClass());
        }

        @Override
        public int hashCode() {
            return 31 * getClass().hashCode();
        }

        @Override
        public String format(LogRecord lr) {
            return "";
        }
    }

    public static final class StaticInitReAuthenticator extends javax.mail.Authenticator {

        static {
            throwPending();
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitReFilter implements Filter {

        static {
            throwPending();
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitReAttachFilter implements Filter {

        static {
            throwPending();
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitReFormatter extends Formatter {

        static {
            throwPending();
        }

        @Override
        public String format(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitReSubjectFormatter extends Formatter {

        static {
            throwPending();
        }

        @Override
        public String format(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitReAttachFormatter extends Formatter {

        static {
            throwPending();
        }

        @Override
        public String format(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitReNameFormatter extends Formatter {

        static {
            throwPending();
        }

        @Override
        public String format(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitReComparator
            implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = 1L;

        static {
            throwPending();
        }

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitReErrorManager extends ErrorManager {

        static {
            throwPending();
        }
    }

    public static final class StaticInitErAuthenticator extends javax.mail.Authenticator {

        static {
            throwPending();
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitErFilter implements Filter {

        static {
            throwPending();
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitErAttachFilter implements Filter {

        static {
            throwPending();
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitErFormatter extends Formatter {

        static {
            throwPending();
        }

        @Override
        public String format(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitErSubjectFormatter extends Formatter {

        static {
            throwPending();
        }

        @Override
        public String format(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitErAttachFormatter extends Formatter {

        static {
            throwPending();
        }

        @Override
        public String format(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitErNameFormatter extends Formatter {

        static {
            throwPending();
        }

        @Override
        public String format(LogRecord record) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitErComparator
            implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = 1L;

        static {
            throwPending();
        }

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
            throw new NoSuchMethodError();
        }
    }

    public final static class StaticInitErErrorManager extends ErrorManager {

        static {
            throwPending();
        }
    }

    private final static class LevelCheckingFormatter extends Formatter {

        private final Level expect;

        public LevelCheckingFormatter(final Level expect) {
            this.expect = expect;
        }

        @Override
        public String format(LogRecord lr) {
            return "";
        }

        @Override
        public String getHead(Handler h) {
            assertEquals(expect, h.getLevel());
            return "";
        }

        @Override
        public String getTail(Handler h) {
            return getHead(h);
        }
    }

    private static class PrintThrowsRuntimeException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        PrintThrowsRuntimeException(Throwable t) {
            super(t);
        }

        @Override
        public void printStackTrace(PrintWriter s) {
            throw this;
        }

        @Override
        public void printStackTrace(PrintStream s) {
            throw this;
        }

        @Override
        public void printStackTrace() {
            throw this;
        }

        @SuppressWarnings("CallToPrintStackTrace")
        public void dump() {
            Throwable t = new Throwable(getClass().getName(), getCause());
            t.setStackTrace(getStackTrace());
            t.printStackTrace();
        }
    }

    private static class CountingUncaughtExceptionHandler
                                implements Thread.UncaughtExceptionHandler {

        int count;

        @SuppressWarnings("override") //JDK-6954234
        public void uncaughtException(Thread t, Throwable e) {
            count++;
        }
    }

    private static class LinkageErrorStream extends PrintStream {

        private final StackTraceElement[] stack;

        LinkageErrorStream() throws IOException {
            this((StackTraceElement[]) null);
        }

        LinkageErrorStream(final StackTraceElement[] stack) throws IOException {
            super(new ByteArrayOutputStream(), false, "UTF-8");
            if (stack != null) {
                this.stack = stack.clone();
            } else {
                this.stack = null;
            }
        }

        @Override
        public void println(String x) {
            setError();
            LinkageError le = new LinkageError(x);
            if (stack != null) {
                le.setStackTrace(stack);
            }
            throw le;
        }
    }

    private static class RuntimeErrorStream extends PrintStream {

        private final StackTraceElement[] stack;

        RuntimeErrorStream() throws IOException {
            this((StackTraceElement[]) null);
        }

        RuntimeErrorStream(final StackTraceElement[] stack) throws IOException {
            super(new ByteArrayOutputStream(), false, "UTF-8");
            if (stack != null) {
                this.stack = stack.clone();
            } else {
                this.stack = null;
            }
        }

        @Override
        public void println(String x) {
            setError();
            PrintThrowsRuntimeException re
                    = new PrintThrowsRuntimeException(new Throwable());
            if (stack != null) {
                re.setStackTrace(stack);
            }
            throw re;
        }
    }

    private final static class LocaleFilter implements Filter {

        private final Locale locale;
        private final boolean allow;

        LocaleFilter(final Locale l, final boolean allow) {
            if (l == null) {
                throw new NullPointerException();
            }
            this.locale = l;
            this.allow = allow;
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            final ResourceBundle rb = record.getResourceBundle();
            return rb == null ? allow : locale.equals(rb.getLocale());
        }
    }

    public static class MailDebugErrorManager extends ErrorManager {

        @Override
        public void error(String msg, Exception ex, int code) {
            try {
                Session session = Session.getInstance(createInitProperties(""));
                session.setDebug(true);
                Message m = new MimeMessage(session);
                m.setFrom();
                m.setRecipient(Message.RecipientType.TO, m.getFrom()[0]);
                m.setText(MailDebugErrorManager.class.getName());
                Transport.send(m);
            } catch (Exception e) {
                super.error(msg, e, code);
            }
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

    public final static class MailHandlerOverride extends MailHandler {

        public MailHandlerOverride() {
            super();
        }

        public MailHandlerOverride(Properties props) {
            super(props);
        }

        public MailHandlerOverride(int capacity) {
            super(capacity);
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            int levelValue = getLevel().intValue();
            if (record.getLevel().intValue() < levelValue
                    || levelValue == Level.OFF.intValue()) {
                return false;
            }

            Filter body = getFilter();
            if (body == null || body.isLoggable(record)) {
                return true;
            }

            final Filter[] filters = this.getAttachmentFilters();
            for (int i = 0; i < filters.length; ++i) {
                final Filter f = filters[i];
                if (f == null || f.isLoggable(record)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final static class ClassLoaderSecurityManager extends SecurityManager {

        volatile boolean secure = false;
        private final boolean debug;

        public ClassLoaderSecurityManager() {
            debug = isSecurityDebug();
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

        @Override
        public void checkRead(String file, Object context) {
        }

        @Override
        public void checkRead(String file) {
        }

        private void checkPermission(java.security.Permission perm, SecurityException se) {
            //Check for set and get context class loader.
            String name = perm.getName();
            if (secure && name.contains("ContextClassLoader")) {
                throw se;
            } else {
                if (debug) {
                    securityDebugPrint(se);
                }
            }
        }
    }

    public final static class ClassLoaderErrorManager extends InternalErrorManager {

        private final ClassLoader expect;

        public ClassLoaderErrorManager() {
            this(LOADER.get());
        }

        public ClassLoaderErrorManager(final ClassLoader expect) {
            this.expect = expect;
        }

        @Override
        public void error(String msg, Exception ex, int code) {
            checkContextClassLoader(expect);
            super.error(msg, ex, code);
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            checkContextClassLoader(expect);
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            checkContextClassLoader(expect);
            return super.hashCode();
        }

        @Override
        public String toString() {
            checkContextClassLoader(expect);
            return super.toString();
        }
    }

    public static final class ClassLoaderAuthenticator
            extends javax.mail.Authenticator {

        private final ClassLoader expect;

        public ClassLoaderAuthenticator() {
            this(LOADER.get());
        }

        ClassLoaderAuthenticator(ClassLoader loader) {
            this.expect = loader;
        }

        @SuppressWarnings("override")
        protected PasswordAuthentication getPasswordAuthentication() {
            checkContextClassLoader(expect);
            for (StackTraceElement se : new Throwable().getStackTrace()) {
                if ("javax.mail.Transport".equals(se.getClassName())
                        && "send".equals(se.getMethodName())) {
                    return null;
                }
            }
            throw new AssertionError("Not calling Transport.send");
        }
    }

    public final static class ClassLoaderComparator
            implements Comparator<LogRecord>, Serializable {

        private static final long serialVersionUID = -1L;

        private final ClassLoader expect;

        public ClassLoaderComparator() {
            this(LOADER.get());
        }

        ClassLoaderComparator(final ClassLoader expect) {
            this.expect = expect;
        }

        @SuppressWarnings("override") //JDK-6954234
        public int compare(LogRecord o1, LogRecord o2) {
            checkContextClassLoader(expect);
            return new SequenceComparator().compare(o1, o2);
        }

        @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass", "override"})
        public boolean equals(Object o) {
            checkContextClassLoader(expect);
            return super.equals(o);
        }

        @SuppressWarnings("override")
        public int hashCode() {
            checkContextClassLoader(expect);
            return super.hashCode();
        }

        @SuppressWarnings("override")
        public String toString() {
            checkContextClassLoader(expect);
            return super.toString();
        }
    }

    public final static class ClassLoaderFilterFormatter
            extends Formatter implements Filter {

        private final ClassLoader expect;
        private final String format;

        public ClassLoaderFilterFormatter() {
            this(LOADER.get());
        }

        ClassLoaderFilterFormatter(final ClassLoader expect) {
            this.expect = expect;
            this.format = "";
        }

        ClassLoaderFilterFormatter(final ClassLoader expect, String format) {
            this.expect = expect;
            this.format = format;
        }

        @Override
        public String getHead(Handler h) {
            checkContextClassLoader(expect);
            return "";
        }

        @Override
        public String getTail(Handler h) {
            checkContextClassLoader(expect);
            return "";
        }

        @Override
        public String format(final LogRecord lr) {
            checkContextClassLoader(expect);
            return format;
        }

        @SuppressWarnings("override") //JDK-6954234
        public boolean isLoggable(LogRecord record) {
            checkContextClassLoader(expect);
            return true;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object o) {
            checkContextClassLoader(expect);
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            checkContextClassLoader(expect);
            return super.hashCode();
        }

        @Override
        public String toString() {
            checkContextClassLoader(expect);
            return super.toString();
        }
    }

    private final static class CloseLogRecord extends LogRecord {

        private static final long serialVersionUID = 1L;
        private transient volatile Handler target;

        CloseLogRecord(Level level, String msg, final Handler target) {
            super(level, msg);
            this.target = target;
        }

        @Override
        public String getSourceMethodName() {
            close();
            return super.getSourceMethodName();
        }

        @Override
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
