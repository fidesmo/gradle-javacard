Compling javacard applets with gradle
-------------------------------------

[![Build Status](https://travis-ci.org/fidesmo/gradle-javacard.svg?branch=master)](https://travis-ci.org/fidesmo/gradle-javacard)

This plugins allows to translate compiled class files to the converted archive format. These files
can be used for installation on smart and sim card supporting SUN/Oracel JavaCard Technology.

Features
--------

  - convert a package of class files into a cap file
  - specify multiple classes implementing Applet for later installation  

Usage
-----

Include the new version of the plugin from OSS Sonatype snapshot, by adding the following to your
`build.gradle`:

    apply plugin: 'javacard'

    buildscript {
        repositories {
            maven {
                url 'https://oss.sonatype.org/content/repositories/snapshots/'
            }
        }

        dependencies {
            classpath 'com.fidesmo.gradle:javacard:0.2-SNAPSHOT'
        }
    }

    javacard {
        cap {
             aid = '0x12:0x34:0x00:0x00:0x00:0x01'
             packageName = 'com.example.my.jc.code'
             applet {
                aid = '0x12:0x34:0x00:0x00:0x00:0x01:0x01'
                className = 'ClassImplementingApplet'
             }
             version = '1.0'
         }
     }

Before running the compilation you need to download the JavaCard SDK. The current implementation is
tested using Linux and the `Java Card Development Kit 2.2.2`. It is supposed the to work also for
the all other `Java Card Development Kit 2.x.x` under MacOSx, Windows and Linux.

The specified package is converted from jvm byte code to javaCard byte code, by running the
`convertJavaCard` task.

     export JC_HOME='path/to/javaCardSdk'
     gradle convertJavacard
