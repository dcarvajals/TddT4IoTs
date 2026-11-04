// Offline behavior checks for the legacy AngularJS controller. No browser or paid API calls.
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const test = require('node:test');
const assert = require('node:assert/strict');
const source = fs.readFileSync(path.join(__dirname, '../../main/webapp/resources/js/js-uml/js_diagram_uml.js'), 'utf8');
const fragment = source.slice(source.indexOf('    $scope.projectModelSelection = null;'), source.indexOf('    $scope.openDocumentWorkspace ='));
function harness() {
    const requests = [], scope = {$applyAsync: fn => fn()};
    const $ = () => ({modal: () => {}}); $.ajax = request => requests.push(request);
    vm.runInNewContext(fragment, {$scope: scope, $, urlWsOpenAi: '/api/', comparisonHeaders: () => ({}), comparisonProjectId: () => 'project-1',
        comparisonImportSnapshot: () => ({inventory: []}), downloadEvidenceFile: () => {}, angular: {copy: v => JSON.parse(JSON.stringify(v))}, Swal: {fire: () => Promise.resolve({isConfirmed: false})}});
    return {scope, requests};
}
test('project model choice is sent without updating Settings selection', () => {
    const {scope, requests} = harness();
    scope.projectModelSelection = {id: 2, registrationKind: 'BASE_MODEL'};
    scope.refreshProjectModels();
    requests.at(-1).success({status: 'OK', data: [
        {id: 1, registrationKind: 'BASE_MODEL', providerModel: 'global-a', selected: true, available: true, mine: true, ownerName: 'A'},
        {id: 2, registrationKind: 'BASE_MODEL', providerModel: 'project-b', selected: false, available: true, mine: false, ownerName: 'B'}]});
    assert.equal(scope.projectVersions.effectiveModel.providerModel, 'project-b');
    assert.equal(scope.projectVersions.globalModel.providerModel, 'global-a');
    assert.ok(requests.every(r => r.method === 'GET'));
    scope.projectVersions.modelKey = ''; scope.projectModelChanged();
    assert.equal(scope.projectModelSelection, null);
    assert.equal(scope.projectVersions.effectiveModel.providerModel, 'global-a');
});
test('saving a correction links its parent and does not submit a claimed model', () => {
    const {scope, requests} = harness();
    scope.activeProjectVersion = {id: 'parent-1'};scope.projectVersions.label = 'Expert revision';
    scope.saveProjectVersion();const body = JSON.parse(requests.at(-1).data);
    assert.equal(body.parentId, 'parent-1');assert.equal(body.projectId, 'project-1');assert.equal(body.model, undefined);
    requests.at(-1).success({status: 'OK', data: 'child-2'});
    assert.equal(scope.activeProjectVersion.id, 'child-2');assert.equal(scope.activeProjectVersion.kind, 'CORRECTION');
});
test('manual snapshot keeps a null origin and cannot fabricate model lineage', () => {
    const {scope, requests} = harness();scope.projectVersions.label = 'Manual';scope.saveProjectVersion();
    assert.equal(JSON.parse(requests.at(-1).data).parentId, null);
    requests.at(-1).success({status: 'OK', data: 'manual-1'});assert.equal(scope.activeProjectVersion.kind, 'MANUAL');
});
test('comparison flags different inputs, settings and derived artifacts', () => {
    const {scope, requests} = harness();scope.projectVersions.selected = {'a': true, 'b': true};scope.compareProjectVersions();
    requests.at(-1).success({status: 'OK', data: [
        {id: 'a', kind: 'GENERATED', metadata: {inputSha256: 'h1', parameters: {maxOutputTokens: 100}, sourceCommit: 'rev1'}, metrics: {classMetricsAvailable: true}},
        {id: 'b', kind: 'CORRECTION', metadata: {inputSha256: 'h2', parameters: {maxOutputTokens: 200}, sourceCommit: 'rev1'}, metrics: {classMetricsAvailable: true}}]});
    assert.ok(scope.projectVersions.warnings.some(w => w.includes('not independent')));
    assert.ok(scope.projectVersions.warnings.some(w => w.includes('requirements input')));
    assert.ok(scope.projectVersions.warnings.some(w => w.includes('Generation settings')));
});
test('filters combine model, type and search without changing the stored rows', () => {
    const {scope} = harness();scope.projectVersions.rows = [
        {id: 'a', label: 'Original', kind: 'GENERATED', metadata: {returnedModel: 'A'}},
        {id: 'b', label: 'Expert correction', kind: 'CORRECTION', metadata: {returnedModel: 'A'}},
        {id: 'c', label: 'Other', kind: 'GENERATED', metadata: {returnedModel: 'B'}}];
    Object.assign(scope.projectVersions, {modelFilter: 'A', kindFilter: 'CORRECTION', search: 'expert'});scope.filterProjectVersions();
    assert.equal(scope.projectVersions.filtered.length, 1);assert.equal(scope.projectVersions.filtered[0].id, 'b');assert.equal(scope.projectVersions.rows.length, 3);
});
test('insufficient comparison selection never makes a server request', () => {
    const {scope, requests} = harness();scope.projectVersions.selected = {'one': true};scope.compareProjectVersions();
    assert.equal(requests.length, 0);assert.match(scope.projectVersions.error, /two and four/);
});
test('opening a version clears a reused use-case canvas before XML import', () => {
    const diagram = {_nodes: ['stale node'], _relations: ['stale relation']};
    const button = {type: 'UMLUseCaseDiagram', diagram};
    const scope = {btnDiagramUml: [button], useCaseDiagrams: [], changeDiagramA: () => {}};
    const context = {$scope: scope, registerUseCaseDiagram: (b,data,identity) => {const entry = {...identity, button:b, data};scope.useCaseDiagrams.push(entry);return entry;},
        graphWithCurrentProjectName: value => value, importDiagram: d => {assert.equal(d._nodes.length,0);assert.equal(d._relations.length,0);d._nodes.push('restored node');d._relations.push('restored relation');},
        getDataDiagram: () => {}, activateUseCaseEntry: () => {}, blankUseCaseData: () => ({})};
    const start = source.indexOf('    const loadWorkspaceUseCaseDiagrams =');
    const end = source.indexOf('    apiLoadModeling =', start);
    vm.runInNewContext(source.slice(start,end) + '\nthis.restore = loadWorkspaceUseCaseDiagrams;', context);
    context.restore({activeDiagramId:'d1',useCaseDiagrams:[{id:'d1',name:'Care',data:{},graph:'<diagram/>'}]});
    assert.equal(diagram._nodes.length,1);assert.equal(diagram._relations.length,1);assert.equal(scope.useCaseDiagrams[0].id,'d1');
});
test('model dialog previews a choice without changing the applied project model', () => {
    const {scope, requests} = harness();
    scope.documentWorkspace = {loading:false};
    const a = {key:'BASE_MODEL:1', id:1, registrationKind:'BASE_MODEL', providerModel:'a', available:true};
    const b = {key:'BASE_MODEL:2', id:2, registrationKind:'BASE_MODEL', providerModel:'b', available:true};
    scope.projectVersions.catalog = [a,b];scope.projectVersions.globalModel = a;
    scope.projectVersions.modelKey = a.key;scope.projectModelChanged();
    scope.projectModelDialog.key = b.key;scope.previewProjectModel();
    assert.equal(scope.projectModelDialog.preview.providerModel,'b');
    assert.equal(scope.projectModelSelection.id,1);
    scope.applyProjectModel();assert.equal(scope.projectModelSelection.id,2);
    assert.equal(requests.length,0);
});
test('cancelling draft selection preserves the existing model', () => {
    const {scope} = harness();scope.projectModelSelection = {id:1,registrationKind:'BASE_MODEL'};
    scope.projectModelDialog.key = 'BASE_MODEL:2';scope.closeProjectModelDialog();
    assert.equal(scope.projectModelSelection.id,1);
});
test('model dialog cannot apply an unavailable registration', () => {
    const {scope} = harness();scope.documentWorkspace = {loading:false};scope.projectModelSelection = {id:1,registrationKind:'BASE_MODEL'};
    scope.projectVersions.catalog = [{key:'BASE_MODEL:2',id:2,available:false,unavailableReason:'Owner credential missing'}];
    scope.projectModelDialog.key = 'BASE_MODEL:2';scope.applyProjectModel();
    assert.equal(scope.projectModelSelection.id,1);assert.match(scope.projectModelDialog.error,/Owner credential/);
});
test('late catalog response after cancellation cannot reopen or change the draft', () => {
    const {scope, requests} = harness();scope.openProjectModelDialog();scope.closeProjectModelDialog();
    scope.projectModelDialog.key = 'cancelled-draft';
    requests.at(-1).success({status:'OK',data:[]});
    assert.equal(scope.projectModelDialog.key,'cancelled-draft');
});
