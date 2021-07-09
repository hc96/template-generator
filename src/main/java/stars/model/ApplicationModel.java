package stars.model;

import io.vertx.core.json.JsonObject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ApplicationModel {
	
	public String id;
	public String lecturerId;
	public boolean containCircle =  false;
	
	List<StarsNode> starsNodesList = new ArrayList<StarsNode>();
	List<NodeConnector> nodeConnectorList = new ArrayList<NodeConnector>();
	Map<String, NodeConnector> connectors = new HashMap<String, NodeConnector>();
	
	public String bpmn;

	
	public ApplicationModel(JsonObject instance, String id) throws DocumentException {
		
		this.id = id;
		bpmn = instance.getString("bpmn");
		
		parseBPMN(bpmn);
	
		
		integrateSubprocess();	
		
	}
	
	/**
	 * integrate subprocess into application model	
	 *
	 */
	
	public void integrateSubprocess() {
			
		for(int i = 0; i < starsNodesList.size(); i++) {
            if(starsNodesList.get(i).subProcessId != null) {
				
				for(NodeConnector connector: starsNodesList.get(i).getIncoming()) {
					if(connector.getStartRef().getType().contains("startEvent")) {
						
						for(NodeConnector con:nodeConnectorList) {
							if(con.getTargetRef().isSubProcess && con.getTargetRef().subProcessId == starsNodesList.get(i).subProcessId) {
								connector.setStartRef(con.getStartRef());
								con.getStartRef().setOutgoing(connector.connectorId, connector);			
							}
						}
						
					}
					
				}
				for(NodeConnector connector: starsNodesList.get(i).getOutgoing()) {
					if(connector.getTargetRef().getType().contains("endEvent")) {
						connector.getTargetRef().subProcessId = starsNodesList.get(i).nodeId;
						for(NodeConnector con:nodeConnectorList) {
							if(con.getStartRef().isSubProcess && con.getStartRef().subProcessId == starsNodesList.get(i).subProcessId) {
								connector.setTargetRef(con.getTargetRef());
								con.getTargetRef().setIncoming(connector.connectorId, connector);
										
							}
						}
					}
					
				}
		
				
				
			
			}
		}
		
		for(int j = 0; j < nodeConnectorList.size(); j++) {
			System.out.println("start: " + nodeConnectorList.get(j).getStartRef().getType());
			if(nodeConnectorList.get(j).getStartRef().isSubProcess) {
				nodeConnectorList.get(j).getTargetRef().incomings.remove(nodeConnectorList.get(j).connectorId);
				nodeConnectorList.remove(j);
				
			}
		}
		
		for(int j = 0; j < nodeConnectorList.size(); j++) {
		
			if(nodeConnectorList.get(j).getTargetRef().isSubProcess) {
				nodeConnectorList.get(j).getStartRef().outgoings.remove(nodeConnectorList.get(j).connectorId);
				nodeConnectorList.remove(j);		
			}
		}
		
		for(int i = 0; i < starsNodesList.size(); i++) {
			if(starsNodesList.get(i).getType().contains("startEvent")) {
				if(!starsNodesList.get(i).getOutgoing().get(0).getStartRef().getType().contains("startEvent")){
					starsNodesList.remove(i);
					break;
				}
			}
		}
		
		for(int i = 0; i < starsNodesList.size(); i++) {
			if(starsNodesList.get(i).isSubProcess) {
				starsNodesList.remove(i);
				break;
			}
		}
		
		for(int i = 0; i < starsNodesList.size(); i++) {
			if(starsNodesList.get(i).getType().contains("endEvent") && starsNodesList.get(i).subProcessId != null) {
				starsNodesList.remove(i);
				i = i - 1;
				
			}
		}	
		
	}
	


	/*
	 * parse BPMN to ApplicationModel
	 * 
	 * */
	public void parseBPMN(String bpmn) throws DocumentException {

		Document document = DocumentHelper.parseText(bpmn);
		Element root = document.getRootElement();
		
		Iterator<Element> it_root = root.elementIterator();
	   
		// iterate root elements
		while (it_root.hasNext()) {
		
			Element root_element = (Element) it_root.next();
			if(root_element.getName().contains("BPMNDiagram")) {break;}
			
			setApplicationModel(root_element);
		
		}
		
		
	}


	private void setApplicationModel(Element root_element) {
		
		// root_element can be either process or subprocess
        Iterator<Element> it_child_1 = root_element.elementIterator();
        Iterator<Element> it_child_2 = root_element.elementIterator();

        
        while (it_child_1.hasNext()) {

            Element child_element = (Element) it_child_1.next();
            
            if(child_element.getName().contains("sequenceFlow")) {

                setNodeConnector(child_element.attributeValue("id"));
                
            }else {
            	continue;
            }    

        }

     
        	List<String> incomings_subprocess = new ArrayList<String>();
        	List<String> outgoings_subprocess = new ArrayList<String>();

        while (it_child_2.hasNext()) {

            Element child_element = (Element) it_child_2.next();
            
            if(root_element.getName().contains("subProcess") && child_element.getName().contains("incoming")) {
            	incomings_subprocess.add(child_element.getText());
            	continue;
            }
            if(root_element.getName().contains("subProcess") && child_element.getName().contains("outgoing")) {
            	outgoings_subprocess.add(child_element.getText());
            	continue;
            }	
            
            if(child_element.getName().contains("sequenceFlow")) {
                continue;       
            }else{
            	
            	List<String> incomings = new ArrayList<String>();
            	List<String> outgoings = new ArrayList<String>();
            
                if(child_element.getName().contains("subProcess")) {
                	
                	// if the node is subprocess
                	
                	setApplicationModel(child_element);
               

                	
                }else {
                	
                	 // <bpmn2: outgoing>
                	 Iterator<Element> it_grandchild = child_element.elementIterator();
                     

                     while (it_grandchild .hasNext()) {

                         Element grandchild_element = (Element) it_grandchild .next();
                         
                         
                         if(grandchild_element.getName().contains("incoming")) {
                         	incomings.add(grandchild_element.getText());
                         	
                         }else if(grandchild_element.getName().contains("outgoing")) {
                         	outgoings.add(grandchild_element.getText());
                         }

                     }
                     
                     if(root_element.getName().contains("subProcess")) {
                    	 setStarsNode(child_element.getName(),child_element.attributeValue("id"), incomings, outgoings, false, root_element.attributeValue("id"));
                     }else {
                    	 setStarsNode(child_element.getName(),child_element.attributeValue("id"), incomings, outgoings, false, null);
                     }
                     
                	
                }
                
               


            }
            


        }
        
        if(root_element.getName().contains("subProcess")) {
        	 setStarsNode(root_element.getName(),root_element.attributeValue("id"), incomings_subprocess, outgoings_subprocess, true, root_element.attributeValue("id"));
        }
		
		
		
	}


	/*
	 *  set node connector 
	 * 
	 * */
	private void setNodeConnector(String id) {
		NodeConnector connector = new NodeConnector();
		connector.connectorId = id;
		nodeConnectorList.add(connector);
		connectors.put(connector.connectorId, connector);	
	}
	
	/*
	 *  set stars node
	 * 
	 * */
	private void setStarsNode(String type, String id, List<String> incomings, List<String> outgoings, boolean isSubProcess, String subProcessId) {
		StarsNode starsNode = new StarsNode();
		
		starsNode.setType(type);
		starsNode.nodeId = id;
		
		if(isSubProcess) {
			starsNode.isSubProcess = true;		
		}
		
		if(subProcessId != null) {
			starsNode.subProcessId = subProcessId;
		}
			
		if(incomings.size() != 0) {
			for(String connectorId: incomings) {
				starsNode.setIncoming(connectorId, connectors.get(connectorId));
    			connectors.get(connectorId).setTargetRef(starsNode);
			}
		}
		
		
		if(outgoings.size() != 0) {
			for(String connectorId: outgoings) {
				starsNode.setOutgoing(connectorId, connectors.get(connectorId));
				connectors.get(connectorId).setStartRef(starsNode);
			}
		}

		starsNodesList.add(starsNode);
	}
	
	
	
	public List<NodeConnector> getConnectors() {
		return nodeConnectorList;
	}
	
	public List<StarsNode> getNodes(){
		return starsNodesList;
	}

}

