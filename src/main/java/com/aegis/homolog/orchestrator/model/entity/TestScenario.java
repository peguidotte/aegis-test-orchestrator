package com.aegis.homolog.orchestrator.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "project")
@Entity
@Table(name = "T_AEGIS_TEST_SCENARIOS")
public class TestScenario extends AuditableEntity {

    @Id
    @Column(name = "SCENARIO_ID", nullable = false, length = 64)
    private String scenarioId;

    @Column(name = "FEATURE_ID", nullable = false, length = 64)
    private String featureId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private TestProject project;

    @Column(name = "TITLE", nullable = false, length = 256)
    private String title;

    @ElementCollection
    @CollectionTable(name = "T_AEGIS_SCENARIO_TAGS", joinColumns = @JoinColumn(name = "SCENARIO_ID"))
    @Column(name = "TAG_ID", length = 64)
    @Builder.Default
    private List<String> tagIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "T_AEGIS_SCENARIO_VARS", joinColumns = @JoinColumn(name = "SCENARIO_ID"))
    @Column(name = "VARIABLE_ID", length = 64)
    @Builder.Default
    private List<String> customVariableIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "T_AEGIS_SCENARIO_CALLS", joinColumns = @JoinColumn(name = "SCENARIO_ID"))
    @Column(name = "CALL_ID", length = 64)
    @Builder.Default
    private List<String> usedApiCallIds = new ArrayList<>();

    @Lob
    @Column(name = "ABSTRACT_MODEL", nullable = false)
    private String abstractModel;

    @Lob
    @Column(name = "GENERATED_GHERKIN", nullable = false)
    private String generatedGherkin;

    @Column(name = "TOTAL_EXECUTIONS", nullable = false)
    private Integer totalExecutions;

    @Column(name = "TOTAL_FAILURES", nullable = false)
    private Integer totalFailures;

    @Column(name = "FAILURE_RATE", nullable = false, precision = 6, scale = 3)
    private BigDecimal failureRate;

    @PrePersist
    void onPersist() {
        if (totalExecutions == null) {
            totalExecutions = 0;
        }
        if (totalFailures == null) {
            totalFailures = 0;
        }
        if (failureRate == null) {
            failureRate = BigDecimal.ZERO;
        }
    }
}
