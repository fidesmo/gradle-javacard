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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Test
import org.junit.BeforeClass

import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.*

class JavacardPluginTest {

    private static Project project

    // valid test data
    def testAid = '0x01:0x02:0x03:0x04:0x05'
    def testSourcePackage = 'org.example.javacard.test'
    def testVersion = '1.0'
    def testApplets = [ '0x01:0x02:0x03:0x04:0x05:0x01': 'Applet' ]

    @BeforeClass static void setUpProject() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'javacard'
    }


    @Test void checkJavacardHomeSetup() {
        def plugin = project.getPlugins().findPlugin('javacard')
        assertThat(plugin.getJavacardHome(project), equalTo('.'))
    }

    @Test void checkCompaibilityForJavaCompile() {
        def task = project.getTasks().findByPath('compileJava')
        assertThat(task.sourceCompatibility, equalTo('1.2'))
        assertThat(task.targetCompatibility, equalTo('1.2'))
    }

    @Test void appliesTheJavaPluginToTheProject() {
        assertTrue(project.getPlugins().hasPlugin(JavaPlugin))
    }

    @Test void addConvertTaskToTheProject() {
        def task = project.getTasks().findByPath('convertJavacard')
        assertThat(task, instanceOf(ConvertJavacardTask))
        assertThat(task.group, equalTo('build'))
        assertThat(task.dependsOn, hasItem(project.compileJava))
    }

    @Test void checkDependsSetup() {
        assertThat(project.build.dependsOn, hasItem(project.convertJavacard))
    }

    @Test void copyExtensionValuesToTask() {
        project.configure(project.javacard) {
            cap {
                aid = '0x01:0x02:0x03:0x04:0x05'
                packageName = 'org.example.javacard.test'

                applet {
                    aid = '0x01:0x02:0x03:0x04:0x05:0x01'
                    className = 'Applet'
                }

                version = '1.0'
            }
        }


        def task = project.getTasks().findByPath('convertJavacard')
        assertThat(task.getAid(), equalTo(testAid))
        assertThat(task.getFullyQualifiedPackageName(), equalTo(testSourcePackage))
        assertThat(task.getVersion(), equalTo(testVersion))
        assertThat(task.getApplets(), equalTo(testApplets))
    }
}
