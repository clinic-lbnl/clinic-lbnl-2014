package disease_comparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Ontology {

	/**********************/
	/* Instance Variables */
	/**********************/
	
	// Keep track of the actual ontology.
	private DirectedGraph<OntologyNode, DefaultEdge> graph;
	
	// It's inefficient to pass around nodes, so we pass around strings and
	// keep a table of identifiers.
	private Map<String, OntologyNode> node_map;
	
	// Keep track of all the nodes each disease is associated with.
	private Map<String, Set<String>> annotation_map;
	
	// Keep track of the names of diseases.
	private Map<String, String> annotation_names;
	
	// Keep a count of all the annotations to the ontology.
	private int total_annotations;
	
	// Keep track of the least common subsumers we've seen so we don't repeat
	// too much work.
	private Map<String, String> lcs_cache;
	
	// Keep track of the root of the graph.
	private String root;
	
	// Hold all the options for the ontology.
	private Options options;
	
	// If we are using thresholding, keep track of the nodes with the
	// highest IC scores.
	private Set<String> important_nodes;

	/****************/
	/* Constructors */
	/****************/
	
	public Ontology(String class_labels, String class_to_class,
			String individual_labels, String individual_to_class)
	{
		// Create a blank ontology.
		graph = new DefaultDirectedGraph<OntologyNode, DefaultEdge>(DefaultEdge.class);
		
		// We don't have any nodes, so our lookup table is empty.
		node_map = new HashMap<String, OntologyNode>();
				
		// We haven't yet seen any diseases.
		annotation_map = new HashMap<String, Set<String>>();
		annotation_names = new HashMap<String, String>();
		
		// We haven't yet seen any annotations.
		total_annotations = 0;
		
		// We haven't yet computed the LCS for any pairs of nodes.
		lcs_cache = new HashMap<String, String>();
		
		// We don't yet have a graph, so we don't know the root yet.
		root = "Empty Graph";
		
		// We haven't yet read in parameters, so just pick values.
		options = new Options();
				
		// We haven't yet seen nodes, so we don't know which are important.
		important_nodes = new HashSet<String>();
		
		// TODO: Don't use magic file path.
		// Get parameters from the configuration file.
		options.configure("./ontology.config");
		
		// Parse the input files and fill in the ontology appropriately.
		parseClassLabels(class_labels);
		parseClassToClass(class_to_class);
		parseIndividualToClass(individual_to_class);
		parseIndividualLabels(individual_labels);
				
		findRoot();
		
		// Get the IC score for every node.
		computeAllICScores();
		
		// If we're using thresholding, find the important nodes.
		if (options.getUseThresholding())
		{
			findImportantNodes();
		}
	}
	
	/****************/
	/* File Parsing */
	/****************/
	
	/*
	 * parseClassLabels
	 * Arguments:
	 * 		filename: The path to the file of class labels.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier and the second holds the
	 * 			name.
	 * This function constructs a node for each line in the input file with the
	 * given identifier and name.
	 */
	private void parseClassLabels(String filename)
	{	
		// Add vertices to the graph.
		try
		{
			// Construct a node from each line of the input file.
			for (Scanner sc = new Scanner(new File(filename)); sc.hasNext(); )
			{
				// Build the node.
				String line = sc.nextLine();
				String [] pieces = line.split("\t");
				String identity = pieces[0];
				String name = pieces[1];
				OntologyNode new_node = new OntologyNode(identity, name);
								
				// Add the node to the graph.
				graph.addVertex(new_node);
				
				// Add the node to our lookup table.
				node_map.put(identity, new_node);
			}
		}
		catch (FileNotFoundException exception)
		{
			// If we're given a bad file, let the user know.
			System.out.println("Class Label file not found at:");
			System.out.println(filename);
		}
	}
	
	/*
	 * parseClassToClass
	 * Arguments:
	 * 		filename: The path to the file of edges.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier of the child node, and
	 * 			the second column holds the identifier of the parent node.
	 * This function constructs the edges of the graph.
	 */
	private void parseClassToClass(String filename)
	{
		// Add edges to the graph.
		try
		{
			// Construct an edge from each line of the input file.
			for (Scanner sc = new Scanner(new File(filename)); sc.hasNext(); )
			{
				String line = sc.nextLine();
				String [] pieces = line.split("\t");
				String child_identity = pieces[0];
				String parent_identity = pieces[1];
				
				// TODO: Handle unnamed nodes better.
				// If either node does not exist, create it and add it to the graph.
				if (!graph.containsVertex(node_map.get(child_identity)))
				{
					OntologyNode child_node = new OntologyNode(child_identity, "unnamed");
					node_map.put(child_identity, child_node);
					graph.addVertex(child_node);
				}
				if (!graph.containsVertex(node_map.get(parent_identity)))
				{
					OntologyNode parent_node = new OntologyNode(parent_identity, "unnamed");
					node_map.put(parent_identity, parent_node);
					graph.addVertex(parent_node);
				}
				
				// Find the nodes corresponding to each of the two identifiers
				// and build the edge between them.
				graph.addEdge(node_map.get(child_identity), node_map.get(parent_identity));
			}
		}
		catch (FileNotFoundException exception)
		{
			// If we're given a bad file, let the user know.
			System.out.println("Class To Class file not found at:");
			System.out.println(filename);
		}
	}
	
	/*
	 * parseIndividualToClass
	 * Arguments:
	 * 		filename: The path to the file of annotations.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier of the annotation, and
	 * 			the second column holds the identifier of the associated node.
	 * This function counts the annotations on each node.
	 */
	private void parseIndividualToClass(String filename)
	{
		// Read in annotations.
		try
		{
			// Add an annotation for each line of the input file.
			for (Scanner sc = new Scanner(new File(filename)); sc.hasNext(); )
			{
				String line = sc.nextLine();
				String [] pieces = line.split("\t");
				String annotation_identity = pieces[0];
				String node_identity = pieces[1];
				OntologyNode node = node_map.get(node_identity);
				
				// Add an annotation to the node count.
				node.addGivenAnnotation();
				
				// Add an annotation to the ontology count.
				total_annotations++;
				
				// Add an annotation to the disease.
				if (annotation_map.containsKey(annotation_identity))
				{
					annotation_map.get(annotation_identity).add(node_identity);
				}
				else
				{
					// If we haven't seen this disease before, build a new set
					// of node identifiers to map this disease to.
					Set<String> annotation_nodes = new HashSet<String>();
					annotation_nodes.add(node_identity);
					annotation_map.put(annotation_identity, annotation_nodes);
				}
			}
		}
		catch (FileNotFoundException exception)
		{
			// If we're given a bad file, let the user know.
			System.out.println("Individual To Class file not found at:");
			System.out.println(filename);
		}
	}
	
	/*
	 * parseIndividualLabels
	 * Arguments:
	 * 		filename: The path to the file of annotations.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier of the disease, and
	 * 			the second column holds the name of the disease.
	 * This function stores the names associated with each disease.
	 */
	private void parseIndividualLabels(String filename)
	{
		// Read in annotations.
		try
		{
			// Add an annotation label for each line of the input file.
			for (Scanner sc = new Scanner(new File(filename)); sc.hasNext(); )
			{
				String line = sc.nextLine();
				String [] pieces = line.split("\t");
				String annotation_identity = pieces[0];
				String annotation_name = pieces[1];
				
				// Save the name.
				annotation_names.put(annotation_identity, annotation_name);
			}
		}
		catch (FileNotFoundException exception)
		{
			// If we're given a bad file, let the user know.
			System.out.println("Individual Labels file not found at:");
			System.out.println(filename);
		}
	}
	
	/********************/
	/* Graph Operations */
	/********************/
	
	/*
	 * directedNeighbors
	 * Arguments:
	 * 		identity: The identifier of the node whose neighbors we want.
	 * 		out_edges: A boolean specifying whether we want to follow outgoing
	 * 			or incoming edges.
	 * This function returns the set of identifiers for neighbors of the given
	 * node in the correct direction.
	 * 
	 * This is a helper function for children and parents.
	 */
	private Set<String> directedNeighbors(String identity, boolean out_edges)
	{
		// Look up the node.
		OntologyNode node = node_map.get(identity);
		
		// Keep track of the neighbors.
		HashSet<String> neighbors = new HashSet<String>();
		
		// Find all the edges in the correct direction.
		Set<DefaultEdge> edges;
		if (out_edges)
		{
			edges = graph.outgoingEdgesOf(node);
		}
		else
		{
			edges = graph.incomingEdgesOf(node);
		}
		
		// Follow each edge to get the neighbor.
		for (DefaultEdge edge : edges)
		{
			if (out_edges)
			{
				// If we're looking at outgoing edges, we want the node at the
				// end of each edge.
				neighbors.add(graph.getEdgeTarget(edge).getIdentity());
			}
			else
			{
				// If we're looking at incoming edges, we want the node at the
				// start of each edge.
				neighbors.add(graph.getEdgeSource(edge).getIdentity());
			}
		}
		
		return neighbors;
	}
	
	/*
	 * children
	 * Arguments:
	 * 		identity: The identifier of the node whose children we want.
	 * This function returns the set of identifiers for children of the given
	 * node.
	 */
	@SuppressWarnings("unused")
	private Set<String> children(String identity)
	{
		// Call the helper function.
		return directedNeighbors(identity, false);
		
	}
	
	/*
	 * parents
	 * Arguments:
	 * 		identity: The identifier of the node whose parents we want.
	 * This function returns the set of identifiers for parents of the given
	 * node.
	 */
	private Set<String> parents(String identity)
	{
		// Call the helper function.
		return directedNeighbors(identity, true);
		
	}
	
	/*
	 * directedRelatives
	 * Arguments:
	 * 		identity: The identifier of the node whose relatives we want.
	 *      out_edges: A boolean specifying whether we want to follow outgoing
	 * 			or incoming edges.
	 * This function returns the set of identifiers for relatives of the given
	 * node in the correct direction.
	 * 
	 * This is a helper function for descendants and subsumers.
	 */
	private Set<String> directedRelatives(String identity, boolean out_edges)
	{
		// Keep track of the identifiers for nodes.
		HashSet<String> relatives = new HashSet<String>();
		
		// Keep track of the identifiers for nodes we still have to expand.
		LinkedList<String> unexplored = new LinkedList<String>();

		unexplored.add(identity);
		relatives.add(identity);
		
		// As long we still have unexpanded descendants, continue exploring.
		while (!unexplored.isEmpty())
		{
			// Choose a descendant to explore.
			String relative = unexplored.pop();
			
			for (String neighbor_identity : directedNeighbors(relative, out_edges))
			{
				// Check if we've already seen the child.
				if (relatives.contains(neighbor_identity))
				{
					continue;
				}
				
				// Otherwise, we've seen it now, and we should explore it later.
				relatives.add(neighbor_identity);
				unexplored.add(neighbor_identity);
			}
		}
		
		return relatives;
		
	}
	
	/*
	 * descendants
	 * Arguments:
	 * 		identity: The identifier of the node whose descendants we want.
	 * This function returns the set of identifiers for descendants of the given
	 * node.
	 */
	private Set<String> descendants(String identity)
	{
		// Call the helper function.
		return directedRelatives(identity, false);		
	}
	
	/*
	 * findRoot
	 * Arguments:
	 * 		None
	 * This function finds the root of the ontology and stores it.
	 */
	private void findRoot()
	{		
		// Keep track of our possible root and nodes to consider.
		String possible_root = "Empty Graph";
		Set<String> node_set = node_map.keySet();
		
		// So long as the possible root has ancestors, we're not done. 
		while (!node_set.isEmpty())
		{
			// If we haven't found the root yet, the root is an ancestor of
			// our possible root, so we try again with the parents.
			possible_root = node_set.iterator().next();
			node_set = parents(possible_root);
		}
		
		// Our possible root no longer has parents, so it is actually the root. 
		root = possible_root;		
	}

	/*
	 * subsumers
	 * Arguments:
	 * 		identity: The identifier of the node whose subsumers we want.
	 * This function returns the set of identifiers for subsumers of the given
	 * node.
	 */
	private Set<String> subsumers(String identity)
	{
		// Call the helper function.
		return directedRelatives(identity, true);
	}
	
	/***********************/
	/* Ontology Operations */
	/***********************/
	
	/*
	 * computeIC
	 * Arguments:
	 * 		identity: The identifier of the node whose IC score we want.
	 * This function computes the IC score for the given node and stores it
	 * in the node.
	 * 
	 * This is a helper function for computeAllICScores.
	 */
	private void computeIC(String identity)
	{
		// Look up the node.
		OntologyNode node = node_map.get(identity); 
		
		// Keep track of the annotations we've seen.
		int derived_annotations = 0;
		
		// Find how many annotations are on each descendant.
		for (String descendant_identity : descendants(identity))
		{
			derived_annotations += node_map.get(descendant_identity).getGivenAnnotations();
		}
		
		// Compute the IC score from the number of annotations.
		double probability = (double)derived_annotations / total_annotations;
				
		// Compute the negative log of the probability.
		node.setICScore(-Math.log(probability) / Math.log(2));
	}
	
	/*
	 * computeAllICScores
	 * Arguments:
	 * 		None
	 * This function computes the IC score for every node in the ontology.
	 */
	private void computeAllICScores()
	{
		// Compute the IC score for each node.
		for (String identity : node_map.keySet())
		{
			computeIC(identity);
		}
	}
	
	/*
	 * findImportantNodes
	 * Arguments:
	 * 		None
	 * This function finds up to max_important_nodes nodes with sufficiently
	 * high IC scores and stores them in important_nodes.
	 */
	private void findImportantNodes()
	{
		// We use a modified version of quickselect to get the important nodes.
		
		// Keep track of all nodes still being considered.
		Set<String> node_set = new HashSet<String>();
		node_set.addAll(node_map.keySet());
		
		// Keep track of the important nodes.
		int available_space = options.getMaxImportantNodes();
		
		// Keep track of those elements with IC score larger than the pivot,
		// those with IC score equal to the pivot, and those with IC score
		// less than the pivot.
		Set<String> large = new HashSet<String>();
		Set<String> equal = new HashSet<String>();
		Set<String> small = new HashSet<String>();
		
		while (node_set.size() > available_space)
		{
			// Keep a pivot for partitioning.
			String pivot = node_set.iterator().next();
			double pivot_ic = node_map.get(pivot).getICScore();
			
			// Partition the nodes by IC score.
			for (String current_node : node_set)
			{				
				// Check each node against the pivot and partition accordingly.
				double current_ic = node_map.get(current_node).getICScore();
				if (current_ic > pivot_ic)
				{
					large.add(current_node);
				}
				else if (current_ic < pivot_ic)
				{
					small.add(current_node);
				}
				else
				{
					equal.add(current_node);
				}
			}
			
			// If we've got more large elements than nodes we want to keep,
			// throw out all the small nodes and recurse on the large ones.
			if (large.size() > available_space)
			{
				node_set.retainAll(large);
				large.clear();
				equal.clear();
				small.clear();
				continue;
			}
			
			// Otherwise, all the large nodes are important.
			important_nodes.addAll(large);
			available_space -= large.size();
			
			// If we've got more elements with equal IC scores than available
			// space, throw them all out and say we're done.
			if (equal.size() > available_space)
			{
				node_set.clear();
				break;
			}
			
			// Otherwise, the equal node are important too.
			important_nodes.addAll(equal);
			available_space -= equal.size();
			
			// Recurse on the small nodes.
			node_set.retainAll(small);
			large.clear();
			equal.clear();
			small.clear();
		}
		
		// When we have space for all the remaining nodes, they're all important.
		important_nodes.addAll(node_set);
	}
	
	/*
	 * computeLCS
	 * Arguments:
	 * 		first_identity: The identifier for the first node.
	 * 		second_identity: The identifier for the second node.
	 * This function computes the least common subsumer for two nodes.
	 */
	private String computeLCS(String first_identity, String second_identity)
	{
		// If we've already computed the LCS for this pair, check the cache
		// to get it.
		String combined_identity = first_identity + "\t" + second_identity;
		if (lcs_cache.containsKey(combined_identity))
		{
			return lcs_cache.get(combined_identity);
		}
		
		// If we're using thresholding, make sure both nodes are sufficiently
		// important.
		if (options.getUseThresholding())
		{
			// If we don't have important nodes, assume the root is the LCS.
			if (! (important_nodes.contains(first_identity)	&&
					important_nodes.contains(second_identity)))
			{
				return root;
			}
		}
		
		// Get all the subsumers for both of the given nodes.
		Set<String> first_subsumers = subsumers(first_identity);
		Set<String> second_subsumers = subsumers(second_identity);
		
		// Find the common subsumers of the two nodes.
		Set<String> common_subsumers = new HashSet<String>(first_subsumers);
		common_subsumers.retainAll(second_subsumers);
		
		// Keep track of the best common subsumer.
		String best_subsumer = "NO SUBSUMERS";
		double best_ic = -1;
		
		// Check each common subsumer against the best so far.
		for (String subsumer : common_subsumers)
		{
			double subsumer_ic = node_map.get(subsumer).getICScore(); 
			if (subsumer_ic > best_ic)
			{
				best_subsumer = subsumer;
				best_ic = subsumer_ic;
			}
		}
		
		// Cache the result.
		lcs_cache.put(combined_identity, best_subsumer);
		
		// Cache the result in the opposite order so we don't compute that.
		String other_combined_identity = second_identity + "\t" + first_identity;
		lcs_cache.put(other_combined_identity, best_subsumer);
		
		// Return the identifier of the least common subsumer.
		return best_subsumer;
	}
	
	/*
	 * compareAllDiseases
	 * Arguments:
	 * 		directory_name: The path of the output directory.
	 * 		options: Which information to output.
	 * This computes the similarity scores for all diseases and write them to
	 * the given output file.
	 */
	public void compareAllDiseases()
			throws IOException, InterruptedException, ClassNotFoundException
	{		
		// TODO: This might cause name conflicts. This should probably check
		// if the temporary file already exists.
		// TODO: Clean up temporary file.
		// Open a temporary file for output.
		String directory_name = options.getOutputDirectory();
		String temp_filename = directory_name + "_temp.txt";		
		PrintWriter writer = new PrintWriter(temp_filename);
		
		// Find all the diseases.
		Set<String> disease_identities = annotation_map.keySet();
				
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
				
				// Use a temporary output file to store each pair of diseases.
				writer.println(first_identity + '\t' + second_identity);
			}
		}
		
		// Close the temporary output file.
		writer.close();
		
		// FIXME: Comment this
		Configuration conf = new Configuration();
		conf.set("class-labels", "../../test-files/small-class-labels.txt");
		conf.set("class-to-class", "../../test-files/small-class-to-class.txt");
		conf.set("individual-to-class", "../../test-files/small-individual-labels.txt");
		conf.set("individual-labels", "../../test-files/small-individual-to-class.txt");
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
	 * 		options: Which information to output.
	 * 		first_identity: The identifier for the first disease.
	 * 		second_identity: The identifier for the second disease.
	 * This function creates a line of the output.
	 */
	private String processOutput(String first_identity, String second_identity)
	{
		// Calculate the maxIC for the first and second identity.
		double max_ic = maxIC(first_identity, second_identity);
		
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
		
		// Return the composite line.
		return line;
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
				String line_output = ontology.processOutput(first_identity, second_identity);
				
				// Pass the output to the driver, along with a dummy string.
				context.write(new Text(line_output), new Text());
			}
		}		
	}
	
	
	/***********************/
	/* Comparison Measures */
	/***********************/
	
	// TODO: Add comparison measures.
	// We've only provided a test example here.
	
	/*
	 * maxIC
	 * Arguments:
	 * 		first_identity: The identifier for the first disease.
	 * 		second_identity: The identifier for the second disease.
	 * This function computes the maxIC measure for the two given diseases.
	 */
	private double maxIC(String first_identity, String second_identity) {
		
		// Find all the nodes associated with each disease.
		Set<String> first_nodes = annotation_map.get(first_identity);
		Set<String> second_nodes = annotation_map.get(second_identity);

		// Keep track of the best IC score we've seen.
		double best_ic = -1;
		
		// Look at the nodes associated with each annotation.
		for (String first_node_identity : first_nodes)
		{
			for (String second_node_identity : second_nodes)
			{
				// Find the least common subsumer for the two nodes.
				String lcs = computeLCS(first_node_identity, second_node_identity);
				double lcs_ic = node_map.get(lcs).getICScore();
				
				// If the LCS has a better IC score, update our best.
				if (lcs_ic > best_ic)
				{
					best_ic = lcs_ic;
				}
			}
		}
		
		// Return the best IC score.
		return best_ic;
		
	}
	
}
