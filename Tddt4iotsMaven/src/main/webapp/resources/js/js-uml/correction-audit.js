/* Observed workspace transitions, not keystrokes or inferred editing intentions. */
(function (root) {
    'use strict';
    const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);
    function diff(before, after, path = '') {
        if (same(before, after)) return [];
        if (before && after && typeof before === 'object' && typeof after === 'object' && Array.isArray(before) === Array.isArray(after)) {
            return [...new Set([...Object.keys(before), ...Object.keys(after)])].flatMap(key =>
                diff(before[key], after[key], path + '/' + key.replace(/~/g, '~0').replace(/\//g, '~1')));
        }
        return [{path, action: before === undefined ? 'add' : after === undefined ? 'remove' : 'modify',
            before: before === undefined ? null : before, after: after === undefined ? null : after}];
    }
    function normalize(snapshot) {
        // Tab selection and derived inventory are not editing actions.
        const copy = JSON.parse(JSON.stringify(snapshot));
        delete copy.activeDiagramId; delete copy.inventory;
        // Stable identity avoids counting an array reorder as edits to every diagram.
        ['useCaseDiagrams', 'sequences'].forEach(key => {
            copy[key] = Object.fromEntries((copy[key] || []).map(item => [item.id || item.useCaseId, item]));
        });
        return copy;
    }
    function create(options) {
        let baseline = null, pending = [], session = null, busy = false;
        const report = () => options.status({pending: pending.length, busy});
        const persist = () => {
            try { options.storage.setItem('uml-correction-audit:' + session, JSON.stringify(pending)); }
            catch (e) { options.error('Local recovery storage unavailable. Keep this page open until all changes are saved.'); }
            report();
        };
        return {
            start(id, snapshot) {
                if (session === id) return;
                if (pending.length) throw new Error('Save pending correction events before changing sessions.');
                session = id; baseline = normalize(snapshot);
                try { pending = JSON.parse(options.storage.getItem('uml-correction-audit:' + id) || '[]'); }
                catch (e) { pending = []; options.error('Local correction events could not be recovered.'); }
                report();
            },
            reset(snapshot) { baseline = normalize(snapshot); },
            capture(snapshot, trigger) {
                if (!session || !baseline) return;
                const next = normalize(snapshot), patches = diff(baseline, next);
                if (!patches.length) return;
                pending.push({eventId: options.uuid(), source: 'automatic', action: 'modify',
                    description: 'Observed UML workspace change: ' + patches.slice(0, 3).map(p => p.path).join(', ').slice(0, 1500) + (patches.length > 3 ? ' (and more)' : ''), capturedAt: new Date().toISOString(),
                    trigger, patches});
                baseline = next; persist();
            },
            async flush() {
                if (busy) throw new Error('Correction events are still being saved. Try again in a moment.');
                busy = true; report();
                try {
                    while (pending.length) {
                        const batch = pending.slice(0, 20);
                        await options.send(session, batch);
                        pending.splice(0, batch.length); persist();
                    }
                } finally { busy = false; report(); }
            },
            get pending() { return pending.length; },
            get session() { return session; }
        };
    }
    const api = {diff, normalize, create};
    if (typeof module !== 'undefined' && module.exports) module.exports = api;
    else root.UmlCorrectionAudit = api;
})(typeof window !== 'undefined' ? window : globalThis);
