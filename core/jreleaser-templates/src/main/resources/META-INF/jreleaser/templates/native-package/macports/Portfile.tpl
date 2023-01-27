# -*- coding: utf-8; mode: tcl; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- vim:fenc=utf-8:ft=tcl:et:sw=4:ts=4:sts=4
# {{jreleaserCreationStamp}}

PortSystem        1.0

name              {{macportsPackageName}}
version           {{projectVersion}}
revision          {{macportsRevision}}

categories        {{macportsCategories}}
license           {{projectLicense}}
maintainers       {{macportsMaintainers}}
platforms         darwin
supported_archs   noarch

description       {{projectDescription}}
long_description  {{projectLongDescription}}

homepage          {{projectLinkHomepage}}

master_sites      {{macportsDistributionUrl}}
set installer_dmg {{macportsDistname}}
distfiles         ${installer_dmg}
use_dmg           yes

checksums         rmd160 {{distributionChecksumRmd160}} \
                  sha256 {{distributionChecksumSha256}} \
                  size   {{distributionSize}}

use_configure     no
extract.mkdir     yes
extract.only

build {}

post-extract {
    set my_dmg_mount [my_attach_disk_image ${distpath}/${installer_dmg}]
    copy "${my_dmg_mount}/{{macportsAppName}}" "${worksrcpath}/{{macportsAppName}}"
    my_detach_disk_image ${my_dmg_mount}
}

destroot {
    xinstall -d ${destroot}${applications_dir}
    file copy "${worksrcpath}/{{macportsAppName}}" ${destroot}${applications_dir}
    ln -s "${applications_dir}/{{macportsAppName}}/Contents/MacOs/{{distributionExecutableUnix}}" ${destroot}${prefix}/bin/{{distributionExecutableName}}
}

# Mounts a disk image.
proc my_attach_disk_image {disk_image} {
    global workpath
    set tmp_disk_image_dir [mkdtemp "${workpath}/.tmp/disk_image.XXXXXXXX"]
    set tmp_disk_image ${tmp_disk_image_dir}/[file tail ${disk_image}].cdr
    set mountpoint [mkdtemp "${workpath}/.tmp/mountpoint.XXXXXXXX"]
    system "hdiutil convert -quiet -ov -format UDTO -o [shellescape ${tmp_disk_image}] [shellescape ${disk_image}]"
    system "hdiutil attach -quiet [shellescape ${tmp_disk_image}] -mountpoint [shellescape ${mountpoint}] -private -nobrowse -noautoopen -noautofsck -noverify -readonly"
    return ${mountpoint}
}

# Unmounts a disk image.
proc my_detach_disk_image {mountpoint} {
    system "hdiutil detach [shellescape ${mountpoint}] -force"
    file delete -force ${mountpoint}
}

livecheck.type    none
