app.controller("trainmodels_controller", function ($scope, $http, $interval) {

    $scope.optionsTrainModel = [
        {name: "Create dataset", classActive: "training-step is-active", showPanel: true},
        {name: "Start training", classActive: "training-step", showPanel: false},
    ];

    $scope.requestTrainingModel = {
        pathJson: "",
        idTrainig: -1,
        datasetName: "",
        datasetVersion: "",
        schemaVersion: "1.0",
        samples: []
    }

    $scope.listTrainingHistory = [];
    $scope.resultJson = {};
    $scope.trainingSummary = {total: 0, completed: 0, running: 0, failed: 0, examples: 0};
    $scope.detailModalTitle = "More information";
    $scope.researchDashboard = {counts: {}, training: [], metrics: []};
    $scope.researchItems = [];
    $scope.researchReview = null;
    $scope.trainingMonitor = null;
    $scope.monitorEventLimit = 12;
    $scope.showMonitorRaw = false;


    $(document).ready(() => {
        angular.element($('[ng-controller="application"]')).scope().changeTittlePage("Train Models", true);
        $scope.appPage.Select = "trainmodels";
        $scope.apiGetListTrainingHistory();
        $scope.trainingMonitorTimer = $interval(() => $scope.apiGetListTrainingHistory(true), 30000);
        $scope.initViewResult();
    });

    $scope.initViewResult = () => {
        $scope.resultJson = ace.edit("resultJson");

        var theme = "ace/theme/chrome";  // Tema claro
        var mode = "ace/mode/json";  // Modo C/C++

        $scope.resultJson.setTheme(theme);
        $scope.resultJson.getSession().setMode(mode);
        $scope.resultJson.getSession().setUseWrapMode(true);  // Habilita el ajuste de línea
        $scope.resultJson.setReadOnly(true); // Deshabilita la edición
        $scope.resultJson.setValue('', 1); // Inicializa con el contenido en blanco
    }

    $scope.createFileModel = (form) => {
        if (form.$valid && $scope.requestTrainingModel.samples.length > 0) {
            $scope.apiCreateFileModel($scope.requestTrainingModel);
        }
    }

    $scope.handleResearchManifest = input => {
        const file = input && input.files ? input.files[0] : null;
        if (!file) return;
        const reader = new FileReader();
        reader.onload = event => {
            $scope.$apply(() => {
                try {
                    const manifest = JSON.parse(event.target.result);
                    if (!manifest.datasetName || !manifest.datasetVersion || !Array.isArray(manifest.samples) || !manifest.samples.length) {
                        throw new Error("datasetName, datasetVersion and a non-empty samples array are required.");
                    }
                    $scope.requestTrainingModel.datasetName = manifest.datasetName;
                    $scope.requestTrainingModel.datasetVersion = manifest.datasetVersion;
                    $scope.requestTrainingModel.schemaVersion = manifest.schemaVersion || "1.0";
                    $scope.requestTrainingModel.samples = manifest.samples;
                    const countSplit = split => manifest.samples.filter(sample => String(sample.split || "TRAIN").toUpperCase() === split).length;
                    $scope.researchManifest = {
                        name: file.name,
                        count: manifest.samples.length,
                        train: countSplit("TRAIN"),
                        validation: countSplit("VALIDATION"),
                        test: countSplit("TEST"),
                        error: countSplit("TRAIN") ? "" : "The manifest must contain at least one TRAIN project."
                    };
                    if ($scope.researchManifest.error) $scope.requestTrainingModel.samples = [];
                } catch (error) {
                    $scope.requestTrainingModel.samples = [];
                    $scope.researchManifest = {name: file.name, count: 0, error: error.message};
                }
            });
        };
        reader.readAsText(file, "UTF-8");
    };

    $scope.trainModel = () => {
        console.log($scope.requestTrainingModel.pathJson);
        let request = {
            idTrainingHistory: $scope.requestTrainingModel.idTrainig
        }
        console.log(request);
        $scope.apiTrainModel(request);
    };

    $scope.deleteTraining = () => {
        let request =
        {
            "classDTO": $scope.requestTrainingModel.idTrainig
        };
        $scope.apiDeleteTraining(request);
    };

    $scope.apiDeleteTraining = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'master-project/deleteTraining',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                swal.close()
                console.log(response);
                let data = response.data;
                if(response.status === "OK") {
                    $scope.closeModalTrainModel();
                    alertAll({status: 2, information: data});
                }
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.apiTrainModel = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'master-project/trainingModelOpenAi',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                swal.close();
                $scope.closeModalTrainModel();
                $scope.apiGetListTrainingHistory();
                alertAll(response);
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.apiCreateFileModel = (request) => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'master-project/createFileTraining',
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(request),
            beforeSend: function (xhr) {
                loading();
            },
            success: function (response) {
                swal.close();
                console.log(response);
                let data = response.data;
                if(response.status === "OK") {
                    $scope.$apply(function () {
                        $scope.requestTrainingModel.pathJson = data.pathJsonFile;
                        $scope.requestTrainingModel.idTrainig = data.idTrainingHistory;
                        $scope.changeOption($scope.optionsTrainModel[1]);
                    });
                }
                alertAll(response);
                console.log(data);
            },
            error: function (objXMLHttpRequest) {
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    }

    $scope.apiGetListTrainingHistory = silent => {
        var dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "GET",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + 'training-history/list',
            headers: {"userToken": dataUser.user_token},
            beforeSend: function (xhr) {
                if (!silent) loading();
            },
            success: function (response) {
                if (!silent) swal.close();
                console.log(response);
                $scope.$apply(function  () {
                    $scope.listTrainingHistory = response.data;

                    $scope.trainingSummary = {total: $scope.listTrainingHistory.length, completed: 0, running: 0, failed: 0, examples: 0};
                    $scope.listTrainingHistory.forEach(item => {
                        try {
                            item.resultJson = item.resultTrining ? JSON.parse(item.resultTrining) : {status: "PENDING", data: {}};
                        } catch (error) {
                            item.resultJson = {status: "ERROR", message: "Stored provider response is not valid JSON.", data: {}};
                        }
                        item.providerStatus = String(item.providerStatus || (item.resultJson.data && item.resultJson.data.status) || item.resultJson.status || "PENDING").toLowerCase();
                        item.progressPercent = item.progressPercent === null || item.progressPercent === undefined ? null : Number(item.progressPercent);
                        item.displayModel = (item.resultJson.data && (item.resultJson.data.fine_tuned_model || item.resultJson.data.model)) || "Not generated";
                        $scope.trainingSummary.examples += Number(item.countRowDataTrining || 0);
                        if (["succeeded", "completed"].indexOf(item.providerStatus) >= 0) $scope.trainingSummary.completed++;
                        else if (["error", "failed", "cancelled", "monitor_error"].indexOf(item.providerStatus) >= 0) $scope.trainingSummary.failed++;
                        else $scope.trainingSummary.running++;
                    });

                    console.log($scope.listTrainingHistory);
                });
            },
            error: function (objXMLHttpRequest) {
                if (!silent) swal.close();
                console.log("Error: ", objXMLHttpRequest.responseText);
            }
        });
    };

    $scope.openTrainingMonitor = training => {
        const dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "GET",
            dataType: "json",
            url: urlWsOpenAi + "training-history/" + training.id + "/monitor",
            headers: {"userToken": dataUser.user_token},
            beforeSend: () => loading(),
            success: response => {
                swal.close();
                $scope.$applyAsync(() => {
                    $scope.trainingMonitor = buildTrainingMonitor(response.data || {}, training);
                    $scope.monitorEventLimit = 12;
                    $scope.showMonitorRaw = false;
                });
                $("#modalTrainingMonitor").modal();
            },
            error: error => { swal.close(); alertAll({status: 1, information: error.responseText}); }
        });
    };

    function buildTrainingMonitor(data, training) {
        const monitor = angular.copy(data);
        monitor.training = training;
        monitor.status = String(monitor.providerStatus || training.providerStatus || "unknown").toLowerCase();
        monitor.statusLabel = monitor.status.replace(/_/g, " ");
        monitor.snapshots = monitor.snapshots || [];
        monitor.events = (monitor.events || []).map(event => {
            event.createdAtMillis = Number(event.createdAt || 0) * 1000;
            return event;
        });
        monitor.checkpoints = (monitor.checkpoints || []).sort((a, b) => Number(a.stepNumber || 0) - Number(b.stepNumber || 0));
        monitor.averageLatency = monitor.snapshots.length
            ? Math.round(monitor.snapshots.reduce((total, item) => total + Number(item.durationMs || 0), 0) / monitor.snapshots.length)
            : 0;
        monitor.latestCheckpoint = monitor.checkpoints.length ? monitor.checkpoints[monitor.checkpoints.length - 1] : null;
        monitor.stages = buildTrainingStages(monitor.status);
        monitor.progressChart = buildProgressPoints(monitor.snapshots);
        monitor.metricChart = buildMetricChart(monitor.checkpoints);
        monitor.raw = JSON.stringify(data, null, 4);
        return monitor;
    }

    function buildTrainingStages(status) {
        const stages = [
            {label: "Job created", icon: "fa-file-upload"},
            {label: "Validation", icon: "fa-check-double"},
            {label: "Queued", icon: "fa-hourglass-half"},
            {label: "Training", icon: "fa-cogs"},
            {label: "Completed", icon: "fa-flag-checkered"}
        ];
        const ranks = {validating_files: 1, queued: 2, running: 3, succeeded: 4, completed: 4, failed: 3, cancelled: 3, monitor_error: 3};
        const rank = ranks[status] === undefined ? 0 : ranks[status];
        const failed = ["failed", "cancelled", "monitor_error", "error"].indexOf(status) >= 0;
        stages.forEach((stage, index) => {
            stage.done = index < rank || (!failed && index === rank && rank === 4);
            stage.active = index === rank && !stage.done;
            stage.error = failed && index === rank;
        });
        return stages;
    }

    function buildProgressPoints(snapshots) {
        const chronological = angular.copy(snapshots || []).reverse();
        if (!chronological.length) return "";
        const statusProgress = {queued: 28, validating_files: 16, running: 68, succeeded: 100, completed: 100, failed: 68, cancelled: 68, monitor_error: 68};
        return chronological.map((item, index) => {
            const progress = item.progressPercent === null || item.progressPercent === undefined
                ? (statusProgress[String(item.status || "").toLowerCase()] || 5) : Number(item.progressPercent);
            const x = chronological.length === 1 ? 160 : 8 + (index * 304 / (chronological.length - 1));
            const y = 88 - Math.max(0, Math.min(100, progress)) * .72;
            return x.toFixed(1) + "," + y.toFixed(1);
        }).join(" ");
    }

    function buildMetricChart(checkpoints) {
        const loss = checkpoints.map(item => firstMetric(item.metrics, ["full_valid_loss", "valid_loss", "validation_loss"]));
        const accuracy = checkpoints.map(item => firstMetric(item.metrics, ["full_valid_mean_token_accuracy", "valid_mean_token_accuracy", "validation_token_accuracy"]));
        return {loss: chartPoints(loss), accuracy: chartPoints(accuracy), hasLoss: loss.some(Number.isFinite), hasAccuracy: accuracy.some(Number.isFinite)};
    }

    function chartPoints(values) {
        const finite = values.filter(Number.isFinite);
        if (!finite.length) return "";
        const min = Math.min.apply(null, finite), max = Math.max.apply(null, finite);
        return values.map((value, index) => {
            if (!Number.isFinite(value)) return null;
            const x = values.length === 1 ? 160 : 8 + (index * 304 / (values.length - 1));
            const normalized = max === min ? .5 : (value - min) / (max - min);
            return x.toFixed(1) + "," + (84 - normalized * 62).toFixed(1);
        }).filter(Boolean).join(" ");
    }

    function firstMetric(metrics, names) {
        metrics = metrics || {};
        for (const name of names) {
            if (metrics[name] !== null && metrics[name] !== undefined && Number.isFinite(Number(metrics[name]))) return Number(metrics[name]);
        }
        return NaN;
    }

    $scope.monitorMetric = (checkpoint, names) => {
        const value = firstMetric(checkpoint ? checkpoint.metrics : null, names);
        return Number.isFinite(value) ? value : null;
    };

    $scope.monitorMetricLabel = (checkpoint, names, percent) => {
        const value = $scope.monitorMetric(checkpoint, names);
        if (value === null) return "—";
        return (percent ? value * 100 : value).toFixed(percent ? 2 : 4) + (percent ? "%" : "");
    };

    $scope.copyMonitorJobId = () => {
        if ($scope.trainingMonitor && navigator.clipboard) navigator.clipboard.writeText($scope.trainingMonitor.providerJobId || "");
    };

    $scope.openResearchSummary = () => {
        const dataUser = store.session.get("user_tddm4iotbs");
        loading();
        const summaryRequest = $.ajax({
            method: "GET",
            dataType: "json",
            url: urlWsOpenAi + "research/summary",
            headers: {"userToken": dataUser.user_token}
        });
        const itemsRequest = $.ajax({
            method: "GET",
            dataType: "json",
            url: urlWsOpenAi + "research/review-items",
            headers: {"userToken": dataUser.user_token}
        });
        $.when(summaryRequest, itemsRequest).done((summaryArgs, itemArgs) => {
            swal.close();
            $scope.$applyAsync(() => {
                $scope.researchDashboard = summaryArgs[0].data || {counts: {}, training: [], metrics: []};
                $scope.researchItems = itemArgs[0].data || [];
                $scope.researchReview = null;
            });
            $("#modalResearchStudio").modal();
        }).fail(error => { swal.close(); alertAll({status: 1, information: error.responseText}); });
    };

    $scope.selectResearchItem = item => {
        $scope.researchReview = {
            datasetItemId: item.id,
            item: item,
            reviewRound: Number(item.review_count || 0) + 1,
            correctness: 3, completeness: 3, consistency: 3, readability: 3, usability: 3,
            correctionSeconds: 0, addedElements: 0, changedElements: 0, deletedElements: 0,
            decision: "APPROVE", notes: ""
        };
    };

    $scope.submitResearchReview = () => {
        const dataUser = store.session.get("user_tddm4iotbs");
        const payload = angular.copy($scope.researchReview);
        delete payload.item;
        $.ajax({
            method: "POST",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            url: urlWsOpenAi + "research/human-review",
            headers: {"userToken": dataUser.user_token},
            data: JSON.stringify(payload),
            beforeSend: () => loading(),
            success: () => {
                swal.close();
                $("#modalResearchStudio").modal("hide");
                alertAll({status: 2, information: "Scientific review saved."});
                $scope.openResearchSummary();
            },
            error: error => { swal.close(); alertAll({status: 1, information: error.responseText}); }
        });
    };

    $scope.downloadResearchCsv = () => {
        const dataUser = store.session.get("user_tddm4iotbs");
        $.ajax({
            method: "GET",
            url: urlWsOpenAi + "research/export-training.csv",
            headers: {"userToken": dataUser.user_token},
            xhrFields: {responseType: "blob"},
            success: blob => {
                const link = document.createElement("a");
                link.href = URL.createObjectURL(blob);
                link.download = "tddt4iots-research-training.csv";
                link.click();
                URL.revokeObjectURL(link.href);
            }
        });
    };

    $scope.$on("$destroy", () => {
        if ($scope.trainingMonitorTimer) $interval.cancel($scope.trainingMonitorTimer);
    });

    $scope.changeOption = (optionActual) => {
        $scope.optionsTrainModel.forEach(option => {
            if (option.showPanel) {
                option.classActive = "training-step";
                option.showPanel = false;
            }
        });
        optionActual.classActive = "training-step is-active";
        optionActual.showPanel = true;
    }

    $scope.openModalTrainModel = () => {
        $scope.researchManifest = null;
        $scope.requestTrainingModel.samples = [];
        $scope.requestTrainingModel.datasetName = "";
        $scope.requestTrainingModel.datasetVersion = "";
        $scope.requestTrainingModel.schemaVersion = "1.0";
        const manifestInput = document.getElementById("researchManifestFile");
        if (manifestInput) manifestInput.value = "";
        $("#modalNewTrainModel").modal();
        $scope.changeOption($scope.optionsTrainModel[0]);
    }

    $scope.closeModalTrainModel = () => {
        $("#modalNewTrainModel").modal("hide");
    }

    $scope.openModalMoreInformation = (trainingModel) => {
        $scope.detailModalTitle = "Provider response";
        $("#modalMoreInformationTrain").modal();
        $scope.changeOption($scope.optionsTrainModel[0]);

        let response = "";
        if(trainingModel.resultJson.status === "OK") {
            response = JSON.stringify(trainingModel.resultJson, null, 4);
        } else {
            let jsonString = trainingModel.resultTrining;
            response = JSON.stringify(JSON.parse(jsonString), null, 4);
        }

        $scope.resultJson.getSession().setValue(response, -1);
    }

    // Función para escapar comillas dobles dentro de una cadena JSON
    function escapeJsonString(str) {
        return str.replace(/\\([\s\S])|(")/g, "\\$1$2");
    }

    $scope.closeModalMoreInformation = () => {
        $("#modalMoreInformationTrain").modal("hide");
    }

});
