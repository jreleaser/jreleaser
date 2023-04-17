# {{jreleaserCreationStamp}}
app-id: {{flatpakComponentId}}
runtime: {{flatpakRuntime}}
runtime-version: '{{flatpakRuntimeVersion}}'
sdk: {{flatpakSdk}}
{{#flatpakHasSdkExtensions}}
sdk-extensions:
  {{#flatpakSdkExtensions}}
  - {{.}}
  {{/flatpakSdkExtensions}}
{{/flatpakHasSdkExtensions}}
command: {{distributionExecutableUnix}}
{{#flatpakHasFinishArgs}}
finish-args:
  {{#flatpakFinishArgs}}
  - {{.}}
  {{/flatpakFinishArgs}}
{{/flatpakHasFinishArgs}}
modules:
  - name: {{distributionExecutable}}
    buildsystem: simple
    build-commands:
      {{#flatpakBinaries}}
      - install -Dm755 {{.}} /app/{{.}}
      {{/flatpakBinaries}}
      {{#flatpakFiles}}
      - install -Dm644 {{.}} /app/{{.}}
      {{/flatpakFiles}}
      - install -Dm644 {{flatpakComponentId}}.metainfo.xml -t /app/share/metainfo
      - install -Dm644 {{flatpakComponentId}}.desktop -t /app/share/applications
      {{#flatpakIcons}}
      - install -Dm644 icons/{{width}}x{{height}}/{{distributionExecutable}}.png /app/share/icons/hicolor/{{width}}x{{height}}/apps/{{flatpakComponentId}}.png
      {{/flatpakIcons}}
    sources:
      - type: archive
        url: {{distributionUrl}}
        sha256: {{distributionChecksumSha256}}
      - type: file
        path: {{flatpakComponentId}}.metainfo.xml
      - type: file
        path: {{flatpakComponentId}}.desktop
      {{#flatpakIcons}}
      - type: file
        path: icons/{{width}}x{{height}}/{{distributionExecutable}}.png
        dest: icons/{{width}}x{{height}}
      {{/flatpakIcons}}
