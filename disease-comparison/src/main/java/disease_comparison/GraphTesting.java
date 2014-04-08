package disease_comparison;

import java.util.Random;
import java.util.Set;

public class GraphTesting {
	
	/*
	public static String gradient(Ontology o) {
		
		String cur = o.root;
		Random r = new Random();
		
		String ans = "";
		
		while (true)
		{
			ans += o.node_map.get(cur).getICScore() + ", ";
			
			Set<String> children = o.children(cur);
			if (children.isEmpty())
			{
				break;
			}
			
			int index = r.nextInt(children.size());
			int i = 0;
			
			for (String child : children)
			{
				if (i == index)
				{
					cur = child;
					break;
				}
				i++;
			}
		}
				
		return ans;
	}
	*/
	
	public static void main(String [] args) {
		
		Ontology o = new Ontology("../../test-files/small-class-labels.txt", "../../test-files/small-class-to-class.txt",
				"../../test-files/small-individual-labels.txt", "../../test-files/small-individual-to-class.txt");
		
		long start_time = System.nanoTime();
		try
		{
			o.compareAllDiseases();
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
		
		long end_time = System.nanoTime();
		System.out.println("Comparing diseases took " + (end_time - start_time) / 1000 + " microseconds");
	}
}
