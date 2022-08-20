package edu.kit.datamanager.ro_crate_rest.controller;

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

import edu.kit.datamanager.ro_crate_rest.storage.LocalStorageZipStrategy;
import edu.kit.datamanager.ro_crate_rest.storage.StorageClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.util.ArrayList;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest
public class CrateControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private ArrayList<String> crateIds = new ArrayList<>();

  final private StorageClient storageClient = new StorageClient(new LocalStorageZipStrategy());

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

    assertNull(this.storageClient.get().getCrate(this.crateIds.get(0)));

  }

  @Test
  public void testCrateUpdate() throws Exception {
    InputStream is = getClass().getClassLoader().getResourceAsStream("basic-crate.zip");

    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "basic-crate.zip",
        "application/zip", is.readAllBytes());

    this.mockMvc
        .perform(multipart("/crates/" + this.crateIds.get(0) + "/example.zip", null, null)
            .file(mockMultipartFile))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()))

        .andReturn();

  }

}