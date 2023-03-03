/*
 * Copyright 2021 Trustin Heuiseung Lee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jreleaser.dependencies.os;

/**
 * Interface exposing system property operations.
 */
public interface SystemPropertyOperationProvider {

  /**
   * Gets the system property indicated by the specified name.
   *
   * @param      name   the name of the system property.
   * @return     the string value of the system property, or {@code null} if there is no
   *             property with that key.
   */
  String getSystemProperty(String name);

  /**
   * Gets the system property indicated by the specified name.
   *
   * @param      name   the name of the system property.
   * @param      def   a default value.
   * @return     the string value of the system property, or the default value if there is
   *             no property with that key.
   */
  String getSystemProperty(String name, String def);

  /**
   * Sets the system property indicated by the specified name.
   *
   * @param      name   the name of the system property.
   * @param      value the value of the system property.
   * @return     the previous value of the system property, or {@code null} if it did not have one.
   */
  String setSystemProperty(String name, String value);
}
