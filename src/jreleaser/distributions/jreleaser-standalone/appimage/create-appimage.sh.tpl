#!/usr/bin/env bash

# Adapted from https://gist.github.com/neilcsmith-net/69bcb23bcc6698815438dc4e3df6caa3
# Original script: (c) 2020 Neil C Smith - neil@codelerity.com

SYSTEM_ARCH="x86_64"
DISTRIBUTION_FILE="{{appImageDistributionArtifactFile}}"
DISTRIBUTION_FILE_NAME="{{appImageDistributionArtifactFileName}}"
DISTRIBUTION_NAME="{{distributionExecutableName}}"
DISTRIBUTION_EXEC="{{distributionExecutableUnix}}"
DISTRIBUTION_ID="{{appImageComponentId}}"
DISTRIBUTION_URL="{{appImageDistributionUrl}}"
APPIMAGETOOL_URL="https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-${SYSTEM_ARCH}.AppImage"

# create build directory for needed resources
mkdir -p build-${SYSTEM_ARCH}/
cd build-${SYSTEM_ARCH}/

# download AppImage tool
wget -c $APPIMAGETOOL_URL
chmod +x "./appimagetool-${SYSTEM_ARCH}.AppImage"

# download and extract release
wget -c -O $DISTRIBUTION_FILE $DISTRIBUTION_URL
if [ "$DISTRIBUTION_FILE" = *.zip ]]; then
  unzip -o $DISTRIBUTION_FILE
else
  tar -xvf $DISTRIBUTION_FILE
fi

# create AppDir structure
mkdir -p AppDir/
mkdir -p AppDir/usr/share/
mv "${DISTRIBUTION_FILE_NAME}" AppDir/usr/share/${DISTRIBUTION_NAME}
mkdir -p AppDir/usr/bin/
ln -s AppDir/usr/share/${DISTRIBUTION_NAME}/bin/${DISTRIBUTION_EXEC} AppDir/usr/bin/${DISTRIBUTION_EXEC}
mkdir -p AppDir/usr/share/applications/
mkdir -p AppDir/usr/share/icons/hicolor/128x128/
cp ../icons/${DISTRIBUTION_NAME}.png AppDir/usr/share/icons/hicolor/128x128/${DISTRIBUTION_NAME}.png
mkdir -p AppDir/usr/share/metainfo
cp ../${DISTRIBUTION_ID}.appdata.xml AppDir/usr/share/metainfo
cp ../${DISTRIBUTION_ID}.appdata.xml AppDir/usr/share/metainfo/${DISTRIBUTION_NAME}.appdata.xml
cp ../${DISTRIBUTION_NAME}.desktop AppDir/usr/share/applications
ln -s usr/share/applications/${DISTRIBUTION_NAME}.desktop AppDir/${DISTRIBUTION_NAME}.desktop
ln -s usr/share/icons/hicolor/128x128/${DISTRIBUTION_NAME}.png AppDir/${DISTRIBUTION_NAME}.png
ln -s usr/share/icons/hicolor/128x128/${DISTRIBUTION_NAME}.png AppDir/.DirIcon

# create AppRun script
cat > AppDir/AppRun << "EOF"
#!/usr/bin/env bash

HERE="$(dirname "$(readlink -f "${0}")")"

exec "$HERE/usr/share/{{distributionExecutableName}}/bin/{{distributionExecutableUnix}}" "$@"
EOF

chmod +x AppDir/AppRun

# build AppImage
ARCH=${SYSTEM_ARCH} "./appimagetool-${SYSTEM_ARCH}.AppImage" -v AppDir/ "../${DISTRIBUTION_NAME}-${DISTRIBUTION_VERSION}-${SYSTEM_ARCH}.AppImage"
