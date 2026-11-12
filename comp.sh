 jpackage \
--name "Oliver's CodeName" \
--input out/artifacts/codeNamesHost_jar \
--main-jar codeNamesHost.jar \
--type app-image \
--vendor "uk.olivercrebbin" \
--dest dist/ \
--java-options "-Dapple.laf.useScreenMenuBar=true" \
--icon "icon.icns"
