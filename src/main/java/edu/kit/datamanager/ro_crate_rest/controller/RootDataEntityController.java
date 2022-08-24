package edu.kit.datamanager.ro_crate_rest.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import edu.kit.datamanager.ro_crate_rest.dto.RootEntityDto;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/crates/{crateId}/entities/root/{rootId}")
public class RootDataEntityController {

  @PutMapping()
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addDataEntity(
      @PathVariable String crateId, @PathVariable String rootId,
      @RequestBody @Validated RootEntityDto payload,
      @RequestAttribute RoCrate crate) {

    RootDataEntity root = new RootDataEntity.RootDataEntityBuilder()
        .setId(URLDecoder.decode(rootId, StandardCharsets.UTF_8)).build();

    for (Map.Entry<String, JsonNode> entry : payload.properties.entrySet()) {
      root.addProperty(entry.getKey(), entry.getValue());
    }

    crate.setRootDataEntity(root);

  }

  @GetMapping()
  @ResponseStatus(code = HttpStatus.OK)
  public ObjectNode getDataEntity(
      @PathVariable String crateId,
      @RequestAttribute RoCrate crate,
      HttpServletResponse res) {

    RootDataEntity root = crate.getRootDataEntity();

    return root.getProperties();

  }

  @PutMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPropertyToDataEntity(
      @PathVariable String crateId, @PathVariable String property,
      @RequestBody JsonNode body,
      @RequestAttribute RoCrate crate) {

    RootDataEntity root = crate.getRootDataEntity();

    root.addProperty(property, body);

  }

  @DeleteMapping("/{property}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePropertyFromDataEntity(
      @PathVariable String crateId, @PathVariable String property,
      @RequestAttribute RoCrate crate) {

    RootDataEntity root = crate.getRootDataEntity();

    root.getProperties().remove(property);
  }

}
