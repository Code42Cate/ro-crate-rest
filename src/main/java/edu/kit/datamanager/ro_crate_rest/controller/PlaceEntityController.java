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
import edu.kit.datamanager.ro_crate.entities.contextual.PlaceEntity;
import edu.kit.datamanager.ro_crate_rest.dto.PlaceEntityDto;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/crates/{crateId}/entities/contextual/places/{placeId}")
public class PlaceEntityController {

  @PutMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPlaceEntity(
      @PathVariable String crateId, @PathVariable String placeId,
      @RequestBody @Validated PlaceEntityDto payload,
      @RequestAttribute RoCrate crate) {

    String decodedPlaceId = URLDecoder.decode(placeId, StandardCharsets.UTF_8);
    if (crate.getContextualEntityById(decodedPlaceId) != null) {
      crate.deleteEntityById(decodedPlaceId);
    }

    PlaceEntity entity = new PlaceEntity.PlaceEntityBuilder()
        .setId(decodedPlaceId)
        .setGeo(payload.geo)
        .addProperty("name", payload.name)
        .build();

    for (Map.Entry<String, JsonNode> entry : payload.properties.entrySet()) {
      entity.addProperty(entry.getKey(), entry.getValue());
    }

    crate = new RoCrate.RoCrateBuilder(crate).addContextualEntity(entity).build();
  }

  @DeleteMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePlaceEntity(
      @PathVariable String crateId, @PathVariable String placeId,
      @RequestAttribute RoCrate crate) {

    crate.deleteEntityById(URLDecoder.decode(placeId, StandardCharsets.UTF_8));

  }

  @GetMapping()
  @ResponseStatus(code = HttpStatus.OK)
  public ObjectNode getPlaceEntity(
      @PathVariable String crateId, @PathVariable String placeId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(placeId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }

    return entity.getProperties();
  }

  @PutMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToPlaceEntity(
      @PathVariable String crateId, @PathVariable String placeId, @PathVariable String property,
      @RequestBody JsonNode body,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(placeId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.addProperty(property, body);

  }

  @DeleteMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePropertyFromPlaceEntity(
      @PathVariable String crateId, @PathVariable String placeId, @PathVariable String property,
      @RequestAttribute RoCrate crate) {

    ContextualEntity entity = crate.getContextualEntityById(URLDecoder.decode(placeId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.getProperties().remove(property);
  }

}
