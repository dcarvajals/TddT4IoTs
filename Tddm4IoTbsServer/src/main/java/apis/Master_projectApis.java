/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apis;

import Controller.Master_projectCtrl;
import Controller.PersonaCtrl;
import com.google.gson.JsonObject;
import java.io.UnsupportedEncodingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import util.DataStatic;
import util.Methods;
import util.WeEncoder;

/**
 * REST Web Service
 *
 * @author tonyp
 */
@Path("projects")
public class Master_projectApis {

    @Context
    private UriInfo context;

    @Context
    private HttpServletRequest request;

    private Master_projectCtrl mpControl;

    /**
     * Creates a new instance of ComponentsApis
     */
    public Master_projectApis() {
        mpControl = new Master_projectCtrl();
    }

    /**
     * This is the web service that allows you to obtain the projects according
     * to the required parameters.
     *
     * @param data String type variable, receives a json with the necessary data
     * (one of them is the session token).
     * @return Returns a json with the results of the processes of the
     * Controller's getProjects function
     */
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/getProjects")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getProjects(String data) {
        String message;
        System.out.println("getProjects()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String type = Methods.JsonToString(Jso, "type", "");
            String[] clains = Methods.getDataToJwt(sessionToken);
            System.out.println("clains 0 => " + clains[0]);
            System.out.println("clains 1 => " + clains[1]);
            //clains[0] = "1";/*BORRARRRR*/

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.selectProjects(type, clains[0]);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/getProject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getProjectById(String data) {
        String message;
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {                
           String id_master_project = Methods.JsonToString(Jso, "idmasterproject", "");
           
            WeEncoder encoder = new WeEncoder();
            id_master_project = encoder.textDecryptor(id_master_project);
            
           String resp = mpControl.selectProjectById(id_master_project);
           message = resp; 
       } else {
            message = Methods.getJsonMessage("4", "Missing data.", "[]");
        }
        return Response.ok(message)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }
    /**
     * This is the web service that allows you to start the module.
     *
     * @param data String type variable, receives a json with the necessary data
     * (one of them is the session token).
     * @return Returns a json with the results of the processes of the
     * Controller's saveComponent function.
     */
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/saveComponent")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response initProjectModule(String data) {
        String message;
        System.out.println("initProjectModule()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String name_mp = Methods.JsonToString(Jso, "nameProject", "");
            String description_mp = Methods.JsonToString(Jso, "description", "");

            String ruta = DataStatic.getLocation(request.getServletContext().getRealPath(""));

            System.out.println(ruta);

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.insertProject(name_mp, description_mp, clains[0], ruta);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/updateInfoProject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateInfoProject(String data) {
        String message;
        System.out.println("updateInfoProject()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String idproj = Methods.JsonToString(Jso, "idproj", "");
            String name_mp = Methods.JsonToString(Jso, "nameProject", "");
            String description_mp = Methods.JsonToString(Jso, "description", "");

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.UpdateInfoProject(name_mp, description_mp, idproj, clains[0]);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/updateModule")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateModule(String data) {
        String message;
        System.out.println("updateModule()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String idproj = Methods.JsonToString(Jso, "idproj", "");
            String module = Methods.JsonToString(Jso, "module", "");
            String state = Methods.JsonToString(Jso, "state", "");

            String rute = DataStatic.getLocation(request.getServletContext().getRealPath(""));
            
            System.out.println("project Path: " + rute);

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.updateModel(idproj, module, state, clains[0], rute);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/getModules")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getModules(String data) {
        String message;
        System.out.println("getModules()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String folder = Methods.JsonToString(Jso, "folder", "");
            String stateUml = Methods.JsonToString(Jso, "stateUml", "");
            String stateIoT = Methods.JsonToString(Jso, "stateIoT", "");

            String rute = DataStatic.getLocation(request.getServletContext().getRealPath(""));

            System.out.println("project Path: " + rute);

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.getModules(folder, stateUml, stateIoT, rute);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/deleteProject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteProject(String data) {
        String message;
        System.out.println("deleteProject()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String idproj = Methods.JsonToString(Jso, "idproj", "");

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.deleteProject(idproj, clains[0]);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/deleteModules")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteModules(String data) {
        String message;
        System.out.println("deleteModules()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String idproj = Methods.JsonToString(Jso, "idproj", "");
            String type = Methods.JsonToString(Jso, "type", "");

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.deleteModules(idproj, type, clains[0]);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/getHome")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getHome(String data) {
        String message;
        System.out.println("getHome()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.getHome(clains[0]);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/saveModuleFile")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveModuleFile(String data) {
        String message;
        System.out.println("saveModuleFile()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String idproj = Methods.JsonToString(Jso, "idproj", "");
            String dataJson = Methods.JsonToString(Jso, "dataJson", "");

            String module = Methods.JsonToString(Jso, "module", "");

            String rute = DataStatic.getLocation(request.getServletContext().getRealPath(""));
            //String rute = DataStatic.pathTemp;

            System.out.println("project Path: " + rute);

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.saveJsonModule(clains[0], idproj, module, rute, dataJson);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/loadModuleFile")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loadModuleFile(String data) throws UnsupportedEncodingException {
        String message;
        System.out.println("loadModuleFile()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String idproj = Methods.JsonToString(Jso, "idproj", "");
            String module = Methods.JsonToString(Jso, "module", "");
            String rute = DataStatic.getLocation(request.getServletContext().getRealPath(""));

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.loadJsonModule(clains[0], idproj, module, rute);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/selectHomeProject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response selectHomeProject(String data) {
        String message;
        System.out.println("selectHomeProject()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String[] clains = Methods.getDataToJwt(sessionToken);

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.selectHomeProject();
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/MavenProject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response MavenProject(String data) {
        String message;
        System.out.println("MavenProject()");
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");

            String idProj = Methods.JsonToString(Jso, "idProj", "");
            String module = Methods.JsonToString(Jso, "module", "");
            String info = Methods.JsonToString(Jso, "info", "");

            String path = DataStatic.getLocation(request.getServletContext().getRealPath(""));
            //String path = DataStatic.pathTemp;

            String[] clains = Methods.getDataToJwt(sessionToken);
            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.MavenProject(clains[0], idProj, module, path, info);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/angularProject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response angularProject(String data) {
        String message;
        System.out.println("angularProject()");
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");

            String idProj = Methods.JsonToString(Jso, "idProj", "");
            String module = Methods.JsonToString(Jso, "module", "");
            String info = Methods.JsonToString(Jso, "info", "");

            String path = DataStatic.getLocation(request.getServletContext().getRealPath(""));
            //String path = DataStatic.pathTemp;

            String[] clains = Methods.getDataToJwt(sessionToken);
            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.angularProject(clains[0], idProj, module, path, info);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/selectMembers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response selectMembers(String data) {
        String message;
        System.out.println("selectMembers()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String idProj = Methods.JsonToString(Jso, "idproj", "");
            
            //Decrypt the id of the project
            WeEncoder codec = new WeEncoder();
            idProj = codec.textDecryptor(idProj);                    
            
            String[] clains = Methods.getDataToJwt(sessionToken);

            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.selectMembersInProject(idProj);
                message = Methods.getJsonMessage("2", "The members was loaded successfully.", res[0]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/shareProject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response shareProject(String data) {
        String message;
        System.out.println("shareProject()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");

            String emailShare = Methods.JsonToString(Jso, "emailShare", "");
            String idproj = Methods.JsonToString(Jso, "idproj", "");
            String permit = Methods.JsonToString(Jso, "permit", "");
            String stateShare = Methods.JsonToString(Jso, "stateShare", "");

            String[] clains = Methods.getDataToJwt(sessionToken);
            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.shareProject(clains[0], emailShare, idproj, permit, stateShare);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/shareProjectComplete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response shareProjectComplete(String data) {
        String message;
        System.out.println("shareProjectComplete()");
        
        //Transform the string input into a json object
        JsonObject Jso = Methods.stringToJSON(data);
        
        if (Jso.size() > 0) {
            
            //Flag to check if the data is correct
            Boolean isCorrectUserToShare = true;
            
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String idproj = Methods.JsonToString(Jso, "idproj", "");
            
            String emailShare = Methods.JsonToString(Jso, "email", "");
            String permit = Methods.JsonToString(Jso, "permit", "");
            String stateShare = Methods.JsonToString(Jso, "stateShare", "");
            
            String shared_user_name = Methods.JsonToString(Jso, "name_person", "");
            String shared_user_last_name = Methods.JsonToString(Jso, "lastname_person", "");
                        
            //Check if the actual user have permission to execute this function
            String[] clains = Methods.getDataToJwt(sessionToken);
            String[] validPermissions = Methods.validatePermit(clains[0], clains[1], 1);
            
            //Check if the email of the user to add exists
            PersonaCtrl personaCtrl = new PersonaCtrl();
            String [] personChecking = personaCtrl.personExists(emailShare);
            
            //If is not equal to two that means the person don't exists
            if (!personChecking[0].equals("2")){
                
                //Check if in the json recieved are the person name and last name
                if (shared_user_name.equals("") || shared_user_last_name.equals("")) {
                    //ERROR INCOMPLETE DATA
                    isCorrectUserToShare = false;
                    
                    message = Methods.getJsonMessage("4", 
                            "The user don't exists, please send the names and last names.", 
                            "[]");                    
                }
            }
            
            if (validPermissions[0].equals("2")) {
                JsonObject jsonUserValid = new JsonObject();
                jsonUserValid.addProperty("valid_user", isCorrectUserToShare);
                    
                if (isCorrectUserToShare){
                    //Replace the values with the simplified value
                    Jso = Methods.replacePropertyJson(Jso, "permit", mpControl.parseSharePermit(permit));
                    Jso = Methods.replacePropertyJson(Jso, "stateShare", mpControl.parseShareState(stateShare));

                    //Change the id encrypted of the project
                    WeEncoder codec = new WeEncoder();
                    Jso = Methods.replacePropertyJson(Jso, "idproj", codec.textDecryptor(idproj));
                    
                    //Create a random password to the new user who is going to be inserted
                    String result = java.util.UUID.randomUUID().toString();
                    String password = result.substring(0, 5);                    
                    String code_verificacion = codec.getEmailCode();
                    
                    Jso.addProperty("password", password);
                    Jso.addProperty("codeverificacion", code_verificacion);
                    Jso.addProperty("pathimg_person", (emailShare + ".png"));
                    
                    //Send the data to the database to execute the function
                    System.out.println("THE PASSWORD " + password);
                    System.out.println(Jso.toString());
                    String [] response = mpControl.shareProjectComplete(Jso.toString());

                    message = Methods.getJsonMessage(response[0], response[1], response[2]);
                } else {                    
                    message = Methods.getJsonMessage("4", "The user don't exists, please write the names.", jsonUserValid.toString());
                }                
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/listShareProject")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listShareProject(String data) {
        String message;
        System.out.println("listShareProject()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String typeSelect = Methods.JsonToString(Jso, "typeSelect", "");
            String idPropject = Methods.JsonToString(Jso, "idProject", "");

             String[] clains = Methods.getDataToJwt(sessionToken);
            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.listShareProject(clains[0], typeSelect, idPropject);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/aceptInvitation")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aceptInvitation(String data) {
        String message;
        System.out.println("aceptInvitation()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
            String sessionToken = Methods.JsonToString(Jso, "user_token", "");
            String idProject = Methods.JsonToString(Jso, "idProject", "");
            String permit = Methods.JsonToString(Jso, "permit", "");
            String joinActive = Methods.JsonToString(Jso, "joinActive", "");

             String[] clains = Methods.getDataToJwt(sessionToken);
            String[] res = Methods.validatePermit(clains[0], clains[1], 1);
            if (res[0].equals("2")) {
                res = mpControl.aceptInvitation(idProject, permit, joinActive);
                message = Methods.getJsonMessage(res[0], res[1], res[2]);
            } else {
                message = Methods.getJsonMessage("4", "Error in the request parameters.", "[]");
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
    @Path("/getProjectProperty")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getProjectProperty(String data) {
        String message;
        
        PersonaCtrl person =new PersonaCtrl();
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {                
           String emailperson = Methods.JsonToString(Jso, "emailperson", "");
           String idmasterproject = Methods.JsonToString(Jso, "idmasterproject", "");
            
           String resp = mpControl.getPropertyProject(person.emailgetid(emailperson), idmasterproject);
           resp=resp.substring(1, resp.length()-1);
           message = resp; 
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
    @Path("/shareProjectMembers")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response shareProjectMembers(String data) {
        String message;
        System.out.println("shareProject()");
        
        JsonObject Jso = Methods.stringToJSON(data);
        if (Jso.size() > 0) {
//            String SsessionToken = Methods.JsonToString(Jso, "user_token", "");
            String emailShare = Methods.JsonToString(Jso, "emailShare", "");
            String idproj = Methods.JsonToString(Jso, "idproj", "");
            String permit = Methods.JsonToString(Jso, "permit", "");
            String stateShare = Methods.JsonToString(Jso, "stateShare", "");

      
            String[] res = mpControl.shareProject("null", emailShare, idproj, permit, stateShare);
            message = Methods.getJsonMessage(res[0], res[1], res[2]);
        
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
    @Path("/getGanttObjects")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getGanttObjects(String data) {
        String message;
        JsonObject Jso = Methods.stringToJSON(data);
        System.out.println(data);
        if (Jso.size() > 0) {                
            String id_master_project = Methods.JsonToString(Jso, "idmasterproject", "");
           
            String [] res = mpControl.getGanttObjects(id_master_project);
            message = Methods.getJsonMessage(res[0], res[1], res[2]);
       } else {
            message = Methods.getJsonMessage("4", "Missing data.", "[]");
        }
        return Response.ok(message)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }
}

