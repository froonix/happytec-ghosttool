MFFILE = build/manifest.mf
JARFILE = build/HTGT.jar
ZIPFILE = build/HTGT.zip

JFLAGS = -g -cp src -d classes
JC = javac
JAR = jar

sources = $(wildcard src/*.java)
classes = $(subst src/,classes/,$(sources:.java=.class))

all: compile jar zip

compile: $(classes)

classes/%.class: src/%.java
	$(JC) $(JFLAGS) $<

jar:
	@echo "Manifest-Version: 1.0" > $(MFFILE)
	@echo "Class-Path: ." >> $(MFFILE)
	@echo "Main-Class: HTGT" >> $(MFFILE)

	$(JAR) -cmf $(MFFILE) $(JARFILE) $(classes) && \
	chmod +x $(JARFILE) && \
	$(RM) $(MFFILE)

zip:
#	TODO: Add licence file and version string!
	zip -j $(ZIPFILE) $(JARFILE) HTGT.bat HTGT.sh

clean:
	$(RM) $(MFFILE) $(JARFILE) $(ZIPFILE)
	$(RM) classes/*.class src/*.class
