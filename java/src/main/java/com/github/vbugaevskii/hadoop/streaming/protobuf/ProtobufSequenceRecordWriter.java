package com.github.vbugaevskii.hadoop.streaming.protobuf;

import org.apache.hadoop.io.BytesWritable;

import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProtobufSequenceRecordWriter implements RecordWriter<BytesWritable, BytesWritable> {
    private DataOutputStream output;

    public ProtobufSequenceRecordWriter(OutputStream output) {
        this.output = new DataOutputStream(output);
    }

    @Override
    public void write(BytesWritable key, BytesWritable value) throws IOException {
        // ignore key
        value.write(output);
    }

    @Override
    public void close(Reporter reporter) throws IOException {
        output.close();
    }
}
