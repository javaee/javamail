/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.mail.mbox;

import java.io.File;
import java.io.FilenameFilter;

public interface FileInterface {
    /**
     * Gets the name of the file. This method does not include the
     * directory.
     * @return the file name.
     */
    public String getName();

    /**
     * Gets the path of the file.
     * @return the file path.
     */
    public String getPath();

    /**
     * Gets the absolute path of the file.
     * @return the absolute file path.
     */
    public String getAbsolutePath();

    /**
     * Gets the official, canonical path of the File.
     * @return canonical path
     */
    // XXX - JDK1.1
    // public String getCanonicalPath();

    /**
     * Gets the name of the parent directory.
     * @return the parent directory, or null if one is not found.
     */
    public String getParent();

    /**
     * Returns a boolean indicating whether or not a file exists.
     */
    public boolean exists();

    /**
     * Returns a boolean indicating whether or not a writable file 
     * exists. 
     */
    public boolean canWrite();

    /**
     * Returns a boolean indicating whether or not a readable file 
     * exists.
     */
    public boolean canRead();

    /**
     * Returns a boolean indicating whether or not a normal file 
     * exists.
     */
    public boolean isFile();

    /**
     * Returns a boolean indicating whether or not a directory file 
     * exists.
     */
    public boolean isDirectory();

    /**
     * Returns a boolean indicating whether the file name is absolute.
     */
    public boolean isAbsolute();

    /**
     * Returns the last modification time. The return value should
     * only be used to compare modification dates. It is meaningless
     * as an absolute time.
     */
    public long lastModified();

    /**
     * Returns the length of the file. 
     */
    public long length();

    /**
     * Creates a directory and returns a boolean indicating the
     * success of the creation.  Will return false if the directory already
     * exists.
     */
    public boolean mkdir();

    /**
     * Renames a file and returns a boolean indicating whether 
     * or not this method was successful.
     * @param dest the new file name
     */
    public boolean renameTo(File dest);

    /**
     * Creates all directories in this path.  This method 
     * returns true if the target (deepest) directory was created,
     * false if the target directory was not created (e.g., if it
     * existed previously).
     */
    public boolean mkdirs();

    /**
     * Lists the files in a directory. Works only on directories.
     * @return an array of file names.  This list will include all
     * files in the directory except the equivalent of "." and ".." .
     */
    public String[] list();

    /**
     * Uses the specified filter to list files in a directory. 
     * @param filter the filter used to select file names
     * @return the filter selected files in this directory.
     * @see FilenameFilter
     */
    public String[] list(FilenameFilter filter);

    /**
     * Deletes the specified file. Returns true
     * if the file could be deleted.
     */
    public boolean delete();
}
