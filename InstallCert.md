No more 'unable to find valid certification path to requested target'
=====================================================================

(This page was rescued from Andreas Sterbenz's blog on blogs.sun.com,
which no longer exists.)

Monday Oct 09, 2006

No more 'unable to find valid certification path to requested target'

Some of you may be familiar with the (not very user friendly) exception
message javax.net.ssl.SSLHandshakeException:
sun.security.validator.ValidatorException: PKIX path building failed:
sun.security.provider.certpath.SunCertPathBuilderException: unable to
find valid certification path to requested target when trying to open
an SSL connection to a host using JSSE. What this usually means is that
the server is using a test certificate (possibly generated using
keytool) rather than a certificate from a well known commercial
Certification Authority such as Verisign or GoDaddy. Web browsers
display warning dialogs in this case, but since JSSE cannot assume an
interactive user is present it just throws an exception by default.

Certificate validation is a very important part of SSL security, but I
am not writing this entry to explain the details. If you are
interested, you can start by reading the Wikipedia blurb. I am writing
this entry to show a simple way to talk to that host with the test
certificate, if you really want to.

Basically, you want to add the server's certificate to the KeyStore
with your trusted certificates. There are any number of ways to achieve
that, but a simple solution is to compile and run
[this program](InstallCert.java)
as java InstallCert hostname, for example

    % java InstallCert ecc.fedora.redhat.com
    Loading KeyStore /usr/jdk/instances/jdk1.5.0/jre/lib/security/cacerts...
    Opening connection to ecc.fedora.redhat.com:443...
    Starting SSL handshake...

    javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
        at com.sun.net.ssl.internal.ssl.Alerts.getSSLException(Alerts.java:150)
        at com.sun.net.ssl.internal.ssl.SSLSocketImpl.fatal(SSLSocketImpl.java:1476)
        at com.sun.net.ssl.internal.ssl.Handshaker.fatalSE(Handshaker.java:174)
        at com.sun.net.ssl.internal.ssl.Handshaker.fatalSE(Handshaker.java:168)
        at com.sun.net.ssl.internal.ssl.ClientHandshaker.serverCertificate(ClientHandshaker.java:846)
        at com.sun.net.ssl.internal.ssl.ClientHandshaker.processMessage(ClientHandshaker.java:106)
        at com.sun.net.ssl.internal.ssl.Handshaker.processLoop(Handshaker.java:495)
        at com.sun.net.ssl.internal.ssl.Handshaker.process_record(Handshaker.java:433)
        at com.sun.net.ssl.internal.ssl.SSLSocketImpl.readRecord(SSLSocketImpl.java:815)
        at com.sun.net.ssl.internal.ssl.SSLSocketImpl.performInitialHandshake(SSLSocketImpl.java:1025)
        at com.sun.net.ssl.internal.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:1038)
        at InstallCert.main(InstallCert.java:63)
    Caused by: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
        at sun.security.validator.PKIXValidator.doBuild(PKIXValidator.java:221)
        at sun.security.validator.PKIXValidator.engineValidate(PKIXValidator.java:145)
        at sun.security.validator.Validator.validate(Validator.java:203)
        at com.sun.net.ssl.internal.ssl.X509TrustManagerImpl.checkServerTrusted(X509TrustManagerImpl.java:172)
        at InstallCert$SavingTrustManager.checkServerTrusted(InstallCert.java:158)
        at com.sun.net.ssl.internal.ssl.JsseX509TrustManager.checkServerTrusted(SSLContextImpl.java:320)
        at com.sun.net.ssl.internal.ssl.ClientHandshaker.serverCertificate(ClientHandshaker.java:839)
        ... 7 more
    Caused by: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
        at sun.security.provider.certpath.SunCertPathBuilder.engineBuild(SunCertPathBuilder.java:236)
        at java.security.cert.CertPathBuilder.build(CertPathBuilder.java:194)
        at sun.security.validator.PKIXValidator.doBuild(PKIXValidator.java:216)
        ... 13 more

    Server sent 2 certificate(s):

     1 Subject CN=ecc.fedora.redhat.com, O=example.com, C=US
       Issuer  CN=Certificate Shack, O=example.com, C=US
       sha1    2e 7f 76 9b 52 91 09 2e 5d 8f 6b 61 39 2d 5e 06 e4 d8 e9 c7 
       md5     dd d1 a8 03 d7 6c 4b 11 a7 3d 74 28 89 d0 67 54 

     2 Subject CN=Certificate Shack, O=example.com, C=US
       Issuer  CN=Certificate Shack, O=example.com, C=US
       sha1    fb 58 a7 03 c4 4e 3b 0e e3 2c 40 2f 87 64 13 4d df e1 a1 a6 
       md5     72 a0 95 43 7e 41 88 18 ae 2f 6d 98 01 2c 89 68 

    Enter certificate to add to trusted keystore or 'q' to quit: [1]

What happened was that the program opened a connection to the specified
host and started an SSL handshake. It printed the exception stack trace
of the error that occured and shows you the certificates used by the
server. Now it prompts you for the certificate you want to add to your
trusted KeyStore. You should only do this if you are sure that this is
the certificate of the trusted host you want to connect to. You may
want to check the MD5 and SHA1 certificate fingerprints against a
fingerprint generated on the server (e.g. using keytool) to make sure
it is the correct certificate.

If you've changed your mind, enter 'q'. If you really want to add the
certificate, enter '1'. (You could also add a CA certificate by
entering a different certificate, but you usually don't want to do
that'). Once you have made your choice, the program will print the
following:

    [
    [
      Version: V3
      Subject: CN=ecc.fedora.redhat.com, O=example.com, C=US
      Signature Algorithm: MD5withRSA, OID = 1.2.840.113549.1.1.4

      Key:  SunPKCS11-Solaris RSA public key, 1024 bits (id 5158256, session object)
      modulus: 1402933022884660852748661816869706021655226675890
    635441166580364941191074987345500771612454338502131694873337
    233737712894815966313948609351561047977102880577818156814678
    041303637255354084762814638611185951230474669455913908815827
    173696651397340074281578017567044868711049821409365743953199
    69584127568303024757
      public exponent: 65537
      Validity: [From: Wed Jan 18 13:16:12 PST 2006,
                   To: Wed Apr 18 14:16:12 PDT 2007]
      Issuer: CN=Certificate Shack, O=example.com, C=US
      SerialNumber: [    0f]

    Certificate Extensions: 2
    [1]: ObjectId: 2.16.840.1.113730.1.1 Criticality=false
    NetscapeCertType [
       SSL server
    ]

    [2]: ObjectId: 2.5.29.15 Criticality=false
    KeyUsage [
      Key_Encipherment
    ]

    ]
      Algorithm: [MD5withRSA]
      Signature:
    0000: 6D F4 2A 63 76 2A 05 70   A2 21 0E 1E 4A 31 BE 6B  m.*cv*.p.!..J1.k
    0010: 15 64 D8 BB 35 36 82 B0   0D 2A 96 FA 7A 9F A1 59  .d..56...*..z..Y
    0020: CA 90 C3 28 C5 A6 9B 59   05 3B EB B2 8D C9 5E 38  ...(...Y.;....^8
    0030: 62 ED 1A D7 93 DF 2A A5   D6 54 94 23 15 A2 0C E5  b.....*..T.#....
    0040: 13 40 2C 3E 59 E4 2A EB   51 AC 9E 28 44 23 87 B1  .@,>Y.*.Q..(D#..
    0050: 34 0B AC F3 E0 39 CA B8   35 B4 78 07 BF 28 4C C4  4....9..5.x..(L.
    0060: 9A 2B A3 E9 04 26 78 19   F0 62 EA 0A B5 BB DC 0B  .+...&x..b......
    0070: 90 59 E7 77 90 F8 BC 8A   1B 74 4B 4D C1 F8 3B 6C  .Y.w.....tKM..;l

    ]

    Added certificate to keystore 'jssecacerts' using alias 'ecc.fedora.redhat.com-1'

It displayed the complete certificate and then added it to a Java
KeyStore 'jssecacerts' in the current directory. To use it in your
program, either configure JSSE to use it as its trust store (as
explained in the documentation) or copy it into your
\$JAVA\_HOME/jre/lib/security directory. If you want all Java
applications to recognize the certificate as trusted and not just JSSE,
you could also overwrite the cacerts file in that directory.

After all that, JSSE will be able to complete a handshake with the
host, which you can verify by running the program again:

    % java InstallCert ecc.fedora.redhat.com
    Loading KeyStore jssecacerts...
    Opening connection to ecc.fedora.redhat.com:443...
    Starting SSL handshake...

    No errors, certificate is already trusted

    Server sent 2 certificate(s):

     1 Subject CN=ecc.fedora.redhat.com, O=example.com, C=US
       Issuer  CN=Certificate Shack, O=example.com, C=US
       sha1    2e 7f 76 9b 52 91 09 2e 5d 8f 6b 61 39 2d 5e 06 e4 d8 e9 c7 
       md5     dd d1 a8 03 d7 6c 4b 11 a7 3d 74 28 89 d0 67 54 

     2 Subject CN=Certificate Shack, O=example.com, C=US
       Issuer  CN=Certificate Shack, O=example.com, C=US
       sha1    fb 58 a7 03 c4 4e 3b 0e e3 2c 40 2f 87 64 13 4d df e1 a1 a6 
       md5     72 a0 95 43 7e 41 88 18 ae 2f 6d 98 01 2c 89 68 

    Enter certificate to add to trusted keystore or 'q' to quit: [1]
    q
    KeyStore not changed

I hope that helps. For more information about the InstallCert program,
have a look at the source code. I am sure you can figure out how it
works.

Posted at 22:28 Oct 09, 2006 by Andreas Sterbenz in Java
