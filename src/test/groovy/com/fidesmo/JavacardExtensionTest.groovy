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

import static org.junit.Assert.assertThat
import static org.hamcrest.Matchers.*

class JavacardExtensionTest {

    JavacardExtension ext

    @Before void intializeValidExtension() {
        ext = new JavacardExtension()

        def configClosure = {
            cap {
                aid = '0x01:0x02:0x03:0x04:0x05'
                packageName = 'org.example.javacard.test'

                applet {
                    aid = '0x01:0x02:0x03:0x04:0x05:0x01'
                    className = 'Applet'
                }

                applet {
                    aid = '0x01:0x02:0x03:0x04:0x05:0x02'
                    className = 'Applet2'
                }

                version = '1.0'
            }
        }

        configClosure.delegate = ext
        configClosure.call()
    }

    @Test void containsApplets() {
        assertThat(ext.cap.applets.size(), equalTo(2))
        assertThat(ext.cap.applets[0].className, equalTo('Applet'))
        assertThat(ext.cap.applets[1].className, equalTo('Applet2'))
        assertThat(ext.cap.applets[0].aid.string, equalTo('0x01:0x02:0x03:0x04:0x05:0x01'))
    }

    @Test(expected = InvalidUserDataException)
    void rejectsInvalidAids() {
        ext.cap.aid= 'sdasdasdasd'
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void rejectsToShortAids() {
        ext.cap.aid = '0x01:0x02:0x03:0x04'
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void rejectsInvalidSourcePackage() {
        ext.cap.packageName = 'com.1domain.blubb'
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void rejectsInvalidVersion() {
        ext.cap.version = '1.0alpha'
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void rejectsInvalidAppletAid() {
        ext.cap.applet {
            aid = '0x01:0x02'
            className = 'Applet'
        }
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void rejectsInvalidAppletName() {
        ext.cap.applet {
            aid = '0x01:0x02:0x03:0x04:0x05'
            className = 'package.Applet'
        }
        ext.validate()
    }

    @Test(expected = InvalidUserDataException)
    void allowOnlyOneCapFile() {
        ext.cap {
            aid = '0x01:0x02:0x03:0x04:0x05'
            packageName = 'org.example.javacard.test'
            version = '1.0'
        }
    }


    @Test void aidReturnsHexString() {
        def hexStr = new JavacardExtension.Aid('0x01:0x23:0x3:0x0').hexString
        assertThat(hexStr, equalTo('01230300'))
    }

    @Test void aidIsPrintables() {
        def str = new JavacardExtension.Aid('0x01:0x23:0x3:0x0').toString()
        assertThat(str, equalTo('Aid(0x01:0x23:0x3:0x0)'))
    }
}
