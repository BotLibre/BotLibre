This is the test component for the Bot Libre AI engine.

This directory contains several test classes written in JUnit in Java.

The prebuild jar file is in /lib/botlibre-ai-test.jar

To build the source code the ant command line build tool is used.
You can download ant from, http://ant.apache.org/
To build run ant, or the build.bat file.

The project is developed in Eclipse.  There is an Eclipse .project file in the root directory.
You can download Eclipse from, https://eclipse.org/

This component also includes a standalone GUI that can be used for testing or to create and access a bot instance.
To run the GUI run "ant gui" or the gui.bat file.

The AI engine requires a PostgreSQL database.  The default user/password is postgres/password.
To change the user/password or database, edit the file, ai-engine/source/META-INF/persistence.xml.
You will need to rebuild the botlibre-ai.jar and copy it to the ai-engine-test lib directory.

