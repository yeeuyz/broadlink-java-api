# broadlink-java-api [![Build Status](https://travis-ci.org/mob41/broadlink-java-api.svg?branch=master)](https://travis-ci.org/mob41/broadlink-java-api) [![CodeFactor](https://www.codefactor.io/repository/github/mob41/broadlink-java-api/badge)](https://www.codefactor.io/repository/github/mob41/broadlink-java-api) [![codecov](https://codecov.io/gh/mob41/broadlink-java-api/branch/master/graph/badge.svg)](https://codecov.io/gh/mob41/broadlink-java-api) [![Maven Central](https://img.shields.io/maven-central/v/com.github.mob41.blapi/broadlink-java-api.svg)](http://central.maven.org/maven2/com/github/mob41/blapi/broadlink-java-api)

A clean Java API to Broadlink devices!

This is a Java version of [mjg59](https://github.com/mjg59)'s [python-broadlink](https://github.com/mjg59/python-broadlink) library.



**yeeuyz**: 

This is a patched version of [a1aw](https://github.com/a1aw)‘s [broadlink-java-api](https://github.com/a1aw/broadlink-java-api) library.

Add support of broadlink Smart Plug 4 series.

```java
 public static final short DEV_SP4L_CN = 0x7568; // added, tested
 public static final short DEV_SPMINI_3 = 0x7d11; // added, tested
 public static final short DEV_SP4M_JP = 0x756B; // added
 public static final short DEV_SP4M = 0x756C; // added
 public static final short DEV_MCB1 = 0x756F; // added
 public static final short DEV_SP4L_EU = 0x7579; // added
 public static final short DEV_SP4L_AU = 0x757B; // added
 public static final short DEV_SPMINI_3_2 = 0x7583; // added
 public static final short DEV_SP4L_UK = 0x7587; // added

 // The following models are not supported because their lengths exceed
 // 'short', and I don't want to easily modify the variable types in the
 // subsequent code. Making changes lightly may cause bugs on devices
 // such as SP1 and SP2, which I do not own and cannot test.
 public static final int DEV_WS4 = 0xA4F9;
 public static final int DEV_SP4L_UK_2 = 0xA569;
 public static final int DEV_MCB1_2 = 0xA56A;
 public static final int DEV_SCB1E = 0xA56B;
 public static final int DEV_SP4L_EU_2 = 0xA56C;
 public static final int DEV_SP4L_AU_2 = 0xA576;
 public static final int DEV_SP4L_UK_3 = 0xA589;
 public static final int DEV_SP4L_EU_3 = 0xA5D3;
 public static final int DEV_SP4D_US = 0xA6F4;
```

## Adding this API

This API is distributed via **Maven Central**. You can import via adding this as dependency in Maven ```pom.xml``` or clone/download this as ZIP and import in IDE.

> [![Maven Central](https://img.shields.io/maven-central/v/com.github.mob41.blapi/broadlink-java-api.svg)](http://central.maven.org/maven2/com/github/mob41/blapi/broadlink-java-api) [![Maven Snapshot](https://img.shields.io/maven-metadata/v/http/oss.sonatype.org/content/repositories/snapshots/com/github/mob41/blapi/broadlink-java-api/maven-metadata.xml.svg?maxAge=2592000&label=maven%20snapshot)](https://oss.sonatype.org/content/repositories/snapshots/com/github/mob41/blapi/broadlink-java-api/) [![Maven Latest](https://img.shields.io/maven-metadata/v/http/oss.sonatype.org/content/groups/public/com/github/mob41/blapi/broadlink-java-api/maven-metadata.xml.svg?maxAge=2592000&label=maven%20latest)](https://oss.sonatype.org/content/groups/public/com/github/mob41/blapi/broadlink-java-api/) 

1. **Via Maven Central**: (Release Builds) Add the following to your ```pom.xml``` under ```<dependencies>```
   
   ```xml
   <dependency>
       <groupId>com.github.mob41.blapi</groupId>
       <artifactId>broadlink-java-api</artifactId>
       <version>1.0.1</version>
   </dependency>
   ```

2. **Via OSSRH Snapshots**: (Development Builds) To access snapshots/development builds e.g. ```1.0.1-SNAPSHOT```, you have to add the OSSRH snapshot repository
   
   ```xml
   <profiles>
     <profile>
        <id>allow-snapshots</id>
           <activation><activeByDefault>true</activeByDefault></activation>
        <repositories>
          <repository>
            <id>snapshots-repo</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
            <releases><enabled>false</enabled></releases>
            <snapshots><enabled>true</enabled></snapshots>
          </repository>
        </repositories>
      </profile>
   </profiles>
   ```

3. **Via Cloning**: (Eclipse) Clone the project via ```git clone https://github.com/mob41/broadlink-java-api.git``` or via the [download ZIP](https://github.com/mob41/broadlink-java-api/archive/master.zip) and extract the ZIP to a folder.
   
   And add the project into your Eclipse IDE by right clicking the ```Package Explorer```, and,
   ```Import...``` -> ```Maven``` -> ```Existing Maven Projects```
   
   Select the folder you cloned/downloaded broadlink-java-api to. And select the ```pom.xml``` inside.
   
   The project should be added. And the following you have to do is add the following dependency to your ```pom.xml```:
   
   ```xml
    <dependency>
        <groupId>com.github.mob41.blapi</groupId>
        <artifactId>broadlink-java-api</artifactId>
        <version>1.0.1</version>
    </dependency>
   ```

## Tutorial-SP4

1. Import necessary libraries
   
   ```java
   import com.github.mob41.blapi.BLDevice;
   import com.github.mob41.blapi.SP4Device;
   import com.github.mob41.blapi.mac.Mac;
   ```

2. Creating/Discovering ```BLDevice``` instances by two methods:
   
   ```java
   //
   // === Method 1. By Discovering Devices In Local Network ===
   //
   
   BLDevice[] devs = BLDevice.discoverDevices(); //Default with 10000 ms (10 sec) timeout, search for multiple devices
   
   //BLDevice[] devs = BLDevice.discoverDevices(0); //No timeout will block the thread and search for one device only
   //BLDevice[] devs = BLDevice.discoverDevices(5000); //With 5000 ms (5 sec) timeout
   
   //The BLDevice[] array stores the found devices in the local network
   
   System.out.println("Number of devices: " + devs.length);
   
   BLDevice blDevice = null;
   for (BLDevice dev : devs){
       System.out.println("Type: " + Integer.toHexString(dev.getDeviceType()) + " Host: " + dev.getHost() + " Mac: " + dev.getMac());
   }
   
   //BLDevice dev = devs[0]
   
   //
   // === Method 2. Create a "RM2Device" or another "BLDevice" child according to your device type ===
   //
   
   BLDevice dev = new RM2Device("192.168.1.123", new Mac("01:12:23:34:43:320"));
   //~do stuff
   //dev.auth();
   ```

3. Before any commands like ```getTemp()``` and ```enterLearning()```, ```BLDevice.auth()``` must be ran to connect and authenticate with the Broadlink device.
   
   ```java
   boolean success = dev.auth();
   System.out.println("Auth status: " + (success ? "Success!" : "Failed!"));
   ```

4. Every <code>BLDevice</code> has its very own methods. Please refer to their own source code in the repository (as the main documentation still not completed...). Here's an example:
   
   ```java
   if(dev instanceof SP4Device)
   {
       SP4Device spd = (SP4Device) dev;
   
       // Obtain remote signaling
       Map<String, Object> status= spd.getState();
   
       // Note: Since the keys in the messages returned by the SP are
       // enclosed in double quotes, make sure to add double quotes to
       // the key names when retrieving their values.
       System.out.println("getState pwr: " + status.get("\"pwr\""));
       System.out.println("getState ntlight: " + status.get("\"ntlight\""));
       System.out.println("getState indicator: " + status.get("\"indicator\""));
       System.out.println("getState ntlbrightness: " + status.get("\"ntlbrightness\""));
       System.out.println("getState maxworktime: " + status.get("\"maxworktime\""));
       System.out.println("getState childlock: " + status.get("\"childlock\""));
   
       // Execute remote control
       spd.setState(false, false, true, 100, 0, false);
   }
   ```

## Tutorial

1. Import necessary libraries
   
   ```java
   import com.github.mob41.blapi.BLDevice;
   import com.github.mob41.blapi.RM2Device; //Necessary if using <code>2.ii</code>
   import com.github.mob41.blapi.mac.Mac; //Necessary if using <code>2.ii</code>
   ```

2. Creating/Discovering ```BLDevice``` instances by two methods:
   
   ```java
   //
   // === Method 1. By Discovering Devices In Local Network ===
   //
   
   BLDevice[] devs = BLDevice.discoverDevices(); //Default with 10000 ms (10 sec) timeout, search for multiple devices
   
   //BLDevice[] devs = BLDevice.discoverDevices(0); //No timeout will block the thread and search for one device only
   //BLDevice[] devs = BLDevice.discoverDevices(5000); //With 5000 ms (5 sec) timeout
   
   //The BLDevice[] array stores the found devices in the local network
   
   System.out.println("Number of devices: " + devs.length);
   
   BLDevice blDevice = null;
   for (BLDevice dev : devs){
       System.out.println("Type: " + Integer.toHexString(dev.getDeviceType()) + " Host: " + dev.getHost() + " Mac: " + dev.getMac());
   }
   
   //BLDevice dev = devs[0]
   
   //
   // === Method 2. Create a "RM2Device" or another "BLDevice" child according to your device type ===
   //
   
   BLDevice dev = new RM2Device("192.168.1.123", new Mac("01:12:23:34:43:320"));
   //~do stuff
   //dev.auth();
   ```

3. Before any commands like ```getTemp()``` and ```enterLearning()```, ```BLDevice.auth()``` must be ran to connect and authenticate with the Broadlink device.
   
   ```java
   boolean success = dev.auth();
   System.out.println("Auth status: " + (success ? "Success!" : "Failed!"));
   ```

4. Every <code>BLDevice</code> has its very own methods. Please refer to their own source code in the repository (as the main documentation still not completed...). Here's an example:
   
   ```java
   if (dev instanceof RM2Device){
       RM2Device rm2 = (RM2Device) dev;
   
       boolean success = rm2.enterLearning();
       System.out.println("Enter Learning status: " + (success ? "Success!" : "Failed!"));
   
       float temp = rm2.getTemp();
       System.out.println("Current temperature reported from RM2: " + temp + " degrees");
   } else {
       System.out.println("The \"dev\" is not a RM2Device instance.");
   }
   ```

## License

[tl;dr](https://tldrlegal.com/license/mit-license) This project is licensed under the MIT License.