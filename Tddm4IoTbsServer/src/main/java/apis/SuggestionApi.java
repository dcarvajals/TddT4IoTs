/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package apis;

import Controller.PersonaCtrl;
import Controller.SuggestionCtrl;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import models.Suggestion;
import util.Methods;

/**
 *
 * @author USUARIO
 */
@Path("suggestionpis")
public class SuggestionApi 
{
    @Context
    private UriInfo context;
    @Context
    private HttpServletRequest request;
    
    
    SuggestionCtrl sControl;
    PersonaCtrl personCTRL;

    /**
     * Creates a new instance of PersonaApis
     */
    public SuggestionApi() {
        sControl = new SuggestionCtrl();
        personCTRL=new PersonaCtrl();
    }
    
    
    @POST
    @Path("/registerSuggestion")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insertSuggestion(String data) {
        String message;
        System.out.println("insertSuggestion...");
        JsonObject jso = Methods.stringToJSON(data);
        if (jso.size() > 0) {
            String email_person = Methods.JsonToString(jso, "emailperson", "");
            String valoration = Methods.JsonToString(jso, "valoration", "");
            String suggestion_type = Methods.JsonToString(jso, "suggestion_type", "");
            String observation = Methods.JsonToString(jso, "observation", "");
            
            String id_person=personCTRL.emailgetid(email_person);
            System.out.println(id_person+" este es el id de persona");
            Suggestion suggestion=new Suggestion(Integer.parseInt(id_person),
                    Integer.parseInt(valoration),suggestion_type,observation);
          
            String[] res = sControl.SaveSuggestion(suggestion);
            message = Methods.getJsonMessage(res[0], res[1], res[2]);
        } else {
            message = Methods.getJsonMessage("4", "Missing data.", "{}");
        }
        return Response.ok(message)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }
    
    
}
