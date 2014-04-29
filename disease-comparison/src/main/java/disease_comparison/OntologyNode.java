package disease_comparison;

import java.util.HashSet;
import java.util.Set;

/*
 * The OntologyNode class represents a node in an ontology.
 * It holds information relevant to a specific node.
 */
public class OntologyNode {

	/**********************/
	/* Instance Variables */
	/**********************/
	
	// The name of the node.
	private String name;
	
	// The unique identifier.
	private String identity;
	
	// The diseases directly associated with the node.
	private Set<String> given_annotations;
	
	// The IC score of the node.
	private double ic_score;

	/****************/
	/* Constructors */
	/****************/
	
	public OntologyNode(String new_identity, String new_name)
	{
		// We're given the name and identifier.
		name = new_name;
		identity = new_identity;
		
		// Until annotations are processed, assume there aren't any.
		given_annotations = new HashSet<String>();
		
		// We can't compute the IC score until we've processed annotations.
		setICScore(-1);
	}
	
	/***********************/
	/* Getters and Setters */
	/***********************/
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Set<String> getGivenAnnotations()
	{
		return given_annotations;
	}

	public void setGivenAnnotations(Set<String> given_annotations)
	{
		this.given_annotations = given_annotations;
	}

	public String getIdentity()
	{
		return identity;
	}

	public void setIdentity(String identity)
	{
		this.identity = identity;
	}

	public double getICScore()
	{
		return ic_score;
	}

	public void setICScore(double d)
	{
		this.ic_score = d;
	}
	
	/************/
	/* Mutators */
	/************/
	
	public void addGivenAnnotation(String identity)
	{
		given_annotations.add(identity);
	}
	
	/****************/
	/* Pretty Print */
	/****************/
	
	public String toString()
	{
		String node_string = "";
		node_string += "{";
		node_string += "Name: " + name;
		node_string += ", ";
		node_string += "Identity: " + identity;
		node_string += ", ";
		node_string += "Given Annotations: " + given_annotations;
		node_string += ", ";
		node_string += "IC Score: " + ic_score;
		node_string += "}";
		return node_string;
	}
	
}
