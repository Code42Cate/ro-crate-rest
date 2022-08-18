package edu.kit.datamanager.ro_crate_rest.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;

public class PersonEntityPayload {

  @NotBlank(message = "name is mandatory")
  public String name;

  public Map<String, JsonNode> properties = new HashMap<String, JsonNode>();

  @JsonAnySetter
  public void add(String key, JsonNode value) {
    properties.put(key, value);
  }
}
