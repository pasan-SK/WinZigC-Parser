# Define variables
JAVAC = javac
SOURCE_DIR = .
OUTPUT_DIR = .

# Define targets and rules
all: check_javac winzigc.class

check_javac:
	@which $(JAVAC) > /dev/null || (echo "Error: javac not found. Please install a Java Development Kit (JDK)." && exit 1)

winzigc.class: $(SOURCE_DIR)/winzigc.java
	$(JAVAC) $<

clean:
	rm -f winzigc.class

.PHONY: all check_javac clean
