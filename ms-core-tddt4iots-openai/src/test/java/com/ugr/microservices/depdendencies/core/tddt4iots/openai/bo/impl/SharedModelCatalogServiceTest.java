package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.*;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.TrainingHistoryRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SharedModelCatalogServiceTest {
    private final TrainingHistoryRepository histories = mock(TrainingHistoryRepository.class);
    private final TrainingHistoryService historyService = mock(TrainingHistoryService.class);
    private final ModelUseToolService selections = mock(ModelUseToolService.class);
    private final PersonService people = mock(PersonService.class);
    private final com.ugr.microservices.dependencies.scheme.tddt4iots.repository.ModelRepository models = mock(com.ugr.microservices.dependencies.scheme.tddt4iots.repository.ModelRepository.class);
    private final SharedModelCatalogService service = new SharedModelCatalogService(histories, historyService, selections, people, models);

    private TrainingHistory history(long id, long ownerId) {
        Person owner = new Person(); owner.setId(ownerId); owner.setNamePerson("Owner " + ownerId);
        owner.setOpenaiSecretKey("private-test-owner-key"); owner.setPasswordPerson("private-test-password");
        Model model = new Model(); model.setId(3L); model.setName("Base model"); model.setActive(true);
        ModelPermission permission = new ModelPermission(); permission.setId(5L); permission.setModel(model); permission.setPerson(owner); permission.setActive(true);
        TrainingHistory h = new TrainingHistory(); h.setId(id); h.setIdPerson(owner); h.setModel(permission);
        h.setStatus('S'); h.setProviderStatus("succeeded"); h.setCommit("version-" + id);
        h.setResultTrining("{\"code\":0,\"data\":{\"model\":\"base\",\"fine_tuned_model\":\"exact-model-" + id + "\"}}");
        return h;
    }

    @Test
    void catalogSeparatesOwnershipAndShowsExactSelectedVersionWithoutSecrets() throws Exception {
        TrainingHistory mine = history(7, 1), shared = history(8, 2);
        ModelUseTool selection = new ModelUseTool(); selection.setActive(true); selection.setModelTraining(shared);
        when(histories.findCatalogRegistrations()).thenReturn(List.of(mine, shared));
        when(selections.getAllModelUseTool(1L)).thenReturn(List.of(selection));
        var items = service.list(1L);
        assertTrue(items.get(0).mine()); assertFalse(items.get(1).mine());
        assertTrue(items.get(1).selected()); assertEquals("exact-model-8", items.get(1).providerModel());
        String json = new ObjectMapper().writeValueAsString(items);
        assertFalse(json.contains("private-test-owner-key")); assertFalse(json.contains("private-test-password"));
        assertFalse(json.contains("resultTrining"));
    }

    @Test
    void selectingAnotherOwnersExactRegistrationDoesNotRequireTheCallersKey() throws Exception {
        TrainingHistory shared = history(8, 2);
        Person caller = new Person(); caller.setId(1L);
        when(historyService.findId(8L)).thenReturn(shared);
        when(people.findById(1L)).thenReturn(caller);
        when(selections.getAllModelUseTool(1L)).thenReturn(List.of());
        var result = service.select(1L, 8L);
        assertTrue(result.selected()); assertFalse(result.mine());
        ArgumentCaptor<ModelUseTool> saved = ArgumentCaptor.forClass(ModelUseTool.class);
        verify(selections).save(saved.capture());
        assertSame(caller, saved.getValue().getPerson()); assertSame(shared, saved.getValue().getModelTraining());
        assertNull(saved.getValue().getPerson().getOpenaiSecretKey());
    }

    @Test
    void unavailableOwnerCredentialAndPendingTrainingCannotBeSelected() throws Exception {
        TrainingHistory shared = history(8, 2);
        when(historyService.findId(8L)).thenReturn(shared);
        shared.getIdPerson().setOpenaiSecretKey(null);
        assertThrows(GenericException.class, () -> service.select(1L, 8L));
        shared.getIdPerson().setOpenaiSecretKey("test-key"); shared.setProviderStatus("running");
        assertThrows(GenericException.class, () -> service.select(1L, 8L));
        verify(selections, never()).save(any());
    }

    @Test
    void selectingAVersionReusesTheSelectionAndDeactivatesDuplicateActiveRows() throws Exception {
        TrainingHistory shared = history(8, 2);
        ModelUseTool first = new ModelUseTool(); first.setId(10L); first.setActive(true);
        ModelUseTool duplicate = new ModelUseTool(); duplicate.setId(11L); duplicate.setActive(true);
        when(historyService.findId(8L)).thenReturn(shared);
        when(selections.getAllModelUseTool(1L)).thenReturn(List.of(first, duplicate));
        when(people.findById(1L)).thenReturn(new Person());
        service.select(1L, 8L);
        assertTrue(first.getActive()); assertSame(shared, first.getModelTraining());
        assertFalse(duplicate.getActive()); verify(selections).save(duplicate);
    }

    @Test
    void registeredBaseModelsAreVisibleAndSelectableWithoutATrainingHistory() throws Exception {
        Model base = history(8, 2).getModel().getModel();
        base.setIdPerson(history(8, 2).getIdPerson()); base.setName("gpt-base-test");
        when(models.getAllModel()).thenReturn(List.of(base));
        when(histories.findCatalogRegistrations()).thenReturn(List.of());
        when(selections.getAllModelUseTool(1L)).thenReturn(List.of());
        when(models.findById(3L)).thenReturn(java.util.Optional.of(base));
        when(people.findById(1L)).thenReturn(new Person());
        assertEquals("BASE_MODEL", service.list(1L).getFirst().registrationKind());
        assertEquals("gpt-base-test", service.select(1L, 3L, "BASE_MODEL").providerModel());
        ArgumentCaptor<ModelUseTool> saved = ArgumentCaptor.forClass(ModelUseTool.class);
        verify(selections).save(saved.capture());
        assertNull(saved.getValue().getModelTraining()); assertSame(base, saved.getValue().getModel());
        verifyNoInteractions(historyService);
    }
    @Test
    void explicitProjectChoiceUsesCanonicalRegistrationWithoutChangingGlobalSelection() throws Exception {
        TrainingHistory shared = history(8, 2);
        when(historyService.findId(8L)).thenReturn(shared);
        ModelUseTool selection = service.resolveForRequest(1L, 8L, "TRAINED_MODEL");
        assertSame(shared, selection.getModelTraining());
        assertEquals(2L, selection.getModelTraining().getIdPerson().getId());
        verifyNoInteractions(selections);
        shared.setProviderStatus("running");
        assertThrows(GenericException.class, () -> service.resolveForRequest(1L, 8L, "TRAINED_MODEL"));
        verifyNoInteractions(selections);
    }

}
