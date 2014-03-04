package disease_comparison;

public class GraphTesting {
	
	public static void main(String [] args) {
		
		Ontology o = new Ontology();
		o.parseClassLabels("../../test-files/class-labels.txt");
		o.parseClassToClass("../../test-files/class-to-class.txt");
		o.parseIndividualToClass("../../test-files/individual-to-class.txt");
	
		o.computeAllICScores();
		
		for (String disease : o.annotation_map.keySet())
		{
			System.out.println(disease + ":");
			for (String node : o.annotation_map.get(disease))
			{
				System.out.print(node + " ");
			}
			System.out.println();
		}
		
	}
}
