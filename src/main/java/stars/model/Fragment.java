package stars.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Fragment {
	
	public Map<String, List<String>> matchedPairs = new HashMap<String, List<String>>();
	
	public Map<String, NodeConnector> connectorMapA = new HashMap<String, NodeConnector>();
	public Map<String, NodeConnector> connectorMapB = new HashMap<String, NodeConnector>();
	
	public Map<String, StarsNode> starsNodeMapA = new HashMap<String, StarsNode>();
	
	public Map<String, Integer> successorCounterA = new HashMap<String, Integer>();
	public Map<String, Integer> successorCounterB = new HashMap<String, Integer>();
	
	public Map<String, Integer> circlePosition = new HashMap<String, Integer>();
	
	public Set<String> setA = new HashSet<String>();
	public Set<String> setB = new HashSet<String>();
	
	public Map<String, String> map_cleanup_A = new HashMap<String, String>();
	public Map<String, String> map_cleanup_B = new HashMap<String, String>();	
	
	public Map<String, String> matchedConnectors = new HashMap<String, String>();
	
	public String startingNode;
	public String startingNodeId;
	public String startingConnectorId;
	public String startingConnector;
	public int counter = -1;
	public int size = 0;
	
	public Set<String> lecturers = new HashSet<String>();
	public Map<String, Integer> applicationModels =  new HashMap<String, Integer>();
	
	public Map<String, Map<String, String>> amStarsNodes = new HashMap<String, Map<String, String>>();
	public Map<String, Map<String, String>> amNodeConnectors = new HashMap<String, Map<String, String>>();
	
	public Map<String, Map<String, String>> fragAmNodeMap = new HashMap<String, Map<String, String>>();
	public Map<String, Map<String, String>> fragAmConnectorMap = new HashMap<String, Map<String, String>>();
	
	
	public String baseModel;
	public String toCompareModel;
	
	public Fragment() {
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String startingNodeAndSize = startingNode + starsNodeMapA.size();
		
		result = prime * result + ((startingNodeAndSize == null) ? 0 : startingNodeAndSize.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
	 
		
		
		Fragment otherFragment = (Fragment) obj;
	
		Map<String, NodeConnector> otherConnectors = otherFragment.connectorMapA;
		NodeConnector startingConnector = null;
		NodeConnector startingConnectorOther = null;

		if(!connectorMapA.containsKey(startingConnectorId)) {
			startingConnector = starsNodeMapA.get(startingNodeId).getOutgoing().get(0);		
		}else {
			startingConnector = connectorMapA.get(this.startingConnectorId);
		}
		
		if(!otherFragment.connectorMapA.containsKey(otherFragment.startingConnectorId)) {
		    startingConnectorOther = otherFragment.starsNodeMapA.get(otherFragment.startingNodeId).getOutgoing().get(0);	
		}else {
			startingConnectorOther = otherFragment.connectorMapA.get(otherFragment.startingConnectorId);
		}
			
		Map <String, String> matchedConnectors = new HashMap<String, String>();
		
		if(otherConnectors.size() == connectorMapA.size()) {
			if(!startingConnector.getStartRef().getType().equals(startingConnectorOther.getStartRef().getType())) {
				return false;
			}
			
			boolean firstInput = true;
			matchedConnectors = compareSuccessors(startingConnector, startingConnectorOther, otherFragment, matchedConnectors, firstInput);
	
			if(matchedConnectors.size() == otherFragment.connectorMapA.size() && matchedConnectors.size() == this.connectorMapA.size()) {
				return true;
			}
			
		}
		
		return false;
	}
	
	private List<NodeConnector> getSuccessorConnectors(StarsNode node, Map<String, NodeConnector> connectors) {
		
		// outgoing NodeConnectors belonging to certain Application Model
		List<NodeConnector> outgoings = node.getOutgoing();
		List<NodeConnector> successorConnectors = new ArrayList<NodeConnector>();
		for(NodeConnector connector: outgoings) {
			if(connectors.containsKey(connector.connectorId)) {
				successorConnectors.add(connector);
			}
			
		}
		
		return successorConnectors;
		
	}
	

	
	
	public Map<String, String> compareSuccessors(NodeConnector connector, NodeConnector connectorOther, Fragment otherFragment, Map<String, String> matchedConnectors, boolean firstInput) {
		List <NodeConnector> connectors = new ArrayList<NodeConnector>();
		List <NodeConnector> connectorsOther =  new ArrayList<NodeConnector>();
		
		if(firstInput && (connector.getStartRef().getType().equals("andFork") || connector.getStartRef().getType().equals("orFork") || connector.getStartRef().getType().equals("decisionFork"))) {
			connectors = getSuccessorConnectors(connector.getStartRef(),connectorMapA);
			connectorsOther = getSuccessorConnectors(connectorOther.getStartRef(), otherFragment.connectorMapA);
			firstInput =  false;
			
			for(NodeConnector connectorSuccessor: connectors) {
				for(NodeConnector connectorSuccessorOther: connectorsOther) {
					matchedConnectors = compareSuccessors(connectorSuccessor, connectorSuccessorOther,otherFragment,matchedConnectors, firstInput);
					
				}
			}
		}
		
		else if(connector.getTargetRef().getType().equals(connectorOther.getTargetRef().getType())) {
			
			if(firstInput) {
				connectors = getSuccessorConnectors(connector.getStartRef(),connectorMapA);
				connectorsOther = getSuccessorConnectors(connectorOther.getStartRef(), otherFragment.connectorMapA);
				firstInput =  false;
			}else {
				connectors = getSuccessorConnectors(connector.getTargetRef(),connectorMapA);
				connectorsOther = getSuccessorConnectors(connectorOther.getTargetRef(), otherFragment.connectorMapA);
				
				if(connectors.size() == 0 && !connector.getTargetRef().getType().contains("endEvent")) {
					for(NodeConnector con: this.connectorMapA.values()) {
						if(con.getStartRef().nodeId == connector.getTargetRef().nodeId) {
							connectors.add(con);
							break;
						}
					}
				}
				
				if(connectorsOther.size() == 0 && !connectorOther.getTargetRef().getType().contains("endEvent")) {
					for(NodeConnector con: otherFragment.connectorMapA.values()) {
						if(con.getStartRef().nodeId == connectorOther.getTargetRef().nodeId) {
							connectorsOther.add(con);
							break;
						}
					}
				}
				
			
			}
			
			
			if(connector.circle && connectorOther.circle) {

				if(this.circlePosition.get(connector.connectorId) == otherFragment.circlePosition.get(connectorOther.connectorId)) {
					
					if(!matchedConnectors.containsKey(connectorOther.connectorId) && !matchedConnectors.values().contains(connector.connectorId)) {
						matchedConnectors.put(connectorOther.connectorId, connector.connectorId);
					}else {
						return matchedConnectors;
					}
					
				}
				return matchedConnectors;
				
			}
			if(connector.circle) {
				return matchedConnectors;
			}
			if(connectorOther.circle) {
				return matchedConnectors;
			}

			if(matchedConnectors.isEmpty()) {
				matchedConnectors.put(connectorOther.connectorId, connector.connectorId);
			}else if(!matchedConnectors.containsKey(connectorOther.connectorId) && !matchedConnectors.values().contains(connector.connectorId)) {
				matchedConnectors.put(connectorOther.connectorId, connector.connectorId);
			}
			
			if(connectors.size() != 0 && connectorsOther.size() != 0) {
				for(NodeConnector connectorSuccessor: connectors) {
					for(NodeConnector connectorSuccessorOther: connectorsOther) {
						matchedConnectors = compareSuccessors(connectorSuccessor, connectorSuccessorOther,otherFragment,matchedConnectors, firstInput);
						
					}
				}
			}
				
			
		}
		
		return matchedConnectors;
		
	}
	
	
	public boolean includes(Object obj) {
		if (this == obj)
			return false;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Fragment otherFragment = (Fragment) obj;
	
		Map<String, NodeConnector> otherConnectors = otherFragment.connectorMapA;
		NodeConnector startingConnector = null;
		NodeConnector startingConnectorOther = null;

		if(!connectorMapA.containsKey(startingConnectorId)) {
			startingConnector = starsNodeMapA.get(startingNodeId).getOutgoing().get(0);		
		}else {
			startingConnector = connectorMapA.get(this.startingConnectorId);
		}
		
		if(!otherFragment.connectorMapA.containsKey(otherFragment.startingConnectorId)) {
		    startingConnectorOther = otherFragment.starsNodeMapA.get(otherFragment.startingNodeId).getOutgoing().get(0);	
		}else {
			startingConnectorOther = otherFragment.connectorMapA.get(otherFragment.startingConnectorId);
		}

		Map <String, String> matchedConnectors = new HashMap<String, String>();

		
		if(otherConnectors.size() < connectorMapA.size()) {
			if(!startingConnector.getStartRef().getType().equals(startingConnectorOther.getStartRef().getType())) {
				return false;
			}

			boolean firstInput = true;
			matchedConnectors = compareSuccessors(startingConnector, startingConnectorOther, otherFragment, matchedConnectors, firstInput);

			if(matchedConnectors.size() == otherFragment.connectorMapA.size()) {
				return true;
			}
			
		}
		
		return false;
	}
	
	
	public void setNode(String id, StarsNode node) {
		starsNodeMapA.put(id, node);
	}
	
	
	public void setConnector(String id, NodeConnector connector) {
		connectorMapA.put(id, connector);
	}
	


}

