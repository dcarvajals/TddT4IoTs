/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package apis;

import Controller.Entregable_MembersCtrl;
import Controller.PersonaCtrl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import util.Methods;
import util.WeEncoder;

/**
 *
 * @author HECTOR CASANOVA
 */
@Path("members")
public class Entregable_MembersApis {

    private Entregable_MembersCtrl eMembersCtrl;
    
    private PersonaCtrl personCtrl;

    public Entregable_MembersApis() {
        eMembersCtrl = new Entregable_MembersCtrl();
        personCtrl = new PersonaCtrl();
    }

    
    
    
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/saveMemberss")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insertMembersComponent(String data) {
        System.out.println(data);
        String message = "";
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String permit_master = Methods.JsonToString(Jso, "permit_master", "");
            String role = Methods.JsonToString(Jso, "role", "");
            String idtask = Methods.JsonToString(Jso, "idTask", "");
            String status = Methods.JsonToString(Jso, "status", "");
            String emailpp = Methods.JsonToString(Jso, "emailperson", "");
            String id_masterproject = Methods.JsonToString(Jso, "idmasterproject", "");
            
            //email = searchiDPerson(email);
            WeEncoder encoder = new WeEncoder();
            id_masterproject = encoder.textDecryptor(id_masterproject);
            
            if(!permit_master.equals("")){            
                String[] response = eMembersCtrl.insertMembers("<project_entregable_task>\n"
                    + "    <permit_master>" + permit_master + "</permit_master>\n"
                    + "    <id_project_task>" + idtask + "</id_project_task>\n"
                    + "    <role_member>" + role + "</role_member>\n"
                    + "    <status_member>" + status + "</status_member>\n"
                    + "    <personid>"+personCtrl.emailgetid(emailpp)+"</personid>\n" 
                    + "    <id_masterproject>"+ id_masterproject +"</id_masterproject>\n" 
                    + "</project_entregable_task>");
                
                System.out.println("<project_entregable_task>\n"
                    + "    <permit_master>" + permit_master + "</permit_master>\n"
                    + "    <id_project_task>" + idtask + "</id_project_task>\n"
                    + "    <role_member>" + role + "</role_member>\n"
                    + "    <status_member>" + status + "</status_member>\n"
                    + "    <personid>"+personCtrl.emailgetid(emailpp)+"</personid>\n" 
                    + "    <id_masterproject>"+ id_masterproject +"</id_masterproject>\n" 
                    + "</project_entregable_task>");
                
                message = Methods.getJsonMessage(response[0], response[1], response[2]);    
            }
            else{
                message = Methods.getJsonMessage("4", "The email inserted doesn't exists.", "[]");
            }
            

            
        } else {
            message = Methods.getJsonMessage("4", "Missing data.", "[]");
        }

        return Response.ok(message)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }

    
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/updateMembers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateMembersComponent(String data) {
        String message = "";
        JsonObject Jso = Methods.stringToJSON(data);        
        System.out.println("ssssssssssssss");
        if (Jso.size() > 0) {
            String id_task = Methods.JsonToString(Jso, "id_task", "");
            String role_member = Methods.JsonToString(Jso, "role_member", "");            
            String status_member = Methods.JsonToString(Jso, "status_member", "");
            String email_member = Methods.JsonToString(Jso, "email_member", "");
            String email = Methods.JsonToString(Jso, "emailperson", "");
            String id_masterproject = Methods.JsonToString(Jso, "idmasterproject", "");
            String permit_master=Methods.JsonToString(Jso, "permit_master", "");
            
            WeEncoder encoder = new WeEncoder();
            id_masterproject = encoder.textDecryptor(id_masterproject);
            
            String toInsert = "<project_task_member>\n"
                    + "    <role_member>" + role_member + "</role_member>\n"
                    + "    <status_member>" + status_member + "</status_member>\n"
                    + "    <id_task>" + id_task + "</id_task>\n"
                    + "    <email_member>" + email_member + "</email_member>\n"
                    + "    <personid>" +personCtrl.emailgetid(email)+"</personid>\n"
                    + "    <id_masterproject>"+ id_masterproject +"</id_masterproject>\n"
                    +        "<permit_master>" +permit_master+"</permit_master>\n"
                    + "</project_task_member>";
            
            System.out.println("XML A INSERTAR \n " + toInsert);
            
            String[] response = eMembersCtrl.updateMember(toInsert);
                
            message = Methods.getJsonMessage(response[0], response[1], response[2]);               
        } else {
            message = Methods.getJsonMessage("4", "Missing data.", "[]");
        }

        return Response.ok(message)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }

    private String searchiDPerson(String Email) {
        return personCtrl.emailgetid(Email).equals("0") ? "" : personCtrl.emailgetid(Email);
    }

    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/getMembers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getMembersComponent(String data) {
        String message = "";
        
        JsonObject Jso = Methods.stringToJSON(data);
        System.out.println(data);
        if (Jso.size() > 0) {
            String id_component = Methods.JsonToString(Jso, "id_project_task", "");
            String type = Methods.JsonToString(Jso, "type", "");

            String[] response = eMembersCtrl.selectMembersEntregable(type, id_component);
            JsonArray jArr = Methods.stringToJsonArray(response[2]);

            message = Methods.getJsonMessage(response[0], response[1], jArr.toString());

        } else {
            message = Methods.getJsonMessage("4", "Missing data.", "[]");
        }

        return Response.ok(message)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }
    
    
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/selectMembersentregable")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response selectMembersentregable(String data) {
        System.out.println(data);
     
        String message = "";
        JsonObject Jso = Methods.stringToJSON(data);
        String [] data_response=null;
        String id_entregable=Methods.JsonToString(Jso, "id_entregable", "");
        WeEncoder codec = new WeEncoder();
        System.out.println("aaaaaaaaaaaaaaaaaa");
        System.out.println(codec.textDecryptor(id_entregable));
        if (Jso.size() > 0) 
        {
            System.out.println("TO THE FUNCTION: ");
            if(id_entregable != "")     
                data_response=eMembersCtrl.selectMembersEntregablectrl(codec.textDecryptor(id_entregable));
        } else 
            message = Methods.getJsonMessage("4", "Missing data.", "[]");
        
        message = Methods.getJsonMessage("4", data_response[0], data_response[1]);

        return Response.ok(message)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }
    
}
