# 2010-12-31 Sebastian Rockel
# Jadex start configurations
# from:
# http://jadex-agents.informatik.uni-hamburg.de/xwiki/bin/view/Standalone+Platform+Guide/02+Starting+the+Platform
# in short:
#The following command-line args can additionally be used.
#
#<start command> [-conf <conf.xml>] [-configname <configname>] [-platformname
#<platformname>] [-componentfactory <classname>] [-adapterfactory <classname>] 
#
#-conf: Filename to the startup application xml, which is used as basic platform
#configuration.
#-configname: This parameter allows to start the platform in
#different setups, e.g. without gui or awareness agent.
#
# Currently the available options are: "all_kernels (rms, awa, jcc)",
# "all_kernels (rms, awa, jcc)", "all_kernels (rms, jcc)", "all_kernels (rms,
# awa)", "all_kernels (rms)", "all_kernels_no_daemons". The abbreviations rms,
# awa and jcc are used for remote management service, awareness agent and Jadex
# Control Center (the gui agent). 
# 
.PHONY: default jadex jadexgui jadexplain jadexrobot

default:
	@echo "Available start configurations:"
	@echo "make jadex      - no gui"
	@echo "make jadexgui   - with gui"
	@echo "make jadexplain - no daemons"
	@echo "make jadexrobot - robot configuration"

jadex:
	java -jar jadex/lib/jadex-platform-standalone-launch-2.0-rc6.jar \
		-configname "all_kernels (rms, awa)" \
		-conf jadex/sources/jadex-platform-standalone/src/main/java/jadex/standalone/Platform.application.xml

jadexgui:
	java -jar jadex/lib/jadex-platform-standalone-launch-2.0-rc6.jar \
		-configname "all_kernels (rms, awa, jcc)" \
		-conf jadex/sources/jadex-platform-standalone/src/main/java/jadex/standalone/Platform.application.xml

jadexplain:
	java -jar jadex/lib/jadex-platform-standalone-launch-2.0-rc6.jar \
		-configname "all_kernels_no_daemons" \
		-conf jadex/sources/jadex-platform-standalone/src/main/java/jadex/standalone/Platform.application.xml

jadexrobot:
	java -jar jadex/lib/jadex-platform-standalone-launch-2.0-rc6.jar \
		-configname "all_kernels (rms, awa)" \
		-conf jadex/sources/jadex-platform-standalone/src/main/java/jadex/standalone/Platform.application.xml
