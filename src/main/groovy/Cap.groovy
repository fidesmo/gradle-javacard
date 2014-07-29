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

    @InputDirectory
    File getSourcePackagePath() {
        new File(classesDir + File.separator + extension.sourcePackage.replace('.', File.separator))
    }

    @OutputFile
    File getCapFile() {
        new File(capsDir + File.separator + extension.sourcePackage.replace('.', File.separator) + '/javacard/ndef.cap') // FIXME: hard coded stuff
    }

    @TaskAction
    def create() {

        def javacardHome = '/home/yves/opt/java-card-sdk' // FIXME: make configurable

        def commandWithOptions = [
            "${javacardHome}${File.separator}bin${File.separator}converter",
            '-out CAP',
            '-d',  capsDir,
            '-classdir', classesDir,
            '-exportpath', "${javacardHome}${File.separator}api_export_files"
        ]

        def appletOptions = extension.applets.collect { aid, className -> [ '-applet', aid, extension.sourcePackage + '.' + className ] }        
        def arguments = [ extension.sourcePackage, extension.aid, extension.version ]
        def translateCommand = (commandWithOptions << appletOptions << arguments).flatten().join(' ')

        // FIXME: add as seperate task
        def proc = translateCommand.execute()
        proc.waitFor()

        if(proc.exitValue() != 0) {
            println translateCommand
            println proc.in.text
            println proc.err.text
            assert(proc.exitValue() == 0)
        }
    }
}
