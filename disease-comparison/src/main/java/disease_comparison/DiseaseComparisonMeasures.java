package disease_comparison;

import java.util.Map;
import java.util.Set;

public class DiseaseComparisonMeasures {
	
	// TODO: Add comparison measures.
	// We've only provided a test example here.
	
	/*
	 * maxIC
	 * Arguments:
	 * 		ontology: The ontology in which to compare the two diseases.
	 * 		first_identity: The identifier for the first disease.
	 * 		second_identity: The identifier for the second disease.
	 * This function computes the maxIC measure for the two given diseases.
	 */
	public static double maxIC(Ontology ontology, String first_identity, String second_identity) {
		
		// Get the relevant data from the ontology.
		Map<String, Set<String>> annotation_map = ontology.getAnnotationMap();
		Map<String, OntologyNode> node_map = ontology.getNodeMap();
		
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
				String lcs = ontology.computeLCS(first_node_identity, second_node_identity);
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
