package disease_comparison;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.HashMap;
import java.io.FileNotFoundException;

public class GraphTesting {
	
	public DirectedGraph<String, DefaultEdge> makeGraph() {
		DirectedGraph<String, DefaultEdge> g =
				new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		
		String v1 = "0042";
		String v2 = "3141";
		String v3 = "2222";
		String v4 = "3333";
		String v5 = "1111";
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);
		g.addVertex(v5);
		g.addEdge(v5, v4);
		g.addEdge(v5, v3);
		g.addEdge(v5, v1);
		g.addEdge(v4, v2);
		g.addEdge(v3, v2);
		g.addEdge(v2, v1);
		
		return g;
	}
	
	public static void main(String [] args) throws FileNotFoundException {
		HashMap<String, OntologyNode> node_map = OntologyFileParser.parseClassLabels("../../test-files/class-labels.txt");
		DirectedGraph<OntologyNode, DefaultEdge> g = OntologyFileParser.parseClassToClass("../../test-files/class-to-class.txt", node_map);
		OntologyFileParser.parseIndividualToClass("../../test-files/individual-to-class.txt", node_map);
		OntologyNode node = node_map.get("HMC:2222");
		System.out.println(node.getGivenAnnotations());
	}
}
