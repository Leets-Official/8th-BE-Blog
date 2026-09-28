package com.leets.blog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BlogApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerUiAndApiDocumentationAreAvailable() throws Exception {
        ResponseEntity<String> ui = restTemplate.getForEntity("/swagger-ui/index.html", String.class);
        assertThat(ui.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ui.getBody()).contains("swagger-ui");

        ResponseEntity<String> docs = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertThat(docs.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode document = objectMapper.readTree(docs.getBody());
        assertThat(document.at("/paths/~1health/get").isMissingNode()).isFalse();
        assertThat(document.at("/paths/~1string~1repeat/post").isMissingNode()).isFalse();
        assertThat(document.at("/components/schemas/StringRepeatRequest/properties/value/example").asText())
                .isEqualTo("hello");
        assertThat(document.at("/components/schemas/StringRepeatResponse/properties").has("string_one")).isTrue();
        assertThat(document.at("/components/schemas/StringRepeatResponse/properties").has("string_two")).isTrue();
    }

    @Test
    void healthReturnsOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_PLAIN)).isTrue();
        assertThat(response.getBody()).isEqualTo("ok");
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "안녕하세요", "", "  hello  ", "\"quoted\"\nline"})
    void repeatReturnsInputInBothJsonFields(String value) throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/string/repeat", Map.of("value", value), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        JsonNode expected = objectMapper.valueToTree(Map.of("string_one", value, "string_two", value));
        assertThat(objectMapper.readTree(response.getBody())).isEqualTo(expected);
    }
}
