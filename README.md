# ProtoSeq

ProtoSeq is a python library that alows working with sequences of specified protobuf messages. The sequence of protobuf messages is stored as sequence of pairs:
* size of message in bytes - 4 bytes (int);
* protobuf message bytes.

This is a flexible storage format that allows to process sequnce with multiprocessing (e.g. with Hadoop) if extra index file is provided.

Unlike popular [Hadoop SequenceFile](https://hadoop.apache.org/docs/current/api/org/apache/hadoop/io/SequenceFile.html) format, ProtoSeq doesn't store meta info inside data files, so it can easily be ported to any programming language. 

## Quick start

Install package with pip: `pip install protogen`.

This is an example program that reads file in protoseq format, saves it to temprorary file and prints protobufs in human readable format.

```python
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import address_pb2

from tempfile import TemporaryFile

from protoseq.reader import ProtobufSequenceReader
from protoseq.writer import ProtobufSequenceWriter

with TemporaryFile(mode='wb+') as f_out:
    reader = ProtobufSequenceReader(address_pb2.Address, sys.stdin.buffer)
    writer = ProtobufSequenceWriter(f_out)

    for record in reader:
        writer.write(record)

    f_out.seek(0)
    reader = ProtobufSequenceReader(address_pb2.Address, f_out)

    for record in reader:
        print(record)
```

Here `record` is an instance of `address_pb2.Address`.

This program needs an `address_pb2.py` file – [generated sources](https://developers.google.com/protocol-buffers/docs/pythontutorial#compiling-your-protocol-buffers) for python. `address_pb2.py` can be changed to your own protobuf.

## Hadoop Streaming

As it was discussed earliear ProtoSeq format is supposed to be used as an alternative to Hadoop SequenceFile format. ProtoSeq has a python [hadoop streaming](https://hadoop.apache.org/docs/stable/hadoop-streaming/HadoopStreaming.html) support.

Here is an example of Map-Reduce program (map-only) that copies a file in HDFS.

There are some dependencies for MR job:

```bash
$ tree
.
├── address-pb2.jar
├── hadoop-streaming-protobuf.jar
├── streaming
│   ├── address_pb2.py
│   └── mapper.py
└── streaming-env-py37.tar.gz

1 directory, 5 files
```

* `address_pb2.jar` – [compiled version](https://developers.google.com/protocol-buffers/docs/javatutorial#compiling-your-protocol-buffers) of protobuf classes for java;
* `hadoop-streaming-protoseq.jar` – ProtoSeq library for Hadoop Streaming;
* `streaming-env-py37.tar.gz` – environment with python3 and intalled ProtoSeq package;
* `streaming/mapper.py` – mapper stage for job;
* `streaming/address_pb2.py` – generated protobuf sources for python.

Mapper can look this way:

```python
#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import address_pb2

from protoseq.reader import ProtobufSequenceReaderStreaming
from protoseq.writer import ProtobufSequenceWriterStreaming


def mapper():
    reader = ProtobufSequenceReaderStreaming(address_pb2.Address, sys.stdin.buffer)
    writer = ProtobufSequenceWriterStreaming(sys.stdout.buffer)

    for record in reader:
        writer.write(record)


if __name__ == "__main__":
    mapper()
```

To run MR program we need to excute command:

```bash
${HADOOP} jar ${HADOOP_STREAMING} \
    -D mapred.job.name="Example: Copy proto file" \
    -D mapred.reduce.tasks=0 \
    -D stream.map.protoseq.class='address_pb2.Address' \
    -D stream.map.input='rawbytes' \
    -D stream.map.input.writer.class='org.apache.hadoop.streaming.io.RawBytesOutputReader' \
    -D stream.map.output='rawbytes' \
    -D stream.map.output.reader.class='org.apache.hadoop.streaming.io.RawBytesOutputReader' \
    -files "streaming/mapper.py" \
    -libjars "hadoop-streaming-protoseq.jar,address_pb2.jar" \
    -archives "streaming-env-py37.tar.gz#env" \
    -inputformat "com.github.vbugaevskii.hadoop.streaming.protobuf.ProtobufSequenceInputFormat" \
    -outputformat "com.github.vbugaevskii.hadoop.streaming.protobuf.ProtobufSequenceOutputFormat" \
    -mapper "env/bin/python streaming/mapper.py" \
    -input  "/tmp/v.bugaevskii/addresses.protoseq" \
    -output "/tmp/v.bugaevskii/protoseq_copy"
```

Parameter `stream.map.protoseq.class` specifies protobuf class, that is stored in input file `addresses.protoseq`.

Full example project can be found here.
