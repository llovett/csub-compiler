############################################################
# Makefile
# 
# Compiles all the files necessary for the CSUB compiler.
# Each component of the compiler is separated out into
# its own package, which has its own Makefile, called here.
# 
# Authors: Liz Bennett, Luke Lovett
# Last Modified: Fri Feb 24 17:47:05 EST 2012
############################################################


###############################
# COMPILER AND COMPILER FLAGS #
###############################
JC = javac
JFLAGS = -g -Xlint


############
# PACKAGES #
############

PACKAGES = \
	lexer \
	parser \
	analyzer \
	generator

###########################
# GET READY FOR BUILDING! #
###########################
export CSUB_ROOT:= $(CURDIR)

ifdef PACKAGE
PACKAGE_LIST 	= $(subst .,,$(PACKAGE))
else	       
PACKAGE_LIST 	= $(subst .,./,$(PACKAGES))
endif	       
PACKAGE_LOC  	= $(subst .,./,$(PACKAGE))
PACKAGE_DIR  	= $(PACKAGE_LOC)
PLIST_BUILD  	= $(patsubst %,$(CSUB_ROOT)/%/.build,$(PACKAGE_LIST))
JAVA_FILES      = $(filter  %.java,$(SOURCE))
CLASS_FILES     = $(JAVA_FILES:%.java=$(PACKAGE_DIR)/%.class)


#######################
# MAIN MAKEFILE RULES #
#######################

# Clear default targets for building
.SUFFIXES: .java .class

### RULE 0
# Make a ".class" out of any ".java" file
$(PACKAGE_DIR)/%.class : $(PACKAGE_LOC)/%.java
	@(echo "Compiling $<" ; $(JC) $(JFLAGS) $< )

### RULE 1
# Make a ".class" out of a ".java" file within a package. Calls RULE 0.
%.class: $(PACKAGE_LOC)/%.java
	$(MAKE) -k $(PACKAGE_DIR)/$@

### RULE 2
# Calls the Makefile on a package's directory. This will call RULE 5.
%.build:
	$(MAKE) -k -f $(subst .build,Makefile,$@) compile

### RULE 3
# Builds each package in turn. Calls RULE 2.
all: $(PLIST_BUILD)
	@echo "Done."

### RULE 4
# A messy rule for clearing out all ".class" files,
# as well as any junk left by vim or emacs
clean:
	@find $(CSUB_ROOT) -name "*.class" | xargs rm -f
	@find $(CSUB_ROOT) -name "*~" | xargs rm -f
	@find $(CSUB_ROOT) -name "\#*" | xargs rm -f

### RULE 5
# What to do with ".class" dependencies. Calls RULE 1
compile: $(CLASS_FILES)

### RULE 6
# Create exuberant tags.
tags:
	@find . -name *.java | etags -
