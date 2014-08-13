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
import com.fidesmo.gradle.javacard.ConvertJavacardTask

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

        def convert = project.getTasks().create("convertJavaCard", ConvertJavacardTask)

        convert.configure {
            group = 'build'
            description = 'Create a CAP file for installation on a smart card'
            dependsOn(project.compileJava)
        }

        convert.conventionMapping.aid = { jcExtension.aid }
        convert.conventionMapping.packagePath = { jcExtension.sourcePackage }
        convert.conventionMapping.version = { jcExtension.version }
        convert.conventionMapping.executableModules = { jcExtension.applets }
    }
}
