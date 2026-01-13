package com.aegis.homolog.orchestrator.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "t_aegis_test_scenarios")
public class TestScenario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "feature_id", nullable = false, length = 64)
    private String featureId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private TestProject project;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @ElementCollection
    @CollectionTable(name = "t_aegis_scenario_tags", joinColumns = @JoinColumn(name = "scenario_id"))
    @Column(name = "tag_id")
    @Builder.Default
    private List<Long> tagIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "t_aegis_scenario_vars", joinColumns = @JoinColumn(name = "scenario_id"))
    @Column(name = "variable_id", length = 64)
    @Builder.Default
    private List<String> customVariableIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "t_aegis_scenario_calls", joinColumns = @JoinColumn(name = "scenario_id"))
    @Column(name = "api_call_id")
    @Builder.Default
    private List<Long> usedApiCallIds = new ArrayList<>();

    @Lob
    @Column(name = "abstract_model", nullable = false)
    private String abstractModel;

    @Lob
    @Column(name = "generated_gherkin", nullable = false)
    private String generatedGherkin;

    @Column(name = "total_executions", nullable = false)
    private Integer totalExecutions;

    @Column(name = "total_failures", nullable = false)
    private Integer totalFailures;

    @Column(name = "failure_rate", nullable = false, precision = 6, scale = 3)
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
