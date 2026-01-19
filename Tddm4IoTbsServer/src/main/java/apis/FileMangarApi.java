/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package apis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import util.DataStatic;
import util.FileAccess;
import util.Methods;

/**
 *
 * @author DUVAL CARVAJAL
 */
@Path("fileManagerApi")
public class FileMangarApi {
    
    @Context
    private HttpServletRequest request;
    
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/saveFile")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveFile(String data) throws IOException {
        String menssage = "";
        String path = DataStatic.getLocation(request.getServletContext().getRealPath(""));
        JsonObject Jso = Methods.stringToJSON(data);
        String filePath = Methods.JsonToString(Jso, "filePath", "");
        String fileContent = Methods.JsonToString(Jso, "fileContent", "");
        System.out.println("fileConent: " + fileContent);
        String rutaJson = path + DataStatic.folderJson + filePath;
        saveToFile(fileContent, rutaJson);
        menssage = Methods.getJsonMessage("2", "File successfully updated.", "[]");
        return Response.ok(menssage)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/saveFileTraining")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveFileTraining(String data) throws IOException {
        String menssage = "";
        String path = DataStatic.getLocation(request.getServletContext().getRealPath(""));
        JsonObject Jso = Methods.stringToJSON(data);
        String filePath = Methods.JsonToString(Jso, "filePath", "");
        String fileContent = Methods.JsonToString(Jso, "fileContent", "");
        System.out.println("fileConent: " + fileContent);
        String rutaJson = path + DataStatic.folderTraining + filePath;
        saveToFileJsonL(fileContent, rutaJson);
        menssage = Methods.getJsonMessage("2", "File successfully updated.", "[]");
        return Response.ok(menssage)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @POST
    @Path("/loadFile")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loadFile (String data) {
        String menssage = "";
        FileAccess fac = new FileAccess();
        String path = DataStatic.getLocation(request.getServletContext().getRealPath(""));
        com.google.gson.JsonObject Jso = Methods.stringToJSON(data);
        String filePath = Methods.JsonToString(Jso, "filePath", "");
        String rutaJson = path + DataStatic.folderJson + filePath;
        String templateApplication = fac.readFileText(rutaJson, "f");
        menssage = Methods.getJsonMessage("2", "Datos cargados.", templateApplication);
        return Response.ok(menssage)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-with")
                .build();
    }
    
    private void saveToFile(String content, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(content);
        }
    }
    
    private void saveToFileJsonL(String content, String path) throws IOException {

        // Reemplazar los caracteres de nueva línea en el contenido JSONL para evitar problemas
        String sanitizedContent = content.replace("\\n", "\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(sanitizedContent);
        }
    }

}
