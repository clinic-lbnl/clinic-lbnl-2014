// FIXME: This file is for practice purposes only. It should be deleted at some point.

package owltools.cli;

// Import standard Java libraries.
import java.io.*;
import java.util.*;

// Import Hadoop for the MapReduce libraries.
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

// This is the class which implements the Map phase of MapReduce for the purpose of WordCount.
public class WordCountMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable>
{	
	// Define an IntWritable so that we can pass 1 to MapReduce.
	private final static IntWritable one = new IntWritable(1);
	// Define a variable which can hold the word we're currently counting.
	private Text word = new Text();
	
	// The map method takes a line of text, breaks it into words, and passes each word with a count of 1 to the Reducer.
	public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException
	{		
		// Break a line of text into its constituent words.
		String line = value.toString();
		StringTokenizer itr = new StringTokenizer(line);
		
		// Send each word to the Reducer.
		while (itr.hasMoreTokens())
		{
			word.set(itr.nextToken());
			output.collect(word, one);
		}
	}
}
