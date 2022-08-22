package edu.kit.datamanager.ro_crate_rest.dto;

import jakarta.validation.constraints.NotBlank;

public class PlaceEntityDto extends BaseDto {

  @NotBlank(message = "Name is mandatory")
  public String name;

  @NotBlank(message = "geo is mandatory")
  public String geo;

}