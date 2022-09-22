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
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate_rest.dto.DataEntityDto;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/crates/{crateId}/entities/data/")
public class DataEntityController {

  @GetMapping("/")
  @ResponseStatus(code = HttpStatus.OK)
  public List<DataEntity> getDataEntities(
      @PathVariable String crateId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    return crate.getAllDataEntities();
  }

  @PutMapping("/{dataId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addDataEntity(
      @PathVariable String crateId, @PathVariable String dataId,
      @RequestBody @Validated DataEntityDto payload,
      @RequestAttribute RoCrate crate) {

    String decodedDataId = URLDecoder.decode(dataId, StandardCharsets.UTF_8);
    if (crate.getDataEntityById(decodedDataId) != null) {
      crate.deleteEntityById(decodedDataId);
    }

    DataEntity entity = new DataEntity.DataEntityBuilder()
        .setId(decodedDataId)
        .addTypes(List.copyOf(payload.type))
        .build();

    for (Map.Entry<String, JsonNode> entry : payload.properties.entrySet()) {
      entity.addProperty(entry.getKey(), entry.getValue());
    }

    crate = new RoCrate.RoCrateBuilder(crate).addDataEntity(entity).build();
  }

  @DeleteMapping("/{dataId}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deleteDataEntity(
      @PathVariable String crateId, @PathVariable String dataId,
      @RequestAttribute RoCrate crate) {

    crate.deleteEntityById(URLDecoder.decode(dataId, StandardCharsets.UTF_8));

  }

  @GetMapping("/{dataId}")
  @ResponseStatus(code = HttpStatus.OK)
  public ObjectNode getDataEntity(
      @PathVariable String crateId, @PathVariable String dataId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    DataEntity entity = crate.getDataEntityById(URLDecoder.decode(dataId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }

    return entity.getProperties();
  }

  @PutMapping("/{dataId}/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToDataEntity(
      @PathVariable String crateId, @PathVariable String dataId, @PathVariable String property,
      @RequestBody JsonNode body,
      @RequestAttribute RoCrate crate) {

    DataEntity entity = crate.getDataEntityById(URLDecoder.decode(dataId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.addProperty(property, body);

  }

  @DeleteMapping("/{dataId}/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePropertyFromDataEntity(
      @PathVariable String crateId, @PathVariable String dataId, @PathVariable String property,
      @RequestAttribute RoCrate crate) {

    DataEntity entity = crate.getDataEntityById(URLDecoder.decode(dataId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.getProperties().remove(property);
  }

}
