package org.mule.extension.kafka.api.params;

import java.util.List;

import org.mule.extension.kafka.api.source.TopicPartition;
import org.mule.sdk.api.annotation.Expression;
import org.mule.sdk.api.annotation.param.ExclusiveOptionals;
import org.mule.sdk.api.annotation.param.NullSafe;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.DisplayName;
import org.mule.sdk.api.annotation.param.display.Example;
import org.mule.sdk.api.annotation.param.display.Summary;
import org.mule.sdk.api.meta.ExpressionSupport;

@ExclusiveOptionals(isOneRequired = true)
public class SubscriptionParamGroup {
  @NullSafe
  @Parameter
  @Summary("Uma lista de tópicos para os quais o consumidor deve se inscrever.")
  @Example("[\"topic1\",\".*topicX.*]]")
  @DisplayName("Topic Subscription Patterns")
  @Optional
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private List<String> topicPatterns;

  @NullSafe
  @Parameter
  @Summary("Uma lista de pares de tópicos-partições para atribuir. Note que não haverá reequilíbrio automático dos consumidores.")
  @Example("[\"topic1\",\".*topicX.*]]")
  @DisplayName("Assignments")
  @Optional
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private List<TopicPartition> assignments;

  public List<String> getTopicPatterns() {
    return this.topicPatterns;
  }

  public List<TopicPartition> getAssignments() {
    return this.assignments;
  }

  public void setTopicPatterns(List<String> topicPatterns) {
    this.topicPatterns = topicPatterns;
  }

  public void setAssignments(List<TopicPartition> assignments) {
    this.assignments = assignments;
  }
}
