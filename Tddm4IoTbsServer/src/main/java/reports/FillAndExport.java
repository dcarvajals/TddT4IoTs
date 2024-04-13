/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reports;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import util.DataStatic;


public class FillAndExport {
    private static boolean deploy=true;
    private String idp;
    private Integer type;
    private String path;
    private String pathImage;
    private String nameProject;
    private Boolean part;
    private String cad;
    private String pathdeploy;

    public FillAndExport(String idp, Integer type, String path, String pathImage,String nameProject,Boolean part,String cad,String pathD) {
        this.cad=cad;
        this.idp = idp;
        this.type = type;
        this.path = path;
        this.pathImage = pathImage;
        this.nameProject=nameProject;
        this.part=part;
        this.pathdeploy=pathD;
    }

    public File fillAndExport() {
        try {

            Map param = new HashMap();

            String patheportsjxml=pathImage;
            System.out.println("noooooooooooo");
            System.out.println(patheportsjxml);
            patheportsjxml=patheportsjxml.replace("target","src");
            if (FillAndExport.deploy){
                patheportsjxml=this.pathdeploy;
                patheportsjxml+="areports/";
            }else
                patheportsjxml=patheportsjxml.replace("Tddm4IoTbsServer-1.0-SNAPSHOT","main\\java\\");
            
            param.put("parampath", DataStatic.getLocation(pathImage) + "ImgEnc/imagen.png");
            param.put("cad", cad);

            
            JasperReport report=null;
            if (FillAndExport.deploy)
                report = JasperCompileManager.compileReport(patheportsjxml + (type.equals(1) ? "reportEntregable.jrxml"
                                                                        : type.equals(2) ? "reportComponent.jrxml"
                                                                        : type.equals(3) ? "reportTask.jrxml" : "reportAll.jrxml"));
            else
                report = JasperCompileManager.compileReport(patheportsjxml + "reports/" + (type.equals(1) ? "reportEntregable.jrxml"
                                                                        : type.equals(2) ? "reportComponent.jrxml"
                                                                        : type.equals(3) ? "reportTask.jrxml" : "reportAll.jrxml"));
            
            
            report.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            JasperPrint print = JasperFillManager.fillReport(report, param, ConfigDSource.getData(this.idp, this.type,DataStatic.getLocation(pathImage)+"\\tddm4iotbs_projects\\",this.nameProject,part));
            File file = File.createTempFile("output", "pdf");
            JasperExportManager.exportReportToPdfStream(print, new FileOutputStream(file));
            return file;
        } catch (Exception ex) {
            System.out.println("Error" + ex.getMessage());
        }
        return null;
    }
}
