#!/usr/bin/env python3
# -*- coding: utf-8 -*-


class ProtobufSequenceWriter:
    def __init__(self, buffer_out):
        self.buffer_out = buffer_out

    def write(self, record):
        num_bytes_written = 0
        if record.IsInitialized():
            num_bytes_written = self._write_bytes(record.SerializeToString())
        return num_bytes_written

    def _write_bytes(self, bytes_):
        num_bytes_written = 0
        num_bytes_written += self.buffer_out.write(len(bytes_).to_bytes(4, byteorder='big', signed=True))
        num_bytes_written += self.buffer_out.write(bytes_)
        return num_bytes_written


class ProtobufSequenceWriterStreaming(ProtobufSequenceWriter):
    KEY = b'\x00\x00\x00\x00'

    def _write_bytes(self, bytes_):
        num_bytes_written = 0
        num_bytes_written += self.buffer_out.write(self.KEY)
        num_bytes_written += super()._write_bytes(bytes_)
        return num_bytes_written
