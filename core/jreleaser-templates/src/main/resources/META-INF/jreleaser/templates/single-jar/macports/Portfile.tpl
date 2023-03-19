# -*- coding: utf-8; mode: tcl; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- vim:fenc=utf-8:ft=tcl:et:sw=4:ts=4:sts=4
# {{jreleaserCreationStamp}}

PortSystem       1.0
PortGroup        java 1.0

name             {{macportsPackageName}}
version          {{projectVersion}}
revision         {{macportsRevision}}

categories       {{macportsCategories}}
license          {{projectLicense}}
maintainers      {{macportsMaintainers}}
platforms        darwin
supported_archs  noarch

description      {{projectDescription}}
long_description {{projectLongDescription}}

homepage         {{projectLinkHomepage}}

master_sites     {{macportsDistributionUrl}}
distname         {{macportsDistname}}.jar
use_zip          no

checksums        rmd160 {{distributionChecksumRmd160}} \
                 sha256 {{distributionChecksumSha256}} \
                 size   {{distributionSize}}

java.version     {{macportsJavaVersion}}

use_configure    no

build {}

destroot {
    set target ${destroot}${prefix}/share/${name}

    # Create target directories
    xinstall -m 755 -d ${target}
    xinstall -m 755 -d ${target}/bin
    xinstall -m 755 -d ${target}/lib

    # Copy over the needed elements of our directory tree
    copy {*}[glob -dir ${worksrcpath} *] ${target}/lib

    cat > ${target}/bin/{{distributionExecutableUnix}} << "EOF"
#!/bin/sh

{{#distributionJavaMainModule}}
$JAVA_HOME/bin/java $JAVA_OPTS \
    -p ${target}/lib/{{distributionArtifactFile}} \
    -m {{distributionJavaMainModule}}/{{distributionJavaMainClass}} \
    "$@"
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
$JAVA_HOME/bin/java $JAVA_OPTS \
    -jar ${target}/lib/{{distributionArtifactFile}} \
    "$@"
{{/distributionJavaMainModule}}
EOF

    ln -s ../share/${name}/bin/{{distributionExecutableUnix}} ${destroot}${prefix}/bin/{{distributionExecutableName}}
}

livecheck.type   none
