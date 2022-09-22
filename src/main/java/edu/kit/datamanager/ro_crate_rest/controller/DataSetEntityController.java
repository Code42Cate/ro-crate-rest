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
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;
import edu.kit.datamanager.ro_crate_rest.dto.DataSetEntityDto;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/crates/{crateId}/entities/data/datasets/{dataSetId}")
public class DataSetEntityController {

  @PutMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addDataSetEntity(
      @PathVariable String crateId, @PathVariable @Validated String dataSetId,
      @RequestBody @Validated DataSetEntityDto payload,
      @RequestAttribute RoCrate crate) {

    String decodedDataSetId = URLDecoder.decode(dataSetId, StandardCharsets.UTF_8);
    if (crate.getDataEntityById(decodedDataSetId) != null) {
      crate.deleteEntityById(decodedDataSetId);
    }

    DataSetEntity entity = new DataSetEntity.DataSetBuilder()
        .setId(decodedDataSetId)
        .build();

    for (Map.Entry<String, JsonNode> entry : payload.properties.entrySet()) {
      entity.addProperty(entry.getKey(), entry.getValue());
    }

    crate = new RoCrate.RoCrateBuilder(crate).addDataEntity(entity).build();
  }

  @DeleteMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deleteDataSetEntity(
      @PathVariable String crateId, @PathVariable String dataSetId,
      @RequestAttribute RoCrate crate) {

    crate.deleteEntityById(URLDecoder.decode(dataSetId, StandardCharsets.UTF_8));

  }

  @GetMapping()
  @ResponseStatus(code = HttpStatus.OK)
  public ObjectNode getDataSetEntity(
      @PathVariable String crateId, @PathVariable String dataSetId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    DataEntity entity = crate.getDataEntityById(URLDecoder.decode(dataSetId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }

    return entity.getProperties();
  }

  @PutMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToDataSetEntity(
      @PathVariable String crateId, @PathVariable String dataSetId, @PathVariable String property,
      @RequestBody JsonNode body,
      @RequestAttribute RoCrate crate) {

    DataEntity entity = crate.getDataEntityById(URLDecoder.decode(dataSetId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.addProperty(property, body);

  }

  @DeleteMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePropertyFromDataSetEntity(
      @PathVariable String crateId, @PathVariable String dataSetId, @PathVariable String property,
      @RequestAttribute RoCrate crate) {

    DataEntity entity = crate.getDataEntityById(URLDecoder.decode(dataSetId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.getProperties().remove(property);
  }

}
