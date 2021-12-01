Name:      {{distributionName}}
Version:   {{projectVersion}}
Release:   {{specRelease}}
Summary:   {{projectDescription}}

License:   {{projectLicense}}
URL:       {{projectWebsite}}
Source0:   {{distributionUrl}}

BuildArch: noarch
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
install -p -m 755 bin/{{distributionExecutable}} %{_appdir}/bin/{{distributionExecutable}}
{{#specFiles}}
install -p -m 644 {{.}} %{_appdir}/{{.}}
{{/specFiles}}

%files
%{_bindir}/%{name}
%{_datadir}/%{name}/bin/{{distributionExecutable}}
{{#specFiles}}
%{_datadir}/%{name}/{{.}}
{{/specFiles}}