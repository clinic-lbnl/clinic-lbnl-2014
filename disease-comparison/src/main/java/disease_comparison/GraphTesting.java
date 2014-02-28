package disease_comparison;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.HashMap;
import java.io.FileNotFoundException;

public class GraphTesting {
	
	public static void main(String [] args) throws FileNotFoundException {
		HashMap<String, OntologyNode> node_map =
				OntologyFileParser.parseClassLabels("../../test-files/class-labels.txt");
		DirectedGraph<OntologyNode, DefaultEdge> g =
				OntologyFileParser.parseClassToClass("../../test-files/class-to-class.txt", node_map);
		OntologyFileParser.parseIndividualToClass("../../test-files/individual-to-class.txt", node_map);
		OntologyUtilities.computeGraphIC(g, node_map);
		for (String id : node_map.keySet())
		{
			OntologyNode node = node_map.get(id);
			System.out.println(node);
		}
		
	}
}
