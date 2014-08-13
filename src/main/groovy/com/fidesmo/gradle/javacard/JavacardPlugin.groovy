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

    def javacardHome = System.env['JC_HOME']

    void apply(Project project) {

        if (!javacardHome) {
            throw new InvalidUserDataException('JC_HOME must be set in order to use javacard plugin')
        } else if(! project.file(javacardHome).isDirectory()) {
            throw new InvalidUserDataException('JC_HOME must point to a valid directory')
        }

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
            javacardTools
        }

        project.dependencies {
            javacardTools project.files("${javacardHome}/ant-tasks/lib/jctasks.jar")
            javacardTools project.files("${javacardHome}/lib/converter.jar")
            javacardTools project.files("${javacardHome}/lib/offcardverifier.jar")
            compile project.files("${javacardHome}/lib/api.jar")
        }

        addConvertTask(project, jcExtension)
    }


    private def addConvertTask(Project project, JavacardExtension jcExtension) {

        def convert = project.getTasks().create("convertJavacard", ConvertJavacardTask)

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
