/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
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
            String.format("spring init --artifactId=%s --boot-version=3.1.2 --build=maven --dependencies=data-jpa,web,%s,ws,lombok --description=\"Description %s\" --groupId=com.tddt4iots --java-version=17 --language=java --name=%s --packaging=war --package-name=com.app.tddt4iots --version=0.0.1-SNAPSHOT --force \"%s\"", ProjectName, dependencieDataBase, ProjectName, ProjectName, projectPath)
        };

        JsonArray entitiesJava = Methods.JsonToArray(entitiesJson, "entities");
        JsonArray enumsJava = Methods.JsonToArray(entitiesJson, "enums");
        JsonArray testJava = Methods.JsonToArray(entitiesJson, "test");
        System.out.println("ENTIDADES PARA GENERAR CODIGO JAVA " + entitiesJava);
        System.out.println("JTESTUNIT PARA CODIGO JAVA " + testJava);
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
            String portApplication = Methods.JsonToString(conectionDataBase, "db_port_app", "");
            String severDataBase = Methods.JsonToString(conectionDataBase, "db_sever", "");
            String db_user = Methods.JsonToString(conectionDataBase, "db_user", "");
            String jdbc = Methods.JsonToString(conectionDataBase, "jdbc", "");
            String create = Methods.JsonToString(conectionDataBase, "create", "");
            String createDrop = Methods.JsonToString(conectionDataBase, "createDrop", "");

            templateApplication = templateApplication.replace("{$s}", "\n");
            templateApplication = templateApplication.replace("{$nameApp}", ProjectName);
            templateApplication = templateApplication.replace("{$pathApp}", ProjectName);
            templateApplication = templateApplication.replace("{$portApp}", portApplication);
            templateApplication = templateApplication.replace("{$jdbcApp}", jdbc);
            templateApplication = templateApplication.replace("{$userdatabase}", db_user);
            templateApplication = templateApplication.replace("{$passworddatabase}", passwordDataBase);
            templateApplication = templateApplication.replace("{$create-drop-table}", create.equals("true") && createDrop.equals("false") ? "create" : create.equals("false") && createDrop.equals("true") ? "create-drop" : "none");

            applicationProperties.writeFileText(projectPath + "\\src\\main\\resources\\application.properties", templateApplication);

            //Model
            tmpFolder = new File(projectPath + "src\\main\\java\\com\\app\\tddt4iots\\entities");
            tmpFolder.mkdir();

            //<editor-fold desc="Crear las entidades">
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

            //Repository
            tmpFolder = new File(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\repository");
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
                    classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\repository\\" + entitieName + "Repository.java", templateDao);
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

            //Controller
            tmpFolder = new File(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\controller");
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
                        classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\controller\\" + entitieName + "Controller.java", templateApi);
                        System.out.println("PathZip: " + contextPath + relPath);
                        System.out.println("Creaando clase ->" + entitieName + " \n");
                    }
                }
            }

            // Servicie
            tmpFolder = new File(projectPath + "src\\main\\java\\com\\app\\tddt4iots\\service");
            tmpFolder.mkdir();
            for (int posEntitie = 0; posEntitie < entitiesJava.size(); posEntitie++) {
                // obtener el json de la posicion respectiva
                JsonObject entitie = Methods.JsonElementToJSO(entitiesJava.get(posEntitie));
                String entitieName = Methods.JsonToString(entitie, "className", "");
                String modifiers = Methods.JsonToString(entitie, "modifiers", "");

                if (!modifiers.equals("interface") && !entitieName.contains("DAO")) {
                    // buscar la platilla para las entites
                    String templateServicie = fac.readFileText(folferTempateJava + "servicieTemplate.txt", "f");
                    templateServicie = templateServicie.replace("{$className}", entitieName);
                    JsonArray methods = Methods.JsonToArray(entitie, "methods");
                    String methodsClass = "";
                    // recorrer los metodos 
                    for (int posMehotd = 0; posMehotd < methods.size(); posMehotd++) {
                        JsonObject methodObject = Methods.JsonElementToJSO(methods.get(posMehotd));
                        String dataType = Methods.JsonToString(methodObject, "type", "");
                        dataType = dataType.equals("Int") ? "int" : dataType;
                        String nameMehotd = Methods.JsonToString(methodObject, "name", "");
                        String visibility = Methods.JsonToString(methodObject, "visibility", "");
                        methodsClass += "\t " + visibility + " " + dataType + " " + nameMehotd;
                        String parametersText = "";
                        // buscar los parametros
                        JsonArray parameters = Methods.JsonToArray(methodObject, "parameters");
                        for (int posParam = 0; posParam < parameters.size(); posParam++) {
                            JsonObject parametersObject = Methods.JsonElementToJSO(parameters.get(posParam));
                            String dataTypeParam = Methods.JsonToString(parametersObject, "type", "");
                            dataTypeParam = dataTypeParam.equals("Int") ? "int" : dataTypeParam;
                            String nameParam = Methods.JsonToString(parametersObject, "name", "");
                            parametersText += (dataTypeParam + " " + nameParam) + (posParam < parameters.size() - 1  ? ", " :"");
                        }
                        
                        if (parametersText.length() > 0) {
                            methodsClass += "(" + parametersText + "); \n \n";
                        } else {
                            methodsClass += "(); \n \n";
                        }
                    }

                    templateServicie = templateServicie.replace("{$s}", "\n");
                    templateServicie = templateServicie.replace("{$className}", entitieName);
                    templateServicie = templateServicie.replace("{$methosClass}", methodsClass);
                    FileAccess classEntitie = new FileAccess();
                    classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\service\\" + entitieName + "Service.java", templateServicie);
                    System.out.println("PathZip: " + contextPath + relPath);
                    System.out.println("Creaando servicie ->" + entitieName + "Servicie \n");
                }
            }

            // Impl
            tmpFolder = new File(projectPath + "src\\main\\java\\com\\app\\tddt4iots\\service\\impl");
            tmpFolder.mkdir();
            for (int posEntitie = 0; posEntitie < entitiesJava.size(); posEntitie++) {
                // obtener el json de la posicion respectiva
                JsonObject entitie = Methods.JsonElementToJSO(entitiesJava.get(posEntitie));
                String entitieName = Methods.JsonToString(entitie, "className", "");
                String modifiers = Methods.JsonToString(entitie, "modifiers", "");

                if (!modifiers.equals("interface") && !entitieName.contains("DAO")) {
                    // buscar la platilla para las entites
                    String templateServicieImpl = fac.readFileText(folferTempateJava + "servicieImplTemplate.txt", "f");
                    templateServicieImpl = templateServicieImpl.replace("{$className}", entitieName);
                    JsonArray methods = Methods.JsonToArray(entitie, "methods");
                    String methodsClass = "";
                    // recorrer los metodos 
                    for (int posMehotd = 0; posMehotd < methods.size(); posMehotd++) {
                        JsonObject methodObject = Methods.JsonElementToJSO(methods.get(posMehotd));
                        String dataType = Methods.JsonToString(methodObject, "type", "");
                        dataType = dataType.equals("Int") ? "int" : dataType;
                        String nameMehotd = Methods.JsonToString(methodObject, "name", "");
                        String visibility = Methods.JsonToString(methodObject, "visibility", "");
                        methodsClass += "\t@Override \n";
                        methodsClass += "\t" +visibility + " " + dataType + " " + nameMehotd;
                        String parametersText = "";
                        // buscar los parametros
                        JsonArray parameters = Methods.JsonToArray(methodObject, "parameters");
                        for (int posParam = 0; posParam < parameters.size(); posParam++) {
                            JsonObject parametersObject = Methods.JsonElementToJSO(parameters.get(posParam));
                            String dataTypeParam = Methods.JsonToString(parametersObject, "type", "");
                            dataTypeParam = dataTypeParam.equals("Int") ? "int" : dataTypeParam;
                            String nameParam = Methods.JsonToString(parametersObject, "name", "");
                            parametersText += (dataTypeParam + " " + nameParam) + (posParam < parameters.size() - 1 ? ", " :"");
                        }
                        if (parametersText.length() > 0) {
                            methodsClass += "(" + parametersText + ") { \n";
                        } else {
                            methodsClass += "() { \n";
                        }
                        
                        methodsClass += "\t \t// Inside this block you can enter your code implementing the business logic you need. \n";
                        
                        if(dataType.equals("void")) {
                            methodsClass += "\t} \n";
                        } else {
                            methodsClass += "\t \t" + dataType + " response = null; \n";
                            methodsClass += "\t \t" + "return response; \n";
                            methodsClass += "\t } \n";
                        }
                    }

                    templateServicieImpl = templateServicieImpl.replace("{$s}", "\n");
                    templateServicieImpl = templateServicieImpl.replace("{$className}", entitieName);
                    templateServicieImpl = templateServicieImpl.replace("{$classNameMinuscula}", makeFirstLetterLowerCase(entitieName));
                    templateServicieImpl = templateServicieImpl.replace("{$methosClass}", methodsClass);
                    FileAccess classEntitie = new FileAccess();
                    classEntitie.writeFileText(projectPath + "\\src\\main\\java\\com\\app\\tddt4iots\\service\\impl\\" + entitieName + "ServiceImpl.java", templateServicieImpl);
                    System.out.println("PathZip: " + contextPath + relPath);
                    System.out.println("Creaando servicieImpl ->" + entitieName + "Servicie \n");
                }
            }

            //</editor-fold>
            //<editor-fold desc="Crear las clases para las test">
            for (int posTest = 0; posTest < testJava.size(); posTest++) {
                // buscar la platilla para las entites
                String templateTestJava = fac.readFileText(folferTempateJava + "testJavaTemplate.txt", "f");
                templateTestJava = templateTestJava.replace("{$s}", "\n");
                JsonObject entitieTest = Methods.JsonElementToJSO(testJava.get(posTest));
                String classTestName = Methods.JsonToString(entitieTest, "class", "") + "AppTest";
                templateTestJava = templateTestJava.replace("${javaTestName}", classTestName);
                // recorrer los metodos por cada clases y conocer sus test
                JsonArray methodsTest = Methods.JsonToArray(entitieTest, "Methods");
                String codeMethods = "";
                for (int posMethd = 0; posMethd < methodsTest.size(); posMethd++) {
                    JsonObject methodObject = Methods.JsonElementToJSO(methodsTest.get(posMethd));
                    JsonArray parametersTestObject = Methods.JsonToArray(methodObject, "parameters");
                    JsonArray returnTestObject = Methods.JsonToArray(methodObject, "return");
                    for (int posParam = 0; posParam < parametersTestObject.size(); posParam++) {
                        String methodName = Methods.JsonToString(methodObject, "NameMethod", "") + "Test" + String.valueOf((posParam + 1));
                        codeMethods += "\t @Test \n";
                        codeMethods += "\t public void " + methodName + "() { \n";
                        JsonObject returnObject = Methods.JsonElementToJSO(returnTestObject.get(posParam));
                        JsonArray parameters = Methods.stringToJsonArray(parametersTestObject.get(posParam).toString());
                        String paramMethodsInvocation = "";
                        for (int posParamMethod = 0; posParamMethod < parameters.size(); posParamMethod++) {
                            JsonObject paramtersObject = Methods.JsonElementToJSO(parameters.get(posParamMethod));
                            String dataType = Methods.JsonToString(paramtersObject, "typeDate", "");
                            dataType = dataType.equals("Int") ? "int" : dataType;
                            String name = Methods.JsonToString(paramtersObject, "name", "");
                            String value = Methods.JsonToString(paramtersObject, "value", "");
                            codeMethods += "\t \t" + dataType + " " + name + " = " + value + "; \n";
                            paramMethodsInvocation += name + (posParamMethod < parameters.size() - 1 ? ", " :"");;

                        }
                        String dataTypeReturn = Methods.JsonToString(returnObject, "type", "");
                        String nameVariableReturn = Methods.JsonToString(returnObject, "value", "");
                        codeMethods += "\t \t" + dataTypeReturn + " expResult" + " = " + nameVariableReturn + "; \n";
                        codeMethods += "\t \t" + dataTypeReturn + " result" + " = instance." + Methods.JsonToString(methodObject, "NameMethod", "") + "(" + paramMethodsInvocation + "); \n";
                        codeMethods += "\t \t //assertEquals(expResult, result);\n"
                                + "\t \t // TODO review the generated test code and remove the default call to fail.\n"
                                + "\t \t if(expResult!=(result))\n"
                                + "\t \t \t System.out.println(\"The test case is a prototype.\");\n"
                                + "\t \t else\n"
                                + "\t \t \t System.out.println(\"The test case is a good prototype!\"); \n"
                                + "\t \t } \n \n";
                    }
                    // crear los archivos respectivos
                    templateTestJava = templateTestJava.replace("{$javaTestName}", classTestName);
                    templateTestJava = templateTestJava.replace("{$javaClassName}", Methods.JsonToString(entitieTest, "class", ""));
                    templateTestJava = templateTestJava.replace("{$methodsTest}", codeMethods);
                    FileAccess classEntitie = new FileAccess();
                    classEntitie.writeFileText(projectPath + "\\src\\test\\java\\com\\app\\tddt4iots\\" + classTestName + ".java", templateTestJava);
                    System.out.println("PathZip: " + contextPath + relPath);
                    System.out.println("Creaando clase Test ->" + classTestName + " \n");
                }
            }
            //</editor-fold>

            System.out.println(logs);
            System.out.println("########################  FIN    ###################################");
            System.out.println("Maker Maven: " + projectPath);

        }

    }
    
    private static String makeFirstLetterLowerCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        char firstLetter = Character.toLowerCase(str.charAt(0));
        return firstLetter + str.substring(1);
    }
    
    public static void MaketarMaven(String contextPath, String relPath) {

        String demo = "MavenApplication";
        String path = contextPath + relPath + demo;
        Thread th = new Thread(() -> {
            FileAccess fac = new FileAccess();
            fac.makeZipFromFolder(contextPath, relPath + ".zip");
            System.out.println("PathZip: " + relPath);
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
