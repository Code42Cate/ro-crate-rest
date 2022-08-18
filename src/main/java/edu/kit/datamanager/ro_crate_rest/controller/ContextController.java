package edu.kit.datamanager.ro_crate_rest.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate_rest.dto.PairValuePayload;

@RestController
@RequestMapping("/crates/{crateId}/context")
public class ContextController {


  @DeleteMapping("/pairs/{key}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void deletePair(@PathVariable String crateId, @PathVariable String key, @RequestAttribute RoCrate crate) {

    crate.deleteValuePairFromContext(key);

  }

  @PutMapping("/pairs/{key}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addPair(@PathVariable String crateId, @PathVariable String key,
      @RequestBody @Validated PairValuePayload pairValuePayload,
      @RequestAttribute RoCrate crate) {

    crate = new RoCrate.RoCrateBuilder(crate).addValuePairToContext(key, pairValuePayload.value).build();

  }

  @PutMapping("/urls/{url}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void addUrl(@PathVariable String crateId, @PathVariable String url, @RequestAttribute RoCrate crate) {

    crate = new RoCrate.RoCrateBuilder(crate).addUrlToContext(URLDecoder.decode(url, StandardCharsets.UTF_8)).build();

  }

  /*
   * @DeleteMapping("/crates/{crateId}/context/urls/{url}")
   * 
   * @ResponseStatus(code = HttpStatus.NO_CONTENT)
   * public void deleteUrl(@PathVariable String crateId, @PathVariable String
   * url, @RequestAttribute RoCrate crate) {
   * 
   * // TODO: Add to ro-crate-java
   * // crate = new
   * // RoCrate.RoCrateBuilder(crate).deleteUrlFromContext(URLDecoder.decode(url,
   * // StandardCharsets.UTF_8))
   * // .build();
   * 
   * }
   */
}
