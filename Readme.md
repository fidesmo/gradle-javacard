Compling javacard applets with gradle
-------------------------------------

Currently only the creation of a single CAP file from a specific package is supported (Or more to
put it the other way round: creation of EXP files is not yet supported).


Usage
-----

In order to use this plugin you need to get into your local maven repository by executing:

    git clone https://github.com/fidesmo/gradle-javacard.git
    gradle publishToMavenLocal

And include at least the following into the build.gradle of your project

    import com.fidesmo.gradle.javacard.JavacardPlugin
    apply plugin: 'java'
    apply plugin: JavacardPlugin

    buildscript {
        repositories {
            mavenLocal()
        }
    }

    dependencies {
        classpath 'com.fidesmo.gradle:javacard:0.1-SNAPSHOT'
    }

    cap {
        aid = '0x12:0x34:0x00:0x00:0x00:0x01'
        sourcePackage = 'com.example.my.jc.code'
        applets = [ '0x12:0x34:0x00:0x00:0x00:0x01:0x01': 'Applet' ]
        version = '1.0'
    }

Before running the compilation you need to download the JavaCard SDK. The current implementation is
tested using Linux and the `Java Card Development Kit 2.2.2`. It is supposed the to work also for
the all other `Java Card Development Kit 2.x.x` under MacOSx, Windows and Linux and as well with the
`Java Card Classic Development Kit 3.x.x` under Windows.

The applet is compiled running the cap target, and can found afterwards in `build/caps`.
