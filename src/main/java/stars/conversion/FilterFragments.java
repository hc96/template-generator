package stars.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import stars.model.Fragment;
import stars.model.NodeConnector;
import stars.model.Template;
import stars.repo.TemplateRepository;

public class FilterFragments {
	
	List<String> blocks =  new ArrayList<String>(Arrays.asList("andFork","orFork","decisionFork", "startEvent", "endEvent", "pauseBlock"));
	TemplateRepository templateRepository = TemplateRepository.getInstance();
	TemplateConverter converter;
	
	public FilterFragments() {
		converter = new TemplateConverter();
		
	}
	
	
	public void filterFragment(Fragment fragment) {
		
		if(fragment.matchedPairs.size() < 3) {
			return;
		}else {
			int functionBlock = 0;
			
			for(NodeConnector connector: fragment.connectorMapA.values()) {
				
		
			  if(!blocks.contains(connector.getTargetRef().getType())) {
				  functionBlock += 1;
				  if(functionBlock == 2) {
					  break;				 
				  }
			   }
		
				
			}
			
			
			if(functionBlock > 1) {

				Template template = new Template();
				
				template = converter.convertFragmentToTemplate(fragment, template);
				templateRepository.addTemplateToRepository(template);
				
				
			}else {
				return;
			}
			
			
		}
		
	}
	
	
	
	

}

