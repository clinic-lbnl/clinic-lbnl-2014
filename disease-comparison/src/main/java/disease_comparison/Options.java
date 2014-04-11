package disease_comparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Options {
	
	/***********************/
	/* Intstance Variables */
	/***********************/
	
	/* Threshold options */
	
	// Do we want to use thresholding?
	private boolean use_thresholding;
	
	// How many nodes do we want to consider?
	private int max_important_nodes;
	
	/* Output Options */
	
	// What directory do we want to use for output files?
	private String output_directory;

	// Do we want to output identities?
	private boolean show_identities;
	
	// Do we want to output names?
	private boolean show_names;
	
	// Which measures do we want to show?
	private boolean show_max_ic;
	// TODO: Add other measures.
	
	// Are all the measures symmetric?
	private boolean symmetric;
	
	/****************/
	/* Constructors */
	/****************/
	
	public Options()
	{
		// By default, we shouldn't use thresholding.
		use_thresholding = false;
		max_important_nodes = -1;
		
		// By default, we should put all our output files in a directory
		// called "output".
		output_directory = "output";
		
		// By default, we want to show everything.
		show_identities = true;
		show_names = true;
		show_max_ic = true;
	}
	
	/*****************/
	/* Configuration */
	/*****************/
	
	/*
	 * configure
	 * Arguments:
	 * 		filename: The path to the configuration file.
	 * This function reads the configuration file and sets parameters
	 * appropriately.
	 */
	public void configure(String filename)
	{
		// Check the configuration file.
		try
		{
			// Check if the user has said whether to assume all measures are
			// symmetric.
			boolean set_symmetric = false;
			
			// Check what each line of the configuration file says and set the
			// appropriate parameters.
			for (Scanner sc = new Scanner(new File(filename)); sc.hasNext(); )
			{
				// Get the next line.
				String line = sc.nextLine();
				
				// Strip out any whitespace and convert to lowercase.
				line = line.replaceAll("\\s+","");
				line.toLowerCase();
				
				// Break at the '=' character.
				String [] pieces = line.split("=");
				String parameter = pieces[0];
				String value = pieces[1];
				
				// Check which parameter we're setting.
				if (parameter.equals("use_thresholding"))
				{
					use_thresholding = Boolean.parseBoolean(value);
				}
				if (parameter.equals("max_important_nodes"))
				{
					max_important_nodes = Integer.parseInt(value);
				}
				if (parameter.equals("output_directory"))
				{
					output_directory = value;
				}
				if (parameter.equals("show_identities"))
				{
					show_identities = Boolean.parseBoolean(value);
				}
				if (parameter.equals("show_names"))
				{
					show_names = Boolean.parseBoolean(value);
				}
				if (parameter.equals("show_max_ic"))
				{
					show_max_ic = Boolean.parseBoolean(value);
				}
				if (parameter.equals("symmetric"))
				{
					symmetric = Boolean.parseBoolean(value);
					// The user has said not to use the default symmetry.
					set_symmetric = true;
				}
			}
			
			// Check if the chosen measures are all symmetric.
			if (!set_symmetric)
			{
				checkSymmetric();
			}
			
		}
		catch (FileNotFoundException exception)
		{
			// If we're given a bad file, let the user know.
			System.out.println("Configuration file not found at:");
			System.out.println(filename);
		}
	}
	
	/*
	 * checkSymmetric
	 * Arguments:
	 * 		None
	 * This function checks if all the specified measures are symmetric.
	 */
	private void checkSymmetric()
	{
		// Check if any asymmetric measure is used.
		symmetric = true;
		
		// TODO: Currently, no asymmetric measures are implemented.
		// We could handle them with
		// symmetric &= !show_iccs;
		// or something similar.
	}

	/***********************/
	/* Getters and Setters */
	/***********************/
	
	public boolean getUseThresholding() {
		return use_thresholding;
	}

	public void setUseThresholding(boolean use_thresholding) {
		this.use_thresholding = use_thresholding;
	}
	
	public int getMaxImportantNodes() {
		return max_important_nodes;
	}

	public void setMaxImportantNodes(int max_important_nodes) {
		this.max_important_nodes = max_important_nodes;
	}

	public String getOutputDirectory() {
		return output_directory;
	}

	public void setOutputDirectory(String output_directory) {
		this.output_directory = output_directory;
	}

	public boolean getShowIdentities() {
		return show_identities;
	}

	public void setShowIdentities(boolean show_identities) {
		this.show_identities = show_identities;
	}
	
	public boolean getShowNames() {
		return show_names;
	}	
	
	public void setShowNames(boolean show_names) {
		this.show_names = show_names;
	}	
	
	public boolean getShowMaxIC() {
		return show_max_ic;
	}	
	
	public void setShowMaxIC(boolean show_max_ic) {
		this.show_max_ic = show_max_ic;
	}

	public boolean getSymmetric() {
		return symmetric;
	}

	public void setSymmetric(boolean symmetric) {
		this.symmetric = symmetric;
	}

}
