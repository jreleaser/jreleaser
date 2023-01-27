# -*- coding: utf-8; mode: tcl; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- vim:fenc=utf-8:ft=tcl:et:sw=4:ts=4:sts=4
# {{jreleaserCreationStamp}}

PortSystem       1.0
PortGroup        github 1.0
PortGroup        java 1.0

github.setup     {{repoOwner}} {{repoName}} {{projectVersion}} v
revision         {{macportsRevision}}

categories       {{macportsCategories}}
license          Apache-2
maintainers      {{macportsMaintainers}}
platforms        any
supported_archs  noarch

description      {{projectDescription}}
long_description {{projectLongDescription}}

homepage         {{projectLinkHomepage}}
github.tarball_from releases
use_zip          yes

checksums        rmd160 {{distributionChecksumRmd160}} \
                 sha256 {{distributionChecksumSha256}} \
                 size   {{distributionSize}}

java.version     {{macportsJavaVersion}}

use_configure    no

build {}

destroot {
    set target ${destroot}${prefix}/share/${name}

    # Create the target directory
    xinstall -m 755 -d ${target}

    # Copy over the needed elements of our directory tree
    copy {*}[glob -dir ${worksrcpath} *] ${target}

    # Remove extraneous files
    delete {*}[glob -directory ${target}/bin *{{distributionExecutableExtensionWindows}}]

    ln -s ../share/${name}/bin/{{distributionExecutableUnix}} ${destroot}${prefix}/bin/{{distributionExecutableName}}
}
