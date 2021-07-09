package stars.comparison;

import stars.model.ApplicationModel;
import stars.model.Fragment;
import stars.model.NodeConnector;
import stars.model.StarsNode;
import stars.repo.FragmentRepository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConnectorComparator {
	

	private ApplicationModel baseModel;
	private ApplicationModel toCompareModel;
	
	public ConnectorComparator(ApplicationModel baseModel, ApplicationModel toCompareModel) {

		if(baseModel.getConnectors().size() >= toCompareModel.getConnectors().size())
		{
				this.baseModel = baseModel;
				this.toCompareModel = toCompareModel;
		}
		else
		{
			this.baseModel = toCompareModel;
			this.toCompareModel = baseModel;
		}

	 }
	
	
	// compare two models
	public void connectorComparision() {
		FragmentRepository fragmentRepository = FragmentRepository.getInstance();
		List<NodeConnector> connectorsModelA = baseModel.getConnectors();
		List<NodeConnector> connectorsModelB = toCompareModel.getConnectors();
		FragmentsDetector detector = new FragmentsDetector();
		Fragment matchedFragment = new Fragment();
		Map<Fragment,Integer> toCompareRound =  new HashMap<Fragment, Integer>();
		Map<Fragment,Integer> fragmentRound =  new LinkedHashMap<Fragment, Integer>();
		int round = 0;
		int position = 0;
			
		for(NodeConnector connectorA: connectorsModelA) {
			round++;
			connectorA.getStartRef().compared =  true;
			if(!connectorA.branchCompared && !connectorA.getStartRef().getType().equals("startEvent")) {	
		    	for(NodeConnector connectorB: connectorsModelB) {
				
				if(!connectorB.branchCompared) {
				connectorB.getStartRef().compared = true;
				
				if(connectorA.getStartRef().getType().equals(connectorB.getStartRef().getType())) {
					
					connectorA.getStartRef().visited = 1;
					connectorB.getStartRef().visited = 1;
					matchedFragment.startingNodeId = connectorA.getStartRef().nodeId;
					matchedFragment.startingNode = connectorA.getStartRef().getType();

					matchedFragment.startingConnectorId = connectorA.connectorId;
					matchedFragment.startingConnector = connectorA.connectorId;
					matchedFragment = detector.discoverFragments(connectorA, connectorB, matchedFragment,true,true,position);
					resetNode();

					//PRE-filter: filter out the fragments whose size less than 3
					if(matchedFragment.matchedPairs.size() < 3) {
						matchedFragment = new Fragment();
						continue;
					}
					
					// add lecturerId, and id of modelA and modelB in fragment		
					matchedFragment.lecturers.add(baseModel.lecturerId);
					matchedFragment.lecturers.add(toCompareModel.lecturerId);
					
					// fix connectorMapA and connectorMapB if necessary
					Iterator<Map.Entry<String, NodeConnector>> entryIterator_A = matchedFragment.connectorMapA.entrySet().iterator();
					while (entryIterator_A.hasNext()) {
					    Map.Entry<String, NodeConnector> next = entryIterator_A.next();
					    if(!matchedFragment.matchedPairs.keySet().contains(next.getKey())) {
					    	entryIterator_A.remove();
					    }
					}
					
					Iterator<Map.Entry<String, NodeConnector>> entryIterator_B = matchedFragment.connectorMapB.entrySet().iterator();
					while (entryIterator_B.hasNext()) {
					    Map.Entry<String, NodeConnector> next = entryIterator_B.next();
					    boolean exist = false;
					    for(List<String> list: matchedFragment.matchedPairs.values()) {
					    	if(list.contains(next.getKey())) {
					    		exist = true;
					    	}
					    }
					    if(!exist) {
					    	entryIterator_B.remove();
					    }
					    
					}
					
					getNodesFragment(matchedFragment);
					
					
					if(!matchedFragment.connectorMapA.containsKey(matchedFragment.startingConnectorId)) {
						if(matchedFragment.startingNode.equals("andFork") || matchedFragment.startingNode.equals("orFork") || matchedFragment.startingNode.equals("decisionFork")) {
							for(NodeConnector con: matchedFragment.starsNodeMapA.get(matchedFragment.startingNodeId).getOutgoing()) {
								if(matchedFragment.connectorMapA.containsKey(con.connectorId)) {
									matchedFragment.startingConnector = con.connectorId;
									matchedFragment.startingConnectorId = con.connectorId;
									break;
								}
							}
						}
					}
					
					if(fragmentRound.containsKey(matchedFragment)) {

						// this fragment repeats in modelA
						if(fragmentRound.get(matchedFragment) != round) {
							int tmpRound;
							for(Fragment frag: fragmentRound.keySet()) {
								if(frag.equals(matchedFragment)) {
									tmpRound = frag.applicationModels.get(baseModel.id);
									tmpRound++;
									frag.applicationModels.put(baseModel.id, tmpRound);
									fragmentRound.put(frag, round);
//										
									if(frag.applicationModels.get(toCompareModel.id) < toCompareRound.get(frag)) {
										frag.applicationModels.put(toCompareModel.id, toCompareRound.get(frag));
									}
									
									toCompareRound.put(frag, 1);
									
									break;
								}
							}
												
						}else {
							// this fragment repeats in modelB			
							int tmpRound = 0;	
							
							for(Fragment frag: fragmentRound.keySet()) {
								if(frag.equals(matchedFragment)) {
									
									tmpRound = toCompareRound.get(frag);
									tmpRound++;
									toCompareRound.put(frag, tmpRound);
									
									if(frag.applicationModels.get(toCompareModel.id) < toCompareRound.get(frag)) {
										frag.applicationModels.put(toCompareModel.id, toCompareRound.get(frag));
									}
									fragmentRound.put(frag, round);
									break;
									
									
								}
							}	
							
						}
						
					}else {
						
						// the first appearance of a fragment
						matchedFragment.applicationModels.put(baseModel.id, 1);
						matchedFragment.applicationModels.put(toCompareModel.id, 1);
						toCompareRound.put(matchedFragment, 1);						
						
						fragmentRound.put(matchedFragment, round);
					}
	
				}
				
				matchedFragment = new Fragment();

			}	
		}
			
			
			for(NodeConnector connector: toCompareModel.getConnectors()) {
				connector.branchCompared = false;
			}
	 		
		}
			
			for(StarsNode node: toCompareModel.getNodes()) {
				node.compared = false;
			}
			
		}
			
			
			for(StarsNode node: baseModel.getNodes()) {
				node.compared = false;
			}
			
			for(NodeConnector connector: baseModel.getConnectors()) {
				connector.branchCompared = false;
			}
			for(NodeConnector connector: toCompareModel.getConnectors()) {
				connector.branchCompared = false;
			}
			
			if(!fragmentRound.isEmpty()) {

				for(Fragment fragSmall: fragmentRound.keySet()) {
					
					int freqToCompare = 1;
					int miniFreq =0;
					int numFreqBase = 0;
					for(Fragment fragLarge: fragmentRound.keySet()) {
						if(fragLarge.includes(fragSmall)) {

								if(miniFreq == 0) {
									miniFreq = fragLarge.applicationModels.get(toCompareModel.id);
								}else {
									if(fragLarge.applicationModels.get(toCompareModel.id) < miniFreq) {
										miniFreq = fragLarge.applicationModels.get(toCompareModel.id);
									}
								}
							
							freqToCompare += fragLarge.applicationModels.get(toCompareModel.id);
							numFreqBase ++;
						}
					}
					if (  !(numFreqBase < fragSmall.applicationModels.get(baseModel.id)) &&  freqToCompare > fragSmall.applicationModels.get(toCompareModel.id)) {
						int tmpFreq = fragSmall.applicationModels.get(toCompareModel.id);
						tmpFreq += miniFreq;
						fragSmall.applicationModels.put(toCompareModel.id, tmpFreq);
					}
					
				}
				
				
				for(Fragment fragSmall: fragmentRound.keySet()) {
					for(Fragment fragLarge: fragmentRound.keySet()) {
						if(fragLarge.includes(fragSmall)) {
							
							if(fragSmall.applicationModels.get(toCompareModel.id) == fragLarge.applicationModels.get(toCompareModel.id)) {
								int freqBase = fragSmall.applicationModels.get(baseModel.id);
								freqBase = freqBase + fragLarge.applicationModels.get(baseModel.id);
								fragSmall.applicationModels.put(baseModel.id, freqBase);
							}

						}
					}
					
				}
				
				
				// store the corresponding nodes and connectors for baseModel and toCompareModel
				for(Fragment frag: fragmentRound.keySet()) {
					
					frag.baseModel = this.baseModel.id;
					frag.toCompareModel = this.toCompareModel.id;
					
					if(!frag.amNodeConnectors.keySet().contains(baseModel.id)) {
						for(NodeConnector con: frag.connectorMapA.values()) {
							if(!frag.amNodeConnectors.keySet().contains(baseModel.id)) {
								Map<String, String> mapConnectors = new HashMap<String, String>(); 
							    mapConnectors.put(con.connectorId,null); 
							    frag.amNodeConnectors.put(baseModel.id,mapConnectors);
							    
							    Map<String, String> mapNodes = new HashMap<String, String>(); 
							    mapNodes.put(con.getStartRef().nodeId,null); 
							    mapNodes.put(con.getTargetRef().nodeId, null);
							    frag.amStarsNodes.put(baseModel.id, mapNodes);
							    		   
							}else {
								frag.amNodeConnectors.get(baseModel.id).put(con.connectorId,null);
								frag.amStarsNodes.get(baseModel.id).put(con.getStartRef().nodeId, null);
								frag.amStarsNodes.get(baseModel.id).put(con.getTargetRef().nodeId, null);
							}
							
						}
						
					}
					
					if(!frag.amNodeConnectors.keySet().contains(toCompareModel.id)) {
						for(NodeConnector con: frag.connectorMapB.values()) {
							if(!frag.amNodeConnectors.keySet().contains(toCompareModel.id)) {
								Map<String, String> mapConnectors = new HashMap<String, String>(); 
							    mapConnectors.put(con.connectorId, null); 
							    frag.amNodeConnectors.put(toCompareModel.id, mapConnectors);
							    
							    Map<String, String> mapNodes = new HashMap<String, String>(); 
							    mapNodes.put(con.getStartRef().nodeId,null); 
							    mapNodes.put(con.getTargetRef().nodeId, null);
							    frag.amStarsNodes.put(toCompareModel.id, mapNodes);
							    		   
							}else {
								frag.amNodeConnectors.get(toCompareModel.id).put(con.connectorId,null);
								frag.amStarsNodes.get(toCompareModel.id).put(con.getStartRef().nodeId, null);
								frag.amStarsNodes.get(toCompareModel.id).put(con.getTargetRef().nodeId, null);
							}
							
						}
						
					}
					
					
					
				}
				
				fragmentRepository.addFragmentToRepository(fragmentRound.keySet());
			}
//			else {
//				System.out.println("fragmentRound is empty.");
//			}		 
		   
	   }
		
	/*
	 * reset the visited variable for nodes
	 * 
	 * */
		
	public void resetNode() {
		
		List<StarsNode> nodesModelA = baseModel.getNodes();
		List<StarsNode> nodesModelB = toCompareModel.getNodes();
		for(StarsNode nodeA: nodesModelA) {
			nodeA.visited = 0;
			nodeA.position = -1;
		}
		
		for(StarsNode nodeB: nodesModelB) {
			nodeB.visited = 0;
			nodeB.position = -1;
		}
		
		
	}
	
	/* reset the circle variable
	 * 
	 * */
	
	public void resetConnectors(List<NodeConnector> connectorsModelA, List<NodeConnector> connectorsModelB) {

		for(NodeConnector connector: connectorsModelA) {
			connector.circle = false;
		}
		
		for(NodeConnector connector: connectorsModelB) {
			connector.circle = false;
		}
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

}

