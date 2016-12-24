JFLAGS = -g
JC = javac

sources = $(wildcard *.java)
classes = $(sources:.java=.class)

all: HTGT jar zip

HTGT: $(classes)

%.class: %.java
	$(JC) $(JFLAGS) $<

jar:
	@echo "Manifest-Version: 1.0" > manifest.mf
	@echo "Class-Path: ." >> manifest.mf
	@echo "Main-Class: HTGT" >> manifest.mf
	@echo "" >> manifest.mf

	jar -cmf manifest.mf HTGT.jar $(classes) && \
	chmod +x HTGT.jar

zip:
	# Add licence file and version string!
	zip HTGT.zip HTGT.jar HTGT.sh HTGT.bat

clean:
	$(RM) *.class
	$(RM) manifest.txt
	$(RM) HTGT.jar HTGT.zip HTGT
