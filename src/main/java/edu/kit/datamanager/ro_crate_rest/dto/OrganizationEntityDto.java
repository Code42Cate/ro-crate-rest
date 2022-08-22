package edu.kit.datamanager.ro_crate_rest.dto;

import jakarta.validation.constraints.NotBlank;

public class OrganizationEntityDto extends BaseDto {

  @NotBlank(message = "Name is mandatory")
  public String name;

  @NotBlank(message = "Url is mandatory")
  public String url;

}
