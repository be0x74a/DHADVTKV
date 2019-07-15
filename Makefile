VER=1.0.4

.PHONY: build clean doc release

build:
	rm -fr build
	mkdir build
	javac -classpath src:lib/jep-2.3.0.jar:lib/djep-1.0.0.jar `find src -name "*.java"` -d ./build
clean:
	rm -rf build
	rm -rf doc
	rm -fr peersim-$(VER)
doc:
	rm -rf doc/*
	javadoc -overview overview.html -classpath src:lib/jep-2.3.0.jar:lib/djep-1.0.0.jar -d doc \
                -group "Peersim" "peersim*" \
                -group "DHADVTKV" "dhadvtkv.*" \
		peersim \
		peersim.cdsim \
		peersim.config \
		peersim.core \
		peersim.dynamics \
		peersim.edsim \
		peersim.graph \
		peersim.rangesim \
		peersim.reports \
		peersim.transport \
		peersim.util \
		peersim.vector

docnew:
	rm -rf doc/*
	javadoc -docletpath lib/peersim-doclet.jar -doclet peersim.tools.doclets.standard.Standard -classpath src:lib/jep-2.3.0.jar:lib/djep-1.0.0.jar -d doc \
                -group "Peersim" "peersim*" \
                -group "DHADVTKV" "dhadvtkv.*" \
		peersim \
		peersim.cdsim \
		peersim.config \
		peersim.core \
		peersim.dynamics \
		peersim.edsim \
		peersim.graph \
		peersim.rangesim \
		peersim.reports \
		peersim.transport \
		peersim.util \
		peersim.vector


release: clean build docnew
	rm -fr peersim-$(VER)
	mkdir peersim-$(VER)
	cp -r doc peersim-$(VER)
	cp Makefile README build.xml lib/peersim-doclet.jar peersim-$(VER)
	mkdir peersim-$(VER)/configs
	mkdir peersim-$(VER)/configs/latencies
	mkdir peersim-$(VER)/configs/simulation_params
	cp configs/latencies/*.latencies peersim-$(VER)/configs/latencies
	cp configs/simulation_params/*.config peersim-$(VER)/configs/simulation_params
	mkdir peersim-$(VER)/src
	cp --parents `find src/peersim src/dhadvtkv -name "*.java"` peersim-$(VER)
	cd build ; jar cf ../peersim-$(VER).jar `find peersim dhadvtkv -name "*.class"`
	mv peersim-$(VER).jar peersim-$(VER)
	cp lib/jep-2.3.0.jar peersim-$(VER)
	cp lib/djep-1.0.0.jar peersim-$(VER)
