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

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate_rest.storage.LocalStorageZipStrategy;
import edu.kit.datamanager.ro_crate_rest.storage.StorageClient;

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
public class OrganizationEntityControllerTest {

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
    String organizationId = "#MIT";
    String organizationIdEncoded = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);
    String name = "MIT";
    JsonNode payload = this.mapper.createObjectNode().put("name", name).put("url", "https://www.mit.edu");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/organizations/" + organizationIdEncoded)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(organizationId));
    assertEquals(crate.getContextualEntityById(organizationId).getProperty("name").asText(), name);

  }

  @Test
  public void testAddEntityMissingName() throws Exception {

    String crateId = crateIds.get(0);
    String organizationId = "#MIT";
    String organizationIdEncoded = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);
    JsonNode payload = this.mapper.createObjectNode().put("url", "https://www.mit.edu");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/organizations/" + organizationIdEncoded)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNull(crate.getContextualEntityById(organizationId));
  }

  @Test
  public void testAddEntityMissingUrl() throws Exception {

    String crateId = crateIds.get(0);
    String organizationId = "#MIT";
    String organizationIdEncoded = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);
    JsonNode payload = this.mapper.createObjectNode().put("name", "KIT");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/organizations/" + organizationIdEncoded)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNull(crate.getContextualEntityById(organizationId));
  }

  @Test
  public void testAddExistingEntity() throws Exception {

    String crateId = crateIds.get(0);
    String organizationId = "#MIT";
    String organizationIdEncoded = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);
    String name = "MIT";
    JsonNode payload = this.mapper.createObjectNode().put("name", name).put("url", "https://www.mit.edu").put(
        "size",
        3);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/organizations/" + organizationIdEncoded)
            .content(
                mapper.writeValueAsString(
                    payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(organizationId));

    JsonNode payloadOverwrite = this.mapper.createObjectNode().put("name", name).put("url", "https://www.mit.edu");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/organizations/" + organizationIdEncoded)
            .content(
                mapper.writeValueAsString((payloadOverwrite)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(organizationId));
    assertNull(crate.getContextualEntityById(organizationId).getProperty("size"));

  }

  @Test
  public void testAddEntityWithInvalidCrateId() throws Exception {

    String crateId = "invalid";
    String organizationId = "#MIT";
    String organizationIdEncoded = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);
    String name = "MIT";
    JsonNode payload = this.mapper.createObjectNode().put("name", name).put("url", "https://www.mit.edu");

    this.mockMvc
        .perform(put("/crates/" + crateId + "/entities/contextual/organizations/" + organizationIdEncoded)
            .content(
                mapper.writeValueAsString(payload))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

  @Test
  public void testDeleteEntity() throws Exception {

    String crateId = this.crateIds.get(0);
    String organizationId = "#KIT";
    String encodedOrganizationId = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(delete("/crates/" + crateId + "/entities/contextual/organizations/" + encodedOrganizationId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNull(crate.getContextualEntityById(organizationId));
  }

  @Test
  public void getEntity() throws Exception {

    String crateId = this.crateIds.get(0);
    String organizationId = "#KIT";
    String encodedOrganizationId = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);

    MvcResult res = this.mockMvc
        .perform(get("/crates/" + crateId + "/entities/contextual/organizations/" + encodedOrganizationId))
        .andExpect(status().is(HttpStatus.OK.value()))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    assertEquals(organizationId, this.mapper.readTree(res.getResponse().getContentAsString()).get("@id").asText());

  }

  @Test
  public void getInvalidEntity() throws Exception {

    String crateId = this.crateIds.get(0);
    String organizationId = "#KIIIT";
    String encodedOrganizationId = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(get("/crates/" + crateId + "/entities/contextual/organizations/" + encodedOrganizationId))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

  @Test
  public void testAddEntityProperty() throws Exception {

    String crateId = crateIds.get(0);
    String organizationId = "#KIT";
    String encodedOrganizationId = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(put(
            "/crates/" + crateId + "/entities/contextual/organizations/" + encodedOrganizationId + "/name")
                .content(mapper.writeValueAsString("GIT"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(organizationId));
    assertEquals(crate.getContextualEntityById(organizationId).getProperty("name").asText(), "GIT");

  }

  @Test
  public void testDeleteEntityProperty() throws Exception {
    String crateId = crateIds.get(0);
    String organizationId = "#KIT";
    String encodedOrganizationId = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(delete(
            "/crates/" + crateId + "/entities/contextual/organizations/" + encodedOrganizationId + "/name")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    RoCrate crate = this.storageClient.get().getCrate(crateId);

    assertNotNull(crate.getContextualEntityById(organizationId));
    assertNull(crate.getContextualEntityById(organizationId).getProperty("name"));

  }

  @Test
  public void testDeleteEntityPropertyWithInvalidCrateId() throws Exception {
    String crateId = crateIds.get(0);
    String organizationId = "#AHHH";
    String encodedOrganizationId = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(delete(
            "/crates/" + crateId + "/entities/contextual/organizations/" + encodedOrganizationId + "/name")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

  @Test
  public void testAddEntityPropertyInvalidId() throws Exception {

    String crateId = crateIds.get(0);
    String organizationId = "#AAAAAAAH";
    String encodedOrganizationId = URLEncoder.encode(organizationId, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(put(
            "/crates/" + crateId + "/entities/contextual/organizations/" + encodedOrganizationId + "/name")
                .content(mapper.writeValueAsString("Alice"))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

  }

}