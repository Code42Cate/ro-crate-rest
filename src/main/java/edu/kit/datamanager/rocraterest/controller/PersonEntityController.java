package edu.kit.datamanager.rocraterest.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.contextual.PersonEntity;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class PersonEntityController {

  static class PersonEntityPayload {
    public String name;

    public PersonEntityPayload() {
    }

    @JsonCreator
    public PersonEntityPayload(String name) {
      this.name = name;
    }
  }

  static class PersonEntityPropertyPayload {
    public JsonNode value;

    @JsonCreator
    public PersonEntityPropertyPayload(JsonNode value) {
      this.value = value.get("value");
    }
  }

  @PutMapping("/crates/{crateId}/entities/contextual/persons/{personId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPersonEntity(@PathVariable String crateId, @PathVariable String personId,
      @RequestBody PersonEntityPayload personEntityPayload, @RequestAttribute RoCrate crate) {

    PersonEntity personEntity = new PersonEntity.PersonEntityBuilder()
        .setId(URLDecoder.decode(personId, StandardCharsets.UTF_8))
        .addProperty("name", personEntityPayload.name)
        .build();

    crate = new RoCrate.RoCrateBuilder(crate).addContextualEntity(personEntity).build();
  }

  @DeleteMapping("/crates/{crateId}/entities/contextual/persons/{personId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePersonEntity(@PathVariable String crateId, @PathVariable String personId,
      @RequestAttribute RoCrate crate) {
    crate.deleteEntityById(URLDecoder.decode(personId, StandardCharsets.UTF_8));
  }

  @GetMapping("/crates/{crateId}/entities/contextual/persons/{personId}")
  @ResponseStatus(code = HttpStatus.OK)
  public ObjectNode getPersonEntity(@PathVariable String crateId, @PathVariable String personId,
      @RequestAttribute RoCrate crate, HttpServletResponse res) {
    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(personId, StandardCharsets.UTF_8));

    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }
    return entity.getProperties();
  }

  @PutMapping("/crates/{crateId}/entities/contextual/persons/{personId}/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToPersonEntity(@PathVariable String crateId, @PathVariable String personId,
      @PathVariable String property, @RequestBody PersonEntityPropertyPayload personEntityPropertyPayload,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(personId, StandardCharsets.UTF_8));

    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }
    System.out.println(personEntityPropertyPayload.value);
    System.out.println(property);

    entity.addProperty(property, personEntityPropertyPayload.value);

    crate.deleteEntityById(personId);

    crate = new RoCrate.RoCrateBuilder(crate).addContextualEntity(entity).build();
  }

}
