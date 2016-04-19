/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2016 Jason Mehrens. All rights reserved.
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
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
        List<Level> a = new ArrayList<Level>(fields.length);
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())
                    && Level.class.isAssignableFrom(field.getType())) {
                try {
                    a.add((Level) field.get((Object) null));
                } catch (IllegalArgumentException ex) {
                    fail(ex.toString());
                } catch (IllegalAccessException ex) {
                    fail(ex.toString());
                }
            }
        }
        return a.toArray(new Level[a.size()]);
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
     * Fails if any declared types are outside of the logging-mailhandler.jar.
     *
     * @param k the type to check for dependencies.
     * @throws Exception if there is a problem.
     */
    final void testJavaMailLinkage(final Class<?> k) throws Exception {
        assertFalse(k.getName(), isFromJavaMail(k));
        for (Annotation an : k.getDeclaredAnnotations()) {
            assertFalse(an.toString(), isFromJavaMail(an.annotationType()));
        }

        for (Method m : k.getDeclaredMethods()) {
            assertFalse(m.getReturnType().getName(),
                    isFromJavaMail(m.getReturnType()));
            for (Class<?> p : m.getParameterTypes()) {
                assertFalse(p.getName(), isFromJavaMail(p));
            }

            for (Class<?> e : m.getExceptionTypes()) {
                assertFalse(e.getName(), isFromJavaMail(e));
            }

            for (Annotation an : m.getDeclaredAnnotations()) {
                assertFalse(an.toString(), isFromJavaMail(an.annotationType()));
            }
        }

        for (Constructor<?> c : k.getDeclaredConstructors()) {
            for (Class<?> p : c.getParameterTypes()) {
                assertFalse(p.getName(), isFromJavaMail(p));
            }

            for (Class<?> e : c.getExceptionTypes()) {
                assertFalse(e.getName(), isFromJavaMail(e));
            }

            for (Annotation an : c.getDeclaredAnnotations()) {
                assertFalse(an.toString(), isFromJavaMail(an.annotationType()));
            }
        }

        for (Field f : k.getDeclaredFields()) {
            for (Annotation an : k.getDeclaredAnnotations()) {
                assertFalse(an.toString(), isFromJavaMail(an.annotationType()));
            }
            assertFalse(f.getName(), isFromJavaMail(f.getType()));
        }
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
    static void tick() throws InterruptedException {
        tick(1L);
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
    static void tick(long delay) throws InterruptedException {
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
}
