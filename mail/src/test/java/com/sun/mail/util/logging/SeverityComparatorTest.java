/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2013-2017 Jason Mehrens. All rights reserved.
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileLockInterruptionException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jason Mehrens
 * @since JavaMail 1.5.2
 */
public class SeverityComparatorTest extends AbstractLogging {

    @Test
    public void testDeclaredClasses() throws Exception {
        Class<?>[] declared = SeverityComparator.class.getDeclaredClasses();
        assertEquals(Arrays.toString(declared), 0, declared.length);
    }

    @Test
    public void testApplyNull() {
        SeverityComparator a = new SeverityComparator();
        assertNull(a.apply(null));
    }

    @Test
    public void testApplyErrorByNormal() {
        SeverityComparator a = new SeverityComparator();
        Throwable next = this.headIeChain(new AssertionError());
        next = new Error(next);
        next = new NoClassDefFoundError().initCause(next);
        Throwable reduced = a.apply(next);
        assertEquals(Error.class, reduced.getClass());
    }

    @Test(timeout = 30000)
    public void testApplyEvil() {
        SeverityComparator a = new SeverityComparator();
        final int len = 7;
        final Class<? extends Throwable> type = Exception.class;
        Throwable l = a.apply(createEvilThrowable(type, len));
        assertEquals(type, l.getClass());
    }

    @Test
    public void testApplyProxyNormal() {
        //UTE->IE->AE = IE
        SeverityComparator a = new SeverityComparator();
        Throwable next = this.headIeChain(new AssertionError());
        next = new UndeclaredThrowableException(next);
        Throwable reduced = a.apply(next);
        assertEquals(InterruptedException.class, reduced.getClass());
    }

    @Test
    public void testApplyReflectNormal() {
        //ITE->IE->AE = IE
        SeverityComparator a = new SeverityComparator();
        Throwable next = this.headIeChain(new AssertionError());
        next = new InvocationTargetException(next);
        Throwable reduced = a.apply(next);
        assertEquals(InterruptedException.class, reduced.getClass());
    }

    @Test
    public void testApplyNormalByNormal() {
        //EE->IIOE->E->IE->AE = IE
        SeverityComparator a = new SeverityComparator();
        Throwable next = this.headIeChain(new AssertionError());
        next = new Error(next);
        next = this.headIioeChain(next);
        next = new ExecutionException(next);
        Throwable reduced = a.apply(next);
        assertEquals(InterruptedException.class, reduced.getClass());
    }

    @Test
    public void testApplyFindsRootCause() {
        /**
         * Lots of frameworks wrap exceptions with runtime exceptions. The root
         * cause is always more important.
         */
        SeverityComparator a = new SeverityComparator();
        final Throwable root = new Exception();
        Throwable next = root;
        for (int i = 0; i < 7; i++) {
            next = new RuntimeException(next);
        }
        Throwable reduced = a.apply(next);
        assertEquals(root, reduced);
    }

    @Test
    public void testCompareThrownDoesNotApply() {
        SeverityComparator a = new SeverityComparator();
        Throwable tc1 = new Error(new Exception());
        Throwable tc2 = new Exception(new Exception());
        assertTrue(a.compareThrowable(tc1, tc2) > 0);
        assertTrue(a.compareThrowable(tc2, tc1) < 0);

        tc1 = new RuntimeException(tc1);
        assertTrue(a.compareThrowable(tc1, tc2) > 0);
        assertTrue(a.compareThrowable(tc2, tc1) < 0);

    }

    @Test
    public void testCompareThrownNull() {
        SeverityComparator a = new SeverityComparator();
        assertEquals(0, a.compareThrowable((Throwable) null, (Throwable) null));
        assertTrue(a.compareThrowable(new Throwable(), (Throwable) null) > 0);
        assertTrue(a.compareThrowable((Throwable) null, new Throwable()) < 0);
    }

    @Test
    public void testApplyThenCompareNull() {
        SeverityComparator a = new SeverityComparator();
        assertEquals(0, a.applyThenCompare((Throwable) null, (Throwable) null));
    }

    @Test(timeout = 30000)
    public void testApplyThenCompareThrownEvilError() {
        testApplyThenCompareThrownEvil(Error.class);
    }

    @Test(timeout = 30000)
    public void testApplyThenCompareThrownEvilRuntimeException() {
        testApplyThenCompareThrownEvil(RuntimeException.class);
    }

    @Test(timeout = 30000)
    public void testApplyThenCompareThrownEvilException() {
        testApplyThenCompareThrownEvil(Exception.class);
    }

    private void testApplyThenCompareThrownEvil(Class<? extends Throwable> t) {
        SeverityComparator a = new SeverityComparator();
        final int len = 7;
        Throwable l = createEvilThrowable(t, len);
        Throwable r = createEvilThrowable(t, len);
        assertEquals(0, a.applyThenCompare(l, r));
    }

    @Test(timeout = 30000)
    public void testCompareThrownEvilError() {
        testCompareThrownEvil(Error.class);
    }

    @Test(timeout = 30000)
    public void testCompareThrownEvilRuntimeException() {
        testCompareThrownEvil(RuntimeException.class);
    }

    @Test(timeout = 30000)
    public void testCompareThrownEvilException() {
        testCompareThrownEvil(Exception.class);
    }

    private void testCompareThrownEvil(Class<? extends Throwable> t) {
        SeverityComparator a = new SeverityComparator();
        final int len = 7;
        Throwable l = createEvilThrowable(t, len);
        Throwable r = createEvilThrowable(t, len);
        assertEquals(0, a.compareThrowable(l, r));
    }

    private Throwable createEvilThrowable(Class<? extends Throwable> t, int len) {
        Throwable tail = create(t, null);
        Throwable head = tail;
        for (int i = 1; i < len; i++) {
            head = create(t, head);
        }
        tail.initCause(head); //Pure evil.
        return head;
    }

    @Test
    public void testGetInstance() {
        SeverityComparator a = new SeverityComparator();
        assertEquals(a, SeverityComparator.getInstance());
        assertEquals(a.getClass(), SeverityComparator.getInstance().getClass());
        assertEquals(a.hashCode(), SeverityComparator.getInstance().hashCode());
    }

    @Test
    public void testIsNormalError() {
        testIsNotNormal(Error.class, headIeChain(null));
    }

    @Test
    public void testIsNormalRuntimeException() {
        testIsNotNormal(RuntimeException.class, headIeChain(null));
    }

    @Test
    public void testIsNormalException() {
        testIsNotNormal(Exception.class, headIeChain(null));
    }

    private void testIsNotNormal(Class<? extends Throwable> t, Throwable root) {
        SeverityComparator a = new SeverityComparator();
        assertFalse(a.isNormal(create(t, null)));

        Throwable next = root;
        for (int i = 0; i < 5; i++) {
            next = create(t, next);
            assertFalse(a.isNormal(next));
        }
    }

    @Test
    public void testIsNormal() {
        SeverityComparator a = new SeverityComparator();
        assertTrue(a.isNormal(this.headIeChain(null)));
        assertTrue(a.isNormal(this.headIioeChain(null)));
        assertTrue(a.isNormal(this.headCbieChain(null)));
        assertTrue(a.isNormal(this.headFlieChain(null)));
        assertTrue(a.isNormal(this.headSubIeChain(null)));
        assertTrue(a.isNormal(this.headSubIioeChain(null)));
        assertTrue(a.isNormal(this.headSubTdChain(null)));
        assertTrue(a.isNormal(this.headSubTdChain(null)));
        assertTrue(a.isNormal(this.headWidChain(null)));
        assertTrue(a.isNormal(this.headWitChain(null)));
    }

    @Test
    public void testIsNormalEvil() {
        SeverityComparator a = new SeverityComparator();
        assertFalse(a.isNormal(createEvilThrowable(Exception.class, 7)));
    }

    @Test
    public void testIsNormalDoesNotUseApply() {
        SeverityComparator a = new SeverityComparator();
        Throwable next = this.headIeChain(new AssertionError());
        next = new Error(next);
        next = this.headIioeChain(next);
        next = new ExecutionException(next);
        assertFalse(a.isNormal(next));
    }

    @Test
    public void testIsNormalNull() {
        SeverityComparator a = new SeverityComparator();
        assertFalse(a.isNormal((Throwable) null));
    }

    @Test(expected = NullPointerException.class)
    public void testCompareNullAndNull() {
        SeverityComparator a = new SeverityComparator();
        a.compare((LogRecord) null, (LogRecord) null);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareNotNullAndNull() {
        SeverityComparator a = new SeverityComparator();
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        a.compare(r1, (LogRecord) null);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareNullAndNotNull() {
        SeverityComparator a = new SeverityComparator();
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        a.compare((LogRecord) null, r1);
    }

    @Test
    public void testLeadingErrorAndInterrupted() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        swapSeq(r1, r2);
        assertEquals(r1.getLevel(), r2.getLevel());
        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());

        r1.setThrown(headIeChain(new Error()));
        r2.setThrown(new Error(headIeChain(null)));
        assertRecordLessThan(r1, r2);

        r1.setThrown(headIeChain(new RuntimeException()));
        r2.setThrown(new Error(headIeChain(null)));
        assertRecordLessThan(r1, r2);

        r1.setThrown(headIeChain(new Exception()));
        r2.setThrown(new Error(headIeChain(null)));
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByLevel() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.WARNING, Level.WARNING.toString());
        assertTrue(r1.getSequenceNumber() < r2.getSequenceNumber());

        //Level test with null thrown.
        assertRecordLessThan(r1, r2);

        //Ensure Error doesn't change primary order.
        r1.setThrown(new Error());
        r2.setThrown(null);
        assertRecordLessThan(r1, r2);

        assertEquals(Error.class, r1.getThrown().getClass());
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertEquals(Error.class, r1.getThrown().getClass());
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertEquals(Error.class, r1.getThrown().getClass());
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);

        assertEquals(Error.class, r1.getThrown().getClass());
        r2.setThrown(new InterruptedException());
        assertRecordLessThan(r1, r2);

        //Ensure RuntimeException doesn't change primary order.
        r1.setThrown(new RuntimeException());
        r2.setThrown(null);
        assertRecordLessThan(r1, r2);

        assertEquals(RuntimeException.class, r1.getThrown().getClass());
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertEquals(RuntimeException.class, r1.getThrown().getClass());
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertEquals(RuntimeException.class, r1.getThrown().getClass());
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);

        assertEquals(RuntimeException.class, r1.getThrown().getClass());
        r2.setThrown(new InterruptedException());
        assertRecordLessThan(r1, r2);

        //Ensure Exception doesn't change primary order.
        r1.setThrown(new Exception());
        r2.setThrown(null);
        assertRecordLessThan(r1, r2);

        assertEquals(Exception.class, r1.getThrown().getClass());
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertEquals(Exception.class, r1.getThrown().getClass());
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertEquals(Exception.class, r1.getThrown().getClass());
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);

        assertEquals(Exception.class, r1.getThrown().getClass());
        r2.setThrown(new InterruptedException());
        assertRecordLessThan(r1, r2);

        //Ensure thrown and sequence doesn't change primary order.
        swapSeq(r1, r2);
        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        assertRecordLessThan(r1, r2);

        r1.setThrown(null);
        r2.setThrown(null);
        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByNullAndFrameworkInterrupted() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        assertEquals(r1.getLevel(), r2.getLevel());

        //Ensure Interrupted is less than null.
        r1.setThrown(this.headWidChain(null));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(this.headWidChain(new Error()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(this.headWidChain(new RuntimeException()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(this.headWidChain(new Exception()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByNullAndInterrupted() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        assertEquals(r1.getLevel(), r2.getLevel());

        //Ensure Interrupted is less than null.
        r1.setThrown(headIeChain(null));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headIeChain(new Error()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headIeChain(new RuntimeException()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headIeChain(new Exception()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByNullAndInterruptedSub() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        assertEquals(r1.getLevel(), r2.getLevel());
        //Ensure subclass of IE is less than null.
        r1.setThrown(headSubIeChain(null));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headSubIeChain(new Error()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headSubIeChain(new RuntimeException()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headSubIeChain(new Exception()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByNullAndInterruptedIo() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        assertEquals(r1.getLevel(), r2.getLevel());

        //Esure subclass of IOE is less than null.
        r1.setThrown(headIioeChain(null));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headIioeChain(new Error()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headIioeChain(new RuntimeException()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headIioeChain(new Exception()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByNullAndInterruptedIoSub() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        assertEquals(r1.getLevel(), r2.getLevel());

        //Esure subclass of IOE is less than null.
        r1.setThrown(headSubIioeChain(null));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headSubIioeChain(new Error()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headSubIioeChain(new RuntimeException()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headSubIioeChain(new Exception()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByNullAndThreadDeath() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        assertEquals(r1.getLevel(), r2.getLevel());

        //Ensure ThreadDeath is less than null.
        r1.setThrown(headTdChain(null));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headTdChain(new Error()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headTdChain(new RuntimeException()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headTdChain(new Exception()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByNullAndThreadDeathSub() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        assertEquals(r1.getLevel(), r2.getLevel());

        //Ensure subclass ThreadDeath is less than null.
        r1.setThrown(headSubTdChain(null));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headSubTdChain(new Error()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headSubTdChain(new RuntimeException()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);

        r1.setThrown(headSubTdChain(new Exception()));
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByNullAndThrown() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        r1.setSequenceNumber(r2.getSequenceNumber());
        setEpochMilli(r2, System.currentTimeMillis()); //Truncate nanos.
        setEpochMilli(r1, r2.getMillis());

        assertEquals(r1.getLevel(), r2.getLevel());
        assertEquals(r1.getSequenceNumber(), r2.getSequenceNumber());
        assertEquals(r1.getMillis(), r2.getMillis());

        //Ensure null is less than Error.
        assertNull(r1.getThrown());
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        //Ensure null is less than RuntimeException.
        assertNull(r1.getThrown());
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        //Ensure null is less than Exception.
        assertNull(r1.getThrown());
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByNullAndNullSeq() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());

        assertTrue(r1.getSequenceNumber() < r2.getSequenceNumber());
        assertNull(r1.getThrown());
        assertNull(r2.getThrown());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByThrownAndThrownSeq() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());

        assertTrue(r1.getSequenceNumber() < r2.getSequenceNumber());
        assertTrue(r1.getSequenceNumber() < r2.getSequenceNumber());
        r1.setThrown(new Error());
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() < r2.getSequenceNumber());
        r1.setThrown(new RuntimeException());
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() < r2.getSequenceNumber());
        r1.setThrown(new Exception());
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByThrownLenSeq() {
        final int MAX_RUNS = 10;
        final int MAX_LEN = 20;

        for (int r = MAX_RUNS; r > 0; r--) {
            for (int i = 0; i < MAX_LEN; i++) {
                testByThrownLenSeq(Error.class, i, r);
            }

            for (int i = 0; i < MAX_LEN; i++) {
                testByThrownLenSeq(RuntimeException.class, i, r);
            }

            for (int i = 0; i < MAX_LEN; i++) {
                testByThrownLenSeq(Exception.class, i, r);
            }
        }
    }

    @Test
    public void testByNormalLenSeq() {
        final int MAX_RUNS = 10;
        final int MAX_LEN = 20;

        for (int r = MAX_RUNS; r > 0; r--) {
            //Null is higher than normal so we have to start at one.
            for (int i = 1; i <= MAX_LEN; i++) {
                testByThrownLenSeq(InterruptedException.class, i, r);
            }

            for (int i = 1; i <= MAX_LEN; i++) {
                testByThrownLenSeq(InterruptedIOException.class, i, r);
            }

            for (int i = 1; i <= MAX_LEN; i++) {
                testByThrownLenSeq(ThreadDeath.class, i, r);
            }
        }
    }

    private void testByThrownLenSeq(
            Class<? extends Throwable> t, int c1, int c2) {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());

        assertTrue(r1.getSequenceNumber() < r2.getSequenceNumber());

        setThrown(r1, t, c1);
        setThrown(r2, t, c2);
        assertRecordLessThan(r1, r2);
    }

    private Throwable create(Class<? extends Throwable> type, Throwable cause) {
        try {
            if (cause == null) {
                return type.getConstructor().newInstance();
            }

            try {
                return type.getConstructor(Throwable.class).newInstance(cause);
            } catch (NoSuchMethodException tryInitCause) {
                Throwable next = type.getConstructor().newInstance();
                return next.initCause(cause);
            }
        } catch (InstantiationException ex) {
            throw new AssertionError(ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        } catch (IllegalArgumentException ex) {
            throw new AssertionError(ex);
        } catch (InvocationTargetException ex) {
            throw new AssertionError(ex);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        }
    }

    private void setThrown(LogRecord r, Class<? extends Throwable> t, int d) {
        for (int i = 0; i < d; i++) {
            r.setThrown(create(t, r.getThrown()));
        }
    }

    @Test
    public void testByThrownAndInterrupted() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        swapSeq(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headIeChain(null));
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headIeChain(null));
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headIeChain(null));
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headIeChain(new Error()));
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headIeChain(new RuntimeException()));
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headIeChain(new Exception()));
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByThrownAndInterruptedSub() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        swapSeq(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubIeChain(null));
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubIeChain(null));
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubIeChain(null));
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubIeChain(new Error()));
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubIeChain(new RuntimeException()));
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubIeChain(new Exception()));
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByThrownAndInterruptedIoSub() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        swapSeq(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubIioeChain(null));
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubIioeChain(null));
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubIioeChain(null));
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByThrownAndThreadDeath() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        swapSeq(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headTdChain(null));
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headTdChain(null));
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headTdChain(null));
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headTdChain(new Error()));
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headTdChain(new RuntimeException()));
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headTdChain(new Exception()));
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testByThrownAndThreadDeathSub() {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        swapSeq(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubTdChain(null));
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubTdChain(null));
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubTdChain(null));
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubTdChain(new Error()));
        r2.setThrown(new Error());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubTdChain(new RuntimeException()));
        r2.setThrown(new RuntimeException());
        assertRecordLessThan(r1, r2);

        assertTrue(r1.getSequenceNumber() > r2.getSequenceNumber());
        r1.setThrown(headSubTdChain(new Exception()));
        r2.setThrown(new Exception());
        assertRecordLessThan(r1, r2);
    }

    @Test
    public void testOfClassLoadReadFailed() {
        /**
         * When exceptions triggers errors that is more important than the root
         * cause.
         */
        Throwable clrf = new NoClassDefFoundError().initCause(
                new ClassNotFoundException("", new IOException()));

        Throwable readFailed = new IOException();

        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());

        r1.setThrown(readFailed);
        r2.setThrown(clrf);
        assertRecordLessThan(r1, r2);

        r1.setThrown(clrf);
        r2.setThrown(readFailed);
        assertRecordLessThan(r2, r1);
    }

    @Test
    public void testOfClassLoadInterrupted() {
        /**
         * When exceptions triggers errors that is more important than the root
         * cause.
         */
        Throwable cli = new NoClassDefFoundError().initCause(
                new ClassNotFoundException("", headIioeChain(null)));

        Throwable iioe = headIioeChain(null);

        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());

        r1.setThrown(iioe);
        r2.setThrown(cli);
        assertRecordLessThan(r1, r2);

        r1.setThrown(cli);
        r2.setThrown(iioe);
        assertRecordLessThan(r2, r1);
    }

    @Test
    public void testOfNpeVsIoe() {
        Throwable readFailed = new IOException();
        Throwable npe = new IOException().initCause(new NullPointerException());

        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());

        r1.setThrown(readFailed);
        r2.setThrown(npe);
        assertRecordLessThan(r1, r2);

        r1.setThrown(npe);
        r2.setThrown(readFailed);
        assertRecordLessThan(r2, r1);
    }

    @Test
    public void testArraySort() {
        testArraySort(new SeverityComparator());
    }

    @Test
    public void testReverseArraySort() {
        testArraySort(Collections.reverseOrder(new SeverityComparator()));
    }

    private void testArraySort(Comparator<LogRecord> sc) {
        LogRecord[] r = createRecords();
        LogRecord[] copy = r.clone();
        Arrays.sort(copy, sc);
        assertTrue(r.length > 1);

        //Ensure createRecords is not already sorted.
        int orderChanged = 0;
        for (int i = 0; i < r.length; i++) {
            if (sc.compare(r[i], copy[i]) != 0) {
                ++orderChanged;
            }
        }
        assertTrue(String.valueOf(orderChanged), orderChanged > r.length / 2);

        //Ensure sorted.
        for (int i = 0; i < r.length - 1; i++) {
            if (sc.compare(copy[i], copy[i + 1]) > 0) {
                fail(toString(copy[i], copy[i + 1]));
            }
        }
    }

    @Test
    public void testListSort() {
        testListSort(new SeverityComparator());
    }

    @Test
    public void testReverseListSort() {
        testListSort(Collections.reverseOrder(new SeverityComparator()));
    }

    private void testListSort(Comparator<LogRecord> sc) {
        List<LogRecord> a = Arrays.asList(createRecords());
        Collections.sort(a, sc);

        List<LogRecord> b = serialClone(a);
        Collections.sort(b, sc);

        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            LogRecord r1 = a.get(i);
            LogRecord r2 = b.get(i);
            if (sc.compare(r1, r2) != 0) {
                throw new AssertionError(toString(i, r1, r2));
            }
        }
    }

    @Test
    public void testTreeMap() {
        testMap(new TreeMap<LogRecord, Boolean>(new SeverityComparator()));
    }

    @Test
    public void testReverseTreeMap() {
        testMap(new TreeMap<LogRecord, Boolean>(Collections
                .reverseOrder(new SeverityComparator())));
    }

    private void testMap(SortedMap<LogRecord, Boolean> m) {
        assertTrue(m.isEmpty());

        for (LogRecord r : createRecords()) {
            assertNull(m.put(r, Boolean.TRUE));
        }

        SortedMap<LogRecord, Boolean> copy = serialClone(m);
        Iterator<LogRecord> keys = copy.keySet().iterator();
        assertTrue(keys.hasNext());
        int i = 0;
        for (LogRecord r1 : m.keySet()) {
            LogRecord r2 = keys.next();
            if (m.comparator().compare(r1, r2) != 0) {
                throw new AssertionError(toString(i, r1, r2));
            }
            ++i;
        }

        //Don't use containsAll.
        assertFalse(m.isEmpty());
        for (LogRecord r : m.keySet()) {
            if (!copy.containsKey(r)) {
                throw new AssertionError(toString(r));
            }
        }

        //Don't use containsAll.
        assertFalse(copy.isEmpty());
        for (LogRecord r : copy.keySet()) {
            if (!m.containsKey(r)) {
                throw new AssertionError(toString(r));
            }
        }
    }

    @Test
    public void testPriorityQueue() {
        SeverityComparator sc = new SeverityComparator();
        LogRecord[] rs = createRecords();
        PriorityQueue<LogRecord> q1
                = new PriorityQueue<>(rs.length, sc);
        Collections.addAll(q1, rs);
        PriorityQueue<LogRecord> q2 = serialClone(q1);

        assertFalse(q1.isEmpty());
        assertFalse(q2.isEmpty());

        for (int i = 0; i < rs.length; i++) {
            LogRecord r1 = q1.poll();
            LogRecord r2 = q2.poll();
            if (sc.compare(r1, r2) != 0) {
                throw new AssertionError(toString(i, r1, r2));
            }
        }

        assertTrue(q1.isEmpty());
        assertTrue(q2.isEmpty());
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEquals() {
        final SeverityComparator a = new SeverityComparator();
        final SeverityComparator b = new SeverityComparator();
        assertNotSame(a, b);

        //NPE checks.
        assertNotNull(a);
        assertFalse(a.equals(null));

        assertNotNull(b);
        assertFalse(b.equals(null));

        //Reflexive test.
        assertTrue(a.equals(a));
        assertTrue(b.equals(b));

        //Transitive test.
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    public void testHashCode() {
        final SeverityComparator a = new SeverityComparator();
        final SeverityComparator b = new SeverityComparator();
        assertNotSame(a, b);

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));

        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testToString() {
        final SeverityComparator a = new SeverityComparator();
        assertNotNull(a.toString());
        assertEquals(a.toString(), a.toString());
    }

    @Test
    public void testJavaMailLinkage() throws Exception {
        testJavaMailLinkage(SeverityComparator.class);
    }

    @Test
    public void testLogManagerModifiers() throws Exception {
        testLogManagerModifiers(SeverityComparator.class);
    }

    @Test
    public void testWebappClassLoaderFieldNames() throws Exception {
        testWebappClassLoaderFieldNames(SeverityComparator.class);
    }

    @Test
    public void testSerializable() throws Exception {
        final SeverityComparator a = new SeverityComparator();
        final SeverityComparator b = serialClone(a);

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));

        assertEquals(a.hashCode(), b.hashCode());
    }

    @SuppressWarnings("unchecked")
    private <T> T serialClone(T t) {
        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final ObjectOutputStream out = new ObjectOutputStream(os);
            try {
                out.writeObject(t);
                out.flush();
            } finally {
                out.close();
            }

            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            final ObjectInputStream in = new ObjectInputStream(is);
            try {
                return (T) in.readObject();
            } finally {
                in.close();
            }
        } catch (ClassNotFoundException CNFE) {
            throw new AssertionError(CNFE);
        } catch (IOException ioe) {
            throw new AssertionError(ioe);
        }
    }

    @Test
    public void testByMillis() {
        testByMillis(new Error());
        testByMillis(new RuntimeException());
        testByMillis(new Exception());
    }

    private void testByMillis(Throwable t) {
        LogRecord r1 = new LogRecord(Level.INFO, Level.INFO.toString());
        LogRecord r2 = new LogRecord(Level.INFO, Level.INFO.toString());
        r2.setSequenceNumber(r1.getSequenceNumber());
        setEpochMilli(r1, 10);
        setEpochMilli(r2, 20);

        assertEquals(r1.getLevel(), r2.getLevel());
        assertEquals(r1.getThrown(), r2.getThrown());
        assertEquals(r1.getSequenceNumber(), r2.getSequenceNumber());
        assertEquals(r1.getThreadID(), r2.getThreadID());
        assertEquals(r1.getClass(), r2.getClass());
        assertTrue(r1.getMillis() < r2.getMillis());
        assertRecordLessThan(r1, r2);

        r1.setThrown(t);
        r2.setThrown(r1.getThrown());
        assertEquals(r1.getLevel(), r2.getLevel());
        assertEquals(r1.getThrown(), r2.getThrown());
        assertEquals(r1.getSequenceNumber(), r2.getSequenceNumber());
        assertEquals(r1.getThreadID(), r2.getThreadID());
        assertEquals(r1.getClass(), r2.getClass());
        assertTrue(r1.getMillis() < r2.getMillis());
        assertRecordLessThan(r1, r2);
    }

    private static void assertRecordLessThan(LogRecord r1, LogRecord r2) {
        final SeverityComparator a = new SeverityComparator();
        if (a.compare(r1, r2) >= 0) {
            throw new AssertionError(toString(r1, r2));
        }

        if (a.compare(r2, r1) <= 0) {
            throw new AssertionError(toString(r1, r2));
        }
    }

    private static String toString(int i, LogRecord r1, LogRecord r2) {
        return "Index: " + i + " " + toString(r1, r2);
    }

    private static String toString(LogRecord r1, LogRecord r2) {
        return toString(r1) + '|' + toString(r2);
    }

    private static String toString(LogRecord r) {
        StringBuilder b = new StringBuilder();
        b.append(r.getLevel()).append(", ");
        b.append(toString(r.getThrown()));
        b.append(' ').append(r.getSequenceNumber());
        return b.toString();
    }

    private static String toString(Throwable chain) {
        StringBuilder b = new StringBuilder();
        if (chain != null) {
            String sep = "";
            for (Throwable t = chain; t != null; t = t.getCause()) {
                b.append(sep).append(t.getClass().getSimpleName());
                sep = ", ";
            }
        } else {
            b.append("null");
        }
        return b.toString();
    }

    private static void swapSeq(LogRecord r1, LogRecord r2) {
        final long seq = r1.getSequenceNumber();
        r1.setSequenceNumber(r2.getSequenceNumber());
        r2.setSequenceNumber(seq);
        assertFalse(r1.getSequenceNumber() == r2.getSequenceNumber());
    }

    private LogRecord[] createRecords() {
        LogRecord[] r = new LogRecord[8];
        r[0] = new LogRecord(Level.INFO, "Started {0}, {1}");
        r[0].setParameters(new Object[]{"10", new Object()});
        r[1] = new LogRecord(Level.CONFIG, "99%");
        r[2] = new LogRecord(Level.SEVERE, "1st failure.");
        r[2].setThrown(new ClassNotFoundException());
        r[3] = new LogRecord(Level.SEVERE, "2nd failure.");
        r[3].setThrown(new ExceptionInInitializerError(new NullPointerException()));
        r[4] = new LogRecord(Level.SEVERE, "3rd failure.");
        r[4].setThrown(new NoClassDefFoundError());
        r[5] = new LogRecord(Level.WARNING, "Restart required.");
        r[6] = new LogRecord(Level.INFO, "Restarting....");
        r[7] = new LogRecord(Level.FINE, "Good bye.");
        return r;
    }

    private Throwable headIeChain(Throwable last) {
        return new InterruptedException(String.valueOf(last)).initCause(last);
    }

    private Throwable headIioeChain(Throwable last) {
        return new InterruptedIOException(String.valueOf(last)).initCause(last);
    }

    private Throwable headCbieChain(Throwable last) {
        return new ClosedByInterruptException().initCause(last);
    }

    private Throwable headFlieChain(Throwable last) {
        return new FileLockInterruptionException().initCause(last);
    }

    private Throwable headSubIeChain(Throwable last) {
        return new SubOfIe().initCause(last);
    }

    private Throwable headSubIioeChain(Throwable last) {
        return new SubOfIioe().initCause(last);
    }

    private Throwable headTdChain(Throwable last) {
        return new ThreadDeath().initCause(last);
    }

    private Throwable headSubTdChain(Throwable last) {
        return new SubOfTd().initCause(last);
    }

    private Throwable headWidChain(Throwable last) {
        return new WorkInterruptedException().initCause(last);
    }

    private Throwable headWitChain(Throwable last) {
        return new WorkInterruptionException().initCause(last);
    }

    /**
     * Lots of framework designers dislike checked exceptions.
     */
    private static class WorkInterruptionException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * Promote access.
         */
        WorkInterruptionException() {
        }
    }

    /**
     * Lots of framework designers dislike checked exceptions.
     */
    private static class WorkInterruptedException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * Promote access.
         */
        WorkInterruptedException() {
        }
    }

    /**
     * Sub class of interrupted.
     */
    private static class SubOfIe extends InterruptedException {

        private static final long serialVersionUID = 1L;

        /**
         * Promote access.
         */
        SubOfIe() {
        }
    }

    /**
     * Sub class of interrupted IO.
     */
    private static class SubOfIioe extends InterruptedIOException {

        private static final long serialVersionUID = 1L;

        /**
         * Promote access.
         */
        SubOfIioe() {
        }
    }

    /**
     * Sub class of thread death.
     */
    private static class SubOfTd extends ThreadDeath {

        private static final long serialVersionUID = 1L;

        /**
         * Promote access.
         */
        SubOfTd() {
        }
    }
}
