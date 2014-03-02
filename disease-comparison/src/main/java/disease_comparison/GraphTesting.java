package disease_comparison;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.HashMap;
import java.io.FileNotFoundException;

public class GraphTesting {
	
	public static void main(String [] args) {
		
		Ontology o = new Ontology();
		o.parseClassLabels("../../test-files/class-labels.txt");
		o.parseClassToClass("../../test-files/class-to-class.txt");
		o.parseIndividualToClass("../../test-files/individual-to-class.txt");
	
		o.computeAllICScores();
		
		System.out.println(o.computeLCS("HMC:2222", "HMC:1111"));
		
	}
}
