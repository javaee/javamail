/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.test;

import java.lang.annotation.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;

import org.junit.runners.Suite;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

/**
 * A special test suite that loads each of the test classes
 * in a separate class loader, along with the class under test.
 * This allows the tests to test methods whose behavior depends on
 * the value of a System property that's read at class initialization
 * time; each test can set a different value of the System property
 * and the corresponding class under test will be loaded in a
 * separate class loader. <p>
 *
 * To use this class, create a test suite class:
 *
 * <pre>
 * @RunWith(ClassLoaderSuite.class)
 * @SuiteClasses({ MyTest1.class, MyTest2.class })
 * @TestClass(ClassToTest.class)
 * public class MyTestSuite {
 * }
 * </pre>
 *
 * The MyTest1 and MyTest2 classes are written as normal JUnit
 * test classes.  Set the System property to test in the @BeforeClass
 * method of these classes.
 *
 * @author Bill Shannon
 */

public class ClassLoaderSuite extends Suite {
    /**
     * An annotation to be used on the test suite class to indicate
     * the class under test.  The class is used to find the classpath
     * to allow loading the class under test in a separate class loader.
     * Note that other classes in the same classpath will also be loaded
     * in the separate class loader, along with the test classes.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface TestClass {
	public Class<?> value();
    }

    /**
     * A special class loader that loads classes from its own class path
     * (specified via URLs) before delegating to the parent class loader.
     * This is used to load the test classes in separate class loaders,
     * even though those classes are also loaded in the parent class loader.
     */
    static class TestClassLoader extends URLClassLoader {
	public TestClassLoader(URL[] urls, ClassLoader parent) {
	    super(urls, parent);
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve)
				    throws ClassNotFoundException {
	    Class<?> c = null;
	    try {
		c = findLoadedClass(name);
		if (c != null)
		    return c;
		c = findClass(name);
		if (resolve)
		    resolveClass(c);
	    } catch (ClassNotFoundException cex) {
		c = super.loadClass(name, resolve);
	    }
	    return c;
	}
    }

    /**
     * Constructor.
     */
    public ClassLoaderSuite(Class<?> klass, RunnerBuilder builder)
				throws InitializationError {
	super(builder, klass,
	    reloadClasses(getTestClass(klass), getSuiteClasses(klass)));
    }

    /**
     * Set the thread's context class loader to the class loader
     * for the test class.
     */
    @Override
    protected void runChild(Runner runner, RunNotifier notifier) {
	// XXX - is it safe to assume it's always a ParentRunner?
	ParentRunner<?> pr = (ParentRunner<?>)runner;
	ClassLoader cl = null;
	try {
	    cl = Thread.currentThread().getContextClassLoader();
	    Thread.currentThread().setContextClassLoader(
		pr.getTestClass().getJavaClass().getClassLoader());
	    super.runChild(runner, notifier);
	} finally {
	    Thread.currentThread().setContextClassLoader(cl);
	}
    }

    /**
     * Get the value of the SuiteClasses annotation.
     */
    private static Class<?>[] getSuiteClasses(Class<?> klass)
				throws InitializationError {
	SuiteClasses annotation = klass.getAnnotation(SuiteClasses.class);
	if (annotation == null)
	    throw new InitializationError("class '" + klass.getName() +
		"' must have a SuiteClasses annotation");
	return annotation.value();
    }

    /**
     * Get the value of the TestClass annotation.
     */
    private static Class<?> getTestClass(Class<?> klass)
				throws InitializationError {
	TestClass annotation = klass.getAnnotation(TestClass.class);
	if (annotation == null)
	    throw new InitializationError("class '" + klass.getName() +
		"' must have a TestClass annotation");
	return annotation.value();
    }

    /**
     * Reload the classes in a separate class loader.
     */
    private static Class<?>[] reloadClasses(Class<?> testClass,
			Class<?>[] suiteClasses) throws InitializationError {
	URL[] urls = new URL[] {
	    classpathOf(testClass),
	    classpathOf(ClassLoaderSuite.class)
	};
	Class<?> sc = null;
	try {
	    for (int i = 0; i < suiteClasses.length; i++) {
		sc = suiteClasses[i];
		ClassLoader cl = new TestClassLoader(urls,
		    ClassLoaderSuite.class.getClassLoader());
		suiteClasses[i] = cl.loadClass(sc.getName());
	    }
	    return suiteClasses;
	} catch (ClassNotFoundException cex) {
	    throw new InitializationError("could not reload class: " + sc);
	}
    }

    /**
     * Return the classpath entry used to load the named resource.
     * XXX - Only handles file: and jar: URLs.
     */
    private static URL classpathOf(Class<?> c) {
	String name = "/" + c.getName().replace('.', '/') + ".class";
	try {
	    URL url = ClassLoaderSuite.class.getResource(name);
	    if (url.getProtocol().equals("file")) {
		String file = url.getPath();
		if (file.endsWith(name))	// has to be true?
		    file = file.substring(0, file.length() - name.length() + 1);
//System.out.println("file URL " + url + " has CLASSPATH " + file);
		return new URL("file", null, file);
	    } else if (url.getProtocol().equals("jar")) {
		String file = url.getPath();
		int i = file.lastIndexOf('!');
		if (i >= 0)
		    file = file.substring(0, i);
//System.out.println("jar URL " + url + " has CLASSPATH " + file);
		return new URL(file);
	    } else
		return url;
	} catch (MalformedURLException mex) {
	    return null;
	}
    }
}
