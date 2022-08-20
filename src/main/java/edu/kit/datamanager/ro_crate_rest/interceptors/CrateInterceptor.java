package edu.kit.datamanager.ro_crate_rest.interceptors;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate_rest.storage.LocalStorageZipStrategy;
import edu.kit.datamanager.ro_crate_rest.storage.StorageClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CrateInterceptor implements HandlerInterceptor {

    // In theory, you could use different strategies for different crates/users
    // here. For example, it might be smart to have big or recently used crates
    // locally, while saving others in something like S3 object storage.
    final private StorageClient storageClient = new StorageClient(new LocalStorageZipStrategy());

    private String parseCrateId(HttpServletRequest request) {
        Map<String, String> pathVariables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVariables == null) {
            return null;
        }
        return pathVariables.get("crateId");
    }

    /*
     * Load the crate referenced in the path from storage and pass it as request
     * attribute to the controller.
     */
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler) {

        String crateId = this.parseCrateId(request);
        if (crateId == null) {
            return true;
        }

        RoCrate crate = this.storageClient.get().getCrate(crateId);
        if (crate == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find entity");
        }

        request.setAttribute("crate", crate);
        // Saving the json for later comparison
        request.setAttribute("_crate_json", crate.getJsonMetadata());

        return true;
    }

    /*
     * Save the modified RoCrate object to the storage.
     */
    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler,
            final Exception ex) throws JsonMappingException, JsonProcessingException {

        String crateId = this.parseCrateId(request);
        if (crateId == null) {
            return;
        }

        String originalCrateJson = (String) request.getAttribute("_crate_json");
        RoCrate updatedCrate = (RoCrate) request.getAttribute("crate");

        JsonNode originalJson = new ObjectMapper().readTree(originalCrateJson);
        JsonNode updatedJson = new ObjectMapper().readTree(updatedCrate.getJsonMetadata());
        // crate got updated, save it. If you dont do this, deleting will fail!
        if (!originalJson.equals(updatedJson)) {
            this.storageClient.get().storeCrate(crateId, updatedCrate);
        }

    }

}
