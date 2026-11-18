package se.magnus.microservices.composite.product;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;
import static se.magnus.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.composite.product.ProductAggregate;
import se.magnus.api.composite.product.RecommendationSummary;
import se.magnus.api.composite.product.ReviewSummary;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.api.event.Event;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"eureka.client.enabled=false"})
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration.class)
class MessagingTests {
  private static final Logger LOG = LoggerFactory.getLogger(MessagingTests.class);
  @Autowired private WebTestClient client;
  @Autowired private OutputDestination target;

  @BeforeEach
  void setUp() {
    purgeMessages("products");
    purgeMessages("recommendations");
    purgeMessages("reviews");
  }

  @Test
  void createCompositeProduct1() {
    ProductAggregate composite = new ProductAggregate(1, "name", 1, null, null, null);
    postAndVerifyProduct(composite);
    List<String> productMessages = getMessages("products");
    assertEquals(1, productMessages.size());
    assertThat(productMessages.get(0), sameEventExceptCreatedAt(
      new Event<>(CREATE, 1, new Product(1, "name", 1, null))));
    assertEquals(0, getMessages("recommendations").size());
    assertEquals(0, getMessages("reviews").size());
  }

  @Test
  void createCompositeProduct2() {
    ProductAggregate composite = new ProductAggregate(1, "name", 1,
      asList(new RecommendationSummary(1, "a1", 1, "c1"),
        new RecommendationSummary(2, "a2", 2, "c2")),
      asList(new ReviewSummary(1, "a1", "s1", "c1"),
        new ReviewSummary(2, "a2", "s2", "c2")), null);
    postAndVerifyProduct(composite);
    List<String> productMessages = getMessages("products");
    List<String> recommendationMessages = getMessages("recommendations");
    List<String> reviewMessages = getMessages("reviews");
    assertEquals(1, productMessages.size());
    assertEquals(2, recommendationMessages.size());
    assertEquals(2, reviewMessages.size());
    assertThat(productMessages.get(0), sameEventExceptCreatedAt(
      new Event<>(CREATE, 1, new Product(1, "name", 1, null))));
    // Mono.when can publish sibling entities in either order.
    for (int id = 1; id <= 2; id++) {
      final int entityId = id;
      assertEquals(1, recommendationMessages.stream().filter(json ->
        sameEventExceptCreatedAt(new Event<>(CREATE, 1,
          new Recommendation(1, entityId, "a" + entityId, entityId, "c" + entityId, null)))
          .matches(json)).count());
      assertEquals(1, reviewMessages.stream().filter(json ->
        sameEventExceptCreatedAt(new Event<>(CREATE, 1,
          new Review(1, entityId, "a" + entityId, "s" + entityId, "c" + entityId, null)))
          .matches(json)).count());
    }
  }

  @Test
  void deleteCompositeProduct() {
    client.delete().uri("/product-composite/1").exchange()
      .expectStatus().isAccepted().expectBody().isEmpty();
    for (String destination : asList("products", "recommendations", "reviews")) {
      List<String> messages = getMessages(destination);
      assertEquals(1, messages.size());
      assertThat(messages.get(0), sameEventExceptCreatedAt(new Event<>(DELETE, 1, null)));
    }
  }

  private void postAndVerifyProduct(ProductAggregate composite) {
    client.post().uri("/product-composite").contentType(APPLICATION_JSON)
      .bodyValue(composite).exchange().expectStatus().isAccepted().expectBody().isEmpty();
  }

  private void purgeMessages(String bindingName) {
    getMessages(bindingName);
  }

  private List<String> getMessages(String bindingName) {
    List<String> messages = new ArrayList<>();
    Message<byte[]> message;
    while ((message = getMessage(bindingName)) != null) {
      messages.add(new String(message.getPayload(), UTF_8));
    }
    return messages;
  }

  private Message<byte[]> getMessage(String bindingName) {
    try {
      return target.receive(0, bindingName);
    } catch (NullPointerException ex) {
      LOG.debug("No output queue yet for {}", bindingName);
      return null;
    }
  }
}
