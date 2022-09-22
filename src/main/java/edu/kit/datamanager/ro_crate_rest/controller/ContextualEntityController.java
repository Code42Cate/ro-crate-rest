package edu.kit.datamanager.ro_crate_rest.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate_rest.dto.ContextualEntityDto;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/crates/{crateId}/entities/contextual/")
public class ContextualEntityController {

  @GetMapping("/")
  @ResponseStatus(code = HttpStatus.OK)
  public List<ContextualEntity> getDataEntities(
      @PathVariable String crateId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    return crate.getAllContextualEntities();
  }

  @PutMapping("/{contextualId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addContextualEntity(
      @PathVariable String crateId, @PathVariable String contextualId,
      @RequestBody @Validated ContextualEntityDto payload,
      @RequestAttribute RoCrate crate) {

    String decodedContextualId = URLDecoder.decode(contextualId, StandardCharsets.UTF_8);
    if (crate.getContextualEntityById(decodedContextualId) != null) {
      crate.deleteEntityById(decodedContextualId);
    }

    ContextualEntity entity = new ContextualEntity.ContextualEntityBuilder()
        .setId(decodedContextualId)
        .addTypes(List.copyOf(payload.type))
        .build();

    for (Map.Entry<String, JsonNode> entry : payload.properties.entrySet()) {
      entity.addProperty(entry.getKey(), entry.getValue());
    }

    crate = new RoCrate.RoCrateBuilder(crate).addContextualEntity(entity).build();
  }

  @DeleteMapping("/{contextualId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deleteContextualEntity(
      @PathVariable String crateId, @PathVariable String contextualId,
      @RequestAttribute RoCrate crate) {

    crate.deleteEntityById(URLDecoder.decode(contextualId, StandardCharsets.UTF_8));

  }

  @GetMapping("/{contextualId}")
  @ResponseStatus(code = HttpStatus.OK)
  public ObjectNode getContextualEntity(
      @PathVariable String crateId, @PathVariable String contextualId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(contextualId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }

    return entity.getProperties();
  }

  @PutMapping("/{contextualId}/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToContextualEntity(
      @PathVariable String crateId, @PathVariable String contextualId, @PathVariable String property,
      @RequestBody JsonNode body,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(contextualId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.addProperty(property, body);

  }

  @DeleteMapping("/{contextualId}/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePropertyFromContextualEntity(
      @PathVariable String crateId, @PathVariable String contextualId, @PathVariable String property,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(contextualId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.getProperties().remove(property);
  }

}
