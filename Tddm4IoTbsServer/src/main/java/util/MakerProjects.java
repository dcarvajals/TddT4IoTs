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
 * @author tonyp
 */
public class MakerProjects {

    private static String attrs = "";

    public static void createMavenProject(String contextPath, String relPath, String ProjectName1, String info) {

        FileAccess fac = new FileAccess();
        String ProjectName = ProjectName1;
        String projectPath = contextPath + relPath;

        // obtener las entities
        JsonObject entitiesJson = Methods.stringToJSON(info);
        JsonObject conectionDataBase = Methods.JsonToSubJSON(entitiesJson, "conectionDB");
        // validar que tipo de conexion se va hacer
        String url_data_base = Methods.JsonToString(conectionDataBase, "url_data_base", "");
        String dependencieDataBase = url_data_base.equals("org.postgresql.Driver") ? "postgresql"
                : url_data_base.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver") ? "sqlserver" : "mysql";

        String[] commands = new String[]{
            String.format("cd \"%s\"", (projectPath)),
            String.format("spring init --artifactId=%s --boot-version=2.7.3 --build=maven --dependencies=data-jpa,web,%s,ws,lombok --description=\"Description %s\" --groupId=com.tddt4iots --java-version=1.8 --language=java --name=%s --packaging=war --package-name=com.app.tddt4iots --version=0.0.1-SNAPSHOT --force \"%s\"", ProjectName, dependencieDataBase, ProjectName, ProjectName, projectPath)
        };

        JsonArray entitiesJava = Methods.JsonToArray(entitiesJson, "entities");
        JsonArray enumsJava = Methods.JsonToArray(entitiesJson, "enums");
        System.out.println("ENTIDADES PARA GENERAR CODIGO JAVA " + entitiesJava);
        System.out.println("ENUMS PARA GENERAR CODIGO JAVA " + enumsJava);

        System.out.println("#####################################################");
        System.out.println("##         INICIO GENERACIÓN MAVEN PROJECT         ##");
        System.out.println("#####################################################");

        //Thread th = new Thread(() -> {
        CommandWindow terminal = new CommandWindow();
        if (terminal.status) {
            String logs = terminal.ejecutarComandos(commands);
            System.out.println(logs);

            String metaFolder = contextPath + DataStatic.folderTemplate + DataStatic.folderMvn;
            String folferTempateJava = contextPath + DataStatic.folderTemplate + DataStatic.folferTempateJava;

            System.out.println(folferTempateJava);

            File tmpFolder;

            // application properties
            FileAccess applicationProperties = new FileAccess();
            String templateApplication = fac.readFileText(folferTempateJava + "applicationProTemplate.txt", "f");

            String databaseName = Methods.JsonToString(conectionDataBase, "db_name", "");
            String passwordDataBase = Methods.JsonToString(conectionDataBase, "db_password", "");
            String portDataBase = Methods.JsonToString(conectionDataBase, "db_port", "");
            String severDataBase = Methods.JsonToString(conectionDataBase, "db_sever", "");
            String db_user = Methods.JsonToString(conectionDataBase, "db_user", "");
            String jdbc = Methods.JsonToString(conectionDataBase, "jdbc", "");

            templateApplication = templateApplication.replace("{$s}", "\n");
            templateApplication = templateApplication.replace("{$nameApp}", ProjectName);
            templateApplication = templateApplication.replace("{$pathApp}", ProjectName);
            templateApplication = templateApplication.replace("{$portApp}", portDataBase);
            templateApplication = templateApplication.replace("{$jdbcApp}", jdbc);
            templateApplication = templateApplication.replace("{$userdatabase}", db_user);
            templateApplication = templateApplication.replace("{$passworddatabase}", passwordDataBase);

            applicationProperties.writeFileText(projectPath + "\\src\\main\\resources\\application.properties", templateApplication);

            //Model
            tmpFolder = new File(projectPath + "src\\main\\java\\com\\app\\tddt4iots\\entities");
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
                    boolean tieneid = false;
                    for (int posAttr = 0; posAttr < attributes.size(); posAttr++) {
                        JsonObject attribute = Methods.JsonElementToJSO(attributes.get(posAttr));
                        String name = Methods.JsonToString(attribute, "name", "");
                        String primary_key = Methods.JsonToString(attribute, "primary_key", "");
                        String not_null = Methods.JsonToString(attribute, "not_null", "");
                        String length_precision = Methods.JsonToString(attribute, "length_precision", "");
                        String type = Methods.JsonToString(attribute, "type", "");

                        if (!name.contains("DAO")) {
                            if (primary_key.equals("true") && !tieneid) {
                                if (!name.equals("id")) {
                                    System.out.println("NO TIENE EL ATRIBUTO ID, POR LO TANTO SE LE AGREGARA UNO POR DEFECTO");
                                    templateEntitie = templateEntitie.replace("{$idClass}", "id");
                                    posAttr = -1;
                                    tieneid = true;
                                } else {
                                    templateEntitie = templateEntitie.replace("{$idClass}", name);
                                }
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
                                        if (object.length > 1) {
                                            String nameColumn = "id" + object[1].replace("[]", "");
                                            if (!idToOrFrom.equals("id")) {
                                                idToOrFrom = "id";
                                            }
                                            if (cardinalidate.equals("1..1")) {
                                                attrs = "    @JoinColumn(name = \"" + nameColumn + "\", referencedColumnName = \"" + idToOrFrom + "\") \n";
                                                attrs += "    @OneToOne \n";
                                                nameAttr += attrs + "    private " + object[1] + " " + object[0] + "; \n \n";
                                            } else if (cardinalidate.equals("1..*")) {
                                                attrs = "    @OneToMany(mappedBy = \"" + idToOrFrom + "\") \n";
                                                imports += "import java.util.List;\n";
                                                nameAttr += attrs + "    private List<" + object[1].replace("[]", "") + "> " + object[0] + "; \n \n";
                                            } else if (cardinalidate.equals("*..1")) {
                                                attrs = "    @JoinColumn(name = \"" + nameColumn + "\", referencedColumnName = \"" + idToOrFrom + "\") \n";
                                                attrs += "    @ManyToOne \n";
                                                nameAttr += attrs + "    private " + object[1].replace("[]", "") + " " + object[0] + "; \n \n";
                                            }
                                        }
                                    }

                                } else if (type.equals("enumeration")) {
                                    String[] object = name.split(":");
                                    attrs = "    // Enumeration \n";
                                    nameAttr += attrs + "    private " + object[1] + " " + object[0] + "; \n \n";
                                } else {
                                    if (type.equals("Date")) {
                                        if (!imports.contains("import java.util.Date")) {
                                            imports += "import java.util.Date; \n";
                                            attrs += "    @Temporal(TemporalType.TIMESTAMP) \n";
                                        }
                                    }

                                    nameAttr += attrs + "    private " + type + " " + name + "; \n \n";
                                }
                            }
                        }
                    }
                    templateEntitie = templateEntitie.replace("${imports}", imports);
                    templateEntitie = templateEntitie.replace("{$attrs}", nameAttr);
                    FileAccess classEntitie = new FileAccess();
                    classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\entities\\" + entitieName + ".java", templateEntitie);
                    System.out.println("PathZip: " + contextPath + relPath);
                    System.out.println("Creaando clase ->" + entitieName + " \n");
                }
            }

            //Dao
            tmpFolder = new File(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\dao");
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
                    classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\dao\\" + entitieName + "Dao.java", templateDao);
                    System.out.println("PathZip: " + contextPath + relPath);
                    System.out.println("Creaando clase ->" + entitieName + " \n");
                }
            }

            //Enum
            tmpFolder = new File(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\enums");
            tmpFolder.mkdir();
            for (int posEnum = 0; posEnum < enumsJava.size(); posEnum++) {
                // obtener el json de la posicion respectiva
                JsonObject enumx = Methods.JsonElementToJSO(enumsJava.get(posEnum));
                String nameEnum = Methods.JsonToString(enumx, "name", "");
                JsonArray elemeents = Methods.JsonToArray(enumx, "elements");
                System.out.println("ELEMENTOS " + elemeents);

                String elementos = "";
                for (int i = 0; i < elemeents.size(); i++) {
                    if (i == elemeents.size() - 1) {
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
                classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\enums\\" + nameEnum + ".java", templateEnum);
                System.out.println("PathZip: " + contextPath + relPath);
                System.out.println("Creaando enum ->" + enumx + " \n");
            }

            //Apis
            tmpFolder = new File(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\apis");
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
                        classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\apis\\" + entitieName + "Api.java", templateApi);
                        System.out.println("PathZip: " + contextPath + relPath);
                        System.out.println("Creaando clase ->" + entitieName + " \n");
                    }
                }
            }

            System.out.println(logs);
            System.out.println("########################  FIN    ###################################");
            System.out.println("Maker Maven: " + projectPath);

        }

    }

    public static void MaketarMaven(String contextPath, String relPath) {

        String demo = "MavenApplication";
        String path = contextPath + relPath + demo;
        Thread th = new Thread(() -> {
        FileAccess fac = new FileAccess();
        fac.makeZipFromFolder(contextPath, relPath + ".zip");
            System.out.println("PathZip: " + relPath );
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
