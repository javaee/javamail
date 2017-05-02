How to Contribute to JavaMail
=============================

If you're interested in contributing fixes, enhancements, etc. to
JavaMail, please contact us at <javamail_ww@oracle.com> before you
start.  We can give you advice you might need to make it easier to
contribute, and we can better coordinate contributions with other
planned or ongoing work on JavaMail.

Contributions to JavaMail follow the same rules and process as
contributions to GlassFish, which is described
[here](https://glassfish.java.net/public/GovernancePolicy.html#OCA_Policy)
and excerpted below:

## Contribution Policy

The first step to contributing code or submitting a patch is to sign
and return [Contributor Agreement](http://oss.oracle.com/oca.pdf).
Please print this form out, fill in all the necessary detail, scan it,
and return it via e-mail: <oracle-ca_us@oracle.com> You may also want
to read the [Contributor agreement FAQ](http://oss.oracle.com/oca-faq.pdf).

See [CONTRIBUTING](CONTRIBUTING) for the legal details.

## Coding Style

Modifications to existing JavaMail source files, and contributions of
new source files, should use the standard Java coding style as
originally described
[here](http://www.oracle.com/technetwork/java/codeconvtoc-136057.html)
and unofficially updated
[here](http://cr.openjdk.java.net/~alundblad/styleguide/index-v6.html).
The most important points are summarized below:

-   Indentation should be in units of 4 spaces, preferably with every 8
    spaces replaced with a tab character. (If using vi, set tabstop=8,
    not 4.)

-   Braces should be at the end of the line they apply to, rather than
    all alone at the beginning of the next line, i.e.,

```java
       if (foo instanceof bar) {  
           foobar();  
           barfoo();  
       }
```

-   Methods should have doc comments of the form:

```java
        \/**
         \* comments here
         \*/
```

-   All keywords should have a space after them, before any paren
    (e.g., "if (", "while (", "for (", etc.)

-   The "comment to end of line" characters (//) should be followed by a space.

-   The start of a multiline comment (/\* or /\*\*) should be alone on a line.

-   No space after left paren or before right paren (e.g., "foo(x)",
    not "foo( x )")

-   There should be no whitespace characters after the last printing
    characters on a line.

-   In method signatures, start with the access-control keyword, then
    the return-type, i.e.,

```java
       public int foobar() {
           ...
       }
```

-   When in doubt, copy the style used in existing JavaMail code.

* * * * *

If using vi, try the following:

1.  Either set up your EXINIT variable or a $HOME/.exrc file with:
    <pre>
    set autoindent
    set tabstop=8
    set shiftwidth=4
    </pre>
2.  Use Ctrl-t to indent forward one level
3.  Use Ctrl-d to indent backwards one level
4.  To indent a range like 10 lines starting at the current line use "10\>\>"
5.  To indent backwards use "\<\<" instead of "\>\>"

Using the actual tab key and spacing over will work, but it slows you down.
