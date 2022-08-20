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
import edu.kit.datamanager.ro_crate.entities.contextual.PersonEntity;
import edu.kit.datamanager.ro_crate_rest.dto.PersonEntityDto;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/crates/{crateId}/entities/contextual/persons/{personId}")
public class PersonEntityController {

  @PutMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPersonEntity(
      @PathVariable String crateId, @PathVariable String personId,
      @RequestBody @Validated PersonEntityDto payload,
      @RequestAttribute RoCrate crate) {

    String decodedPersonId = URLDecoder.decode(personId, StandardCharsets.UTF_8);

    if (crate.getContextualEntityById(decodedPersonId) != null) {
      crate.deleteEntityById(decodedPersonId);
    }

    PersonEntity entity = new PersonEntity.PersonEntityBuilder()
        .setId(decodedPersonId)
        .addProperty("name", payload.name)
        .build();

    for (Map.Entry<String, JsonNode> entry : payload.properties.entrySet()) {
      entity.addProperty(entry.getKey(), entry.getValue());
    }

    crate = new RoCrate.RoCrateBuilder(crate).addContextualEntity(entity).build();
  }

  @DeleteMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePersonEntity(
      @PathVariable String crateId, @PathVariable String personId,
      @RequestAttribute RoCrate crate) {

    crate.deleteEntityById(URLDecoder.decode(personId, StandardCharsets.UTF_8));

  }

  @GetMapping()
  @ResponseStatus(code = HttpStatus.OK)
  public ObjectNode getPersonEntity(
      @PathVariable String crateId, @PathVariable String personId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(personId, StandardCharsets.UTF_8));

    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }

    return entity.getProperties();
  }

  @PutMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToPersonEntity(
      @PathVariable String crateId, @PathVariable String personId, @PathVariable String property,
      @RequestBody JsonNode body,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(personId, StandardCharsets.UTF_8));

    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.addProperty(property, body);

  }

  @DeleteMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePropertyFromPersonEntity(
      @PathVariable String crateId, @PathVariable String personId, @PathVariable String property,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(personId, StandardCharsets.UTF_8));

    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.getProperties().remove(property);
  }

}
