package io.student.rangiffler.extension;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class ProbeTargetCondition implements ExecutionCondition {
  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    return context.getConfigurationParameter("rfr.probe.enabled").filter("true"::equals)
        .map(value -> ConditionEvaluationResult.enabled("Probe explicitly selected by EngineTestKit"))
        .orElseGet(() -> ConditionEvaluationResult.disabled("Probe runs only inside EngineTestKit"));
  }
}
