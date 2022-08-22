package edu.kit.datamanager.ro_crate_rest.dto;

import jakarta.validation.constraints.NotBlank;

public class PersonEntityDto extends BaseDto {

  @NotBlank(message = "name is mandatory")
  public String name;

}
