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

import org.gradle.api.GradleException

class CapExtension {

    static final String NAME = "cap"

    String aid
    String sourcePackage
    Map<String, String> applets
    String version

    def validate() {
        String aidRegEx = '^(0x[0-9A-Fa-f]{1,2}(:|$)){5,16}'
        if (!aid.matches(aidRegEx)) {
            throw new GradleException('Invalid aid for CAP')
        }

        if(!sourcePackage.matches('^([a-zA-Z_]\\w*(\\.|$))+')) {
            throw new GradleException('Invalid sourcePackage name for CAP')
        }

        if(!version.matches('^\\d+\\.\\d+$')) {
            throw new GradleException('Invalid version format for CAP')
        }

        applets.each { aid, className ->
            if(!aid.matches(aidRegEx)) {
                throw new GradleException("Invalid aid for applet '${className}'")
            }

            if(!className.matches('^[a-zA-Z_]\\w*$')) {
                throw new GradleException("Invalid class name '${className}'")
            }
        }
    }
}
