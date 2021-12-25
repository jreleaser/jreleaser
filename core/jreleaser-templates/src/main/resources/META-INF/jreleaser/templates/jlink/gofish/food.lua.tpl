-- [JRELEASER_VERSION]

local name = "{{distributionExecutable}}"
local version = "{{projectVersion}}"

food = {
    name = name,
    description = "{{projectDescription}}",
    license = "{{projectLicense}}"
    homepage = "{{projectWebsite}}",
    version = version,
    packages = {
    {{#gofishPackages}}
        {
            os = "{{packageOs}}",
            arch = "{{packageArch}}",
            url = "{{packageUrl}}",
            sha256 = "{{packageChecksum}}",
            resources = {
                {
                    path = {{packagePath}},
                    installpath = {{packageInstallPath}},
                    executable = true
                }
            }
        },
    {{/gofishPackages}}
    }
}
