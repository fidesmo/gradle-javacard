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

import org.junit.Test
import org.junit.Before
import org.gradle.api.InvalidUserDataException

class JavacardExtensionTest {

    JavacardExtension ext

    @Before void intializeValidExtension() {
        ext = new JavacardExtension()
        ext.aid = '0x01:0x02:0x03:0x04:0x05'
        ext.sourcePackage = 'org.example.javacard.test'
        ext.version = '1.0'
        ext.applets = [ '0x01:0x02:0x03:0x04:0x05:0x01': 'Applet' ]

    }

    @Test(expected = InvalidUserDataException)
    void rejectsToInvalidAids() {
        ext.aid = 'sdasdasdasd'
        ext.validate()
    }
   
    @Test(expected = InvalidUserDataException)
    void rejectsToShortAids() {
        ext.aid = '0x01:0x02:0x03:0x04'
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void rejectsInvalidSourcePackage() {
        ext.sourcePackage = 'com.1domain.blubb'
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void rejectsInvalidVersion() {
        ext.sourcePackage = '1.0alpha'
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void rejectsInvalidAppletAid() {
        ext.applets = [ '0x01:0x02': 'Applet' ]
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void rejectsInvalidAppletName() {
        ext.applets = [ '0x01:0x02:0x03:0x04:0x05': 'package.Applet' ]
        ext.validate()
    }
}
