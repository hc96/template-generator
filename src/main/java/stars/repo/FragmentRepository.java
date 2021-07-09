package stars.repo;

import stars.model.Fragment;
import stars.model.NodeConnector;
import stars.model.StarsNode;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FragmentRepository {
	
	
	private static volatile FragmentRepository fragmentRepository = new FragmentRepository();
	private Map<Fragment, Fragment> fragments =  new HashMap<Fragment, Fragment>();
	
    private FragmentRepository() {
		
	}
	
	
	public static FragmentRepository getInstance() {
		return fragmentRepository;
	}
	
	private void checkStartandEnd(Fragment fragment) {

		for(NodeConnector con: fragment.connectorMapA.values()) {
			
			if(con.connectorId.equals(fragment.startingConnectorId)) {
				fragment.amStarsNodes.get(fragment.baseModel).put(con.getStartRef().nodeId, "source");	
				fragment.amStarsNodes.get(fragment.toCompareModel).put(fragment.connectorMapB.get(fragment.matchedPairs.get(con.connectorId).get(0)).getStartRef().nodeId, "source");
				break;
			}		
			
		}
			
		
	}
	
	
	public void addFragmentToRepository(Set<Fragment> fragmentCollection) {
		
		
		for(Fragment fragment: fragmentCollection) {
			
			
			
			checkStartandEnd(fragment);	

			Map<String, StarsNode> tmpStarsNode = new HashMap<String, StarsNode>();

			
			for(StarsNode node: fragment.starsNodeMapA.values()) {
				
				// construction of startEvent
				// the type of the startingNode is not startEvent
				if(node.nodeId.equals(fragment.startingNodeId) && !node.getType().equals("startEvent")) {
					StarsNode startNode = new StarsNode();
					startNode.nodeId = "StartEvent_1";
					startNode.setType("startEvent");
					tmpStarsNode.put(startNode.nodeId, startNode);
					
					NodeConnector startConnector = new NodeConnector();
					String id = UUID.randomUUID().toString();
					startConnector.connectorId = id;
					startConnector.setStartRef(startNode);
					startConnector.setTargetRef(node);
					startNode.setOutgoing(startConnector.connectorId, startConnector);
					fragment.setConnector(startConnector.connectorId, startConnector);
					
					fragment.startingNodeId = startNode.nodeId;
					fragment.startingNode = startNode.getType();
					fragment.startingConnectorId =  startConnector.connectorId;
					
					
					
				}
				
				// construction of endEvents
				// this node is not endEvent and none of its outgoing connectors exists in connectorMapA
				boolean outgoing = false;
				if(!node.getType().contains("endEvent")) {
					for(NodeConnector con: node.getOutgoing()) {
						if(fragment.connectorMapA.containsKey(con.connectorId)) {
							outgoing = true;
							break;
						}
					}
					
					if(!outgoing) {
						StarsNode endNode = new StarsNode();
						String endNodeId = UUID.randomUUID().toString();
						endNode.nodeId = "EndEvent_" + endNodeId;
						endNode.setType("endEvent");
						tmpStarsNode.put(endNode.nodeId, endNode);

						NodeConnector endConnector = new NodeConnector();
						String endConnectorId = UUID.randomUUID().toString();
						endConnector.connectorId = endConnectorId;
						endConnector.setTargetRef(endNode);
						endConnector.setStartRef(node);
						endNode.setIncoming(endConnector.connectorId, endConnector);
						fragment.setConnector(endConnector.connectorId, endConnector);
						fragment.amStarsNodes.get(fragment.baseModel).put(node.nodeId, endNode.nodeId);
						
						String toCompare = null;
						
						for(NodeConnector con_base: node.getIncoming()) {
							if(fragment.connectorMapA.containsKey(con_base.connectorId)) {
								 toCompare = fragment.connectorMapB.get(fragment.matchedPairs.get(con_base.connectorId).get(0)).getTargetRef().nodeId;					 
								 break;
							}
						}

						fragment.amStarsNodes.get(fragment.toCompareModel).put(toCompare, endNode.nodeId);			


						
					}
				}
				
			}
			
			
			fragment.starsNodeMapA.putAll(tmpStarsNode);
			
			
			// add fragment to the Fragment Repository
			if(fragments.isEmpty()) {
				
				for(String conA: fragment.matchedPairs.keySet()) {
					
					Map<String, String> consB = new HashMap<String, String>();
					consB.put(fragment.toCompareModel,fragment.matchedPairs.get(conA).get(0));
					consB.put(fragment.baseModel,conA);
					fragment.fragAmConnectorMap.put(conA, consB);
					
				
					// nodes	
					String startNode = fragment.connectorMapA.get(conA).getStartRef().nodeId;
					String targetNode = fragment.connectorMapA.get(conA).getTargetRef().nodeId;
					
					String startNodeB = fragment.connectorMapB.get(fragment.matchedPairs.get(conA).get(0)).getStartRef().nodeId;
					String targetNodeB = fragment.connectorMapB.get(fragment.matchedPairs.get(conA).get(0)).getTargetRef().nodeId;
					
					
					if(!fragment.fragAmNodeMap.containsKey(startNode)) {
						Map<String, String> startNodesB = new HashMap<String, String>();
						startNodesB.put(fragment.baseModel, startNode);
						startNodesB.put(fragment.toCompareModel, startNodeB);
						fragment.fragAmNodeMap.put(startNode, startNodesB);
					}
					
					if(!fragment.fragAmNodeMap.containsKey(targetNode)) {
						Map<String, String> targetNodesB = new HashMap<String, String>();
						targetNodesB.put(fragment.baseModel, targetNode);
						targetNodesB.put(fragment.toCompareModel, targetNodeB);
						fragment.fragAmNodeMap.put(targetNode, targetNodesB);
					}
					
					
					
				}
				
				fragments.put(fragment, fragment);
			}else if(fragments.containsKey(fragment)) {
				
			Set<String> lecturers1 = fragment.lecturers; 
			Map<String, Integer> applicationModels1 = fragment.applicationModels;
			
			for(String lecturer: lecturers1) {
				if(!fragments.get(fragment).lecturers.contains(lecturer)) {
					fragments.get(fragment).lecturers.add(lecturer);
				}
			}
			
			for(String am: applicationModels1.keySet()) {
				if(!fragments.get(fragment).applicationModels.containsKey(am)) {
					fragments.get(fragment).applicationModels.put(am, applicationModels1.get(am));

					fragments.get(fragment).amNodeConnectors.put(am, fragment.amNodeConnectors.get(am));
					fragments.get(fragment).amStarsNodes.put(am, fragment.amStarsNodes.get(am));

					setMappings(fragments.get(fragment), fragment, am);
					
				}
				
			}
				
				
			}else {
				
				for(String conA: fragment.matchedPairs.keySet()) {
					
					Map<String, String> consB = new HashMap<String, String>();
					consB.put(fragment.toCompareModel,fragment.matchedPairs.get(conA).get(0));
					consB.put(fragment.baseModel,conA);
					fragment.fragAmConnectorMap.put(conA, consB);
					

					// nodes	
					String startNode = fragment.connectorMapA.get(conA).getStartRef().nodeId;
					String targetNode = fragment.connectorMapA.get(conA).getTargetRef().nodeId;
					
					String startNodeB = fragment.connectorMapB.get(fragment.matchedPairs.get(conA).get(0)).getStartRef().nodeId;
					String targetNodeB = fragment.connectorMapB.get(fragment.matchedPairs.get(conA).get(0)).getTargetRef().nodeId;
					
					if(!fragment.fragAmNodeMap.containsKey(startNode)) {
						Map<String, String> startNodesB = new HashMap<String, String>();
						startNodesB.put(fragment.baseModel, startNode);
						startNodesB.put(fragment.toCompareModel, startNodeB);
						fragment.fragAmNodeMap.put(startNode, startNodesB);
					}
					
					if(!fragment.fragAmNodeMap.containsKey(targetNode)) {
						Map<String, String> targetNodesB = new HashMap<String, String>();
						targetNodesB.put(fragment.baseModel, targetNode);
						targetNodesB.put(fragment.toCompareModel, targetNodeB);
						fragment.fragAmNodeMap.put(targetNode, targetNodesB);
					}
				}
				
				fragments.put(fragment, fragment);
				
			}
			
		}
		
	}
	
	
	public void setMappings(Fragment fragmentRepo, Fragment fragmentNew, String amId) {
		if(amId.equals(fragmentNew.baseModel)) {
			NodeConnector startingConnectorRepo = fragmentRepo.connectorMapA.get(fragmentRepo.startingConnector);
			NodeConnector startingConnectorNew = fragmentNew.connectorMapA.get(fragmentNew.startingConnector);
			Map <String, String> matchedConnectors = new HashMap<String, String>();
			
			
			compareSuccessors(startingConnectorRepo, startingConnectorNew, fragmentRepo, fragmentNew, "baseModel", amId, true,matchedConnectors);
					
		}else if(amId.equals(fragmentNew.toCompareModel)) {
			String startingConnectorIdToCompare = fragmentNew.matchedPairs.get(fragmentNew.startingConnector).get(0);
			NodeConnector startingConnectorRepo = fragmentRepo.connectorMapA.get(fragmentRepo.startingConnector);
			NodeConnector startingConnectorNew = fragmentNew.connectorMapB.get(startingConnectorIdToCompare);
			Map <String, String> matchedConnectors = new HashMap<String, String>();
			
			compareSuccessors(startingConnectorRepo, startingConnectorNew, fragmentRepo, fragmentNew, "toCompareModel", amId, true,matchedConnectors);

			
			
		}else {
			System.out.println("am: Somethign Wring in Fragment Repository.");
		}
	}
	
	// get mappings
	public Map<String, String> compareSuccessors(NodeConnector connector, NodeConnector connectorOther,Fragment fragmentRepo, Fragment fragmentNew, String model, String amId, boolean firstInput, Map<String, String> matchedConnectors) {
		List <NodeConnector> connectors = new ArrayList<NodeConnector>();
		List <NodeConnector> connectorsOther =  new ArrayList<NodeConnector>();
		
		if(firstInput && (connector.getStartRef().getType().equals("andFork") || connector.getStartRef().getType().equals("orFork") || connector.getStartRef().getType().equals("decisionFork"))) {
			connectors = getSuccessorConnectors(connector.getStartRef(),fragmentRepo.connectorMapA);
			if(model.contains("baseModel")) {
				connectorsOther = getSuccessorConnectors(connectorOther.getStartRef(), fragmentNew.connectorMapA);
			}else {
				connectorsOther = getSuccessorConnectors(connectorOther.getStartRef(), fragmentNew.connectorMapB);
			}
			
			firstInput =  false;
			
		
				for(NodeConnector connectorSuccessor: connectors) {
					for(NodeConnector connectorSuccessorOther: connectorsOther) {
						matchedConnectors = compareSuccessors(connectorSuccessor, connectorSuccessorOther, fragmentRepo,fragmentNew,model,amId, firstInput,matchedConnectors);
						
					}
				}
		
			
		}
		
		else if(connector.getTargetRef().getType().equals(connectorOther.getTargetRef().getType())) {
			
			if(firstInput) {
				connectors = getSuccessorConnectors(connector.getStartRef(),fragmentRepo.connectorMapA);
				
				if(model.contains("baseModel")) {
					connectorsOther = getSuccessorConnectors(connectorOther.getStartRef(), fragmentNew.connectorMapA);
				}else {
					connectorsOther = getSuccessorConnectors(connectorOther.getStartRef(), fragmentNew.connectorMapB);
				}
				firstInput =  false;
			}else {
				connectors = getSuccessorConnectors(connector.getTargetRef(),fragmentRepo.connectorMapA);
				
				if(model.contains("baseModel")) {
					connectorsOther = getSuccessorConnectors(connectorOther.getTargetRef(), fragmentNew.connectorMapA);
				}else {
					connectorsOther = getSuccessorConnectors(connectorOther.getTargetRef(), fragmentNew.connectorMapB);
				}
				
			
			}
			
			
			if(connector.circle && connectorOther.circle) {

				if(fragmentRepo.circlePosition.get(connector.connectorId) == fragmentNew.circlePosition.get(connectorOther.connectorId)) {

					if(fragmentRepo.connectorMapA.containsKey(connector.connectorId)) {
						if(!fragmentRepo.fragAmConnectorMap.keySet().contains(connector.connectorId)) {
							
							Map<String, String> amConnectors = new HashMap<String, String>();
							amConnectors.put(amId,connectorOther.connectorId);
							amConnectors.put(fragmentRepo.baseModel,connector.connectorId);
							fragmentRepo.fragAmConnectorMap.put(connector.connectorId, amConnectors);
							matchedConnectors.put(connectorOther.connectorId, connector.connectorId);
							addNode(connector, connectorOther, fragmentRepo, amId);
						}else {
							
							if(!fragmentRepo.fragAmConnectorMap.get(connector.connectorId).containsKey(amId) && !matchedConnectors.containsKey(connectorOther.connectorId) && !matchedConnectors.values().contains(connector.connectorId)) {
								fragmentRepo.fragAmConnectorMap.get(connector.connectorId).put(amId, connectorOther.connectorId);
								matchedConnectors.put(connectorOther.connectorId, connector.connectorId);
								addNode(connector, connectorOther, fragmentRepo, amId);
							}
						}
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
			
			if(fragmentRepo.connectorMapA.containsKey(connector.connectorId)) {
				
				if(fragmentRepo.fragAmConnectorMap.isEmpty()) {
					Map<String, String> amConnectors = new HashMap<String, String>();
					amConnectors.put(amId,connectorOther.connectorId);
					amConnectors.put(fragmentRepo.baseModel,connector.connectorId);
					fragmentRepo.fragAmConnectorMap.put(connector.connectorId, amConnectors);
					matchedConnectors.put(connectorOther.connectorId, connector.connectorId);

					addNode(connector, connectorOther, fragmentRepo, amId);
					
				}else if(!fragmentRepo.fragAmConnectorMap.keySet().contains(connector.connectorId)) {
					
					Map<String, String> amConnectors = new HashMap<String, String>();
					amConnectors.put(amId,connectorOther.connectorId);
					amConnectors.put(fragmentRepo.baseModel,connector.connectorId);
					fragmentRepo.fragAmConnectorMap.put(connector.connectorId, amConnectors);
					matchedConnectors.put(connectorOther.connectorId, connector.connectorId);

					// nodes
					addNode(connector, connectorOther, fragmentRepo, amId);
					
				}else {
					if(!fragmentRepo.fragAmConnectorMap.get(connector.connectorId).containsKey(amId) && !matchedConnectors.containsKey(connectorOther.connectorId) && !matchedConnectors.values().contains(connector.connectorId)) {
						fragmentRepo.fragAmConnectorMap.get(connector.connectorId).put(amId, connectorOther.connectorId);
						matchedConnectors.put(connectorOther.connectorId, connector.connectorId);
						addNode(connector, connectorOther, fragmentRepo, amId);
					}
					
				}
			}
			
			
			
				for(NodeConnector connectorSuccessor: connectors) {
					for(NodeConnector connectorSuccessorOther: connectorsOther) {
						 compareSuccessors(connectorSuccessor, connectorSuccessorOther, fragmentRepo ,fragmentNew, model,amId, firstInput,matchedConnectors);
						
					}
				}
				
			
			
			
			
		}
		
		return matchedConnectors;
		
	}
	
	private void addNode(NodeConnector connector, NodeConnector connectorOther, Fragment fragmentRepo, String amId) {
		// nodes	
		String startNode = connector.getStartRef().nodeId;
		String targetNode = connector.getTargetRef().nodeId;
		
		String startNodeB = connectorOther.getStartRef().nodeId;
		String targetNodeB = connectorOther.getTargetRef().nodeId;
		
		if(!fragmentRepo.fragAmNodeMap.containsKey(startNode)) {
			
			Map<String, String> startNodesB = new HashMap<String, String>();
			startNodesB.put(fragmentRepo.baseModel, startNode);
			startNodesB.put(amId, startNodeB);
			fragmentRepo.fragAmNodeMap.put(startNode, startNodesB);
		}else {
			fragmentRepo.fragAmNodeMap.get(startNode).put(amId,startNodeB);
		}
		
		if(!fragmentRepo.fragAmNodeMap.containsKey(targetNode)) {
			
			Map<String, String> targetNodesB = new HashMap<String, String>();
			targetNodesB.put(fragmentRepo.baseModel, targetNode);
			targetNodesB.put(amId, targetNodeB);
			fragmentRepo.fragAmNodeMap.put(targetNode, targetNodesB);
		}else {
			fragmentRepo.fragAmNodeMap.get(targetNode).put(amId, targetNodeB);
		}
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
	
	
	//set the starsNodeMap for fragment
	public void getNodesFragment(Fragment fragment) {
		
		for(NodeConnector connector: fragment.connectorMapA.values()) {
			if(!fragment.starsNodeMapA.containsKey(connector.getStartRef().nodeId)) {
				fragment.starsNodeMapA.put(connector.getStartRef().nodeId, connector.getStartRef());
				
			}
		if(!fragment.starsNodeMapA.containsKey(connector.getTargetRef().nodeId)) {
			fragment.starsNodeMapA.put(connector.getTargetRef().nodeId, connector.getTargetRef());
			}
		}
		
	}
	
	public Set<Fragment> getFragments(){
		return fragments.keySet();
	}
	
	public void reset() {
		this.fragments.clear();
	}

}
