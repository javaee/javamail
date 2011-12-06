@echo off
REM
REM  Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
REM
REM  Redistribution and use in source and binary forms, with or without
REM  modification, are permitted provided that the following conditions
REM  are met:
REM
REM    - Redistributions of source code must retain the above copyright
REM      notice, this list of conditions and the following disclaimer.
REM
REM    - Redistributions in binary form must reproduce the above copyright
REM      notice, this list of conditions and the following disclaimer in the
REM      documentation and/or other materials provided with the distribution.
REM
REM    - Neither the name of Oracle nor the names of its
REM      contributors may be used to endorse or promote products derived
REM      from this software without specific prior written permission.
REM
REM  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
REM  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
REM  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
REM  PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
REM  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
REM  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
REM  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
REM  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
REM  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
REM  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
REM  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
REM
mkdir src\docroot\WEB-INF\classes
mkdir src\docroot\WEB-INF\classes\demo
mkdir src\docroot\WEB-INF\lib
cd src\classes
echo compiling classes directory
javac -d ..\docroot\WEB-INF\classes demo\*.java
cd ..\taglib
echo compiling lib directory
javac -classpath "..\docroot\WEB-INF\classes;%CLASSPATH%" demo\*.java
echo creating tag library archive
jar cvf ..\docroot\WEB-INF\lib\taglib.jar META-INF demo\*.class
del demo\*.class
cd ..\docroot
echo creating web archive
jar cvf ..\..\javamail.war index.html *.jsp WEB-INF
cd WEB-INF\classes\demo
del *.*
cd ..
rmdir demo
cd ..
rmdir classes
cd lib
del *.*
cd ..
rmdir lib
cd ..\..\..
