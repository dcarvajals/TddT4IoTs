const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const path=require('node:path');
const audit=require('../../main/webapp/resources/js/js-uml/correction-audit.js');
const source=fs.readFileSync(path.join(__dirname,'../../main/webapp/resources/js/js-uml/js_diagram_uml.js'),'utf8');
const fragment=source.slice(source.indexOf('    let correctionAuditTransition ='),source.indexOf('    $scope.selectAssessmentReference ='));
test('editor interactions are captured while panel is closed, saved and detached on destroy',async()=>{
 const requests=[],events={},timers=[],storage=new Map();let interval,destroy,serial=0;
 let workspace={useCaseDiagrams:[{id:'d1',data:{name:'Before'},graph:'<diagram/>'}],sequences:[],classModel:{}};
 const scope={$evalAsync:f=>f(),$applyAsync:f=>f(),$on:(n,f)=>{destroy=f;},generationEvidence:{runId:'run-1'}};
 const $=()=>({modal:()=>{}});$.ajax=r=>requests.push(r);
 vm.runInNewContext(fragment,{$scope:scope,$,UmlCorrectionAudit:audit,urlWsOpenAi:'/api/',comparisonHeaders:()=>({userToken:'session'}),
  comparisonImportSnapshot:()=>workspace,alertAll:()=>{},
  window:{crypto:{randomUUID:()=>String(++serial)},localStorage:{getItem:k=>storage.get(k),setItem:(k,v)=>storage.set(k,v)},addEventListener:()=>{},removeEventListener:()=>{}},
  document:{addEventListener:(n,f)=>events[n]=f,removeEventListener:n=>delete events[n]},
  setTimeout:f=>{timers.push(f);return timers.length;},clearTimeout:()=>{},setInterval:f=>{interval=f;return 1;},clearInterval:()=>{interval=null;}});
 scope.selectAssessmentReference=()=>{};scope.openComparisonAssessment({id:'run-1'});
 requests.at(-1).success({status:'OK',data:{model:'model-a',currentUserId:1,efforts:[{id:'session-1',reviewer_id:1,status:'ACTIVE',change_log:[]}],references:[]}});
 workspace={...workspace,classModel:{changed:true}};
 events.click({type:'click'});timers.splice(0).forEach(f=>f());assert.equal(scope.correctionAuditStatus.pending,1);
 interval();const request=requests.at(-1);assert.match(request.url,/run-1\/assessments\/effort$/);
 const body=JSON.parse(request.data);assert.equal(body.action,'log_batch');assert.equal(body.sessionId,'session-1');
 assert.equal(body.changes[0].patches[0].path,'/classModel/changed');
 request.success({status:'OK'});await new Promise(resolve=>setImmediate(resolve));assert.equal(scope.correctionAuditStatus.pending,0);
 // A paused session never captures subsequent canvas changes.
 scope.assessmentPanel.activeEffort.status='PAUSED';workspace={...workspace,classModel:{changed:false}};
 events.click({type:'click'});timers.splice(0).forEach(f=>f());assert.equal(scope.correctionAuditStatus.pending,0);
 destroy();assert.equal(Object.keys(events).length,0);assert.equal(interval,null);
});
