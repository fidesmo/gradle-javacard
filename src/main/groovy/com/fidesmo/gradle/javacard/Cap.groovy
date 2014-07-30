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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile

class Cap extends DefaultTask {

    String group = 'build'
    String description = 'Create a cap for installation on a smart card'

    CapExtension extension;

    @Input
    String classesDir

    @Input
    String capsDir

    @Input
    String javacardHome

    @InputDirectory
    File getSourcePackagePath() {
        extension.validate()
        new File(classesDir + File.separator + extension.sourcePackage.replace('.', File.separator))
    }

    @OutputFile
    File getCapFile() {
        extension.validate()
        new File(capsDir + File.separator + extension.sourcePackage.replace('.', File.separator) + '/javacard/ndef.cap') // FIXME: hard coded stuff
    }


    protected def osDependent(Map<String, Object> options) {
        def os = System.getProperty('os.name').toLowerCase()
        if(os.contains('windows') && options.contains('windows')) {
            options['windows']
        } else {
            options['others']
        }
    }

    protected def findExecutable(String name) {
        [ javacardHome, 'bin', osDependent([windows: "${name}.bat", others: name])].join(File.separator)
    }

    @TaskAction
    def create() {
        extension.validate()
        project.exec {
            commandLine(findExecutable('converter'))
            args([ '-out', 'CAP',
                   '-d',  capsDir,
                   '-classdir', classesDir,
                   '-exportpath', "${javacardHome}${File.separator}api_export_files" ])

            args(extension.applets.collect {
                     aid, className -> [ '-applet', aid, extension.sourcePackage + '.' + className ] } .flatten())

            args([ extension.sourcePackage, extension.aid, extension.version ])
        }
    }
}
