package disease_comparison;

import java.util.HashMap;
import java.util.HashSet;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class OntologyUtilities {
	
	/*
	 * computeNodeIC
	 * Arguments:
	 * 		derived_annotations: The number of annotations below a given node.
	 * 		total_annotations: The total number of annotations to the ontology.
	 * This compute the IC score of a node based on the number of annotations.
	 */
	public static double computeNodeIC(int derived_annotations, int total_annotations) {
		// Calculate the probability that a given annotation is below the node in question.
		double probability = (double)derived_annotations / total_annotations;
		
		// Compute the negative log of the probability.
		return -Math.log(probability) / Math.log(2);
	}
	
	public static void computeGraphIC(DirectedGraph<OntologyNode, DefaultEdge> graph,
			HashMap<String, OntologyNode> node_map) {
		
		// Count the total number of annotations in the graph.
		int total_annotations = 0;
		
		// Add the annotations on each node to the total.
		for (String identity : node_map.keySet())
		{
			total_annotations += node_map.get(identity).getGivenAnnotations();
		}
		
		// Get the IC score for each node.
		for (String identity : node_map.keySet())
		{
			OntologyNode node = node_map.get(identity);
			
			// Count the annotations below the current node.
			int derived_annotations = 0;
			
			// The annotations below the current node are the annotations on each of the
			// current node's descendants.
			HashSet<String> descendants = GraphUtilities.descendants(graph, node_map, node);
			for (String descendant : descendants)
			{
				derived_annotations += node_map.get(descendant).getGivenAnnotations();
			}
			
			// Save the count of derived annotations.
			node.setDerivedAnnotations(derived_annotations);
			
			// Compute the IC score directly.
			node.setICScore(computeNodeIC(derived_annotations, total_annotations));
		}
	}
	
}
