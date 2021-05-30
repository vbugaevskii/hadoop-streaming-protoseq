# ProtoSeq

ProtoSeq is a python library that allows working with sequences of specified protobuf messages. The sequence of protobuf messages is stored as a sequence of pairs:
* size of message in bytes – 4 bytes (int);
* protobuf message bytes.

This sequence format is a flexible storage format similar to [Hadoop SequenceFile](https://hadoop.apache.org/docs/current/api/org/apache/hadoop/io/SequenceFile.html) that allows to process files with multiprocessing (e.g. with Hadoop) if extra index is provided.

This repository is an example how to work with binary data using Hadoop Streaming.

## Quick Start

Install package with pip: `pip install protoseq`.

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

## Hadoop Streaming Example

Here is an example of Map-Reduce program (map-only) that copies a file in HDFS.

There are some dependencies we need to run the MR job:

```bash
$ tree mapreduce
mapreduce
├── hadoop-streaming-protoseq.jar
├── streaming
│   ├── address_pb2.py
│   └── mapper.py
└── streaming-env-py37.tar.gz

1 directory, 5 files
``` 

You can get all these files just running `make all` command inside [example directory](examples/streaming):
* `hadoop-streaming-protoseq.jar` – ProtoSeq library for Hadoop Streaming;
* `streaming-env-py37.tar.gz` – environment with python3 and installed ProtoSeq package;
* `streaming/mapper.py` – mapper stage for job;
* `streaming/address_pb2.py` – generated protobuf sources for python.

You supposed to have [conda](https://conda.io/projects/conda/en/latest/index.html) and [conda pack](https://conda.github.io/conda-pack/) to prepare `streaming-env-py37.tar.gz` for streaming.

To run MR program we need to execute command:

```bash
${HADOOP} jar ${HADOOP_STREAMING} \
    -D mapred.job.name="Example: Copy proto file" \
    -D mapred.reduce.tasks=0 \
    -D stream.map.input='rawbytes' \
    -D stream.map.input.writer.class='org.apache.hadoop.streaming.io.RawBytesOutputReader' \
    -D stream.map.output='rawbytes' \
    -D stream.map.output.reader.class='org.apache.hadoop.streaming.io.RawBytesOutputReader' \
    -files "streaming/mapper.py" \
    -libjars "hadoop-streaming-protoseq.jar" \
    -archives "streaming-env-py37.tar.gz#env" \
    -inputformat  "com.github.vbugaevskii.hadoop.streaming.protobuf.ProtobufSequenceInputFormat" \
    -outputformat "com.github.vbugaevskii.hadoop.streaming.protobuf.ProtobufSequenceOutputFormat" \
    -mapper "env/bin/python streaming/mapper.py" \
    -input  "/tmp/v.bugaevskii/addresses.protoseq" \
    -output "/tmp/v.bugaevskii/protoseq_copy"
```

