// FIXME: This file is for practice purposes only. It should be deleted at some point.

package owltools.cli;

// Import standard Java libraries.
import java.io.*;
import java.util.*;

// Import Hadoop for the MapReduce libraries.
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

// This is the class which implements the Reduce phase of MapReduce for the purpose of WordCount.
public class WordCountReducer extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable>
{
	// The reduce method counts the words passed in and sends the result to the Driver.
	public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException
	{
		// Add 1 for each word in our input, that is, count the number of words.
		int sum = 0;
		while (values.hasNext())
		{
			sum += values.next().get();			
		}
		
		// Send the count to the Driver.
		output.collect(key, new IntWritable(sum));
	}	
}