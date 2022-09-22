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
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate_rest.dto.FileEntityDto;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/crates/{crateId}/entities/data/files/{fileId}")
public class FileEntityController {

  @PutMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addFileEntity(
      @PathVariable String crateId, @PathVariable @Validated String fileId,
      @RequestBody @Validated FileEntityDto payload,
      @RequestAttribute RoCrate crate) {

    String decodedFileId = URLDecoder.decode(fileId, StandardCharsets.UTF_8);
    if (crate.getDataEntityById(decodedFileId) != null) {
      crate.deleteEntityById(decodedFileId);
    }

    FileEntity entity = new FileEntity.FileEntityBuilder()
        .setId(decodedFileId)
        .build();

    for (Map.Entry<String, JsonNode> entry : payload.properties.entrySet()) {
      entity.addProperty(entry.getKey(), entry.getValue());
    }

    crate = new RoCrate.RoCrateBuilder(crate).addDataEntity(entity).build();
  }

  @DeleteMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deleteFileEntity(
      @PathVariable String crateId, @PathVariable String fileId,
      @RequestAttribute RoCrate crate) {

    crate.deleteEntityById(URLDecoder.decode(fileId, StandardCharsets.UTF_8));

  }

  @GetMapping()
  @ResponseStatus(code = HttpStatus.OK)
  public ObjectNode getFileEntity(
      @PathVariable String crateId, @PathVariable String fileId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    DataEntity entity = crate.getDataEntityById(URLDecoder.decode(fileId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }

    return entity.getProperties();
  }

  @PutMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToFileEntity(
      @PathVariable String crateId, @PathVariable String fileId, @PathVariable String property,
      @RequestBody JsonNode body,
      @RequestAttribute RoCrate crate) {

    DataEntity entity = crate.getDataEntityById(URLDecoder.decode(fileId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.addProperty(property, body);

  }

  @DeleteMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePropertyFromFileEntity(
      @PathVariable String crateId, @PathVariable String fileId, @PathVariable String property,
      @RequestAttribute RoCrate crate) {

    DataEntity entity = crate.getDataEntityById(URLDecoder.decode(fileId, StandardCharsets.UTF_8));
    if (entity == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
    }

    entity.getProperties().remove(property);
  }

}
