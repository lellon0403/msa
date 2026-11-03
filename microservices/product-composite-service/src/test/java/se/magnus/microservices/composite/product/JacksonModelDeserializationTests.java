package se.magnus.microservices.composite.product;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.json.JsonMapper;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.http.HttpErrorInfo;
import static org.junit.jupiter.api.Assertions.*;
class JacksonModelDeserializationTests {
  Object read(String json, Class<?> type) throws Exception {
    var template = new ProductCompositeServiceApplication().restTemplate();
    var converter = (JacksonJsonHttpMessageConverter) template.getMessageConverters().stream()
        .filter(c -> c instanceof JacksonJsonHttpMessageConverter).findFirst().orElseThrow();
    var input = new MockHttpInputMessage(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    input.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    return converter.read(type, input);
  }
  @Test void product() throws Exception {
    var p=(Product) read("{\"productId\":1,\"name\":\"name-1\",\"weight\":123,\"serviceAddress\":\"product:7002\"}", Product.class);
    assertEquals(1,p.getProductId()); assertEquals("name-1",p.getName()); assertEquals(123,p.getWeight());
  }
  @Test void recommendation() throws Exception {
    var p=(Recommendation) read("{\"productId\":1,\"recommendationId\":2,\"author\":\"author\",\"rate\":4,\"content\":\"content\",\"serviceAddress\":\"rec:7003\"}", Recommendation.class);
    assertEquals(1,p.getProductId()); assertEquals(2,p.getRecommendationId()); assertEquals(4,p.getRate()); assertEquals("author",p.getAuthor());
  }
  @Test void review() throws Exception {
    var p=(Review) read("{\"productId\":1,\"reviewId\":3,\"author\":\"author\",\"subject\":\"subject\",\"content\":\"content\",\"serviceAddress\":\"review:7004\"}", Review.class);
    assertEquals(1,p.getProductId()); assertEquals(3,p.getReviewId()); assertEquals("subject",p.getSubject());
  }
  @Test void errorMapper() {
    var p=JsonMapper.builder().build().readValue("{\"timestamp\":\"2026-11-02T16:00:00+09:00\",\"status\":404,\"error\":\"Not Found\",\"path\":\"/product/13\",\"message\":\"No product found for productId: 13\"}",HttpErrorInfo.class);
    assertEquals("No product found for productId: 13",p.getMessage()); assertEquals("/product/13",p.getPath());
  }
}