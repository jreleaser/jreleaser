# Homebrew

## Formulae
Invoke either of the following commands if the formula is hosted at GitHub

```sh
$ brew install {{brewTapRepoOwner}}/{{brewTapName}}/<formula>
```

Or

```sh
$ brew tap {{brewTapRepoOwner}}/{{brewTapName}}
$ brew install <formula>
```

Invoke the following command if the formula is *not* hosted at GitHub

```sh
brew tap {{brewTapRepoOwner}}/{{brewTapRepoName}} {{tapRepoCloneUrl}}
brew install <formula>
```

## Casks
Invoke either of the following commands if the cask is hosted at GitHub

```sh
$ brew install --cask {{brewTapRepoOwner}}/{{brewTapName}}/<cask>
```

Or

```sh
$ brew tap {{brewTapRepoOwner}}/{{brewTapName}}
$ brew install --cask <cask>
```

Invoke the following command if the cask is *not* hosted at GitHub

```sh
brew tap {{brewTapRepoOwner}}/{{brewTapRepoName}} {{tapRepoCloneUrl}}
brew install --cask <formula>
```

If you get a dialog stating the cask is broken try installing with `--no-quarantine`.

## Documentation
`brew help`, `man brew` or check [Homebrew's documentation](https://docs.brew.sh).
