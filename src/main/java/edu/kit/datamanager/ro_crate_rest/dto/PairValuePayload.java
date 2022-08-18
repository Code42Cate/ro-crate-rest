package edu.kit.datamanager.ro_crate_rest.dto;

import jakarta.validation.constraints.NotBlank;

public class PairValuePayload {

  @NotBlank(message = "Value is mandatory")
  public String value;
}