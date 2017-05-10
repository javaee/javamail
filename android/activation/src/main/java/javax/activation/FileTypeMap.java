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

package javax.activation;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The FileTypeMap is an abstract class that provides a data typing
 * interface for files. Implementations of this class will
 * implement the getContentType methods which will derive a content
 * type from a file name or a File object. FileTypeMaps could use any
 * scheme to determine the data type, from examining the file extension
 * of a file (like the MimetypesFileTypeMap) to opening the file and
 * trying to derive its type from the contents of the file. The
 * FileDataSource class uses the default FileTypeMap (a MimetypesFileTypeMap
 * unless changed) to determine the content type of files.
 *
 * @see javax.activation.FileTypeMap
 * @see javax.activation.FileDataSource
 * @see javax.activation.MimetypesFileTypeMap
 */

public abstract class FileTypeMap {

    private static FileTypeMap defaultMap = null;
    private static Map<ClassLoader,FileTypeMap> map =
				new WeakHashMap<ClassLoader,FileTypeMap>();

    /**
     * The default constructor.
     */
    public FileTypeMap() {
	super();
    }

    /**
     * Return the type of the file object. This method should
     * always return a valid MIME type.
     *
     * @param file A file to be typed.
     * @return The content type.
     */
    abstract public String getContentType(File file);

    /**
     * Return the type of the file passed in.  This method should
     * always return a valid MIME type.
     *
     * @param filename the pathname of the file.
     * @return The content type.
     */
    abstract public String getContentType(String filename);

    /**
     * Sets the default FileTypeMap for the system. This instance
     * will be returned to callers of getDefaultFileTypeMap.
     *
     * @param fileTypeMap The FileTypeMap.
     * @exception SecurityException if the caller doesn't have permission
     *					to change the default
     */
    public static synchronized void setDefaultFileTypeMap(FileTypeMap fileTypeMap) {
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    try {
		// if it's ok with the SecurityManager, it's ok with me...
		security.checkSetFactory();
	    } catch (SecurityException ex) {
		// otherwise, we also allow it if this code and the
		// factory come from the same (non-system) class loader (e.g.,
		// the JAF classes were loaded with the applet classes).
		ClassLoader cl = FileTypeMap.class.getClassLoader();
		if (cl == null || cl.getParent() == null ||
		    cl != fileTypeMap.getClass().getClassLoader())
		    throw ex;
	    }
	}
	// remove any per-thread-context-class-loader FileTypeMap
	map.remove(SecuritySupport.getContextClassLoader());
	defaultMap = fileTypeMap;	
    }

    /**
     * Return the default FileTypeMap for the system.
     * If setDefaultFileTypeMap was called, return
     * that instance, otherwise return an instance of
     * <code>MimetypesFileTypeMap</code>.
     *
     * @return The default FileTypeMap
     * @see javax.activation.FileTypeMap#setDefaultFileTypeMap
     */
    public static synchronized FileTypeMap getDefaultFileTypeMap() {
	if (defaultMap != null)
	    return defaultMap;

	// fetch per-thread-context-class-loader default
	ClassLoader tccl = SecuritySupport.getContextClassLoader();
	FileTypeMap def = map.get(tccl);
	if (def == null) {
	    def = new MimetypesFileTypeMap();
	    map.put(tccl, def);
	}
	return def;
    }
}
