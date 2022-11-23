/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
//import java.io.File;

/**
 *
 * @author tonyp
 */
public class MakerProjects {

    private static String attrs = "";

    public static void createMavenProject(String contextPath, String relPath, String ProjectName1, String info) {

        FileAccess fac = new FileAccess();
        String ProjectName = ProjectName1;
//

        String projectPath = contextPath + relPath;
//        File fil = new File(projectPath);
//
//        fac.deleteDirectoryOrFile(fil);
//        if (!fil.exists()) {
//            if (!fil.mkdir()) {
//                System.out.println("Forzar creación de carpeta");
//                System.out.println(projectPath);
//                fil.mkdirs();
//            }
//        }

        String[] commands = new String[]{
            String.format("cd \"%s\"", (projectPath)),
            String.format("spring init --artifactId=%s --boot-version=2.7.3 --build=maven --dependencies=data-jpa,web,postgresql,ws,lombok --description=\"description %s\" --groupId=com.anth --java-version=1.8 --language=java --name=%s --packaging=war --package-name=com.app.tddtm4iots --version=0.0.1-SNAPSHOT --force \"%s\"", ProjectName, ProjectName, ProjectName, projectPath)
        //String.format("mvn archetype:generate -DgroupId=com.mycompany -DartifactId=%s -DarchetypeArtifactId=maven-archetype-webapp -DinteractiveMode=false", ProjectName)
        };

        // obtener las entities
        JsonObject entitiesJson = Methods.stringToJSON(info);
        JsonArray entitiesJava = Methods.JsonToArray(entitiesJson, "entities");
        JsonArray enumsJava = Methods.JsonToArray(entitiesJson, "enums");
        System.out.println("ENTIDADES PARA GENERAR CODIGO JAVA " + entitiesJava);
        System.out.println("ENUMS PARA GENERAR CODIGO JAVA " + enumsJava);

        System.out.println("#####################################################");
        System.out.println("##         INICIO GENERACIÓN MAVEN PROJECT         ##");
        System.out.println("#####################################################");

        Thread th = new Thread(() -> {
            CommandWindow terminal = new CommandWindow();
            if (terminal.status) {
                String logs = terminal.ejecutarComandos(commands);
                System.out.println(logs);

                String metaFolder = contextPath + DataStatic.folderTemplate + DataStatic.folderMvn;
                String folferTempateJava = contextPath + DataStatic.folderTemplate + DataStatic.folferTempateJava;

                System.out.println(folferTempateJava);

                File tmpFolder;
//                if (!tmpFolder.exists()) {
//                    tmpFolder.mkdirs();
//                }
//                tmpFolder = new File(projectPath + "/" + ProjectName + "/src/main/java");
//                if (!tmpFolder.exists()) {
//                    tmpFolder.mkdir();
//                }

                //Model
                tmpFolder = new File(projectPath + "src\\main\\java\\com\\app\\tddtm4iots\\entities");
                tmpFolder.mkdir();
                // crear clases entidades
                for (int posEntitie = 0; posEntitie < entitiesJava.size(); posEntitie++) {
                    // obtener el json de la posicion respectiva
                    JsonObject entitie = Methods.JsonElementToJSO(entitiesJava.get(posEntitie));
                    String entitieName = Methods.JsonToString(entitie, "className", "");

                    String modifiers = Methods.JsonToString(entitie, "modifiers", "");

                    if (!modifiers.equals("interface") && !entitieName.contains("DAO")) {

                        // buscar la platilla para las entites
                        String templateEntitie = fac.readFileText(folferTempateJava + "entitieTemplate.txt", "f");
                        templateEntitie = templateEntitie.replace("{$s}", "\n");
                        // nombre de la clase
                        templateEntitie = templateEntitie.replace("{$nameClass}", entitieName);
                        System.out.println(templateEntitie);
                        // crear los atributos de las clases
                        JsonArray attributes = Methods.JsonToArray(entitie, "attributes");
                        System.out.println("ATRIBUTOS " + attributes);
                        String nameAttr = "";
                        String imports = "";
                        for (int posAttr = 0; posAttr < attributes.size(); posAttr++) {
                            System.out.println("INDICE " + posAttr);
                            System.out.println("CANTIDAD DE ELEMENMTOS " + attributes.size());
                            JsonObject attribute = Methods.JsonElementToJSO(attributes.get(posAttr));
                            String name = Methods.JsonToString(attribute, "name", "");
                            String primary_key = Methods.JsonToString(attribute, "primary_key", "");
                            String not_null = Methods.JsonToString(attribute, "not_null", "");
                            String length_precision = Methods.JsonToString(attribute, "length_precision", "");
                            String type = Methods.JsonToString(attribute, "type", "");
                            System.out.println(name);

                            if (!name.contains("DAO")) {

                                if (primary_key.equals("true")) {
                                    templateEntitie = templateEntitie.replace("{$idClass}", name);
                                } else {
                                    
                                        if (length_precision.equals("-1")) {
                                            attrs = "    @Column(name = \"" + name + "\", nullable = " + not_null + ", unique = " + primary_key + ") \n";
                                        } else {
                                            attrs = "    @Column(name = \"" + name + "\", nullable = " + not_null + ", unique = " + primary_key + ", length = " + length_precision + ") \n";
                                        }
                                    

                                    if (type.equals("fk")) {
                                        String cardinalidate = Methods.JsonToString(attribute, "cardinalidate", "");
                                        String idToOrFrom = Methods.JsonToString(attribute, "idToOrFrom", "");
                                        if (!idToOrFrom.equals("-1")) {
                                            String[] object = name.split(":");

                                            if (cardinalidate.equals("1..1")) {
                                                attrs = "   @JoinColumn(name = \"" + idToOrFrom + "\", referencedColumnName = \"" + idToOrFrom + "\") \n";
                                                attrs += "   @OneToOne \n";
                                                nameAttr += attrs + "    private " + object[1] + " " + object[0] + "; \n \n";
                                            } else if (cardinalidate.equals("1..*")) {
                                                attrs = "   @OneToMany(mappedBy = \"" + idToOrFrom + "\") \n";
                                                imports += "import java.util.ArrayList;\n";
                                                nameAttr += attrs + "    private ArrayList<" + object[1].replace("[]", "") + "> " + object[0] + "; \n \n";
                                            } else if (cardinalidate.equals("*..1")) {
                                                attrs = "   @JoinColumn(name = \"" + idToOrFrom + "\", referencedColumnName = \"" + idToOrFrom + "\") \n";
                                                attrs += "   @ManyToOne \n";
                                                imports += "import java.util.ArrayList;\n";
                                                nameAttr += attrs + "    private ArrayList<" + object[1].replace("[]", "") + "> " + object[0] + "; \n \n";
                                            }
                                        }

                                    } else if (type.equals("enumeration")) {
                                        String[] object = name.split(":");
                                        attrs = "    // Enumeration \n";
                                        nameAttr += attrs + "    private " + object[1] + " " + object[0] + "; \n \n";
                                    } else {
                                        nameAttr += attrs + "    private " + type + " " + name + "; \n \n";
                                    }
                                }
                            }
                        }
                        templateEntitie = templateEntitie.replace("${imports}", imports);
                        templateEntitie = templateEntitie.replace("{$attrs}", nameAttr);
                        FileAccess classEntitie = new FileAccess();
                        classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddtm4iots\\entities\\" + entitieName + ".java", templateEntitie);
                        System.out.println("PathZip: " + contextPath + relPath);
                        System.out.println("Creaando clase ->" + entitieName + " \n");
                    }
                }

                //Dao
                tmpFolder = new File(projectPath + "\\src\\main\\java\\com\\app\\tddtm4iots\\dao");
                tmpFolder.mkdir();

                // crear las clases dao
                for (int posEntitie = 0; posEntitie < entitiesJava.size(); posEntitie++) {
                    // obtener el json de la posicion respectiva
                    JsonObject entitie = Methods.JsonElementToJSO(entitiesJava.get(posEntitie));
                    String entitieName = Methods.JsonToString(entitie, "className", "");
                    String modifiers = Methods.JsonToString(entitie, "modifiers", "");

                    if (!modifiers.equals("interface") && !entitieName.contains("DAO")) {
                        // buscar la platilla para las entites
                        String templateDao = fac.readFileText(folferTempateJava + "daoTemplate.txt", "f");
                        templateDao = templateDao.replace("{$s}", "\n");
                        // nombre de la clase
                        templateDao = templateDao.replace("{$nameClass}", entitieName);

                        FileAccess classEntitie = new FileAccess();
                        classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddtm4iots\\dao\\" + entitieName + "Dao.java", templateDao);
                        System.out.println("PathZip: " + contextPath + relPath);
                        System.out.println("Creaando clase ->" + entitieName + " \n");
                    }
                }

                //Enum
                tmpFolder = new File(projectPath + "\\src\\main\\java\\com\\app\\tddtm4iots\\enums");
                tmpFolder.mkdir();
                for (int posEnum = 0; posEnum < enumsJava.size(); posEnum++) {
                    // obtener el json de la posicion respectiva
                    JsonObject enumx = Methods.JsonElementToJSO(enumsJava.get(posEnum));
                    String nameEnum = Methods.JsonToString(enumx, "name", "");
                    JsonArray elemeents = Methods.JsonToArray(enumx, "elements");
                    System.out.println("ELEMENTOS " + elemeents);
                    
                    String elementos = "";
                    for(int i = 0; i < elemeents.size(); i++){
                        if(i == elemeents.size() - 1) {
                            elementos += elemeents.get(i);
                        } else {
                            elementos += elemeents.get(i) + ",";
                        }
                    }
                    
                    
                    String templateEnum = fac.readFileText(folferTempateJava + "enumTemplate.txt", "f");
                    templateEnum = templateEnum.replace("{$s}", "\n");
                    templateEnum = templateEnum.replace("{$nameClass}", nameEnum);
                    templateEnum = templateEnum.replace("{$params}", elementos.replaceAll("\"", ""));
                    System.out.println(templateEnum);
                    FileAccess classEntitie = new FileAccess();
                    classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddtm4iots\\enums\\" + nameEnum + ".java", templateEnum);
                    System.out.println("PathZip: " + contextPath + relPath);
                    System.out.println("Creaando enum ->" + enumx + " \n");
                }

                //Apis
                tmpFolder = new File(projectPath + "\\src\\main\\java\\com\\app\\tddtm4iots\\apis");
                tmpFolder.mkdir();

                // crear las clases apis
                for (int posEntitie = 0; posEntitie < entitiesJava.size(); posEntitie++) {
                    // obtener el json de la posicion respectiva
                    JsonObject entitie = Methods.JsonElementToJSO(entitiesJava.get(posEntitie));
                    String entitieName = Methods.JsonToString(entitie, "className", "");
                    String modifiers = Methods.JsonToString(entitie, "modifiers", "");

                    if (!modifiers.equals("interface") && !entitieName.contains("DAO")) {
                        // buscar la platilla para las entites
                        String templateApi = fac.readFileText(folferTempateJava + "apisTemplate.txt", "f");
                        templateApi = templateApi.replace("{$s}", "\n");
                        // nombre de la clase
                        templateApi = templateApi.replace("{$nameClass}", entitieName);
                        templateApi = templateApi.replace("{$nameClassMinuscula}", entitieName.toLowerCase());

                        JsonArray attributes = Methods.JsonToArray(entitie, "attributes");
                        if (attributes.size() > 0) {
                            JsonObject attribute = Methods.JsonElementToJSO(attributes.get(0));
                            String name = Methods.JsonToString(attribute, "name", "");

                            templateApi = templateApi.replace("{$idClass}", name);

                            FileAccess classEntitie = new FileAccess();
                            classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddtm4iots\\apis\\" + entitieName + "Api.java", templateApi);
                            System.out.println("PathZip: " + contextPath + relPath);
                            System.out.println("Creaando clase ->" + entitieName + " \n");
                        }
                    }
                }

//                String metaInf = fac.readFileText(metaFolder + "context_xml.txt", "f");
//                String webXml = fac.readFileText(metaFolder + "web_xml.txt", "f");
//                String poomXml = fac.readFileText(metaFolder + "pom_xml.txt", "f");
//
//                metaInf = metaInf.replace("{$param1}", ProjectName);
//                poomXml = poomXml.replace("{$param1}", ProjectName);
//
//                fac.writeFileText(projectPath + "/" + ProjectName + "/src/main/webapp/META-INF/context.xml", metaInf);
//                fac.writeFileText(projectPath + "/" + ProjectName + "/src/main/webapp/WEB-INF/web.xml", webXml);
//                fac.writeFileText(projectPath + "/" + ProjectName + "/pom.xml", poomXml);
                System.out.println(logs);
                System.out.println("########################  FIN    ###################################");
                System.out.println("Maker Maven: " + projectPath);
//                FileAccess zip = new FileAccess();
//                zip.extracZip(projectPath);
            }
        });
        th.start();
    }

    public static void MaketarMaven(String contextPath, String relPath) {

        String demo = "MavenApplication";
        String path = contextPath + relPath + demo;
        Thread th = new Thread(() -> {
            FileAccess fac = new FileAccess();
            fac.makeZipFromFolder(path, path + ".zip");
            System.out.println("PathZip: " + contextPath + relPath);
        });
        th.start();
    }

    public static void setMavenProject(String contextPath, String relPath, String ProjectName1, String info) {
        FileAccess fac = new FileAccess();
        String ProjectName = fac.cleanFileName(ProjectName1);

        String demo = "MavenApplication";
        String projectPath = contextPath + relPath + demo;

        projectPath += projectPath + "/" + ProjectName;

        //info
        // PROCESA Y GENERA LAS CLASES JAVA DENTRO DEL MAVEN PROJECT
    }
}
