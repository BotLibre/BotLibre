This directory includes the Bot Libre Web Platform.

The Bot Libre Web Platform is written in Java.  It requires a Java web server (Tomcat) and a database (PostgreSQL) to run.

The prebuild war file is in /web/botlibreplatform.war

To build the source code the ant command line build tool is used.
You can download ant from, http://ant.apache.org/
To build run the build.bat file, or run ant,
ant -lib .\lib\jpa\eclipselink.jar -lib .\lib\jpa\persistence.jar war
or on Linux/Mac,
ant -lib ./lib/jpa/eclipselink.jar -lib ./lib/jpa/persistence.jar war

The project is developed in Eclipse.  There is an Eclipse .project file in the root directory.
You can download Eclipse from, https://eclipse.org/

Refer to /doc/BotLibre-Web-Installation-Guide.pdf for installation.

The Twitter, Facebook, Skype, Kik, WeChat, and Google support also require developers keys from Twitter, Facebook, Microsoft, Kik, WeChat, and Google.


