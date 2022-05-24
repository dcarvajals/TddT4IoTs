/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javax.xml.crypto.Data;

/**
 *
 * @author Usuario
 */
public class DataStatic {

    public static String nameApplication = "Tddm4IoTbs";

    /**
     * aplicaciones.uteq.edue.ec*
     */
//    public static String dbName = "cQf5KIlaaVd6IRjES95RoQ==";
//    public static String dbUser = "9qy+3vHmh8zClkk38dnm3g==";
//    public static String dbPassword = "Xg54kwNTey9YPcFBN6WNWWABv+Q+C4mg4xW9VO+zN7jClkk38dnm3g==";
//    public static String dbPort = "nYsIHU+jDcE=";
//    public static String dbHost = "EjCoYJyzeuQdRCjDM4JrjA==";//remoto
//    
    /**
     * bioforest.uteq.edue.ec*
     */
    public static String dbName = "cQf5KIlaaVd6IRjES95RoQ==";
    public static String dbUser = "9qy+3vHmh8zClkk38dnm3g==";
    public static String dbPassword = "Ok9kbRfRsOrgEkfJ8bouhge5dQQCpnO5ytBA+lbCxOEpTujTi/G1ORNoJ/GC0fI1nqj5cldF+8fwdA8vXBJpGZCZhyGfxpMWiunNJ5XG+y/h+wXA59Lpe+r2O2YPHAVBSy7gzFoLNMM=";
    public static String dbPort = "nYsIHU+jDcE=";
    public static String dbHost = "i+aTFOSJFYo6UcZQmtEsRg=="; //remoto
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

    public static String folderUml = "UmlDiagram/";
    public static String folderEasy = "EasyIoT/";
    public static String folderMvmSpring = "/ProjectMvnSpr/";
    public static String pathTemp = "D:\\REPOSITORIOS\\umleasyiotrep\\storageTddm4IoTbs\\src\\main\\webapp\\";

    public static String folderTemplate = "tddm4iotbs_template/";
    public static String folderMvn = "mvn_config/";
    public static String folferTempateJava = "template_java/";

    public static String getLocation(String context) {
        if (!fileLocation.equals("")) {
            context = fileLocation;
        }
        return context.replace(StringTarget, StringReplacement);
    }
}
