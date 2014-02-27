package disease_comparison;

/*
 * The OntologyNode class represents a node in an ontology.
 * It holds information relevant to a specific node.
 */
public class OntologyNode {

	/* Instance Variables */
	// The name of the node.
	private String name;
	// The unique identifier.
	private String identity;
	// The number of annotations directly associated with the node.
	private int given_annotations;
	// The number of annotations associated with this node and all its descendants.
	private int derived_annotations;
	// The IC score of the node.
	private double ic_score;

	/* Constructors */
	
	public OntologyNode(String new_identity, String new_name)
	{
		// We're given the name and identifier.
		name = new_name;
		identity = new_identity;
		
		// Until annotations are processed, assume there aren't any.
		given_annotations = 0;
		derived_annotations = 0;
		
		// We can't compute the IC score until we've processed annotations.
		setICScore(-1);
	}
	
	/* Getters and Setters */
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getGivenAnnotations()
	{
		return given_annotations;
	}

	public void setGivenAnnotations(int given_annotations)
	{
		this.given_annotations = given_annotations;
	}

	public int getDerivedAnnotations()
	{
		return derived_annotations;
	}

	public void setDerivedAnnotations(int derived_annotations)
	{
		this.derived_annotations = derived_annotations;
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
	
	/* Mutators */
	
	public void addGivenAnnotation()
	{
		given_annotations++;
	}
	
	/* Pretty Print */
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
		node_string += "Derived Annotations: " + derived_annotations;
		node_string += ", ";
		node_string += "IC Score: " + ic_score;
		node_string += "}";
		return node_string;
	}
	
}
