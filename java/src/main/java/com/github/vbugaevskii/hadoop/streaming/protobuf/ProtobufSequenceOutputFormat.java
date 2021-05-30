package com.github.vbugaevskii.hadoop.streaming.protobuf;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.compress.CompressionCodec;

import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.RecordWriter;

import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.DataOutputStream;
import java.io.IOException;

public class ProtobufSequenceOutputFormat extends FileOutputFormat<BytesWritable, BytesWritable> {
    @Override
    public RecordWriter<BytesWritable, BytesWritable> getRecordWriter(FileSystem ignored, JobConf conf, String name, Progressable progressable)
            throws IOException {
        boolean isCompressed = getCompressOutput(conf);
        DataOutputStream output = null;

        if (isCompressed) {
            Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(conf, GzipCodec.class);
            CompressionCodec codec = ReflectionUtils.newInstance(codecClass, conf);

            Path file = getTaskOutputPath(conf, name + codec.getDefaultExtension());
            FileSystem fs = file.getFileSystem(conf);

            output = new DataOutputStream(codec.createOutputStream(fs.create(file, false)));
        } else {
            Path file = getTaskOutputPath(conf, name);
            FileSystem fs = file.getFileSystem(conf);

            output = fs.create(file, false);
        }

        return new ProtobufSequenceRecordWriter(output);
    }
}
