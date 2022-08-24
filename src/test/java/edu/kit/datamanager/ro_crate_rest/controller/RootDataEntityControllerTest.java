package edu.kit.datamanager.ro_crate_rest.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate_rest.storage.LocalStorageZipStrategy;
import edu.kit.datamanager.ro_crate_rest.storage.StorageClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@WebMvcTest
public class RootDataEntityControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private ArrayList<String> crateIds = new ArrayList<>();

  final private StorageClient storageClient = new StorageClient(new LocalStorageZipStrategy());
  final private ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    InputStream is = getClass().getClassLoader().getResourceAsStream("basic-crate.zip");

    String crateId = this.storageClient.get().storeCrate(is);
    this.crateIds.add(crateId);
  }

  @AfterEach
  public void tearDown() {
    for (String crateId : crateIds) {
      this.storageClient.get().deleteCrate(crateId);
    }
    crateIds.clear();
  }

  @Test
  public void testAddEntity() throws Exception {

    String crateId = crateIds.get(0);
    String rootId = "cool_entity";
    String encodedRootId = URLEncoder.encode(rootId, StandardCharsets.UTF_8);
    String description = "Some Root Data Entity Stuff";
    String descriptionKey = "description";
    ObjectNode payload = this.mapper.createObjectNode().put(descriptionKey, description);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/root/" + encodedRootId)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getRootDataEntity());
    assertEquals(crate.getRootDataEntity().getProperty(descriptionKey).asText(), description);
  }

  @Test
  public void testAddExistingEntity() throws Exception {
    String crateId = crateIds.get(0);
    String rootId = "cool_entity";
    String encodedRootId = URLEncoder.encode(rootId, StandardCharsets.UTF_8);
    String name = "Some Entity";
    String type = "File";
    ObjectNode payload = this.mapper.createObjectNode().put("name", name).put("@type", type);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/root/" + encodedRootId)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getRootDataEntity());

    ObjectNode payloadOverwrite = this.mapper.createObjectNode().put("@type", type);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/root/" + encodedRootId)
            .content(
                mapper.writeValueAsString((payloadOverwrite)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getRootDataEntity());
    assertNull(crate.getRootDataEntity().getProperty("cool"));

  }

  @Test
  public void testAddEntityWithInvalidCrateId() throws Exception {

    String crateId = "invalid";
    String rootId = "./";
    String encodedPlaceId = URLEncoder.encode(rootId, StandardCharsets.UTF_8);
    String name = "Catalina Memorial Park";
    JsonNode payload = this.mapper.createObjectNode().put("name", name).put("geo", "http://sws.geonames.org/8152663/");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/root/" + encodedPlaceId)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

  @Test
  public void getEntity() throws Exception {

    String crateId = this.crateIds.get(0);
    String rootId = "./";
    String encodedRootId = URLEncoder.encode(rootId, StandardCharsets.UTF_8);

    MvcResult res = this.mockMvc
        .perform(get("/crates/" + crateId + "/entities/root/" + encodedRootId))
        .andExpect(status().is(HttpStatus.OK.value())).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertEquals(rootId, this.mapper.readTree(res.getResponse().getContentAsString()).get("@id").asText());

  }

  @Test
  public void testAddEntityProperty() throws Exception {

    String crateId = this.crateIds.get(0);
    String rootId = "./";
    String encodedRootId = URLEncoder.encode(rootId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/root/" + encodedRootId + "/hello")
            .content(mapper.writeValueAsString("world"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getRootDataEntity());
    assertEquals(crate.getRootDataEntity().getProperty("hello").asText(), "world");

  }

  @Test
  public void testDeleteEntityProperty() throws Exception {
    String crateId = this.crateIds.get(0);
    String rootId = "./";
    String encodedRootId = URLEncoder.encode(rootId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(delete("/crates/" + crateId + "/entities/root/" + encodedRootId + "/description")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getRootDataEntity());
    assertNull(crate.getRootDataEntity().getProperty("description"));

  }

}