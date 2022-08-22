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
public class DataEntityControllerTest {

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
    String entityId = "cool_entity";
    String encodedEntityId = URLEncoder.encode(entityId, StandardCharsets.UTF_8);
    String name = "Some Entity";
    String type = "File";
    ObjectNode payload = this.mapper.createObjectNode().put("name", name).put("@type", type);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/data/" + encodedEntityId)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getDataEntityById(entityId));
    assertEquals(crate.getDataEntityById(entityId).getProperty("name").asText(), name);
    assertEquals(crate.getDataEntityById(entityId).getProperties().get("@type").asText(), type);

  }

  @Test
  public void testAddEntityMultipleTypes() throws Exception {

    String crateId = crateIds.get(0);
    String entityId = "cool_entity";
    String encodedEntityId = URLEncoder.encode(entityId, StandardCharsets.UTF_8);
    String name = "Some Entity";
    String type = "File";
    String type1 = "File1";
    ObjectNode payload = this.mapper.createObjectNode().put("name", name);

    payload.putArray("@type").add(type).add("File1");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/data/" + encodedEntityId)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getDataEntityById(entityId));
    assertEquals(crate.getDataEntityById(entityId).getProperty("name").asText(), name);

    for (final JsonNode someType : crate.getDataEntityById(entityId).getProperties().get("@type")) {
      assertTrue(someType.asText().equals(type) || someType.asText().equals(type1));
    }

  }

  @Test
  public void testAddEntityMissingType() throws Exception {

    String crateId = crateIds.get(0);
    String entityId = "cool_entity";
    String encodedEntityId = URLEncoder.encode(entityId, StandardCharsets.UTF_8);
    String name = "Some Entity";
    ObjectNode payload = this.mapper.createObjectNode().put("name", name);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/data/" + encodedEntityId)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNull(crate.getDataEntityById(entityId));

  }

  @Test
  public void testAddExistingEntity() throws Exception {
    String crateId = crateIds.get(0);
    String entityId = "cool_entity";
    String encodedEntityId = URLEncoder.encode(entityId, StandardCharsets.UTF_8);
    String name = "Some Entity";
    String type = "File";
    ObjectNode payload = this.mapper.createObjectNode().put("name", name).put("@type", type);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/data/" + encodedEntityId)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getDataEntityById(entityId));

    ObjectNode payloadOverwrite = this.mapper.createObjectNode().put("@type", type);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/data/" + encodedEntityId)
            .content(
                mapper.writeValueAsString((payloadOverwrite)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getDataEntityById(entityId));
    assertNull(crate.getDataEntityById(entityId).getProperty("cool"));

  }

  @Test
  public void testAddEntityWithInvalidCrateId() throws Exception {

    String crateId = "invalid";
    String placeId = "http://sws.geonames.org/8152663/";
    String encodedPlaceId = URLEncoder.encode(placeId, StandardCharsets.UTF_8);
    String name = "Catalina Memorial Park";
    JsonNode payload = this.mapper.createObjectNode().put("name", name).put("geo", "http://sws.geonames.org/8152663/");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/data/" + encodedPlaceId)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

  @Test
  public void testDeleteEntity() throws Exception {

    String crateId = crateIds.get(0);
    String placeId = "http://sws.geonames.org/8152662/";
    String encodedPlaceId = URLEncoder.encode(placeId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(delete("/crates/" + crateId + "/entities/data/" + encodedPlaceId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNull(crate.getDataEntityById(placeId));
  }

  @Test
  public void getEntity() throws Exception {

    String crateId = this.crateIds.get(0);
    String entityId = "file.txt";
    String encodedEntityId = URLEncoder.encode(entityId, StandardCharsets.UTF_8);

    MvcResult res = this.mockMvc
        .perform(get("/crates/" + crateId + "/entities/data/" + encodedEntityId))
        .andExpect(status().is(HttpStatus.OK.value())).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertEquals(entityId, this.mapper.readTree(res.getResponse().getContentAsString()).get("@id").asText());

  }

  @Test
  public void getInvalidEntity() throws Exception {

    String crateId = this.crateIds.get(0);
    String placeId = "#notvalidid";
    String encodedplaceId = URLEncoder.encode(placeId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(get("/crates/" + crateId + "/entities/data/" + encodedplaceId))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

  @Test
  public void testAddEntityProperty() throws Exception {

    String crateId = this.crateIds.get(0);
    String entityId = "file.txt";
    String encodedEntityId = URLEncoder.encode(entityId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/data/" + encodedEntityId + "/open")
            .content(mapper.writeValueAsString("yes"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getDataEntityById(entityId));
    assertEquals(crate.getDataEntityById(entityId).getProperty("open").asText(), "yes");

  }

  @Test
  public void testDeleteEntityProperty() throws Exception {
    String crateId = this.crateIds.get(0);
    String entityId = "file.txt";
    String encodedEntityId = URLEncoder.encode(entityId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(delete("/crates/" + crateId + "/entities/data/" + encodedEntityId + "/name")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getDataEntityById(entityId));
    assertNull(crate.getDataEntityById(entityId).getProperty("name"));

  }

  @Test
  public void testDeleteEntityPropertyWithInvalidPlaceId() throws Exception {
    String crateId = crateIds.get(0);
    String placeId = "#AHHH";
    String encodedplaceId = URLEncoder.encode(placeId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(delete("/crates/" + crateId + "/entities/data/" + encodedplaceId + "/name")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

  @Test
  public void testAddEntityPropertyInvalidPlaceId() throws Exception {

    String crateId = crateIds.get(0);
    String placeId = "#AAAAAAAH";
    String encodedplaceId = URLEncoder.encode(placeId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/data/" + encodedplaceId + "/name")
            .content(mapper.writeValueAsString("Alice"))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

}