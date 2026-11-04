package com.ugr.microservices.depdendencies.core.tddt4iots.openai.research;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.math.*;
import java.text.Normalizer;
import java.util.*;

/** Deterministic measurements; lexical checks are explicitly not semantic judgments. */
public final class ComparisonMetrics {
    private static final ObjectMapper J = new ObjectMapper();
    public static final String VERSION = "comparison-metrics-v1";
    public static final Set<String> KINDS = Set.of("actor","use_case","uc_relation","class","class_relation","attribute","operation","participant","message","response");
    private ComparisonMetrics() {}
    static String norm(String s) { return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ""); }
    public static ArrayNode validateInventory(JsonNode items) {
        if (!items.isArray() || items.size()>20000) throw new IllegalArgumentException("inventory debe ser un array de hasta 20000 elementos.");
        Set<String> ids=new HashSet<>();
        for(JsonNode e:items) {
            if (!e.isObject() || e.path("id").asText().isBlank() || !ids.add(e.path("id").asText())
                    || !KINDS.contains(e.path("kind").asText()) || e.path("label").asText().isBlank())
                throw new IllegalArgumentException("Cada elemento requiere id único, kind válido y label no vacío.");
        }
        return (ArrayNode)items.deepCopy();
    }
    private static void add(ArrayNode a,String id,String kind,String label,String owner,String from,String to) {
        ObjectNode e=a.addObject().put("id",id).put("kind",kind).put("label",label).put("owner",owner);
        if(from!=null)e.put("from",from); if(to!=null)e.put("to",to);
    }
    public static ArrayNode inventory(JsonNode raw,JsonNode snapshot) {
        if(snapshot!=null && snapshot.has("inventory")) return validateInventory(snapshot.path("inventory"));
        ArrayNode items=J.createArrayNode();
        if(raw!=null)for(JsonNode d:raw.path("diagrams")) {
            String scope=d.path("id").asText();
            for(JsonNode a:d.path("actors"))add(items,"actor:"+scope+":"+a.path("id").asText(),"actor",a.path("name").asText(),scope,null,null);
            int r=0; for(JsonNode rel:d.path("relations"))add(items,"uc_relation:"+scope+":"+(r++),"uc_relation",rel.path("type").asText(),scope,rel.path("from").asText(),rel.path("to").asText());
            for(JsonNode u:d.path("useCases")) {
                String uid=u.path("id").asText();add(items,"use_case:"+scope+":"+uid,"use_case",u.path("name").asText(),scope,null,null);
                Map<String,String> names=new HashMap<>();
                for(JsonNode p:u.path("sequence").path("participants")) {String name=p.path("name").asText();names.put(p.path("id").asText(),name);add(items,"participant:"+uid+":"+name,"participant",name,uid,null,null);}
                int i=0;for(JsonNode m:u.path("sequence").path("messages")) {
                    String mid="message:"+uid+":"+(i++),from=names.getOrDefault(m.path("from").asText(),""),to=names.getOrDefault(m.path("to").asText(),"");
                    add(items,mid,"message",m.path("message").asText(),uid,from,to);
                    if(!m.path("response").asText("").isBlank())add(items,mid+":response","response",m.path("response").asText(),uid,to,from);
                }
            }
        }
        // Native consolidated class diagram, including existing snapshots made before migration 006.
        if(snapshot!=null)for(JsonNode p:snapshot.path("classModel").path("diagram"))for(JsonNode c:p.path("class")) {
            String name=c.path("className").asText();String owner=p.path("packName").asText()+":"+name;
            add(items,"class:"+owner,"class",name,p.path("packName").asText(),null,null);
            int i=0;for(JsonNode a:c.path("attributes"))add(items,"attribute:"+owner+":"+(i++),"attribute",a.path("name").asText(),name,null,null);
            i=0;for(JsonNode m:c.path("methods"))add(items,"operation:"+owner+":"+(i++),"operation",m.path("name").asText(),name,null,null);
        }
        if(snapshot!=null){int i=0;for(JsonNode r:snapshot.path("classModel").path("relationships"))add(items,"class_relation:"+(i++),"class_relation",r.path("type").asText("relation"),"",r.path("from").asText(),r.path("to").asText());}
        return items;
    }
    public static ObjectNode analyze(JsonNode raw,JsonNode snapshot) {
        ArrayNode inventory=inventory(raw,snapshot);
        ObjectNode out=J.createObjectNode().put("evaluatorVersion",VERSION).put("artifactStage",snapshot==null?"PROVIDER_WORKSPACE":"INITIAL_IMPORTED_UML");
        out.set("inventory",inventory); ObjectNode counts=out.putObject("counts");
        for(String k:new TreeSet<>(KINDS))counts.put(k,java.util.stream.StreamSupport.stream(inventory.spliterator(),false).filter(e->k.equals(e.path("kind").asText())).count());
        counts.put("message_total",counts.path("message").asLong()+counts.path("response").asLong());
        out.put("classMetricsAvailable",snapshot!=null);
        ArrayNode checks=out.putArray("coherence");
        if(raw!=null)for(JsonNode d:raw.path("diagrams")) {
            Set<String> actors=new HashSet<>(), useCases=new HashSet<>();for(JsonNode a:d.path("actors"))actors.add(a.path("id").asText());for(JsonNode u:d.path("useCases"))useCases.add(u.path("id").asText());
            for(JsonNode u:d.path("useCases")) {
                String uid=u.path("id").asText();
                check(checks,"SEQUENCE_PRESENT",uid,u.path("sequence").path("messages").isArray()&&!u.path("sequence").path("messages").isEmpty(),"structural");
                Set<String> pids=new HashSet<>();for(JsonNode p:u.path("sequence").path("participants"))pids.add(p.path("id").asText());
                for(JsonNode m:u.path("sequence").path("messages"))check(checks,"MESSAGE_ENDPOINTS",uid,pids.contains(m.path("from").asText())&&pids.contains(m.path("to").asText()),"structural");
                for(JsonNode step:u.path("mainFlow"))if(!"System".equals(step.path("actor").asText())) {
                    String actor=step.path("actor").asText();check(checks,"FLOW_ACTOR_EXISTS",uid,actors.contains(actor),"structural");
                    boolean associated=false;for(JsonNode r:d.path("relations"))if("association".equals(r.path("type").asText())&&((actor.equals(r.path("from").asText())&&uid.equals(r.path("to").asText()))||(uid.equals(r.path("from").asText())&&actor.equals(r.path("to").asText()))))associated=true;
                    check(checks,"ACTOR_USE_CASE_ASSOCIATION",uid,associated,"structural");
                }
            }
            for(JsonNode r:d.path("relations"))check(checks,"USE_CASE_RELATION_ENDPOINTS",d.path("id").asText(),(actors.contains(r.path("from").asText())||useCases.contains(r.path("from").asText()))&&(actors.contains(r.path("to").asText())||useCases.contains(r.path("to").asText())),"structural");
        }
        Set<String> classes=new HashSet<>();Map<String,Set<String>> operations=new HashMap<>();
        for(JsonNode e:inventory) {if("class".equals(e.path("kind").asText()))classes.add(norm(e.path("label").asText()));if("operation".equals(e.path("kind").asText()))operations.computeIfAbsent(norm(e.path("owner").asText()),x->new HashSet<>()).add(norm(e.path("label").asText()));}
        for(JsonNode e:inventory)if("participant".equals(e.path("kind").asText())) {
            String name=e.path("label").asText(); boolean actor=false;
            for(JsonNode a:inventory)if("actor".equals(a.path("kind").asText())&&norm(a.path("label").asText()).equals(norm(name)))actor=true;
            if(!actor&&!"system".equals(norm(name))) {
                ObjectNode c=checks.addObject().put("rule","LIFELINE_CLASS_NAME").put("element",e.path("id").asText()).put("method","lexical-candidate");
                c.put("status",snapshot==null?"NOT_ASSESSED":classes.contains(norm(name))?"MATCH_CANDIDATE":"UNMAPPED");
            }
        }
        for(JsonNode e:inventory)if("message".equals(e.path("kind").asText())) {
            String receiver=norm(e.path("to").asText()), label=e.path("label").asText().replaceFirst("^\\d+\\)\\s*", "").split("\\(",2)[0];
            checks.addObject().put("rule","MESSAGE_RECEIVER_OPERATION_NAME").put("element",e.path("id").asText()).put("method","lexical-candidate")
                .put("status",snapshot==null?"NOT_ASSESSED":operations.getOrDefault(receiver,Set.of()).contains(norm(label))?"MATCH_CANDIDATE":"UNMAPPED");
        }
        out.put("semanticCoherenceRequiresExpertReview",true);return out;
    }
    private static void check(ArrayNode checks,String rule,String owner,boolean pass,String method){checks.addObject().put("rule",rule).put("element",owner).put("method",method).put("status",pass?"PASS":"FAIL");}
    public static ObjectNode compare(JsonNode generated,JsonNode reference,JsonNode pairs) {
        Map<String,JsonNode> g=index(validateInventory(generated)),r=index(validateInventory(reference));
        if(!pairs.isArray())throw new IllegalArgumentException("matches debe ser un array.");
        Set<String> usedG=new HashSet<>(),usedR=new HashSet<>();Map<String,Integer> tp=new HashMap<>();
        for(JsonNode p:pairs) {
            String a=p.path("generatedId").asText(),b=p.path("referenceId").asText();
            if(!g.containsKey(a)||!r.containsKey(b)||!usedG.add(a)||!usedR.add(b)||!g.get(a).path("kind").equals(r.get(b).path("kind")))throw new IllegalArgumentException("Las correspondencias deben ser uno a uno, existentes y del mismo tipo.");
            tp.merge(g.get(a).path("kind").asText(),1,Integer::sum);
        }
        ObjectNode result=J.createObjectNode();ArrayNode groups=result.putArray("groups");
        for(String k:new TreeSet<>(KINDS)) {int ng=(int)g.values().stream().filter(e->k.equals(e.path("kind").asText())).count(),nr=(int)r.values().stream().filter(e->k.equals(e.path("kind").asText())).count();groups.add(scores(k,tp.getOrDefault(k,0),ng-tp.getOrDefault(k,0),nr-tp.getOrDefault(k,0)));}
        result.set("micro",scores("micro",usedG.size(),g.size()-usedG.size(),r.size()-usedR.size()));
        result.put("matchingMethod","expert-confirmed-one-to-one");return result;
    }
    private static ObjectNode scores(String kind,int tp,int fp,int fn) {
        ObjectNode r=J.createObjectNode().put("kind",kind).put("tp",tp).put("fp",fp).put("fn",fn);
        r.set("precision",tp+fp==0?NullNode.instance:DoubleNode.valueOf((double)tp/(tp+fp)));
        r.set("recall",tp+fn==0?NullNode.instance:DoubleNode.valueOf((double)tp/(tp+fn)));
        r.set("f1",2*tp+fp+fn==0?NullNode.instance:DoubleNode.valueOf((double)(2*tp)/(2*tp+fp+fn)));return r;
    }
    private static Map<String,JsonNode> index(JsonNode items){Map<String,JsonNode> map=new LinkedHashMap<>();items.forEach(e->map.put(e.path("id").asText(),e));return map;}
    public static ObjectNode changes(JsonNode before,JsonNode after) {
        Map<String,JsonNode> a=index(validateInventory(before)),b=index(validateInventory(after));int added=0,removed=0,modified=0;
        for(String id:a.keySet())if(!b.containsKey(id))removed++;else if(!a.get(id).equals(b.get(id)))modified++;
        for(String id:b.keySet())if(!a.containsKey(id))added++;
        return J.createObjectNode().put("added",added).put("removed",removed).put("modified",modified).put("interpretation","net-inventory-difference-not-edit-actions");
    }
    public static ObjectNode cost(JsonNode usage,JsonNode tariff) {
        ObjectNode out=J.createObjectNode().put("currency","USD").put("kind","estimated-api-token-cost");
        if(usage==null||!usage.path("prompt_tokens").isIntegralNumber()||!usage.path("completion_tokens").isIntegralNumber())return out.put("status","USAGE_UNAVAILABLE");
        long input=usage.path("prompt_tokens").asLong(),output=usage.path("completion_tokens").asLong(),cached=usage.path("prompt_tokens_details").path("cached_tokens").asLong(0),write=usage.path("prompt_tokens_details").path("cache_write_tokens").asLong(0);
        if(input<0||output<0||cached<0||write<0||cached+write>input)throw new IllegalArgumentException("Contadores de tokens incompatibles.");
        BigDecimal rate=rate(tariff,"inputPerMillion"),cache=rate(tariff,"cachedPerMillion"),writing=rate(tariff,"cacheWritePerMillion"),completion=rate(tariff,"outputPerMillion");
        JsonNode lifetimes=usage.path("prompt_tokens_details").path("cache_write_tokens_details");
        long longWrites=lifetimes.path("ttl_12h_tokens").asLong(0);
        if(longWrites<0||longWrites>write||(lifetimes.has("ttl_30m_tokens")&&lifetimes.path("ttl_30m_tokens").asLong()+longWrites!=write))throw new IllegalArgumentException("Desglose de escrituras de caché incompatible.");
        BigDecimal longRate=longWrites>0?rate(tariff,"cacheWrite12hPerMillion"):writing;
        BigDecimal total=rate.multiply(BigDecimal.valueOf(input-cached-write)).add(cache.multiply(BigDecimal.valueOf(cached))).add(writing.multiply(BigDecimal.valueOf(write-longWrites))).add(longRate.multiply(BigDecimal.valueOf(longWrites))).add(completion.multiply(BigDecimal.valueOf(output))).divide(BigDecimal.valueOf(1000000),10,RoundingMode.HALF_UP);
        out.put("status","ESTIMATED").put("amountUsd",total).put("inputTokens",input).put("cachedTokens",cached).put("cacheWriteTokens",write).put("cacheWrite12hTokens",longWrites).put("completionTokens",output).put("reasoningIncludedInCompletion",true);return out;
    }
    private static BigDecimal rate(JsonNode t,String key){if(!t.path(key).isNumber()||t.path(key).decimalValue().signum()<0||t.path(key).decimalValue().compareTo(BigDecimal.valueOf(1000000))>0)throw new IllegalArgumentException("Tarifa inválida: "+key);return t.path(key).decimalValue();}
}
