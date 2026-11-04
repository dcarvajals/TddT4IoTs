const {test} = require('node:test');
const assert = require('node:assert/strict');
const {create, normalize} = require('../../main/webapp/resources/js/js-uml/correction-audit.js');
const snapshot = name => ({activeDiagramId: 'd1', inventory: [], useCaseDiagrams: [{id:'d1',data:{name},graph:'<diagram/>'}], sequences:[], classModel:{}});
function harness(send = async () => {}) {
    const values = new Map(), states = []; let serial = 0;
    const options = {storage:{getItem:k=>values.get(k),setItem:(k,v)=>values.set(k,v)}, status:s=>states.push(s), error:()=>{}, uuid:()=>String(++serial), send};
    return {audit:create(options), options, values, states};
}
test('captures an edit and its reversal as separate durable events', async () => {
    const saved = [], {audit} = harness(async (session,batch)=>saved.push(...batch));
    audit.start('s1',snapshot('Original'));
    audit.capture(snapshot('Corrected'),'change'); audit.capture(snapshot('Original'),'undo');
    assert.equal(audit.pending,2); await audit.flush();
    assert.equal(saved.length,2); assert.equal(saved[0].patches[0].before,'Original');
    assert.equal(saved[0].patches[0].after,'Corrected'); assert.equal(saved[1].patches[0].after,'Original');
    assert.equal(audit.pending,0);
});
test('navigation and repeated snapshots do not create editing actions', () => {
    const {audit} = harness(); audit.start('s1',snapshot('A'));
    audit.capture({...snapshot('A'),activeDiagramId:'d2',inventory:[{id:'derived'}]},'pointerup');
    audit.capture(snapshot('A'),'periodic-check'); assert.equal(audit.pending,0);
});
test('failed delivery is retried with same event IDs and recovered after reload', async () => {
    const h = harness(async()=>{throw Error('offline');}); h.audit.start('s1',snapshot('A'));h.audit.capture(snapshot('B'),'change');
    await assert.rejects(h.audit.flush(),/offline/); assert.equal(h.audit.pending,1);
    const saved=[];const recovered=create({...h.options,send:async(s,b)=>saved.push(...b)});
    recovered.start('s1',snapshot('B'));await recovered.flush();assert.equal(saved[0].eventId,'1');assert.equal(recovered.pending,0);
});
test('reset on resume excludes paused edits from automatic event count', () => {
    const {audit}=harness();audit.start('s1',snapshot('A'));audit.reset(snapshot('Paused edit'));
    audit.capture(snapshot('Paused edit'),'resume');assert.equal(audit.pending,0);
    audit.capture(snapshot('New edit'),'change');assert.equal(audit.pending,1);
});
test('records graph layout edits even with unchanged semantic data', async () => {
    let saved;const {audit}=harness(async(s,b)=>saved=b);audit.start('s1',snapshot('A'));
    const moved=snapshot('A');moved.useCaseDiagrams[0].graph='<diagram x="20"/>';
    audit.capture(moved,'pointerup');await audit.flush();assert.match(saved[0].patches[0].path,/graph/);
});
test('pending events cannot be silently attributed to a different session',()=>{
    const {audit}=harness();audit.start('s1',snapshot('A'));audit.capture(snapshot('B'),'change');
    assert.throws(()=>audit.start('s2',snapshot('C')),/pending/);assert.equal(audit.session,'s1');
});
