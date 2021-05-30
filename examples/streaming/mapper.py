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
