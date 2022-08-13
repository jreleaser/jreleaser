project:
  name: {{projectName}}
  description: {{projectDescription}}
  links:
    homepage: {{projectLinkHomepage}}
  authors:
    {{#projectAuthors}}
    - {{ . }}
    {{/projectAuthors}}
  license: {{projectLicense}}
  inceptionYear: {{projectInceptionYear}}

release:
  github:
    overwrite: true
    sign: true
    skipTag: true
    changelog:
      formatted: ALWAYS
      preset: conventional-commits{{=<% %>=}}
      format: '- {{commitShortHash}} {{commitTitle}}'
      contributors:
        format: '- {{contributorName}}{{#contributorUsernameAsLink}} ({{.}}){{/contributorUsernameAsLink}}'<%={{ }}=%>
      hide:
        contributors:
          - 'GitHub'

signing:
  active: always
  armored: true

announce:
  twitter:
    active: release
    status: '🚀 {{distributionExecutableName}}-{{=<% %>=}}{{projectVersion}}<%={{ }}=%>-x86_64.AppImage has been released! {{=<% %>=}}{{releaseNotesUrl}}<%={{ }}=%>'

files:
  artifacts:
    - path: '{{distributionExecutableName}}-{{=<% %>=}}{{projectVersion}}<%={{ }}=%>-x86_64.AppImage'
