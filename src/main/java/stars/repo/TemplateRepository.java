package stars.repo;

import stars.model.Template;

import java.util.ArrayList;
import java.util.List;

public class TemplateRepository {
	
	
	private static volatile TemplateRepository templateRepository = new TemplateRepository();
	private List<Template> templates = new ArrayList<Template>();
	
    private TemplateRepository() {
		
	}
	
	public static TemplateRepository getInstance() {
		return templateRepository;
	}
	
	
	public void addTemplateToRepository(Template template) {
		templates.add(template);	
	}
	
	public List<Template> getTemplates(){
		return templates;
	}
	
	public void reset() {
		this.templates.clear();
	}
	
	

}

