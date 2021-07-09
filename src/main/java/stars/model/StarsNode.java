package stars.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarsNode {


	public Map<String, NodeConnector> incomings = new HashMap<String, NodeConnector>();
	public Map<String, NodeConnector> outgoings = new HashMap<String, NodeConnector>();
	public List<String> outgoingConnectorId = new ArrayList<String>();
	public List<String> incomingConnectorId = new ArrayList<String>();
	public boolean matched = false;
	public boolean compared = false;
	public int visited = 0;
	public int position=-1;
	public boolean isSubProcess = false;
	public String subProcessId;
	public String nodeId;
	
	
	private String nodeType = null;

	
	public StarsNode() {

	}
	public void setIncoming(String id,NodeConnector connector) {
		this.incomings.put(id, connector);
	}
	
	public void setOutgoing(String id, NodeConnector connector) {
		this.outgoings.put(id, connector);
	}
	
	public List<NodeConnector> getIncoming() {
		List<NodeConnector> connectors = new ArrayList<NodeConnector>();
		for(NodeConnector con: this.incomings.values()) {
			connectors.add(con);
		}
		
		return connectors;
	
		
	}
	
	public List<NodeConnector> getOutgoing() {
		List<NodeConnector> connectors = new ArrayList<NodeConnector>();
		for(NodeConnector con: this.outgoings.values()) {
			connectors.add(con);
		}
		return connectors;
	}
	
	public void setType(String type) {
		this.nodeType = type; 
		
	}
	
	public String getType() {
		return this.nodeType;
	}

}
