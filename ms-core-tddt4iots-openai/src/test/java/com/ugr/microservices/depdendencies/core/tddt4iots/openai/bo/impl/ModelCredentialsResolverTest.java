package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelUseToolDto;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.PersonDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.dto.TrainingHistoryDTO;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Person;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.TrainingHistory;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.PersonService;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.TrainingHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelCredentialsResolverTest {
    private final TrainingHistoryService histories = mock(TrainingHistoryService.class);
    private final PersonService people = mock(PersonService.class);
    private final com.ugr.microservices.dependencies.scheme.tddt4iots.repository.ModelRepository models = mock(com.ugr.microservices.dependencies.scheme.tddt4iots.repository.ModelRepository.class);
    private final ModelCredentialsResolver resolver = new ModelCredentialsResolver(histories, people, models);
    private ModelUseToolDto selection;
    private Person owner;

    @BeforeEach
    void setUp() throws Exception {
        selection = ModelUseToolDto.builder()
                .person(PersonDTO.builder().id(20L).openaiSecretKey("caller-test-key").build())
                .modelTraining(TrainingHistoryDTO.builder().id(7L)
                        .idPerson(PersonDTO.builder().id(99L).openaiSecretKey("stale-test-key").build()).build())
                .build();
        owner = new Person();
        owner.setId(10L);
        owner.setOpenaiSecretKey("owner-test-key");
        TrainingHistory registration = new TrainingHistory();
        registration.setIdPerson(owner);
        when(histories.findId(7L)).thenReturn(registration);
        when(people.findById(10L)).thenReturn(owner);
    }

    @Test
    void usesPersistedOwnerInsteadOfCallerOrNestedDtoCredentials() throws Exception {
        assertEquals("owner-test-key", resolver.resolve(selection));
        verify(people).findById(10L);
        verify(people, never()).findById(20L);
        verify(people, never()).findById(99L);
    }

    @Test
    void callerDoesNotNeedAPersonalKey() throws Exception {
        selection.getPerson().setOpenaiSecretKey(null);
        assertEquals("owner-test-key", resolver.resolve(selection));
    }

    @Test
    void usesUpdatedOwnerCredentialOnNextRequest() throws Exception {
        assertEquals("owner-test-key", resolver.resolve(selection));
        owner.setOpenaiSecretKey("rotated-test-key");
        assertEquals("rotated-test-key", resolver.resolve(selection));
    }

    @Test
    void missingOwnerKeyFailsWithoutFallingBackToCaller() {
        owner.setOpenaiSecretKey("  ");
        assertThrows(GenericException.class, () -> resolver.resolve(selection));
    }

    @Test
    void missingSelectionFailsBeforeLookingUpCredentials() {
        assertThrows(GenericException.class, () -> resolver.resolve(null));
        selection.setModelTraining(null);
        assertThrows(GenericException.class, () -> resolver.resolve(selection));
        verifyNoInteractions(people);
    }

    @Test
    void missingRegistrationOwnerFails() throws Exception {
        when(histories.findId(7L)).thenReturn(new TrainingHistory());
        assertThrows(GenericException.class, () -> resolver.resolve(selection));
        verifyNoInteractions(people);
    }

    @Test
    void directlyRegisteredBaseModelUsesItsPersistedOwnersKeyWithoutTraining() throws Exception {
        selection.setModelTraining(null);
        selection.setModel(com.ugr.microservices.dependencies.core.tddt4iots.dto.ModelDTO.builder().id(3L).build());
        com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Model base = new com.ugr.microservices.dependencies.scheme.tddt4iots.entity.Model();
        base.setId(3L); base.setActive(true); base.setIdPerson(owner);
        when(models.findById(3L)).thenReturn(java.util.Optional.of(base));
        assertEquals("owner-test-key", resolver.resolve(selection));
        verifyNoInteractions(histories);
    }

    @Test
    void nestedOwnerKeyIsNotReturnedInJsonButKeyCanStillBeSubmitted() throws Exception {
        ObjectMapper json = new ObjectMapper();
        JsonNode response = json.readTree(json.writeValueAsString(selection));
        assertFalse(response.path("person").has("openaiSecretKey"));
        assertFalse(response.path("modelTraining").path("idPerson").has("openaiSecretKey"));
        PersonDTO submitted = json.readValue("{\"openaiSecretKey\":\"submitted-test-key\"}", PersonDTO.class);
        assertEquals("submitted-test-key", submitted.getOpenaiSecretKey());
    }
}
