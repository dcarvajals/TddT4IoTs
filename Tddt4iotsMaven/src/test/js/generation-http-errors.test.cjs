const test = require('node:test');
const assert = require('node:assert/strict');
const vm = require('node:vm');
const fs = require('node:fs');
const path = require('node:path');
const source=fs.readFileSync(path.join(__dirname,'../../main/webapp/resources/js/js-uml/js_diagram_uml.js'),'utf8');
const helper=source.slice(source.indexOf('    const documentGenerationHttpError ='),source.indexOf('    $scope.generateCaseWorkspaceFromDocuments ='));
const format=vm.runInNewContext(helper+'\ndocumentGenerationHttpError');
test('gateway HTML is replaced by actionable HTTP diagnostics, never rendered',()=>{
 const result=format({status:504,responseText:'<html>upstream timed out</html>'},'error');
 assert.match(result,/HTTP 504/);assert.match(result,/may still be running/);assert.doesNotMatch(result,/<html>/);
});
test('preserves server JSON error with HTTP status',()=>{
 assert.equal(format({status:400,responseJSON:{message:'Invalid schema'}},'error'),'HTTP 400: Invalid schema');
 assert.equal(format({status:500,responseText:'{"detail":"Execution failed"}'},'error'),'HTTP 500: Execution failed');
});
test('distinguishes request size, missing route and connection failure',()=>{
 assert.match(format({status:413},'error'),/request size/);
 assert.match(format({status:404},'error'),/endpoint was not found/);
 assert.match(format({status:0},'error'),/No HTTP response/);
 assert.match(format({status:0},'timeout'),/may still be running/);
});
test('invalid success JSON has its own diagnostic',()=>{
 assert.match(format({status:200,responseText:'<html>login</html>'},'parsererror'),/not valid JSON/);
});
