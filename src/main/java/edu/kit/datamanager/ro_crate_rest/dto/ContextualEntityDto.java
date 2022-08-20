package edu.kit.datamanager.ro_crate_rest.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotEmpty;

public class ContextualEntityDto {
  public Map<String, JsonNode> properties = new HashMap<String, JsonNode>();

  @JsonProperty("@type")
  @NotEmpty
  public Set<String> type = new HashSet<>();

  @JsonProperty("@type")
  public void setType(JsonNode value) {
    if (value.isArray()) {
      for (JsonNode node : value) {
        type.add(node.asText());
      }
    } else if (value.isTextual()) {
      type.add(value.asText());
    }
  }

  @JsonAnySetter
  public void set(String key, JsonNode value) {
    properties.put(key, value);
  }

}
