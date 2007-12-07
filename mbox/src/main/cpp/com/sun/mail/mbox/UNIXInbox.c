/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


/*
 * Copyright (c) 1997-2005 by Sun Microsystems, Inc.
 * All rights reserved.
 */

#include <jni.h>
#include <maillock.h>
extern void touchlock();	/* XXX - should be in maillock.h */

#include "com_sun_mail_mbox_UNIXInbox.h"

/*
 * Class:     com_sun_mail_mbox_UNIXInbox
 * Method:    maillock
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_sun_mail_mbox_UNIXInbox_maillock(JNIEnv *env, jobject obj,
    jstring user, jint retry_count)
{
	jboolean ret;
	const char *name = (*env)->GetStringUTFChars(env, user, 0);
	ret = maillock((char *)name, retry_count) == L_SUCCESS ?
	    JNI_TRUE : JNI_FALSE;
	(*env)->ReleaseStringUTFChars(env, user, name);
	return (ret);
}

/*
 * Class:     com_sun_mail_mbox_UNIXInbox
 * Method:    mailunlock
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_com_sun_mail_mbox_UNIXInbox_mailunlock(JNIEnv *env, jobject obj)
{
	(void) mailunlock();
}

/*
 * Class:     com_sun_mail_mbox_UNIXInbox
 * Method:    touchlock0
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_com_sun_mail_mbox_UNIXInbox_touchlock0(JNIEnv *env, jobject obj)
{
	(void) touchlock();
}
