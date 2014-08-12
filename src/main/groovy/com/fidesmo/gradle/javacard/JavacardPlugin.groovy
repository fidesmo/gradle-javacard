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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Exec

class JavacardPlugin implements Plugin<Project> {

    // FIXME: abort build if this is not set
    def javaCardHome = System.env.JC_HOME

    void apply(Project project) {

         if (!project.plugins.hasPlugin(JavaPlugin)) {
             project.plugins.apply(JavaPlugin)
         }

        // configure java build
        project.sourceCompatibility = '1.2'
        project.targetCompatibility = '1.2'

        // FIXME: support multiple packages
        def jcExtension = project.extensions.create(JavacardExtension.NAME, JavacardExtension)
        project.afterEvaluate {
            jcExtension.validate()
        }

        project.configurations {
            javaCardTools
        }

        project.dependencies {
            javaCardTools project.files("${javaCardHome}/ant-tasks/lib/jctasks.jar")
            javaCardTools project.files("${javaCardHome}/lib/converter.jar")
            javaCardTools project.files("${javaCardHome}/lib/offcardverifier.jar")
            compile project.files("${javaCardHome}/lib/api.jar")
        }

        addConvertTask(project, jcExtension)
    }


    private def addConvertTask(Project project, JavacardExtension jcExtension) {

        def convert = project.getTasks().create("convertJavaCard") {
            group = 'build'
            description = 'Create a cap for installation on a smart card'
            dependsOn(project.compileJava)

            ext.executableModules = [ '0xd2:0x76:0x00:0x00:0x85:0x01:0x01': 'de.spline.uves.ndef.Ndef' ]
            ext.packagePath = 'de.spline.uves.ndef'
            ext.aid = '0xd2:0x76:0x00:0x00:0x85:0x01'
            ext.version = '1.0'
            ext.javaCardDirectory = new File(project.getBuildDir(), 'javacard')

            def packageFilepath = packagePath.replace('.', File.separator)
            def packageName = "ndef" // FIXME:

            ext.capFile = "${javaCardDirectory}/${packageFilepath}/javaCard/${packageName}.cap"
            ext.extFile = "${javaCardDirectory}/${packageFilepath}/javaCard/${packageName}.ext"
            ext.classesDir = project.sourceSets.main.output.classesDir

            inputs.dir classesDir
            outputs.file capFile
            outputs.file extFile

            doLast {
                ant.taskdef(name: 'convert',
                            classname: 'com.sun.javacard.ant.tasks.ConverterTask',
                            classpath: project.configurations.javaCardTools.asPath)

                ant.convert(CAP: true,
                            EXP: true,
                            packagename: packagePath,
                            packageaid: aid,
                            majorminorversion: version,
                            debug: true,
                            classdir: classesDir,
                            outputdirectory: new File(project.getBuildDir(), 'javacard'),
                            exportpath: "${javaCardHome}/api_export_files",
                            classpath: project.configurations.javaCardTools.asPath) {

                    executableModules.each() { emAid, fqImplementorClass ->
                        AppletNameAID(appletname: fqImplementorClass, aid: emAid )
                    }
                }
            }
        }

        // cap.conventionMapping.sourcePackage = { capExtension.sourcePackage }
        // cap.conventionMapping.aid = { capExtension.aid }
        // cap.conventionMapping.version = { capExtension.version }
        // cap.conventionMapping.applets = { capExtension.applets }
        // cap.conventionMapping.javacardHome = { jcExtension.sdk }
    }
}
