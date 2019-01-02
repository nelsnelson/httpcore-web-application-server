# httpcore-web-application-server

A minimalist implementation of a Java web application server based on the Apache httpcore library.


## Dependencies



### Apache Maven 3.6.0

https://maven.apache.org/download.cgi

https://maven.apache.org/install.html


#### Download

Download maven.

```{sh}
curl --location --silent --insecure http://apache.spinellicreations.com/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.tar.gz > apache-maven-3.6.0-bin.tar.gz
```


#### Install

Install maven into your user Java library.

```{sh}
tar -zxf apache-maven-3.6.0-bin.tar.gz
rm -f apache-maven-3.6.0-bin.tar.gz
mkdir -p ~/Library/Java
mv apache-maven-3.6.0 ~/Library/Java/
rm -f apache-maven-3.6.0-bin.tar.gz
```

### HttpComponents

Of course.

https://hc.apache.org/index.html
https://hc.apache.org/downloads.cgi
https://www-eu.apache.org/dist//httpcomponents/httpcore/binary/httpcomponents-core-5.0-beta6-bin.tar.gz

cp httpcomponents-core-5.0-beta6/lib/httpcore5-5.0-beta6.jar lib/
cp httpcomponents-core-5.0-beta6/lib/commons-cli-1.4.jar lib/
cp httpcomponents-core-5.0-beta6/lib/slf4j-api-1.7.25.jar lib/


### servlet-api

#### Source

https://github.com/eclipse-ee4j
https://github.com/eclipse-ee4j/servlet-api
https://github.com/eclipse-ee4j/servlet-api/archive/4.0.2-RELEASE.tar.gz

#### Spec

https://javaee.github.io/servlet-spec/

#### Artifact

https://jcp.org/aboutJava/communityprocess/final/jsr369/index.html
https://maven.java.net/content/repositories/releases/javax/servlet/javax.servlet-api/4.0.0/
https://maven.java.net/content/repositories/releases/javax/servlet/javax.servlet-api/4.0.0/javax.servlet-api-4.0.0.jar
https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api

### Java API for JSON Processing 1.1

https://javaee.github.io/jsonp/
http://download.oracle.com/otndocs/jcp/json_p-1_1-pr-spec/index.html


### Java EE Reference Implementation

http://download.java.net/glassfish/5.0/release/javaee8-ri.zip
https://javaee.github.io/glassfish/downloads/ri/README


#### Java Servlets

#### Java API for JSON Binding

https://projects.eclipse.org/projects/rt.yasson/downloads

