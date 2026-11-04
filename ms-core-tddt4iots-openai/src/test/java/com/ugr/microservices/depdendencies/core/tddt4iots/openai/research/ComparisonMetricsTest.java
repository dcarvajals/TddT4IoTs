package com.ugr.microservices.depdendencies.core.tddt4iots.openai.research;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ComparisonMetricsTest {
 private final ObjectMapper j=new ObjectMapper();
 private ArrayNode items(String... ids){ArrayNode a=j.createArrayNode();for(String id:ids)a.addObject().put("id",id).put("kind","class").put("label",id);return a;}
 @Test void correspondenceCalculatesCountsAndUndefinedDenominatorsWithoutInflation() throws Exception {
   var pairs=j.readTree("[{\"generatedId\":\"g1\",\"referenceId\":\"r1\"}]");
   var result=ComparisonMetrics.compare(items("g1","g2"),items("r1","r2","r3"),pairs).path("micro");
   assertEquals(1,result.path("tp").asInt());assertEquals(1,result.path("fp").asInt());assertEquals(2,result.path("fn").asInt());
   assertEquals(.5,result.path("precision").asDouble());assertEquals(1.0/3,result.path("recall").asDouble());assertEquals(.4,result.path("f1").asDouble());
   var empty=ComparisonMetrics.compare(items(),items(),j.createArrayNode()).path("micro");assertTrue(empty.path("precision").isNull());assertTrue(empty.path("recall").isNull());assertTrue(empty.path("f1").isNull());
   var missed=ComparisonMetrics.compare(items(),items("r1"),j.createArrayNode()).path("micro");assertEquals(0,missed.path("f1").asDouble());assertTrue(missed.path("precision").isNull());
 }
 @Test void matchesMustBeOneToOneAndOfTheSameKind() throws Exception {
   var pairs=j.readTree("[{\"generatedId\":\"g1\",\"referenceId\":\"r1\"},{\"generatedId\":\"g2\",\"referenceId\":\"r1\"}]");
   assertThrows(IllegalArgumentException.class,()->ComparisonMetrics.compare(items("g1","g2"),items("r1"),pairs));
   ArrayNode refs=items("r1");((ObjectNode)refs.get(0)).put("kind","actor");
   assertThrows(IllegalArgumentException.class,()->ComparisonMetrics.compare(items("g1"),refs,j.readTree("[{\"generatedId\":\"g1\",\"referenceId\":\"r1\"}]")));
 }
 @Test void costUsesCachedInputAndIncludesReasoningOnlyOnce() throws Exception {
   var usage=j.readTree("{\"prompt_tokens\":1000,\"completion_tokens\":200,\"prompt_tokens_details\":{\"cached_tokens\":400,\"cache_write_tokens\":100},\"completion_tokens_details\":{\"reasoning_tokens\":150}}");
   var price=j.readTree("{\"inputPerMillion\":10,\"cachedPerMillion\":1,\"cacheWritePerMillion\":12.5,\"outputPerMillion\":50}");
   assertEquals(0,new BigDecimal("0.01665").compareTo(ComparisonMetrics.cost(usage,price).path("amountUsd").decimalValue()));
   assertEquals("USAGE_UNAVAILABLE",ComparisonMetrics.cost(j.nullNode(),price).path("status").asText());
   ((ObjectNode)usage.path("prompt_tokens_details")).put("cached_tokens",1001);
   assertThrows(IllegalArgumentException.class,()->ComparisonMetrics.cost(usage,price));
 }
 @Test void structuralCountsAreFromActualImportedInventoryAndLexicalHintsRemainHints() throws Exception {
   var raw=j.readTree("{\"diagrams\":[{\"id\":\"d\",\"actors\":[{\"id\":\"a\",\"name\":\"Caregiver\"}],\"relations\":[],\"useCases\":[{\"id\":\"u\",\"name\":\"Alert\",\"mainFlow\":[{\"actor\":\"a\"}],\"sequence\":{\"participants\":[{\"id\":\"p\",\"name\":\"Sensor\"}],\"messages\":[{\"from\":\"p\",\"to\":\"missing\",\"message\":\"read()\"}]}}]}]}");
   var generated=ComparisonMetrics.analyze(raw,null);assertFalse(generated.path("classMetricsAvailable").asBoolean());
   assertTrue(generated.path("coherence").toString().contains("NOT_ASSESSED"));assertTrue(generated.path("coherence").toString().contains("FAIL"));
   ObjectNode snapshot=j.createObjectNode();snapshot.set("inventory",items("c1","c2"));
   var imported=ComparisonMetrics.analyze(raw,snapshot);assertEquals(2,imported.path("counts").path("class").asInt());assertEquals("INITIAL_IMPORTED_UML",imported.path("artifactStage").asText());
 }
 @Test void mixedCacheWriteLifetimesRequireTheirOwnRates() throws Exception {
   var usage=j.readTree("{\"prompt_tokens\":1000,\"completion_tokens\":0,\"prompt_tokens_details\":{\"cache_write_tokens\":1000,\"cache_write_tokens_details\":{\"ttl_30m_tokens\":400,\"ttl_12h_tokens\":600}}}");
   var tariff=j.readTree("{\"inputPerMillion\":10,\"cachedPerMillion\":1,\"cacheWritePerMillion\":12.5,\"outputPerMillion\":50}");
   assertThrows(IllegalArgumentException.class,()->ComparisonMetrics.cost(usage,tariff));
   ((ObjectNode)tariff).put("cacheWrite12hPerMillion",25);
   assertEquals(0,new BigDecimal(".02").compareTo(ComparisonMetrics.cost(usage,tariff).path("amountUsd").decimalValue()));
 }
 @Test void differenceIsNetArtifactChangeAndDoesNotClaimEditingActionCounts(){
   ArrayNode before=items("a","b"),after=items("a","c");((ObjectNode)after.get(0)).put("label","renamed");
   var delta=ComparisonMetrics.changes(before,after);assertEquals(1,delta.path("added").asInt());assertEquals(1,delta.path("removed").asInt());assertEquals(1,delta.path("modified").asInt());
 }
}
