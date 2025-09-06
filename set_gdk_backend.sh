#!/bin/sh

# Die Idee ist nun extracted_dir/opt/ldparteditor/bin/LDPartEditorX11 als Script zu erstellen und dann opt/ldparteditor/bin/LDPartEditor zu starten.

rm -rf extracted_dir
DEB_FILE=$(ls bin/linux-gtk3/setup/*.deb)
dpkg-deb -R $DEB_FILE extracted_dir
touch extracted_dir/opt/ldparteditor/bin/LDPartEditorX11
(echo '#!/bin/sh') >> extracted_dir/opt/ldparteditor/bin/LDPartEditorX11
(echo 'export GDK_BACKEND=x11') >> extracted_dir/opt/ldparteditor/bin/LDPartEditorX11
(echo '/opt/ldparteditor/bin/LDPartEditor "$@"') >> extracted_dir/opt/ldparteditor/bin/LDPartEditorX11
chmod 755 extracted_dir/opt/ldparteditor/bin/LDPartEditorX11

dpkg-deb -b extracted_dir $DEB_FILE

