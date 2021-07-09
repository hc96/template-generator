package stars.repo;

import stars.model.ApplicationModel;
import stars.tool.CircleChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.json.JsonObject;
import org.dom4j.DocumentException;




public class AMRepository {
	
	
	private static volatile AMRepository amRepository = new AMRepository();	//for initiating the singleton
	private List<ApplicationModel> allModels = new ArrayList<ApplicationModel>();
	private CircleChecker circleChecker =  new CircleChecker();
	
	public Map<String, String> bpmns = new HashMap<String, String>();
	
	private AMRepository() {
		
	}
	
	public static AMRepository getInstance() {
		
		return amRepository;
	}
	
	public void constructApplicationModel(List<JsonObject> instances, String circle) throws DocumentException {
		
		for(JsonObject instance: instances) {
			String amId = instance.getString("id");
			String lectureId = instance.getString("ownerID");

			try {
				ApplicationModel model = new ApplicationModel(instance, amId);
	
				model.lecturerId = lectureId;
				
				circleChecker.checkCircle(model);
				bpmns.put(model.id, model.bpmn);
				
				// if circle is true, which means application models with circles will not be considered regarding comparison
				if(circle.contains("noCircle")) {
					if(!model.containCircle) {
						this.allModels.add(model);
					}	
				}else if(circle.contains("onlyCircle")) {
					if(model.containCircle) {
						this.allModels.add(model);
					}
				}else {
					this.allModels.add(model);	
				}	
			
		    }catch (NullPointerException e) {
			    
			    System.out.println("Null Pointer Exception in ApplicationModel:" + amId);
			    
			 }
			
		}
		
	}
	
	
	public List<ApplicationModel> getAllModels() {
		return allModels;
	}
	
	public void reset() {
		this.allModels.clear();
		this.circleChecker =  new CircleChecker();
		this.bpmns.clear();
		
	}
	

}
