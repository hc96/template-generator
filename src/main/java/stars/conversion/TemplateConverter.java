package stars.conversion;

import stars.model.Fragment;
import stars.model.NodeConnector;
import stars.model.StarsNode;
import stars.model.Template;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class TemplateConverter {
	
	
	public TemplateConverter() {
		
	}
	
	
	
	public Template convertFragmentToTemplate(Fragment fragment, Template template) {
		
		Map<String, String> connectorIdMap = new HashMap<String, String>();
		String followingNodeId =  null;
		String followingConnectorId = null;
		Map<String, String> previous = new HashMap<String, String>();
		List<String> endEvents =  new ArrayList<String>();
		
		// metadata
		template.lecturers.addAll(fragment.lecturers);
		template.applicationModels.putAll(fragment.applicationModels);
		template.size = fragment.size;

		// construct startEvent
		for(StarsNode node: fragment.starsNodeMapA.values()) {
					
			 if(node.getType().equals("startEvent")){
			    followingNodeId = node.getOutgoing().get(0).getTargetRef().nodeId;
			    StarsNode nodeTemplate = new StarsNode();
				nodeTemplate.nodeId =  "StartEvent_1";
				nodeTemplate.setType(node.getType());
				template.setNode(nodeTemplate.nodeId, nodeTemplate);
					
								
				NodeConnector connectorTemplate = new NodeConnector();
				String id = UUID.randomUUID().toString();
				connectorTemplate.connectorId = "SequenceFlow_" + id;
				followingConnectorId = connectorTemplate.connectorId;
				template.setConnector(connectorTemplate.connectorId, connectorTemplate);
				
				connectorIdMap.put(node.getOutgoing().get(0).connectorId, connectorTemplate.connectorId);
				
				connectorTemplate.setStartRef(nodeTemplate);
				nodeTemplate.setOutgoing(connectorTemplate.connectorId, connectorTemplate);
					
				// add mapping 	
				if(node.getOutgoing().get(0).connectorId.equals(fragment.startingConnector)) {					
					for(String amId: fragment.amNodeConnectors.keySet()) {
						for(String connectorAM: fragment.amNodeConnectors.get(amId).keySet()) {
							try {
								if(fragment.fragAmConnectorMap.get(node.getOutgoing().get(0).connectorId).get(amId).equals(connectorAM)) {
									fragment.amNodeConnectors.get(amId).put(connectorAM, connectorTemplate.connectorId);
									break;
								}	
						   }catch (NullPointerException e) {
						       System.out.println("Null Pointer Exception in Template Converter.");
						    
						 }

						}
					}
				}
				
				break;
					
			 }
		}
		
		
		for(StarsNode node: fragment.starsNodeMapA.values()) {
			
			// a model can have multiple endEvents
			 if(node.getType().equals("endEvent")){
				 	
				 	// self-add endEvent
				 if(node.getIncoming().get(0).getStartRef().getOutgoing().get(0).getTargetRef().nodeId != node.nodeId) {
					 
				    StarsNode nodeTemplate = new StarsNode();
					nodeTemplate.nodeId =  node.nodeId;
					nodeTemplate.setType(node.getType());
					template.setNode(nodeTemplate.nodeId, nodeTemplate);


					if(node.getIncoming().size() != 0) {

						for(NodeConnector connector:  node.getIncoming()) {
							
							NodeConnector connectorTemplate = new NodeConnector();
							String id = UUID.randomUUID().toString();
							connectorTemplate.connectorId = "SequenceFlow_" + id;
							previous.put(connector.getStartRef().nodeId, connectorTemplate.connectorId);
							template.setConnector(connectorTemplate.connectorId, connectorTemplate);
							
							connectorIdMap.put(connector.connectorId, connectorTemplate.connectorId);
							
							connectorTemplate.setTargetRef(nodeTemplate);
							nodeTemplate.setIncoming(connectorTemplate.connectorId, connectorTemplate);
				
							
						}
					 }
					  endEvents.add(node.nodeId);
				}
					 
					 
					
				 
			 }
		}
		
		

		for(StarsNode node: fragment.starsNodeMapA.values()) {
			
			
			if(!node.getType().equals("startEvent") && !endEvents.contains(node.nodeId)) {
				
				StarsNode nodeTemplate = new StarsNode();
				String nodeId = UUID.randomUUID().toString();
				nodeTemplate.nodeId =  captureName(node.getType()) + "_" + nodeId;
				nodeTemplate.setType(node.getType());
				template.setNode(nodeTemplate.nodeId, nodeTemplate);
				

				if(node.getOutgoing().size() != 0) {
					if(previous.containsKey(node.nodeId)) {
						 template.getConnector(previous.get(node.nodeId)).setStartRef(nodeTemplate);
						  nodeTemplate.setOutgoing(previous.get(node.nodeId), template.getConnector(previous.get(node.nodeId)));
						
					}else {

						for(NodeConnector connector: node.getOutgoing()) {

							if(fragment.connectorMapA.containsKey(connector.connectorId)) {
								if(connectorIdMap.containsKey(connector.connectorId)) {
									nodeTemplate.setOutgoing(connectorIdMap.get(connector.connectorId), template.getConnectors().get(connectorIdMap.get(connector.connectorId)));
									template.getConnectors().get(connectorIdMap.get(connector.connectorId)).setStartRef(nodeTemplate);
	
								}else {
									
									NodeConnector connectorTemplate = new NodeConnector();
									String id = UUID.randomUUID().toString();
									connectorTemplate.connectorId = "SequenceFlow_" + id;
									template.setConnector(connectorTemplate.connectorId, connectorTemplate);
									
									connectorIdMap.put(connector.connectorId, connectorTemplate.connectorId);
									
									connectorTemplate.setStartRef(nodeTemplate);
									nodeTemplate.setOutgoing(connectorTemplate.connectorId, connectorTemplate);
									
									
									// add mapping 	
									for(String amId: fragment.amNodeConnectors.keySet()) {
										for(String connectorAM: fragment.amNodeConnectors.get(amId).keySet()) {
											try {
												if(fragment.fragAmConnectorMap.get(connector.connectorId).get(amId).equals(connectorAM)) {
													fragment.amNodeConnectors.get(amId).put(connectorAM, connectorTemplate.connectorId);
													break;
												}
											}catch (NullPointerException e) {							    
											    System.out.println("Null Pointer Exception in Template Converter.");	    
											 }
										}
									}
								
									
								}
								
							}
							
						}
						
					}	
					
			}
				
				
			  if(node.getIncoming().size() != 0) {  
					  for(NodeConnector connector:  node.getIncoming()) {
							if(fragment.connectorMapA.containsKey(connector.connectorId)) {
								if(connectorIdMap.containsKey(connector.connectorId)) {
									nodeTemplate.setIncoming(connectorIdMap.get(connector.connectorId), template.getConnectors().get(connectorIdMap.get(connector.connectorId)));
									template.getConnectors().get(connectorIdMap.get(connector.connectorId)).setTargetRef(nodeTemplate);				
								}else {
									NodeConnector connectorTemplate = new NodeConnector();
									String id = UUID.randomUUID().toString();
									connectorTemplate.connectorId = "SequenceFlow_" + id;
									template.setConnector(connectorTemplate.connectorId, connectorTemplate);
									
									connectorIdMap.put(connector.connectorId, connectorTemplate.connectorId);
									
									connectorTemplate.setTargetRef(nodeTemplate);
									nodeTemplate.setIncoming(connectorTemplate.connectorId, connectorTemplate);
									
									// add mapping 				
									for(String amId: fragment.amNodeConnectors.keySet()) {
										for(String connectorAM: fragment.amNodeConnectors.get(amId).keySet()) {
											try {
												if(fragment.fragAmConnectorMap.get(connector.connectorId).get(amId).equals(connectorAM)) {
													fragment.amNodeConnectors.get(amId).put(connectorAM, connectorTemplate.connectorId);
													break;
												}
											}catch (NullPointerException e) {			    
											    System.out.println("Null Pointer Exception in Template Converter.");	    
											 }
										}
									}
								}
								
							}
						}

				  	
					  if(node.nodeId.equals(followingNodeId)) {
						  template.getConnector(followingConnectorId).setTargetRef(nodeTemplate);
						  nodeTemplate.setIncoming(followingConnectorId, template.getConnector(followingConnectorId));
					  }
				
			  }
			  
			// set up the mapping	
			  for(String amId: fragment.amStarsNodes.keySet()) {
					for(String nodeAM: fragment.amStarsNodes.get(amId).keySet()) {
						  try {	
							if(fragment.fragAmNodeMap.get(node.nodeId).get(amId).equals(nodeAM)) {		
								if(fragment.amStarsNodes.get(amId).get(nodeAM) != null && fragment.amStarsNodes.get(amId).get(nodeAM).contains("source")) {
									fragment.amStarsNodes.get(amId).put(nodeAM, "source,"+nodeTemplate.nodeId);
								}else if(fragment.amStarsNodes.get(amId).get(nodeAM) != null && fragment.amStarsNodes.get(amId).get(nodeAM).contains("EndEvent")){
									String target = nodeTemplate.getOutgoing().get(0).getTargetRef().nodeId +","+nodeTemplate.nodeId;
									fragment.amStarsNodes.get(amId).put(nodeAM, target); 
								}else {
									fragment.amStarsNodes.get(amId).put(nodeAM, nodeTemplate.nodeId);
								}	
								
								if(nodeTemplate.getType().equals("andFork") || nodeTemplate.getType().equals("orFork") || nodeTemplate.getType().equals("decisionFork")) {
									if(nodeTemplate.getOutgoing().size() < 2) {
										template.hint = "Exsit at least one transition Block has less than 2 outgoing branches.";
									}
								}
								
								break;
							}
						  } catch (NullPointerException e) {   
							    System.out.println("Null Pointer Exception in Template Converter.");	    
						  }
					}
				}
			 
			 
				
			}
	
		}
		
		
		template.amStarsNodes.putAll(fragment.amStarsNodes);
		template.amNodeConnectors.putAll(fragment.amNodeConnectors);
			
		
		return template;
		
		
	}
	

	
	public void createElements(StarsNode node, Template template, Fragment fragment) {
		StarsNode nodeTemplate = new StarsNode();
		template.addNode(nodeTemplate);
		
		for(NodeConnector connector: node.getOutgoing()) {
			if(fragment.connectorMapA.containsKey(connector.connectorId)) {
				NodeConnector connectorTemplate = new NodeConnector();
				connectorTemplate.setStartRef(nodeTemplate);
				nodeTemplate.setOutgoing(null, connectorTemplate);
				createElements(connector.getTargetRef(), template, fragment);
			}
			
		}
	}
	
	private static String captureName(String name) {
		char[] result = name.toCharArray();
        result[0] -= 32;
        return String.valueOf(result);
    }
	
	
	

}
