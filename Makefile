GPGKEY      ="8038DEBE14AD09A4"
MFFILE      = build/manifest.mf
LICENCEFILE = build/LICENCE.txt
MACFILE     = build/HTGT-Debug-macOS_$(version)
MACSTART    = build/Start-HTGT-macOS_$(version)
SHFILE      = build/HTGT-Debug-Linux_$(version).sh
SHSTART     = build/Start-HTGT-Linux_$(version).sh
BATFILE     = build/HTGT-Debug-Windows_$(version).bat
BATSTART    = build/Start-HTGT-Windows_$(version).bat
SIGFILE     = build/HTGT_$(version).sha512.sig
CSUMFILE    = build/HTGT_$(version).sha512
JARFILE     = build/HTGT_$(version).jar
ZIPFILE     = build/HTGT_$(version).zip
VFILE       = htgt-version.txt

TARGET   = 8
XLINT    = -Xlint:all -Xlint:-options
JFLAGS   = -source $(TARGET) -target $(TARGET) -g -sourcepath ./src -classpath ./classes -d ./classes $(XLINT)
VMFLAGS  = -classpath ./classes
JC       = javac
JAVA     = java
JAR      = jar

sources  = $(wildcard src/*.java)
classes  = $(sources:.java=.class)
lbundles = $(wildcard src/LangBundle_*.properties)
rbundles = $(lbundles:src/LangBundle_%=src/RealLangBundle_%)
version  = $(strip $(shell $(JAVA) $(VMFLAGS) HTGT -v))
commit   = $(shell git rev-parse --short HEAD)

all: clean compile jar zip

i18n: $(rbundles)

RealLangBundle_%.properties: LangBundle_%.properties
	native2ascii -encoding UTF-8 $< $@
	sed -i 's/(UTF-8!)/(ISO-8859-1!)/' $@

compile: $(classes) i18n
	echo Original version: $(version)
	@echo git-$(commit) > $(VFILE)

	cp -af ./src/RealLangBundle_*.properties ./classes/

%.class: %.java
	$(JC) $(JFLAGS) $<

jar: compile
	@echo Packaging version $(version)

	@echo "Manifest-Version: 1.0" > $(MFFILE)
	@echo "Class-Path: ." >> $(MFFILE)
	@echo "Main-Class: HTGT" >> $(MFFILE)
	@echo "Permissions: all-permissions" >> $(MFFILE)

	cd ./classes && \
	$(JAR) -cmf ../$(MFFILE) ../$(JARFILE) ./*.class ./RealLangBundle_*.properties ../$(VFILE) && \
	chmod +x ../$(JARFILE) && $(RM) ../$(MFFILE)

zip: jar
	@echo Zipping version $(version)

	cp -af HTGT-Debug-Linux.sh $(SHFILE)
	sed -i "s/HTGT.jar/HTGT_$(version).jar/" $(SHFILE)

	cp -af Start-HTGT-Linux.sh $(SHSTART)
	sed -i "s/HTGT.jar/HTGT_$(version).jar/" $(SHSTART)

	cp -af HTGT-Debug-macOS $(MACFILE)
	sed -i "s/HTGT.jar/HTGT_$(version).jar/" $(MACFILE)

	cp -af Start-HTGT-macOS $(MACSTART)
	sed -i "s/HTGT.jar/HTGT_$(version).jar/" $(MACSTART)

	cp -af HTGT-Debug-Windows.bat $(BATFILE)
	sed -i "s/HTGT.jar/HTGT_$(version).jar/" $(BATFILE)

	cp -af Start-HTGT-Windows.bat $(BATSTART)
	sed -i "s/HTGT.jar/HTGT_$(version).jar/" $(BATSTART)

	cp -af LICENCE $(LICENCEFILE)
	unix2dos $(LICENCEFILE)

	zip -j $(ZIPFILE) $(LICENCEFILE) $(JARFILE) $(SHFILE) $(SHSTART) $(MACFILE) $(MACSTART) $(BATFILE) $(BATSTART)
	$(RM) $(LICENCEFILE) $(SHFILE) $(SHSTART) $(MACFILE) $(MACSTART) $(BATFILE) $(BATSTART)

sig: zip
	@echo Signing version $(version)

	sha512sum $(JARFILE) $(ZIPFILE) > $(CSUMFILE)
	sed -i 's#build/##' $(CSUMFILE) && $(RM) $(SIGFILE)
	gpg -u $(GPGKEY) --armor --output $(SIGFILE) --detach-sig $(CSUMFILE)

clean:
	$(RM) build/HTGT_*.*
	$(RM) $(MFFILE) $(VFILE) $(LICENCEFILE)
	$(RM) classes/*.class classes/*.properties src/*.class src/RealLangBundle_*.properties
