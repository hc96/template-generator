package stars.tool;

import stars.model.NodeConnector;
import stars.model.StarsNode;
import stars.model.Template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;


public class XMLGenerator {
	
	List<String> bpmnElements =  new ArrayList<String>(Arrays.asList("startEvent", "endEvent"));

	
	public XMLGenerator() {
		
	}

	public String generateXML(Template template) {
		
		Map<String, StarsNode> nodes = null;
		Map<String, NodeConnector> connectors = null;
		
		nodes = template.getNodes();
		connectors = template.getConnectors();

		Document doc = DocumentHelper.createDocument();

	    Namespace bpmnNamespace = new Namespace("bpmn2", "http://www.omg.org/spec/BPMN/20100524/MODEL");
	    Namespace xsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

	    
	
		Element root = doc.addElement(new QName("definitions", bpmnNamespace));
		root.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.addNamespace("bpmndi", "http://www.omg.org/spec/BPMN/20100524/DI");
		root.addNamespace("dc", "http://www.omg.org/spec/DD/20100524/DC");
		root.addNamespace("stars", "https://some-company/schema/bpmn/stars");
		root.addNamespace("di", "http://www.omg.org/spec/DD/20100524/DI");

	    root.addAttribute("id", "sample-diagram");
	    root.addAttribute("targetNamespace", "http://bpmn.io/schema/bpmn");
	    root.addAttribute(new QName("schemaLocation", xsi), "http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd");
	    
		Element process = root.addElement("bpmn2:process");
		process.addAttribute("id", "Process_1");
		process.addAttribute("isExecutable", "false");
		
		for(StarsNode node: nodes.values()) {
			if(bpmnElements.contains(node.getType())) {
				Element event = process.addElement("bpmn2:" + node.getType());
				event.addAttribute("id",  node.nodeId);
				event.addAttribute("name", captureName(node.getType()));
				
				if(node.getIncoming().size() > 0) {
					for(NodeConnector con: node.getIncoming()) {
						Element incoming = event.addElement("bpmn2:incoming");
						incoming.setText(con.connectorId);
					}
				}
				
				if(node.getOutgoing().size() > 0) {
					for(NodeConnector con: node.getOutgoing()) {
						Element outgoing = event.addElement("bpmn2:outgoing");
						outgoing.setText(con.connectorId);
					}
				}
				
			}else {
				// stars-specific node
				Element starNode = process.addElement("stars:" + node.getType());
				starNode.addAttribute("id",  node.nodeId);
				starNode.addAttribute("name", captureName(node.getType()));
				
				if(node.getIncoming().size() > 0) {
					for(NodeConnector con: node.getIncoming()) {
						Element incoming = starNode.addElement("bpmn2:incoming");
						incoming.setText(con.connectorId);
					}
				}
				
				if(node.getOutgoing().size() > 0) {
					for(NodeConnector con: node.getOutgoing()) {
						Element outgoing = starNode.addElement("bpmn2:outgoing");
						outgoing.setText(con.connectorId);
					}
				}
	
			}
			
			
		}
		
		for(NodeConnector connector: connectors.values()) {
			Element sequenceFlow = process.addElement("bpmn2:sequenceFlow");
			sequenceFlow.addAttribute("id", connector.connectorId);
			sequenceFlow.addAttribute("sourceRef", connector.getStartRef().nodeId);
			sequenceFlow.addAttribute("targetRef", connector.getTargetRef().nodeId);			
		}

		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + root.asXML();
	}
	
	
	
	
	
	// diagram generation
	
	public String addBPMNDiagram(Template template, String modelId, String bpmn_template, String bpmn_am) throws DocumentException {
			
		
		Map<String, String> nodes = new HashMap<String, String>();
		Map<String, String> connectors = new HashMap<String, String>();
		Map<String, String> endNodes = new HashMap<String, String>();
		String startNode = null;
		String start_sequenceFlow = null;
		String bpmnDiagram =  "";
		boolean containsStartEvent = false;
		
		connectors = template.amNodeConnectors.get(modelId);
		nodes = template.amStarsNodes.get(modelId);
		
		for(String node: nodes.keySet()) {
			if(node != null && node.contains("StartEvent")) {
				containsStartEvent = true;
			}
		}
		
		
		if(!containsStartEvent) {
			for (Map.Entry<String, String> entry : nodes.entrySet()) { 
				  
				  if(entry.getValue() != null && entry.getValue().contains("source")) {
					  startNode = entry.getKey();
					  break;
				  }
				}
			
			for(NodeConnector con: template.getConnectors().values()) {
				if(con.getStartRef().getType().contains("startEvent")) {
					start_sequenceFlow = con.connectorId;
					break;
				}
				
			}

		}
		
		
		for (Map.Entry<String, String> entry : nodes.entrySet()) { 
			  
			  if(entry.getValue() != null && entry.getValue().contains("EndEvent")) {
				 endNodes.put(entry.getKey(), entry.getValue().split(",")[0]);
			  }
		}

		try {
			
		Document document = DocumentHelper.parseText(bpmn_am);
		Element root = document.getRootElement();
		
		Iterator<Element> it_root = root.elementIterator();
	   
		// iterate root elements
		while (it_root.hasNext()) {
		
			Element root_element = (Element) it_root.next();
			if(root_element.getName().contains("BPMNDiagram")) {
				Iterator<Element> it_child = root_element.elementIterator();
				while (it_child.hasNext()) {

					Element child_element = (Element) it_child.next();            
					if(child_element.getName().contains("BPMNPlane")) {
						Iterator<Element> it_grandchild = child_element.elementIterator();
						
						while(it_grandchild.hasNext()) {
							Element grandchild_element = (Element)it_grandchild.next();

							
							String bpmnElement = grandchild_element.attributeValue("bpmnElement");
							if(nodes.keySet().contains(bpmnElement)) {
								String shape = null;

								if(nodes.get(bpmnElement).contains("source")) {
									shape = "<bpmndi:BPMNShape id=\""+ nodes.get(bpmnElement).split(",")[1] +"_di\" bpmnElement=\""+ nodes.get(bpmnElement).split(",")[1] +"\">\n";
								}else if(nodes.get(bpmnElement).contains("EndEvent")) {
									shape = "<bpmndi:BPMNShape id=\""+ nodes.get(bpmnElement).split(",")[1] +"_di\" bpmnElement=\""+ nodes.get(bpmnElement).split(",")[1] +"\">\n";
								}else {
									shape = "<bpmndi:BPMNShape id=\""+ nodes.get(bpmnElement) +"_di\" bpmnElement=\""+ nodes.get(bpmnElement) +"\">\n";
								}
								
								
								Iterator<Element> boundLabel = grandchild_element.elementIterator();
								
								while(boundLabel.hasNext()) {
									Element label = (Element)boundLabel.next();
									String subElement = label.asXML().replaceAll("xmlns:[^\"]*=\"[^\"]*\"","");
									
									shape = shape + subElement;
								}
								
								shape = shape + "</bpmndi:BPMNShape>";

								bpmnDiagram = bpmnDiagram + shape;
								
							}else if(connectors.containsKey(bpmnElement)) {
								
								String edge = "<bpmndi:BPMNEdge id=\""+ connectors.get(bpmnElement) +"_di\" bpmnElement=\""+ connectors.get(bpmnElement) +"\">\n";
								
								Iterator<Element> waypoints = grandchild_element.elementIterator();
								
								while(waypoints.hasNext()) {
									Element point = (Element)waypoints.next();
									String pointElement = point.asXML().replaceAll("xmlns:[^\"]*=\"[^\"]*\"","");
									
									edge = edge + pointElement;

								}
								
								edge = edge + "</bpmndi:BPMNEdge>";

								bpmnDiagram = bpmnDiagram + edge;
								
							}
										
						
								// add startEvent
								if(startNode != null) {
									
									if(bpmnElement.equals(startNode)) {

										Element bounds = grandchild_element.element("Bounds");
										String x = bounds.attributeValue("x");
										String y = bounds.attributeValue("y");
										String x_1  = String.valueOf(Integer.parseInt(x) - 150);
										String y_1  = String.valueOf(Integer.parseInt(y) + 20);
										String x_2  = String.valueOf(Integer.parseInt(x_1) - 7);
										String y_2  = String.valueOf(Integer.parseInt(y_1) + 43);
										
										String x_3  = String.valueOf(Integer.parseInt(x_1) + 36);
										String y_3  = String.valueOf(Integer.parseInt(y_1) + 20);
										
										
										String startEvent = "<bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"StartEvent_1\">\n"
												+ "        <dc:Bounds x=\""+ x_1 +"\" y=\""+ y_1 +"\" width=\"36\" height=\"36\"/>\n"
												+ "        <bpmndi:BPMNLabel>\n"
												+ "          <dc:Bounds x=\""+ x_2 +"\" y=\""+ y_2 +"\" width=\"51\" height=\"14\"/>\n"
												+ "        </bpmndi:BPMNLabel>\n"
												+ "      </bpmndi:BPMNShape>";
										
										String sequenceFlow = "<bpmndi:BPMNEdge id=\""+ start_sequenceFlow +"_di\" bpmnElement=\""+ start_sequenceFlow +"\">\n"
												+ "        <di:waypoint  x=\""+ x_3 +"\" y=\""+ y_3 +"\"/>\n"
												+ "        <di:waypoint  x=\""+ x +"\" y=\""+ y_3 +"\"/>\n"
												+ "      </bpmndi:BPMNEdge>";

										
										bpmnDiagram = bpmnDiagram + startEvent + sequenceFlow;
									}
								}else {
									System.out.println("StartEvent already exists!");
								}
								
								// add EndEvent 
								
								if(endNodes.size() != 0) {
									for(String endNode: endNodes.keySet()) {
										if(bpmnElement.equals(endNode)) {
											Element bounds = grandchild_element.element("Bounds");
											String x = bounds.attributeValue("x");
											String y = bounds.attributeValue("y");
											String x_1  = String.valueOf(Integer.parseInt(x) + 150);
											String y_1  = String.valueOf(Integer.parseInt(y) + 20);
											String x_2  = String.valueOf(Integer.parseInt(x_1) - 7);
											String y_2  = String.valueOf(Integer.parseInt(y_1) + 43);
											
											String x_3  = String.valueOf(Integer.parseInt(x) + Integer.parseInt(bounds.attributeValue("width")));
											String y_3  = String.valueOf(Integer.parseInt(y_1) + 20);

											String end_sequenceFlow = template.getNodes().get(endNodes.get(endNode)).getIncoming().get(0).connectorId;
											
											
											String endEvent = "<bpmndi:BPMNShape id=\""+ endNodes.get(endNode) +"_di\" bpmnElement=\""+ endNodes.get(endNode) +"\">\n"
													+ "        <dc:Bounds x=\""+ x_1 +"\" y=\""+ y_1 +"\" width=\"36\" height=\"36\"/>\n"
													+ "        <bpmndi:BPMNLabel>\n"
													+ "          <dc:Bounds x=\""+ x_2 +"\" y=\""+ y_2 +"\" width=\"47\" height=\"14\"/>\n"
													+ "        </bpmndi:BPMNLabel>\n"
													+ "      </bpmndi:BPMNShape>";
											
											String sequenceFlow = "<bpmndi:BPMNEdge id=\""+ end_sequenceFlow +"_di\" bpmnElement=\""+ end_sequenceFlow +"\">\n"
													+ "        <di:waypoint  x=\""+ x_3 +"\" y=\""+ y_3 +"\"/>\n"
													+ "        <di:waypoint  x=\""+ x_1 +"\" y=\""+ y_3 +"\"/>\n"
													+ "      </bpmndi:BPMNEdge>";

											
											bpmnDiagram = bpmnDiagram + endEvent + sequenceFlow;
											
											
										}
									}
								}else {
									System.out.println("EndEvent already exists!");
								}
						
							
						}
						// bpmnPlane
						break;
					}
				}
				// bpmnDiagram
				break;
			}
			
		
		
		}
		
	       
		    } catch (NullPointerException e) {
		    
		    return "No BPMN Diagram detected.";
		    
		 }
		
		bpmnDiagram = "<bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n"
				+ "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_1\">" + bpmnDiagram + "</bpmndi:BPMNPlane>\n"
						+ "  </bpmndi:BPMNDiagram>";
		
		
		int index = bpmn_template.indexOf("</bpmn2:definitions>");
		StringBuilder  stringBuilder = new StringBuilder (bpmn_template);
		stringBuilder.insert(index, bpmnDiagram);
		
		return stringBuilder.toString();
	}
	
	private static String captureName(String str) {
		char[] result = str.toCharArray();
        result[0] -= 32;
        return String.valueOf(result);
    }
}
