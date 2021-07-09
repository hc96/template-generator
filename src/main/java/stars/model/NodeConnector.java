package stars.model;


public class NodeConnector {
	
	public String connectorId;

	
	private StarsNode startRef =  null;
	private StarsNode targetRef = null;
	public boolean circle = false;
	public boolean matched = false;
	
	public boolean isCircle = false;
	
	public boolean branchCompared = false;

	
	public NodeConnector() {

	}
	
	public void setStartRef(StarsNode startRef) {
		this.startRef = startRef;
		
	}
	
	public void setTargetRef(StarsNode targetRef) {
		this.targetRef = targetRef;
		
	}
	
	public StarsNode getStartRef() {
		return this.startRef;
	}
	
	public StarsNode getTargetRef() {
		return this.targetRef;
	}
	

}
