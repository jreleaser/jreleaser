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

files:
  artifacts:
    - path: '{{distributionExecutableName}}-{{=<% %>=}}{{projectVersion}}<%={{ }}=%>-x86_64.AppImage'
