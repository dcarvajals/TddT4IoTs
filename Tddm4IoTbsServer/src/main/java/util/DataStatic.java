/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;
//
import javax.xml.crypto.Data;

/**
 *
 * @author Usuario
 */
public class DataStatic {

    public static String nameApplication = "tddt4iots";

    /**
     * localhost
     */
//    public static String dbName = "tddt4iots";
//    public static String dbUser = "postgres";
//    public static String dbPassword = "carvajal2000";
//    public static String dbPort = "5432";
//    public static String dbHost = "localhost";//local
    
     //app.applications uteq
    
    public static String dbName = "tddt4iots";
    public static String dbUser = "postgres";
    public static String dbPassword = "ACD5254BA9BB71B85948D702E8D9426569F6541BDC569E31B0F0CB25085A3F4F";
    public static String dbPort = "5432";
    public static String dbHost = "82.180.136.74";//remoto
    
//    public static String dbHost = "aplicaciones.uteq.edu.ec";
//    public static String dbName = "tddt4iots";
//    public static String dbPort = "9010";
//    public static String dbPassword = "z8E9bYdQpHmOvtfH6Up5dE1HKCh35pgwlEDuZqMklOtg3Zm2UA";
//    public static String dbUser = "aplicaciones";
    
//    
    /**
     * bioforest.uteq.edue.ec*
     */
//    public static String dbName = "cQf5KIlaaVd6IRjES95RoQ==";
//    public static String dbUser = "9qy+3vHmh8zClkk38dnm3g==";
//    public static String dbPassword = "Ok9kbRfRsOrgEkfJ8bouhge5dQQCpnO5ytBA+lbCxOEpTujTi/G1ORNoJ/GC0fI1nqj5cldF+8fwdA8vXBJpGZCZhyGfxpMWiunNJ5XG+y/h+wXA59Lpe+r2O2YPHAVBSy7gzFoLNMM=";
//    public static String dbPort = "nYsIHU+jDcE=";
//    public static String dbHost = "i+aTFOSJFYo6UcZQmtEsRg=="; //remoto
    /**
     * heroku
     */
//    public static String dbName = "x9E2MQ/IBa6N21A4+XAb+Q==";
//    public static String dbUser = "gKVIS36GBlKKAUY/Xc+Nkw==";
//    public static String dbPassword = "E+PxPSdOqloWDrQyqZ9bpPzQWdreKvjvMDbE+wjUOMxTq+Le0FNEY4cXgXRrGYWRGostrb+dpk/RmfMKDqZDasKWSTfx2ebe";
//    public static String dbPort = "nYsIHU+jDcE=";
//    public static String dbHost = "pEviztVENNXMQuywAnNBlG7/2zO5IVb/E5vmiMG2gxm+0wOla0kEFQ=="; //remoto

    public static String privateKey = "fldsmdfr";

    public static String protocol = "wss";
//    public static String uriWebSockeet = "://localhost:8080";
    public static String uriWebSockeet = "://localhost:443";

    private static String fileLocation = "";

    private static String StringTarget = "Tddm4IoTbsServer";//
    private static String StringReplacement = "storageTddm4IoTbs";

    public static String proyectName = "storageTddm4IoTbs/";

    public static String folderUser = "UserImage/";
    public static String folderProyect = "tddm4iotbs_projects/";
    public static String folderComponent = "tddm4iotbs_components/";

    public static String folderEntregables = "Entregables/";
    public static String folderComponents = "Components/";
    public static String folderTasks = "Tasks/";
    public static String folderUml = "UmlDiagram/";
    public static String folderEasy = "EasyIoT/";
    public static String folderMvmSpring = "SprProject";
    public static String folderAngular = "AngProject";
    //public static String pathTemp = "D:\\Repositorios\\TddT4IoTs\\storageTddm4IoTbs\\src\\main\\webapp\\";

    public static String folderTemplate = "tddm4iotbs_template/";
    public static String folderJson = "tddt4iots_json/";
    public static String folderTraining = "tddt4iotsTrainingFile/";
    public static String folderMvn = "mvn_config/";
    public static String folferTempateJava = "template_java/";
    public static String folferTempateAngular = "template_angular/";
    
    // Parametros para generar proyecto de spring
    public static String VERSION_SPRING = "3.2.0";

    public static String getLocation(String context) {
        if (!fileLocation.equals("")) {
            context = fileLocation;
        }
        return context.replace(StringTarget, StringReplacement);
    }
}
