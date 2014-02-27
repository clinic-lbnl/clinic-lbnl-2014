package disease_comparison;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;

import org.jgrapht.*;
import org.jgrapht.graph.*;

public class OntologyFileParser {
	
	/*
	 * parseClassLabels
	 * Arguments:
	 * 		filename: The path to the file of class labels.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier and the second holds the
	 * 			name.
	 * This function constructs a node for each line in the input file with the
	 * given identifier and name, and returns a map from the identifiers to the
	 * associated nodes.
	 * 
	 * If the file is not found, this function throws a FileNotFoundException.
	 */
	public static HashMap<String, OntologyNode> parseClassLabels(String filename) {
		
		// Keep track of all the nodes we've parsed thus far.
		HashMap<String, OntologyNode> node_map = new HashMap<String, OntologyNode>();
		
		try
		{
			// Construct a node from each line of the input file.
			for (Scanner sc = new Scanner(new File(filename)); sc.hasNext(); )
			{
				String line = sc.nextLine();
				String [] pieces = line.split("\t");
				String identity = pieces[0];
				String name = pieces[1];
				OntologyNode new_node = new OntologyNode(identity, name);
				node_map.put(identity, new_node);
			}
		}
		catch (FileNotFoundException exception)
		{
			// If we're given a bad file, let the user know.
			System.out.println("Class Label file not found at:");
			System.out.println(filename);
		}
		
		return node_map;
	}
	
	/*
	 * parseClassToClass
	 * Arguments:
	 * 		filename: The path to the file of edges.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier of the child node, and
	 * 			the second column holds the identifier of the parent node.
	 * 		node_map: The map from identifiers to nodes.
	 * This function constructs a graph with edges from the given file and
	 * nodes from the given list.
	 * 
	 * If the file is not found, this function throws a FileNotFoundException.
	 */
	public static DirectedGraph<OntologyNode, DefaultEdge>
		parseClassToClass(String filename, HashMap<String, OntologyNode> node_map) {
		
		// Construct the vertices of a graph from the list of nodes.
		DirectedGraph<OntologyNode, DefaultEdge> graph =
				new DefaultDirectedGraph<OntologyNode, DefaultEdge>(DefaultEdge.class);
		
		// Add nodes to the graph.
		for (String identity : node_map.keySet())
		{
			graph.addVertex(node_map.get(identity));			
		}
		
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
		
		return graph;
	}

	/*
	 * parseIndividualToClass
	 * Arguments:
	 * 		filename: The path to the file of annotations.
	 * 			The input file must be formatted in two tab-separated columns.
	 * 			The first column holds the identifier of the annotation, and
	 * 			the second column holds the identifier of the associated node.
	 * 		node_map: The map from identifiers to nodes.
	 * This function counts the annotations on each node.
	 * 
	 * If the file is not found, this function throws a FileNotFoundException.
	 */
	public static void parseIndividualToClass(String filename, HashMap<String, OntologyNode> node_map) {
		
		// Read in annotations
		try
		{
			// Add an annotation for each line of the input file.
			for (Scanner sc = new Scanner(new File(filename)); sc.hasNext(); )
			{
				String line = sc.nextLine();
				String [] pieces = line.split("\t");
				// We don't actually care what the annotations are called.
				String node_identity = pieces[1];
				OntologyNode node = node_map.get(node_identity);
				node.addGivenAnnotation();
			}
		}
		catch (FileNotFoundException exception)
		{
			// If we're given a bad file, let the user know.
			System.out.println("Individual To Class file not found at:");
			System.out.println(filename);
		}

	}

}
