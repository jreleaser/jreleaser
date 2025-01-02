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
package org.jreleaser.model.api.packagers;

import org.jreleaser.model.api.common.Domain;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public interface WingetPackager extends RepositoryPackager {
    String TYPE = "winget";
    String SKIP_WINGET = "skipWinget";

    Package getPackage();

    Publisher getPublisher();

    Installer getInstaller();

    String getDefaultLocale();

    String getAuthor();

    String getMoniker();

    List<String> getTags();

    String getMinimumOsVersion();

    String getProductCode();

    PackagerRepository getRepository();

    interface Package extends Domain {
        String getIdentifier();

        String getVersion();

        String getName();

        String getUrl();
    }

    interface Publisher extends Domain {
        String getName();

        String getUrl();

        String getSupportUrl();
    }

    interface Installer extends Domain {
        Type getType();

        Scope getScope();

        Set<Mode> getModes();

        UpgradeBehavior getUpgradeBehavior();

        String getCommand();

        Dependencies getDependencies();

        enum Type {
            MSIX,
            MSI,
            APPX,
            EXE,
            ZIP,
            INNO,
            NULLSOFT,
            WIX,
            BURN,
            PWA;

            public String formatted() {
                return name().toLowerCase(Locale.ENGLISH);
            }

            public static Type of(String str) {
                if (isBlank(str)) return null;
                return Type.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
            }
        }

        enum Scope {
            USER,
            MACHINE;

            public String formatted() {
                return name().toLowerCase(Locale.ENGLISH);
            }

            public static Scope of(String str) {
                if (isBlank(str)) return null;
                return Scope.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
            }
        }

        enum Mode {
            INTERACTIVE("interactive"),
            SILENT("silent"),
            SILENT_WITH_PROGRESS("silentWithProgress");

            private final String alias;

            Mode(String alias) {
                this.alias = alias;
            }

            public String formatted() {
                return alias;
            }

            public static Mode of(String str) {
                if (isBlank(str)) return null;

                String value = str.replace(" ", "")
                    .replace("-", "")
                    .replace("_", "")
                    .toUpperCase(Locale.ENGLISH).trim();

                // try alias
                for (Mode type : Mode.values()) {
                    if (type.alias.toUpperCase(Locale.ENGLISH).equals(value)) {
                        return type;
                    }
                }

                return Mode.valueOf(value);
            }
        }

        enum UpgradeBehavior {
            INSTALL("install"),
            UNINSTALL_PREVIOUS("uninstallPrevious");

            private final String alias;

            UpgradeBehavior(String alias) {
                this.alias = alias;
            }

            public String formatted() {
                return alias;
            }

            public static UpgradeBehavior of(String str) {
                if (isBlank(str)) return null;

                String value = str.replace(" ", "")
                    .replace("-", "")
                    .replace("_", "")
                    .toUpperCase(Locale.ENGLISH).trim();

                // try alias
                for (UpgradeBehavior type : UpgradeBehavior.values()) {
                    if (type.alias.toUpperCase(Locale.ENGLISH).equals(value)) {
                        return type;
                    }
                }

                return UpgradeBehavior.valueOf(value);
            }
        }
    }

    interface Dependencies extends Domain {
        Set<String> getWindowsFeatures();

        Set<String> getWindowsLibraries();

        Set<String> getExternalDependencies();

        Set<PackageDependency> getPackageDependencies();
    }

    interface PackageDependency extends Domain {
        String getPackageIdentifier();

        String getMinimumVersion();
    }
}
