/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class StringUtils {
    private static final String PROPERTY_SET_PREFIX = "set";
    private static final String PROPERTY_GET_PREFIX = "get";
    private static final Pattern GETTER_PATTERN_1 = Pattern.compile("^get[A-Z][\\w]*$");
    private static final Pattern GETTER_PATTERN_2 = Pattern.compile("^is[A-Z][\\w]*$");
    private static final Pattern SETTER_PATTERN = Pattern.compile("^set[A-Z][\\w]*$");
    private static final String ERROR_METHOD_NULL = "Argument 'method' must not be null";
    private static final Pattern REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|/]");

    /**
     * Capitalizes a String (makes the first char uppercase) taking care
     * of blank strings and single character strings.
     *
     * @param str The String to be capitalized
     * @return Capitalized version of the target string if it is not blank
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }

        if (str.length() == 1) {
            return str.toUpperCase(Locale.ENGLISH);
        }

        return ((String) (str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1)));
    }

    public static String getFilenameExtension(String path) {
        if (path == null) {
            return null;
        }

        int extIndex = path.lastIndexOf(".");
        if (extIndex == -1) {
            return null;
        }

        int folderIndex = path.lastIndexOf(File.separator);
        if (folderIndex > extIndex) {
            return null;
        }

        return path.substring(extIndex + 1);
    }

    public static String getFilename(String path) {
        if (path == null) {
            return null;
        }

        int extIndex = path.lastIndexOf(".");
        if (extIndex == -1) {
            return null;
        }

        int folderIndex = path.lastIndexOf(File.separator);
        if (folderIndex > extIndex) {
            return null;
        }

        folderIndex = folderIndex < 0 ? 0 : folderIndex;

        return path.substring(folderIndex, extIndex);
    }

    public static String getFilename(String path, Collection<String> extensions) {
        for (String extension : extensions) {
            extension = extension.startsWith(".") ? extension : "." + extension;
            if (path.endsWith(extension)) {
                return path.substring(0, path.length() - extension.length());
            }
        }

        return path;
    }

    /**
     * Retrieves the name of a setter for the specified property name
     *
     * @param propertyName The property name
     * @return The setter equivalent
     */
    public static String getSetterName(String propertyName) {
        return ((String) (PROPERTY_SET_PREFIX + capitalize(propertyName)));
    }

    /**
     * Calculate the name for a getter method to retrieve the specified property
     *
     * @param propertyName The property name
     * @return The name for the getter method for this property, if it were to exist, i.e. getConstraints
     */
    public static String getGetterName(String propertyName) {
        return ((String) (PROPERTY_GET_PREFIX + capitalize(propertyName)));
    }

    /**
     * Returns the class name for the given logical name and trailing name. For example "person" and "Controller" would evaluate to "PersonController"
     *
     * @param logicalName  The logical name
     * @param trailingName The trailing name
     * @return The class name
     */
    public static String getClassName(String logicalName, String trailingName) {
        if (isBlank(logicalName)) {
            throw new IllegalArgumentException("Argument [logicalName] must not be null or blank");
        }


        String className = capitalize(logicalName);
        if (trailingName != null) {
            className = ((String) (className + trailingName));
        }

        return className;
    }

    /**
     * Returns the class name representation of the given name
     *
     * @param name The name to convert
     * @return The property name representation
     */
    public static String getClassNameRepresentation(String name) {
        StringBuilder buf = new StringBuilder();
        if (name != null && name.length() > 0) {
            String[] tokens = name.split("[^\\w\\d]");
            for (String token1 : tokens) {
                String token = token1.trim();
                buf.append(capitalize(token));
            }
        }

        return buf.toString();
    }

    /**
     * Converts foo-bar into FooBar. Empty and null strings are returned
     * as-is.
     *
     * @param name The lower case hyphen separated name
     * @return The class name equivalent.
     */
    public static String getClassNameForLowerCaseHyphenSeparatedName(String name) {
        // Handle null and empty strings.
        if (isBlank(name)) {
            return name;
        }

        if (name.contains("-")) {
            StringBuilder buf = new StringBuilder();
            String[] tokens = name.split("-");
            for (String token : tokens) {
                if (token == null || token.length() == 0) {
                    continue;
                }

                buf.append(capitalize(token));
            }

            return buf.toString();
        }

        return capitalize(name);
    }

    /**
     * Retrieves the logical class name of a Griffon artifact given the Griffon class
     * and a specified trailing name
     *
     * @param clazz        The class
     * @param trailingName The trailing name such as "Controller" or "TagLib"
     * @return The logical class name
     */
    public static String getLogicalName(Class<?> clazz, String trailingName) {
        return getLogicalName(clazz.getName(), trailingName);
    }

    /**
     * Retrieves the logical name of the class without the trailing name
     *
     * @param name         The name of the class
     * @param trailingName The trailing name
     * @return The logical name
     */
    public static String getLogicalName(String name, String trailingName) {
        if (isNotBlank(name) && isNotBlank(trailingName)) {
            String shortName = getShortName(name);
            if (shortName.endsWith(trailingName)) {
                return shortName.substring(0, shortName.length() - trailingName.length());
            }
        }

        return name;
    }

    public static String getLogicalPropertyName(String className, String trailingName) {
        if (isNotBlank(className) && isNotBlank(trailingName) && className.length() == trailingName.length() + 1 && className.endsWith(trailingName)) {
            return className.substring(0, 1).toLowerCase(Locale.ENGLISH);
        }

        return getLogicalName(getPropertyName(className), trailingName);
    }

    /**
     * Shorter version of getPropertyNameRepresentation
     *
     * @param name The name to convert
     * @return The property name version
     */
    public static String getPropertyName(String name) {
        return getPropertyNameRepresentation(name);
    }

    /**
     * Shorter version of getPropertyNameRepresentation
     *
     * @param clazz The clazz to convert
     * @return The property name version
     */
    public static String getPropertyName(Class<?> clazz) {
        return getPropertyNameRepresentation(clazz);
    }

    /**
     * Returns the property name representation of the given {@code Method}
     *
     * @param method The method to inspect
     * @return The property name representation
     * @since 3.0.0
     */
    public static String getPropertyName(Method method) {
        Objects.requireNonNull(method, ERROR_METHOD_NULL);
        String name = method.getName();
        if (GETTER_PATTERN_1.matcher(name).matches() || SETTER_PATTERN.matcher(name).matches()) {
            return uncapitalize(name.substring(3));
        } else if (GETTER_PATTERN_2.matcher(name).matches()) {
            return uncapitalize(name.substring(2));
        }

        return name;
    }

    /**
     * Returns the property name equivalent for the specified class
     *
     * @param targetClass The class to get the property name for
     * @return A property name representation of the class name (eg. MyClass becomes myClass)
     */
    public static String getPropertyNameRepresentation(Class<?> targetClass) {
        String shortName = getShortName(targetClass);
        return getPropertyNameRepresentation(shortName);
    }

    /**
     * Returns the property name representation of the given name
     *
     * @param name The name to convert
     * @return The property name representation
     */
    public static String getPropertyNameRepresentation(String name) {
        if (isBlank(name)) {
            return name;
        }

        // Strip any package from the name.
        int pos = name.lastIndexOf(".");
        if (pos != -1) {
            name = name.substring(pos + 1);
        }

        // Check whether the name begins with two upper case letters.
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }

        String propertyName = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
        if (propertyName.contains(" ")) {
            propertyName = propertyName.replaceAll("\\s", "");
        }

        return propertyName;
    }

    /**
     * Converts foo-bar into fooBar
     *
     * @param name The lower case hyphen separated name
     * @return The property name equivalent
     */
    public static String getPropertyNameForLowerCaseHyphenSeparatedName(String name) {
        return getPropertyName(getClassNameForLowerCaseHyphenSeparatedName(name));
    }

    /**
     * Returns the class name without the package prefix
     *
     * @param targetClass The class to get a short name for
     * @return The short name of the class
     */
    public static String getShortName(Class<?> targetClass) {
        String className = targetClass.getName();
        return getShortName(className);
    }

    /**
     * Returns the class name without the package prefix
     *
     * @param className The class name to get a short name for
     * @return The short name of the class
     */
    public static String getShortName(String className) {
        if (isBlank(className)) {
            return className;
        }

        int i = className.lastIndexOf(".");
        if (i > -1) {
            className = className.substring(i + 1, className.length());
        }

        return className;
    }

    /**
     * Converts a property name into its natural language equivalent eg ('firstName' becomes 'First Name')
     *
     * @param name The property name to convert
     * @return The converted property name
     */
    public static String getNaturalName(String name) {
        name = getShortName(name);
        if (isBlank(name)) {
            return name;
        }

        List<String> words = new ArrayList<String>();
        int i = 0;
        char[] chars = name.toCharArray();
        for (char c : chars) {
            String w;
            if (i >= words.size()) {
                w = "";
                words.add(i, w);
            } else {
                w = words.get(i);
            }

            if (Character.isLowerCase(c) || Character.isDigit(c)) {
                if (Character.isLowerCase(c) && w.length() == 0) {
                    c = Character.toUpperCase(c);
                } else if (w.length() > 1 && Character.isUpperCase(w.charAt(w.length() - 1))) {
                    w = "";
                    words.add(++i, w);
                }

                words.set(i, w + c);
            } else if (Character.isUpperCase(c)) {
                if ((i == 0 && w.length() == 0) || Character.isUpperCase(w.charAt(w.length() - 1))) {
                    words.set(i, w + c);
                } else {
                    words.add(++i, String.valueOf(c));
                }
            }
        }

        StringBuilder buf = new StringBuilder();
        for (Iterator<String> j = words.iterator(); j.hasNext(); ) {
            String word = j.next();
            buf.append(word);
            if (j.hasNext()) {
                buf.append(" ");
            }
        }

        return buf.toString();
    }

    /**
     * <p>Determines whether a given string is <code>null</code>, empty,
     * or only contains whitespace. If it contains anything other than
     * whitespace then the string is not considered to be blank and the
     * method returns <code>false</code>.</p>
     *
     * @param str The string to test.
     * @return <code>   true</code> if the string is <code>null</code>, or
     * blank.
     */
    public static boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>Determines whether a given string is not <code>null</code>, empty,
     * or only contains whitespace. If it contains anything other than
     * whitespace then the string is not considered to be blank and the
     * method returns <code>true</code>.</p>
     *
     * @param str The string to test.
     * @return <code>   true</code> if the string is not <code>null</code>, nor
     * blank.
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Checks that the specified String is not {@code blank}. This
     * method is designed primarily for doing parameter validation in methods
     * and constructors, as demonstrated below:
     * <blockquote><pre>
     *  Foo(String str) {*     this.str = GriffonNameUtils.requireNonBlank(str)
     * }* </pre></blockquote>
     *
     * @param str the String to check for blank
     * @return {@code str} if not {@code blank}
     * @throws IllegalArgumentException if {@code str} is {@code blank}
     */
    public static String requireNonBlank(String str) {
        if (isBlank(str)) {
            throw new IllegalArgumentException();
        }

        return str;
    }

    /**
     * Checks that the specified String is not {@code blank} and
     * throws a customized {@link IllegalArgumentException} if it is. This method
     * is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters, as demonstrated below:
     * <blockquote><pre>
     *  Foo(String str) {*     this.str = GriffonNameUtils.requireNonBlank(str, "str must not be null")
     * }* </pre></blockquote>
     *
     * @param str     the String to check for blank
     * @param message detail message to be used in the event that a {@code
     *                IllegalArgumentException} is thrown
     * @return {@code str} if not {@code blank}
     * @throws IllegalArgumentException if {@code str} is {@code blank}
     */
    public static String requireNonBlank(String str, String message) {
        if (isBlank(str)) {
            throw new IllegalArgumentException(message);
        }

        return str;
    }

    /**
     * Retrieves the hyphenated name representation of the supplied class. For example
     * MyFunkyGriffonThingy would be my-funky-griffon-thingy.
     *
     * @param clazz The class to convert
     * @return The hyphenated name representation
     */
    public static String getHyphenatedName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        return getHyphenatedName(clazz.getName());
    }

    /**
     * Retrieves the hyphenated name representation of the given class name.
     * For example MyFunkyGriffonThingy would be my-funky-griffon-thingy.
     *
     * @param name The class name to convert.
     * @return The hyphenated name representation.
     */
    public static String getHyphenatedName(String name) {
        if (isBlank(name)) {
            return name;
        }

        if (name.endsWith(".groovy")) {
            name = name.substring(0, name.length() - 7);
        }

        String naturalName = getNaturalName(getShortName(name));
        return naturalName.replaceAll("\\s", "-").toLowerCase(Locale.ENGLISH);
    }

    /**
     * Uncapitalizes a String (makes the first char lowercase) taking care
     * of blank strings and single character strings.
     *
     * @param str The String to be uncapitalized
     * @return Uncapitalized version of the target string if it is not blank
     */
    public static String uncapitalize(String str) {
        if (isBlank(str)) {
            return str;
        }

        if (str.length() == 1) {
            return String.valueOf(Character.toLowerCase(str.charAt(0)));
        }

        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public static Object splitValue(String value) {
        if (isBlank(value) || value.length() == 1) return value;
        char splitter = value.charAt(value.length() - 1);
        return Arrays.asList(value.substring(0, value.length() - 1).split(escape(splitter)));
    }

    private static String escape(char character) {
        switch (character) {
            case '$':
            case '(':
            case ')':
            case '.':
            case '[':
            case '\\':
            case ']':
            case '^':
            case '{':
            case '|':
            case '}':
                return "\\" + character;
            default:
                return "" + character;
        }
    }

    public static String padLeft(String str, int numChars) {
        return padLeft(str, numChars, " ");
    }

    public static String padLeft(String str, int numChars, String padding) {
        return numChars <= str.length() ? str : getPadding(padding.toString(), numChars - str.length()) + str;
    }

    public static String padRight(String str, int numChars) {
        return padRight(str, numChars, " ");
    }

    public static String padRight(String str, int numChars, String padding) {
        return numChars <= str.length() ? str : str + getPadding(padding, numChars - str.length());
    }

    private static String getPadding(String padding, int length) {
        return padding.length() < length ? times(padding, length / padding.length() + 1).substring(0, length) : "" + padding.subSequence(0, length);
    }

    public static String times(String str, int num) {
        if (num < 0) {
            throw new IllegalArgumentException("times() should be called with a number >= 0, got " + num);
        } else if (num == 0) {
            return "";
        } else {
            StringBuilder b = new StringBuilder(str);

            for (int i = 1; i < num; ++i) {
                b.append(str);
            }

            return b.toString();
        }
    }

    public static String stripMargin(String str) {
        return Stream.of(str.split(lineSeparator()))
            .map(String::trim)
            .collect(joining(lineSeparator()));
    }

    public static String escapeRegexChars(String str) {
        boolean start = false;
        boolean end = false;

        String s = str;
        if (s.charAt(0) == '^' && s.length() > 1) {
            start = true;
            s = s.substring(1);
        }
        if (s.charAt(s.length() - 1) == '$' && s.length() > 1) {
            end = true;
            s = s.substring(0, s.length() - 2);
        }

        String replaced = REGEX_CHARS.matcher(s).replaceAll("\\\\$0");
        return (start ? "^" : "") + replaced + (end ? "$" : "");
    }

    public static String toSafeRegexPattern(String str) {
        StringBuilder b = new StringBuilder();
        if (!str.startsWith("^")) {
            b.append(".*");
        }
        b.append(escapeRegexChars(str));
        if (!str.endsWith("$")) {
            b.append(".*");
        }

        return b.toString();
    }

    public static String normalizeRegexPattern(String str) {
        StringBuilder b = new StringBuilder();
        if (!str.startsWith("^")) {
            b.append(".*");
        }
        b.append(str);
        if (!str.endsWith("$")) {
            b.append(".*");
        }

        return b.toString();
    }

    public static Pattern toSafePattern(String str) {
        return Pattern.compile(toSafeRegexPattern(str));
    }

    public static boolean isTrue(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        return "true".equalsIgnoreCase(String.valueOf(o).trim());
    }

    public static boolean isFalse(Object o) {
        return !isTrue(o);
    }

    /**
     * Applies single or double quotes to a string if it contains whitespace characters
     *
     * @param str the String to be surrounded by quotes
     * @return a copy of the original String, surrounded by quotes
     */
    public static String quote(String str) {
        if (isBlank(str)) {
            return str;
        }
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                str = applyQuotes(str);
                break;
            }
        }
        return str;
    }

    /**
     * Removes single or double quotes from a String
     *
     * @param str the String from which quotes will be removed
     * @return the unquoted String
     */
    public static String unquote(String str) {
        if (isBlank(str)) {
            return str;
        }
        if ((str.startsWith("'") && str.endsWith("'")) ||
            (str.startsWith("\"") && str.endsWith("\""))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    private static String applyQuotes(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len * 2);
        String t;
        char[] chars = string.toCharArray();
        char[] buffer = new char[1030];
        int bufferIndex = 0;
        sb.append('"');
        for (i = 0; i < len; i += 1) {
            if (bufferIndex > 1024) {
                sb.append(buffer, 0, bufferIndex);
                bufferIndex = 0;
            }
            b = c;
            c = chars[i];
            switch (c) {
                case '\\':
                case '"':
                    buffer[bufferIndex++] = '\\';
                    buffer[bufferIndex++] = c;
                    break;
                case '/':
                    if (b == '<') {
                        buffer[bufferIndex++] = '\\';
                    }
                    buffer[bufferIndex++] = c;
                    break;
                default:
                    if (c < ' ') {
                        switch (c) {
                            case '\b':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'b';
                                break;
                            case '\t':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 't';
                                break;
                            case '\n':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'n';
                                break;
                            case '\f':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'f';
                                break;
                            case '\r':
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'r';
                                break;
                            default:
                                t = "000" + Integer.toHexString(c);
                                int tLength = t.length();
                                buffer[bufferIndex++] = '\\';
                                buffer[bufferIndex++] = 'u';
                                buffer[bufferIndex++] = t.charAt(tLength - 4);
                                buffer[bufferIndex++] = t.charAt(tLength - 3);
                                buffer[bufferIndex++] = t.charAt(tLength - 2);
                                buffer[bufferIndex++] = t.charAt(tLength - 1);
                        }
                    } else {
                        buffer[bufferIndex++] = c;
                    }
            }
        }
        sb.append(buffer, 0, bufferIndex);
        sb.append('"');
        return sb.toString();
    }
}
