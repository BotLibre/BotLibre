This directory includes the Bot Libre Micro AI engine.

The Bot Libre Micro AI engine is written in Java and optimized for Android.
It is a light-weight version of the AI engine that stores to a local file instead of a database.

Bot Libre includes an AIML and Self compiler/interpreter.

This component only includes the AI engine, the Android SDK and apps are separate components.

The prebuild jar file is in /lib/botlibre-micro.jar

To build the source code the ant command line build tool is used.
You can download ant from, http://ant.apache.org/
To build run the build.bat file, or run ant,
ant

The project is developed in Eclipse.  There is an Eclipse .project file in the root directory.
You can download Eclipse from, https://eclipse.org/

The botlibre-micro.jar is just a library.  It is not runnable, and does not contain any interface.

The AI engine is not required for the Android and iOS SDK.  The SDK accesses the free Bot Libre server.  The micro AI engine library is only required to store and run the bot locally on the Android device.

The micro AI engine does not support all of the features of the full version of Bot Libre.
