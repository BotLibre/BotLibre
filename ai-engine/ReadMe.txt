This directory includes the Bot Libre AI engine.

The Bot Libre AI engine is written in Java.  It requires a database to run (PostgreSQL).

Bot Libre includes an AIML and Self compiler/interpreter.

This component only includes the AI engine, the web and REST interfaces are separate components.

The prebuild jar file is in /lib/botlibre-ai.jar

To build the source code the ant command line build tool is used.
You can download ant from, http://ant.apache.org/
To build run ant, or the build.bat file.

The botlibre-ai.jar is just a library.  It is not runnable, and does not contain any interface.  It can be used from a Java web server such as Tomcat, or a standalone Java application.
The AI engine has not yet been ported or tested on Android.  To run on Android the database would need to be changed to use an Android compatible database.

The AI engine is not required for the Android and iOS SDK.  The SDK accesses the free Bot Libre server.  The AI engine library is only required to create your own server, or standalone application.

A GUI and examples can be found in the ai-engine-test directory.  That is the best place to start.
