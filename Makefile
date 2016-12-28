MFFILE = build/manifest.mf
JARFILE = build/HTGT.jar
ZIPFILE = build/HTGT.zip

JFLAGS = -g -sourcepath ./src -classpath ./classes -d ./classes
JC = javac
JAR = jar

sources = $(wildcard src/*.java)
classes = $(sources:.java=.class)

all: compile jar zip

compile: $(classes)

%.class: %.java
	$(JC) $(JFLAGS) $<

jar:
	@echo "Manifest-Version: 1.0" > $(MFFILE)
	@echo "Class-Path: ." >> $(MFFILE)
	@echo "Main-Class: HTGT" >> $(MFFILE)

	cd ./classes && \
	$(JAR) -cmf ../$(MFFILE) ../$(JARFILE) ./*.class && \
	chmod +x ../$(JARFILE)

zip:
#	TODO: Add licence file and version string!
	zip -j $(ZIPFILE) $(JARFILE) HTGT.bat HTGT.sh

clean:
	$(RM) $(MFFILE) $(JARFILE) $(ZIPFILE)
	$(RM) classes/*.class src/*.class
