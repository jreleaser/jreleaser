#!/usr/bin/env bash

# Adapted from https://gist.github.com/neilcsmith-net/69bcb23bcc6698815438dc4e3df6caa3
# Original script: (c) 2020 Neil C Smith - neil@codelerity.com

SYSTEM_ARCH="x86_64"
DISTRIBUTION_NAME="{{distributionName}}"
DISTRIBUTION_FILE="{{distributionArtifactFile}}"
DISTRIBUTION_EXEC="{{distributionExecutableName}}"
DISTRIBUTION_ID="{{appImageComponentId}}"
DISTRIBUTION_URL="{{appImageDistributionUrl}}"
APPIMAGETOOL_FILE="appimagetool-${SYSTEM_ARCH}.AppImage"
APPIMAGETOOL_URL="https://github.com/AppImage/AppImageKit/releases/download/continuous/${APPIMAGETOOL_FILE}"

# create build directory for needed resources
mkdir -p build-${SYSTEM_ARCH}/
cd build-${SYSTEM_ARCH}/

# download AppImage tool
wget -c $APPIMAGETOOL_URL
if [ ! -f "$APPIMAGETOOL_FILE" ]; then
    echo "ERROR: ${APPIMAGETOOL_FILE} does not exist."
    exit 1
fi
chmod +x "$APPIMAGETOOL_FILE"

# download and extract release
wget -c -O DISTRIBUTION_FILE $DISTRIBUTION_URL
if [ ! -f "DISTRIBUTION_FILE" ]; then
    echo "ERROR: ${DISTRIBUTION_FILE} does not exist."
    exit 1
fi

# create AppDir structure
mkdir -p AppDir/
mkdir -p AppDir/usr/share/${DISTRIBUTION_NAME}/bin/
mv "${DISTRIBUTION_FILE}" AppDir/usr/share/${DISTRIBUTION_NAME}/bin/${DISTRIBUTION_EXEC}
mkdir -p AppDir/usr/bin/
ln -s AppDir/usr/share/${DISTRIBUTION_NAME}/bin/${DISTRIBUTION_EXEC} AppDir/usr/bin/${DISTRIBUTION_EXEC}
mkdir -p AppDir/usr/share/applications/
mkdir -p AppDir/usr/share/metainfo
cp ../${DISTRIBUTION_ID}.appdata.xml AppDir/usr/share/metainfo
cp ../${DISTRIBUTION_ID}.appdata.xml AppDir/usr/share/metainfo/${DISTRIBUTION_NAME}.appdata.xml
cp ../${DISTRIBUTION_NAME}.desktop AppDir/usr/share/applications
ln -s usr/share/applications/${DISTRIBUTION_NAME}.desktop AppDir/${DISTRIBUTION_NAME}.desktop
{{#appImageIcons}}
mkdir -p AppDir/usr/share/icons/hicolor/{{width}}x{{height}}/
cp ../icons/{{width}}x{{height}}/${DISTRIBUTION_NAME}.png AppDir/usr/share/icons/hicolor/{{width}}x{{height}}/${DISTRIBUTION_NAME}.png
{{#primary}}ln -s usr/share/icons/hicolor/{{width}}x{{height}}/${DISTRIBUTION_NAME}.png AppDir/${DISTRIBUTION_NAME}.png{{/primary}}
{{#primary}}ln -s usr/share/icons/hicolor/{{width}}x{{height}}/${DISTRIBUTION_NAME}.png AppDir/.DirIcon{{/primary}}
{{/appImageIcons}}

# create AppRun script
cat > AppDir/AppRun << "EOF"
#!/usr/bin/env bash

HERE="$(dirname "$(readlink -f "${0}")")"

exec "$HERE/usr/share/{{distributionName}}/bin/{{distributionExecutableName}}" "$@"
EOF

chmod +x AppDir/AppRun

# build AppImage
ARCH=${SYSTEM_ARCH} "./${APPIMAGETOOL_FILE}" -v AppDir/ "../${DISTRIBUTION_NAME}-${DISTRIBUTION_VERSION}-${SYSTEM_ARCH}.AppImage"
