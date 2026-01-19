package com.ugr.microservices.dependencies.core.tddt4iots.util;

import com.ugr.microservices.dependencies.core.tddt4iots.dto.request.WriteReadFileReq;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@AllArgsConstructor
@Slf4j
public class Tddt4iotsFileAcces {

    public static String readFileText(WriteReadFileReq writeReadFileReq) {
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        String result = "";
        try {
            archivo = new File(writeReadFileReq.getPathAbsolute());
            if (archivo.exists()) {
                fr = new FileReader(archivo);
                br = new BufferedReader(fr);
                String linea;
                while ((linea = br.readLine()) != null) {
                    result += linea + (writeReadFileReq.getContent().equals("t") ? "\\n" : "");
                }
            } else {
                result = "{}";
                log.error("Archivo no existe");
            }
        } catch (Exception e) {
            result = "{}";
        } finally {
            try {
                if (null != fr) {
                    fr.close();
                }
                if (null != br) {
                    br.close();
                }
            } catch (Exception e2) {
                log.error("Error in readFileText.fr.close()");
            }
        }
        return result;
    }

    public static boolean writeFile(WriteReadFileReq writeReadFileReq) {
        FileWriter fichero = null;
        PrintWriter pw = null;
        try {
            fichero = new FileWriter(writeReadFileReq.getPathAbsolute());
            pw = new PrintWriter(fichero);
            pw.println(writeReadFileReq.getContent());
        } catch (Exception e) {
            log.error("Error in save File project");
        } finally {
            try {
                if (null != fichero) {
                    fichero.close();
                }
                if (null != pw) {
                    pw.close();
                }
            } catch (Exception e2) {
                log.error("Error in writeFileText.fichero.close()");
            }
        }
        return true;
    }

}
