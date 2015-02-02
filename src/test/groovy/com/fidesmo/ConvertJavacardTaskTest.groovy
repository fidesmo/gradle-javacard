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
import org.gradle.testfixtures.ProjectBuilder

import org.junit.Test
import org.junit.BeforeClass

import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.*

class ConvertJavacardTaskTest {

    private static Project project
    private static ConvertJavacardTask task

    // valid test data
    static def testAid = '0x01:0x02:0x03:0x04:0x05'
    static def testSourcePackage = 'org.example.javacard.test'
    static def testVersion = '1.0'
    static def testApplets = [ '0x01:0x02:0x03:0x04:0x05:0x01': 'Applet' ]


    @BeforeClass static void setupTask() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply('java')
        task = project.getTasks().create('convertJavacard', ConvertJavacardTask)
        task.configure {
            aid = testAid
            fullyQualifiedPackageName = testSourcePackage
            version = testVersion
            applets = testApplets
        }
    }


    @Test void inputs() {
        assertThat(task.getClassesDir().path, equalTo(project.file('build/classes/main/org/example/javacard/test').path))
    }

    @Test void outputs() {
        assertThat(task.getCapFile().path, equalTo(project.file('build/javacard/org/example/javacard/test/javacard/test.cap').path))
        assertThat(task.getExpFile().path, equalTo(project.file('build/javacard/org/example/javacard/test/javacard/test.exp').path))
    }
}
