package gate.liwc;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.receptiviti.samples.personality.Content;
import com.receptiviti.samples.personality.Person;
import com.receptiviti.samples.personality.Receptiviti;
import com.receptiviti.samples.personality.ReceptivitiExpection;
import java.util.HashMap;
import org.kohsuke.args4j.Option;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.text.MessageFormat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class LIWC {
	
    @Option(name = "-server", usage = "name of server to use")
    private static String server = "https://app.receptiviti.com";

    static <T> T parseResponseBody(HttpMethod httpMethod) throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        TypeReference<T> typeRef = new TypeReference<T>() {};
        
        return mapper.readValue(httpMethod.getResponseBodyAsString(), typeRef);
    }

    static void setAuthenticationHeaders(HttpMethod method, String anAPIKey, String aSecrectKey) {
        method.setRequestHeader("X-API-KEY", anAPIKey);
        method.setRequestHeader("X-API-SECRET-KEY", aSecrectKey);
    }

    public static String getBaseUrl() {
        String providedBaseUrl = server;
        return providedBaseUrl != null ? providedBaseUrl : "https://app.receptiviti.com";
    }

    static String getPersonContentAPIUrl(String personId) {
        return MessageFormat.format("{0}/{1}/contents", getPersonAPIUrl(), personId);
    }

    static String getPersonAPIUrl() {
        return MessageFormat.format("{0}/api/person", getBaseUrl());
    }


    public static Map<String, Double> getLIWCFeatures(String aContent, String anID, String anAPIKey, String aSecrectKey, Integer aSourceType) throws UnsupportedEncodingException, IOException {
        Content content;
        Receptiviti receptiviti;
        Person person;
        Map<String, Double> map = new HashMap<String, Double>();
        content = new Content(aContent, anID, aSourceType);

        if (content.content != null) {
            try {
                receptiviti = new Receptiviti(server, anAPIKey, aSecrectKey);
                person = new Person(content.name, content.name, 0);

                person = receptiviti.GetPersonID(person);
                if (person.getID() == null) {
                    person = receptiviti.SavePerson(person);
                }

                content = receptiviti.AnalyseContent(person, content);

                PostMethod postRequest = new PostMethod(getPersonContentAPIUrl(person.getID()));
                setAuthenticationHeaders(postRequest, anAPIKey, aSecrectKey);

                String responseJsonString = new ObjectMapper().writeValueAsString(content);

                StringRequestEntity requestEntity = new StringRequestEntity(
                        responseJsonString,
                        "application/json",
                        "UTF-8");
                postRequest.setRequestEntity(requestEntity);

                HttpClient client = new HttpClient();
                client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
                client.executeMethod(postRequest);

                String text = postRequest.getResponseBodyAsString();                
                try {
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(text);
                    JSONObject array = (JSONObject) obj;

                    Iterator setIt = array.entrySet().iterator();

                    while (setIt.hasNext()) {
                        Object id = setIt.next();
                        if (!(id.toString().startsWith("liwc_scores"))) {
//                        if (!(id.toString().startsWith("receptiviti_scores") || id.toString().startsWith("liwc_scores"))) {
                            continue;
                        }
                        String strVals[] = id.toString().split("=");
                        if (strVals[1].startsWith("{")) {
                            Object obj2 = parser.parse(strVals[1]);
                            JSONObject array2 = ((JSONObject) obj2);
                            Iterator setIt2 = array2.entrySet().iterator();
                            while (setIt2.hasNext()) {
                                Object id2 = setIt2.next();

                                String vals[] = id2.toString().split("=");
                                if (vals[1].startsWith("{")) {
                                    Object obj3 = parser.parse(vals[1]);
                                    JSONObject array3 = ((JSONObject) obj3);
                                    Iterator setIt3 = array3.entrySet().iterator();
                                    while (setIt3.hasNext()) {
                                        Object id3 = setIt3.next();
                                        String vals2[] = id3.toString().split("=");
                                        map.put(vals2[0] + "_" + strVals[0], Double.parseDouble(vals2[1]));
                                    }
                                } else {
                                    map.put(vals[0] + "_" + strVals[0], Double.parseDouble(vals[1]));
                                }
                            }

                        }
                    }

                } catch (ParseException pe) {
                    System.out.println("position: " + pe.getPosition());
                    System.out.println(pe);
                }

            } catch (UnirestException e) {
                System.err.println("Error communication with Receptiviti: " + e.getMessage());
            } catch (ReceptivitiExpection e) {
                System.err.println("Receptiviti error: " + e.getMessage());
            }
        }
        return map;
    }

    public static void main(String args[]) throws IOException {
        System.out.println(getLIWCFeatures("Mike Brown was staying with his grandmother for the summer, who lived in the community. #Ferguson", "111112", "key1", "key2", 4));

    }


}
