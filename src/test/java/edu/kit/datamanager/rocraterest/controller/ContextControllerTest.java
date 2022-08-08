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

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.datamanager.rocraterest.storage.LocalStorageService;
import edu.kit.datamanager.rocraterest.storage.StorageService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest
public class ContextControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private ArrayList<String> crateIds = new ArrayList<>();

  final private StorageService storageService = new LocalStorageService();
  final private ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    InputStream is = getClass().getClassLoader().getResourceAsStream("basic-crate.zip");

    String crateId = UUID.randomUUID().toString();

    this.storageService.store(is, crateId);

    this.crateIds.add(crateId);
  }

  @AfterEach
  public void tearDown() {
    for (String crateId : crateIds) {
      storageService.delete(crateId);
    }
    crateIds.clear();
  }

  @Test
  public void testAddUrl() throws Exception {

    String crateId = crateIds.get(0);
    String urlEncoded = URLEncoder.encode("https://google.de", StandardCharsets.UTF_8);

    this.mockMvc.perform(put("/crates/" + crateId + "/context/urls/" + urlEncoded))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    // check if url is in crate
    // TODO: ro-crate-java needs to be able to return the urls

  }

  @Test
  public void testAddPair() throws Exception {

    String crateId = crateIds.get(0);
    String key = "key";
    String keyEncoded = URLEncoder.encode(key, StandardCharsets.UTF_8);

    this.mockMvc
        .perform(put("/crates/" + crateId + "/context/pairs/" + keyEncoded)
            .content(mapper.createObjectNode().put("value", "value").toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    // check if pair is in crate
  }

  @Test
  public void testRemovePair() throws Exception {

    String crateId = crateIds.get(0);
    String key = "key";
    String keyEncoded = URLEncoder.encode(key, StandardCharsets.UTF_8);

    this.mockMvc.perform(delete("/crates/" + crateId + "/context/pairs/" + keyEncoded))
        .andExpect(status().is(HttpStatus.NO_CONTENT.value()));
  }
}