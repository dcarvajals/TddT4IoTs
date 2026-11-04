package com.ugr.microservices.depdendencies.core.tddt4iots.openai.bo.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugr.microservices.dependencies.core.tddt4iots.util.GenericException;
import com.ugr.microservices.dependencies.scheme.tddt4iots.entity.*;
import com.ugr.microservices.dependencies.scheme.tddt4iots.repository.TrainingHistoryRepository;
import com.ugr.microservices.dependencies.scheme.tddt4iots.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SharedModelCatalogService {
    private final TrainingHistoryRepository histories;
    private final TrainingHistoryService historyService;
    private final ModelUseToolService selections;
    private final PersonService people;
    private final com.ugr.microservices.dependencies.scheme.tddt4iots.repository.ModelRepository models;
    private static final ObjectMapper JSON = new ObjectMapper();

    /** Explicit public projection: no nested Person or provider payload is serialized. */
    public record Entry(Long id, String registrationKind, String modelName, String providerModel, Long ownerId, String ownerName,
                        boolean mine, boolean selected, boolean available, String unavailableReason,
                        String version, Date createdAt) {}

    @Transactional(readOnly = true)
    public List<Entry> list(Long userId) throws GenericException {
        ModelUseTool current = selections.getAllModelUseTool(userId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive())).findFirst().orElse(null);
        Long selectedId = current == null || current.getModelTraining() == null ? null : current.getModelTraining().getId();
        List<Entry> result = new ArrayList<>();
        for (Model model : models.getAllModel()) result.add(baseEntry(model, userId,
                current != null && current.getModelTraining() == null && current.getModel() != null && Objects.equals(current.getModel().getId(), model.getId())));
        result.addAll(histories.findCatalogRegistrations().stream().map(h -> entry(h, userId, selectedId)).toList());
        return result;
    }

    @Transactional
    public Entry select(Long userId, Long registrationId) throws GenericException {
        return select(userId, registrationId, "TRAINED_MODEL");
    }

    @Transactional
    public Entry select(Long userId, Long registrationId, String kind) throws GenericException {
        if (registrationId == null) throw new GenericException("Seleccione un modelo registrado.");
        if (!Set.of("BASE_MODEL", "TRAINED_MODEL").contains(kind)) throw new GenericException("Tipo de registro de modelo no válido.");
        TrainingHistory history = kind.equals("TRAINED_MODEL") ? historyService.findId(registrationId) : null;
        Model base = kind.equals("BASE_MODEL") ? models.findById(registrationId).orElse(null) : null;
        if (history == null && base == null) throw new GenericException("El registro de modelo no existe.");
        Entry item = history == null ? baseEntry(base, userId, false) : entry(history, userId, null);
        if (!item.available()) throw new GenericException(item.unavailableReason());
        List<ModelUseTool> existing = selections.getAllModelUseTool(userId);
        ModelUseTool selection = existing.isEmpty() ? new ModelUseTool() : existing.getFirst();
        // The authenticated user owns the selection; the registered owner still supplies the credentials.
        selection.setPerson(people.findById(userId));
        selection.setModelTraining(history);
        selection.setModel(history == null ? base : history.getModel().getModel());
        selection.setActive(true);
        selections.save(selection);
        for (int i = 1; i < existing.size(); i++) {
            existing.get(i).setActive(false);
            selections.save(existing.get(i));
        }
        return history == null ? baseEntry(base, userId, true) : entry(history, userId, registrationId);
    }

    private Entry baseEntry(Model model, Long userId, boolean selected) {
        Person owner = model.getIdPerson();
        String reason = !Boolean.TRUE.equals(model.getActive()) ? "Este modelo está inactivo." :
                owner == null || owner.getOpenaiSecretKey() == null || owner.getOpenaiSecretKey().isBlank()
                        ? "El propietario debe configurar sus credenciales de OpenAI." :
                model.getName() == null || model.getName().isBlank() ? "Falta el identificador API del modelo." : "";
        Long ownerId = owner == null ? null : owner.getId();
        String name = owner == null ? "Unknown owner" : (Objects.toString(owner.getNamePerson(), "") + " " + Objects.toString(owner.getLastnamePerson(), "")).trim();
        return new Entry(model.getId(), "BASE_MODEL", model.getName(), model.getName(), ownerId,
                name.isBlank() ? "User #" + ownerId : name, Objects.equals(userId, ownerId), selected,
                reason.isBlank(), reason, "Registered base model", model.getCreateAt());
    }

    /** Resolve an explicit project choice without modifying the user's global selection. */
    @Transactional(readOnly = true)
    public ModelUseTool resolveForRequest(Long userId, Long registrationId, String kind) throws GenericException {
        if (registrationId == null || !Set.of("BASE_MODEL", "TRAINED_MODEL").contains(Objects.toString(kind, "")))
            throw new GenericException("Seleccione un registro de modelo válido para el proyecto.");
        TrainingHistory history = "TRAINED_MODEL".equals(kind) ? historyService.findId(registrationId) : null;
        Model base = "BASE_MODEL".equals(kind) ? models.findById(registrationId).orElse(null) : null;
        if (history == null && base == null) throw new GenericException("El registro de modelo no existe.");
        Entry item = history == null ? baseEntry(base, userId, false) : entry(history, userId, null);
        if (!item.available()) throw new GenericException(item.unavailableReason());
        ModelUseTool selection = new ModelUseTool();
        selection.setPerson(people.findById(userId));
        selection.setModelTraining(history);
        selection.setModel(history == null ? base : history.getModel().getModel());
        selection.setActive(true);
        return selection;
    }

    private Entry entry(TrainingHistory h, Long userId, Long selectedId) {
        Person owner = h.getIdPerson();
        ModelPermission permission = h.getModel();
        Model model = permission == null ? null : permission.getModel();
        String providerModel = "";
        String providerStatus = Objects.toString(h.getProviderStatus(), "");
        String reason = "";
        try {
            JsonNode result = JSON.readTree(Objects.toString(h.getResultTrining(), "{}"));
            JsonNode data = result.path("data");
            providerModel = data.path("fine_tuned_model").asText("");
            if (providerModel.isBlank()) providerModel = data.path("model").asText("");
            if (providerStatus.isBlank()) providerStatus = data.path("status").asText("");
            if (result.path("code").asInt(0) != 0) reason = "El registro contiene un error del proveedor.";
        } catch (Exception e) { reason = "El registro del proveedor no es válido."; }
        if (model == null || !Boolean.TRUE.equals(model.getActive()) || !Boolean.TRUE.equals(permission.getActive()) || Character.valueOf('E').equals(h.getStatus()))
            reason = "Este registro de modelo está inactivo o falló.";
        else if (!providerStatus.isBlank() && !providerStatus.equalsIgnoreCase("succeeded"))
            reason = "El registro aún no está disponible: " + providerStatus;
        else if (providerModel.isBlank()) reason = "El registro todavía no tiene un identificador de modelo utilizable.";
        else if (owner == null || owner.getOpenaiSecretKey() == null || owner.getOpenaiSecretKey().isBlank())
            reason = "El propietario debe configurar sus credenciales de OpenAI.";
        Long ownerId = owner == null ? null : owner.getId();
        String ownerName = owner == null ? "Unknown owner" : (Objects.toString(owner.getNamePerson(), "") + " " + Objects.toString(owner.getLastnamePerson(), "")).trim();
        if (ownerName.isBlank()) ownerName = "User #" + ownerId;
        return new Entry(h.getId(), "TRAINED_MODEL", model == null ? "Unknown model" : model.getName(), providerModel,
                ownerId, ownerName, Objects.equals(ownerId, userId), Objects.equals(h.getId(), selectedId),
                reason.isBlank(), reason, h.getCommit(), h.getDateCreation());
    }
}
