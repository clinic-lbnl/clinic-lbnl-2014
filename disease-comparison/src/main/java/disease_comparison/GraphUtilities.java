package disease_comparison;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class GraphUtilities {
	
	/*
	 * children
	 * Arguments:
	 * 		graph: The graph we want to explore.
	 * 		node_map: The map between identifiers and nodes.
	 * 		node: The node whose children we want.
	 * This function returns the set of identifiers for children of the given node.
	 */
	public static HashSet<String> children(DirectedGraph<OntologyNode, DefaultEdge> graph,
			HashMap<String, OntologyNode> node_map, OntologyNode node) {
		
		// Keep track of the children.
		HashSet<String> children = new HashSet<String>();
		
		// The edges in our graph all point from child to parent, so the
		// children are along the incoming edges.
		Set<DefaultEdge> incoming = graph.incomingEdgesOf(node);
		for (DefaultEdge edge : incoming)
		{
			// The child is where the edge comes from.
			children.add(graph.getEdgeSource(edge).getIdentity());
		}
		
		return children;
	}

	/*
	 * descendants
	 * Arguments:
	 * 		graph: The graph we want to explore.
	 * 		node_map: The map between identifiers and nodes.
	 * 		node: The node whose descendants we want.
	 * This function returns the set of identifiers for descendants of the given node.
	 */
	public static HashSet<String> descendants(DirectedGraph<OntologyNode, DefaultEdge> graph,
			HashMap<String, OntologyNode> node_map, OntologyNode node) {
		
		// Keep track of the identifiers for nodes.
		HashSet<String> descendants = new HashSet<String>();
		
		// Keep track of the identifiers for nodes we still have to expand.
		LinkedList<String> unexplored = new LinkedList<String>();
		String identity = node.getIdentity();
		unexplored.add(identity);
		descendants.add(identity);
		
		// As long we still have unexpanded descendants, continue exploring.
		while (!unexplored.isEmpty())
		{
			// Choose a descendant to explore.
			String descendant = unexplored.pop();
			
			for (String child_identity : children(graph, node_map, node_map.get(descendant)))
			{
				// Check if we've already seen the child.
				if (descendants.contains(child_identity))
				{
					continue;
				}
				
				// Otherwise, we've seen it now, and we should explore it later.
				descendants.add(child_identity);
				unexplored.add(child_identity);
			}
		}
		
		return descendants;
		
	}
	
}
