# [JRELEASER_VERSION]
Name:      {{specPackageName}}
Version:   {{projectVersion}}
Release:   {{specRelease}}
Summary:   {{projectDescription}}

License:   {{projectLicense}}
URL:       {{projectWebsite}}
Source0:   {{distributionUrl}}

BuildArch: x86_64
{{#specRequires}}
Requires:  {{.}}
{{/specRequires}}

%description
{{projectLongDescription}}

%prep
%setup -q

%install
mkdir -p %{buildroot}%{_bindir}
%define _appdir %{buildroot}%{_datadir}/%{name}
mkdir -p %{_appdir}/bin

cat > %{buildroot}%{_bindir}/{{distributionExecutable}} <<-EOF
#!/bin/sh
%{_datadir}/%{name}/bin/{{distributionExecutable}} "$@"
EOF
chmod 0755 %{buildroot}%{_bindir}/{{distributionExecutable}}

{{#specDirectories}}
mkdir -p %{_appdir}/{{.}}
{{/specDirectories}}
{{#specBinaries}}
install -p -m 755 bin/{{.}} %{_appdir}/bin/{{.}}
{{/specBinaries}}
{{#specFiles}}
install -p -m 644 {{.}} %{_appdir}/{{.}}
{{/specFiles}}

%files
%{_bindir}/%{name}
{{#specBinaries}}
%{_datadir}/%{name}/bin/{{.}}
{{/specBinaries}}
{{#specFiles}}
%{_datadir}/%{name}/{{.}}
{{/specFiles}}