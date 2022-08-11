package edu.kit.datamanager.rocraterest.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.PersonEntity;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/crates/{crateId}/entities/contextual/persons")
public class PersonEntityController {

  static class PersonEntityPayload {
    public String name;
    public Map<String, JsonNode> properties = new HashMap<String, JsonNode>();

    @JsonAnySetter
    public void add(String key, JsonNode value) {
      properties.put(key, value);
    }

  }

  // TODO: This feels stupid
  static class PersonEntityPropertyPayload {
    public JsonNode value;

    @JsonCreator
    public PersonEntityPropertyPayload(JsonNode json) {
      this.value = json.get("value");
    }
  }

  @PutMapping("/{personId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPersonEntity(
      @PathVariable String crateId, @PathVariable String personId,
      @RequestBody PersonEntityPayload payload,
      @RequestAttribute RoCrate crate) {

    if (crate.getEntityById(personId) != null) {
      crate.deleteEntityById(personId);
    }

    PersonEntity personEntity = new PersonEntity.PersonEntityBuilder()
        .setId(URLDecoder.decode(personId, StandardCharsets.UTF_8))
        .addProperty("name", payload.name)
        .build();

    for (Map.Entry<String, JsonNode> entry : payload.properties.entrySet()) {
      personEntity.addProperty(entry.getKey(), entry.getValue());
    }

    crate = new RoCrate.RoCrateBuilder(crate).addContextualEntity(personEntity).build();
  }

  @DeleteMapping("/{personId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePersonEntity(
      @PathVariable String crateId, @PathVariable String personId,
      @RequestAttribute RoCrate crate) {

    crate.deleteEntityById(URLDecoder.decode(personId, StandardCharsets.UTF_8));

  }

  @GetMapping("/{personId}")
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

  @PutMapping("/{personId}/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToPersonEntity(
      @PathVariable String crateId, @PathVariable String personId, @PathVariable String property,
      @RequestBody PersonEntityPropertyPayload personEntityPropertyPayload,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(personId, StandardCharsets.UTF_8));

    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.addProperty(property, personEntityPropertyPayload.value);

  }

  @DeleteMapping("/{personId}/{property}")
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
