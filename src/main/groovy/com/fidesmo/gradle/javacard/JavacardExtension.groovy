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

import org.gradle.api.InvalidUserDataException

class JavacardExtension {

    static final String NAME = "javacard"

    static class Aid {

        String string

        def Aid(String aidStr) {
            string = aidStr
        }

        def validate() {
            if (!string.matches('^(0x[0-9A-Fa-f]{1,2}(:|$)){5,16}')) {
                throw new InvalidUserDataException('Invalid aid for CAP')
            }
        }
    }

    static class Cap {
        static class Applet {
            Aid aid
            String className

            def setAid(String aidStr) {
                aid = new Aid(aidStr)
            }

            def validate() {
                aid.validate()

                if(!className.matches('^[a-zA-Z_]\\w*$')) {
                    throw new InvalidUserDataException("Invalid class name '${className}'")
                }
            }
        }

        Aid aid
        String packageName
        List<Applet> applets = []
        String version

        Applet applet(Closure closure) {
            def newApplet = new Applet()
            closure.delegate = newApplet
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call()
            applets.add(newApplet)
            newApplet
        }

        def setAid(String aidStr) {
            aid = new Aid(aidStr)
        }

        def validate() {
            aid.validate()

            if(!version.matches('^\\d+\\.\\d+$')) {
                throw new InvalidUserDataException('Invalid version format for CAP')
            }

            if(!packageName.matches('^([a-zA-Z_]\\w*(\\.|$))+')) {
                throw new InvalidUserDataException('Invalid sourcePackage name for CAP')
            }

            if(applets.size() == 0) {
                throw new InvalidUserDataException('At least on applet should be specified')
            }

            applets.each { applet ->
                applet.validate()
            }
        }
    }

    Cap cap;

    Cap cap(Closure closure) {
        cap = new Cap()
        closure.delegate = cap
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
        return cap
    }

    def validate() {
        cap.validate()
    }
}
