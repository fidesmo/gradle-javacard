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

    void apply(Project project) {

         if (!project.plugins.hasPlugin(JavaPlugin)) {
             project.plugins.apply(JavaPlugin)
         }

        // configure java build
        project.sourceCompatibility = '1.2'
        project.targetCompatibility = '1.2'

        def jcExtension = project.extensions.create(JavacardExtension.NAME, JavacardExtension)
        def capExtension = jcExtension.extensions.create(CapExtension.NAME, CapExtension)
        project.afterEvaluate {
            capExtension.validate() FIXME
        }

        addCapTask(project, jcExtension, capExtension)
    }


    private def addCapTask(Project project, JavacardExtension jcExtension, CapExtension capExtension) {

        def cap = project.getTasks().create("cap", Cap)
        cap.group = 'build'
        cap.description = 'Create a cap for installation on a smart card'
        cap.dependsOn(project.compileJava)
        cap.classesDir = project.sourceSets.main.output.classesDir
        cap.capsDir = new File(project.getBuildDir(), 'caps')

        cap.conventionMapping.sourcePackage = { capExtension.sourcePackage }
        cap.conventionMapping.aid = { capExtension.aid }
        cap.conventionMapping.version = { capExtension.version }
        cap.conventionMapping.applets = { capExtension.applets }
        cap.conventionMapping.javacardHome = { jcExtension.sdk }
    }
}


