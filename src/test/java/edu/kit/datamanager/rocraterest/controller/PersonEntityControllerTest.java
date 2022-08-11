package edu.kit.datamanager.rocraterest.controller;

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

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.rocraterest.storage.LocalStorageZipStrategy;
import edu.kit.datamanager.rocraterest.storage.StorageClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@WebMvcTest
public class PersonEntityControllerTest {

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
    String personId = "#Alice";
    String keyEncoded = URLEncoder.encode(personId, StandardCharsets.UTF_8);
    JsonNode payload = this.mapper.createObjectNode().put("name", "Alice");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded)
            .content(
                mapper.writeValueAsString(new PersonEntityController.PersonEntityPropertyPayload(
                    payload)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(personId));

  }

  @Test
  public void testAddExistingEntity() throws Exception {

    String crateId = crateIds.get(0);
    String personId = "#Alice";
    String keyEncoded = URLEncoder.encode(personId, StandardCharsets.UTF_8);
    JsonNode payload = this.mapper.createObjectNode().put("name", "Alice").put("hobby", "being nice");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded)
            .content(
                mapper.writeValueAsString(new PersonEntityController.PersonEntityPropertyPayload(
                    payload)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(personId));

    JsonNode payloadOverwrite = this.mapper.createObjectNode().put("name", "Alice");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded)
            .content(
                mapper.writeValueAsString(new PersonEntityController.PersonEntityPropertyPayload(
                    payloadOverwrite)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(personId));
    assertNull(crate.getContextualEntityById(personId).getProperty("hobby"));

  }

  @Test
  public void testAddEntityWithInvalidCrateId() throws Exception {

    String crateId = "invalid";
    String personId = "#Alice";
    String keyEncoded = URLEncoder.encode(personId, StandardCharsets.UTF_8);
    JsonNode payload = this.mapper.createObjectNode().put("name", "Alice");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded)
            .content(
                mapper.writeValueAsString(new PersonEntityController.PersonEntityPropertyPayload(
                    payload)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

  @Test
  public void testDeleteEntity() throws Exception {

    String crateId = this.crateIds.get(0);
    String personId = "#Eve";
    String keyEncoded = URLEncoder.encode(personId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(delete("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    System.out.println(crate.getJsonMetadata());

    assertNull(crate.getContextualEntityById(personId));
  }

  @Test
  public void getEntity() throws Exception {

    String crateId = this.crateIds.get(0);
    String personId = "#Eve";
    String keyEncoded = URLEncoder.encode(personId, StandardCharsets.UTF_8);

    MvcResult res = this.mockMvc
        .perform(get("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded))
        .andExpect(status().is(HttpStatus.OK.value())).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertEquals(personId, this.mapper.readTree(res.getResponse().getContentAsString()).get("@id").asText());

  }

  @Test
  public void getInvalidEntity() throws Exception {

    String crateId = this.crateIds.get(0);
    String personId = "#Eveeee";
    String keyEncoded = URLEncoder.encode(personId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(get("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

  @Test
  public void testAddEntityProperty() throws Exception {

    String crateId = crateIds.get(0);
    String personId = "#Eve";
    String keyEncoded = URLEncoder.encode(personId, StandardCharsets.UTF_8);
    JsonNode payload = this.mapper.createObjectNode().put("value", "Alice");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded + "/name")
            .content(mapper.writeValueAsString(new PersonEntityController.PersonEntityPropertyPayload(
                payload)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(personId));
    assertEquals(crate.getContextualEntityById(personId).getProperty("name").asText(), "Alice");

  }

  @Test
  public void testDeleteEntityProperty() throws Exception {
    String crateId = crateIds.get(0);
    String personId = "#Eve";
    String keyEncoded = URLEncoder.encode(personId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(delete("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded + "/name")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(personId));
    assertNull(crate.getContextualEntityById(personId).getProperty("name"));

  }

  @Test
  public void testAddEntityPropertyInvalidId() throws Exception {

    String crateId = crateIds.get(0);
    String personId = "#Eveeeee";
    String keyEncoded = URLEncoder.encode(personId, StandardCharsets.UTF_8);
    JsonNode payload = this.mapper.createObjectNode().put("value", "Alice");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/persons/" + keyEncoded + "/name")
            .content(mapper.writeValueAsString(new PersonEntityController.PersonEntityPropertyPayload(
                payload)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

}