## Hadoop Streaming Example

Here is an example of Map-Reduce program (map-only) that copies a file in HDFS.

There are some dependencies we need to run the MR job:

```bash
$ tree mapreduce
mapreduce
├── hadoop-streaming-protoseq.jar
├── protogen.jar
├── streaming
│   ├── address_pb2.py
│   └── mapper.py
└── streaming-env-py37.tar.gz

1 directory, 5 files
``` 

You can get all these files just running `make all` command inside this directory:
* `protogen.jar` – [compiled](https://developers.google.com/protocol-buffers/docs/javatutorial#compiling-your-protocol-buffers) protobuf classes for java;
* `hadoop-streaming-protoseq.jar` – ProtoSeq library for Hadoop Streaming;
* `streaming-env-py37.tar.gz` – environment with python3 and installed ProtoSeq package;
* `streaming/mapper.py` – mapper stage for job;
* `streaming/address_pb2.py` – generated protobuf sources for python.

[Mapper](mapper.py) can look this way:

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
    -D stream.map.input='rawbytes' \
    -D stream.map.input.writer.class='org.apache.hadoop.streaming.io.RawBytesOutputReader' \
    -D stream.map.output='rawbytes' \
    -D stream.map.output.reader.class='org.apache.hadoop.streaming.io.RawBytesOutputReader' \
    -files "streaming/mapper.py" \
    -libjars "hadoop-streaming-protoseq.jar,protogen.jar" \
    -archives "streaming-env-py37.tar.gz#env" \
    -inputformat "com.github.vbugaevskii.hadoop.streaming.protobuf.ProtobufSequenceInputFormat" \
    -outputformat "com.github.vbugaevskii.hadoop.streaming.protobuf.ProtobufSequenceOutputFormat" \
    -mapper "env/bin/python streaming/mapper.py" \
    -input  "/tmp/v.bugaevskii/addresses.protoseq" \
    -output "/tmp/v.bugaevskii/protoseq_copy"
```
