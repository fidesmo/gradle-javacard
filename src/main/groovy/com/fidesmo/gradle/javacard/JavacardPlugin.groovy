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

    static def getJavacardHomeUnchecked(Project project) {
        project.properties['com.fidesmo.gradle.javacard.home'] ?: System.env['JC_HOME']
    }

    static def getJavacardHome(Project project) {
        def javacardHome = getJavacardHomeUnchecked(project)

        if (!javacardHome) {
            throw new InvalidUserDataException('Java card home must be set in order to use javacard plugin')
        } else if(! project.file(javacardHome).isDirectory()) {
            throw new InvalidUserDataException('Java card home must point to a valid directory')
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

            def jcardsim = project.configurations.getByName('testCompile').dependencies.find {
                it.name == 'jcardsim'
            }

            // check if Java card home is not set and if jcardsim was available and in that case use jcardsim api
            // this is used to run tests and compile if no javacard sdk is available (e.g ci systems)
            if (jcardsim != null && getJavacardHomeUnchecked(project) == null) {
                project.logger.info('Using jcardsim as replacement for JC_HOME/lib/api.jar, due to missing Java card home.')
                project.dependencies {
                    compile "com.licel:jcardsim:${jcardsim.version}"
                }
            } else {

                def apiJar = ""
                if (jcExtension.sdkVersion.matches("3.0.[0-4]")) {
                    apiJar = "${getJavacardHome(project)}/lib/api_classic.jar"
                } else {
                    apiJar = "${getJavacardHome(project)}/lib/api.jar"
                }

                project.dependencies {
                    compile project.files(apiJar)
                }

                if (jcardsim != null) {
                    project.sourceSets {
                        test {
                            runtimeClasspath -= project.files(apiJar)
                        }
                    }
                }
            }


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

        addConvertTask(project, jcExtension)
    }


    private def addConvertTask(Project project, JavacardExtension jcExtension) {

        def convert = project.tasks.create("convertJavacard", ConvertJavacardTask)

        convert.configure {
            group = 'build'
            description = 'Create a CAP file for installation on a smart card'
            dependsOn(project.classes)
        }

        project.build.dependsOn(convert)

        convert.conventionMapping.aid = { jcExtension.cap.aid.string }
        convert.conventionMapping.fullyQualifiedPackageName = { jcExtension.cap.packageName }
        convert.conventionMapping.version = { jcExtension.cap.version }
        convert.conventionMapping.applets = { jcExtension.cap.applets.collectEntries{[(it.aid.string): it.className]}}
        convert.conventionMapping.sdkVersion = { jcExtension.sdkVersion }
    }
}
