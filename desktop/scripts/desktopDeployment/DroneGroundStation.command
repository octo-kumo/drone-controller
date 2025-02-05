#!/bin/bash
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
cd "${DIR}"
./jre/Contents/Home/bin/java -XstartOnFirstThread -classpath "lib/*" me.kumo.drone.desktopmodule.DesktopLauncher
exit 0
