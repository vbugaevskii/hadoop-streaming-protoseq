all: setup proto pyenv

setup:
	mkdir -p mapreduce/streaming
	cp mapper.py mapreduce/streaming

proto_create:
	mkdir -p python/src.gen java/src.gen
	protoc -I=etc --python_out=python/src.gen --java_out=java/src.gen etc/*
	touch proto_create

proto_remove:
	rm proto_create || true

proto: proto_create
	mvn install
	cp target/protogen-0.0.1.jar mapreduce/protogen.jar
	cp python/src.gen/* mapreduce/streaming

pyenv_create:
	conda env create -f pyenv.yml
	touch pyenv_create

pyenv_remove:
	conda remove --name streaming-env-py37 --all -y
	rm pyenv_create || true

pyenv: pyenv_create
	rm mapreduce/streaming-env-py37.tar.gz || true
	conda pack -n streaming-env-py37 -o mapreduce/streaming-env-py37.tar.gz

clean: pyenv_remove proto_remove
	rm -r target python java || true
	rm streaming-env-py37.tar.gz || true
	rm -r mapreduce || true
