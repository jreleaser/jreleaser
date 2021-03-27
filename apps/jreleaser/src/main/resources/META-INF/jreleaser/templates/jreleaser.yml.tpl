project:
  name: app
  version: 1.0.0-SNAPSHOT
  description: Awesome App
  longDescription: Awesome App
  website: https://acme.com/app
  authors:
    - Duke
  license: Apache-2
  java:
    groupId: com.acme
    version: 8

release:
  github:
    owner: duke

distributions:
  app:
    artifacts:
      - path: path/to/{{distributionName}}-{{projectVersion}}.zip