package gate.liwc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.Utils;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

@CreoleResource(name = "LIWCFeatureExtraction", comment = "Extracts features using LIWC, See for the entire set of features http://receptiviti2.wpengine.com/liwc2015-output-variables/")
public class LIWCFeatureExtraction extends AbstractLanguageAnalyser {
	  private String inputASName;
	  private String outputASName;
	  private String annotationType;
	  private String apiKey;
	  private String secrectApiKey;
	  private String sourceType;

	  public Resource init() throws ResourceInstantiationException {
		  return this;
	  }
	  
	  @Override
	  public void reInit() throws ResourceInstantiationException {
	    init();
	  }

	  public void execute() throws ExecutionException {

		    AnnotationSet originalMarkups = document.getAnnotations(inputASName);

		    AnnotationSet annotations = originalMarkups.get(annotationType);
		    
			String str = Utils.cleanStringFor(document, annotations);
			
			if (str != null) {
				String docName = document.getName();
				try {
					Map<String, Double> features = LIWC.getLIWCFeatures(str, docName, apiKey, secrectApiKey, Integer.parseInt(sourceType));
					
					FeatureMap fm = Factory.newFeatureMap();
					Iterator<String> it = features.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						fm.put(key, features.get(key));
					}
					Utils.addAnn(document.getAnnotations(outputASName), annotations, "liwc", fm);
					
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
	  }
	  
	  @Optional
	  @RunTime
	  @CreoleParameter(defaultValue = "Original markups", comment = "annotation name to be set")
	  public void setInputASName(String inputASName) {
	    this.inputASName = inputASName;
	  }

	  public String getInputASName() {
	    return this.inputASName;
	  }

	  @Optional
	  @RunTime
	  @CreoleParameter(defaultValue = "4", comment = "LIWC text type codes 0-6. 4 means social media (e.g. tweet), for more information see http://www.receptiviti.ai/receptiviti-api-user-manual/")
	  public void setTextSourceType(String sourceType) {
	    this.sourceType = sourceType;
	  }

	  public String getSourceType() {
	    return this.sourceType;
	  }

	  
	  @Optional
	  @RunTime
	  @CreoleParameter(defaultValue = "Tweet", comment = "annotation type to be set")
	  public void setAnnotationType(String annotationType) {
	    this.annotationType = annotationType;
	  }

	  public String getAnnotationType() {
	    return this.annotationType;
	  }


	  @Required
	  @CreoleParameter(defaultValue = "", comment = "api key for using liwc")
	  public void setAPIKey(String apiKey) {
	    this.apiKey = apiKey;
	  }

	  public String getApiKey() {
	    return this.apiKey;
	  }


	  @Required
	  @CreoleParameter(defaultValue = "", comment = "secret api key for using liwc")
	  public void setSecrectAPIKey(String secretApiKey) {
	    this.secrectApiKey = secretApiKey;
	  }

	  public String getSecretApiKey() {
	    return this.secrectApiKey;
	  }

	  
	  @Optional
	  @RunTime
	  @CreoleParameter(comment = "output annotation name to be set")
	  public void setOutputASName(String outputASName) {
	    this.outputASName = outputASName;
	  }

	  public String getOutputASName() {
	    return this.outputASName;
	  }


}
