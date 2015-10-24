all: subblocks

subblocks:
	cd user; make
	cd cs; make
	cd ss; make
 
clean:
	rm -f user/*.class
	rm -f cs/*.class
	rm -f ss/*.class
