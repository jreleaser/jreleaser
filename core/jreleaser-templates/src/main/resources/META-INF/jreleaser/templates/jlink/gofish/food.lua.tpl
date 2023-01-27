-- {{jreleaserCreationStamp}}

local name = "{{distributionExecutableName}}"
local version = "{{projectVersion}}"

food = {
    name = name,
    description = "{{projectDescription}}",
    license = "{{projectLicense}}",
    homepage = "{{projectLinkHomepage}}",
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
                    installpath = {{packageInstallPath}}{{#packageNotWindows}},{{/packageNotWindows}}
                    {{#packageNotWindows}}executable = true{{/packageNotWindows}}
                }
            }
        },
    {{/gofishPackages}}
    }
}
