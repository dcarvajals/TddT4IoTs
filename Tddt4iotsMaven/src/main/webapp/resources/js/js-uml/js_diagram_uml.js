/* global angular, Swal, rutasStorage, UMLUseCaseDiagram, UMLClassDiagram, UMLSequenceDiagram, UMLCommunication, UMLGeneralization, UMLExtend, UMLInclude, XMLSerializer, urlWebServicies, swal, JSONE, elementsClass, store */

app = angular.module('app', []);
controller = app.controller("workAreaController", function ($scope) {

    // expandir el controlador a otro script js
    app.expandControllerA($scope);

    // expandir el controlador al archivo de socket
    app.expandControllerSocket($scope);

    // expandir el controlador al script de maven project
    app.expandControllerMavenProject($scope);

    // expandir el controlador al script de angular project
    app.expandControllerAngularProject($scope);

    $scope.DatoUsuario = {};
    $scope.rutaImgUser = location.origin + rutasStorage.imguser;
    $scope.notifications = [];
    $scope.activateOpenAi = false;
    $scope.showMessageOpenAi = false;
    $scope.aiRequestSerial = 0;
    $scope.jsonDiagramClassIA = {
        diagram: [],
        xmldiagram: "",
        relationships: [],
        action: [],
        notifications: []
    };
    $scope.natural_text = [];
    $scope.workspaceUserName = user => {
        const fullName = user ? [user.name_person, user.lastname_person].filter(Boolean).join(" ").trim() : "";
        return fullName || (user && user.email_person) || "Application user";
    };
    $scope.workspaceUserInitials = user => {
        const initials = user ? [user.name_person, user.lastname_person].filter(Boolean)
            .map(value => String(value).trim().charAt(0)).join("").slice(0, 2) : "";
        return initials.toUpperCase() || "US";
    };
    $scope.workspaceUserRole = user => user && user.type_person === "R" ? "Root administrator" :
        user && user.type_person === "A" ? "Administrator" : user && user.type_person === "U" ? "User" : "Inactive";

    $(function () {
        $('[data-toggle="tooltip"]').tooltip();
    });

    $scope.showLS = false;

    // json para almacenar los datos del diagrama de caso de uso
    $scope.jsonUseCase = {
        "useCase": [],
        "actors": [],
        "relations": []
    };
    // vector para controlar los actores que seran agregados automaticamente por los casos de uso
    $scope.actorsUseCase = [];

    // json para el escenario principal y el flujo alternativo de la descripcion del caso de uso
    $scope.manager_maf = {
        "main_stage": [],
        "alternative_flow": []
    };
    $scope.alternative_outcome = "return";

    // json para el diagrama de clases
    $scope.jsonClass = {};

    //variable para activar la edicion de los objetos
    $scope.flag_update = false;

    //objeto para obtener el objeto que se va a selelccionar
    $scope.object_update = [];

    //posicion de la descripcion del de caso de uso que se va editar
    $scope.encontrado = -1;

    //objeto para editar las acciones de la descripcion de los casos de uos
    $scope.update_action = {};

    //botones para interactuar entre los diagramas
    $scope.diagramsUml = [];

    $scope.btnDiagramUml = [];

    // Workspace multi-diagram. The active entry is also exposed through the
    // legacy jsonUseCase/diagramUseCase variables so the UML library and the
    // existing editor commands can continue working unchanged.
    $scope.useCaseDiagrams = [];
    $scope.activeUseCaseDiagram = null;
    $scope.modelExplorerCollapsed = false;
    $scope.toolRailCollapsed = false;

    $scope.showTool = "UMLUseCaseDiagram";
    $scope.canvasZoom = Math.round((window.umlWorkspaceZoom || 1) * 100);
    $scope.classLayoutMode = "orthogonal";

    $scope.setCanvasZoom = (percent) => {
        const normalized = Math.max(25, Math.min(150, Math.round(percent / 5) * 5));
        $scope.canvasZoom = normalized;
        window.umlWorkspaceZoom = normalized / 100;
        const activeButton = $scope.btnDiagramUml.find(button => button.status === "O");
        if (activeButton && activeButton.diagram && typeof activeButton.diagram.setZoom === "function") {
            const container = activeButton.diagram._div;
            const previousZoom = activeButton.diagram.getZoom();
            const centerX = container ? (container.scrollLeft + container.clientWidth / 2) / previousZoom : 0;
            const centerY = container ? (container.scrollTop + container.clientHeight / 2) / previousZoom : 0;
            activeButton.diagram.setZoom(window.umlWorkspaceZoom);
            activeButton.diagram.draw();
            if (container) {
                container.scrollLeft = Math.max(0, centerX * activeButton.diagram.getZoom() - container.clientWidth / 2);
                container.scrollTop = Math.max(0, centerY * activeButton.diagram.getZoom() - container.clientHeight / 2);
            }
            $scope.canvasZoom = Math.round(activeButton.diagram.getZoom() * 100);
        }
    };

    $scope.zoomCanvasIn = () => $scope.setCanvasZoom($scope.canvasZoom + 10);
    $scope.zoomCanvasOut = () => $scope.setCanvasZoom($scope.canvasZoom - 10);
    $scope.applyClassLayout = mode => {
        if (typeof applyClassDiagramLayout !== "function" || !diagramClass || !diagramClass._nodes.length) return;
        const result = applyClassDiagramLayout(mode);
        $scope.classLayoutMode = result.mode;
        $scope.canvasZoom = Math.round(diagramClass.getZoom() * 100);
    };
    $scope.resetCanvasZoom = () => {
        const activeButton = $scope.btnDiagramUml.find(button => button.status === "O");
        if (activeButton && activeButton.type === "UMLClassDiagram" && typeof activeButton.diagram.fitToContent === "function") {
            const fittedZoom = activeButton.diagram.fitToContent(0.25, 0.85);
            activeButton.diagram.draw();
            $scope.canvasZoom = Math.round(fittedZoom * 100);
            return;
        }
        $scope.setCanvasZoom(100);
    };

    $scope.notifications = [];
    $scope.viewConsole = false;

    //datos para el diagrama de secuencia
    $scope.secuenceData = {
        package: [],
        secuence: {normal: [], loop: {}, alternative: {condition_if: "", else: "", mr_if: [], mr_else: [], index: ""}}
    };

    $scope.dataProject = {}; // datos del proyecto abierto

    var id_project = "";

    $scope.selectedClass = null;

    //########################## variables de dibujo ###################################

    //########################## variables de dibujo ###################################

    // funcion que se ejecuta al cargar la pagina e inicializa el diagrama de caso de uso
    $(document).ready(function () {
        if (window.location.search !== "") {
            loading();
            let urlParams = new URLSearchParams(window.location.search);
            id_project = urlParams.get('identifiquer');
            $scope.$apply(() => {
                $scope.DatoUsuario = getDataSession();
                $scope.showLS = false;
            });
            $scope.natural_text = [];
            store.session.set("natural_text", $scope.natural_text);
            store.session.set("jsonClass", []);
            $scope.loadDataProject(id_project, $scope.DatoUsuario.user_token);
            $scope.natural_text = store.session.get("natural_text");
        }
    });

    $scope.actualizarNaturalText = (jsonClass) => {
        store.session.set("natural_text", []);
        let descriptionUseCase = "";
        let useCase = getAllUseCases();
        for (let x = 0; x < useCase.length; x++) {
            let usecase_name = useCase[x].name;
            let description_interpret = useCase[x].description_interpret;
            let usecase_postcondition_i = useCase[x].post_condition;
            let usecase_precondition_i = useCase[x].pre_condition;
            let usecase_porpuse_i = useCase[x].porpuse;

            descriptionUseCase += usecase_name === undefined ? "" : usecase_name + ", ";
            descriptionUseCase += usecase_porpuse_i === undefined ? "" : usecase_porpuse_i + ", ";
            descriptionUseCase += usecase_precondition_i === undefined ? "" : usecase_precondition_i + ", ";
            descriptionUseCase += usecase_postcondition_i === undefined ? "" : usecase_postcondition_i + ", ";
            descriptionUseCase += description_interpret === undefined ? "" : description_interpret + ", ";

            for (let y = 0; y < useCase[x].main_stage.length; y++) {
                // action_original
                let action_original = useCase[x].main_stage[y].action_original;
                descriptionUseCase += action_original + ", ";
            }

            for (let z = 0; z < useCase[x].alternative_flow.length; z++) {
                // action_original
                let action_original = useCase[x].alternative_flow[z].action_original;
                let condition = useCase[x].alternative_flow[z].condition;
                descriptionUseCase += condition === undefined ? "" : condition + ", ";
                descriptionUseCase += action_original + ", ";
            }
        }
        $scope.natural_text.push({
            text: descriptionUseCase,
            classDiagram: Object.assign({}, jsonClass)
        });
        console.log("diagrama class", $scope.natural_text);
        store.session.set("natural_text", $scope.natural_text);
        // obtener el diagrama de clases guardado en la ultima modificacion
        store.session.set("jsonClass", Object.assign({}, jsonClass));
    };

    $scope.activateOpenAiApi = () => {
        let dataUser = store.session.get("user_tddm4iotbs");
        const requestedStatus = $scope.activateOpenAi;
        let api_param = {
            idmasterproject: $scope.dataProject.id_masterproject,
            status_openai: requestedStatus ? "T" : "F"
        };
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/activateOpenAi',
                data: JSON.stringify({
                    ...api_param
                }),
                beforeSend: function (xhr) {
                    $scope.$evalAsync(() => {
                        $scope.aiToggleSaving = true;
                    });
                },
                success: function (data) {
                    $scope.$apply(() => {
                        $scope.aiToggleSaving = false;
                        $scope.dataProject.status_openai = requestedStatus ? "T" : "F";
                        if (!requestedStatus) $scope.aiRequestSerial++;
                        superInterpret();
                        if (!requestedStatus) {
                            updateClassDiagram($scope.jsonClass, "U");
                        }
                    });
                    alertAll(data);
                },
                error: function (objXMLHttpRequest) {
                    $scope.$applyAsync(() => {
                        $scope.aiToggleSaving = false;
                        $scope.activateOpenAi = !requestedStatus;
                    });
                    alertAll({status: 4, information: "The analysis mode could not be changed. Please try again."});
                }
            });
        } else {
            location.href = "index.html";
        }
    };

    $scope.showHideConsole = () => {
        $scope.viewConsole = !$scope.viewConsole;
    }

    $scope.loadDataProject = (idProject, user_token) => {
        let dataLoadDataProject = {
            "user_token": user_token,
            "typeSelect": "4",
            "idProject": idProject
        };
        wsLoadDataProject(dataLoadDataProject);
    };

    /**
     * FUNCION PARA CARGAR LOS DATOS DEL PROYECTO
     * */
    $scope.nameProject = "";
    let wsLoadDataProject = (api_param) => {
        let dataUser = store.session.get("user_tddm4iotbs");
        if (dataUser !== undefined && dataUser !== null) {
            $.ajax({
                method: "POST",
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                url: urlWebServicies + 'projects/listShareProject',
                data: JSON.stringify({
                    "user_token": dataUser.user_token,
                    ...api_param
                }),
                beforeSend: function (xhr) {
                    //loading();
                },
                success: function (data) {
                    //swal.close();
                    console.log(data);
                    $scope.$apply(() => {
                        $scope.dataProject = data.data[0];
                        $scope.nameProject = $scope.dataProject.name_mp;
                        $scope.activateOpenAi = $scope.dataProject.status_openai === 'T';

                        initDiagramUml(function () {
                            $scope.loadUseCase();
                        });
                        $scope.loadInterpret();
                        $scope.initCode();
                        // conectar los usuarios por websocket
                        $scope.webSocketInit(id_project);
                    });
                    alertAll(data);
                },
                error: function (objXMLHttpRequest) {
                    console.log("error: ", objXMLHttpRequest);
                }
            });
        } else {
            location.href = "index.html";
        }
    };

    const workspaceId = (prefix) => prefix + "-" + Date.now().toString(36) + "-" + Math.random().toString(36).slice(2, 8);

    const blankUseCaseData = () => ({useCase: [], actors: [], relations: []});

    const registerUseCaseDiagram = (button, data, metadata) => {
        const entry = {
            id: metadata && metadata.id ? metadata.id : workspaceId("ucd"),
            name: metadata && metadata.name ? metadata.name : button.name,
            diagram: button.diagram,
            data: data || blankUseCaseData(),
            button: button,
            expanded: true,
            sequencesExpanded: true
        };
        button.workspaceId = entry.id;
        button.name = entry.name;
        $scope.useCaseDiagrams.push(entry);
        return entry;
    };

    const activateUseCaseEntry = (entry) => {
        if (!entry) return;
        $scope.activeUseCaseDiagram = entry;
        $scope.jsonUseCase = entry.data;
        diagramUseCase = entry.diagram;
    };

    const getAllUseCases = () => {
        if (!$scope.useCaseDiagrams.length) return $scope.jsonUseCase.useCase || [];
        return $scope.useCaseDiagrams.reduce((all, entry) => all.concat(entry.data.useCase || []), []);
    };

    const emptyClassDiagramData = () => ({
        diagram: [],
        xmldiagram: "",
        relationships: [],
        action: [],
        notifications: []
    });

    const symbolExpression = /(\([^()]+\)|\*?¡[^!]+!|\*?¿[^?]+\?)/;

    const hasRecognizedModelData = text => {
        if (typeof text !== "string" || !symbolExpression.test(text)) return false;
        try {
            const parsed = getHackDiagram(text)[1];
            const hasDiagramElement = (parsed.diagram || []).some(item =>
                (item.class && item.class.length) || (item.enums && item.enums.length));
            return Boolean(hasDiagramElement || (parsed.relationships && parsed.relationships.length));
        } catch (error) {
            return false;
        }
    };

    $scope.caseWorkspaceImport = {
        fileName: "",
        payload: null,
        summary: null,
        errors: [],
        warnings: []
    };
    $scope.documentWorkspace = {description: "", files: [], errors: [], loading: false};

    $scope.openDocumentWorkspace = () => {
        $scope.documentWorkspace = {description: "", files: [], errors: [], loading: false};
        const input = document.getElementById("naturalLanguageDocuments");
        if (input) input.value = "";
        $("#modal_generate_case_workspace").modal("show");
    };

    $scope.handleNaturalLanguageDocuments = input => {
        const selected = Array.from(input && input.files ? input.files : []);
        const allowed = /\.(txt|pdf|doc|docx)$/i;
        selected.forEach(file => {
            if (!allowed.test(file.name)) {
                $scope.$applyAsync(() => $scope.documentWorkspace.errors.push(file.name + ": unsupported format."));
                return;
            }
            if (file.size > 10 * 1024 * 1024) {
                $scope.$applyAsync(() => $scope.documentWorkspace.errors.push(file.name + ": exceeds the 10 MB limit."));
                return;
            }
            if ($scope.documentWorkspace.files.some(existing => existing.name === file.name && existing.size === file.size)) return;
            const reader = new FileReader();
            reader.onload = event => $scope.$applyAsync(() => {
                $scope.documentWorkspace.files.push({
                    name: file.name,
                    contentType: file.type || "application/octet-stream",
                    size: file.size,
                    sizeLabel: file.size < 1024 * 1024 ? Math.ceil(file.size / 1024) + " KB" : (file.size / 1024 / 1024).toFixed(1) + " MB",
                    base64Content: String(event.target.result).split(",")[1]
                });
            });
            reader.onerror = () => $scope.$applyAsync(() => $scope.documentWorkspace.errors.push(file.name + ": could not be read."));
            reader.readAsDataURL(file);
        });
        input.value = "";
    };

    $scope.removeNaturalLanguageDocument = index => $scope.documentWorkspace.files.splice(index, 1);

    $scope.generateCaseWorkspaceFromDocuments = () => {
        const description = ($scope.documentWorkspace.description || "").trim();
        if (!description && !$scope.documentWorkspace.files.length) return;
        const dataUser = store.session.get("user_tddm4iotbs");
        if (!dataUser || !dataUser.user_token) {
            $scope.documentWorkspace.errors = ["Your session has expired. Sign in again to use AI analysis."];
            return;
        }
        $scope.documentWorkspace.errors = [];
        $scope.documentWorkspace.loading = true;
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + "master-project/generate-case-workspace",
            headers: {userToken: dataUser.user_token},
            data: JSON.stringify({classDTO: {
                description: description,
                documents: $scope.documentWorkspace.files.map(file => ({
                    fileName: file.name,
                    contentType: file.contentType,
                    base64Content: file.base64Content
                }))
            }}),
            success: response => $scope.$apply(() => {
                $scope.documentWorkspace.loading = false;
                if (response.status !== "OK" || !response.data) {
                    $scope.documentWorkspace.errors = [response.message || "The AI service did not return a project model."];
                    return;
                }
                const validation = validateCaseWorkspace(response.data);
                if (validation.errors.length) {
                    $scope.documentWorkspace.errors = ["The generated model did not pass local validation."].concat(validation.errors);
                    return;
                }
                $("#modal_generate_case_workspace").modal("hide");
                Swal.fire({
                    title: "Generate the complete UML workspace?",
                    text: validation.summary.diagrams + " use case diagrams, " + validation.summary.useCases + " use cases and " + validation.summary.sequences + " sequences will replace the current workspace.",
                    icon: "question",
                    showCancelButton: true,
                    confirmButtonText: "Generate workspace",
                    cancelButtonText: "Review later"
                }).then(result => {
                    if (!result.isConfirmed) return;
                    $scope.$apply(() => {
                        importCaseWorkspace(response.data);
                        alertAll({status: 1, information: "The complete CASE workspace was generated from the natural-language requirements."});
                    });
                });
            }),
            error: xhr => $scope.$applyAsync(() => {
                $scope.documentWorkspace.loading = false;
                const message = xhr.responseJSON && xhr.responseJSON.message ? xhr.responseJSON.message : "The requirements could not be analyzed.";
                $scope.documentWorkspace.errors = [message];
            })
        });
    };

    const resetCaseWorkspaceImport = () => {
        $scope.caseWorkspaceImport = {fileName: "", payload: null, summary: null, errors: [], warnings: []};
        const fileInput = document.getElementById("caseWorkspaceFile");
        if (fileInput) fileInput.value = "";
    };

    $scope.openCaseWorkspaceImport = () => {
        resetCaseWorkspaceImport();
        $("#modal_import_case_workspace").modal("show");
    };

    const normalizeRelationType = (type) => ({
        association: "association",
        communication: "association",
        UMLCommunication: "association",
        include: "include",
        UMLInclude: "include",
        extend: "extend",
        UMLExtend: "extend",
        generalization: "generalization",
        UMLGeneralization: "generalization"
    }[type]);

    const validateCaseWorkspace = (payload) => {
        const errors = [];
        const warnings = [];
        const result = {diagrams: 0, actors: 0, useCases: 0, relations: 0, sequences: 0};
        const addError = (path, message) => errors.push(path + ": " + message);

        if (!payload || Array.isArray(payload) || typeof payload !== "object") {
            return {errors: ["JSON: the root value must be an object."], warnings: [], summary: result};
        }
        if (payload.format !== "TDDT4IOTS_CASE_WORKSPACE") addError("format", "expected TDDT4IOTS_CASE_WORKSPACE.");
        if (String(payload.schemaVersion || "") !== "1.0") addError("schemaVersion", "this importer supports version 1.0.");
        if (!Array.isArray(payload.diagrams) || !payload.diagrams.length) {
            addError("diagrams", "add at least one use case diagram.");
            return {errors: errors, warnings: warnings, summary: result};
        }

        const diagramIds = new Set();
        const diagramNames = new Set();
        payload.diagrams.forEach((diagramSpec, diagramIndex) => {
            const path = "diagrams[" + diagramIndex + "]";
            const diagramId = String(diagramSpec.id || "").trim();
            const diagramName = String(diagramSpec.name || "").trim();
            if (!diagramId) addError(path + ".id", "is required.");
            else if (diagramIds.has(diagramId)) addError(path + ".id", "must be unique.");
            else diagramIds.add(diagramId);
            if (!diagramName) addError(path + ".name", "is required.");
            else if (diagramNames.has(diagramName.toLowerCase())) addError(path + ".name", "must be unique.");
            else diagramNames.add(diagramName.toLowerCase());

            const actors = Array.isArray(diagramSpec.actors) ? diagramSpec.actors : [];
            const useCases = Array.isArray(diagramSpec.useCases) ? diagramSpec.useCases : [];
            const relations = Array.isArray(diagramSpec.relations) ? diagramSpec.relations : [];
            if (!useCases.length) addError(path + ".useCases", "add at least one use case.");
            result.diagrams++;
            result.actors += actors.length;
            result.useCases += useCases.length;
            result.sequences += useCases.length;
            result.relations += relations.length;

            const actorIds = new Set();
            const actorNames = new Set();
            actors.forEach((actor, actorIndex) => {
                const actorPath = path + ".actors[" + actorIndex + "]";
                const id = String(actor.id || "").trim();
                const name = String(actor.name || "").trim();
                if (!id) addError(actorPath + ".id", "is required.");
                else if (actorIds.has(id)) addError(actorPath + ".id", "must be unique in the diagram.");
                else actorIds.add(id);
                if (!name) addError(actorPath + ".name", "is required.");
                else if (actorNames.has(name.toLowerCase())) addError(actorPath + ".name", "must be unique in the diagram.");
                else actorNames.add(name.toLowerCase());
            });

            const useCaseIds = new Set();
            const useCaseNames = new Set();
            useCases.forEach((useCase, useCaseIndex) => {
                const useCasePath = path + ".useCases[" + useCaseIndex + "]";
                const id = String(useCase.id || "").trim();
                const name = String(useCase.name || "").trim();
                if (!id) addError(useCasePath + ".id", "is required.");
                else if (useCaseIds.has(id)) addError(useCasePath + ".id", "must be unique in the diagram.");
                else useCaseIds.add(id);
                if (!name) addError(useCasePath + ".name", "is required.");
                else if (useCaseNames.has(name.toLowerCase())) addError(useCasePath + ".name", "must be unique in the diagram.");
                else useCaseNames.add(name.toLowerCase());

                ["purpose", "precondition", "postcondition", "description"].forEach(field => {
                    if (typeof useCase[field] !== "string" || !useCase[field].trim()) addError(useCasePath + "." + field, "is required.");
                    else if (!hasRecognizedModelData(useCase[field])) addError(useCasePath + "." + field, "must contain symbols recognized by the local class parser.");
                });

                const mainFlow = Array.isArray(useCase.mainFlow) ? useCase.mainFlow : [];
                if (!mainFlow.length) addError(useCasePath + ".mainFlow", "add at least one step.");
                mainFlow.forEach((step, stepIndex) => {
                    const stepPath = useCasePath + ".mainFlow[" + stepIndex + "]";
                    const actor = String(step.actor || "").trim();
                    if (!actor) addError(stepPath + ".actor", "is required.");
                    else if (actor !== "System" && !actorIds.has(actor)) addError(stepPath + ".actor", "must reference an actor id or System.");
                    if (typeof step.action !== "string" || !step.action.trim()) addError(stepPath + ".action", "is required.");
                    else if (!hasRecognizedModelData(step.action)) addError(stepPath + ".action", "must include symbols recognized by the local class parser.");
                });

                const alternativeFlows = Array.isArray(useCase.alternativeFlows) ? useCase.alternativeFlows : [];
                alternativeFlows.forEach((flow, flowIndex) => {
                    const flowPath = useCasePath + ".alternativeFlows[" + flowIndex + "]";
                    const actor = String(flow.actor || "").trim();
                    const startStep = Number(flow.startStep);
                    const returnStep = Number(flow.returnStep);
                    if (!Number.isInteger(startStep) || startStep < 1 || startStep > mainFlow.length) addError(flowPath + ".startStep", "must reference a normal-flow step.");
                    if (!String(flow.condition || "").trim()) addError(flowPath + ".condition", "is required.");
                    if (!actor) addError(flowPath + ".actor", "is required.");
                    else if (actor !== "System" && !actorIds.has(actor)) addError(flowPath + ".actor", "must reference an actor id or System.");
                    if (typeof flow.action !== "string" || !flow.action.trim()) addError(flowPath + ".action", "is required.");
                    else if (!hasRecognizedModelData(flow.action)) addError(flowPath + ".action", "must include symbols recognized by the local class parser.");
                    if (["return", "end"].indexOf(flow.outcome) === -1) addError(flowPath + ".outcome", "must be return or end.");
                    if (flow.outcome === "return" && (!Number.isInteger(returnStep) || returnStep < startStep || returnStep > mainFlow.length)) {
                        addError(flowPath + ".returnStep", "must be between the origin step and the last normal-flow step.");
                    }
                });

                if (useCase.sequence !== undefined) {
                    const sequencePath = useCasePath + ".sequence";
                    const participants = Array.isArray(useCase.sequence.participants) ? useCase.sequence.participants : [];
                    const messages = Array.isArray(useCase.sequence.messages) ? useCase.sequence.messages : [];
                    const participantIds = new Set();
                    if (participants.length < 2) addError(sequencePath + ".participants", "add at least two lifelines.");
                    participants.forEach((participant, participantIndex) => {
                        const participantPath = sequencePath + ".participants[" + participantIndex + "]";
                        const id = String(participant.id || "").trim();
                        if (!id) addError(participantPath + ".id", "is required.");
                        else if (participantIds.has(id)) addError(participantPath + ".id", "must be unique in the sequence.");
                        else participantIds.add(id);
                        if (!String(participant.name || "").trim()) addError(participantPath + ".name", "is required.");
                    });
                    if (!messages.length) addError(sequencePath + ".messages", "add at least one interaction.");
                    messages.forEach((message, messageIndex) => {
                        const messagePath = sequencePath + ".messages[" + messageIndex + "]";
                        if (!participantIds.has(message.from)) addError(messagePath + ".from", "must reference a sequence participant.");
                        if (!participantIds.has(message.to)) addError(messagePath + ".to", "must reference a sequence participant.");
                        if (!String(message.message || "").trim()) addError(messagePath + ".message", "is required.");
                        if (["sync", "async"].indexOf(message.type || "sync") === -1) addError(messagePath + ".type", "must be sync or async.");
                    });
                } else {
                    warnings.push(useCasePath + ": no explicit sequence was supplied; one will be generated from the normal flow.");
                }
            });

            relations.forEach((relation, relationIndex) => {
                const relationPath = path + ".relations[" + relationIndex + "]";
                const type = normalizeRelationType(relation.type);
                const fromActor = actorIds.has(relation.from);
                const toActor = actorIds.has(relation.to);
                const fromUseCase = useCaseIds.has(relation.from);
                const toUseCase = useCaseIds.has(relation.to);
                if (!type) addError(relationPath + ".type", "use association, include, extend or generalization.");
                if (!(fromActor || fromUseCase)) addError(relationPath + ".from", "references an unknown element.");
                if (!(toActor || toUseCase)) addError(relationPath + ".to", "references an unknown element.");
                if (type === "association" && !((fromActor && toUseCase) || (fromUseCase && toActor))) addError(relationPath, "an association must connect an actor and a use case.");
                if ((type === "include" || type === "extend") && !(fromUseCase && toUseCase)) addError(relationPath, "include and extend must connect two use cases.");
                if (type === "generalization" && !((fromActor && toActor) || (fromUseCase && toUseCase))) addError(relationPath, "generalization must connect elements of the same type.");
            });

            useCases.forEach((useCase, useCaseIndex) => {
                const associatedActors = new Set(relations.filter(relation => normalizeRelationType(relation.type) === "association")
                    .filter(relation => relation.from === useCase.id || relation.to === useCase.id)
                    .map(relation => actorIds.has(relation.from) ? relation.from : relation.to));
                (useCase.mainFlow || []).forEach(step => {
                    if (step.actor !== "System" && actorIds.has(step.actor) && !associatedActors.has(step.actor)) {
                        warnings.push(path + ".useCases[" + useCaseIndex + "]: actor " + step.actor + " participates in the flow but has no association relation.");
                    }
                });
            });
        });
        return {errors: errors, warnings: warnings, summary: result};
    };

    $scope.handleCaseWorkspaceFile = (input) => {
        const file = input && input.files ? input.files[0] : null;
        if (!file) return;
        const reader = new FileReader();
        reader.onload = event => {
            $scope.$apply(() => {
                resetCaseWorkspaceImport();
                $scope.caseWorkspaceImport.fileName = file.name;
                try {
                    const payload = JSON.parse(event.target.result);
                    const validation = validateCaseWorkspace(payload);
                    $scope.caseWorkspaceImport.payload = payload;
                    $scope.caseWorkspaceImport.summary = validation.summary;
                    $scope.caseWorkspaceImport.errors = validation.errors;
                    $scope.caseWorkspaceImport.warnings = validation.warnings;
                } catch (error) {
                    $scope.caseWorkspaceImport.errors = ["The selected file is not valid JSON: " + error.message];
                }
            });
        };
        reader.onerror = () => $scope.$apply(() => {
            $scope.caseWorkspaceImport.errors = ["The selected file could not be read."];
        });
        reader.readAsText(file, "UTF-8");
    };

    const locallyInterpret = text => getHackDiagram(String(text || ""));

    const sequenceSpecToData = (useCaseSpec, actorMap) => {
        const sequence = useCaseSpec.sequence || {};
        let participants = Array.isArray(sequence.participants) ? sequence.participants : [];
        let messages = Array.isArray(sequence.messages) ? sequence.messages : [];
        if (!participants.length || !messages.length) {
            const participantByName = new Map();
            const ensureParticipant = name => {
                if (!participantByName.has(name)) participantByName.set(name, {id: "participant-" + participantByName.size, name: name});
                return participantByName.get(name);
            };
            const system = ensureParticipant("System");
            const applicationService = ensureParticipant("Application service");
            let lastExternal = null;
            messages = (useCaseSpec.mainFlow || []).map((step, index) => {
                const actorName = step.actor === "System" ? "System" : (actorMap[step.actor] ? actorMap[step.actor].data.name : step.actor);
                const actor = ensureParticipant(actorName);
                if (actorName !== "System") lastExternal = actor;
                const from = actorName === "System" ? system : actor;
                const to = actorName === "System" ? (lastExternal || applicationService) : system;
                return {from: from.id, to: to.id, message: step.action, type: "sync", response: ""};
            });
            participants = Array.from(participantByName.values());
        }
        const participantNames = {};
        participants.forEach(participant => participantNames[participant.id] = participant.name);
        return {
            table_normal_flow: messages.map((message, index) => ({
                from: participantNames[message.from] || message.from,
                to: participantNames[message.to] || message.to,
                message: (index + 1) + ") " + message.message,
                message_type: message.type === "async" ? "async_message" : "sync_message",
                response: message.response || "",
                response_type: message.responseType === "async" ? "async_response" : (message.response ? "sync_response" : ""),
                index: index + 1
            })),
            table_loop: [],
            table_alternative_if: [],
            table_alternative_else: []
        };
    };

    const createImportedUseCase = (entry, useCaseSpec, actorMap, useCaseMap) => {
        const position = useCaseSpec.position || {};
        const objectUseCase = createUseCase({name: useCaseSpec.name, pos_Y: Number(position.y) || 120});
        objectUseCase.position(Number(position.x) || 670, Number(position.y) || 120);

        const sequenceDiagram = createDiagram({
            width: 100, height: 81, id_div: "areaDiagram", diagram: UMLSequenceDiagram,
            name: "Sequence Diagram - " + useCaseSpec.name, interaction: false, draw: false
        });
        const sequenceButton = {
            diagram: sequenceDiagram,
            name: "Sequence Diagram - " + useCaseSpec.name,
            type: "UMLSequenceDiagram",
            status: "C",
            btnactive: "btn btn-sm button-dt mr-2 mt-1",
            useCaseId: useCaseSpec.id,
            parentDiagramId: entry.id
        };
        $scope.btnDiagramUml.push(sequenceButton);

        const actorName = actorId => actorId === "System" ? "System" : actorMap[actorId].data.name;
        const mainFlow = useCaseSpec.mainFlow.map(step => ({
            actor: actorName(step.actor),
            action_interpret: locallyInterpret(step.action)[0],
            action_original: step.action
        }));
        const alternativeFlows = (useCaseSpec.alternativeFlows || []).map((flow, index) => ({
            id: flow.id || workspaceId("af"),
            he_passed: Number(flow.startStep),
            start_step: Number(flow.startStep),
            condition: flow.condition,
            actor: actorName(flow.actor),
            action_interpret: locallyInterpret(flow.action)[0],
            action_original: flow.action,
            outcome: flow.outcome,
            return: flow.outcome === "return" ? Number(flow.returnStep) : null,
            return_step: flow.outcome === "return" ? Number(flow.returnStep) : null
        }));

        const data = {
            id: useCaseSpec.id,
            diagramId: entry.id,
            obj_usecase: objectUseCase,
            name: useCaseSpec.name,
            name_i: useCaseSpec.name,
            porpuse: useCaseSpec.purpose,
            porpuse_i: locallyInterpret(useCaseSpec.purpose)[0],
            pre_condition: useCaseSpec.precondition,
            pre_condition_i: locallyInterpret(useCaseSpec.precondition)[0],
            post_condition: useCaseSpec.postcondition,
            post_condition_i: locallyInterpret(useCaseSpec.postcondition)[0],
            description_interpret: useCaseSpec.description,
            description_original: locallyInterpret(useCaseSpec.description)[0],
            main_stage: mainFlow,
            alternative_flow: alternativeFlows,
            secuence_diagram: sequenceDiagram,
            secuence_button: sequenceButton,
            secuence_data: sequenceSpecToData(useCaseSpec, actorMap)
        };
        updateSecuenceDiagram(sequenceDiagram, data.secuence_data);
        entry.data.useCase.push(data);
        useCaseMap[useCaseSpec.id] = {object: objectUseCase, data: data};
    };

    const importCaseWorkspace = payload => {
        const classButtons = $scope.btnDiagramUml.filter(button => button.type === "UMLClassDiagram");
        $scope.btnDiagramUml = classButtons;
        $scope.useCaseDiagrams = [];
        $scope.activeUseCaseDiagram = null;
        $scope.jsonUseCase = blankUseCaseData();
        $scope.jsonClass = emptyClassDiagramData();
        $scope.editionClasDiagram = false;
        if (diagramClass) {
            diagramClass._nodes.length = 0;
            diagramClass._relations.length = 0;
        }
        if (typeof elementsClass !== "undefined" && elementsClass && typeof elementsClass.length === "number") elementsClass.length = 0;

        payload.diagrams.forEach((diagramSpec, diagramIndex) => {
            const diagram = createDiagram({
                width: 100, height: 81, id_div: "areaDiagram", diagram: UMLUseCaseDiagram,
                name: diagramSpec.name, interaction: false, draw: false
            });
            const button = {diagram: diagram, name: diagramSpec.name, type: "UMLUseCaseDiagram", status: "C", btnactive: "btn btn-sm button-dt mr-2 mt-1"};
            $scope.btnDiagramUml.splice(diagramIndex, 0, button);
            const entry = registerUseCaseDiagram(button, blankUseCaseData(), {id: diagramSpec.id, name: diagramSpec.name});
            activateUseCaseEntry(entry);
            createSystem((payload.project && payload.project.name) || $scope.nameProject || diagramSpec.name);

            const actorMap = {};
            const useCaseMap = {};
            (diagramSpec.actors || []).forEach((actorSpec, actorIndex) => {
                const position = actorSpec.position || {};
                const objectActor = createActorTools({name: actorSpec.name, pos_Y: Number(position.y) || (110 + actorIndex * 115)});
                objectActor.position(Number(position.x) || 250, Number(position.y) || (110 + actorIndex * 115));
                const actorData = {id: actorSpec.id, obj_actor: objectActor, name: actorSpec.name};
                entry.data.actors.push(actorData);
                actorMap[actorSpec.id] = {object: objectActor, data: actorData};
            });
            (diagramSpec.useCases || []).forEach(useCaseSpec => createImportedUseCase(entry, useCaseSpec, actorMap, useCaseMap));

            const relationConstructors = {
                association: UMLCommunication,
                include: UMLInclude,
                extend: UMLExtend,
                generalization: UMLGeneralization
            };
            (diagramSpec.relations || []).forEach(relationSpec => {
                const type = normalizeRelationType(relationSpec.type);
                const from = actorMap[relationSpec.from] || useCaseMap[relationSpec.from];
                const to = actorMap[relationSpec.to] || useCaseMap[relationSpec.to];
                const relation = createRelation({a: from.object, b: to.object, type: relationConstructors[type]});
                entry.data.relations.push({type: relationConstructors[type].name || relation._type, obj_relation: relation});
            });
            diagram.draw();
        });

        const previousAiStatus = $scope.activateOpenAi;
        $scope.activateOpenAi = false;
        superInterpret();
        $scope.activateOpenAi = previousAiStatus;
        updateClassDiagram($scope.jsonClass, "C");
        $scope.classLayoutMode = "orthogonal";
        $scope.actualizarNaturalText($scope.jsonClass);

        const firstEntry = $scope.useCaseDiagrams[0];
        activateUseCaseEntry(firstEntry);
        $scope.changeDiagramA(firstEntry.button);
    };

    $scope.confirmCaseWorkspaceImport = () => {
        if (!$scope.caseWorkspaceImport.payload || $scope.caseWorkspaceImport.errors.length) return;
        Swal.fire({
            title: "Replace the UML workspace?",
            text: "The diagrams currently open on the canvas will be replaced. You can cancel and save them first.",
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Import and generate",
            cancelButtonText: "Cancel"
        }).then(result => {
            if (!result.isConfirmed) return;
            $scope.$apply(() => {
                try {
                    importCaseWorkspace($scope.caseWorkspaceImport.payload);
                    $("#modal_import_case_workspace").modal("hide");
                    alertAll({status: 1, information: "Workspace imported. The consolidated class diagram was generated from the symbolic text."});
                } catch (error) {
                    console.error(error);
                    alertAll({status: 4, information: "The workspace could not be imported: " + error.message});
                }
            });
        });
    };

    initDiagramUml = (onReady) => {
        $.getJSON("resources/js/js-uml/diagramsUml.json", function (data) {
            $scope.$apply(function () {
                $scope.diagramsUml = data;
            });

            for (let posDiagram = 0; posDiagram < $scope.diagramsUml.length; posDiagram++) {
                $scope.$apply(function () {
                    $scope.btnDiagramUml.push({
                        diagram: createDiagram({
                            "width": 100,
                            "height": 81,
                            "id_div": "areaDiagram",
                            "diagram": $scope.diagramsUml[posDiagram].type === "UMLUseCaseDiagram" ? UMLUseCaseDiagram :
                                    $scope.diagramsUml[posDiagram].type === "UMLClassDiagram" ? UMLClassDiagram : undefined,
                            "name": $scope.diagramsUml[posDiagram].name,
                            "interaction": $scope.diagramsUml[posDiagram].interaction,
                            "draw": $scope.diagramsUml[posDiagram].draw
                        }),
                        "name": $scope.diagramsUml[posDiagram].name,
                        "type": $scope.diagramsUml[posDiagram].type,
                        "status": $scope.diagramsUml[posDiagram].status,
                        "btnactive": $scope.diagramsUml[posDiagram].btnactive
                    });
                });
            }

            diagramUseCase = getDiagramUseCase();
            diagramClass = getDiagramClass();
            const useCaseButton = $scope.btnDiagramUml.find(button => button.type === "UMLUseCaseDiagram");
            const mainEntry = registerUseCaseDiagram(useCaseButton, $scope.jsonUseCase, {
                name: "Main use case diagram"
            });
            activateUseCaseEntry(mainEntry);
            if (typeof onReady === "function") onReady();
        });
    };

    $scope.changeDiagramA = (new_diagram_open) => {
        let open_current_diagram = {};
        for (let i = 0; i < $scope.btnDiagramUml.length; i++) {
            if ($scope.btnDiagramUml[i].status === "O") {
                open_current_diagram = $scope.btnDiagramUml[i].diagram;
                $scope.btnDiagramUml[i].status = "C";
                $scope.btnDiagramUml[i].btnactive = "btn btn-sm button-dt mr-2 mt-1";
            }
        }
        new_diagram_open.status = "O";
        $scope.showTool = new_diagram_open.type;
        new_diagram_open.btnactive = "btn btn-sm button-active mr-2 mt-1";
        if (new_diagram_open.type === "UMLUseCaseDiagram") {
            const entry = $scope.useCaseDiagrams.find(item => item.button === new_diagram_open);
            activateUseCaseEntry(entry);
        }
        changeDiagram(open_current_diagram, new_diagram_open.diagram);
        if (new_diagram_open.diagram && typeof new_diagram_open.diagram.getZoom === "function") {
            $scope.canvasZoom = Math.round(new_diagram_open.diagram.getZoom() * 100);
        }
    };

    $scope.openUseCaseDiagram = (entry) => {
        activateUseCaseEntry(entry);
        $scope.changeDiagramA(entry.button);
    };

    $scope.openClassDiagram = () => $scope.changeDiagramA($scope.btnDiagramUml.find(button => button.type === "UMLClassDiagram"));

    $scope.openSequenceDiagram = (entry, useCase) => {
        activateUseCaseEntry(entry);
        const sequenceButton = useCase.secuence_button || $scope.btnDiagramUml.find(button =>
            button.type === "UMLSequenceDiagram" && button.useCaseId === useCase.id);
        if (sequenceButton) $scope.changeDiagramA(sequenceButton);
    };

    $scope.focusUseCase = (entry, useCase) => {
        $scope.openUseCaseDiagram(entry);
        if (useCase.obj_usecase) window.setTimeout(() => viewUseCase(useCase.obj_usecase), 0);
    };

    $scope.focusActor = (entry, actor) => {
        $scope.openUseCaseDiagram(entry);
        if (actor.obj_actor) window.setTimeout(() => selectedActor(actor.obj_actor), 0);
    };

    $scope.actorsForUseCase = (entry, useCase) => {
        if (!entry || !useCase || !useCase.obj_usecase) return [];
        const actorNames = [];
        (entry.data.relations || []).forEach(relationData => {
            const relation = relationData.obj_relation;
            if (!relation || relation._type !== "UMLCommunication") return;
            const endpoints = [relation._elemA, relation._elemB];
            if (endpoints.indexOf(useCase.obj_usecase) === -1) return;
            endpoints.forEach(endpoint => {
                if (endpoint && endpoint._type === "UMLActor" && actorNames.indexOf(endpoint.getName()) === -1) actorNames.push(endpoint.getName());
            });
        });
        return (entry.data.actors || []).filter(actor => actorNames.indexOf(actor.name) !== -1);
    };

    $scope.createUseCaseDiagram = () => {
        Swal.fire({
            title: "New use case diagram",
            input: "text",
            inputLabel: "Diagram name",
            inputPlaceholder: "Example: Order management",
            showCancelButton: true,
            confirmButtonText: "Create",
            inputValidator: value => {
                const name = (value || "").trim();
                if (!name) return "Enter a diagram name.";
                if ($scope.useCaseDiagrams.some(entry => entry.name.toLowerCase() === name.toLowerCase())) return "A diagram with this name already exists.";
            }
        }).then(result => {
            if (!result.isConfirmed) return;
            const name = result.value.trim();
            const diagram = createDiagram({width: 100, height: 81, id_div: "areaDiagram", diagram: UMLUseCaseDiagram, name: name, interaction: true, draw: false});
            const button = {diagram: diagram, name: name, type: "UMLUseCaseDiagram", status: "C", btnactive: "btn btn-sm button-dt mr-2 mt-1"};
            $scope.$apply(() => {
                $scope.btnDiagramUml.push(button);
                const entry = registerUseCaseDiagram(button, blankUseCaseData(), {name: name});
                activateUseCaseEntry(entry);
                $scope.changeDiagramA(button);
                createSystem($scope.nameProject);
            });
        });
    };

    $scope.renameUseCaseDiagram = (entry, event) => {
        if (event) event.stopPropagation();
        Swal.fire({title: "Rename diagram", input: "text", inputValue: entry.name, showCancelButton: true, confirmButtonText: "Save"})
            .then(result => {
                const name = result.isConfirmed ? (result.value || "").trim() : "";
                if (!name) return;
                $scope.$apply(() => {
                    entry.name = name;
                    entry.button.name = name;
                    entry.diagram.setName(name);
                });
            });
    };

    getDiagramUseCase = () => {
        for (let i = 0; i < $scope.btnDiagramUml.length; i++) {
            if ($scope.btnDiagramUml[i].type === "UMLUseCaseDiagram") {
                return $scope.btnDiagramUml[i].diagram;
            }
        }
    };

    getDiagramClass = () => {
        for (let i = 0; i < $scope.btnDiagramUml.length; i++) {
            if ($scope.btnDiagramUml[i].type === "UMLClassDiagram") {
                return $scope.btnDiagramUml[i].diagram;
            }
        }
    };

    $scope.loadDiagram = () => {
        $.getJSON("resources/js/js-uml/diagramsUml.json", function (data) {
            $scope.$apply(function () {
                $scope.diagramsUml = data;
                console.log($scope.diagramsUml);
            });
        });
    };

    //funcion para cerrar sesion
    $scope.logOut = () => {
        cerrarSesion();
    };

    /**
     * funcion para crear los valores aleatorios en Y para los objetos de los casos de uso
     * */
    function randomPosition(max, min) {
        return Math.floor(Math.random() * (+max - +min)) + +min;
    }

    /**
     * ###############################################################################################################
     * Inicio Eventos clicks que abren  y cierran los modales para ingresar los datos correspondientes a los objetos
     * del diagrama
     * ###############################################################################################################
     * */

    let areadiagram = document.getElementById("areaDiagram");
    areadiagram.addEventListener("umlzoomchange", function (event) {
        $scope.$applyAsync(() => {
            const activeButton = $scope.btnDiagramUml.find(button => button.status === "O");
            if (activeButton && activeButton.diagram && typeof activeButton.diagram.getZoom === "function") {
                $scope.canvasZoom = Math.round(activeButton.diagram.getZoom() * 100);
            }
        });
    });
    areadiagram.addEventListener("wheel", function (event) {
        if (!event.ctrlKey && !event.metaKey) return;
        event.preventDefault();
        $scope.$applyAsync(() => {
            $scope.setCanvasZoom($scope.canvasZoom + (event.deltaY < 0 ? 10 : -10));
        });
    }, {passive: false});
    var statusDropEnd = "";
    areadiagram.addEventListener('dragleave', function () {
        switch (statusDropEnd) {
            case "actor":
                $("#modal_actor").modal();
                statusDropEnd = "";
                break;
            case "usecase":
                $scope.actorsUseCase.length = 0;
                statusDropEnd = "";
                $("#modal_usecase").modal();
                break;
            case "newclass":
                statusDropEnd = "";
                $("#modal_newclass").modal();
                break;
        }
    });

    let actor = document.getElementById("btnactor");
    actor.addEventListener('dragstart', function () {

        if ($scope.editionClasDiagram) {
            alertAll({
                "status": 3,
                "information": "You cannot modify the use case diagram while the class diagram editing is active."
            });
            return;
        }

        statusDropEnd = "actor";
        console.log(statusDropEnd);
    });

    let usecase = document.getElementById("btnusecase");
    usecase.addEventListener('dragstart', function () {

        if ($scope.editionClasDiagram) {
            alertAll({
                "status": 3,
                "information": "You cannot modify the use case diagram while the class diagram editing is active."
            });
            return;
        }

        statusDropEnd = "usecase";
        console.log(statusDropEnd);
    });

    let newclass = document.getElementById("btnclass");
    newclass.addEventListener('dragstart', function () {

        if (!$scope.editionClasDiagram) {
            alertAll({"status": 3, "information": "You cannot modify the class diagram if editing is disabled."});
            return;
        }

        statusDropEnd = "newclass";
        console.log(statusDropEnd);
    });

    //cerrar actor
    $("#btn_close_actor").click(function () {
        $scope.$apply(function () {
            //determinamos que la accion de editar se establece en false
            $scope.flag_update = false;
        });
        //cerramos el modal del caso de uso
        $("#modal_actor").modal('hide');
    });

    //cerrar caso de uso
    $scope.cancelUseCase = (form) => {

        Swal.fire({
            title: 'Are you sure?',
            text: "All information entered will be permanently deleted",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, remove it!'
        }).then((result) => {
            if (result.isConfirmed) {
                $scope.$apply(() => {
                    //limpiamos los actores que pertenecen al caso de uso
                    $scope.actorsUseCase.length = 0;
                    //limpiamos el escenario principal y el flujo alternativo del caso de uso
                    $scope.manager_maf.main_stage.length = 0;
                    $scope.manager_maf.alternative_flow.length = 0;

                    //determinamos que la accion de editar se establece en false
                    $scope.flag_update = false;
                    clearFormUseCase(form);

                    form.$setPristine();
                    form.$setUntouched();

                    $scope.flag_update = false;
                });
                //cerramos el modal del caso de uso
                $("#modal_usecase").modal('hide');
            }
        });
    };

    $scope.interpret = [];
    $scope.selectedSymbol = null;
    $scope.symbolGuideQuery = "";
    $scope.loadInterpret = function () {
//        console.log("buscar el:"+valuex);
        $.getJSON("resources/json/interpret.json", function (data) {
            $scope.$apply(function () {
                $scope.interpret = data.map(item => {
                    const container = document.createElement("div");
                    container.innerHTML = String(item.example || "").replace(/<\/?br\s*\/?\s*>/gi, "\n");
                    item.exampleText = (container.textContent || container.innerText || "")
                        .replace(/\u00a0/g, " ").replace(/[ \t]+\n/g, "\n").replace(/\n{3,}/g, "\n\n").trim();
                    return item;
                });
                $scope.selectedSymbol = $scope.interpret[0] || null;
            });
        });
    };

    $scope.loadExample = (simbol) => {
        $scope.selectedSymbol = simbol;
    };

    //cerrar edicion de la accion seleciconada
    $("#btn_cancel_update_action").click(function () {
        $("#modal_update_duc").modal('hide');
    });

    $("#Symbols_use_cases").click(function () {
        $("#modal_Symbols").modal();
    });

    $("#Symbols_use_cases_import").click(function () {
        $("#modal_import_case_workspace").modal("hide");
        $("#modal_Symbols").modal();
    });

    $("#btninheritanceactor").click(function () {

        if ($scope.editionClasDiagram) {
            alertAll({
                "status": 3,
                "information": "You cannot modify the use case diagram while the class diagram editing is active."
            });
            return;
        }

        $("#modal_inheritance_actor").modal();
    });

    $("#btnassociationusecase").click(function () {

        if ($scope.editionClasDiagram) {
            alertAll({
                "status": 3,
                "information": "You cannot modify the use case diagram while the class diagram editing is active."
            });
            return;
        }

        $("#modal_association_usecase").modal();
    });

    $("#btnasosition").click(function () {

        if ($scope.editionClasDiagram) {
            alertAll({
                "status": 3,
                "information": "You cannot modify the use case diagram while the class diagram editing is active."
            });
            return;
        }

        $("#modal_association").modal();
    });

    $("#btninclusion").click(function () {

        if ($scope.editionClasDiagram) {
            alertAll({
                "status": 3,
                "information": "You cannot modify the use case diagram while the class diagram editing is active."
            });
            return;
        }

        $("#modal_inclusion").modal();
    });

    $("#btnextend").click(function () {

        if ($scope.editionClasDiagram) {
            alertAll({
                "status": 3,
                "information": "You cannot modify the use case diagram while the class diagram editing is active."
            });
            return;
        }

        $("#modal_extend").modal();
    });

    $("#btngeneralization").click(function () {

        if ($scope.editionClasDiagram) {
            alertAll({
                "status": 3,
                "information": "You cannot modify the use case diagram while the class diagram editing is active."
            });
            return;
        }

        $("#modal_generalization").modal();
    });


    /**
     * ###############################################################################################################
     * Fin de Eventos clicks que abren los modales para ingresar los datos correspondientes a los objetos del diagrama
     * ###############################################################################################################
     * */

    /**
     * @function funcion para crear los actores
     * @param {form} form
     * */
    $scope.createActor = function (form, flag) {
        if (form.$valid) {
            if (!$scope.nameEquals(form.actor_name.$viewValue, -1)) {
                let actorobject = createActorTools({
                    "name": form.actor_name.$viewValue,
                    "pos_Y": randomPosition(400, 70)
                });
                $scope.jsonUseCase.actors.push({
                    "obj_actor": actorobject,
                    "name": actorobject.getName()
                });
                $("#modal_actor").modal('hide');
                if (flag)
                    $scope.utilWebSocket("createActor", {"form": JSON.decycle(form)});

                console.log($scope.jsonUseCase.actors);
                console.log($scope.jsonUseCase.actors[0].obj_actor.getName());
            }
        }
    };

    // funcion para crear la herencia entre actores
    $scope.createInHeritanceActor = function (form) {
        if (form.$valid) {
            let relation = {};

            relation = createRelation({
                "type": UMLGeneralization,
                "a": $scope.jsonUseCase.actors[form.selecactor1.$viewValue].obj_actor,
                "b": $scope.jsonUseCase.actors[form.selecactor2.$viewValue].obj_actor
            });

            $scope.jsonUseCase.relations.push({
                "type": "UMLGeneralization",
                "obj_relation": relation
            });

            $("#modal_inheritance_actor").modal("hide");
        } else {
            alert("aguante :v");
        }
    };

    $scope.createAssociationUseCase = function (form) {
        if (form.$valid) {
            let relation = {};

            relation = createRelation({
                "type": UMLCommunication,
                "a": $scope.jsonUseCase.useCase[form.selectusecase1.$viewValue].obj_usecase,
                "b": $scope.jsonUseCase.useCase[form.selectusecase2.$viewValue].obj_usecase
            });

            $scope.jsonUseCase.relations.push({
                "type": "UMLCommunication",
                "obj_relation": relation
            });

            $("#modal_association_usecase").modal("hide");
        } else {
            alert("aguante :v");
        }
    };

    //funcion para editar los actores
    $scope.updateActor = function (form, flag) {
        try {
            if (form.$valid) {
                if ($scope.nameEquals(form.actor_name.$modelValue, -1)) {
                    return;
                }
                $scope.object_update.setName(form.actor_name.$modelValue);

                diagramUseCase.draw();
                $scope.flag_update = false;
                $("#modal_actor").modal('hide');
                if (flag)
                    $scope.utilWebSocket("updateActor", {"form": JSON.decycle(form)});
            }
        } catch (ErrorMessage) {
            console.log(ErrorMessage);
            alertAll({
                "status": 4,
                "information": "[updateActor]: " + ErrorMessage.message
            });
        }
    };

    /**
     * ##############################################################################################################
     * INICIO BLOQUE DE CODIGO PARA LA DESCRIPCION DEL CASO DE USO
     * ##############################################################################################################
     * */

    //funcion para crear los actores dentro de la descripcion de los casos de uso
    $scope.createActorUC = function (flag) {
        try {
            if ($scope.usecase_actorname !== undefined && $scope.usecase_actorname.trim() !== '') {

                if (!$scope.nameEquals($scope.usecase_actorname, -1)) {
                    //crear lista de actores
                    let actorobject = createActorTools({
                        "name": $scope.usecase_actorname,
                        "pos_Y": randomPosition(400, 70)
                    });

                    $scope.actorsUseCase.push({
                        "obj_actor": actorobject,
                        "name": actorobject.getName()
                    });

                    $scope.jsonUseCase.actors.push({
                        "obj_actor": actorobject,
                        "name": actorobject.getName()
                    });

                    if (flag)
                        $scope.utilWebSocket("createActorUC", {"form": $scope.usecase_actorname});

                    $scope.usecase_actorname = "";
                } else {
                    alertAll({"status": 3, "information": "Enter at least one actor's name"});
                }
            }
        } catch (ErrorMessage) {
            console.log(ErrorMessage);
            alertAll({
                "status": 4,
                "information": "[createActorUC]: " + ErrorMessage.message
            });
        }

    };

    $scope.addActorCreated = function (actorobject) {
        $scope.actorsUseCase.push({
            "obj_actor": actorobject,
            "name": actorobject.getName()
        });

    };

    // funcion para crear los casos de uso y reelacionarlo con los actores que desee de forma automatica
    $scope.createUseCaseUC = function (form, flag) {

        try {

            const modeledName = form.usecase_name_i.$modelValue || form.usecase_name.$modelValue;
            $scope.usecase_name_i = modeledName;
            $scope.usecase_porpuse_i = form.usecase_porpuse_i.$modelValue || form.usecase_porpuse.$modelValue;
            $scope.usecase_precondition_i = form.usecase_precondition_i.$modelValue || form.usecase_precondition.$modelValue;
            $scope.usecase_postcondition_i = form.usecase_postcondition_i.$modelValue || form.usecase_postcondition.$modelValue;
            $scope.usecase_description_original = form.usecase_description_original.$modelValue || form.usecase_description_interpret.$modelValue;

            if ($scope.nameEquals(modeledName, -1)) {
                return;
            }

            //crear objeto use case
            let usecase = createUseCase({
                "name": modeledName,
                "pos_Y": randomPosition(400, 70)
            });

            /**
             * recorre todos los actores que fueron agregados dentro de la descripcion del caso de uso para realizar su
             * respectiva relacion a todos los actores
             **/
            for (let index = 0; index < $scope.actorsUseCase.length; index++) {
                let actorUC = $scope.actorsUseCase[index].obj_actor;
                let relation = {};
                relation = createRelation({
                    "a": actorUC,
                    "b": usecase,
                    "type": UMLCommunication
                });

                //agrega las relaciones al json de los datos para el diagrama de casos de uso
                $scope.jsonUseCase.relations.push({
                    "type": "UMLCommunication",
                    "obj_relation": relation
                });

                if (!$scope.nameEqualsNoMess(actorUC.getName(), -1)) {
                    //agrega los actores que fueron creados por la descripcion del caso de uso existente
                    $scope.jsonUseCase.actors.push({
                        "obj_actor": actorUC,
                        "name": actorUC.getName()
                    });
                }


            }
            /**
             * Agrega finalmente el objeto del caso de uso y los datos de la descripcion del caso de uso al json de los
             * DATOS del json del diagrama de casos de uso
             **/
            //variable para agregar los diagramas de secuencia por cada caso de uso
            let secuence_diagram = {};

            secuence_diagram = createDiagram({
                "width": 100,
                "height": 81,
                "id_div": "areaDiagram",
                "diagram": UMLSequenceDiagram,
                "name": "Secuence Diagram - " + usecase.getName(),
                "interaction": false,
                "draw": false
            });

            const useCaseId = workspaceId("uc");
            const sequenceButton = {
                diagram: secuence_diagram,
                "name": "Secuence Diagram - " + usecase.getName(),
                "type": "UMLSequenceDiagram",
                "status": "C",
                "btnactive": "btn btn-sm button-dt mr-2 mt-1",
                "useCaseId": useCaseId,
                "parentDiagramId": $scope.activeUseCaseDiagram ? $scope.activeUseCaseDiagram.id : null
            };
            $scope.btnDiagramUml.push(sequenceButton);

            $scope.jsonUseCase.useCase.push({
                "id": useCaseId,
                "diagramId": $scope.activeUseCaseDiagram ? $scope.activeUseCaseDiagram.id : null,
                "obj_usecase": usecase,
                "name": form.usecase_name.$modelValue,
                "name_i": usecase.getName(),
                "porpuse": form.usecase_porpuse.$modelValue,
                "porpuse_i": form.usecase_porpuse_i.$modelValue || form.usecase_porpuse.$modelValue,
                "pre_condition": form.usecase_precondition.$modelValue,
                "pre_condition_i": form.usecase_precondition_i.$modelValue || form.usecase_precondition.$modelValue,
                "post_condition": form.usecase_postcondition.$modelValue,
                "post_condition_i": form.usecase_postcondition_i.$modelValue || form.usecase_postcondition.$modelValue,
                "description_interpret": form.usecase_description_interpret.$modelValue,
                "description_original": form.usecase_description_original.$modelValue || form.usecase_description_interpret.$modelValue,
                "main_stage": Object.assign([], $scope.manager_maf.main_stage),
                "alternative_flow": Object.assign([], $scope.manager_maf.alternative_flow),
                "secuence_diagram": secuence_diagram,
                "secuence_button": sequenceButton,
                "secuence_data": {
                    "table_normal_flow": Object.assign([], $scope.table_normal_flow),
                    "table_loop": Object.assign([], $scope.table_loop),
                    "table_alternative_if": Object.assign([], $scope.table_alternative_if),
                    "table_alternative_else": Object.assign([], $scope.table_alternative_else)
                }
            });
            superInterpret();
            if (flag)
                $scope.utilWebSocket("createUseCase", {
                    "form": JSON.decycle(form),
                    "manager_maf": Object.assign({}, $scope.manager_maf),
                    "table_normal_flow": Object.assign([], $scope.table_normal_flow),
                    "table_loop": Object.assign([], $scope.table_loop),
                    "table_alternative_if": Object.assign([], $scope.table_alternative_if),
                    "table_alternative_else": Object.assign([], $scope.table_alternative_else)
                });

            clearFormUseCase(form, flag);
            updateClassDiagram($scope.jsonClass, "C");

            for (ipack = 0; ipack < $scope.jsonClass.diagram.length; ipack++) {
                for (isec = 0; isec < $scope.jsonUseCase.useCase.length; isec++) {
                    let secuence = $scope.jsonUseCase.useCase[isec];
                    if (secuence.name === usecase.getName())
                        updateSecuenceDiagram(secuence.secuence_diagram, secuence.secuence_data);
                }
            }

            //$scope.flag_update = true;
            diagramUseCase.draw();
            //cerramos el modal del caso de uso
            $("#modal_usecase").modal('hide');

        } catch (ErrorMessage) {
            console.log(ErrorMessage);
            alertAll({
                "status": 4,
                "information": "[createUseCaseUC]: " + ErrorMessage.message
            });
        }
    };

    $scope.angularSuperInterpret = function () {
        superInterpret();
    };

    $scope.angularUpdateClassDiagram = function () {
        updateClassDiagram($scope.jsonClass, "C");
    };

    //funcion para interpretar la descripcion del caso de uso y sus acciones
    function superInterpret() {
        try {
            // Manual editing only parses the explicit symbol language locally.
            // AI is invoked exclusively from the requirements-document workflow.
            $scope.jsonClass.diagram.length = 0;
            $scope.jsonClass.relationships.length = 0;
            let useCase = getAllUseCases();
            let minjson = [];
            for (let x = 0; x < useCase.length; x++) {
                let usecase_name = useCase[x].name;
                let description_interpret = useCase[x].description_interpret;
                let usecase_postcondition_i = useCase[x].post_condition;
                let usecase_precondition_i = useCase[x].pre_condition;
                let usecase_porpuse_i = useCase[x].porpuse;
                minjson = locallyInterpret(usecase_name === undefined ? "" : usecase_name);

                mergeClassDiagram($scope.jsonClass, minjson[1]);
                minjson = locallyInterpret(usecase_porpuse_i === undefined ? "" : usecase_porpuse_i);
                mergeClassDiagram($scope.jsonClass, minjson[1]);
                minjson = locallyInterpret(usecase_precondition_i === undefined ? "" : usecase_precondition_i);
                mergeClassDiagram($scope.jsonClass, minjson[1]);
                minjson = locallyInterpret(usecase_postcondition_i === undefined ? "" : usecase_postcondition_i);
                mergeClassDiagram($scope.jsonClass, minjson[1]);
                minjson = locallyInterpret(description_interpret === undefined ? "" : description_interpret);
                mergeClassDiagram($scope.jsonClass, minjson[1]);
                const mainStage = useCase[x].main_stage || [];
                for (let y = 0; y < mainStage.length; y++) {
                    // action_original
                    let action_original = mainStage[y].action_original;
                    minjson = locallyInterpret(action_original);
                    mergeClassDiagram($scope.jsonClass, minjson[1]);
                }

                const alternativeFlow = useCase[x].alternative_flow || [];
                for (let z = 0; z < alternativeFlow.length; z++) {
                    // action_original
                    let action_original = alternativeFlow[z].action_original;
                    minjson = locallyInterpret(action_original);
                    mergeClassDiagram($scope.jsonClass, minjson[1]);
                }
            }

            if ($scope.jsonClass.diagram.length > 0) {
                // agregar ls fk a las tablas relacionadas
                $scope.addForeingKey($scope.jsonClass.diagram[0].class, $scope.jsonClass.relationships);
            }

        } catch (ErrorMessage) {
            console.log(ErrorMessage);
            alertAll({
                "status": 4,
                "information": "[superInterpret]: " + ErrorMessage.message
            });
        }
    }

    $scope.addForeingKey = (arrayClass, arrayRelations) => {
        for (let positionRel = 0; positionRel < arrayRelations.length; positionRel++) {

            let rel = arrayRelations[positionRel];

            if (rel !== undefined) {

                let nameClassFrom = rel.from === undefined ? "" : rel.from;
                let nameClassTo = rel.to === undefined ? "" : rel.to;
                let from_fk = rel.from_fk === undefined ? "" : rel.from_fk;
                let to_fk = rel.to_fk === undefined ? "" : rel.to_fk;
                let simbol = rel.simbol === undefined ? "" : rel.simbol;
                let type = "fk";

                if (simbol === "->") {
                    type = "enumeration";
                }
                for (let positionClass = 0; positionClass < arrayClass.length; positionClass++) {
                    if (arrayClass[positionClass].modifiers === "") {
                        let objectClass = arrayClass[positionClass];
                        if (objectClass.className === nameClassFrom) {
                            objectClass.attributes.push({
                                name: from_fk,
                                type: type,
                                visibility: "private",
                                cardinalidate: arrayRelations[positionRel].cardinalidate,
                                idToOrFrom: $scope.getIdClass(arrayRelations[positionRel].to)
                            });
                        } else if (objectClass.className === nameClassTo) {
                            let cardinalidate = arrayRelations[positionRel].cardinalidate;
                            let cardSplit = cardinalidate.toString().split("..");

                            objectClass.attributes.push({
                                name: to_fk,
                                type: type,
                                visibility: "private",
                                cardinalidate: cardSplit[1] + ".." + cardSplit[0],
                                idToOrFrom: $scope.getIdClass(arrayRelations[positionRel].from)
                            }
                            );
                        }
                    }
                }
            }
        }
    };

    // obtener el id de una clase
    $scope.getIdClass = (nameClass) => {
        let varReturn = "-1";
        if ($scope.jsonClass.diagram[0].class.length > 0) {
            for (let i = 0; i < $scope.jsonClass.diagram[0].class.length; i++) {
                classObject = $scope.jsonClass.diagram[0].class[i];
                if (classObject.className === nameClass) {

                    // pregunto si se encuentra al menos un atributo de la clase
                    if (classObject.attributes[0] === undefined) {
                        if (classObject.modifiers === "") {
                            classObject.attributes.push({name: "id", type: "Int", visibility: "private"});
                            varReturn = classObject.attributes[0].name;
                        }

                    } else {
                        // procedo a retornar la posicion 0 que es el id de la clase
                        varReturn = classObject.attributes[0].name;
                    }
                }
            }
        }
        return varReturn;
    };

    //funcion para editar los casos de uso
    $scope.updateUseCase = function (form, flag) {

        try {

            const modeledName = form.usecase_name_i.$modelValue || form.usecase_name.$modelValue;
            const editedUseCase = $scope.jsonUseCase.useCase[$scope.encontrado];

            if ($scope.nameEquals(modeledName, $scope.encontrado)) {
                return;
            }

            //asignamos el nuevo nombre del caso de uso
            $scope.object_update.setName(modeledName);
            diagramUseCase.draw();
            //asignamos los nuevos datos al casos de uso
            $scope.jsonUseCase.useCase[$scope.encontrado].name = form.usecase_name.$modelValue;
            $scope.jsonUseCase.useCase[$scope.encontrado].name_i = modeledName;
            $scope.jsonUseCase.useCase[$scope.encontrado].porpuse = form.usecase_porpuse.$modelValue;
            $scope.jsonUseCase.useCase[$scope.encontrado].porpuse_i = form.usecase_porpuse_i.$modelValue || form.usecase_porpuse.$modelValue;
            $scope.jsonUseCase.useCase[$scope.encontrado].pre_condition = form.usecase_precondition.$modelValue;
            $scope.jsonUseCase.useCase[$scope.encontrado].pre_condition_i = form.usecase_precondition_i.$modelValue || form.usecase_precondition.$modelValue;
            $scope.jsonUseCase.useCase[$scope.encontrado].post_condition = form.usecase_postcondition.$modelValue;
            $scope.jsonUseCase.useCase[$scope.encontrado].post_condition_i = form.usecase_postcondition_i.$modelValue || form.usecase_postcondition.$modelValue;
            $scope.jsonUseCase.useCase[$scope.encontrado].description_interpret = form.usecase_description_interpret.$modelValue;
            $scope.jsonUseCase.useCase[$scope.encontrado].description_original = form.usecase_description_original.$modelValue || form.usecase_description_interpret.$modelValue;
            if (editedUseCase.secuence_button) editedUseCase.secuence_button.name = "Sequence Diagram - " + modeledName;
            editedUseCase.secuence_diagram.setName("Sequence Diagram - " + modeledName);

            //preguntaremos si se agregaron nuevos actores dentro del caso de uso
            let relations_usecase = $scope.object_update.getRelations(); // sacamos las relaciones del caso de uso
            let index_Y = 0, index_actor_relation = 0;
            if (relations_usecase.length > 0) {
                for (let index_X = 0; index_X < $scope.actorsUseCase.length;
                        index_Y === relations_usecase.length ? index_X++ : index_X) {
                    for (index_Y = 0; index_Y < relations_usecase.length; index_Y++) {
                        //pregunto si este actor no se encuentra en las relaciones de este caso de uso
                        if ($scope.actorsUseCase[index_X].obj_actor.getName() !==
                                relations_usecase[index_Y]._elemA.getName()) {
                            index_actor_relation++;
                        }

                        if (index_actor_relation === relations_usecase.length) {
                            let relation = createRelation({
                                "a": $scope.actorsUseCase[index_X].obj_actor,
                                "b": $scope.object_update,
                                "type": UMLCommunication
                            });

                            //agrega las relaciones al json de los datos para el diagrama de casos de uso
                            $scope.jsonUseCase.relations.push({
                                "obj_relation": relation
                            });

                            if (!$scope.nameEqualsNoMess($scope.actorsUseCase[index_X].obj_actor.getName(), -1)) {
                                //agrega los actores que fueron creados por la descripcion del caso de uso existente
                                $scope.jsonUseCase.actors.push({
                                    "obj_actor": $scope.actorsUseCase[index_X].obj_actor
                                });
                            }
                            index_actor_relation = 0;
                        }
                    }
                    index_actor_relation = 0;
                }
            } else {
                for (let index_X = 0; index_X < $scope.actorsUseCase.length; index_X++) {
                    let relation = createRelation({
                        "a": $scope.actorsUseCase[index_X].obj_actor,
                        "b": $scope.object_update,
                        "type": UMLCommunication
                    });

                    //agrega las relaciones al json de los datos para el diagrama de casos de uso
                    $scope.jsonUseCase.relations.push({
                        "obj_relation": relation
                    });

                    if (!$scope.nameEqualsNoMess($scope.actorsUseCase[index_X].obj_actor.getName(), -1)) {
                        //agrega los actores que fueron creados por la descripcion del caso de uso existente
                        $scope.jsonUseCase.actors.push({
                            "obj_actor": $scope.actorsUseCase[index_X].obj_actor
                        });
                    }
                }
            }

            $scope.jsonUseCase.useCase[$scope.encontrado].main_stage =
                    Object.assign([], $scope.manager_maf.main_stage);
            $scope.jsonUseCase.useCase[$scope.encontrado].alternative_flow =
                    Object.assign([], $scope.manager_maf.alternative_flow);

            $scope.jsonUseCase.useCase[$scope.encontrado].secuence_data.table_normal_flow =
                    Object.assign([], $scope.table_normal_flow);
            $scope.jsonUseCase.useCase[$scope.encontrado].secuence_data.table_loop =
                    Object.assign([], $scope.table_loop);
            $scope.jsonUseCase.useCase[$scope.encontrado].secuence_data.table_alternative_if =
                    Object.assign([], $scope.table_alternative_if);
            $scope.jsonUseCase.useCase[$scope.encontrado].secuence_data.table_alternative_else =
                    Object.assign([], $scope.table_alternative_else);

            $scope.flag_update = false;
            superInterpret();

            if (flag)
                $scope.utilWebSocket("updateUseCase", {
                    "form": JSON.decycle(form),
                    "encontrado": $scope.encontrado,
                    "main_stage": Object.assign([], $scope.jsonUseCase.useCase[$scope.encontrado].main_stage),
                    "alternative_flow": Object.assign([], $scope.jsonUseCase.useCase[$scope.encontrado].alternative_flow),
                    "table_normal_flow": Object.assign([], $scope.jsonUseCase.useCase[$scope.encontrado].table_normal_flow),
                    "table_loop": Object.assign([], $scope.jsonUseCase.useCase[$scope.encontrado].table_loop),
                    "table_alternative_if": Object.assign([], $scope.jsonUseCase.useCase[$scope.encontrado].table_alternative_if),
                    "table_alternative_else": Object.assign([], $scope.jsonUseCase.useCase[$scope.encontrado].table_alternative_else)
                });

            clearFormUseCase(form, flag);
            updateClassDiagram($scope.jsonClass, "U");
            updateSecuenceDiagram(editedUseCase.secuence_diagram, editedUseCase.secuence_data);
            diagramUseCase.draw();
            //cerramos el modal del caso de uso
            $("#modal_usecase").modal('hide');
        } catch (ErrorMessage) {
            console.log(ErrorMessage);
            alertAll({
                "status": 4,
                "information": "[updateUseCase]: " + ErrorMessage.message
            });
        }

    };

    //funcion para limpiar el formuario de caso de uso
    function clearFormUseCase(form, flag) {
        //$scope.$apply(() => {
        //limpiar formulario
        $scope.usecase_name_i = "";
        $scope.usecase_name = "";
        $scope.usecase_porpuse = "";
        $scope.usecase_porpuse_i = "";
        $scope.usecase_precondition = "";
        $scope.usecase_precondition_i = "";
        $scope.usecase_postcondition = "";
        $scope.usecase_postcondition_i = "";
        $scope.usecase_description_interpret = "";
        $scope.usecase_description_original = "";

        //});

        if (flag) {
            if (form !== undefined) {
                form.$setPristine();
                form.$setUntouched();
            }
        }

        //limpiamos mi variable auxiliar
        $scope.actorsUseCase.length = 0;
        $scope.manager_maf.main_stage.length = 0;
        $scope.manager_maf.alternative_flow.length = 0;

        $scope.table_normal_flow.length = 0;
        $scope.table_loop.length = 0;
        $scope.table_alternative_if.length = 0;
        $scope.table_alternative_else.length = 0;
        $scope.alternate_flow_begins = "";
        $scope.alternative_condition = "";
        $scope.actor_fa = "";
        $scope.alternative_outcome = "return";
        $scope.return_step = "";
        $scope.action_fa = "";
        $scope.index_secuence = 0;
        $scope.step = 0;
        // });s
    }

    //funcion para interpretar la descripcion general del caso de uso
    $scope.interpretUseCaseDescription = function (flag, scope_usecase, scope_original) {
        let minjson = $scope.interpretUseCase(scope_usecase.$modelValue, false);

        // leer todas las notificaciones que se ingresan en ese momento
        for (let i = 0; i < minjson[1].notifications.length; i++) {
            let notification = minjson[1].notifications[i];
            $scope.notifications.push(notification);
        }
        switch (scope_original) {
            case "usecase_description_original" :
                $scope.usecase_description_original = minjson[0];
                break;
            case "usecase_postcondition_i":
                $scope.usecase_postcondition_i = minjson[0];
                break;
            case "usecase_precondition_i":
                $scope.usecase_precondition_i = minjson[0];
                break;
            case "usecase_porpuse_i":
                $scope.usecase_porpuse_i = minjson[0];
                break;
            case "usecase_name_i":
                $scope.usecase_name_i = minjson[0];
                break;
        }

        mergeClassDiagram($scope.jsonClass, minjson[1]);
        $scope.updateSecuence($scope.jsonClass);

        if (flag)
            $scope.utilWebSocket("interpretUseCaseDescription", {"form": scope_usecase.$modelValue});

        alertAll({
            "status": 2,
            "information": "Text interpreted successfully."
        });
    };

    //funcion para interpretar la accion a editar
    $scope.interpretUseCaseAction = function () {
        let minjson = $scope.interpretUseCase($scope.action_original, false);
        mergeClassDiagram($scope.jsonClass, minjson[1]);
        $scope.updateSecuence($scope.jsonClass);
        $scope.action_interpret = minjson[0];
        alertAll({
            "status": 2,
            "information": "Text interpreted successfully."
        });
    };

    //funcion para agregar el escenario principal del caso de uso
    $scope.mainStage = function (form) {
        let minjson = $scope.interpretUseCase(form.action_ms.$viewValue, false);

        mergeClassDiagram($scope.jsonClass, minjson[1]);
        $scope.updateSecuence($scope.jsonClass);
        $scope.manager_maf.main_stage.push({
            "actor": form.actor_ms.$viewValue,
            "action_interpret": minjson[0],
            /*"action_interpret": $scope.manager_maf.main_stage.length > 0 && form.check_final_step.$viewValue === false ? minjson[0] :
             form.check_final_step.$viewValue === true ? 'This use case ends when ' + minjson[0] :
             form.check_final_step.$viewValue === false ? 'This use case starts when ' + minjson[0] :
             minjson[0]*/
            "action_original": form.action_ms.$viewValue
        });

        //limpiar form
        $scope.actor_ms = "";
        $scope.action_ms = "";
        $scope.check_final_step = false;
        form.$setPristine();
        form.$setUntouched();
    };

    const alternativeReference = (flow, key) => {
        const value = key === "start" ? (flow.start_step || flow.he_passed) : (flow.return_step || flow.return);
        return value === undefined || value === null || value === "" ? null : parseInt(value, 10);
    };

    const setAlternativeReference = (flow, key, value) => {
        if (key === "start") {
            flow.start_step = value;
            flow.he_passed = value;
        } else {
            flow.return_step = value;
            flow.return = value;
        }
    };

    const normalizeAlternativeFlow = (flow) => {
        flow.id = flow.id || workspaceId("af");
        flow.actor = flow.actor || "System";
        flow.condition = flow.condition || "Alternative condition";
        flow.outcome = flow.outcome || (alternativeReference(flow, "return") === null ? "end" : "return");
        setAlternativeReference(flow, "start", alternativeReference(flow, "start"));
        setAlternativeReference(flow, "return", flow.outcome === "return" ? alternativeReference(flow, "return") : null);
        if (typeof flow.action_interpret === "string") {
            flow.action_interpret = flow.action_interpret.replace(/\s*\(return to step\s+\d+\)\s*$/i, "");
        }
        return flow;
    };

    const remapAlternativeReferencesOnMove = (oldIndex, newIndex) => {
        const oldStep = oldIndex + 1;
        const newStep = newIndex + 1;
        if (oldStep === newStep) return;
        $scope.manager_maf.alternative_flow.forEach(flow => {
            ["start", "return"].forEach(key => {
                let reference = alternativeReference(flow, key);
                if (reference === null) return;
                if (reference === oldStep) reference = newStep;
                else if (oldStep < newStep && reference > oldStep && reference <= newStep) reference--;
                else if (newStep < oldStep && reference >= newStep && reference < oldStep) reference++;
                setAlternativeReference(flow, key, reference);
            });
        });
    };

    const removeMainStepDependencies = (removedIndex) => {
        const removedStep = removedIndex + 1;
        $scope.manager_maf.alternative_flow = $scope.manager_maf.alternative_flow.filter(flow => {
            const start = alternativeReference(flow, "start");
            const returns = alternativeReference(flow, "return");
            return start !== removedStep && returns !== removedStep;
        });
        $scope.manager_maf.alternative_flow.forEach(flow => {
            const start = alternativeReference(flow, "start");
            const returns = alternativeReference(flow, "return");
            if (start !== null && start > removedStep) setAlternativeReference(flow, "start", start - 1);
            if (returns !== null && returns > removedStep) setAlternativeReference(flow, "return", returns - 1);
        });
    };

    //funcion para eliminar una accion del escenario principal
    $scope.removeActionMS = function (index, fluje) {
        const removedStep = index + 1;
        const dependentAlternatives = fluje === 'M' ? $scope.manager_maf.alternative_flow.filter(flow =>
            alternativeReference(flow, "start") === removedStep || alternativeReference(flow, "return") === removedStep).length : 0;
        Swal.fire({
            title: 'Are you sure?',
            text: dependentAlternatives > 0 ?
                "This step is referenced by " + dependentAlternatives + " alternative flow(s). Those alternatives will also be removed." :
                "You won't be able to revert this!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, remove it!'
        }).then((result) => {
            if (result.isConfirmed) {
                if (fluje === 'M') {
                    $scope.$apply(function () {
                        removeMainStepDependencies(index);
                        $scope.manager_maf.main_stage.splice(index, 1);
                    });
                } else {
                    $scope.$apply(function () {
                        $scope.manager_maf.alternative_flow.splice(index, 1);
                    });
                }
            }
        });
    };

    //funcion para eliminar un actor dentro de la descripcion del caso de uso con sus respectivas acciones
    $scope.removeActorUC = function (obj, positionActor) {
        Swal.fire({
            title: 'Are you sure?',
            text: "All data entered will be permanently deleted.!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, deletes all!'
        }).then((result) => {
            if (result.isConfirmed) {
                let elem = diagramUseCase.getElementByPoint(obj.obj_actor.getX(), obj.obj_actor.getY());
                diagramUseCase.delElement(elem);
                diagramUseCase.draw();
                let lengthMainStage = $scope.manager_maf.main_stage.length;
                if (lengthMainStage > 0) {
                    for (let indexX = (lengthMainStage - 1); indexX > -1; indexX--) {
                        if ($scope.manager_maf.main_stage[indexX].actor === obj.obj_actor.getName()) {
                            $scope.$apply(() => {
                                $scope.manager_maf.main_stage.splice(indexX, 1);
                            });
                        }
                    }
                    for (let alternativeIndex = $scope.manager_maf.alternative_flow.length - 1; alternativeIndex >= 0; alternativeIndex--) {
                        if (($scope.manager_maf.alternative_flow[alternativeIndex].actor || "System") === obj.obj_actor.getName()) {
                            $scope.manager_maf.alternative_flow.splice(alternativeIndex, 1);
                        }
                    }
                    $scope.$apply(() => {
                        let positionObj = searchPositionEqualsObject($scope.jsonUseCase.actors, "name", $scope.actorsUseCase[positionActor].name);
                        if (positionObj[0]) {
                            $scope.jsonUseCase.actors.splice(positionObj[1], 1);
                            $scope.actorsUseCase.splice(positionActor, 1);
                        }
                    });
                    alertAll({
                        "status": 2,
                        "information": "Actor successfully eliminated, shares belonging to the actor were also eliminated."
                    });
                } else {
                    $scope.$apply(() => {
                        let positionObj = searchPositionEqualsObject($scope.jsonUseCase.actors, "name", $scope.actorsUseCase[positionActor].name);
                        if (positionObj[0]) {
                            $scope.jsonUseCase.actors.splice(positionObj[1], 1);
                            $scope.actorsUseCase.splice(positionActor, 1);
                        }
                    });
                    alertAll({
                        "status": 2,
                        "information": "Actor successfully eliminated, shares belonging to the actor were also eliminated."
                    });
                }
            }
        });
    };


    /**
     * funcion para abrir el modal y editar la acciono
     * @param {obj} obj
     * @param {fluje} fluje 
     * 
     * @version 1.0.1 - 22-07-2023
     * */
    $scope.openModalUpdateAction = function (obj, fluje) {
        $("#modal_update_duc").modal();
        $scope.update_action = obj;
        $scope.updateFlowType = fluje;
        if (fluje === 'M') {
            $scope.action_interpret = $scope.update_action.action_interpret;
            $scope.action_original = $scope.update_action.action_original;
            $scope.actor = $scope.update_action.actor;
            $scope.position = String($scope.manager_maf.main_stage.indexOf(obj));
            $scope.positionActual = $scope.manager_maf.main_stage.indexOf(obj) + 1;
        } else {
            $scope.action_interpret = $scope.update_action.action_interpret;
            $scope.action_original = $scope.update_action.action_original;
            $scope.actor = $scope.update_action.actor || "System";
            $scope.position = String($scope.manager_maf.alternative_flow.indexOf(obj));
            $scope.positionActual = $scope.manager_maf.alternative_flow.indexOf(obj) + 1;
            $scope.alternativeEditStart = String(alternativeReference(obj, "start") || "");
            $scope.alternativeEditCondition = obj.condition || "Alternative condition";
            $scope.alternativeEditOutcome = obj.outcome || (alternativeReference(obj, "return") === null ? "end" : "return");
            $scope.alternativeEditReturn = alternativeReference(obj, "return") === null ? "" : String(alternativeReference(obj, "return"));
        }
    };

    /**
     * funcion para editar las acciones del flujo normal de eventos
     * */

    //funcion para actualizar los datos de la accion editada
    $scope.updateActionMSFA = function (form) {
        if (form.$valid) {
            $scope.update_action.action_interpret = form.action_interpret.$viewValue;
            $scope.update_action.action_original = form.action_original.$viewValue;
            $scope.update_action.actor = form.actor.$viewValue;

            const newPosition = parseInt($scope.position, 10);
            if ($scope.updateFlowType === 'M') {
                const oldPosition = $scope.positionActual - 1;
                let objectActual = $scope.manager_maf.main_stage.splice(oldPosition, 1)[0];
                $scope.manager_maf.main_stage.splice(newPosition, 0, objectActual);
                remapAlternativeReferencesOnMove(oldPosition, newPosition);
            } else {
                const start = parseInt($scope.alternativeEditStart, 10);
                const outcome = $scope.alternativeEditOutcome;
                const returnStep = outcome === "return" ? parseInt($scope.alternativeEditReturn, 10) : null;
                if (!start || (outcome === "return" && (!returnStep || returnStep < start))) {
                    alertAll({status: 3, information: "The return step must be the origin step or a later step."});
                    return;
                }
                const condition = ($scope.alternativeEditCondition || "").trim();
                if (!condition) {
                    alertAll({status: 3, information: "Describe the condition that activates this alternative."});
                    return;
                }
                $scope.update_action.condition = condition;
                $scope.update_action.outcome = outcome;
                setAlternativeReference($scope.update_action, "start", start);
                setAlternativeReference($scope.update_action, "return", returnStep);
                let alternativeActual = $scope.manager_maf.alternative_flow.splice($scope.positionActual - 1, 1)[0];
                $scope.manager_maf.alternative_flow.splice(newPosition, 0, alternativeActual);
            }

            // buscar la posicion a donde se va cambiar los datos
            /*let objetoCambiarPosicion =  $scope.manager_maf.main_stage[$scope.position];
             let objectActual = $scope.update_action;*/

            /*objectActual.action_interpret = objetoCambiarPosicion.action_interpret;
             objectActual.action_original = objetoCambiarPosicion.action_original;
             objectActual.actor = objetoCambiarPosicion.actor;
             
             $scope.manager_maf.main_stage[$scope.position].action_interpret = form.action_interpret.$viewValue;
             $scope.manager_maf.main_stage[$scope.position].action_original = form.action_original.$viewValue;
             $scope.manager_maf.main_stage[$scope.position].actor = form.actor.$viewValue;
             */
            $("#modal_update_duc").modal('hide');
        }
    };

    //funcion para agregar el flujo alternativo del caso de uso
    $scope.flowAlternate = function (form) {
        if (form.$invalid || $scope.manager_maf.main_stage.length === 0) return;
        const startStep = parseInt(form.alternate_flow_begins.$viewValue, 10);
        const outcome = form.alternative_outcome.$viewValue;
        const returnStep = outcome === "return" ? parseInt(form.return_step.$viewValue, 10) : null;
        const condition = (form.alternative_condition.$viewValue || "").trim();
        const action = (form.action_fa.$viewValue || "").trim();
        if (!condition || !action) {
            alertAll({status: 3, information: "Enter both the alternative condition and its action."});
            return;
        }
        if (outcome === "return" && (!returnStep || returnStep < startStep)) {
            alertAll({status: 3, information: "The return step must be the origin step or a later step."});
            return;
        }
        let minjson = $scope.interpretUseCase(action, false);
        mergeClassDiagram($scope.jsonClass, minjson[1]);
        $scope.updateSecuence($scope.jsonClass);
        $scope.manager_maf.alternative_flow.push({
            "id": workspaceId("af"),
            "he_passed": startStep,
            "start_step": startStep,
            "condition": condition,
            "actor": form.actor_fa.$viewValue,
            "action_interpret": minjson[0],
            "action_original": action,
            "outcome": outcome,
            "return": returnStep,
            "return_step": returnStep
        });
        $scope.alternate_flow_begins = "";
        $scope.alternative_condition = "";
        $scope.actor_fa = "";
        $scope.alternative_outcome = "return";
        $scope.return_step = "";
        $scope.action_fa = "";
        form.$setPristine();
        form.$setUntouched();
    };

    //funcion para agregar las clases y metodos para generar el diagrama de clases
    $scope.updateSecuence = (jsonClass) => {
        if (jsonClass.diagram === undefined) {
            return;
        }
        if (jsonClass.diagram.length > 0) {
            $scope.secuenceData.package.length = 0;
            for (let ipackage = 0; ipackage < jsonClass.diagram.length; ipackage++) {
                let package = jsonClass.diagram[ipackage];
                $scope.secuenceData.package.push({packagename: package.packName, objects: []});
                //buscamos las clases
                for (let iclass = 0; iclass < package.class.length; iclass++) {
                    let classdiagram = package.class[iclass];
                    $scope.secuenceData.package[ipackage].objects.push({
                        objectname: classdiagram.className.toString().toLowerCase() + ":" + classdiagram.className,
                        methods: []
                    });
                    for (let imethods = 0; imethods < classdiagram.methods.length; imethods++) {
                        let methods = classdiagram.methods[imethods];
                        $scope.secuenceData.package[ipackage].objects[iclass]
                                .methods.push({methodname: methods.name + "(" + getParamethersSecuence(methods.parameters) + ")"});
                    }
                }
            }
        }
    };

    getParamethersSecuence = (parameters) => {
        let rparam = "";
        if (parameters.length > 0) {
            for (let i = 0; i < parameters.length; i++) {
                if (i === parameters.length - 1) {
                    rparam += parameters[i].name;
                } else {
                    rparam += parameters[i].name + ",";
                }
            }
        }
        return rparam;
    };

    /**
     * ###############################################################################################
     * DIAGRAMA DE SECUENCIA
     * ###############################################################################################
     * */

    $scope.table_normal_flow = [];
    $scope.table_loop = [];
    $scope.table_alternative_if = [];
    $scope.table_alternative_else = [];
    $scope.index_secuence = 0;
    $scope.step = 0;

    $scope.addNormalFlow = (form) => {
        if (form.$valid) {
            const packageData = $scope.secuenceData.package[$scope.s_package];
            const fromObject = packageData && packageData.objects[$scope.s_object1];
            const toObject = packageData && packageData.objects[$scope.s_object2];
            const selectedMessage = form.nf_objectmethods1.$modelValue || form.nf_objectmethods2x.$modelValue;
            if (!fromObject || !toObject || fromObject === toObject || !selectedMessage) {
                alertAll({status: 3, information: "Select two different objects and the method that will be invoked."});
                return;
            }
            $scope.step = $scope.step + 1;
            $scope.table_normal_flow.push({
                from: fromObject.objectname,
                to: toObject.objectname,
                message: $scope.step.toString() + ") " + selectedMessage,
                message_type: form.mnf_message.$modelValue,
                response: form.nf_objectresponse2.$modelValue === undefined || form.mnf_message.$modelValue === "async_message" ? "" : form.nf_objectresponse2.$modelValue,
                response_type: form.rnf_response.$modelValue === undefined || form.mnf_message.$modelValue === "async_message" ? "" : form.rnf_response.$modelValue,
                index: $scope.index_secuence = ($scope.index_secuence + 1)
            });

            $scope.nf_objectmethods1 = null;
            $scope.nf_objectmethods2 = "";
            $scope.mnf_message = "";
            $scope.rnf_response = "";
            form.$setPristine();
            form.$setUntouched();

            console.log("SECUENCIA FLUJO NORMAL", $scope.table_normal_flow);
            $scope.secuenceData.secuence.normal.push($scope.table_normal_flow);
        }
    };

    $scope.generateSequenceFromUseCaseFlow = () => {
        const useCase = $scope.encontrado >= 0 ? $scope.jsonUseCase.useCase[$scope.encontrado] : null;
        const flow = useCase ? (useCase.main_stage || []) : ($scope.manager_maf.main_stage || []);
        if (!flow.length) {
            alertAll({status: 3, information: "Add the normal use-case flow before generating its sequence draft."});
            return;
        }
        const draft = [];
        flow.forEach((step, index) => {
            let target = "Application service";
            let message = step.action_interpret || step.action_original || "Execute step";
            try {
                const parsed = locallyInterpret(step.action_original || "")[1];
                const packageWithClass = (parsed.diagram || []).find(item => item.class && item.class.length);
                if (packageWithClass) {
                    const modeledClass = packageWithClass.class[0];
                    target = modeledClass.className.toLowerCase() + ":" + modeledClass.className;
                    if (modeledClass.methods && modeledClass.methods.length) message = modeledClass.methods[0].name + "()";
                }
            } catch (error) {
                // Natural-language-only steps keep their readable action text.
            }
            draft.push({
                from: step.actor || "System",
                to: target,
                message: (index + 1) + ") " + message,
                message_type: "sync_message",
                response: "",
                response_type: "",
                index: index + 1
            });
        });
        $scope.table_normal_flow = draft;
        $scope.step = draft.length;
        $scope.index_secuence = draft.length;
        alertAll({status: 2, information: "Sequence draft generated. Review the participating objects before saving the use case."});
    };

    $scope.addLoop = (form) => {
        if (form.$valid) {
            $scope.step = $scope.step + 1;
            $scope.table_loop.push({
                from: $scope.secuenceData.package[$scope.s_package].objects[$scope.s_object1].objectname,
                to: $scope.secuenceData.package[$scope.s_package].objects[$scope.s_object2].objectname,
                message: $scope.step.toString() + ")" + $scope.secuenceData.package[$scope.s_package].objects[$scope.s_object1].methods[form.slip_objectmethods1.$modelValue].methodname,
                message_type: form.msl_message.$modelValue,
                response: form.loop_objectmethods2.$modelValue === undefined || form.msl_message.$modelValue === "async_message" ? "" : form.loop_objectmethods2.$modelValue,
                response_type: form.rsl_response.$modelValue === undefined || form.msl_message.$modelValue === "async_message" ? "" : form.rsl_response.$modelValue
            });

            $scope.slip_objectmethods1 = "";
            $scope.msl_message = null;
            $scope.loop_objectmethods2 = "";
            $scope.rsl_response = null;

            console.log("LOOP", $scope.table_loop);
            $scope.secuenceData.secuence.loop = {
                condition: form.slip_condition.$modelValue,
                mr: $scope.table_loop,
                index: $scope.index_secuence++
            };
        }
    };

    $scope.addAlternativeIf = (form) => {
        if (form.$valid) {
            $scope.step = $scope.step + 1;
            $scope.table_alternative_if.push({
                from: $scope.secuenceData.package[$scope.s_package].objects[$scope.s_object1].objectname,
                to: $scope.secuenceData.package[$scope.s_package].objects[$scope.s_object2].objectname,
                message: $scope.step.toString() + ")" + $scope.secuenceData.package[$scope.s_package].objects[$scope.s_object1].methods[form.scif_objectmethods1.$modelValue].methodname,
                message_type: form.smcif_message.$modelValue,
                response: form.scif_objectmethods2.$modelValue === undefined ? "" : form.scif_objectmethods2.$modelValue,
                response_type: form.srcif_response.$modelValue === undefined ? "" : form.srcif_response.$modelValue
            });
            console.log("ALTERNATIVE IF", $scope.table_alternative_if);
            $scope.secuenceData.secuence.alternative.condition_if = form.ipscif_condition_if.$modelValue;
            $scope.secuenceData.secuence.alternative.mr_if = $scope.table_alternative_if;
            $scope.secuenceData.secuence.alternative.index = $scope.index_secuence++;
        }
    };

    $scope.addAlternativeElse = (form) => {
        if (form.$valid) {
            $scope.step = $scope.step + 1;
            $scope.table_alternative_else.push({
                from: $scope.secuenceData.package[$scope.s_package].objects[$scope.s_object1].objectname,
                to: $scope.secuenceData.package[$scope.s_package].objects[$scope.s_object2].objectname,
                message: $scope.step.toString() + ")" + $scope.secuenceData.package[$scope.s_package].objects[$scope.s_object1].methods[form.celse_objectmethods1.$modelValue].methodname,
                message_type: form.smcelse_message.$modelValue,
                response: form.celse_objectmethods2.$modelValue === undefined ? "" : form.celse_objectmethods2.$modelValue,
                response_type: form.smcelse_response.$modelValue === undefined ? "" : form.smcelse_response.$modelValue
            });
            console.log("ALTERNATIVE IF", $scope.table_alternative_else);
            $scope.secuenceData.secuence.alternative.else = form.ipscelse_condition_else.$modelValue;
            $scope.secuenceData.secuence.alternative.mr_else = $scope.table_alternative_else;

            console.log("JSON DIAGRAMA DE SECUENCIA", $scope.secuenceData.secuence);
        }
    };

    $scope.changeSM = () => {
        $scope.rsl_response = null;
    };

    /**
     * ##############################################################################################################
     * FIN BLOQUE DE CODIGO PARA LA DESCRIPCION DEL CASO DE USO
     * ##############################################################################################################
     * */

    /**
     * ##############################################################################################################
     * INICIO RELACIONES
     * ##############################################################################################################
     * */

    // funcion para crear la relacion de inclusion de los casos de uso
    $scope.createInclusion = function (form) {
        if (form.$valid) {
            let relation = {};
            relation = createRelation({
                "type": UMLInclude,
                "a": $scope.jsonUseCase.useCase[form.selectusecase1.$viewValue].obj_usecase,
                "b": $scope.jsonUseCase.useCase[form.selectusecase2.$viewValue].obj_usecase
            });
            $scope.jsonUseCase.relations.push({
                "type": "UMLInclude",
                "obj_relation": relation
            });
            $("#modal_inclusion").modal("hide");
        } else {
            alert("aguante :v");
        }
    };

    // funcion para crear la relacion de extension de los casos de uso
    $scope.createExtend = function (form) {
        if (form.$valid) {
            let relation = {};
            relation = createRelation({
                "type": UMLExtend,
                "a": $scope.jsonUseCase.useCase[form.selectusecase1.$viewValue].obj_usecase,
                "b": $scope.jsonUseCase.useCase[form.selectusecase2.$viewValue].obj_usecase
            });
            $scope.jsonUseCase.relations.push({
                "type": "UMLExtend",
                "obj_relation": relation
            });
            $("#modal_extend").modal("hide");
        } else {
            alert("aguante :v");
        }
    };

    // funcion para crear la relacion de generalizacion de los casos de uso
    $scope.createGeneralization = function (form) {
        if (form.$valid) {
            let relation = {};
            relation = createRelation({
                "type": UMLGeneralization,
                "a": $scope.jsonUseCase.useCase[form.selectusecase1.$viewValue].obj_usecase,
                "b": $scope.jsonUseCase.useCase[form.selectusecase2.$viewValue].obj_usecase
            });
            $scope.jsonUseCase.relations.push({
                "type": "UMLGeneralization",
                "obj_relation": relation
            });
            $("#modal_generalization").modal("hide");
        } else {
            alert("aguante :v");
        }
    };

    // funcion para crear la relacion de asociacion de los casos de uso
    $scope.createAssociation = function (form) {
        if (form.$valid) {
            let relation = {};
            relation = createRelation({
                "type": UMLCommunication,
                "a": $scope.jsonUseCase.actors[form.selecactor.$viewValue].obj_actor,
                "b": $scope.jsonUseCase.useCase[form.selectusecase.$viewValue].obj_usecase
            });
            $scope.jsonUseCase.relations.push({
                "type": "UMLCommunication",
                "obj_relation": relation
            });
            $("#modal_association").modal("hide");
        } else {
            alert("aguante :v");
        }
        console.log($scope.jsonUseCase);
    };

    /**
     * ##############################################################################################################
     * FIN RELACIONES
     * ##############################################################################################################
     * */

    /**
     * ##############################################################################################################
     * GUARDAR Y CARGAR EL MODELADO UML
     * ##############################################################################################################
     * */

    function customDecycle(obj) {
        return JSON.decycle(obj, (key, value) => {
            if (typeof value === 'string') {
                return value.replace(/''/g, "'");
            }
            return value;
        });
    }

    $scope.saveModeling = () => {
        console.log($scope.jsonUseCase);
        console.log(JSON.parse(JSON.stringify($scope.jsonClass)));
        var xml = (new DOMParser()).parseFromString('<applicationtag/>', 'text/xml');
        let secuence_data = [];
        let secuence_diagram = [];

        const workspaceDiagrams = $scope.useCaseDiagrams.map(entry => {
            (entry.data.useCase || []).forEach(useCase => {
                useCase.id = useCase.id || workspaceId("uc");
                useCase.diagramId = entry.id;
                if (useCase.secuence_button) {
                    useCase.secuence_button.useCaseId = useCase.id;
                    useCase.secuence_button.parentDiagramId = entry.id;
                }
                secuence_data.push({useCaseId: useCase.id, parentDiagramId: entry.id, data: useCase.secuence_data});
                secuence_diagram.push({useCaseId: useCase.id, parentDiagramId: entry.id, graph: (new XMLSerializer()).serializeToString(useCase.secuence_diagram.getXML(xml))});
            });
            return {
                id: entry.id,
                name: entry.name,
                data: JSON.decycle(entry.data),
                graph: (new XMLSerializer()).serializeToString(entry.diagram.getXML(xml))
            };
        });
        const legacyData = {
            useCase: getAllUseCases(),
            actors: $scope.useCaseDiagrams.reduce((all, entry) => all.concat(entry.data.actors || []), []),
            relations: $scope.useCaseDiagrams.reduce((all, entry) => all.concat(entry.data.relations || []), [])
        };
        $scope.jsonClass["edition"] = $scope.editionClasDiagram;
        if ($scope.jsonClass["diagram"] === undefined) {
            $scope.jsonClass["diagram"] = [];
        }

        if ($scope.jsonClass["relationships"] === undefined) {
            $scope.jsonClass["relationships"] = [];
        }

        let api_param = {
            "user_token": $scope.DatoUsuario.user_token,
            "idproj": id_project,
            "dataJson": JSON.stringify([
                {
                    diagramType: "duml",
                    dataJson: {
                        "data": JSON.decycle(legacyData),
                        graph: (new XMLSerializer()).serializeToString(diagramUseCase.getXML(xml)),
                        workspace: {
                            schemaVersion: 2,
                            activeDiagramId: $scope.activeUseCaseDiagram ? $scope.activeUseCaseDiagram.id : null,
                            useCaseDiagrams: workspaceDiagrams
                        }
                    },
                    base64: ""
                }, {
                    diagramType: "dclass",
                    dataJson: {
                        "data": Object.assign({}, $scope.jsonClass),
                        graph: (new XMLSerializer()).serializeToString(diagramClass.getXML(xml))
                    },
                    base64: ""
                }, {
                    diagramType: "dseq",
                    dataJson: {
                        "data": secuence_data,
                        graph: secuence_diagram
                    },
                    base64: ""
                }, {
                    diagramType: "ctest",
                    dataJson: {
                        "data": $scope.jsonTdd,
                        graph: ""
                    },
                    base64: ""
                }
            ]),
            "module": "DiagramUml"
        };
        console.log(JSON.parse(api_param.dataJson));
        console.log(JSON.stringify(api_param));
        apiSaveModeling(JSON.stringify(api_param));
    };

    apiSaveModeling = (api_param) => {
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'projects/saveModuleFile',
            data: api_param,
            beforeSend: function () {
                loading();
            },
            success: function (data) {
                swal.close();
                alertAll(data);
            },
            error: function (objXMLHttpRequest) {
                console.log("error", objXMLHttpRequest);
                swal.fire("Error Severe", "Details: " + objXMLHttpRequest, "error");
            }
        });
    };

    $scope.loadUseCase = () => {
        let api_param = {
            "user_token": $scope.DatoUsuario.user_token,
            "idproj": id_project,
            "module": "DiagramUml"
        };
        apiLoadModeling(api_param);
        $(function () {
            $('[data-toggle="tooltip"]').tooltip()
        });
    };

    $scope.loadClass = () => {
        let api_param = {
            "user_token": $scope.DatoUsuario.user_token,
            "idproj": id_project,
            "module": "ClassDiagram"
        };
        apiLoadModeling(api_param);
    };

    $scope.loadSecuence = () => {
        let api_param = {
            "user_token": $scope.DatoUsuario.user_token,
            "idproj": id_project,
            "module": "SeguenceDiagram"
        };
        apiLoadModeling(api_param);
    };

    $scope.loadTddJUnit = () => {
        let api_param = {
            "user_token": $scope.DatoUsuario.user_token,
            "idproj": id_project,
            "module": "Test"
        };
        apiLoadModeling(api_param);
    };

    function hasDuplicateHashKeys(arr, key) {
        const seen = new Set();
        for (let item of arr[key]) {
            if (seen.has(item.$$hashKey)) {
                return true;
            }
            seen.add(item.$$hashKey);
        }
        return false;
    }

    function updateDuplicateHashKeys(arr, key) {
        const seen = new Set();
        let newHashKeyCounter = 0;

        arr[key].forEach(item => {
            if (seen.has(item.$$hashKey)) {
                // Asignar un nuevo $$hashKey único
                item.$$hashKey = `unique_${newHashKeyCounter++}`;
            }
            seen.add(item.$$hashKey);
        });

        return arr;
    }

    const graphWithCurrentProjectName = (graph) => {
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(graph, "text/xml");
        const umlSystem = xmlDoc.getElementsByTagName("UMLSystem")[0];
        if (umlSystem) {
            const item = umlSystem.getElementsByTagName("item")[0];
            if (item) item.setAttribute("value", $scope.nameProject);
        }
        return (new XMLSerializer()).serializeToString(xmlDoc);
    };

    const loadWorkspaceUseCaseDiagrams = (workspace) => {
        const classButtons = $scope.btnDiagramUml.filter(button => button.type === "UMLClassDiagram");
        const originalUseCaseButton = $scope.btnDiagramUml.find(button => button.type === "UMLUseCaseDiagram");
        $scope.btnDiagramUml = classButtons;
        $scope.useCaseDiagrams = [];

        (workspace.useCaseDiagrams || []).forEach((saved, index) => {
            const diagram = index === 0 && originalUseCaseButton ? originalUseCaseButton.diagram : createDiagram({
                width: 100, height: 81, id_div: "areaDiagram", diagram: UMLUseCaseDiagram,
                name: saved.name, interaction: false, draw: false
            });
            const button = {
                diagram: diagram, name: saved.name, type: "UMLUseCaseDiagram",
                status: "C", btnactive: "btn btn-sm button-dt mr-2 mt-1"
            };
            $scope.btnDiagramUml.unshift(button);
            const entry = registerUseCaseDiagram(button, saved.data || blankUseCaseData(), {id: saved.id, name: saved.name});
            importDiagram(diagram, graphWithCurrentProjectName(saved.graph), "UseCase");
            getDataDiagram(diagram, entry.data, entry);
        });

        const active = $scope.useCaseDiagrams.find(entry => entry.id === workspace.activeDiagramId) || $scope.useCaseDiagrams[0];
        activateUseCaseEntry(active);
        if (active) $scope.changeDiagramA(active.button);
    };

    apiLoadModeling = (api_param) => {
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWebServicies + 'projects/loadModuleFile',
            data: JSON.stringify({...api_param}),
            beforeSend: function () {
                //loading();
            },
            success: function (data) {

                switch (api_param.module) {
                    case "DiagramUml":
                        if (!isObjEmpty(data.data)) {
                            clearDiagram();

                            if (data.data.workspace && data.data.workspace.schemaVersion >= 2) {
                                loadWorkspaceUseCaseDiagrams(data.data.workspace);
                                $scope.loadClass();
                                break;
                            }

                            // Verificar si hay duplicados
                            if (hasDuplicateHashKeys(data.data.data, "useCase")) {
                                // Modificar $$hashKey duplicados
                                data.data.data = updateDuplicateHashKeys(data.data.data, "useCase");
                            }

                            // Verificar si hay duplicados
                            if (hasDuplicateHashKeys(data.data.data, "actors")) {
                                // Modificar $$hashKey duplicados
                                data.data.data = updateDuplicateHashKeys(data.data.data, "actors");
                            }

                            let jsonresponse = data.data.data;
                            $scope.$apply(() => {
                                $scope.jsonUseCase = jsonresponse;
                            });
                            data.data.graph = graphWithCurrentProjectName(data.data.graph);
                            importDiagram(diagramUseCase, data.data.graph, "UseCase");
                            const legacyEntry = $scope.useCaseDiagrams[0];
                            legacyEntry.data = $scope.jsonUseCase;
                            activateUseCaseEntry(legacyEntry);
                            getDataDiagram(diagramUseCase, $scope.jsonUseCase, legacyEntry);
                        } else {
                            // crea el sistema principal dentro del diagrama (rectangulo central)
                            createSystem($scope.nameProject);
                        }
                        diagramUseCase.draw();
                        $scope.loadClass();
                        break;
                    case "ClassDiagram":
                        if (data.data.graph !== "") {
                            importDiagram(diagramClass, data.data.graph, "ClassDiagram");
                            if (!isObjEmpty(data.data.data)) {
                                let jsonresponse = data.data.data;
                                $scope.jsonClass = Object.assign([], jsonresponse);
                                // eliminar las relaciones por la parte grafica
                                diagramClass._relations.length = 0;
                                diagramClass.draw();
                                // actualizar diagrama de secuencia
                                $scope.updateSecuence($scope.jsonClass);
                                for (let iclass = 0; iclass < diagramClass._nodes.length; iclass++) {
                                    let nclass = diagramClass._nodes[iclass];
                                    if (nclass._type === "UMLClass") {
                                        elementsClass.push({element: nclass});
                                        $scope.data_type.push(nclass.getName().split(" ")[1]);
                                    }
                                }
                                // actualizar la grafica de las lineas de las relaciones
                                relationsClass($scope.jsonClass.relationships);
                                if (typeof diagramClass.setZoom === "function") diagramClass.setZoom(1);
                                diagramClass.draw();
                                $scope.editionClasDiagram = $scope.jsonClass.edition;
                                $scope.actualizarNaturalText(jsonresponse);
                            }
                            diagramUseCase.draw();
                            $scope.loadSecuence();
                        }
                        break;
                    case "SeguenceDiagram":
                        if (!isObjEmpty(data.data.data)) {
                            let jsonresponse = data.data.data;
                            const sequenceButtons = $scope.btnDiagramUml.filter(button => button.type === "UMLSequenceDiagram");
                            sequenceButtons.forEach((sequence, index) => {
                                const saved = jsonresponse.find(item => item.useCaseId && item.useCaseId === sequence.useCaseId) || jsonresponse[index];
                                if (saved) updateSecuenceDiagram(sequence.diagram, saved.data);
                            });
                        }
                        diagramUseCase.draw();
                        $scope.loadTddJUnit();
                        break;
                    case "Test":
                        swal.close();
                        if (!isObjEmpty(data.data.data)) {
                            let jsonresponse = data.data.data;
                            $scope.jsonTdd = Object.assign({}, jsonresponse);
                            $scope.classTdd = Object.assign([], jsonresponse.receiveTDDJSOM);
                            $scope.jsonTdd["receiveTDDJSOM"] = $scope.classTdd;
                        }
                        alertAll(data);
                        break;
                }
            },
            error: function (objXMLHttpRequest) {
                swal.fire("!Oh no¡", "Se ha producido un problema: " + objXMLHttpRequest, "error");
            }
        });
    };

    getDataDiagram = (diagram, data, entry) => {
        (data.useCase || []).forEach(useCaseData => {
            useCaseData.alternative_flow = (useCaseData.alternative_flow || []).map(normalizeAlternativeFlow);
        });
        let arrayNodes = diagram._nodes;
        let arrayNodesRelations = diagram._relations;

        let arrayActors = [];
        let arrayUseCase = [];
        let arrayRelations = [];

        for (let inodes = 0; inodes < arrayNodes.length; inodes++) {
            let node = arrayNodes[inodes];
            if (node._type === "UMLActor") {
                arrayActors.push({obj: node});
            } else if (node._type === "UMLUseCase") {
                arrayUseCase.push({obj: node});
            }
        }

        for (let irelations = 0; irelations < arrayNodesRelations.length; irelations++) {
            let noderelation = arrayNodesRelations[irelations];
            arrayRelations.push({obj: noderelation});
        }

        for (let idata = 0; idata < data.actors.length; idata++) {
            let actorNode = data.actors[idata];
            for (let iobj = 0; iobj < arrayActors.length; iobj++) {
                if (actorNode.name === arrayActors[iobj].obj.getName()) {
                    actorNode.obj_actor = arrayActors[iobj].obj;
                }
            }
        }

        if (data.useCase.length === arrayUseCase.length) {
            for (let iusecase = 0; iusecase < data.useCase.length; iusecase++) {
                for (let iusecase_ = 0; iusecase_ < data.useCase.length; iusecase_++) {
                    if (data.useCase[iusecase].name === arrayUseCase[iusecase_].obj.getName()) {
                        data.useCase[iusecase].obj_usecase = arrayUseCase[iusecase_].obj;
                        iusecase_ = data.useCase.length;
                    }
                }
            }
        }

        for (let iusecase = 0; iusecase < data.useCase.length; iusecase++) {
            let usecaseNode = data.useCase[iusecase];
            usecaseNode.id = usecaseNode.id || workspaceId("uc");
            usecaseNode.diagramId = entry ? entry.id : usecaseNode.diagramId;
            for (let iusecasediagram = 0; iusecasediagram < arrayUseCase.length; iusecasediagram++) {
                if (usecaseNode.name_i === undefined)
                    usecaseNode["name_i"] = usecaseNode.name;

                if (usecaseNode.name_i === arrayUseCase[iusecasediagram].obj.getName()) {
                    usecaseNode.obj_usecase = arrayUseCase[iusecasediagram].obj;
                    let secuence_diagram = createDiagram({
                        "width": 100,
                        "height": 81,
                        "id_div": "areaDiagram",
                        "diagram": UMLSequenceDiagram,
                        "name": "Secuence Diagram - " + usecaseNode.name,
                        "interaction": false,
                        "draw": false
                    });
                    $scope.$apply(() => {
                        const sequenceButton = {
                            diagram: secuence_diagram,
                            "name": "Secuence Diagram - " + usecaseNode.name,
                            "type": "UMLSequenceDiagram",
                            "status": "C",
                            "btnactive": "btn btn-sm button-dt mr-2 mt-1",
                            "useCaseId": usecaseNode.id,
                            "parentDiagramId": entry ? entry.id : usecaseNode.diagramId
                        };
                        $scope.btnDiagramUml.push(sequenceButton);
                        usecaseNode.secuence_button = sequenceButton;
                    });
                    usecaseNode.secuence_diagram = secuence_diagram;
                }
            }

        }

        if (data !== undefined) {
            if (data.relations !== undefined) {
                for (let irel = 0; irel < arrayRelations.length; irel++) {
                    data.relations[irel].obj_relation = arrayRelations[irel].obj;
                }
            }
        }

        if (arrayActors.length > 0 && data.actors.length === 0) {
            for (let iactor = 0; iactor < arrayActors.length; iactor++) {
                data.actors.push({
                    "obj_actor": arrayActors[iactor].obj,
                    "name": arrayActors[iactor].obj.getName()
                });
            }
        } else if (arrayActors.length === data.actors.length) {
            for (let iactor = 0; iactor < arrayActors.length; iactor++) {
                data.actors[iactor].obj_actor = arrayActors[iactor].obj;
                data.actors[iactor].name = arrayActors[iactor].obj.getName();
            }
        } else if (arrayActors.length !== data.actors.length) {
            data.actors.length = 0;
            for (let iactor = 0; iactor < arrayActors.length; iactor++) {
                if (arrayActors[iactor].obj.getName() !== undefined) {
                    data.actors.push({
                        "obj_actor": arrayActors[iactor].obj,
                        "name": arrayActors[iactor].obj.getName()
                    });
                }
            }
        }


    };

    /**
     * ##############################################################################################################
     * FIN GUARDAR Y CARGAR EL MODELADO UML
     * ##############################################################################################################
     * */

    $scope.exportImage = () => {
        const activeButton = $scope.btnDiagramUml.find(button => button.status === "O");
        if (!activeButton || !exportImg(activeButton.diagram, activeButton.name)) {
            alertAll({status: 3, information: "There is no diagram content to export."});
        }
    };

    $scope.exportAllBehaviorDiagrams = () => {
        const activeButton = $scope.btnDiagramUml.find(button => button.status === "O");
        const exports = [];
        $scope.useCaseDiagrams.forEach(entry => exports.push({diagram: entry.diagram, name: "use-case-" + entry.name}));
        $scope.btnDiagramUml.filter(button => button.type === "UMLSequenceDiagram")
            .forEach(button => exports.push({diagram: button.diagram, name: "sequence-" + button.name.replace(/^Secu?ence Diagram\s*-\s*/i, "")}));
        const available = exports.filter(item => item.diagram && item.diagram._nodes && item.diagram._nodes.length);
        if (!available.length) {
            alertAll({status: 3, information: "There are no use case or sequence diagrams with content to export."});
            return;
        }
        available.forEach((item, index) => window.setTimeout(() => exportImg(item.diagram, item.name), index * 180));
        window.setTimeout(() => {
            if (activeButton && activeButton.diagram) activeButton.diagram.draw();
        }, available.length * 180 + 50);
        alertAll({status: 2, information: available.length + " complete diagram images are being downloaded separately."});
    };

    $scope.deleteStepSecuence = (index) => {
        Swal.fire({
            title: 'Are you sure?',
            text: "All data entered will be permanently deleted.!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, deletes all!'
        }).then((result) => {
            if (result.isConfirmed) {
                $scope.$apply(() => {
                    $scope.table_normal_flow.splice(index, 1);
                });
            }
        });
    };

    $scope.updateStepSecuence = () => {
        alertAll({status: 3, information: "Construction zone"});
    };

    /**
     * ##############################################################################################################
     * INICIO VALIDADORES NOMBRES DE ACTORES Y CASOS DE USO
     * ##############################################################################################################
     * */

    /**
     * @param {string} name
     * @param index indice
     * */
    $scope.nameEquals = (name, index) => {
        let isEquals = false;
        if ($scope.jsonUseCase.useCase.length > 0) {
            for (let indice = 0; indice < $scope.jsonUseCase.useCase.length; indice++) {
                let usecase = $scope.jsonUseCase.useCase[indice];
                if (indice !== index) {
                    if (name === usecase.name) {
                        isEquals = true;
                    }
                }
            }
        }

        if ($scope.jsonUseCase.actors.length > 0) {
            for (let indice = 0; indice < $scope.jsonUseCase.actors.length; indice++) {
                let actor = $scope.jsonUseCase.actors[indice];
                if (indice !== index) {
                    if (name === actor.name) {
                        isEquals = true;
                    }
                }
            }
        }

        if ($scope.actorsUseCase.length > 0) {
            for (let indice = 0; indice < $scope.actorsUseCase.length; indice++) {
                let actorusecase = $scope.actorsUseCase[indice];
                if (actorusecase.obj_actor._type !== "UMLUseCase") {
                    if (indice !== index) {
                        if (name === actorusecase.name) {
                            isEquals = true;
                        }
                    }
                }
            }
        }

        if ($scope.jsonUseCase.actors.length > 0) {
            for (let indice = 0; indice < $scope.jsonUseCase.actors.length; indice++) {
                let actor = $scope.jsonUseCase.actors[indice];
                if (indice !== index) {
                    if (name === actor.name) {
                        isEquals = true;
                    }
                }
            }
        }

        if (isEquals)
            alertAll({
                status: 3,
                information: "The name entered is already assigned to another object in the use case diagram."
            });

        return isEquals;
    };

    /**
     * @param {string} name
     * @param index indice
     * */
    $scope.nameEqualsNoMess = (name, index) => {
        let isEquals = false;
        if ($scope.jsonUseCase.useCase.length > 0) {
            for (let indice = 0; indice < $scope.jsonUseCase.useCase.length; indice++) {
                let usecase = $scope.jsonUseCase.useCase[indice];
                if (indice !== index) {
                    if (name === usecase.name) {
                        isEquals = true;
                    }
                }
            }
        }

        if ($scope.jsonUseCase.actors.length > 0) {
            for (let indice = 0; indice < $scope.jsonUseCase.actors.length; indice++) {
                let actor = $scope.jsonUseCase.actors[indice];
                if (indice !== index) {
                    if (name === actor.name) {
                        isEquals = true;
                    }
                }
            }
        }

        let encontrado = 0;
        if ($scope.actorsUseCase.length > 0) {
            for (let indice = 0; indice < $scope.actorsUseCase.length; indice++) {
                let actorusecase = $scope.actorsUseCase[indice];
                if (actorusecase.obj_actor._type !== "UMLUseCase") {
                    if (indice !== index) {
                        if (name === actorusecase.name) {
                            encontrado++;
                        }
                    }
                }
            }

            if (encontrado > 1) {
                isEquals = true;
            }
        }

        return isEquals;
    };

    /**
     * ##############################################################################################################
     * FIN VALIDADORES NOMBRES DE ACTORES Y CASOS DE USO
     * ##############################################################################################################
     * */

    /**
     * ##############################################################################################################
     * INICIO FUNCIONES DIAGRAMA DE CLASES
     * ##############################################################################################################
     * */

    $scope.editionClasDiagram = false;
    $scope.description = ["public", "private", "protected", "static", "abstract", "final", "interface"];
    $scope.data_type = ['String', 'Int', 'Float', 'Boolean', 'Double', 'Long', 'Byte[]'];
    $scope.dataType = [];
    $scope.visibility = [];
    $scope.attributes = [];
    $scope.methods = [];
    $scope.paramMethods = [];
    $scope.positionSelected = 0;
    $scope.updateClass = false;
    $scope.positionClass = -1;

    $scope.activeEditionParam = false;
    $scope.typeRelation = {"value": "", "object": ""};

    $scope.updateLegend = (form) => {
        if (form.$valid) {
            updateLegendTools($scope.selectedClass, form.up_legendclass.$modelValue);
        }
    };

    $scope.nameEqualsGeneric = (array, key, name) => {
        let flag = false;
        for (let x = 0; x < array.length; x++) {
            if (array[x][key] === name) {
                flag = true;
            }
        }
        return flag;
    }

    //#region atributos

    /**
     * INICIO METODOS PARA LOS ATRIBUTOS DE UNA CLASE
     * */

    // agregar atributos a una clase nueva
    $scope.addAttribute = () => {

        if ($scope.nameEqualsGeneric($scope.attributes, "name", $scope.att_name)) {
            alertAll({"status": 3, "information": "There is already a attribute with that name."});
            return;
        }

        $scope.attributes.push({
            "name": $scope.att_name,
            "type": $scope.att_datatype,
            "visibility": $scope.att_visibility
        });
        $scope.clearAtt();
    };

    //obtener los datos del atributo a editar
    $scope.selectedAtt = (object, index) => {
        console.log("OBJETO SELECCIONADO ", object);
        $scope.up_att_name = object.name;
        $scope.up_att_visibility = object.visibility;
        $scope.up_att_datatype = object.type;

        $scope.positionSelected = index;
        $("#modal_updateAtt").modal();
    };

    // editar el atributo seleccionado
    $scope.updateAttribute = (form) => {
        $scope.attributes[$scope.positionSelected].name = form.up_att_name.$modelValue;
        $scope.attributes[$scope.positionSelected].visibility = form.up_att_visibility.$modelValue;
        $scope.attributes[$scope.positionSelected].type = form.up_att_datatype.$modelValue;
        $("#modal_updateAtt").modal("hide");
    };

    //eliminar atributos de una clase nueva
    $scope.deleteAttribute = (index) => {
        Swal.fire({
            title: 'Are you sure?',
            text: "Delete the selected attribute.",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, remove it!'
        }).then((result) => {
            if (result.isConfirmed) {
                $scope.$apply(() => {
                    $scope.attributes.splice(index, 1);
                });
            }
        });
    };

    /**
     * FIN METODOS PARA LOS ATRIBUTOS DE UNA CLASE
     * */

    //#endregion atributos

    //#region metodos

    // funcion para agregar un nuevo metodo
    $scope.addMethod = () => {

        if ($scope.nameEqualsGeneric($scope.methods, "name", $scope.mt_name)) {
            alertAll({"status": 3, "information": "There is already a method with that name."});
            return;
        }

        $scope.paramMethods = [];
        $scope.methods.push({
            "name": $scope.mt_name,
            "parameters": $scope.paramMethods,
            "type": $scope.mt_datatype,
            "visibility": $scope.mt_visibility
        });
        $scope.clearMeth();
    };

    // obtener los parametros del metodo seleccionado
    $scope.getParameters = (object, index) => {
        console.log(object.parameters);
        $scope.positionMethod = index;
        $scope.paramMethods = object.parameters;
        $("#modal_parameters").modal();
    };

    // agregar un nuevo parametro al metodo seleccionado
    $scope.addParameter = () => {
        $scope.paramMethods.push({
            "name": "",
            "type": "",
            "active": true
        })
    };

    $scope.positionMethod = -1;
    $scope.saveParameters = () => {
        $scope.methods[$scope.positionMethod].parameters = Object.assign([], $scope.paramMethods);
        $scope.paramMethods.length = 0;
        $("#modal_parameters").modal("hide");
    }

    // guardar el nuevo parametro
    $scope.saveParameter = (index) => {
        let nameParam = $("#param_name_" + index).val();

        let flag = false;
        for (let x = 0; x < $scope.paramMethods.length; x++) {
            if ($scope.paramMethods[x].name !== $scope.paramMethods[index].name) {
                if ($scope.paramMethods[x].name === nameParam) {
                    flag = true;
                }
            }
        }

        if (flag) {
            alertAll({"status": 3, "information": "There is already a parameter with that name."});
            return;
        }

        let datatype = $("#datatype_" + index).val();
        if (!$scope.activeEditionParam) {
            $scope.paramMethods[index].name = nameParam;
            $scope.paramMethods[index].type = datatype;
            $scope.paramMethods[index].active = false;
            //delete $scope.paramMethods[index]['active'];
            console.log($scope.paramMethods);
        } else {
            $scope.paramMethods[index].name = nameParam;
            $scope.paramMethods[index].type = datatype;
            $scope.paramMethods[index].active = false;
            let status = [];
            for (let x = 0; x < $scope.paramMethods.length; x++) {
                if (!$scope.paramMethods[x].active)
                    status.push($scope.paramMethods[x].active);
            }
            if (status.length === $scope.paramMethods.length)
                $scope.activeEditionParam = false;
        }
    };

    // eliminar parametro
    $scope.deleteParam = (index) => {
        $scope.paramMethods.splice(index, 1);
    };

    // metodo para seleccionar un parametro
    $scope.selectedTypeParameter = (value) => {
        let valueAux = value.split(":");
        if (valueAux[0] === "string") {
            return value;
        } else {
            return "string:" + value;
        }
    };

    // eliminar un metodo
    $scope.deleteMethod = (index) => {
        Swal.fire({
            title: 'Are you sure?',
            text: "This action cannot be reversed.",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, activate!'
        }).then((result) => {
            if (result.isConfirmed) {
                $scope.$apply(() => {
                    $scope.methods.splice(index, 1);
                });
            }
        });
    }

    //#endregion metodos

    //#region relaciones

    $("#btnagregation").click(() => {

        if (!$scope.editionClasDiagram) {
            alertAll({"status": 3, "information": "You cannot modify the class diagram if editing is disabled."});
            return;
        }

        $scope.$apply(() => {
            $scope.typeRelation.value = "agregation";
            $scope.typeRelation.object = UMLAggregation;
        });
        $("#modal_relations_class").modal();
    })

    $("#btndependecy").click(() => {

        if (!$scope.editionClasDiagram) {
            alertAll({"status": 3, "information": "You cannot modify the class diagram if editing is disabled."});
            return;
        }

        $scope.$apply(() => {
            $scope.typeRelation.value = "dependency";
            $scope.typeRelation.object = UMLAssociation;
        });
        $("#modal_relations_class").modal();
    });

    $("#btngeneralizationc").click(() => {

        if (!$scope.editionClasDiagram) {
            alertAll({"status": 3, "information": "You cannot modify the class diagram if editing is disabled."});
            return;
        }

        $scope.$apply(() => {
            $scope.typeRelation.value = "generalization";
            $scope.typeRelation.object = UMLAssociation;
        });
        $("#modal_relations_class").modal();
    });

    $("#btnasositionc").click(() => {

        if (!$scope.editionClasDiagram) {
            alertAll({"status": 3, "information": "You cannot modify the class diagram if editing is disabled."});
            return;
        }

        $scope.$apply(() => {
            $scope.typeRelation.value = "association";
            $scope.typeRelation.object = UMLAssociation;
        });
        $("#modal_relations_class").modal();
    });

    $scope.createRelation = (form) => {

        if (form.class_from.$modelValue.className === form.class_to.$modelValue.className) {
            alertAll({"status": 3, "information": "Select different class."});
            return;
        }

        let flag = false;
        for (let x = 0; x < $scope.jsonClass.relationships.length; x++) {
            let from = $scope.jsonClass.relationships[x].from;
            let to = $scope.jsonClass.relationships[x].to;
            if ((from === form.class_from.$modelValue.className && to === form.class_to.$modelValue.className) ||
                    (from === form.class_to.$modelValue.className && to === form.class_from.$modelValue.className)) {
                flag = true;
            }
        }

        if (!flag) {
            let cardinalidateObject = form.cardinalidate.$modelValue.toString()
            let from_fk = "";
            let to_fk = "";
            if (cardinalidateObject === "1..1") {
                from_fk = form.class_to.$modelValue.className.toLowerCase() + ":" + form.class_to.$modelValue.className;
                to_fk = form.class_from.$modelValue.className.toLowerCase() + ":" + form.class_from.$modelValue.className;
            } else if (cardinalidateObject === "1..*") {
                from_fk = form.class_to.$modelValue.className.toLowerCase() + ":" + form.class_to.$modelValue.className + "[]";
                to_fk = form.class_from.$modelValue.className.toLowerCase() + ":" + form.class_from.$modelValue.className;
            } else if (cardinalidateObject === "*..1") {
                from_fk = form.class_to.$modelValue.className.toLowerCase() + ":" + form.class_to.$modelValue.className;
                to_fk = form.class_from.$modelValue.className.toLowerCase() + ":" + form.class_from.$modelValue.className + "[]";
            }

            let relationAux = [];
            relationAux.push({
                "from": form.class_from.$modelValue.className,
                "to": form.class_to.$modelValue.className,
                "typeRelatioship": $scope.typeRelation.value,
                "cardinalidate": cardinalidateObject,
                from_fk: from_fk,
                to_fk: to_fk
            });
            $scope.jsonClass.relationships.push(relationAux[0]);

            // agregar ls fk a las tablas relacionadas
            $scope.addForeingKey($scope.jsonClass.diagram[0].class, relationAux);

            let from = getFromToRelation(form.class_from.$modelValue.className);
            let to = getFromToRelation(form.class_to.$modelValue.className);
            from.addAttribute("- " + from_fk);
            to.addAttribute("- " + to_fk);
            let relation = createRelationClass({
                type: $scope.typeRelation.object,
                value: $scope.typeRelation.value,
                typeRelatioship: $scope.typeRelation.value,
                a: from,
                b: to,
                card_A: cardinalidateObject.split("..")[0],
                card_B: cardinalidateObject.split("..")[1]
            });

            elementsClass.push({"element": relation});
            diagramClass.draw();
            $("#modal_relations_class").modal("hide");
        } else {
            alertAll({"status": 3, "information": "The selected classes already have a relationship."});
        }
    };

    //#endregion relaciones

    // activar edicion de los parametros
    $scope.activeEdition = () => {
        $scope.activeEditionParam = true;
        for (let x = 0; x < $scope.paramMethods.length; x++) {
            $scope.paramMethods[x].active = true;
        }
    };

    // guardar los cambios al momento de tener activa la edicion de todos los campos
    $scope.saveUpateActive = (index) => {
        let datatype = $("#datatype_" + index).val();
        $scope.paramMethods[index].type = datatype;
    }

    // desactivar la edicion de los parametros
    $scope.disabledEdition = () => {
        $scope.activeEditionParam = false;
        for (let x = 0; x < $scope.paramMethods.length; x++) {
            $scope.paramMethods[x].active = false;
        }
    };

    /**
     * FIN METODOS PARA LOS METODOS DE UNA CLASE
     * */

    // activar la edicion del diagrama de clase
    $scope.activateEditionClassDiagram = () => {
        Swal.fire({
            title: 'Are you sure?',
            text: "You are about to manually edit the class diagram, if you edit the class diagram you will not be able to modify the changes in the descriptions or actions of the use case. ",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, activate!'
        }).then((result) => {
            if (result.isConfirmed) {
                $scope.$apply(() => {
                    $scope.editionClasDiagram = true;
                });
            }
        });
    };

    // desactivar la edicion del diagrama de clase
    $scope.restoreClassDiagram = () => {
        Swal.fire({
            title: 'Are you sure?',
            text: "You are about to revert all the changes made, everything done will be deleted definitively and the class diagram will be generated again from the use case descriptions.\n",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, restart!'
        }).then((result) => {
            if (result.isConfirmed) {
                $scope.$apply(() => {
                    $scope.editionClasDiagram = false;
                    superInterpret();
                    updateClassDiagram($scope.jsonClass, "U");
                    diagramClass.draw();
                });
            }
        });
    }

    $scope.cancelClass = (form) => {
        $("#modal_newclass").modal("hide");
        $scope.clearFormClass();
        form.$setPristine();
        form.$setUntouched();
    };

    // METODOS PARA CREAR LOS OBJETOS EN LE DIAGRAMA
    $scope.newClass = (form) => {

        if ($scope.nameEqualsGeneric($scope.jsonClass.diagram[0].class, "className", form.nc_classname.$modelValue)) {
            alertAll({"status": 3, "information": "There is already a class with that name."});
            return;
        }

        if ($scope.attributes.length === 0) {
            alertAll({"status": 3, "information": "Enter at least one attribute for the class."});
            return;
        }

        if ($scope.methods.length === 0) {
            alertAll({"status": 3, "information": "Enter at least one method for the class."});
            return;
        }

        if (form.$valid) {
            $scope.jsonClass.diagram[0].class.push({
                "action": "create",
                "attributes": Object.assign([], $scope.attributes),
                "className": form.nc_classname.$modelValue,
                "derivative": [],
                "methods": Object.assign([], $scope.methods),
                "visibility": form.nc_visibility.$modelValue
            });
            // updateClassDiagram($scope.jsonClass, "C");
            let newclass = createClass({
                name: visibilityGlobal[form.nc_visibility.$modelValue] + " " + form.nc_classname.$modelValue,
                x: (Math.floor(Math.random() * 10) * 100),
                y: (Math.floor(Math.random() * 15) * 30)
            });
            for (let iattributes = 0; iattributes < $scope.attributes.length; iattributes++) {
                let attributes = $scope.attributes[iattributes];
                newclass.addAttribute(visibilityGlobal[attributes.visibility] + " " + attributes.name + ":" + attributes.type);
            }
            for (let imethods = 0; imethods < $scope.methods.length; imethods++) {
                let methods = $scope.methods[imethods];
                newclass.addOperation(visibilityGlobal[methods.visibility] + " " + methods.name + "(" + getParamethersReplace(methods.parameters) + "):" + methods.type);
            }
            $("#modal_newclass").modal("hide");
            elementsClass.push({"element": newclass});
            diagramClass.draw();
            $scope.clearFormClass();

            form.$setPristine();
            form.$setUntouched();

            alertAll({"status": 2, "information": "Class successfully adds."});
        }
    };

    $scope.modifyClass = (form) => {
        /*if($scope.nameEqualsGeneric($scope.jsonClass.diagram[0].class, "className", form.nc_classname.$modelValue)){
         alertAll({"status": 3, "information": "There is already a class with that name."});
         return;
         }*/
        let classModify = $scope.jsonClass.diagram[0].class[$scope.positionClass];
        let flag = false;
        // validar que no existe un nombre igual al de otra clase pero excepto la clase seleccionada
        for (let x = 0; x < $scope.jsonClass.diagram[0].class.length; x++) {
            let classx = $scope.jsonClass.diagram[0].class[x];
            if (classx.className !== classModify["className"]) {
                if (classx.className === form.nc_classname.$modelValue) {
                    flag = true;
                }
            } else
                flag = false;
        }

        if (flag) {
            alertAll({"status": 3, "information": "There is already a class with that name."});
            return;
        }

        if ($scope.attributes.length === 0) {
            alertAll({"status": 3, "information": "Enter at least one attribute for the class."});
            return;
        }

        /*if($scope.methods.length === 0){
         alertAll({"status": 3, "information": "Enter at least one method for the class."});
         return;
         }*/

        if (form.$valid) {
            let positionElement = -1;
            let classEdit = undefined;
            for (let x = 0; x < elementsClass.length; x++) {
                let elemen = elementsClass[x].element;
                if (elemen.getType() === "UMLClass") {
                    if (elemen.getName().split(" ")[1] === classModify["className"]) {
                        let components = elemen.getComponents();
                        classEdit = elemen;
                        console.log(components[2]._childs);
                        console.log(components[3]._childs);
                        components[2]._childs.length = 0;
                        components[3]._childs.length = 0;
                        positionElement = x;
                    }
                }
            }

            let classname = form.nc_classname.$modelValue.replaceAll(" ", "_");
            classModify["className"] = classname;
            classModify["visibility"] = form.nc_visibility.$modelValue;
            classModify["modifiers"] = "";
            classModify["action"] = "update";
            classModify["attributes"] = $scope.attributes;
            classModify["methods"] = $scope.methods;

            classEdit.setName(visibilityGlobal[form.nc_visibility.$modelValue] + " " + classname);

            for (let iattributes = 0; iattributes < $scope.attributes.length; iattributes++) {
                let attributes = $scope.attributes[iattributes];
                classEdit.addAttribute(visibilityGlobal[attributes.visibility] + " " + attributes.name.replaceAll(" ", "_") + ":" + attributes.type);
            }
            for (let imethods = 0; imethods < $scope.methods.length; imethods++) {
                let methods = $scope.methods[imethods];
                classEdit.addOperation(visibilityGlobal[methods.visibility] + " " + methods.name.replaceAll(" ", "_") + "(" + getParamethersReplace(methods.parameters) + "):" + methods.type);
            }

            $("#modal_newclass").modal("hide");
            diagramClass.draw();
            $scope.updateClass = false;

            form.$setPristine();
            form.$setUntouched();

            alertAll({"status": 2, "information": "Class successfully update."});
        }
    };

    $scope.clearFormClass = () => {
        $scope.nc_classname = "";
        $scope.nc_visibility = "-1";

        $scope.att_name = "";
        $scope.att_datatype = "-1";
        $scope.att_visibility = "-1";

        $scope.mt_name = "";
        $scope.mt_datatype = "-1";
        $scope.mt_visibility = "-1";

        $scope.attributes.length = 0;
        $scope.methods.length = 0;
        //$scope.paramMethods.length = 0;

        $scope.updateClass = false;
    };

    $scope.clearAtt = () => {
        $scope.att_name = "";
        $scope.att_datatype = "-1";
        $scope.att_visibility = "-1";
    };

    $scope.clearMeth = () => {
        $scope.mt_name = "";
        $scope.mt_datatype = "-1";
        $scope.mt_visibility = "-1";
    };

    $scope.copyUseCase = function () {
        selectElementContents("viewUseCaseTable");
    };

    async function selectElementContents(elementId) {
        const table = document.getElementById(elementId);

        if (!table) {
            alertAll({status: 4, information: "No se encontró la tabla seleccionada."});
            return;
        }

        // 1. Construimos el HTML completo con estilos para que Word los entienda
        const htmlContent = `
        <html>
        <head>
        <style>
            table { 
                border-collapse: collapse; 
                width: auto; 
                font-family: 'Consolas', 'Courier New', monospace; 
                font-size: 10px;
                line-height: 1.1;
            }
            table, th, td { 
                border: 0.5pt solid #000000; 
            }
            th, td { 
                padding: 2px 4px; /* Padding reducido para hacerlo compacto */
                text-align: left;
            }
            .table-active { 
                background-color: #f2f2f2; 
            }
            .center-d { 
                text-align: center; 
            }
        </style>
        </head>
        <body>
            ${table.outerHTML}
        </body>
        </html>
    `;

        // 2. Usamos el API de Portapapeles moderno (soporta formato HTML)
        try {
            const type = "text/html";
            const blob = new Blob([htmlContent], {type});
            const data = [new ClipboardItem({[type]: blob})];

            await navigator.clipboard.write(data);

            // 3. Alerta de éxito con tu función alertAll
            alertAll({status: 2, information: "Contenido copiado con éxito para Word."});

        } catch (err) {
            console.error('Error al copiar:', err);
            alertAll({status: 4, information: "Error al copiar al portapapeles."});
        }
    }

    function copyFallback(htmlContent) {
        const container = document.createElement('div');
        container.innerHTML = htmlContent;
        container.style.position = 'fixed';
        container.style.pointerEvents = 'none';
        container.style.opacity = 0;
        container.style.zIndex = -1;

        document.body.appendChild(container);

        /*const range = document.createRange();
         range.selectNodeContents(container);
         const selection = window.getSelection();
         selection.removeAllRanges();
         selection.addRange(range);*/

        try {
            const successful = document.execCommand('copy');
            const msg = successful ? "Content successfully copied." : "Failed to copy content.";
            console.log(msg);
            alertAll({"status": successful ? 2 : 0, "information": msg});
        } catch (err) {
            console.error('Error al copiar al portapapeles: ', err);
            alertAll({"status": 0, "information": "Failed to copy content."});
        }

        selection.removeAllRanges();
        document.body.removeChild(container);
    }

    /**
     * ##############################################################################################################
     * FIN FUNCIONES DIAGRAMA DE CLASES
     * ##############################################################################################################
     * */

    /**
     * ##############################################################################################################
     * INICIO API PARA CONSUMIR EL MODELO DE INTELIGENCIA ARTIFICIAL
     * ##############################################################################################################
     * */

    $scope.validarExistenciaTextoNatural = (naturalText, propiedad) => {
        $scope.natural_text = store.session.get("natural_text") || [];
        // Verificamos si ya existe un objeto con el mismo valor en la propiedad especificada
        const existe = $scope.natural_text.some(item => item[propiedad] === naturalText);
        const item = $scope.natural_text.find(item => item[propiedad] === naturalText);

        if (existe) {
            console.log(`El valor ${naturalText} ya existe en la propiedad ${propiedad}, no se agrega.`);
        } else {
            return {interpret: {}, flag: false};
        }

        return {interpret: item, flag: true};
    };

    $scope.interpretUseCase = (textNatural, seInterpreta) => {
        let minjson = [];
        if ($scope.activateOpenAi) {

            minjson = seInterpreta ? $scope.interpretTextIA(textNatural) :
                    [textNatural,
                        {
                            diagram: [],
                            xmldiagram: "",
                            relationships: [],
                            action: [],
                            notifications: []
                        }];
        } else {
            minjson = getHackDiagram(textNatural);
        }
        return minjson;
    };

    $scope.interpretTextIA = (textNatural) => {

        if (textNatural === "") {
            return [
                textNatural,
                $scope.jsonDiagramClassIA
            ];
        }

        // validar si el texto ya ha sido analizado anteriormente
        let existeTexto = $scope.validarExistenciaTextoNatural(textNatural, "text");
        if (existeTexto.flag) {
            $scope.showMessageOpenAi = false;
            updateClassDiagram(existeTexto.interpret.classDiagram, "U");
            diagramUseCase.draw();
            return [
                textNatural,
                existeTexto.interpret.classDiagram
            ];
        }

        let request = {
            classDTO: {
                descriptionUseCase: textNatural
            }
        };
        $scope.apiUseModel(request);
        $scope.jsonDiagramClassIA["notifications"].length = 0;
        return [
            textNatural,
            $scope.jsonDiagramClassIA
        ];
    };

    $scope.apiUseModel = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        if (!dataUser || !dataUser.user_token) {
            $scope.showMessageOpenAi = false;
            alertAll({status: 4, information: "Your session has expired. Sign in again to use AI analysis."});
            return;
        }
        const requestSerial = ++$scope.aiRequestSerial;
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'master-project/useModelTraining',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
                $scope.$evalAsync(() => {
                    $scope.showMessageOpenAi = true;
                });
            },
            success: function (response) {
                if (!$scope.activateOpenAi || requestSerial !== $scope.aiRequestSerial) {
                    $scope.$applyAsync(() => {
                        $scope.showMessageOpenAi = false;
                    });
                    return;
                }
                if (response.status === "OK") {
                    $scope.$apply(function () {
                        // The request contains the complete project description,
                        // therefore its result is authoritative and must not be
                        // mixed with stale classes from a previous analysis mode.
                        $scope.jsonClass = response.data;
                        updateClassDiagram($scope.jsonClass, "U");
                        diagramUseCase.draw();
                        $scope.showMessageOpenAi = false;
                        $scope.natural_text.push({
                            text: request.classDTO.descriptionUseCase,
                            classDiagram: response.data
                        });
                        store.session.set("natural_text", $scope.natural_text);
                        store.session.set("jsonClass", $scope.jsonClass);
                    });
                } else {
                    $scope.$applyAsync(() => {
                        $scope.showMessageOpenAi = false;
                    });
                    alertAll({status: 4, information: response.message});

                }
                console.log(response);
            },
            error: function (objXMLHttpRequest) {
                if (requestSerial !== $scope.aiRequestSerial) return;
                $scope.$applyAsync(() => {
                    $scope.showMessageOpenAi = false;
                });
                alertAll({status: 4, information: "AI analysis could not be completed. The current diagram was preserved."});
            }
        });
    };

    /**
     * ##############################################################################################################
     * FIN API PARA CONSUMIR EL MODELO DE INTELIGENCIA ARTIFICIAL
     * ##############################################################################################################
     * */
});
