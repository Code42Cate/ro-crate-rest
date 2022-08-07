package edu.kit.datamanager.rocraterest.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.rocraterest.storage.LocalStorageService;
import edu.kit.datamanager.rocraterest.storage.StorageService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest
public class CrateControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private ArrayList<String> crateIds = new ArrayList<>();

  final private StorageService storageService = new LocalStorageService();

  @BeforeEach
  public void setUp() {
    InputStream is = getClass().getClassLoader().getResourceAsStream("basic-crate.zip");

    String crateId = UUID.randomUUID().toString();

    this.storageService.store(is, crateId);

    this.crateIds.add(crateId);
  }

  @Test
  public void testCratePost() throws Exception {
    InputStream is = getClass().getClassLoader().getResourceAsStream("basic-crate.zip");

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "basic-crate.zip",
        "application/zip", is.readAllBytes());

    MvcResult res = this.mockMvc.perform(multipart("/crates", null, null).file(mockMultipartFile))
        .andExpect(status().isOk())
        .andReturn();

    String jsonString = res.getResponse().getContentAsString();

    String id = ((ObjectNode) new ObjectMapper().readTree(jsonString)).get("id").asText();

    assertNotNull(id);

    this.crateIds.add(id);

  }

  @Test
  public void testCrateGet() throws Exception {
    this.mockMvc.perform(get("/crates/" + this.crateIds.get(0)))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/zip"));
  }

  @Test
  public void testCrateDelete() throws Exception {
    this.mockMvc.perform(delete("/crates/" + this.crateIds.get(0)))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    assertFalse(this.storageService.get(this.crateIds.get(0)).exists());

  }

  @Test
  public void testCrateUpdate() throws Exception {
    InputStream is = getClass().getClassLoader().getResourceAsStream("basic-crate.zip");

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "basic-crate.zip",
        "application/zip", is.readAllBytes());

    MvcResult res = this.mockMvc
        .perform(multipart("/crates/" + this.crateIds.get(0), null, null).file(mockMultipartFile))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()))

        .andReturn();

  }

  @AfterEach
  public void tearDown() {
    for (String crateId : crateIds) {
      storageService.delete(crateId);
    }
    crateIds.clear();
  }

}