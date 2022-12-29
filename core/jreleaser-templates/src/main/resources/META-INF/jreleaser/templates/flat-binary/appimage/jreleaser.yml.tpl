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
      contributors:
        format: '- {{contributorName}}{{#contributorUsernameAsLink}} ({{.}}){{/contributorUsernameAsLink}}'<%={{ }}=%>
      hide:
        contributors:
          - 'GitHub'

files:
  artifacts:
    - path: '{{distributionName}}-{{=<% %>=}}{{projectVersion}}<%={{ }}=%>-x86_64.AppImage'
