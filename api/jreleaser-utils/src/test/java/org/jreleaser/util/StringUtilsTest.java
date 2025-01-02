/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {
    @Test
    void testCapitalize() {
        assertEquals("", StringUtils.capitalize(""));
        assertEquals(" ", StringUtils.capitalize(" "));
        assertNull(StringUtils.capitalize(null));
        assertEquals("A", StringUtils.capitalize("a"));
        assertEquals("Jreleaser", StringUtils.capitalize("jreleaser"));
    }

    @Test
    void testUncapitalize() {
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals(" ", StringUtils.uncapitalize(" "));
        assertNull(StringUtils.uncapitalize(null));
        assertEquals("a", StringUtils.uncapitalize("A"));
        assertEquals("jreleaser", StringUtils.uncapitalize("Jreleaser"));
    }

    @Test
    void testGetSetterName() {
        assertEquals("setA", StringUtils.getSetterName("a"));
        assertEquals("setJreleaser", StringUtils.getSetterName("jreleaser"));
    }

    @Test
    void testGetGetterName() {
        assertEquals("getA", StringUtils.getGetterName("a"));
        assertEquals("getJreleaser", StringUtils.getGetterName("jreleaser"));
    }

    @Test
    void testGetClassName() {
        assertEquals("PersonController", StringUtils.getClassName("person", "Controller"));
        assertEquals("Person", StringUtils.getClassName("person", ""));
    }

    @Test
    void testGetPropertyName() {
        assertEquals("foo", StringUtils.getPropertyName(Foo.class));
    }

    @Test
    void testGetPropertyNameRepresentation() {
        assertEquals("foo", StringUtils.getPropertyName("foo"));
        assertEquals("foo", StringUtils.getPropertyName("Foo"));
        assertEquals("foo", StringUtils.getPropertyName("jreleaser.util.Foo"));
        assertEquals("fooBar", StringUtils.getPropertyName("Foo Bar"));
    }

    @Test
    void testGetShortName() {
        assertEquals("Foo", StringUtils.getShortName("jreleaser.util.Foo"));
        assertEquals("Foo", StringUtils.getShortName(Foo.class));
    }

    @Test
    void testGetClassNameRepresentation() {
        assertEquals("MyClass", StringUtils.getClassNameRepresentation("my-class"));
        assertEquals("MyClass", StringUtils.getClassNameRepresentation("MyClass"));
        assertEquals("F", StringUtils.getClassNameRepresentation(".f"));
        assertEquals("AB", StringUtils.getClassNameRepresentation(".a.b"));
        assertEquals("AlphaBakerCharlie", StringUtils.getClassNameRepresentation(".alpha.baker.charlie"));
    }

    @Test
    void testGetNaturalName() {
        assertEquals("First Name", StringUtils.getNaturalName("firstName"));
        assertEquals("URL", StringUtils.getNaturalName("URL"));
        assertEquals("Local URL", StringUtils.getNaturalName("localURL"));
        assertEquals("URL local", StringUtils.getNaturalName("URLlocal"));
        assertEquals("My Domain Class", StringUtils.getNaturalName("MyDomainClass"));
        assertEquals("My Domain Class", StringUtils.getNaturalName("com.myco.myapp.MyDomainClass"));
    }

    @Test
    void testGetLogicalName() {
        assertEquals("Test", StringUtils.getLogicalName("TestController", "Controller"));
        assertEquals("Test", StringUtils.getLogicalName("org.music.TestController", "Controller"));
    }

    @Test
    void testGetLogicalPropertyName() {
        assertEquals("myFunky", StringUtils.getLogicalPropertyName("MyFunkyController", "Controller"));
        assertEquals("HTML", StringUtils.getLogicalPropertyName("HTMLCodec", "Codec"));
        assertEquals("payRoll", StringUtils.getLogicalPropertyName("org.something.PayRollController", "Controller"));
    }

    @Test
    void testGetLogicalPropertyNameForArtefactWithSingleCharacterName() {
        assertEquals("a", StringUtils.getLogicalPropertyName("AController", "Controller"));
        assertEquals("b", StringUtils.getLogicalPropertyName("BService", "Service"));
    }

    @Test
    void testGetLogicalPropertyNameForArtefactWithAllUpperCaseName() {
        assertEquals("ABC", StringUtils.getLogicalPropertyName("ABCController", "Controller"));
        assertEquals("BCD", StringUtils.getLogicalPropertyName("BCDService", "Service"));
    }

    @Test
    void testIsBlank() {
        assertTrue(StringUtils.isBlank(null), "'null' value should count as blank.");
        assertTrue(StringUtils.isBlank(""), "Empty string should count as blank.");
        assertTrue(StringUtils.isBlank("  "), "Spaces should count as blank.");
        assertTrue(StringUtils.isBlank("\t"), "A tab should count as blank.");
        assertFalse(StringUtils.isBlank("\t  h"), "String with whitespace and non-whitespace should not count as blank.");
        assertFalse(StringUtils.isBlank("test"), "String should not count as blank.");
    }

    @Test
    void testIsNotBlank() {
        assertFalse(StringUtils.isNotBlank(null), "'null' value should count as blank.");
        assertFalse(StringUtils.isNotBlank(""), "Empty string should count as blank.");
        assertFalse(StringUtils.isNotBlank("  "), "Spaces should count as blank.");
        assertFalse(StringUtils.isNotBlank("\t"), "A tab should count as blank.");
        assertTrue(StringUtils.isNotBlank("\t  h"), "String with whitespace and non-whitespace should not count as blank.");
        assertTrue(StringUtils.isNotBlank("test"), "String should not count as blank.");
    }

    @Test
    void testQuote() {
        assertEquals(" ", StringUtils.quote(" "));
        assertEquals("\" a\"", StringUtils.quote(" a"));
        assertEquals("\" a \"", StringUtils.quote(" a "));
        assertEquals("\"a \"", StringUtils.quote("a "));
    }

    @Test
    void testUnquote() {
        assertEquals("", StringUtils.unquote(""));
        assertEquals(" ", StringUtils.unquote(" "));
        assertEquals("", StringUtils.unquote("\"\""));
        assertEquals(" ", StringUtils.unquote("\" \""));
        assertEquals("foo", StringUtils.unquote("\"foo\""));
        assertEquals("", StringUtils.unquote("''"));
        assertEquals(" ", StringUtils.unquote("' '"));
        assertEquals("foo", StringUtils.unquote("'foo'"));
        assertEquals("\"foo", StringUtils.unquote("\"foo"));
        assertEquals("foo\"", StringUtils.unquote("foo\""));
        assertEquals("'foo", StringUtils.unquote("'foo"));
        assertEquals("foo'", StringUtils.unquote("foo'"));
    }

    @Test
    void testGetHyphenatedName() {
        assertEquals("string-utils", StringUtils.getHyphenatedName("string-utils"));
        assertEquals("string-utils", StringUtils.getHyphenatedName(StringUtils.class));
        assertEquals("string-utils", StringUtils.getHyphenatedName(StringUtils.class.getName()));
    }

    @Test
    void testGetClassNameForLowerCaseHyphenSeparatedName() {
        assertEquals("StringUtils", StringUtils.getClassNameForLowerCaseHyphenSeparatedName("string-utils"));
    }

    @Test
    void testPropertyConversion() {
        assertFalse(StringUtils.isTrue(null));
        assertFalse(StringUtils.isTrue(null, false));
        assertTrue(StringUtils.isTrue(null, true));

        assertTrue(StringUtils.isTrue("true"));
        assertTrue(StringUtils.isTrue("tRuE"));

        assertFalse(StringUtils.isTrue(""));
    }
}

