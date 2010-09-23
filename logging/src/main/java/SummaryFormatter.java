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

import java.util.logging.*;
import java.text.DateFormat;
import java.util.Date;

/**
 * A compact formatter used to summarize an error report.
 * @author Jason Mehrens
 */
public class SummaryFormatter extends Formatter {

    private long oldest;
    private long newest;
    private long count;

    public SummaryFormatter() {
        reset();
    }

    public String format(LogRecord record) {
        final String data;
        if (record.getThrown() != null) {
            data = record.getLevel() + " with detail." + newLine();
        } else {
            data = record.getLevel() + " with no detail." + newLine();
        }
        track(record.getMillis());
        return data;
    }

    public synchronized String getTail(Handler h) {
        try {
            if (count > 0L) {
                return formatNow();
            }
            return "";
        } finally {
            reset();
        }
    }

    public synchronized String toString() {
        return formatNow();
    }

    private String formatNow() {
        assert Thread.holdsLock(this);
        DateFormat f = DateFormat.getDateTimeInstance();
        return newLine() + "These " + count + " messages occured between " +
                f.format(new Date(oldest)) + " and " +
                f.format(new Date(newest));

    }

    private synchronized void track(long time) {
        count++;
        this.oldest = Math.min(this.oldest, time);
        this.newest = Math.max(this.newest, time);
    }

    private synchronized void reset() {
        this.count = 0L;
        this.oldest = Long.MAX_VALUE;
        this.newest = Long.MIN_VALUE;
    }

    private static String newLine() {
        return "\n";
    }
}
