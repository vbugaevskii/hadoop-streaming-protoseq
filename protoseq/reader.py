#!/usr/bin/env python3
# -*- coding: utf-8 -*-


class ProtobufSequenceReader:
    def __init__(self, cls_proto, buffer_in):
        self.cls_proto = cls_proto
        self.buffer_in = buffer_in

    def __iter__(self):
        return self

    def __next__(self):
        record = self.read()
        if record is None:
            raise StopIteration
        return record

    def read(self):
        proto = self.cls_proto()
        proto.ParseFromString(self._read_bytes())
        return proto if proto.IsInitialized() else None

    def _read_bytes(self):
        proto_size = int.from_bytes(self.buffer_in.read(4), byteorder='big', signed=True)
        proto_data = self.buffer_in.read(proto_size)
        return proto_data


class ProtobufSequenceReaderStreaming(ProtobufSequenceReader):
    def read(self):
        # skip NullWritable key
        _ = self._read_bytes()
        return super().read()


if __name__ == '__main__':
    import sys
    import argparse
    import importlib.util

    ap = argparse.ArgumentParser(
        description='Tool for protobuf printing',
        usage='python %(prog)s --proto addressbook_pb2.py -c AddressBook',
    )
    ap.add_argument('-p', '--proto', required=True, help='protobuf file to import')
    ap.add_argument('-c', '--cls', required=True, help='protobuf class to import')
    args = ap.parse_args()

    spec = importlib.util.spec_from_file_location('custom_pb2', args.proto)
    custom_pb2 = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(custom_pb2)

    cls_proto = getattr(custom_pb2, args.cls)

    reader = ProtobufSequenceReader(cls_proto, sys.stdin.buffer)
    for record in reader:
        print(record, end='\n\n')
