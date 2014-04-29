/**
 * 
 */
package disease_comparison;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author slharris
 *
 */
public class OntologyTest {

	//Ontology trivial = new Ontology();
	Ontology simple = new Ontology(
			"../../test-files/simple-class-labels.txt",
			"../../test-files/simple-class-to-class.txt",
			"../../test-files/simple-individual-to-class.txt",
			"../../test-files/simple-individual-labels.txt"
		);
	//Ontology complex = new Ontology();
	
	/**
	 * Test method for {@link disease_comparison.Ontology#computeLCS(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testComputeLCS() {
		assert(simple.computeLCS("HMC:2222","HMC:3333") == "HMC:3141");
		assert(simple.computeLCS("HMC:1111","HMC:3333") == "HMC:3333");
	}

	/**
	 * Test method for {@link disease_comparison.Ontology#getGraph()}.
	 */
	@Test
	public void testGetGraph() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link disease_comparison.Ontology#getNodeMap()}.
	 */
	@Test
	public void testGetNodeMap() {
		assert(simple.getNodeMap().get("HMC:0042").getName() == "AAAAAAAAAAA!!!");
		assert(simple.getNodeMap().get("HMC:3141").getName() == "BBBBBBBBBBB!!!");
		assert(simple.getNodeMap().get("HMC:1111").getName() == "CCCCCCCCCCC!!!");
		assert(simple.getNodeMap().get("HMC:2222").getName() == "DDDDDDDDDDD!!!");
		assert(simple.getNodeMap().get("HMC:3333").getName() == "EEEEEEEEEEE!!!");
	}

	/**
	 * Test method for {@link disease_comparison.Ontology#getAnnotationMap()}.
	 */
	@Test
	public void testGetAnnotationMap() {
		assert(simple.getAnnotationMap().get("PI:3").contains("HMC:2222"));
		assert(simple.getAnnotationMap().get("PI:3").contains("HMC:3333"));
		assert(!simple.getAnnotationMap().get("PI:3").contains("HMC:3141"));
	}

	/**
	 * Test method for {@link disease_comparison.Ontology#getAnnotationNames()}.
	 */
	@Test
	public void testGetAnnotationNames() {
		assert(simple.getAnnotationNames().get("PI:1") == "Spam Deficiency");
		assert(simple.getAnnotationNames().get("PI:2") == "Sleep Deprivation");
		assert(simple.getAnnotationNames().get("PI:3") == "Pyromania");
		assert(simple.getAnnotationNames().get("PI:4") == "Social Anxiety");
		assert(simple.getAnnotationNames().get("PI:5") == "Other");
	}

	/**
	 * Test method for {@link disease_comparison.Ontology#getRoot()}.
	 */
	@Test
	public void testGetRoot() {
		assert(simple.getRoot() == "HMC:0042");
	}

	/**
	 * Test method for {@link disease_comparison.Ontology#getOptions()}.
	 */
	@Test
	public void testGetOptions() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link disease_comparison.Ontology#getImportantNodes()}.
	 */
	@Test
	public void testGetImportantNodes() {
		fail("Not yet implemented");
	}

}
