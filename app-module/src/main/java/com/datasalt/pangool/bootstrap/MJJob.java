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

import com.datasalt.utils.mapred.joiner.MultiJoinChanneledMapper;
import com.datasalt.utils.mapred.joiner.MultiJoinDatum;
import com.datasalt.utils.mapred.joiner.MultiJoinReducer;
import com.datasalt.utils.mapred.joiner.MultiJoiner;

public class MJJob extends Configured implements Tool { 
	
  public static class Map1 extends MultiJoinChanneledMapper<LongWritable, Text, Text> {

  	protected void setup(Context context) throws IOException ,InterruptedException {
  		System.out.println("Input Split: " + ToStringBuilder.reflectionToString(context.getInputSplit()));
  		super.setup(context);
  	};
  	
  	int count = 0;
  	
  	protected void map(LongWritable key, Text value, Context context) throws IOException ,InterruptedException {  	
			emit(new Text("join" + count++), new Text("m1: " + value));
		};		
	}

  public static class Map2 extends MultiJoinChanneledMapper<LongWritable, Text, Text> {

  	protected void setup(Context context) throws IOException ,InterruptedException {  		
  		System.out.println("Input Split: " + ToStringBuilder.reflectionToString(context.getInputSplit()));
  		super.setup(context);
  	};
  	
  	int count = 0;
  	
  	protected void map(LongWritable key, Text value, Context context) throws IOException ,InterruptedException {
			emit(new Text("join" + count++), new Text("m2: " + value));
		};		
	}

  public static class Red extends MultiJoinReducer<Text, Text> {
  	
  	protected void reduce(com.datasalt.utils.mapred.joiner.MultiJoinPair key, java.lang.Iterable<com.datasalt.utils.mapred.joiner.MultiJoinDatum<?>> values, Context context) throws IOException ,InterruptedException {

  		for (MultiJoinDatum datum : values) {
  			Text t1 = deserializeKey(key, new Text());
  			Text t2 = deserialize(datum);
  			
  			context.write(t1, t2);
  		}
  		
  	};
  	
  }
  
	
	@Override
  public int run(String[] args) throws Exception {
		if(args.length != 3) {
			System.out.println("Invalid number of arguments\n\n" +
					"Usage: MJJob <input_path1> <input_path2> <output_path>\n\n");
			return -1;
		}
		String input1 = args[0];
		String input2 = args[1];
		String output = args[2];
		
		FileSystem.get(getConf()).delete(new Path(output), true);
		
		MultiJoiner jo = new MultiJoiner("MJJob", getConf());
		jo.setJarByClass(MJJob.class);
		jo.setOutputFormat(TextOutputFormat.class);
		jo.setOutputKeyClass(Text.class);
		jo.setOutputValueClass(Text.class);
		jo.setReducer(Red.class);
		jo.setOutputPath(new Path(output));
		jo.addChanneledInput(0, new Path(input1), Text.class, TextInputFormat.class, Map1.class);
		jo.addChanneledInput(1, new Path(input2), Text.class, TextInputFormat.class, Map2.class);
		jo.getJob().waitForCompletion(true);
		
		return 0;
  }

	public static void main(String args[]) throws Exception {
		ToolRunner.run(new MJJob(), args);
	}
}
