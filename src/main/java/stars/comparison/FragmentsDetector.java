package stars.comparison;

import stars.model.Fragment;
import stars.model.NodeConnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FragmentsDetector {
	
	
	public FragmentsDetector() {
		
	}
	
	
	// traverse all sequence flows based on DFS
	public Fragment discoverFragments(NodeConnector connectorA, NodeConnector connectorB, Fragment matchedFragment, boolean firstInput, boolean test, int position) {
		
		if(connectorA.getTargetRef().getType().equals(connectorB.getTargetRef().getType()) && !connectorA.getTargetRef().getType().contains("endEvent")) {
							
			boolean brother = false;
			if(test) {
				brother =  true;
			}
			
			List<NodeConnector> connectorsA = null;
			List<NodeConnector> connectorsB = null;
			if(!(!firstInput|| !test)) {
				 connectorsA = connectorA.getStartRef().getOutgoing();
				 connectorsB = connectorB.getStartRef().getOutgoing();
//				 System.out.println("----FIRST INPUT-----");
				 firstInput =  false;
				 
				 if(connectorsA.size() > 1) {
					 for(NodeConnector connector: connectorsA) {
						 connector.branchCompared = true;
					 }
				 }
				  
				 if(connectorsB.size() > 1) {
					 for(NodeConnector connector: connectorsB) {
						 connector.branchCompared = true;
					 }
				 }
				 
				
			}else {
				connectorsA = connectorA.getTargetRef().getOutgoing();
				connectorsB = connectorB.getTargetRef().getOutgoing();
				test = false;

				if(connectorA.getTargetRef().visited == 1 && connectorB.getTargetRef().visited == 1 && connectorA.getTargetRef().position == connectorB.getTargetRef().position) {
					connectorA.circle = true;
					connectorB.circle = true;		
					Integer pos = connectorA.getTargetRef().position;
					matchedFragment.circlePosition.put(connectorA.connectorId, pos);
					
				}else if(connectorA.getTargetRef().visited == 1){
					connectorA.circle = true;
					return matchedFragment;
				}else if(connectorB.getTargetRef().visited == 1){
					connectorB.circle = true;
					return matchedFragment;
				}else if((connectorA.isCircle || connectorB.isCircle)) {
					return matchedFragment;
				}
				
				
				// set connectorA and connectorB as visited
				if(connectorA.getTargetRef().visited == 0){
					connectorA.getTargetRef().visited = 1;
				}
				if(connectorB.getTargetRef().visited == 0){
					connectorB.getTargetRef().visited = 1;
				}
				
			}
			
			
			// connectorMapA: stores matched sequence flows(connectors) in modelA
			// connectorMapB: stores matched sequence flows(connectors) in modelB
	
			checkConnectorDuplicate(connectorA, connectorB, matchedFragment);

			// avoid the first input NodeConnector recognized as controversial NodeConnector
			if(!test) {
			
				if(!matchedFragment.connectorMapA.containsKey(connectorA.connectorId)) {					
					matchedFragment.connectorMapA.put(connectorA.connectorId, connectorA);
				}
				if(!matchedFragment.connectorMapB.containsKey(connectorB.connectorId)) {
					matchedFragment.connectorMapB.put(connectorB.connectorId, connectorB);
				}	
				
				if(!matchedFragment.matchedConnectors.containsKey(connectorA.connectorId)) {
					if(matchedFragment.matchedPairs.containsKey(connectorA.connectorId)) {
						matchedFragment.matchedPairs.get(connectorA.connectorId).add(connectorB.connectorId);
						
					}else {
						List<String> list = new ArrayList<String>(); 
					    list.add(connectorB.connectorId); 
					    matchedFragment.matchedPairs.put(connectorA.connectorId,list) ;
					}
				}
				
				
			}
			
			
			// return if two matched connectors contributing to circles
			if(connectorA.circle && connectorB.circle) {
				return matchedFragment;
			}
			
			// record the position of matched node
			position++;
			connectorA.getTargetRef().position = position;
			connectorB.getTargetRef().position = position;
			
			if(!connectorA.getTargetRef().getType().contains("endEvent")) {

				for(NodeConnector connA: connectorsA) {
					for(NodeConnector connB: connectorsB) {
						matchedFragment = discoverFragments(connA, connB, matchedFragment, firstInput, test, position);
					}			
				}
				
				// matchedFragments clean up		
				matchedFragment = cleanUp(matchedFragment,connectorA, connectorB, brother, test);
				
				
			}

			// all outgoing branches has visited
			connectorA.getTargetRef().visited = 2;
			connectorB.getTargetRef().visited = 2;
	 }
		
		return matchedFragment;
		
		
	}
	
    /* Controversial NodeConnector Identification
     * 
     * */

	public void checkConnectorDuplicate(NodeConnector connectorA, NodeConnector connectorB, Fragment matchedFragment) {
		
		
		if(matchedFragment.connectorMapA.containsKey(connectorA.connectorId)){
			if(matchedFragment.matchedPairs.get(connectorA.connectorId)!= null && !matchedFragment.matchedPairs.get(connectorA.connectorId).contains(connectorB.connectorId)) {
				matchedFragment.setA.add(connectorA.connectorId);
			}

		}
		
		if(matchedFragment.connectorMapB.containsKey(connectorB.connectorId)){
			boolean flag = false;
			for (Map.Entry<String, List<String>> entry : matchedFragment.matchedPairs.entrySet()) {
				if(entry.getValue().contains(connectorB.connectorId)) {
					if(entry.getKey() != connectorA.connectorId) {
						flag = true;
						break;
					}
					
				}
				
			}
			
			if(flag) {
				matchedFragment.setB.add(connectorB.connectorId);
			}
		
		}
		
	}
	
	/*
	 * Clean Up
	 * 
	 * */
	public Fragment cleanUp(Fragment matchedFragment, NodeConnector connectorA, NodeConnector connectorB, boolean brother, boolean test) {
		
		Set<String> setA = new HashSet<String>();
		Set<String> setB = new HashSet<String>();

		if(brother && test) {
			setA = matchedFragment.setA;
			setB = matchedFragment.setB;
		}else {
			if(!connectorA.getTargetRef().getType().contains("endEvent")) {
				for(NodeConnector con: connectorA.getTargetRef().getOutgoing()) {
					if(matchedFragment.setA.contains(con.connectorId)) {
						setA.add(con.connectorId);
						if(matchedFragment.map_cleanup_A.containsKey(con.connectorId)) {
							setA.add(matchedFragment.map_cleanup_A.get(con.connectorId));
						}
					}
				}
				
				
			}
			
			if(!connectorB.getTargetRef().getType().contains("endEvent")) {
				for(NodeConnector con: connectorB.getTargetRef().getOutgoing()) {
					if(matchedFragment.setB.contains(con.connectorId)) {
						setB.add(con.connectorId);
						if(matchedFragment.map_cleanup_B.containsKey(con.connectorId)) {
							setB.add(matchedFragment.map_cleanup_B.get(con.connectorId));
						}
					}
				}
			}
		}
		

		calculateSuccessorCounter(matchedFragment, connectorA, "connectorA", brother);
		calculateSuccessorCounter(matchedFragment, connectorB, "connectorB", brother);

		
		// sort the successorCounterA and successorCounterB
		sort(matchedFragment);
		
	    // clean up controversial NodeConnectors
		if(setA.size() != 0) {

			// Controversial NodeConnectors exist		
			for (Map.Entry<String, Integer> entryA : matchedFragment.successorCounterA.entrySet()) {				
				if(setA.isEmpty()) {
					break;
				}
				
				// eliminate controversial NodeConnectors
				if(setA.contains(entryA.getKey())) {
					
					boolean parentExist = false;
					for(NodeConnector con: matchedFragment.connectorMapA.get(entryA.getKey()).getStartRef().getIncoming()) {
						if(matchedFragment.setA.contains(con.connectorId)) {
							matchedFragment.map_cleanup_A.put(con.connectorId, entryA.getKey());
							parentExist = true;
							break;
						}
					}
					
				if(!parentExist) {
					List<String> connectorsB = matchedFragment.matchedPairs.get(entryA.getKey());
						
					if(connectorsB.size() < 2) {
						matchedFragment.setA.remove(entryA.getKey());
						setA.remove(entryA.getKey());
					}else {
						for (Map.Entry<String, Integer> entryB : matchedFragment.successorCounterB.entrySet()) {							
							if(connectorsB.contains(entryB.getKey()) && !matchedFragment.matchedConnectors.values().contains(entryB.getKey())) {
								connectorsB.clear();
								connectorsB.add(entryB.getKey());
								matchedFragment.matchedConnectors.put(entryA.getKey(), entryB.getKey());
								
								// clean setB if entryB.getValue exists in setB
								if(setB.contains(entryB.getKey())) {
									// NodeConnector entryB.getKey() is controversial
									Iterator<Map.Entry<String,List<String>>> iter = matchedFragment.matchedPairs.entrySet().iterator();
									while (iter.hasNext()) {
										Map.Entry<String, List<String>> tmpEntry = iter.next();
										
										if(tmpEntry.getValue().contains(entryB.getKey()) && tmpEntry.getKey() != entryA.getKey()) {
											if(tmpEntry.getValue().size()>1) {
												tmpEntry.getValue().remove(entryB.getKey());
											}else {
												iter.remove();
											}
											
										}
									}
									// remove the controversial NodeConnector in setB
									matchedFragment.setB.remove(entryB.getKey());
									setB.remove(entryB.getKey());
								}
								
								//  till now: the optimal matched pair has been identified
								matchedFragment.setA.remove(entryA.getKey());
								setA.remove(entryA.getKey());
								break;
							}
			
						}	
						
				     }
				  }
				
			   }	
				
			}
						
		}

		// Clean controversial NodeConnectors in setB
		if(setB.size() != 0) {
	
			for (Map.Entry<String, Integer> entryB : matchedFragment.successorCounterB.entrySet()) {
				if(setB.isEmpty()) {
					break;
				}

			if(setB.contains(entryB.getKey())) {
				// get all keys which have the same value
				boolean parentExist = false;
				for(NodeConnector con: matchedFragment.connectorMapB.get(entryB.getKey()).getStartRef().getIncoming()) {
					if(matchedFragment.setB.contains(con.connectorId)) {
						parentExist = true;
						matchedFragment.map_cleanup_B.put(con.connectorId, entryB.getKey());
						break;
					}
				}

				if(!parentExist) {
				
				for (Map.Entry<String, Integer> entryA : matchedFragment.successorCounterA.entrySet()) {
					
					boolean flag = false;
					for (Map.Entry<String, List<String>> entry : matchedFragment.matchedPairs.entrySet()) {
						if(entry.getValue().contains(entryB.getKey())) {
							if(entry.getKey() == entryA.getKey()) {
								flag = true;
								break;
							}
							
						}
						
					}		
					if(flag) {
						// remove other pairs
						matchedFragment.setB.remove(entryB.getKey());
						setB.remove(entryB.getKey());
						List<String> removeKeys = new ArrayList<String>();
						Iterator<Map.Entry<String,List<String>>> iter = matchedFragment.matchedPairs.entrySet().iterator();
						while (iter.hasNext()) {
							Map.Entry<String, List<String>> tmpEntry = iter.next();
							if(tmpEntry.getValue().contains(entryB.getKey())) {		
								if(tmpEntry.getKey() != entryA.getKey()) {
									removeKeys.add(tmpEntry.getKey());
								}
								
							}
						}
						if(removeKeys.size() != 0) {
							matchedFragment.matchedPairs.keySet().removeAll(removeKeys);
						}
						
						if(matchedFragment.matchedPairs.get(entryA.getKey()).size() > 1) {
							matchedFragment.matchedPairs.get(entryA.getKey()).clear();
							List<String> bb = new ArrayList<String>();
							bb.add(entryB.getKey());
							matchedFragment.matchedPairs.put(entryA.getKey(), bb);
						}
						
						matchedFragment.matchedConnectors.put(entryA.getKey(), entryB.getKey());
						break;
					}
			   }	
		   }	
				
		}
				
	  }	
		
	}

		return matchedFragment;
		
	}
	
	
	public void calculateSuccessorCounter(Fragment matchedFragment, NodeConnector connector, String aORb, boolean brother) {
			
		boolean noMatchedSuccessor = false;
	
		if(aORb.equals("connectorA")) {		
		for(NodeConnector successor: connector.getTargetRef().getOutgoing()) {
			int count = 0;
			if(matchedFragment.connectorMapA.containsKey(successor.connectorId)) {
				if(successor.getTargetRef().getType().contains("endEvent") || successor.circle) {
					// endEvent has no successors
					matchedFragment.successorCounterA.put(successor.connectorId, 0);
				}else {
					for(NodeConnector secondSuccessor: successor.getTargetRef().getOutgoing()) {
						if(matchedFragment.connectorMapA.containsKey(secondSuccessor.connectorId)) {
							noMatchedSuccessor =  true;
							count++;
							count += matchedFragment.successorCounterA.get(secondSuccessor.connectorId);
							
						}
					}
					
					// noMatchedSuccssor is true, which means secondSuccessors contains already matched NodeConnector
					if(noMatchedSuccessor != true) {
						matchedFragment.successorCounterA.put(successor.connectorId, 0);
					}else if(noMatchedSuccessor == true) {
						matchedFragment.successorCounterA.put(successor.connectorId, count);
						
					}			
				}

			}
		}
		
		if(brother) {
			int count_parent = 0;
			for(NodeConnector successor: connector.getTargetRef().getOutgoing()) {
				if(matchedFragment.connectorMapA.containsKey(successor.connectorId)) {
					count_parent++;
					count_parent += matchedFragment.successorCounterA.get(successor.connectorId);
	
				}
			}
			
			matchedFragment.successorCounterA.put(connector.connectorId, count_parent);
			
		}
		
	}else if(aORb.equals("connectorB")) {
		for(NodeConnector successor: connector.getTargetRef().getOutgoing()) {
			int count = 0;
			if(matchedFragment.connectorMapB.containsKey(successor.connectorId)) {
				if(successor.getTargetRef().getType().contains("endEvent") || successor.circle) {
					// endEvent has no successors
					matchedFragment.successorCounterB.put(successor.connectorId, 0);
				}else {
					for(NodeConnector secondSuccessor: successor.getTargetRef().getOutgoing()) {
						if(matchedFragment.connectorMapB.containsKey(secondSuccessor.connectorId)) {
							noMatchedSuccessor =  true;
							count++;
							count += matchedFragment.successorCounterB.get(secondSuccessor.connectorId);
							
						}
					}
					
					// noMatchedSuccssor is true, which means secondSuccessors contains already matched NodeConnector
					if(noMatchedSuccessor != true) {
						matchedFragment.successorCounterB.put(successor.connectorId, 0);
					}else if(noMatchedSuccessor == true) {		
						matchedFragment.successorCounterB.put(successor.connectorId, count);
						
					}
								
				}

			}
		}
		
		//	add the successorCount for those NodeConnectors who are the first batch/input
		if(brother) {
			int count_parent = 0;
			for(NodeConnector successor: connector.getTargetRef().getOutgoing()) {
				if(matchedFragment.connectorMapB.containsKey(successor.connectorId)) {
					count_parent++;
					count_parent += matchedFragment.successorCounterB.get(successor.connectorId);

				}
			}
			
			matchedFragment.successorCounterB.put(connector.connectorId, count_parent);
			
		}
		
	}

}
	
	
	/*
	 *  sort successorCounterA and successorCounterB
	 * 
	 * */
	public void sort(Fragment matchedFragment) {
		

		List<Entry<String, Integer>> list01 = new ArrayList<Entry<String, Integer>>(matchedFragment.successorCounterA.entrySet());
		Collections.sort(list01, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue() - o1.getValue());
			}
		});
		
		Map<String,Integer> map01 = new LinkedHashMap<String,Integer>();
		for(Entry<String, Integer> t:list01){
			map01.put(t.getKey(), t.getValue());
		}
		
		matchedFragment.successorCounterA =  map01;
		
		List<Entry<String, Integer>> list02 = new ArrayList<Entry<String, Integer>>(matchedFragment.successorCounterB.entrySet());
		Collections.sort(list02, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue() - o1.getValue());
			}
		});
		
		Map<String,Integer> map02 = new LinkedHashMap<String,Integer>();
		for(Entry<String, Integer> t:list02){
			map02.put(t.getKey(), t.getValue());
		}
		
		matchedFragment.successorCounterB =  map02;
		
		for (Map.Entry<String, Integer> entry : matchedFragment.successorCounterB.entrySet()) {
		
	}
		
	}

}
