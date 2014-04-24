package disease_comparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class DiseaseComparisonDriver {
	
	/*
	 * compareAllDiseases
	 * Arguments:
	 * 		class_labels: A path to the file of nodes.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier and the second holds the
	 * 			name.
	 * 		class_to_class: The path to the file of edges.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier of the child node, and
	 * 			the second column holds the identifier of the parent node.
	 * 		individual_to_class: The path to the file of annotations.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier of the annotation, and
	 * 			the second column holds the identifier of the associated node.
	 * 		individual_labels: The path to the file of annotation names.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier of the disease, and
	 * 			the second column holds the name of the disease.
	 * This computes the similarity scores for all diseases and writes them to
	 * the output directory specified in ontology.config.
	 */
	public static void compareAllDiseases(String class_labels, String class_to_class,
			String individual_to_class, String individual_labels)
			throws IOException, InterruptedException, ClassNotFoundException
	{
		// TODO: Don't use magic file path.
		// Get the options from the configuration file.
		Options options = new Options();
		options.configure("ontology.config");
		
		// Hadoop creates a directory of output files. Get the name from
		// our configuration.
		String directory_name = options.getOutputDirectory();
		
		// TODO: This might cause name conflicts. This should probably check
		// if the temporary file already exists.
		// TODO: Clean up temporary file.
		// Open a temporary file for output.
		String temp_filename = directory_name + "_temp.txt";		
		PrintWriter writer = new PrintWriter(temp_filename);
		
		// Find all the diseases.
		Set<String> disease_identities = parseIndividualLabels(individual_labels);
				
		// Compare each pair of diseases.
		for (String first_identity : disease_identities)
		{
			for (String second_identity : disease_identities)
			{
				// We don't need to compare a disease with itself.
				if (first_identity.equals(second_identity))
				{
					continue;
				}
				
				// If the measures are all symmetric, we don't need to compare
				// in both directions.
				if (options.getSymmetric())
				{
					if (first_identity.compareTo(second_identity) < 0)
					{
						continue;
					}
				}
				
				// Use a temporary output file to store each pair of diseases.
				writer.println(first_identity + '\t' + second_identity);
			}
		}
		
		// Close the temporary output file.
		writer.close();
		
		// Create and set up the Hadoop job.
		Configuration conf = new Configuration();
		conf.set("class-labels", class_labels);
		conf.set("class-to-class", class_to_class);
		conf.set("individual-to-class", individual_to_class);
		conf.set("individual-labels", individual_labels);
		Job job = new Job(conf, "Disease Comparison");

		// Our reducer outputs <Text, Text> key-value pairs.
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		// We want to map with our Mapper and reduce with our Reducer.
		job.setMapperClass(DiseaseComparisonMapper.class);
		job.setReducerClass(DiseaseComparisonReducer.class);
		
		// We read and write text files.
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		// We take our input from the temporary file and put our output in
		// the given file.
		FileInputFormat.addInputPath(job, new Path(temp_filename));
		FileOutputFormat.setOutputPath(job, new Path(directory_name));
		
		// Run MapReduce.
		job.setJarByClass(Ontology.class);
		job.waitForCompletion(true);
	}
	
	/* 
	 * processOutput
	 * Arguments:
	 * 		ontology: The ontology in which to compare the two diseases.
	 * 		first_identity: The identifier for the first disease.
	 * 		second_identity: The identifier for the second disease.
	 * This function creates a line of the output.
	 */
	private static String processOutput(Ontology ontology, String first_identity, String second_identity)
	{
		// Get the relevant information from the ontology.
		Options options = ontology.getOptions();
		Map<String, String> annotation_names = ontology.getAnnotationNames();
		
		// Calculate the similarity measures for the two diseases.
		double max_ic = DiseaseComparisonMeasures.maxIC(ontology, first_identity, second_identity);
		
		// Create the output string.	
		String line = "";
		
		// Show identities and names if the appropriate options are chosen.
		if (options.getShowIdentities())
		{
			line += first_identity + "\t";
		}
		if (options.getShowNames())
		{
			line += annotation_names.get(first_identity) + "\t";
		}
		if (options.getShowIdentities())
		{
			line += second_identity + "\t";
		}
		if (options.getShowNames())
		{
			line += annotation_names.get(second_identity) + "\t";
		}
		// Show measures if the corresponding options are chosen.
		if (options.getShowMaxIC())
		{
			line += max_ic + "\t";
		}
		
		// FIXME: Remove this.
		line += DiseaseComparisonMeasures.bestLCS(ontology, first_identity, second_identity);
		
		// Strip excess whitespace.
		line = line.trim();
		
		// Return the composite line.
		return line;
	}
	
	/****************/
	/* File Parsing */
	/****************/
	
	/*
	 * parseIndividualLabels
	 * Arguments:
	 * 		filename: The path to the file of annotations.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier of the disease, and
	 * 			the second column holds the name of the disease.
	 * This function stores the names associated with each disease.
	 */
	private static Set<String> parseIndividualLabels(String filename)
	{
		// Keep track of the identities of each disease.
		Set<String> identities = new HashSet<String>();
		
		// Read in annotations.
		try
		{
			// Add an annotation label for each line of the input file.
			for (Scanner sc = new Scanner(new File(filename)); sc.hasNext(); )
			{
				String line = sc.nextLine();
				String [] pieces = line.split("\t");
				String annotation_identity = pieces[0];
				// String annotation_name = pieces[1];
				
				// Save the identity.
				identities.add(annotation_identity);
			}
		}
		catch (FileNotFoundException exception)
		{
			// If we're given a bad file, let the user know.
			System.out.println("Individual Labels file not found at:");
			System.out.println(filename);
		}
		
		return identities;
	}
	
	/*************/
	/* MapReduce */
	/*************/

	/*
	 * The DiseaseComparisonMapper and DiseaseComparisonReducer classes allow
	 * us to use MapReduce to parallelize disease comparison.
	 */
	private static class DiseaseComparisonMapper extends Mapper<LongWritable, Text, Text, Text>
	{
		/*
		 * map
		 * Arguments:
		 * 		offset: The offset of the line to process (unused).
		 * 		line_text: A line of the input, consisting of two tab-separated
		 * 			disease identifiers.
		 * 		context: The object which lets us pass output to the reducer.
		 * This function divides the work of comparing diseases, then sends the
		 * pieces to the reducer.
		 */
		public void map(LongWritable offset, Text line_text, Context context)
				throws IOException, InterruptedException
		{
			// We can't process Text, so convert the input to a String.
			String line = line_text.toString();
			
			// Extract the diseases.
			String [] pieces = line.split("\t");
			String first_identity = pieces[0];
			String second_identity = pieces[1];
			
			// Hadoop can't handle Strings, so convert back to Text.
			Text first_text = new Text(first_identity);
			Text second_text = new Text(second_identity);
						
			// Send the pair of diseases to the reducer for processing.
			context.write(first_text, second_text);
		}
	}
	
	/*
	 * The DiseaseComparisonMapper and DiseaseComparisonReducer classes allow
	 * us to use MapReduce to parallelize disease comparison.
	 */	
	private static class DiseaseComparisonReducer extends Reducer<Text, Text, Text, Text>
	{
		// Keep track of the ontology so we don't rebuild it too many times.
		private Ontology ontology = null;
		
		/*
		 * reduce
		 * Arguments:
		 * 		first_text: The identifier of the first disease (as text).
		 * 		second_texts: An iterator through all identifiers of second
		 * 			diseases (as text).
		 * 		context: The object which lets us pass output to the driver. 
		 * This function converts pairs of disease identifiers into lines of
		 * output.
		 */
		public void reduce(Text first_text, Iterable<Text> second_texts, Context context)
				throws IOException, InterruptedException
		{
			// Construct an ontology if we don't have one already.
			if (ontology == null)
			{
				// Get the input files.
				Configuration conf = context.getConfiguration();
				String class_labels = conf.get("class-labels");
				String class_to_class = conf.get("class-to-class");
				String individual_to_class = conf.get("individual-to-class");
				String individual_labels = conf.get("individual-labels");
				
				// Build the ontology.
				ontology = new Ontology(class_labels, class_to_class, individual_to_class, individual_labels);
			}
			
			// Convert the identifier for the first disease to a string.
			String first_identity = first_text.toString();
			
			// Iterate through each possible second disease.
			for (Text second_text : second_texts)
			{
				// Convert the identifier for each second disease to a string.
				String second_identity = second_text.toString();
						
				// Get the next line of the output.
				String line_output = processOutput(ontology, first_identity, second_identity);
				
				// Pass the output to the driver, along with a dummy string.
				context.write(new Text(line_output), new Text());
			}
		}		
	}
	
	/********/
	/* Main */
	/********/
	
	public static void main(String [] args)
	{		
		try
		{
			// Compare all pairs of diseases.
			compareAllDiseases(
				// Class labels
				args[0],
				// Class to class
				args[1],
				// Individual to class
				args[2],
				// Individual labels
				args[3]
			);
		}
		catch (Exception e)
		{
			// Complain if something fails.
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		System.out.println("Disease Comparison Completed");		
	}

}
