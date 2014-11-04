/*
 * Copyright 2014 Fidesmo AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fidesmo.gradle.javacard

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.InvalidUserDataException
import org.gradle.api.plugins.JavaPlugin
import com.fidesmo.gradle.javacard.ConvertJavacardTask


class JavacardPlugin implements Plugin<Project> {

    static def getJavacardHome(Project project) {
        def javacardHome = System.env['JC_HOME']

        if (!javacardHome) {
            throw new InvalidUserDataException('JC_HOME must be set in order to use javacard plugin')
        } else if(! project.file(javacardHome).isDirectory()) {
            throw new InvalidUserDataException('JC_HOME must point to a valid directory')
        }

        javacardHome
    }


    void apply(Project project) {

        if (!project.plugins.hasPlugin(JavaPlugin)) {
            project.plugins.apply(JavaPlugin)
        }

        // configure java build
        project.compileJava {
            sourceCompatibility = '1.2'
            targetCompatibility = '1.2'
        }

        // FIXME: support multiple packages
        def jcExtension = project.extensions.create(JavacardExtension.NAME, JavacardExtension)
        project.afterEvaluate {
            // validate extension
            jcExtension.validate()
       }

        project.configurations {
            javacardTools {
                visible = false
            }
            javacardExport {
                visible = false
            }
        }

        // check if JC_HOME is not set add jcardsim from maven central
        // this is used to run tests and compile if no javacard sdk is available (e.g ci systems)
        if (!System.env['JC_HOME']) {
            project.logger.info("Using jcardsim as replacement for JC_HOME/lib/api.jar, due to missing JC_HOME.")

            project.repositories {
                mavenCentral()
            }

            project.dependencies {
                compile 'com.licel:jcardsim:2.2.2'
            }
        } else {
            project.dependencies {
                compile project.files("${getJavacardHome(project)}/lib/api.jar")
            }
        }

        addConvertTask(project, jcExtension)
    }


    private def addConvertTask(Project project, JavacardExtension jcExtension) {

        def convert = project.tasks.create("convertJavacard", ConvertJavacardTask)

        convert.configure {
            group = 'build'
            description = 'Create a CAP file for installation on a smart card'
            dependsOn(project.compileJava)
        }

        project.build.dependsOn(convert)

        convert.conventionMapping.aid = { jcExtension.cap.aid.string }
        convert.conventionMapping.fullyQualifiedPackageName = { jcExtension.cap.packageName }
        convert.conventionMapping.version = { jcExtension.cap.version }
        convert.conventionMapping.applets = { jcExtension.cap.applets.collectEntries{[(it.aid.string): it.className]}}
    }
}
