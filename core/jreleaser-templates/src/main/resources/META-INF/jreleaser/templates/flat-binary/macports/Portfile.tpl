# -*- coding: utf-8; mode: tcl; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- vim:fenc=utf-8:ft=tcl:et:sw=4:ts=4:sts=4
# {{jreleaserCreationStamp}}

PortSystem       1.0

name             {{macportsPackageName}}
version          {{projectVersion}}
revision         {{macportsRevision}}

categories       {{macportsCategories}}
license          {{projectLicense}}
maintainers      {{macportsMaintainers}}
platforms        darwin
supported_archs  x86_64

description      {{projectDescription}}
long_description {{projectLongDescription}}

homepage         {{projectLinkHomepage}}

master_sites     {{macportsDistributionUrl}}
distname         {{macportsDistname}}
use_zip          no

checksums        rmd160 {{distributionChecksumRmd160}} \
                 sha256 {{distributionChecksumSha256}} \
                 size   {{distributionSize}}

use_configure    no

build {}

destroot {
    set target ${destroot}${prefix}/share/${name}

    # Create the target directory
    xinstall -m 755 -d ${target}

    # Copy over the needed elements of our directory tree
    copy {*}[glob -dir ${worksrcpath} *] ${target}

    ln -s ../share/${name}/{{distributionExecutableName}} ${destroot}${prefix}/{{distributionExecutableName}}
}

livecheck.type   none
