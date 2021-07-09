package stars.generator;

import stars.comparison.ModelComparator;
import stars.conversion.FilterFragments;
import stars.model.Fragment;
import stars.model.StarsNode;
import stars.model.Template;
import stars.repo.AMRepository;
import stars.repo.FragmentRepository;
import stars.repo.TemplateRepository;
import stars.tool.XMLGenerator;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.impl.HttpStatusException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.core.http.HttpMethod;
import org.dom4j.DocumentException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Generator extends AbstractVerticle {
	
	     RoutingContext routingContext = null;
	
		AMRepository amRepository = AMRepository.getInstance();
		FragmentRepository fragmentRepository = FragmentRepository.getInstance();
		TemplateRepository templateRepository = TemplateRepository.getInstance();
		
		FilterFragments filter = new FilterFragments();
		XMLGenerator xmlGenerator = new XMLGenerator();
		
		// URL parameters
		String repository;
		String startDate;
		String endDate;
		String sizeSmall;
		String sizeLarge;
		String circle;
		String accessToken;  
		String page;
		Integer itemsPerPage = 6;
		Integer totalPages;
		
		JsonArray arrayTemplate = new JsonArray();
		JsonArray filteredTemplate = new JsonArray();
		JsonArray allInstances = new JsonArray();
		
		List<JsonObject> instances = new ArrayList<JsonObject>();
	  
	  @Override
	  public void start() throws Exception {
		
		 System.setProperty("vertx.disableDnsResolver", "true");

		 VertxOptions options = new VertxOptions();
		 options.setBlockedThreadCheckInterval(1000*60*60);
		 options.setMaxWorkerExecuteTime(2147483647);
		 
		 // Create a Router
	    Router router = Router.router(vertx);
	    HashSet<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("Access-Control-Allow-Headers");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("Authorization");
        allowedHeaders.add("origin");
        allowedHeaders.add("accept");

        HashSet<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PATCH);
        allowedMethods.add(HttpMethod.PUT);
	    
        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

	    // Mount the handler for all incoming requests at every path and HTTP method
	    router.route("/api/v1/generator").handler(this::getInstances);
	    router.route("/api/v1/templates").handler(this::getTemplates);
	    router.route("/api/v1/templates/sorting").handler(this::sortTemplates);
	    router.route("/api/v1/templates/size").handler(this::sortTemplatesBySize);
	    router.route("/api/v1/templates/elements").handler(this::sortTemplatesByElements);
	    router.route("/api/v1/templates/request").handler(this::requestTemplates);
	    
	   
	    

	    // Create the HTTP server
	    vertx.createHttpServer()
	      // Handle every request using the router
	      .requestHandler(router)
	      // Start listening
	      .listen(8888)
	      // Print the port
	      .onSuccess(server ->
	        System.out.println(
	          "HTTP server started on port " + server.actualPort()
	        )
	      );
	  }
	  
	  
	  public void getInstances(RoutingContext routingContext) {	  
		  
		  this.amRepository.reset();
		  this.fragmentRepository.reset();
		  this.templateRepository.reset();
		  this.allInstances.clear();
		  this.instances.clear();
		  
		  this.routingContext = routingContext;
	    	
	      HttpServerRequest req = routingContext.request();
	    		    	
	      MultiMap queryParams = routingContext.queryParams();
	      repository = queryParams.contains("repository") ? queryParams.get("repository") : null;
	      startDate = queryParams.contains("startDate") ? queryParams.get("startDate") : null;
	      endDate = queryParams.contains("endDate") ? queryParams.get("endDate") : null;
	      sizeSmall = queryParams.contains("sizeSmall") ? queryParams.get("sizeSmall") : null;
	      sizeLarge = queryParams.contains("sizeLarge") ? queryParams.get("sizeLarge") : null;
	      circle = queryParams.contains("circle") ? queryParams.get("circle") : null;
	      page = queryParams.contains("page") ? queryParams.get("page") : null;

	      accessToken = routingContext.request().getHeader(HttpHeaders.AUTHORIZATION).substring("Bearer ".length());
//	      accessToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzZXNzaW9uIjoiZGMxYjZiNzgtYWFiYS00ZTE5LTllZTAtZTljY2E4MGYzNzRlIiwicGVybWlzc2lvbnMiOlsibGVjdHVyZXIiLCJzdHVkZW50Il0sImlhdCI6MTYyNTgxODY3NCwiZXhwIjoxNjI1ODYxODc0LCJzdWIiOiJkYzFiNmI3OC1hYWJhLTRlMTktOWVlMC1lOWNjYTgwZjM3NGUifQ.Hyl-dsBvlESA_jdKML3tu3LZcNVyVm61TBpnQuvEjkQ";
	      
	   
	      WebClient client = WebClient.create(vertx);
	      
	      // Send a GET request
	      if(repository.equals("amRepository")) {
	    	  
	    	    client
		        .getAbs("https://backend.stars-project.com/api/v1/backend/instances")
//		        .ssl(true)
		        .authentication(new TokenCredentials(accessToken))
		        .send()
		        .onSuccess(response -> {
		        	if (response.statusCode() == 200 && response.getHeader("content-type").equals("application/json; charset=utf-8")) {
		        		JsonArray body  = response.bodyAsJsonArray();
		        		if(body.size() != 0) {
		        			try {
								filterInstances(body);
							} catch (DocumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
		        		}
		        	
			        	System.out.println("Received response with status code" + response.statusCode() + " with body ");
		        	}else {
		        	      System.out.println("Something went wrong " + response.statusCode());
		        	      responseError("Wrong Status Code",response.statusCode());
		        	}
		        	
		        	
		        	
		        })
		        .onFailure(err ->{
		        	responseError("Failed",0);
		        	System.out.println("Something went wrong " + err.getMessage());
		        }
		          );
	    	  
	      }else if(repository.equals("privateTemplates")) {
	    	  
	    	    client
		        .getAbs("https://backend.stars-project.com/api/v1/backend/templates")
//		        .ssl(true)
		        .authentication(new TokenCredentials(accessToken))
		        .send()
		        .onSuccess(response -> {
		        	if (response.statusCode() == 200 && response.getHeader("content-type").equals("application/json; charset=utf-8")) {
		        		JsonArray body  = response.bodyAsJsonArray();
		        		if(body.size() != 0) {
		        			try {
								filterInstances(body);
							} catch (DocumentException e) {
								e.printStackTrace();
							} 
		        		}
		        	
			        	System.out.println("Received response with status code" + response.statusCode() + " with body ");
		        	}else {
		        	      System.out.println("Something went wrong " + response.statusCode());
		        	      responseError("Wrong Status Code",response.statusCode());
		        	}
		        	
		        	
		        	
		        })
		        .onFailure(err ->{
		        	responseError("Failed",0);
		        	System.out.println("Something went wrong " + err.getMessage());
		        }
		          );
	    	  
	      }

		 
		 
	 }
	  
	  
	  
	  private void getTemplates(RoutingContext routingContext) {		  
	
		  routingContext.json(arrayTemplate);	  
		  
	  }

	  
	  /*
	   * filter instances based on date range
	   * */
	  
	  private void filterInstances(JsonArray body) throws DocumentException {	

    	  
    	  Iterator<Object> iter = body.iterator();
    	  allInstances = body;
    	  System.out.println("body: " + body.size());
	      
	      if(startDate != "" && endDate != "") {
		     
		      while (iter.hasNext()) {
		    	 Object obj = iter.next();
		    	 JsonObject instance = (JsonObject) obj; 	 
		    	 String instanceDate = instance.getJsonObject("stars").getString("scenarioDate");
		    	 Boolean isValid = instance.getBoolean("isValid");
		    	 
		    	 if(instanceDate != null) {
		    		 int compare_with_startDate = instanceDate.compareTo(startDate);
			    	 int compare_with_endDate = instanceDate.compareTo(endDate);
			    	 if(compare_with_startDate > 0 && compare_with_endDate < 0 && isValid) {
			    		instances.add(instance);
			    	 }		 
		    	 }	    	
		    	
		      }
	    	  
	      }else {
	    	  // all instances are needed
	    	  System.out.println("get all instances.");
		      while (iter.hasNext()) {
		    	  Object obj = iter.next();
			      JsonObject instance = (JsonObject) obj; 
			      Boolean isValid = instance.getBoolean("isValid");
			      if(isValid != null && isValid) {
			    	  
			    	  instances.add(instance);
			      }
			      
		      }
	      }
	      
	      System.out.println("size of instances: " + instances.size());
	      // at least 2 instances
	      if(instances.size() > 1) {
	    	  amRepository.constructApplicationModel(instances,circle);
	    	  
	    	  // model comparison
	    	  triggerComparison();
	      }else {
	    	  // response with "limited number of instances"
	    	  responseError("limited size-instances", instances.size());
	      }
	      
	      
	     
		  
	  }
	  
	  /*
	   * trigger comparison among application models
	   * 
	   * */
	  
	  private void triggerComparison() throws DocumentException {
		  
		  if(amRepository.getAllModels().size() > 1) {
			  ModelComparator.modelComparision(amRepository.getAllModels());
			  filterFragments();
			  
		  }else {
			  responseError("limited size-models", amRepository.getAllModels().size());
		  }
		  
	  }
	  
	  
	  /*
	   * filter fragments
	   * 
	   * */
	  
	  private void filterFragments() throws DocumentException {
		  int small = 0;
		  int large = 0;
		  System.out.println("small: " + this.sizeSmall + " , large: " + this.sizeLarge);
		  //TODO: sizeSmall, sizeLarge
		  small = Integer.parseInt(this.sizeSmall);
		  large = Integer.parseInt(this.sizeLarge);


		for(Fragment frag: fragmentRepository.getFragments()) {
			if(checkSize(frag, small, large)) {
				filter.filterFragment(frag);	
			}	
		} 
		
		
		generateXMLandJson();
		  
		  
	  }
	  
	  private boolean checkSize(Fragment frag, int small, int large) {
		  
		  Set<String> nodes = new HashSet<String>();
		  for(StarsNode node: frag.starsNodeMapA.values()) {
			  if(!node.getType().contains("startEvent") && !node.getType().contains("endEvent")) {
				  nodes.add(node.nodeId);
			  }
		  }
		  frag.size = nodes.size();
		  
		  if(large == 0) {
			  return true;
		  }
		 
		  if(nodes.size() >= small && nodes.size() <= large) {
			  return true;
		  }else {
			  return false;
		  }
	  }
	  
	  
	  private void generateXMLandJson() throws DocumentException {
		  this.arrayTemplate.clear();
		  
		  for(Template template: templateRepository.getTemplates()) {
			  String bpmn = xmlGenerator.generateXML(template);
			  
			  JsonArray bpmns_with_diagram = new JsonArray();  
			  for(String amId: template.applicationModels.keySet()) {
				  if(amRepository.bpmns.containsKey(amId)) {
					  String bpmn_with_diagram = xmlGenerator.addBPMNDiagram(template, amId, bpmn, amRepository.bpmns.get(amId));
					  JsonObject bpmn_diagram = new JsonObject().put("id",  amId).put("content", bpmn_with_diagram);
					  bpmns_with_diagram.add(bpmn_diagram);	
				  }
			  }
			  
			  
			  Iterator<JsonObject> iter = instances.iterator();
			
			  String bpmnAM = iter.next().getString("bpmn");
			  
			  JsonArray ams = new JsonArray();
			  int frequency = 0;
			  for(String amId: template.applicationModels.keySet()) {
				  JsonObject am = new JsonObject().put("id", amId ).put("frequency", template.applicationModels.get(amId));
				  ams.add(am);
				  frequency = frequency + template.applicationModels.get(amId);
			  }
			  
			  JsonObject jsonTemplate = new JsonObject()
					  .put("bpmn", bpmns_with_diagram)
					  .put("popularity", template.lecturers.size())
					  .put("size", template.size)
					  .put("frequency", frequency)
					  .put("models", ams)
					  .put("name", "Template")
					  .put("type", "Template")
					  .put("isPublic", true)
					  .put("id", UUID.randomUUID().toString())
					  .put("categoryNames", null)
					  .put("hint", template.hint);
			  arrayTemplate.add(jsonTemplate);
		  }
		  
		  // response to user request
		  send(arrayTemplate);
		  
	  }
	  
	  
	  /*
	   * 
	   * response to user request
	   * 
	   * */
	  
	  private void send(JsonArray arrayTemplateSend) {		
		  Integer pageRequest = null;
          try {
        	  if(page != null) {
        		  pageRequest = Integer.parseInt(page);
        	  }else {
        		  routingContext.fail(new HttpStatusException(400, "Page is not correct."));
        	  }
          } catch (NumberFormatException e) {
          }

          
          JsonArray sortedTemplates = new JsonArray();
        
          if(arrayTemplateSend.size()-(itemsPerPage*(pageRequest-1)) > 6) {
        	  for(int i = itemsPerPage*(pageRequest-1); i < itemsPerPage*pageRequest; i++) {
            	  sortedTemplates.add(arrayTemplateSend.getJsonObject(i));
              }
          }else {
        	  for(int i = itemsPerPage*(pageRequest-1); i < arrayTemplateSend.size(); i++) {
            	  sortedTemplates.add(arrayTemplateSend.getJsonObject(i));
              }
          }
          
		  totalPages = Math.max((int) Math.ceil((double)arrayTemplateSend.size() / itemsPerPage), 1);

          
          
          JsonObject meta = new JsonObject();
          meta.put("currentPage", pageRequest);
          meta.put("totalPages", totalPages);
          meta.put("totalCount", arrayTemplateSend.size());

          JsonObject result = new JsonObject();
          result.put("templates", sortedTemplates);
          result.put("meta", meta);
          
          
		  routingContext.json(result);	  
	  }
	  
	  private void requestTemplates(RoutingContext routingContext) {
		  this.routingContext = routingContext;
		  
		  MultiMap queryParams = routingContext.queryParams();
	      page = queryParams.contains("page") ? queryParams.get("page") : null;
	      String isFiltered = queryParams.contains("filtered") ? queryParams.get("filtered") : null;
	      
	      if(isFiltered.equals("true")){
	    	  if(this.filteredTemplate.isEmpty() || this.filteredTemplate.size() < 1) {
				  routingContext.json(
						   new JsonObject().put("error","No Templates")
				   );
			  }else {
				  send(filteredTemplate);
			  }
	      }else {
	    	  if(this.arrayTemplate.isEmpty() || this.arrayTemplate.size() < 1) {
				  routingContext.json(
						   new JsonObject().put("error","No Templates")
				   );
			  }else {
				  send(arrayTemplate);
			  }
	      }
	     
	  }
	  

	  
	  private void sortTemplates(RoutingContext routingContext) {
		  this.routingContext = routingContext;
		  JsonArray sortedTemplates = new JsonArray();
		  List<JsonObject> jsonLists = new ArrayList<JsonObject>();
		  
		  MultiMap queryParams = routingContext.queryParams();
	      String sortType = queryParams.contains("type") ? queryParams.get("type") : null;

		  if(this.arrayTemplate.isEmpty() || this.arrayTemplate.size() < 1) {
			  routingContext.json(
					   new JsonObject().put("error","No Templates")
			   );
		  }else {
			  // sort arrayTemplate
			  for(Object tmp: this.arrayTemplate) {
				  jsonLists.add((JsonObject)tmp);		  
			  }
			  
			  Collections.sort(jsonLists, new Comparator<JsonObject>() {

			        @Override
			        public int compare(JsonObject a, JsonObject b) {
			            int popA = 0;
			            int popB = 0;
			            int sizeA = 0;
			            int sizeB = 0;
			            int freqA = 0;
			            int freqB = 0;

		                popA = (Integer) a.getInteger("popularity");
		                popB = (Integer) b.getInteger("popularity");
		                sizeA = (Integer) a.getInteger("size");
		                sizeB = (Integer) b.getInteger("size");
		                freqA = (Integer) a.getInteger("frequency");
		                freqB = (Integer) b.getInteger("frequency");
			            
		                if(sortType.equals("ascPopularity")) {
		                	return popA-popB;
		                }else if(sortType.equals("descPopularity")) {
		                	return popB-popA;
		                }else if(sortType.equals("ascSize")) {
		                	return sizeA-sizeB;
		                }else if(sortType.equals("descSize")) {
		                	return sizeB-sizeA;
		                }else if(sortType.equals("ascFrequency")) {
		                	return freqA-freqB;
		                }else if(sortType.equals("descFrequency")) {
		                	return freqB-freqA;
		                }else {
		                	return 0;
		                }
			            
			          
			        }
			    });
			  
			  for(JsonObject obj: jsonLists) {
				  sortedTemplates.add(obj);
			  }
			  
			  if(!sortedTemplates.isEmpty()) {
				  this.arrayTemplate = sortedTemplates;
				  send(arrayTemplate);
			  }
			  
		  }

	  }
	  
	  /*
	   * sort templates by size
	   * 
	   * */
	  
	  private void sortTemplatesBySize(RoutingContext routingContext) {
		  this.routingContext = routingContext;
		  JsonArray sortedTemplates = new JsonArray();
		  MultiMap queryParams = routingContext.queryParams();
	      String requestedSize = queryParams.contains("size") ? queryParams.get("size") : null;
	      int size = Integer.parseInt(requestedSize);
	      
	      if(this.arrayTemplate.isEmpty() || this.arrayTemplate.size() < 1) {
			  routingContext.json(
					   new JsonObject().put("error","No Templates")
			   );
		  }else {
			  // sort arrayTemplate
			  Iterator<Object> iter = arrayTemplate.iterator();
			  
			  while(iter.hasNext()) {
				  JsonObject objTemplate = (JsonObject)iter.next();
				 if(objTemplate.getInteger("size") == size) {
					 sortedTemplates.add(objTemplate);
				 }
			  
			  }
			  
			  
			  if(!sortedTemplates.isEmpty()) {
				  this.filteredTemplate = sortedTemplates;
				  send(filteredTemplate);
			  }else {
				  routingContext.json(
						   new JsonObject().put("error","No templates found in this size.")
				   ); 
			  }
		  }
	  }
	  
	  /*
	   * sort templates by requested elements
	   * 
	   * */
	  
	  
	  private void sortTemplatesByElements(RoutingContext routingContext)
	  {
		  this.routingContext = routingContext;  
		  List<String> elements = new ArrayList<String>();
		  JsonArray sortedTemplates = new JsonArray();
		  MultiMap queryParams = routingContext.queryParams();
	      List<String> requestedElements = queryParams.getAll("elements[]");
	      
	      for(String ele: requestedElements) {
	    	  switch(ele) {
	    	  case "1":
	    		  elements.add("pauseBlock");
	    		  break;
	    	  case "2":
	    		  elements.add("lectureBlock");
	    		  break;
	    	  case "3":
	    		  elements.add("ativityBlock");
	    		  break;
	    	  case "4":
	    		  elements.add("presentMaterial");
	    		  break;
	    	  case "5":
	    		  elements.add("presentResult");
	    		  break;
	    	  case "6":
	    		  elements.add("presentCountdown");
	    		  break;
	    	  case "7":
	    		  elements.add("andFork");
	    		  break;
	    	  case "8":
	    		  elements.add("orFork");
	    		  break;
	    	  case "9":
	    		  elements.add("decisionFork");
	    		  break;
	    	  case "10":
	    		  elements.add("singleChoiceLearningQuestion");
	    		  break;
	    	  case "11":
	    		  elements.add("multipleChoiceLearningQuestion");
	    		  break;
	    	  case "12":
	    		  elements.add("freetextLearningQuestion");
	    		  break;
	    	  case "13":
	    		  elements.add("numericalLearningQuestion");
	    		  break;
	    	  case "14":
	    		  elements.add("singleChoiceSurveyQuestion");
	    		  break;
	    	  case "15":
	    		  elements.add("multipleChoiceSurveyQuestion");
	    		  break;
	    	  case "16":
	    		  elements.add("freetextSurveyQuestion");
	    		  break;
	    	  case "17":
	    		  elements.add("numericalSurveyQuestion");
	    		  break;
	    	  case "18":
	    		  elements.add("fileUploadSurveyQuestion");
	    		  break;
	    	  case "19":
	    		  elements.add("closedFeedback");
	    		  break;
	    	  case "20":
	    		  elements.add("openDiscussion");
	    		  break;
	    	  case "21":
	    		  elements.add("groupBuilder");
	    		  break;
	    	  case "22":
	    		  elements.add("groupChat");
	    		  break;
	    	  case "23":
	    		  elements.add("presentGroupAnswers");
	    		  break;
	    	  case "24":
	    		  elements.add("groupVoting");
	    		  break;
	    	  case "25":
	    		  elements.add("groupAudioVideoChat");
	    		  break;
	    	  case "26":
	    		  elements.add("peerBuilder");
	    		  break;
	    	  case "27":
	    		  elements.add("peerChat");
	    		  break;
	    	  case "28":
	    		  elements.add("presentPeerAnswers");
	    		  break;
	    	  case "29":
	    		  elements.add("presentPeerFeedback");
	    		  break;
	    	  }
	      }
	      
	      if(this.arrayTemplate.isEmpty() || this.arrayTemplate.size() < 1) {
			  routingContext.json(
					   new JsonObject().put("error","No Templates")
			   );
		  }else {
			// sort arrayTemplate
			  Iterator<Object> iter = arrayTemplate.iterator();
			  
			  while(iter.hasNext()) {
				  JsonObject objTemplate = (JsonObject)iter.next();
				  boolean contains = true;
				  for(String ele: elements) {
					  if(!objTemplate.getJsonArray("bpmn").getJsonObject(0).getString("content").contains(ele)) {
						  contains =  false;
						  break;
					  }
				  }
				 if(contains) {
					 sortedTemplates.add(objTemplate);
				 }
			  
			  }
			  
			  if(!sortedTemplates.isEmpty()) {
				  this.filteredTemplate = sortedTemplates;
				  send(filteredTemplate);
			  }else {
				  routingContext.json(
						   new JsonObject().put("error","No templates found in this size.")
				   ); 
			  }
			  
		  }
	
	      
	      System.out.println("elements: " + elements);
	  }
	  /*
	   * response with error message
	   * 
	   * */
	  
	  private void responseError(String errorMsg, int code) {
		  
		  switch(errorMsg) {
		  	case "Wrong Status Code":
		  		routingContext.json(
				   new JsonObject().put("error", "Wrong Status Code. " + code)
		         );
			  break;
		  	case "limited size-instances":
		  		routingContext.json(
				   new JsonObject().put("error", "Limited size-instances. " + code)
				 );
				  break;
		  	case "limited size-models":
		  		routingContext.json(
				   new JsonObject().put("error", "Limited size-models. " + code)
				 );
				  break;
		  	case "Failed":
		  		routingContext.json(
				  new JsonObject().put("error", "Failed.")
				);
				  break;	
			default:
				routingContext.json(
				   new JsonObject().put("error", "Unkown error.")
			    );
		  }
	
	  }
	  
	  
}