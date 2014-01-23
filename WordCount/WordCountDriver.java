// FIXME: This file is for practice purposes only. It should be deleted at some point.

package owltools.cli;

// Import Hadoop for the MapReduce libraries.
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

// This is the class which implements the MapReduce Driver for the purpose of WordCount.
public class WordCountDriver
{
	// This runs the Mapper and Reducer on a given file and writes the output to a separate file.
	public static void wordCount(String inputFile, String outputFile) throws Exception
	{
		// Create a new job to run.
		JobConf conf = new JobConf(WordCountDriver.class);
		conf.setJobName("wordcount");
		
		// We want to output <Text, IntWritable> key-value pairs for words and counts.
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);
		
		// We want to map with our Mapper and reduce with our Reducer.
		conf.setMapperClass(WordCountMapper.class);
		conf.setCombinerClass(WordCountReducer.class);
		conf.setReducerClass(WordCountReducer.class);
		
		// We want to input text and output text
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
		
		// Specify which files we're using for input and output.
		FileInputFormat.setInputPaths(conf, new Path(inputFile));
		FileOutputFormat.setOutputPath(conf, new Path(outputFile));
		
		// Run MapReduce.
		JobClient.runJob(conf);
	}
	
}
