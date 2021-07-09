package stars.comparison;

import stars.model.ApplicationModel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class ModelComparator {
	
	
	
	
	public static void modelComparision(final List<ApplicationModel> modelCollection)
	{
		
		Set<String> visitedModels = new LinkedHashSet<String>();
		//each model with each model
		String baseModelId;
		String toCompareModelId;
		
		for(ApplicationModel baseModel : modelCollection)
		{
			baseModelId = baseModel.id;
			visitedModels.add(baseModelId);
			for(ApplicationModel toCompareModel : modelCollection)
			{
				toCompareModelId = toCompareModel.id;
				
				try{
					if( baseModelId != null && toCompareModelId != null && (!baseModelId.equals(toCompareModelId)) && (!visitedModels.contains(toCompareModelId)))
					{	
						
						ConnectorComparator connectorComparator = new ConnectorComparator(baseModel, toCompareModel);
						connectorComparator.connectorComparision();

					}
				}
				catch(Throwable e)
				{
					e.printStackTrace();
					System.out.println("Exception thrown");
					System.out.println("baseModel: "+ baseModel + "- toCompareModel:"+ toCompareModel);
				}
				
				
			}
			
		}
		
		
	
	}

}
