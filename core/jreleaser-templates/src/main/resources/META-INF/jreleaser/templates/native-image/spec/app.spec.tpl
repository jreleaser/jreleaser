# {{jreleaserCreationStamp}}
Name:      {{specPackageName}}
Version:   {{projectVersion}}
Release:   {{specRelease}}
Summary:   {{projectDescription}}

License:   {{projectLicense}}
URL:       {{projectLinkHomepage}}
Source0:   {{distributionUrl}}

BuildArch: x86_64
{{#specRequires}}
Requires:  {{.}}
{{/specRequires}}

%description
{{projectLongDescription}}

%prep
%setup -q -n {{distributionArtifactRootEntryName}}

%install
mkdir -p %{buildroot}%{_bindir}
%define _appdir %{buildroot}%{_datadir}/%{name}
mkdir -p %{_appdir}/bin

cat > %{buildroot}%{_bindir}/{{distributionExecutableName}} <<-EOF
#!/bin/sh
%{_datadir}/%{name}/bin/{{distributionExecutableUnix}} "$@"
EOF
chmod 0755 %{buildroot}%{_bindir}/{{distributionExecutableName}}

{{#specDirectories}}
mkdir -p %{_appdir}/{{.}}
{{/specDirectories}}
{{#specBinaries}}
install -p -m 755 {{.}} %{_appdir}/{{.}}
{{/specBinaries}}
{{#specFiles}}
install -p -m 644 {{.}} %{_appdir}/{{.}}
{{/specFiles}}

%files
%{_bindir}/%{name}
{{#specBinaries}}
%{_datadir}/%{name}/{{.}}
{{/specBinaries}}
{{#specFiles}}
%{_datadir}/%{name}/{{.}}
{{/specFiles}}