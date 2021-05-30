package com.github.vbugaevskii.hadoop.streaming.protobuf;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.BytesWritable;

import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;

import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.RecordReader;

import com.google.protobuf.AbstractMessage;

public class ProtobufSequenceRecordReader<T extends AbstractMessage>
        implements RecordReader<NullWritable, BytesWritable> {
    private DataInputStream input;

    public ProtobufSequenceRecordReader(Configuration conf, InputSplit split) throws IOException {
        FileSplit fileSplit = (FileSplit) split;
        Path filePath = fileSplit.getPath();
        FSDataInputStream inputFs = FileSystem.get(conf).open(filePath);

        CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(conf);
        CompressionCodec codec = compressionCodecs.getCodec(filePath);
        input = (codec == null) ? inputFs : new DataInputStream(codec.createInputStream(inputFs));
    }

    @Override
    public NullWritable createKey() {
        return NullWritable.get();
    }

    @Override
    public BytesWritable createValue() {
        return new BytesWritable();
    }

    @Override
    public boolean next(NullWritable key, BytesWritable value) throws IOException {
        try {
            int messageSize = this.input.readInt();
            byte[] messageBytes = new byte[messageSize];
            this.input.readFully(messageBytes);
            value.set(messageBytes, 0, messageBytes.length);
            return true;
        } catch (EOFException eof) {
            return false;
        }
    }

    @Override
    public float getProgress() {
        return 0;
    }

    @Override
    public long getPos() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        if (input != null) {
            input.close();
        }
    }
}