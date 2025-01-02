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
package org.jreleaser.model.api.assemble;

import org.jreleaser.model.api.common.Domain;

import java.util.Locale;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.16.0
 */
public interface DebAssembler extends Assembler {
    String TYPE = "deb";

    String getExecutable();

    String getInstallationPath();

    String getArchitecture();

    Control getControl();

    String getAssemblerRef();

    interface Control extends Domain {
        String getPackageName();

        String getPackageVersion();

        Integer getPackageRevision();

        String getProvides();

        String getMaintainer();

        Section getSection();

        Priority getPriority();

        boolean isEssential();

        String getDescription();

        String getHomepage();

        String getBuiltUsing();

        Set<String> getDepends();

        Set<String> getPreDepends();

        Set<String> getRecommends();

        Set<String> getSuggests();

        Set<String> getEnhances();

        Set<String> getBreaks();

        Set<String> getConflicts();
    }

    enum Section {
        ADMIN("admin"),
        CLI_MONO("cli-mono"),
        COMM("comm"),
        DATABASE("database"),
        DEBUG("debug"),
        DEVEL("devel"),
        DOC("doc"),
        EDITORS("editors"),
        EDUCATION("education"),
        ELECTRONICS("electronics"),
        EMBEDDED("embedded"),
        FONTS("fonts"),
        GAMES("games"),
        GNOME("gnome"),
        GNU_R("gnu-r"),
        GNUSTEP("gnustep"),
        GRAPHICS("graphics"),
        HAMRADIO("hamradio"),
        HASKELL("haskell"),
        HTTPD("httpd"),
        INTERPRETERS("interpreters"),
        INTROSPECTION("introspection"),
        JAVA("java"),
        JAVASCRIPT("javascript"),
        KDE("kde"),
        KERNEL("kernel"),
        LIBDEVEL("libdevel"),
        LIBS("libs"),
        LISP("lisp"),
        LOCALIZATION("localization"),
        MAIL("mail"),
        MATH("math"),
        METAPACKAGES("metapackages"),
        MISC("misc"),
        NET("net"),
        NEWS("news"),
        OCAML("ocaml"),
        OLDLIBS("oldlibs"),
        OTHEROSFS("otherosfs"),
        PERL("perl"),
        PHP("php"),
        PYTHON("python"),
        RUBY("ruby"),
        RUST("rust"),
        SCIENCE("science"),
        SHELLS("shells"),
        SOUND("sound"),
        TASKS("tasks"),
        TEX("tex"),
        TEXT("text"),
        UTILS("utils"),
        VCS("vcs"),
        VIDEO("video"),
        WEB("web"),
        X11("x11"),
        XFCE("xfce"),
        ZOPE("zope");

        private final String value;

        Section(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public static Section of(String str) {
            if (isBlank(str)) return null;
            return valueOf(str.toUpperCase(Locale.ENGLISH).trim()
                .replace("-", "_"));
        }
    }

    enum Priority {
        REQUIRED,
        IMPORTANT,
        STANDARD,
        OPTIONAL;

        public String value() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Priority of(String str) {
            if (isBlank(str)) return null;
            return valueOf(str.toUpperCase(Locale.ENGLISH).trim());
        }
    }
}
