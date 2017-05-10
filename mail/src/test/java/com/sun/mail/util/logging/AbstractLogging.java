/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2017 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2016-2017 Jason Mehrens. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Common super class for the logging test suite.
 *
 * @author Jason Mehrens
 * @since JavaMail 1.5.6
 */
abstract class AbstractLogging {

    /**
     * Used to print debug information about the given throwable.
     *
     * @param t the throwable.
     * @throws NullPointerException if given throwable is null.
     */
    @SuppressWarnings({"CallToThreadDumpStack", "CallToPrintStackTrace"})
    static void dump(final Throwable t) {
        t.printStackTrace();
    }

    /**
     * Gets all of the predefined Levels.
     *
     * @return an array of log levels.
     */
    static Level[] getAllLevels() {
        final Field[] fields = Level.class.getFields();
        List<Level> a = new ArrayList<>(fields.length);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())
                    && Level.class.isAssignableFrom(field.getType())) {
                try {
                    a.add((Level) field.get((Object) null));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    fail(ex.toString());
                }
            }
        }
        return a.toArray(new Level[a.size()]);
    }

    /**
     * Determines if the given class is from the JavaMail API Reference
     * Implementation {@code com.sun.mail} package.
     *
     * @param k the type to test.
     * @return true if this is part of reference implementation but not part of
     * the official API spec.
     * @throws Exception if there is a problem.
     */
    final boolean isPrivateSpec(final Class<?> k) throws Exception {
        return isFromJavaMail(k, false);
    }

    /**
     * Reinitialize the logging properties using the given properties.
     *
     * @param manager the log manager.
     * @param props the properties.
     * @throws IOException if there is a problem.
     * @throws NullPointerException if either argument is null.
     */
    final void read(LogManager manager, Properties props) throws IOException {
        //JDK-4810637
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        props.store(out, getClass().getName());
        manager.readConfiguration(new ByteArrayInputStream(out.toByteArray()));
    }

    /**
     * Sets the log record time using milliseconds from the epoch of
     * 1970-01-01T00:00:00Z. Any nanosecond information is set to zero. This
     * method is used to support JDK8 when running on JDK9 or newer.
     *
     * @param record the log record to adjust.
     * @param epochMilli the time in milliseconds from epoch.
     * @throws NullPointerException if the given record is null.
     */
    @SuppressWarnings("deprecation") //See JDK-8144262 and K7091.
    static void setEpochMilli(final LogRecord record, final long epochMilli) {
        record.setMillis(epochMilli);
    }

    /**
     * Sets the log record time using the seconds and nanoseconds of the epoch
     * from 1970-01-01T00:00:00Z.
     *
     * @param record the log record.
     * @param epochSecond the seconds.
     * @param nanoAdjustment the nano seconds.
     * @throws ClassNotFoundException if running on pre JDK 8.
     * @throws NoSuchMethodException if running on JDK 8.
     * @throws Exception if there is a problem.
     */
    static void setEpochSecond(final LogRecord record, final long epochSecond,
            final long nanoAdjustment) throws Exception {
        final Class<?> k = Class.forName("java.time.Instant");
        Method instant = k.getMethod("ofEpochSecond", long.class, long.class);
        Method set = LogRecord.class.getMethod("setInstant", k);
        set.invoke(record, instant.invoke(null, epochSecond, nanoAdjustment));
    }

    /**
     * Determines if the {@code java.time} APIs are available for this JVM.
     *
     * @return true if the time classes can be loaded.
     */
    static boolean hasJavaTimeModule() {
        try {
            Class.forName("java.time.Duration");
            Class.forName("java.time.Instant");
            Class.forName("java.time.ZonedDateTime");
            Class.forName("java.time.ZoneId");
            return true;
        } catch (final ClassNotFoundException | LinkageError notSupported) {
        }
        return false;
    }

    /**
     * Fails if any declared types are outside of the logging-mailhandler.jar.
     * This includes classes from the JavaMail spec.
     *
     * @param k the type to check for dependencies.
     * @throws Exception if there is a problem.
     */
    final void testJavaMailLinkage(final Class<?> k) throws Exception {
        testJavaMailLinkage(k, true);
    }

    /**
     * Fails if any declared types are outside of the logging-mailhandler.jar.
     *
     * @param k the type to check for dependencies.
     * @param includeSpec if true this includes official JavaMail spec classes.
     * @throws Exception if there is a problem.
     */
    final void testJavaMailLinkage(final Class<?> k, final boolean includeSpec)
            throws Exception {
        assertFalse(k.getName(), isFromJavaMail(k, includeSpec));
        for (Annotation an : k.getDeclaredAnnotations()) {
            assertFalse(an.toString(),
                    isFromJavaMail(an.annotationType(), includeSpec));
        }

        for (Method m : k.getDeclaredMethods()) {
            assertFalse(m.getReturnType().getName(),
                    isFromJavaMail(m.getReturnType(), includeSpec));
            for (Class<?> p : m.getParameterTypes()) {
                assertFalse(p.getName(), isFromJavaMail(p, includeSpec));
            }

            for (Class<?> e : m.getExceptionTypes()) {
                assertFalse(e.getName(), isFromJavaMail(e, includeSpec));
            }

            for (Annotation an : m.getDeclaredAnnotations()) {
                assertFalse(an.toString(),
                        isFromJavaMail(an.annotationType(), includeSpec));
            }
        }

        for (Constructor<?> c : k.getDeclaredConstructors()) {
            for (Class<?> p : c.getParameterTypes()) {
                assertFalse(p.getName(), isFromJavaMail(p, includeSpec));
            }

            for (Class<?> e : c.getExceptionTypes()) {
                assertFalse(e.getName(), isFromJavaMail(e, includeSpec));
            }

            for (Annotation an : c.getDeclaredAnnotations()) {
                assertFalse(an.toString(),
                        isFromJavaMail(an.annotationType(), includeSpec));
            }
        }

        for (Field f : k.getDeclaredFields()) {
            for (Annotation an : k.getDeclaredAnnotations()) {
                assertFalse(an.toString(),
                        isFromJavaMail(an.annotationType(), includeSpec));
            }
            assertFalse(f.getName(), isFromJavaMail(f.getType(), includeSpec));
        }
    }

    /**
     * Tests that the private static loadDeclaredClasses method of the given
     * type. Objects used by the MailHandler during a push might require
     * declaring classes to be loaded on create since a push may happen after a
     * class loader is shutdown.
     *
     * @param k the type to check never null.
     * @throws Exception if there is a problem.
     */
    final void testLoadDeclaredClasses(Class<?> k) throws Exception {
        Method m = k.getDeclaredMethod("loadDeclaredClasses");
        assertTrue(Modifier.isStatic(m.getModifiers()));
        assertTrue(Modifier.isPrivate(m.getModifiers()));
        assertEquals(Class[].class, m.getReturnType());
        m.setAccessible(true);
        Class<?>[] named = (Class<?>[]) m.invoke((Object) null);
        assertTrue(named.length != 0);
        HashSet<Class<?>> declared = new HashSet<>(
                Arrays.<Class<?>>asList(k.getDeclaredClasses()));
        for (Class<?> c : named) {
            assertEquals(c.toString(), k, c.getEnclosingClass());
            assertTrue(c.getDeclaredClasses().length == 0);
            declared.remove(c);
        }
        assertTrue(declared.toString(), declared.isEmpty());
    }

    /**
     * Checks that the given class is visible to the LogManager.
     *
     * @param c the class to check.
     * @throws Exception if there is a problem.
     */
    final void testLogManagerModifiers(final Class<?> c) throws Exception {
        assertTrue(Modifier.isPublic(c.getModifiers()));
        assertTrue(Modifier.isPublic(c.getConstructor().getModifiers()));
    }

    /**
     * Checks that the given class is not dependent on the
     * {@code javax.annotation} classes as they are not present in all
     * environments.
     *
     * @param k the class to inspect.
     * @throws Exception if there is a problem.
     */
    final void testNoDependencyOnJavaxAnnotations(Class<?> k) throws Exception {
        for (Method m : k.getDeclaredMethods()) {
            testNoJavaxAnnotation(m);
        }

        for (Field f : k.getDeclaredFields()) {
            testNoJavaxAnnotation(f);
        }

        for (Constructor<?> c : k.getDeclaredConstructors()) {
            testNoJavaxAnnotation(c);
        }

        for (Class<?> i : k.getInterfaces()) {
            testNoJavaxAnnotation(i);
        }

        for (Class<?> d : k.getDeclaredClasses()) {
            testNoDependencyOnJavaxAnnotations(d);
        }
    }

    /**
     * WebappClassLoader.clearReferencesStaticFinal() method will ignore fields
     * that have type names that start with 'java.' or 'javax.'. This test
     * checks that the given class conforms to this rule so it doesn't become a
     * target that will be nullified by the WebappClassLoader.
     *
     * @param c the class to check.
     * @throws Exception if there is a problem.
     */
    final void testWebappClassLoaderFieldNames(Class<?> c) throws Exception {
        for (Field f : c.getDeclaredFields()) {
            Class<?> k = f.getType();
            while (k.isArray()) {
                k = k.getComponentType();
            }

            /**
             * The WebappClassLoader ignores primitives, non-static, and
             * synthetic fields. For the logging API, this test is stricter than
             * what the WebappClassLoader actually clears. This restricts the
             * logging API to standard field types for both static and
             * non-static fields. The idea is to try to stay forward compatible
             * with WebappClassLoader.
             */
            if (f.getName().indexOf('$') < 0 && !k.isPrimitive()
                    && !k.getName().startsWith("java.")
                    && !k.getName().startsWith("javax.")) {
                fail(f.toString());
            }
        }

        for (Class<?> ic : c.getDeclaredClasses()) {
            testWebappClassLoaderFieldNames(ic);
        }
    }

    /**
     * Blocks the current thread until current time has elapsed by one
     * millisecond.
     *
     * @throws InterruptedException if the current thread is interrupted.
     */
    static void tickMilli() throws InterruptedException {
        tickMilli(1L);
    }

    /**
     * Blocks the current thread until current time has elapsed by the given
     * delay in milliseconds.
     *
     * @param delay the number of milliseconds that have to elapse.
     * @throws IllegalArgumentException if the given delay is zero or less.
     * @throws InterruptedException if the current thread is interrupted.
     */
    @SuppressWarnings("SleepWhileInLoop")
    static void tickMilli(long delay) throws InterruptedException {
        if (delay <= 0L) {
            throw new IllegalArgumentException(Long.toString(delay));
        }
        long then = System.currentTimeMillis();
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            long now = System.currentTimeMillis();
            long delta = (now - then);
            if (delta >= delay) {
                return;
            }
            Thread.sleep(delay - delta);
        }
        throw new AssertionError(then + " " + System.currentTimeMillis());
    }

    private boolean isFromJavaMail(Class<?> k, boolean include) throws Exception {
        for (Class<?> t = k; t != null; t = t.getSuperclass()) {
            final String n = t.getName();
            if (n.startsWith("javax.mail.")) {
                return include;
            }

            //Not included with logging-mailhandler.jar.
            if (n.startsWith("com.sun.mail.")
                    && !n.startsWith("com.sun.mail.util.logging.")) {
                return true;
            }
        }
        return false;
    }

    private void testNoJavaxAnnotation(AccessibleObject fm) throws Exception {
        for (Annotation a : fm.getAnnotations()) {
            testNoJavaxAnnotation(a.annotationType());
        }
    }

    private void testNoJavaxAnnotation(Class<?> k) throws Exception {
        for (; k != null; k = k.getSuperclass()) {
            assertFalse(k.toString(),
                    k.getName().startsWith("javax.annotation"));
        }
    }
}
