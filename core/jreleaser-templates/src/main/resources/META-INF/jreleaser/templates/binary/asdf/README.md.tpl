<div align="center">

# asdf-{{distributionName}}

[{{distributionName}}]({{projectWebsite}}) plugin for the [asdf version manager](https://asdf-vm.com).

</div>

# Contents

- [Dependencies](#dependencies)
- [Install](#install)

# Dependencies

- `bash`, `curl`, `tar`: generic POSIX utilities.
- `SOME_ENV_VAR`: set this environment variable in your shell config to load the correct version of tool x.

# Install

Plugin:

```shell
asdf plugin add {{distributionName}}
# or
asdf plugin add {{distributionName}} {{asdfPluginRepoUrl}}.git
```

{{distributionName}}:

```shell
# Show all installable versions
asdf list-all {{distributionName}}

# Install specific version
asdf install {{distributionName}} latest

# Set a version globally (on your ~/.tool-versions file)
asdf global {{distributionName}} latest

# Now {{distributionName}} commands are available
{{asdfPluginToolCheck}}
```

Check [asdf](https://github.com/asdf-vm/asdf) readme for more instructions on how to install & manage versions.
