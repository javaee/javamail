/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2015-2017 Jason Mehrens. All rights reserved.
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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test case for the DurationFilter spec.
 *
 * @author Jason Mehrens
 */
public class DurationFilterTest extends AbstractLogging {

    public DurationFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        checkJVMOptions();
    }

    @AfterClass
    public static void tearDownClass() {
        checkJVMOptions();
    }

    private static void checkJVMOptions() {
        assertTrue(DurationFilterTest.class.desiredAssertionStatus());
        assertNull(System.getProperty("java.util.logging.manager"));
        assertNull(System.getProperty("java.util.logging.config.class"));
        assertNull(System.getProperty("java.util.logging.config.file"));
        assertEquals(LogManager.class, LogManager.getLogManager().getClass());
    }

    @Test
    public void testDeclaredClasses() throws Exception {
        Class<?>[] declared = DurationFilter.class.getDeclaredClasses();
        assertEquals(Arrays.toString(declared), 0, declared.length);
    }

    @Test
    public void testClone() throws Exception {
        DurationFilterExt source = new DurationFilterExt();
        final Filter clone = source.clone();
        assertNotNull(clone);
        assertFalse(source == clone);
        assertTrue(source.equals(clone));
        assertEquals(source.getClass(), clone.getClass());

        LogRecord r = new LogRecord(Level.INFO, "");
        assertTrue(source.isLoggable(r));
        assertFalse(source.equals(clone));
        assertTrue(((DurationFilterExt) clone).clone().equals(clone));
    }

    @Test
    public void testCloneState() throws Exception {
        long millis = 0;
        final int records = 10;
        final int duration = 5 * 60 * 1000;
        Level lvl = Level.INFO;
        DurationFilterExt sf = new DurationFilterExt(records, duration);
        String msg = Long.toString(millis);
        LogRecord r = new LogRecord(lvl, msg);

        //Allow
        for (int i = 0; i < records; i++) {
            setEpochMilli(r, millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
        }

        Filter clone = sf.clone();
        for (int i = 0; i < records; i++) {
            setEpochMilli(r, millis);
            String m = Integer.toString(i);
            assertFalse(m, sf.isLoggable(r));
            assertTrue(m, clone.isLoggable(r));
        }

        assertFalse(sf.isLoggable(r));
        assertFalse(clone.isLoggable(r));
    }

    @Test(timeout = 15000)
    public void testIsLoggableNow() throws Exception {
        final int records = 10;
        final int duration = 1000;
        Level lvl = Level.INFO;
        DurationFilter sf = new DurationFilter(records, duration);
        assertTrue(sf.isLoggable());

        LogRecord r = new LogRecord(lvl, "");
        assertTrue(sf.isLoggable(r));
        assertTrue(sf.isLoggable());

        //Allow
        for (int i = 1; i < records; i++) {
            r = new LogRecord(lvl, "");
            String msg = Integer.toString(i);
            assertTrue(msg, sf.isLoggable());
            assertTrue(msg, sf.isLoggable(r));
        }

        assertFalse(sf.isLoggable());
        assertFalse(sf.isLoggable(r));

        tickMilli(duration + 100); //Cool down and allow.

        for (int i = 0; i < records; i++) {
            r = new LogRecord(lvl, "");
            String msg = Integer.toString(i);
            assertTrue(msg, sf.isLoggable());
            assertTrue(msg, sf.isLoggable(r));
        }

        assertFalse(sf.isLoggable());
        assertFalse(sf.isLoggable(r));
    }

    @Test(timeout = 15000)
    public void testIsIdleNow() throws Exception {
        final int records = 10;
        final int duration = 1000;
        Level lvl = Level.INFO;
        DurationFilter sf = new DurationFilter(records, duration);
        LogRecord r = new LogRecord(lvl, "");
        assertTrue(sf.isIdle());
        assertTrue(sf.isLoggable(r));
        assertFalse(sf.isIdle());

        //Allow
        for (int i = 1; i < records; i++) {
            r = new LogRecord(lvl, "");
            String msg = Integer.toString(i);
            assertFalse(msg, sf.isIdle());
            assertTrue(msg, sf.isLoggable(r));
        }

        assertFalse(sf.isIdle());
        assertFalse(sf.isLoggable(r));

        tickMilli(duration + 100); //Cool down and allow.

        assertTrue(sf.isIdle());
        for (int i = 0; i < records; i++) {
            r = new LogRecord(lvl, "");
            String msg = Integer.toString(i);
            assertTrue(msg, sf.isLoggable(r));
            assertFalse(msg, sf.isIdle());
        }

        assertFalse(sf.isIdle());
        assertFalse(sf.isLoggable(r));
    }

    @Test
    public void testSaturation() {
        long millis = 0;
        final int records = 10;
        final int duration = 5 * 60 * 1000;
        Level lvl = Level.INFO;
        DurationFilter sf = new DurationFilter(records, duration);
        LogRecord r;

        //Allow
        for (int i = 0; i < records; i++) {
            ++millis;
            r = new LogRecord(lvl, Long.toString(millis));
            setEpochMilli(r, millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
        }

        //Saturate.
        for (int i = 0; i < records * 10; i++) {
            r = new LogRecord(lvl, Long.toString(millis));
            setEpochMilli(r, millis);
            assertFalse(Integer.toString(i), sf.isLoggable(r));
        }

        //Cool down and allow.
        millis += duration;
        for (int i = 0; i < records; i++) {
            ++millis;
            r = new LogRecord(lvl, Long.toString(millis));
            setEpochMilli(r, millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
        }
    }

    @Test
    public void testSaturateIntergral() {
        long duration = 15L * 60L * 1000L;
        for (long i = 0; i <= duration * 2; i++) {
            testSaturateIntergral(i, duration);
        }
    }

    private void testSaturateIntergral(long millis, long duration) {
        final int records = 10;
        Level lvl = Level.INFO;
        DurationFilter sf = new DurationFilter(records, duration);
        LogRecord r = new LogRecord(lvl, "");
        sf.isLoggable(r); //Init the duration.
        millis += (2 * duration) - 1;
        for (int i = 0; i < records - 2; i++) {
            setEpochMilli(r, millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
        }

        millis += 100;
        setEpochMilli(r, millis);
        assertTrue(sf.isLoggable(r));

        for (int i = 0; i < records - 1; i++) {
            setEpochMilli(r, millis);
            assertFalse(Integer.toString(i), sf.isLoggable(r));
        }
    }

    @Test
    public void testSaturatePositiveOutOfOrder() {
        testSaturateOutOfOrder(Integer.MAX_VALUE);
    }

    @Test
    public void testSaturateNegativeOutOfOrder() {
        testSaturateOutOfOrder(-Integer.MAX_VALUE);
    }

    @Test
    public void testSaturateOverFlowOutOfOrder() {
        testSaturateOutOfOrder(Long.MAX_VALUE);
    }

    @Test
    public void testSaturateUnderFlowOutOfOrder() {
        testSaturateOutOfOrder(-Long.MAX_VALUE);
    }

    public void testSaturateOutOfOrder(long millis) {
        final int records = 10;
        final int duration = 5 * 60 * 1000;
        Level lvl = Level.INFO;
        DurationFilter sf = new DurationFilter(records, duration);
        LogRecord r;

        //Allow
        for (int i = 0; i < records; i++) {
            r = new LogRecord(lvl, Long.toString(millis));
            setEpochMilli(r, millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
            --millis;
        }

        //Still saturated.
        millis += duration;
        final long peak = millis;
        for (int i = 0; i < records; i++) {
            r = new LogRecord(lvl, Long.toString(millis));
            setEpochMilli(r, millis);
            assertFalse(Integer.toString(i), sf.isLoggable(r));
        }

        //Cool down and allow.
        millis = peak + duration;
        for (int i = 0; i < records; i++) {
            r = new LogRecord(lvl, Long.toString(millis));
            setEpochMilli(r, millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
            ++millis;
        }
    }

    @Test
    public void testTimeSpringShort() {
        testClockAdjustment(5 * 60 * 1000, 1);
    }

    @Test
    public void testTimeSpringLong() {
        testClockAdjustment(24 * 60 * 60 * 1000, 1);
    }

    @Test
    public void testTimeFallShort() {
        testClockAdjustment(5 * 60 * 1000, -1);
    }

    @Test
    public void testTimeFallLong() {
        testClockAdjustment(24 * 60 * 60 * 1000, -1);
    }

    private void testClockAdjustment(int records, int signum) {
        assertFalse(0 == signum);
        assertEquals(Integer.signum(signum), signum);
        long millis = 0L;
        DurationFilter sf = new DurationFilter(records, records);
        LogRecord r = new LogRecord(Level.INFO, "");
        for (int i = 1; i < (records / 2); i++) {
            setEpochMilli(r, ++millis);
            assertTrue(sf.isLoggable(r));
        }

        millis += signum * (60L * 60L * 1000L);
        for (int i = (records / 2); i <= records; i++) {
            setEpochMilli(r, ++millis);
            assertTrue(sf.isLoggable(r));
        }
    }

    @Test
    public void testPredictedOverflow() {
        int records = 4;
        int duration = 4;
        DurationFilter sf = new DurationFilter(records, duration);
        for (int i = 0; i < records; i++) {
            LogRecord r = new LogRecord(Level.INFO, "");
            setEpochMilli(r, Long.MAX_VALUE);
            assertTrue(sf.isLoggable(r));
        }

        LogRecord r = new LogRecord(Level.INFO, "");
        setEpochMilli(r, Long.MAX_VALUE);
        assertFalse(sf.isLoggable(r));

        r = new LogRecord(Level.INFO, "");
        setEpochMilli(r, Long.MAX_VALUE + duration);
        assertTrue(sf.isLoggable(r));
    }

    @Test
    public void testMillisNegativeSaturation() {
        int records = 4;
        int duration = 4;
        DurationFilter sf = new DurationFilter(records, duration);
        for (int i = 0; i < records; i++) {
            LogRecord r = new LogRecord(Level.INFO, "");
            setEpochMilli(r, Long.MIN_VALUE);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
        }

        LogRecord r = new LogRecord(Level.INFO, "");
        setEpochMilli(r, Long.MIN_VALUE);
        assertFalse(sf.isLoggable(r));

        r = new LogRecord(Level.INFO, "");
        setEpochMilli(r, Long.MIN_VALUE + duration);
        assertTrue(sf.isLoggable(r));
    }

    @Test
    public void testExactRate() throws Exception {
        long millis = System.currentTimeMillis();
        final int records = 1000;
        final int duration = 5 * 60 * 1000;
        Level lvl = Level.INFO;
        DurationFilter sf = new DurationFilter(records, duration);
        LogRecord r;

        int period = duration / records;
        assertEquals(period, (double) duration / (double) records, 0.0);
        for (int i = 0; i < records * records; i++) {
            r = new LogRecord(lvl, Long.toString(millis));
            setEpochMilli(r, millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
            millis += period;
        }
    }

    @Test
    public void testCeilRate() throws Exception {
        double millis = 0L;
        final int records = 3;
        final int duration = 40;
        Level lvl = Level.INFO;
        DurationFilter sf = new DurationFilter(records, duration);
        LogRecord r;

        double period = duration / (double) records;
        for (int i = 0; i < (duration * records) * 2; i++) {
            r = new LogRecord(lvl, Double.toString(millis));
            setEpochMilli(r, (long) millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
            millis = millis + Math.ceil(period);
        }
    }

    @Test
    public void testFloorRate() {
        double millis = 0.0d;
        final int records = 30;
        final int duration = 400;
        Level lvl = Level.INFO;
        DurationFilter sf = new DurationFilter(records, duration);
        LogRecord r;
        long period = duration / records;
        for (int i = 0; i < records; i++) {
            r = new LogRecord(lvl, Long.toString((long) millis));
            setEpochMilli(r, (long) millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
            millis += period;
        }

        //Saturated for records + one.
        for (int i = 0; i <= records; i++) {
            r = new LogRecord(lvl, Long.toString((long) millis));
            setEpochMilli(r, (long) millis);
            assertFalse(Integer.toString(i), sf.isLoggable(r));
            millis += period;
        }

        for (int i = 0; i < records; i++) {
            r = new LogRecord(lvl, Long.toString((long) millis));
            setEpochMilli(r, (long) millis);
            assertTrue(Integer.toString(i), sf.isLoggable(r));
            millis += period;
        }
    }

    private void testRate(long millis, long records, long duration) {
        Level lvl = Level.INFO;
        DurationFilter sf = new DurationFilter(records, duration);
        LogRecord r = new LogRecord(lvl, Long.toString(millis));

        for (long i = 0; i < records; i++) {
            setEpochMilli(r, millis);
            assertTrue(sf.isLoggable(r));
        }

        r = new LogRecord(lvl, Long.toString(millis));
        setEpochMilli(r, millis);
        assertFalse(sf.isLoggable(r));
    }

    @Test
    public void testOneTenthErrorRate() {
        testRate(0, 10, 1);
    }

    @Test
    public void testOneHundredthErrorRate() {
        testRate(0, 100, 1);
    }

    @Test
    public void testOneThousanthErrorRate() {
        testRate(0, 1000, 1);
    }

    @Test
    public void testOneMillionthErrorRate() {
        testRate(0, 1000000, 1);
    }

    @Test
    public void testTwoToThe53rdRate() {
        testRate(0, 1, 1L << 53L);
    }

    @Ignore
    public void testIntegerMaxValueByTenRate() {
        /**
         * This can take a few minutes to run.
         */
        testRate(0, Integer.MAX_VALUE, 10);
    }

    @Test(expected = NullPointerException.class)
    public void testIsLoggableNull() {
        new DurationFilter().isLoggable((LogRecord) null);
    }

    @Test
    public void testEquals() {
        DurationFilter one = new DurationFilter();
        DurationFilter two = new DurationFilter();
        assertTrue(one.equals(one));
        assertTrue(two.equals(two));
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));

        LogRecord r = new LogRecord(Level.INFO, "");
        assertTrue(one.isLoggable(r));
        assertTrue(one.equals(one));
        assertTrue(two.equals(two));
        assertFalse(one.equals(two));
        assertFalse(two.equals(one));
        assertFalse(one.equals((Object) null));
        assertFalse(two.equals((Object) null));
    }

    @Test
    public void testHashCode() {
        DurationFilter one = new DurationFilter(10, 10);
        DurationFilter two = new DurationFilter(10, 10);
        DurationFilter three = new DurationFilter(3, 3);

        assertTrue(one.hashCode() == two.hashCode());
        assertFalse(one.hashCode() == three.hashCode());

        LogRecord r = new LogRecord(Level.INFO, "");
        assertTrue(one.isLoggable(r));
        assertTrue(one.hashCode() == two.hashCode());
        assertFalse(one.hashCode() == three.hashCode());
    }

    @Test
    public void testToString() {
        testToString(new DurationFilter());
    }

    @Test
    public void testToStringEx() {
        testToString(new DurationFilterExt());
    }

    private void testToString(DurationFilter f) {
        String s = f.toString();
        assertTrue(s.startsWith(f.getClass().getName()));
        assertTrue(s.contains("records="));
        assertTrue(s.contains("duration="));
        assertTrue(s.contains("idle="));
        assertTrue(s.contains("loggable="));
    }

    @Test
    public void testJavaMailLinkage() throws Exception {
        testJavaMailLinkage(DurationFilter.class);
    }

    @Test
    public void testLogManagerModifiers() throws Exception {
        testLogManagerModifiers(DurationFilter.class);
    }

    @Test
    public void testWebappClassLoaderFieldNames() throws Exception {
        testWebappClassLoaderFieldNames(DurationFilter.class);
    }

    @Test
    public void testInitRecords() throws Exception {
        testInitRecords("210", 210);
    }

    @Test
    public void testInitRecordsZero() throws Exception {
        testInitRecords("0", 1000);
    }

    @Test
    public void testInitRecordsNegative() throws Exception {
        testInitRecords("-1", 1000);
    }

    @Test
    public void testInitRecordIso8601() throws Exception {
        if (hasJavaTimeModule()) {
            testInitRecords("PT30M", 1000);
        }
    }

    @Test
    public void testInitDuration() throws Exception {
        testInitDuration("1024", 1024);
    }

    @Test
    public void testInitDurationZero() throws Exception {
        testInitDuration("0", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationNegative() throws Exception {
        testInitDuration("-1", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExp() throws Exception {
        testInitDuration("15 * 60 * 1000", 15L * 60L * 1000L);
        testInitDuration("15*60*1000", 15L * 60L * 1000L);
        testInitDuration("15L * 60L * 1000L", 15L * 60L * 1000L);
        testInitDuration("15L*60L*1000L", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpLifetime() throws Exception {
        testInitDuration("125L * 366 * 24L * 60L * 60L * 1000L",
                125L * 366 * 24L * 60L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpOverflow() throws Exception {
        testInitDuration(Long.MAX_VALUE + " * "
                + Long.MAX_VALUE, 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpAlpha() throws Exception {
        testInitDuration("15LL * 60 * 1000", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpTooManyMult() throws Exception {
        testInitDuration("15L ** 60 ** 1000", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpTrailing() throws Exception {
        testInitDuration("15 * 60 * 1000*", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpSpace() throws Exception {
        testInitDuration("15 * 60 * 1000* ", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpLeading() throws Exception {
        testInitDuration("*15 * 60 * 1000", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpAdd() throws Exception {
        testInitDuration("15 + 60 + 1000", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpDivide() throws Exception {
        testInitDuration("15 / 60 / 1000", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationExpSubstract() throws Exception {
        testInitDuration("15 - 60 - 1000", 15L * 60L * 1000L);
    }

    @Test
    public void testInitNegativeDuration() throws Exception {
        testInitDuration("-1024", 15L * 60L * 1000L);
    }

    @Test
    public void testInitDurationIso8601Ms() throws Exception {
        if (hasJavaTimeModule()) {
            testInitDuration("PT0.345S", 345);
        }
    }

    @Test
    public void testInitDurationIso8601Sec() throws Exception {
        if (hasJavaTimeModule()) {
            testInitDuration("PT20.345S", (20L * 1000L) + 345);
        }
    }

    @Test
    public void testInitDurationIso8601Min() throws Exception {
        if (hasJavaTimeModule()) {
            testInitDuration("PT30M", 30L * 60L * 1000L);
        }
    }

    @Test
    public void testInitDurationIso8601Hour() throws Exception {
        if (hasJavaTimeModule()) {
            testInitDuration("PT10H", 10L * 60L * 60L * 1000L);
        }
    }

    @Test
    public void testInitDurationIso8601Day() throws Exception {
        if (hasJavaTimeModule()) {
            testInitDuration("P2D", 2L * 24L * 60L * 60L * 1000L);
        }
    }

    @Test
    public void testInitDurationIso8601All() throws Exception {
        if (hasJavaTimeModule()) {
            testInitDuration("P2DT3H4M20.345S", (2L * 24L * 60L * 60L * 1000L)
                    + (3L * 60L * 60L * 1000L) + (4L * 60L * 1000L)
                    + ((20L * 1000L) + 345));
        }
    }

    private void testInitDuration(String d, long expect) throws Exception {
        testInit("duration", d, expect);
    }

    private void testInitRecords(String r, long expect) throws Exception {
        testInit("records", r, expect);
    }

    private void testInit(String field, String value, long expect) throws Exception {
        String p = DurationFilter.class.getName();
        Properties props = new Properties();
        props.put(p + '.' + field, value);
        LogManager m = LogManager.getLogManager();
        try {
            read(m, props);
            DurationFilter sf = new DurationFilter();
            Field f = DurationFilter.class.getDeclaredField(field);
            f.setAccessible(true);
            assertEquals(expect, f.get(sf));
        } finally {
            m.reset();
        }
    }

    public static final class DurationFilterExt extends DurationFilter
            implements Cloneable {

        public DurationFilterExt() {
            super();
        }

        public DurationFilterExt(long records, long duration) {
            super(records, duration);
        }

        @Override
        public DurationFilterExt clone() throws CloneNotSupportedException {
            return (DurationFilterExt) super.clone();
        }
    }
}
