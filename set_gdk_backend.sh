#!/bin/sh
rm -rf extracted_dir
DEB_FILE=$(ls bin/linux-gtk3/setup/*.deb)
dpkg-deb -R $DEB_FILE extracted_dir
(echo 'export GDK_BACKEND=x11' && cat extracted_dir/DEBIAN/postinst) > new_postinst && mv new_postinst extracted_dir/DEBIAN/postinst
(echo '#!/bin/sh' && cat extracted_dir/DEBIAN/postinst) > new_postinst && mv new_postinst extracted_dir/DEBIAN/postinst
chmod 755 extracted_dir/DEBIAN/postinst

dpkg-deb -b extracted_dir $DEB_FILE

