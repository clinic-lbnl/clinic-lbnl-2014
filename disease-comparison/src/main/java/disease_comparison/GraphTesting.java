package disease_comparison;

public class GraphTesting {
	
	public static void main(String [] args) {
		
		Ontology o = new Ontology("../../test-files/class-labels.txt", "../../test-files/class-to-class.txt",
				"../../test-files/individual-labels.txt", "../../test-files/individual-to-class.txt");

		for (String id: o.annotation_names.keySet())
		{
			System.out.println(id);
			System.out.println(o.annotation_names.get(id));
		}
		
	}
}
