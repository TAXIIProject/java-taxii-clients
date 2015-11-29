# java-taxii-clients

A set of sample client classes that use the java-taxii library to send TAXII Messages invoking TAXII Services.
For more information, see http://taxiiproject.github.io/.

[![Build Status](https://travis-ci.org/TAXIIProject/java-taxii-clients.svg?branch=master)](https://travis-ci.org/TAXIIProject/java-taxii-clients)

## Overview

This project consists of several classes that utilize the java-taxii library to 
demonstrate the use of the library. The client classes provided are patterned 
after the sample clients provided by the libtaxii Python library.

## Building

This project uses Gradle 1.12+ as the primary build tool.  See
www.gradle.org for details.

Common targets (see a complete list by running 'gradle tasks'):

    clean             - Deletes the build directory.
    build             - Builds the project, creates the jar
    allJar            - Creates a single jar containing all dependencies

The build depends on having the java-taxii library available. Download or build 
a copy of that library and place it in a directory gradle can access. 

Change the following line to point to your local copy of the java-taxii library.

    compile files('../java-taxii/build/libs/java-taxii.jar')
    
To run gradle behind a web proxy, set the following properties in a
gradle.properties file in your USER_HOME/.gradle directory. See
http://www.gradle.org/docs/current/userguide/build_environment.html#sec:accessing_the_web_via_a_proxy
for details.

```INI
systemProp.http.proxyHost=www.somehost.org
systemProp.http.proxyPort=8080
systemProp.http.proxyUser=userid
systemProp.http.proxyPassword=password
systemProp.http.nonProxyHosts=*.nonproxyrepos.com|localhost
systemProp.https.proxyHost=www.somehost.org
systemProp.https.proxyPort=8080
systemProp.https.proxyUser=userid
systemProp.https.proxyPassword=password
systemProp.https.nonProxyHosts=*.nonproxyrepos.com|localhost
```

## Executing

The simplest way to run the "allJar" task. Then run one of the scripts in the "scripts"
directory. Runnning a script with no arguments will bring up a help message.

## Feedback

Please provide feedback and/or comments on open issues to taxii@mitre.org.
