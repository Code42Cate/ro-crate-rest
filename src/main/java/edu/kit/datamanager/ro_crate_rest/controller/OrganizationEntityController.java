package edu.kit.datamanager.ro_crate_rest.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.OrganizationEntity;
import edu.kit.datamanager.ro_crate_rest.dto.OrganizationEntityDto;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/crates/{crateId}/entities/contextual/organizations/{organizationId}")
@Validated
public class OrganizationEntityController {

  @PutMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Validated
  public void addOrganizationEntity(
      @PathVariable String crateId, @PathVariable String organizationId,
      @RequestBody @Validated OrganizationEntityDto payload,
      @RequestAttribute RoCrate crate) {

    String decodedOrganizationId = URLDecoder.decode(organizationId, StandardCharsets.UTF_8);

    if (crate.getContextualEntityById(decodedOrganizationId) != null) {
      crate.deleteEntityById(decodedOrganizationId);
    }

    OrganizationEntity entity = new OrganizationEntity.OrganizationEntityBuilder()
        .setId(decodedOrganizationId)
        .addProperty("name", payload.name)
        .addProperty("url", payload.url)
        .build();

    for (Map.Entry<String, JsonNode> entry : payload.properties.entrySet()) {
      entity.addProperty(entry.getKey(), entry.getValue());
    }

    crate = new RoCrate.RoCrateBuilder(crate).addContextualEntity(entity).build();
  }

  @DeleteMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deleteOrganizationEntity(
      @PathVariable String crateId, @PathVariable String organizationId,
      @RequestAttribute RoCrate crate) {

    crate.deleteEntityById(URLDecoder.decode(organizationId, StandardCharsets.UTF_8));

  }

  @GetMapping()
  @ResponseStatus(code = HttpStatus.OK)
  public ObjectNode getOrganizationEntity(
      @PathVariable String crateId, @PathVariable String organizationId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(organizationId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }

    return entity.getProperties();
  }

  @PutMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToOrganizationEntity(
      @PathVariable String crateId, @PathVariable String organizationId, @PathVariable String property,
      @RequestBody JsonNode body,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(organizationId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.addProperty(property, body);

  }

  @DeleteMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePropertyFromOrganizationEntity(
      @PathVariable String crateId, @PathVariable String organizationId, @PathVariable String property,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(organizationId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.getProperties().remove(property);
  }

}
