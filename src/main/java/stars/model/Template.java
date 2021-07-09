package stars.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Template {
	
	String templateId;
	
	List<StarsNode> starsNodesList = new ArrayList<StarsNode>();
	List<NodeConnector> nodeConnectorList = new ArrayList<NodeConnector>();
	
	Map<String, StarsNode> nodesMap = new HashMap<String, StarsNode>();
	Map<String, NodeConnector> connectorsMap = new HashMap<String, NodeConnector>();
	
	public String bpmn = null;
	public int size = 0;
	public String hint  = "";

	
	public Set<String> lecturers = new HashSet<String>();
	public Map<String, Integer> applicationModels =  new HashMap<String, Integer>();
	public Map<String, Map<String, String>> amStarsNodes = new HashMap<String, Map<String, String>>();
	public Map<String, Map<String, String>> amNodeConnectors = new HashMap<String, Map<String, String>>();
	
	public Template() {
		
	}
	
	
	public void addNode(StarsNode node) {
		this.starsNodesList.add(node);
	}
	
	public Map<String, NodeConnector> getConnectors() {
		return connectorsMap;
		
	}
	
	public void setConnector(String id, NodeConnector connector) {
		connectorsMap.put(id, connector);
	}
	
	public NodeConnector getConnector(String id) {
		return connectorsMap.get(id);
		
	}
	
	public void setNode(String id, StarsNode node) {
		nodesMap.put(id, node);
	}
	
	public Map<String, StarsNode> getNodes(){
		return nodesMap;
	}
	

}

