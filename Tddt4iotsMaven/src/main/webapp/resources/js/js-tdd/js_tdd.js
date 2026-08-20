app.expandControllerA = function ($scope) {
    //#region VARIABLES
    $scope.methodsTdd = [];
    $scope.classTdd = [];
    $scope.cssMethod = "";
    $scope.methodSelected = {};
    $scope.jsonTdd = {
        "idproject": "",
        "type": "message",
        "subytpe": "tdd",
        "typeSave": "",
        "numer": "",
        "receiveTDDJSOM": $scope.classTdd
    }; // variable general de todo el test a guardar
    $scope.simbolVisi = {
        "public": "+",
        "private": "-",
        "protected": "#",
        "static": "$",
        "abstract": "?",
        "final": "¬",
        "interface": "@"
    }
    $scope.classNameSelected = "";
    $scope.arr = [];
    var global = "";
    $scope.instanceCode = undefined;
    $scope.flagSelected = false;
    $scope.testStudioOpen = false;
    $scope.testStudioSearch = "";
    $scope.testStudioInputs = {};
    $scope.testStudioCase = {
        name: "",
        assertion: "equals",
        expected: ""
    };
    $scope.testStudioCode = "Select a method from the model tree to preview its JUnit 5 tests.";
    //#endregion VARIABLES

    //#region FUNCIONES UTILITARIAS

    // funcion para abrir el modal de las tdd
    $scope.openModalTdd = () => {
        $scope.testStudioOpen = true;
        $scope.flagSelected = false;
        $scope.testStudioInputs = {};
        $scope.testStudioCase = {name: "", assertion: "equals", expected: ""};
        $scope.refreshTestStudioCode();
        window.setTimeout(() => window.dispatchEvent(new Event("resize")), 0);
    };

    $scope.closeTestStudio = () => {
        $scope.testStudioOpen = false;
        window.setTimeout(() => window.dispatchEvent(new Event("resize")), 0);
    };

    $scope.testStudioClassesCount = () => {
        let total = 0;
        for (const pack of (($scope.jsonClass && $scope.jsonClass.diagram) || [])) {
            total += (pack.class || []).length;
        }
        return total;
    };

    $scope.testStudioMethodsCount = () => {
        let total = 0;
        for (const pack of (($scope.jsonClass && $scope.jsonClass.diagram) || [])) {
            for (const itemClass of (pack.class || [])) total += (itemClass.methods || []).length;
        }
        return total;
    };

    $scope.testStudioConfiguredMethodsCount = () => {
        let total = 0;
        for (const itemClass of ($scope.classTdd || [])) total += (itemClass.Methods || []).length;
        return total;
    };

    $scope.testStudioCasesCount = () => {
        let total = 0;
        for (const itemClass of ($scope.classTdd || [])) {
            for (const method of (itemClass.Methods || [])) total += (method.parameters || []).length;
        }
        return total;
    };

    $scope.testStudioCoverage = () => {
        const methods = $scope.testStudioMethodsCount();
        return methods ? Math.round(($scope.testStudioConfiguredMethodsCount() * 100) / methods) : 0;
    };

    $scope.testStudioMethodHasTests = (className, methodName) => {
        const classPosition = $scope.existMethod($scope.classTdd, "class", className);
        if (!classPosition[1]) return false;
        return $scope.existMethod($scope.classTdd[classPosition[0]].Methods || [], "NameMethod", methodName)[1];
    };

    $scope.testStudioSelectedMethodData = () => {
        if (!$scope.methodSelected || !$scope.methodSelected.name || !$scope.classNameSelected) return null;
        const classPosition = $scope.existMethod($scope.classTdd, "class", $scope.classNameSelected);
        if (!classPosition[1]) return null;
        const methodPosition = $scope.existMethod($scope.classTdd[classPosition[0]].Methods || [], "NameMethod", $scope.methodSelected.name);
        return methodPosition[1] ? $scope.classTdd[classPosition[0]].Methods[methodPosition[0]] : null;
    };

    $scope.testStudioSelectedCases = () => {
        const method = $scope.testStudioSelectedMethodData();
        return method ? (method.parameters || []) : [];
    };

    $scope.testStudioCaseMetadata = index => {
        const method = $scope.testStudioSelectedMethodData();
        return method && method.cases && method.cases[index] ? method.cases[index] : {};
    };

    $scope.selectTestStudioMethod = (method, pack, className) => {
        $scope.testStudioSelectedPack = pack;
        $scope.activeMethod(method, pack, className);
        $scope.testStudioInputs = {};
        $scope.testStudioCase = {
            name: method.name + " - expected behavior",
            assertion: method.type === "void" ? "completes" : "equals",
            expected: ""
        };
        $scope.refreshTestStudioCode();
    };

    const normalizedTestValue = (type, value) => {
        const raw = value === undefined || value === null ? "" : String(value).trim();
        const normalizedType = String(type || "").split(":").pop().toLowerCase();
        if (normalizedType === "string") return '"' + raw.replace(/\\/g, "\\\\").replace(/"/g, '\\"') + '"';
        if (normalizedType === "char") return "'" + raw.replace(/'/g, "\\'") + "'";
        if (normalizedType === "long" && /^-?\d+$/.test(raw)) return raw + "L";
        if (normalizedType === "float" && /^-?\d+(\.\d+)?$/.test(raw)) return raw + "f";
        return raw || "null";
    };

    $scope.addTestFromStudio = (method) => {
        const values = [];
        for (const parameter of (method.parameters || [])) {
            const value = $scope.testStudioInputs[parameter.name];
            if (value === undefined || String(value).trim() === "") {
                alertAll({status: 3, information: "Complete all method inputs before adding the test."});
                return;
            }
            values.push({
                typeDate: String(parameter.type || "").split(":").pop(),
                name: parameter.name,
                value: String(value).trim()
            });
        }

        const assertion = $scope.testStudioCase.assertion;
        if (assertion === "equals" && String($scope.testStudioCase.expected || "").trim() === "") {
            alertAll({status: 3, information: "Enter the expected result for this test."});
            return;
        }

        let classPosition = $scope.existMethod($scope.classTdd, "class", $scope.classNameSelected);
        if (!classPosition[1]) {
            $scope.classTdd.push({Type: "", Pack: $scope.testStudioSelectedPack, class: $scope.classNameSelected, Methods: []});
            classPosition = [$scope.classTdd.length - 1, true];
        }
        const methods = $scope.classTdd[classPosition[0]].Methods;
        let methodPosition = $scope.existMethod(methods, "NameMethod", method.name);
        if (!methodPosition[1]) {
            methods.push({
                typeVisibility: method.visibility,
                TypeMethods: method.type,
                NameMethod: method.name,
                parameters: [],
                return: [],
                cases: []
            });
            methodPosition = [methods.length - 1, true];
        }

        const target = methods[methodPosition[0]];
        target.parameters.push(values);
        target.return.push({type: method.type, value: String($scope.testStudioCase.expected || "").trim()});
        if (!target.cases) target.cases = [];
        target.cases.push({
            name: String($scope.testStudioCase.name || (method.name + " test")).trim(),
            assertion: assertion
        });
        $scope.jsonTdd.receiveTDDJSOM = $scope.classTdd;
        $scope.testStudioInputs = {};
        $scope.testStudioCase.expected = "";
        $scope.refreshTestStudioCode();
        alertAll({status: 1, information: "Test case added to the design."});
    };

    $scope.deleteTestStudioCase = (methodName, index) => {
        const itemMethod = $scope.searchMethodTest(methodName);
        if (!itemMethod || !itemMethod.parameters) return;
        itemMethod.parameters.splice(index, 1);
        if (itemMethod.return) itemMethod.return.splice(index, 1);
        if (itemMethod.cases) itemMethod.cases.splice(index, 1);
        if (itemMethod.parameters.length === 0) {
            const classPosition = $scope.existMethod($scope.classTdd, "class", $scope.classNameSelected);
            if (classPosition[1]) {
                const methodPosition = $scope.existMethod($scope.classTdd[classPosition[0]].Methods, "NameMethod", methodName);
                if (methodPosition[1]) $scope.classTdd[classPosition[0]].Methods.splice(methodPosition[0], 1);
                if ($scope.classTdd[classPosition[0]].Methods.length === 0) $scope.classTdd.splice(classPosition[0], 1);
            }
        }
        $scope.refreshTestStudioCode();
    };

    $scope.testStudioSelectedPack = "";

    $scope.cancelActiveMethod = () => {
        $scope.flagSelected = false;
    }

    // funcion para seleccionar un metodo
    $scope.activeMethod = (method, pack, className) => {
        $scope.classNameSelected = className;
        $scope.flagSelected = true;
        let objectSelected = $scope.existMethod($scope.classTdd, "class", className);
        if (method.type !== "void") {
            if (!objectSelected[1]) {
                $scope.classTdd.push({
                    "Type": "",
                    "Pack": pack,
                    "class": className,
                    "Methods": []
                });
            }
        }
        $scope.cssMethod = method.name; // methodsDiagram.name
        $scope.methodSelected = method;
        console.log($scope.classTdd);
    }


    // funcion para agregar un nuevo test
    $scope.addTest = (method) => {
        let values = [];
        // recorremos los parametros
        for (let i = 0; i < method.parameters.length; i++) {
            let typeData = method.parameters[i].type.split(":");
            let newTypeData = "";
            if (typeData.length > 1) {
                newTypeData = typeData[1];
            } else {
                newTypeData = method.parameters[i].type;
            }
            let value = $("#param" + i);

            if($scope.returnMethod === undefined){
                alertAll({status: 3, information: "Please fill in all parameters."});
                values.length = 0;
                return;
            }

            if(value.val().trim() === "" || $scope.returnMethod.trim() === ""){
                alertAll({status: 3, information: "Please fill in all parameters."});
                values.length = 0;
                return;
            }

            values.push({
                "typeDate": newTypeData,
                "name": method.parameters[i].name,
                "value": value.val()
            })
            value.val("");
        }
        let classSelected = $scope.existMethod($scope.classTdd, "class", $scope.classNameSelected);
        let position = !classSelected[1] ? 0 : classSelected[0];
        $scope.methodsTdd = $scope.classTdd[position]["Methods"];
        let existMethod = $scope.existMethod($scope.methodsTdd, "NameMethod", method.name);
        if (existMethod[1]) {
            $scope.methodsTdd[existMethod[0]].parameters.push(values);
            $scope.methodsTdd[existMethod[0]].return.push({
                "type": $scope.methodSelected.type,
                "value": $scope.returnMethod
            });
            $scope.returnMethod = "";
        } else {
            $scope.methodsTdd.push({
                "typeVisibility": method.visibility,
                "TypeMethods": method.type,
                "NameMethod": method.name,
                "parameters": [values],
                "return": [{"type": $scope.methodSelected.type, "value": $scope.returnMethod}]
            });
        }
        $scope.returnMethod = "";
        console.log($scope.jsonTdd);

    }

    $scope.deleteTest = (position) => {
        Swal.fire({
            title: 'Are you sure?',
            text: "All data entered will be permanently deleted.!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Yes, cancels all!'
        }).then((result) => {
            if (result.isConfirmed) {
                let objectTest = $scope.searchMethodTest($scope.methodSelected.name).parameters;
                console.log(objectTest);
                $scope.$apply(() => {
                    objectTest.splice(position, 1);
                    if (objectTest.length === 0) {
                        let objClass = $scope.existMethod($scope.classTdd, "class", $scope.classNameSelected);
                        if (objClass[1]) {
                            $scope.classTdd.splice(objClass[0], 1);
                            console.log($scope.classTdd);
                            console.log($scope.jsonTdd);
                        }
                    }
                });
            }
        });
    };

    // buscar el json del metodo que tiene un tezst
    $scope.searchMethodTest = (name) => {
        $(function () {
            $('[data-toggle="tooltip"]').tooltip()
        });
        let classSelected = $scope.existMethod($scope.classTdd, "class", $scope.classNameSelected);
        if (classSelected[0] !== -1) {
            let position = !classSelected[1] ? 0 : classSelected[0];
            $scope.methodsTdd = $scope.classTdd[position]["Methods"];
            for (let i = 0; i < $scope.methodsTdd.length; i++) {
                if ($scope.methodsTdd[i].NameMethod === name) {
                    return $scope.methodsTdd[i];
                }
            }
            return [];
        }
    };

    // metodo que verifica su un metodo ya existe con test
    $scope.existMethod = (array, key, name) => {
        for (let i = 0; i < array.length; i++) {
            if (array[i][key] === name) {
                return [i, true];
            }
        }
        return [-1, false];
    };

    //#endregion FUNCIONES UTILITARIAS

    $scope.generateTddGlobal = () => {
        let codeJUnit = $scope.generateCodeJUnit($scope.classTdd);
        loadCodeTDD(codeJUnit);
        $("#modal_code_tdd").modal();
        console.log(codeJUnit);
    };

    // funcion para visualziar el codigo tdd de una clase seleccionada
    $scope.viewCodeTddClass = (className) => {
        let positionClass = $scope.existMethod($scope.classTdd, "class", className);
        if (positionClass[1]) {
            let codeJUnit = $scope.generateCodeJUnitClass($scope.classTdd[positionClass[0]]);
            loadCodeTDD(codeJUnit);
            $("#modal_code_tdd").modal();
            console.log(codeJUnit);
        }
    };

    /**
     * METODO PARA GENERAR EL CODIGO DE JTESTUNIT
     * */
    $scope.generateCodeJUnit = (jsonconjuntest) => {
        var a = global.split(";");
        var string = "//package " + "package of name" + " \nimport org.junit.After;\n" +
            "import org.junit.AfterClass;\n" +
            "import org.junit.Before;\n" +
            "import org.junit.BeforeClass;\n" +
            "import org.junit.Test;\n" +
            "import static org.junit.Assert.*; \n\n";
        for (let itemClass of jsonconjuntest) {
            var type = itemClass.Type;
            var className = itemClass.class;
            var arrayclassName = className;
            string += "\t" + type + " class " + arrayclassName + "{\n";
            string += "\t" + arrayclassName + " instance = new " + arrayclassName + "();\n";
            string += "\t" + type + " " + arrayclassName + "(){\n}\n";
            string += "\t@BeforeClass \n" +
                "\tpublic static void setUpClass() { \n" +
                "\t} \n" +
                "\t@AfterClass \n" +
                "\tpublic static void tearDownClass() { \n" +
                "\t} \n" +
                "\t@Before \n" +
                "\tpublic void setUp() { \n" +
                "\t} \n" +
                "\t@After \n" +
                "\tpublic void tearDown() { \n" +
                "\t}\n";
            let methods = itemClass.Methods;
            var num = 0;
            for (let itemTest of methods) {
                for (let itemMethods of itemTest.parameters) {
                    if (num < itemTest.parameters.length) {
                        string += "\n \t@Test \n";
                        var typeMethods = itemTest.TypeMethods;
                        var nameMethod = itemTest.NameMethod;
                        string += "\tpublic void" + " " + nameMethod + "_Test" + num + "(){\n";
                        //let data2 = JSON.parse(item.parameter);
                        string += "\t\tSystem.out.println(\"" + nameMethod + "\");\n";
                        let parameters = itemTest.parameters[num];
                        var params = new Array(parameters.length);
                        var typeDate = new Array(parameters.length);
                        var value = new Array(parameters.length);
                        var indx = 0;
                        for (let itemParameters of parameters) {
                            typeDate[indx] = (itemParameters.typeDate);
                            params[indx] = (itemParameters.name);
                            value[indx] = (itemParameters.value);

                            if (typeDate[indx] === ("String"))
                                string += "\t\t" + typeDate[indx] + " " + params[indx] + " = \"" + value[indx] + "\";\n";
                            else if (typeDate[indx] === ("char"))
                                string += "\t\t" + typeDate[indx] + " " + params[indx] + " = '" + value[indx] + "';\n";
                            else
                                string += "\t\t" + typeDate[indx] + " " + params[indx] + " = " + value[indx] + ";\n";
                            indx++;
                        }
                        var retrun = (itemTest.return[num]);
                        if (retrun["type"] === ("String"))
                            string += "\t\t" + typeMethods + " expResult =\"" + retrun["value"] + "\";\n";
                        else if (retrun["type"] === ("char"))
                            string += "\t\t" + typeMethods + " expResult ='" + retrun["value"] + "';\n";
                        else
                            string += "\t\t" + typeMethods + " expResult =" + retrun["value"] + ";\n";

                        string += "\t\t" + typeMethods + " result = " + "instance" + "." + nameMethod + "(";
                        indx = 0;
                        for (let itemParameters of parameters) {
                            if (indx === params.length - 1)
                                string += " " + params[indx] + ");\n";
                            else
                                string += " " + params[indx] + ", ";

                            indx++;
                        }
                        string += "\t\t//assertEquals(expResult, result);\n";
                        string += "\t\t// TODO review the generated test code and remove the default call to fail.\n";
                        if (typeMethods === "String")
                            string += "\t\tif(!expResult.equals(result))\n";
                        else
                            string += "\t\tif(expResult!=(result))\n";
                        string += "\t\t\tfail(\"The test case is a prototype.\");\n";
                        string += "\t\telse\n\t\t\tSystem.out.println(\"The test case is a good prototype!\");\n}";

                        num += 1;
                    }
                    string += "\t}\n\n";
                }
            }
        }

        //console.log(string);
        return string;
    }

    $scope.generateCodeJUnitClass = (itemClass) => {
        var a = global.split(";");
        var string = "//package " + "package of name" + " \nimport org.junit.After;\n" +
            "import org.junit.AfterClass;\n" +
            "import org.junit.Before;\n" +
            "import org.junit.BeforeClass;\n" +
            "import org.junit.Test;\n" +
            "import static org.junit.Assert.*; \n\n";
        var type = itemClass.Type;
        var className = itemClass.class;
        var arrayclassName = className;
        string += "\t" + type + " class " + arrayclassName + "{\n";
        string += "\t" + arrayclassName + " instance = new " + arrayclassName + "();\n";
        string += "\t" + type + " " + arrayclassName + "(){\n}\n";
        string += "\t@BeforeClass \n" +
            "\tpublic static void setUpClass() { \n" +
            "\t} \n" +
            "\t@AfterClass \n" +
            "\tpublic static void tearDownClass() { \n" +
            "\t} \n" +
            "\t@Before \n" +
            "\tpublic void setUp() { \n" +
            "\t} \n" +
            "\t@After \n" +
            "\tpublic void tearDown() { \n" +
            "\t}\n";
        let methods = itemClass.Methods;
        var num = 0;
        for (let itemTest of methods) {
            for (let itemMethods of itemTest.parameters) {
                if (num < itemTest.parameters.length) {
                    string += "\n \t@Test \n";
                    var typeMethods = itemTest.TypeMethods;
                    var nameMethod = itemTest.NameMethod;
                    string += "\tpublic void" + " " + nameMethod + "_Test" + num + "(){\n";
                    //let data2 = JSON.parse(item.parameter);
                    string += "\t\tSystem.out.println(\"" + nameMethod + "\");\n";
                    let parameters = itemTest.parameters[num];
                    var params = new Array(parameters.length);
                    var typeDate = new Array(parameters.length);
                    var value = new Array(parameters.length);
                    var indx = 0;
                    for (let itemParameters of parameters) {
                        typeDate[indx] = (itemParameters.typeDate);
                        params[indx] = (itemParameters.name);
                        value[indx] = (itemParameters.value);

                        if (typeDate[indx] === ("String"))
                            string += "\t\t" + typeDate[indx] + " " + params[indx] + " = \"" + value[indx] + "\";\n";
                        else if (typeDate[indx] === ("char"))
                            string += "\t\t" + typeDate[indx] + " " + params[indx] + " = '" + value[indx] + "';\n";
                        else
                            string += "\t\t" + typeDate[indx] + " " + params[indx] + " = " + value[indx] + ";\n";
                        indx++;
                    }
                    var retrun = (itemTest.return[num]);
                    if (retrun["type"] === ("String"))
                        string += "\t\t" + typeMethods + " expResult =\"" + retrun["value"] + "\";\n";
                    else if (retrun["type"] === ("char"))
                        string += "\t\t" + typeMethods + " expResult ='" + retrun["value"] + "';\n";
                    else
                        string += "\t\t" + typeMethods + " expResult =" + retrun["value"] + ";\n";

                    string += "\t\t" + typeMethods + " result = " + "instance" + "." + nameMethod + "(";
                    indx = 0;
                    for (let itemParameters of parameters) {
                        if (indx === params.length - 1)
                            string += " " + params[indx] + ");\n";
                        else
                            string += " " + params[indx] + ", ";

                        indx++;
                    }
                    string += "\t\t//assertEquals(expResult, result);\n";
                    string += "\t\t// TODO review the generated test code and remove the default call to fail.\n";
                    if (typeMethods === "String")
                        string += "\t\tif(!expResult.equals(result))\n";
                    else
                        string += "\t\tif(expResult!=(result))\n";
                    string += "\t\t\tfail(\"The test case is a prototype.\");\n";
                    string += "\t\telse\n\t\t\tSystem.out.println(\"The test case is a good prototype!\");\n}";

                    num += 1;
                }
                string += "\t}\n\n";
            }
        }

        //console.log(string);
        return string;
    }

    const safeJavaIdentifier = (value, fallback) => {
        const identifier = String(value || fallback || "test").replace(/[^A-Za-z0-9_$]/g, "_");
        return /^\d/.test(identifier) ? "test_" + identifier : identifier;
    };

    const findModeledClass = (className) => {
        for (const pack of (($scope.jsonClass && $scope.jsonClass.diagram) || [])) {
            for (const itemClass of (pack.class || [])) {
                if (itemClass.className === className) return {pack: pack.packName || "", itemClass: itemClass};
            }
        }
        return null;
    };

    const generateJUnit5ForClass = (testClass) => {
        if (!testClass) return "Select a configured class to preview its tests.";
        const modeled = findModeledClass(testClass.class);
        const packageName = testClass.Pack || (modeled && modeled.pack) || "";
        let code = packageName ? "package " + packageName + ";\n\n" : "";
        code += "import org.junit.jupiter.api.Test;\n";
        code += "import static org.junit.jupiter.api.Assertions.*;\n\n";
        code += "class " + safeJavaIdentifier(testClass.class, "Generated") + "Test {\n\n";
        code += "    private final " + testClass.class + " instance = new " + testClass.class + "();\n";

        for (const itemMethod of (testClass.Methods || [])) {
            const modeledMethod = modeled && (modeled.itemClass.methods || []).find(method => method.name === itemMethod.NameMethod);
            const declaredParameters = modeledMethod ? (modeledMethod.parameters || []) : [];
            for (let caseIndex = 0; caseIndex < (itemMethod.parameters || []).length; caseIndex++) {
                const values = itemMethod.parameters[caseIndex] || [];
                const metadata = (itemMethod.cases || [])[caseIndex] || {};
                const assertion = metadata.assertion || (itemMethod.TypeMethods === "void" ? "completes" : "equals");
                const caseName = safeJavaIdentifier(metadata.name, itemMethod.NameMethod + "_case_" + (caseIndex + 1));
                code += "\n    @Test\n";
                code += "    void " + caseName + "() {\n";
                const invocationArgs = [];
                for (let paramIndex = 0; paramIndex < values.length; paramIndex++) {
                    const parameter = values[paramIndex];
                    const declared = declaredParameters[paramIndex] || {};
                    const type = String(declared.type || parameter.typeDate || "Object").split(":").pop();
                    const name = safeJavaIdentifier(parameter.name, "arg" + paramIndex);
                    code += "        " + type + " " + name + " = " + normalizedTestValue(type, parameter.value) + ";\n";
                    invocationArgs.push(name);
                }
                const invocation = "instance." + itemMethod.NameMethod + "(" + invocationArgs.join(", ") + ")";
                const expected = (itemMethod.return || [])[caseIndex] || {type: itemMethod.TypeMethods, value: ""};
                if (assertion === "notNull") {
                    code += "        assertNotNull(" + invocation + ");\n";
                } else if (assertion === "throws") {
                    code += "        assertThrows(Exception.class, () -> " + invocation + ");\n";
                } else if (assertion === "completes" || itemMethod.TypeMethods === "void") {
                    code += "        assertDoesNotThrow(() -> " + invocation + ");\n";
                } else {
                    code += "        " + itemMethod.TypeMethods + " expected = " + normalizedTestValue(expected.type, expected.value) + ";\n";
                    code += "        assertEquals(expected, " + invocation + ");\n";
                }
                code += "    }\n";
            }
        }
        code += "}\n";
        return code;
    };

    $scope.refreshTestStudioCode = () => {
        let selectedClass = null;
        if ($scope.classNameSelected) {
            const classPosition = $scope.existMethod($scope.classTdd, "class", $scope.classNameSelected);
            if (classPosition[1]) selectedClass = $scope.classTdd[classPosition[0]];
        }
        $scope.testStudioCode = selectedClass
            ? generateJUnit5ForClass(selectedClass)
            : "Select a method and add its first test case to generate the JUnit 5 preview.";
    };

    const downloadTextArtifact = (content, fileName, mimeType) => {
        const blob = new Blob([content], {type: mimeType || "text/plain;charset=utf-8"});
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    };

    $scope.downloadTestStudioCode = () => {
        if (!$scope.classNameSelected || !$scope.testStudioMethodHasTests($scope.classNameSelected, $scope.methodSelected.name)) {
            alertAll({status: 3, information: "Add at least one test case before downloading the source."});
            return;
        }
        downloadTextArtifact($scope.testStudioCode, safeJavaIdentifier($scope.classNameSelected, "Generated") + "Test.java", "text/x-java;charset=utf-8");
    };

    const escapeReportHtml = value => String(value === undefined || value === null ? "" : value)
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");

    $scope.downloadTestStudioReport = () => {
        const rows = [];
        for (const itemClass of ($scope.classTdd || [])) {
            for (const method of (itemClass.Methods || [])) {
                for (let index = 0; index < (method.parameters || []).length; index++) {
                    const metadata = (method.cases || [])[index] || {};
                    rows.push("<tr><td>" + escapeReportHtml(itemClass.class) + "</td><td>" + escapeReportHtml(method.NameMethod) +
                        "</td><td>" + escapeReportHtml(metadata.name || ("Case " + (index + 1))) + "</td><td>" +
                        escapeReportHtml(metadata.assertion || "equals") + "</td><td>Designed · not executed</td></tr>");
                }
            }
        }
        const projectName = ($scope.dataProject && $scope.dataProject.name_mp) || "TDDT4IoTS project";
        const html = "<!doctype html><html><head><meta charset='utf-8'><title>Test design report</title><style>" +
            "body{font-family:Arial,sans-serif;color:#34434c;margin:36px}h1{font-size:22px;margin-bottom:4px}p{color:#718089;font-size:12px}" +
            ".summary{display:flex;gap:12px;margin:24px 0}.summary div{border:1px solid #dbe5e0;border-radius:8px;padding:12px 18px}.summary strong{display:block;color:#176b48;font-size:20px}" +
            "table{border-collapse:collapse;width:100%;font-size:11px}th{background:#30463d;color:#fff;text-align:left}th,td{border:1px solid #e1e8e4;padding:8px}tr:nth-child(even){background:#f6f9f7}" +
            "</style></head><body><h1>Test design report</h1><p>" + escapeReportHtml(projectName) + " · Generated by TDDT4IoTS Test Studio</p>" +
            "<div class='summary'><div><strong>" + $scope.testStudioCasesCount() + "</strong>Cases</div><div><strong>" +
            $scope.testStudioConfiguredMethodsCount() + "/" + $scope.testStudioMethodsCount() + "</strong>Methods</div><div><strong>" +
            $scope.testStudioCoverage() + "%</strong>Design coverage</div></div><table><thead><tr><th>Class</th><th>Method</th><th>Case</th><th>Assertion</th><th>Status</th></tr></thead><tbody>" +
            (rows.join("") || "<tr><td colspan='5'>No test cases have been designed.</td></tr>") + "</tbody></table></body></html>";
        downloadTextArtifact(html, "test-design-report.html", "text/html;charset=utf-8");
    };

    //#region VIEW CODE

    let loadCodeTDD = (code) => {
        $("#modal_generate_tdd").modal("hide");
        $("#modal_code_tdd").modal("show");
        $scope.instanceCode.setValue(code);
    }

    $scope.closeCodeTdd = () => {
        $("#modal_generate_tdd").modal("show");
        $("#modal_code_tdd").modal("hide");
    }

    $scope.initCode = () => {
        let codeDefault = "//Code generated by the application TddM4IoTs";
        $scope.instanceCode = CodeMirror(document.getElementById("codeTdd"),
            {
                lineNumbers: true,
                mode: "text/x-java",
                matchBrackets: true,
                readOnly: true,
                styleActiveLine: true,
                theme: 'eclipse',
                indentUnit: 2,
                height: 765
            });
        //instanceCode.setValue(codeDefault);
    }

    //#endregion VIEW CODE

}
