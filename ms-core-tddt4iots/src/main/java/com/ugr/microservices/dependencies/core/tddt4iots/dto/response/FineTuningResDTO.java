package com.ugr.microservices.dependencies.core.tddt4iots.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FineTuningResDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("error")
    private Error error;

    @JsonProperty("fine_tuned_model")
    private String fineTunedModel;

    @JsonProperty("finished_at")
    private Long finishedAt;

    @JsonProperty("model")
    private String model;

    @JsonProperty("object")
    private String object;

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("result_files")
    private List<String> resultFiles;

    @JsonProperty("seed")
    private Long seed;

    @JsonProperty("status")
    private String status;

    @JsonProperty("trained_tokens")
    private Long trainedTokens;

    @JsonProperty("training_file")
    private String trainingFile;

    @JsonProperty("validation_file")
    private String validationFile;

    @JsonProperty("estimated_finish")
    private Long estimatedFinish;

    @JsonProperty("integrations")
    private List<Object> integrations;

    @JsonProperty("user_provided_suffix")
    private String userProvidedSuffix;

    @JsonProperty("hyperparameters")
    private Hyperparameters hyperparameters;

    @Data
    public static class Error {
        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;

        @JsonProperty("param")
        private String param;
    }

    @Data
    public static class Hyperparameters {
        @JsonProperty("n_epochs")
        private Object nEpochs;

        @JsonProperty("batch_size")
        private Object batchSize;

        @JsonProperty("learning_rate_multiplier")
        private Object learningRateMultiplier;
    }
}
