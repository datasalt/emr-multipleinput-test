package com.datasalt.pangool.bootstrap;

import java.io.IOException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MIJob extends Configured implements Tool {
	
  public static class Map1 extends Mapper<LongWritable, Text, Text, NullWritable> {

  	protected void setup(org.apache.hadoop.mapreduce.Mapper<LongWritable,Text,Text,NullWritable>.Context context) throws IOException ,InterruptedException {  		
  		System.out.println("Input Split: " + ToStringBuilder.reflectionToString(context.getInputSplit()));
  	};
		
		protected void map(LongWritable key, Text value, org.apache.hadoop.mapreduce.Mapper<LongWritable,Text,Text,NullWritable>.Context context) throws IOException ,InterruptedException {
			context.write(new Text("m1: " + value), NullWritable.get());
		};		
	}
	
  public static class Map2 extends Mapper<LongWritable, Text, Text, NullWritable> {

  	protected void setup(org.apache.hadoop.mapreduce.Mapper<LongWritable,Text,Text,NullWritable>.Context context) throws IOException ,InterruptedException {  		
  		System.out.println("Input Split: " + ToStringBuilder.reflectionToString(context.getInputSplit()));
  	};
  	
		protected void map(LongWritable key, Text value, org.apache.hadoop.mapreduce.Mapper<LongWritable,Text,Text,NullWritable>.Context context) throws IOException ,InterruptedException {
			context.write(new Text("m2: " + value), NullWritable.get());
		};		
	}
	
	@Override
  public int run(String[] args) throws Exception {
		if(args.length != 3) {
			System.out.println("Invalid number of arguments\n\n" +
					"Usage: MIJob <input_path1> <input_path2> <output_path>\n\n");
			return -1;
		}
		String input1 = args[0];
		String input2 = args[1];
		String output = args[2];
		
		FileSystem.get(getConf()).delete(new Path(output), true);
		
		Job job = new Job(getConf(), "MIJob");
		job.setJarByClass(MIJob.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		
		MultipleInputs.addInputPath(job, new Path(input1), TextInputFormat.class, Map1.class);
		MultipleInputs.addInputPath(job, new Path(input2), TextInputFormat.class, Map2.class);
		
		FileOutputFormat.setOutputPath(job, new Path(output));
		
		job.waitForCompletion(true);
		return 0;
  }

	public static void main(String args[]) throws Exception {
		ToolRunner.run(new MIJob(), args);
	}
}
