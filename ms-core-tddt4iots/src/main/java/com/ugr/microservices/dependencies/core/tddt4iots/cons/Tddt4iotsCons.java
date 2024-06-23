package com.ugr.microservices.dependencies.core.tddt4iots.cons;

public abstract class Tddt4iotsCons {
    public static final Integer MISSING_VALUES = 10;
    public static final Integer INFORMATIVE_VALUES = 11;
    public static final Integer EXISTING_VALUES = 12;
    public static final Integer ERROR_PARSE_VALUES = 13;

    public static final int FAILURE_CODE_DEFAULT = 1;
    public static final String FAILURE_STATUS_DEFAULT = "ERROR";
    public static final String USER = "user";
    public static final String ASSISTANT = "assistant";

    // FOLDERS NFS
    public static final String URL_TDDT4IOTS_SERVER = "http://tddt4iots.com/Tddm4IoTbsServer/webapis/";
    public static final String PATH_PROJECTS = "tddm4iotbs_projects/";
    public static final String UML_DIAGRAM = "/UmlDiagram/";
    public static final String PATH_TRAINING_DATA = "tddt4iotsTrainingFile/";
    public static final String URL_SAVE_FILE_TRAINING = "fileManagerApi/saveFileTraining";
    public static final String URL_VALIDATE_TOKEN = "personapis/validateToken";
    public static final String UML_PRO_JSON = "umlPro";
    public static final String TRAINING_DATA = "trainingData";

    // EXTENSIONS
    public static final String JSON = ".json";
    public static final String JSONL = ".jsonl";
}
