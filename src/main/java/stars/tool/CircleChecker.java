package stars.tool;

import stars.model.ApplicationModel;
import stars.model.NodeConnector;
import stars.model.StarsNode;

import java.util.List;


public class CircleChecker {
	
	ApplicationModel model = null;
	
	public CircleChecker() {
		
	}
	
	public void checkCircle(ApplicationModel model) {
		this.model = model;
		
		for(NodeConnector connector: model.getConnectors()) {
			if(connector.getStartRef().getType().contains("startEvent")) {			
			    identifyCircle(connector);		
				break;
			}	
		}
		
		// reset visited.
		
		for(StarsNode node: model.getNodes()) {
			node.visited = 0;		
		}
	}
	
	
	
	private void identifyCircle(NodeConnector connector) {
		
		if(!connector.getTargetRef().getType().contains("endEvent")) {
			
			if(connector.isCircle) {
				return;
			}
			
			if(connector.getTargetRef().visited == 0) {
				connector.getTargetRef().visited = 1;
			}else if(connector.getTargetRef().visited == 1) {
				connector.isCircle = true;
				this.model.containCircle = true;
				return;
			}
			
			List<NodeConnector> connectors = null;
			connectors =  connector.getTargetRef().getOutgoing();
			
			
			for(NodeConnector con: connectors) {
				identifyCircle(con);
			}
			
			connector.getTargetRef().visited = 2;
			
		}
		
	}

}
