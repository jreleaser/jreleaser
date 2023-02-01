JReleaser {{projectVersion}} is now available! This release brings plenty of features and bug fixes

**Environment:** Several properties may not be set using either System Properties or environment variables; review the
_Environment` section of each DSL element in the [Reference]({{projectLinkDocumentation}}/reference). Additionally, local
`.env` files may be used to define environment variables. A new [env]({{projectLinkDocumentation}}/reference/environment.html#_inspection)
command may be used to display System property names and environment variable names in use.

**Packagers:** [Winget]({{projectLinkDocumentation}}/reference/packagers/winget.html), the preferred package manager for
Windows, is now supported for `NATIVE_PACKAGE` distributions.

**Docker:** New capabilities such as reusing an existing buildx builder, allow login into registries outside of the tool,
useful when running in CI.

**Templates:** Arbitrary templates may now be evaluated using the `template eval` [command]({{projectLinkDocumentation}}/tools/jreleaser-cli.html#_template_eval)

**CLI:** Updates to CLI flags following the Command Line Interface Guidelines document https://github.com/jreleaser/jreleaser/issues/1185

[🚀 Changelog](https://github.com/jreleaser/jreleaser/releases/tag/{{tagName}})