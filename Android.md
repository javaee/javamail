JavaMail for Android
====================

JavaMail for Android is now available!

Android does not provide a Java Compatible runtime and so can't run the
standard JavaMail distribution.  Instead, a special version of JavaMail is
available for Android.  This special version of JavaMail depends
on a special version of the JavaBeans Activation Framework.

This version is available from the java.net maven repository.
You can try out this version by adding the following to your
build.gradle file for your Android application:

    android {
        packagingOptions {
            pickFirst 'META-INF/LICENSE.txt' // picks the JavaMail license file
        }
    }
    
    repositories { 
        jcenter()
        maven {
            url "https://maven.java.net/content/groups/public/"
        }
    }
    
    dependencies {
        compile 'com.sun.mail:android-mail:1.5.5'
        compile 'com.sun.mail:android-activation:1.5.5'
    }


One of the standard Java features not supported on Android is SASL.  That means
none of the "mail._protocol_.sasl.*" properties will have any effect.  One of
the main uses of SASL was to enable OAuth2 support.  This latest version
of JavaMail includes built-in OAuth2 support that doesn't require SASL.
See the [OAuth2](OAuth2) page for more details.

If you discover problems, please report them to
[javamail_ww@oracle.com](mailto:javamail_ww@oracle.com).
