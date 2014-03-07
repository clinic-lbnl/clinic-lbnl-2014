package disease_comparison;

public class OutputOptions {

	// Do we want to output identities?
	private boolean show_identities;
	
	// Do we want to output names?
	private boolean show_names;
	
	// Which measures do we want to show?
	private boolean show_max_ic;
	//private boolean show_iccs;
	
	/****************/
	/* Constructors */
	/****************/
	
	public OutputOptions() {
		// By default, we want to show everything.
		show_identities = true;
		show_names = true;
		show_max_ic = true;
	}
	
	/***********************/
	/* Getters and Setters */
	/***********************/
	
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
	
	

}
