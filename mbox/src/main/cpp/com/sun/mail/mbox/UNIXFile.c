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

#include <jni.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

extern int _fcntl();

#include "com_sun_mail_mbox_UNIXFile.h"

static	jfieldID IO_fd_fdID;
static	int fd_offset;

/*
 * Class:     com_sun_mail_mbox_UNIXFile
 * Method:    initIDs
 * Signature: (Ljava/lang/Class;Ljava/io/FileDescriptor;)V
 */
JNIEXPORT void JNICALL
Java_com_sun_mail_mbox_UNIXFile_initIDs(JNIEnv *env, jclass ufClass,
    jclass fdClass, jobject stdin_obj)
{
	IO_fd_fdID = (*env)->GetFieldID(env, fdClass, "fd", "I");
	/*
	 * Because pre-JDK 1.2 stored the "fd" as one more than
	 * its actual value, we remember the value it stored for
	 * stdin, which should be zero, and use it as the offset
	 * for other fd's we extract.
	 */
	fd_offset = (*env)->GetIntField(env, stdin_obj, IO_fd_fdID);
}

/*
 * Class:     com_sun_mail_mbox_UNIXFile
 * Method:    lock0
 * Signature: (Ljava/io/FileDescriptor;Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_sun_mail_mbox_UNIXFile_lock0(JNIEnv *env, jclass clazz,
    jobject fdobj, jstring umode, jboolean block)
{
	int fd;
	const char *mode;
	static struct flock flock0;
	struct flock flock = flock0;

	fd = (*env)->GetIntField(env, fdobj, IO_fd_fdID);
	fd -= fd_offset;
	/* XXX - a lot of work to examine one character in a string */
	mode = (*env)->GetStringUTFChars(env, umode, 0);
	flock.l_type = mode[1] == 'w' ? F_WRLCK : F_RDLCK;
	(*env)->ReleaseStringUTFChars(env, umode, mode);
	flock.l_whence = SEEK_SET;
	flock.l_start = 0;
	flock.l_len = 0;
	return (_fcntl(fd, block ? F_SETLKW : F_SETLK, &flock) == 0 ?
		JNI_TRUE : JNI_FALSE);
}

/*
 * Class:     com_sun_mail_mbox_UNIXFile
 * Method:    lastAccessed0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_com_sun_mail_mbox_UNIXFile_lastAccessed0(JNIEnv *env, jclass clazz,
	jstring uname)
{
	const char *name;
	jlong ret = -1;
	struct stat st;

	name = (*env)->GetStringUTFChars(env, uname, 0);
	if (stat(name, &st) == 0) {
		/*
		 * Should be...
		ret = (jlong)st.st_atim.tv_sec * 1000 +
			st.st_atim.tv_nsec / 1000000;
		 * but for compatibility with lastModified we use...
		 */
		ret = (jlong)st.st_atime * 1000;
	}
	(*env)->ReleaseStringUTFChars(env, uname, name);
	return ret;
}
