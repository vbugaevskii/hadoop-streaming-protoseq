package com.github.vbugaevskii.hadoop.streaming.protobuf;

import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.RecordReader;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.BytesWritable;

import com.google.protobuf.AbstractMessage;

import java.io.IOException;

public class ProtobufSequenceInputFormat<T extends AbstractMessage>
        extends FileInputFormat<NullWritable, BytesWritable> {
    @Override
    protected boolean isSplitable(FileSystem fs, Path filename) {
        return false;
    }

    @Override
    public RecordReader<NullWritable, BytesWritable> getRecordReader(InputSplit split, JobConf conf, Reporter reporter)
            throws IOException {
        return new ProtobufSequenceRecordReader<T>(conf, split);
    }
}
