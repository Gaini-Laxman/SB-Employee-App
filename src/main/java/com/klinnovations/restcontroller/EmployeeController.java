package com.klinnovations.restcontroller;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    private final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final String PACK1_URL = "https://6466e9a7ba7110b663ab51f2.mockapi.io/api/v1/pack1";
    private final String PACK2_URL = "https://6466e9a7ba7110b663ab51f2.mockapi.io/api/v1/pack2";

    @GetMapping("/combinedData")
    public Mono<ResponseEntity<String>> getCombinedData() {
        // Fetch data from pack1 and pack2 endpoints
        Mono<String> pack1DataMono = fetchDataFromEndpoint(PACK1_URL);
        Mono<String> pack2DataMono = fetchDataFromEndpoint(PACK2_URL);

        return Mono.zip(pack1DataMono, pack2DataMono)
                .flatMap(tuple -> {
                    String pack1Data = tuple.getT1();
                    String pack2Data = tuple.getT2();

                    // Combine pack1Data and pack2Data with the provided JSON data
                    String combinedData = combineWithProvidedData(pack1Data, pack2Data);

                    // Return the combined data as a Mono<ResponseEntity<String>>
                    return Mono.just(ResponseEntity.ok().body(combinedData));
                })
                .onErrorResume(e -> {
                    // Log the error
                    logError(e);

                    // Return internal server error response
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    private Mono<String> fetchDataFromEndpoint(String endpoint) {
        return WebClient.create().get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(String.class);
    }

    private String combineWithProvidedData(String pack1Data, String pack2Data) {
        JSONObject response = new JSONObject();
        response.put("id", 1);
        response.put("customer_id", 101);

        JSONArray pack1Array = new JSONArray(pack1Data);
        JSONArray pack2Array = new JSONArray(pack2Data);

        response.put("pack1", pack1Array);
        response.put("pack2", pack2Array);

        return response.toString();
    }

    private void logError(Throwable e) {
        logger.error("An error occurred:", e);
    }
}
