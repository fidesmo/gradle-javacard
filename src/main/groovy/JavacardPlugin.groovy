package com.fidesmo.gradle.javacard

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.plugins.JavaBasePlugin

class JavacardPlugin implements Plugin<Project> {

    void apply(Project project) {

        // configure java build
        project.sourceCompatibility = '1.2'
        project.targetCompatibility = '1.2'

        // register extension for building cap
        CapExtension capExtension = project.extensions.create(CapExtension.NAME, CapExtension)

        project.getTasks().create('cap', Cap.class) {
            dependsOn(project.compileJava)
            extension = capExtension
            javacardHome = '/home/yves/opt/java-card-sdk'
            classesDir = project.sourceSets.main.output.classesDir.getPath()
            capsDir = project.getBuildDir().getPath() + "${File.separator}caps"
        }
    }
}


class CapExtension {

    static final String NAME = "cap"

    String aid
    String sourcePackage
    Map<String, String> applets
    String version
}
