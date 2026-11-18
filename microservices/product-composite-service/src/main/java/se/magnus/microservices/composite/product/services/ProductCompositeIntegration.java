package se.magnus.microservices.composite.product.services;

import static java.util.logging.Level.FINE;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.event.Event;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);
  private static final String PRODUCT_SERVICE_URL = "http://product";
  private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
  private static final String REVIEW_SERVICE_URL = "http://review";

  private final StreamBridge streamBridge;
  private final Scheduler publishEventScheduler;
  private final WebClient webClient;
  private final ObjectMapper mapper;

  public ProductCompositeIntegration(StreamBridge streamBridge,
      @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
      WebClient.Builder webClientBuilder, ObjectMapper mapper) {
    this.streamBridge = streamBridge;
    this.publishEventScheduler = publishEventScheduler;
    this.webClient = webClientBuilder.build();
    this.mapper = mapper;
  }

  @Override
  public Mono<Product> getProduct(int productId) {
    String url = PRODUCT_SERVICE_URL + "/product/" + productId;
    return webClient.get().uri(url).retrieve().bodyToMono(Product.class)
      .log(LOG.getName(), FINE)
      .onErrorMap(WebClientResponseException.class, this::handleException);
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    String url = RECOMMENDATION_SERVICE_URL + "/recommendation?productId=" + productId;
    return webClient.get().uri(url).retrieve().bodyToFlux(Recommendation.class)
      .log(LOG.getName(), FINE)
      .onErrorResume(error -> Flux.empty());
  }

  @Override
  public Flux<Review> getReviews(int productId) {
    String url = REVIEW_SERVICE_URL + "/review?productId=" + productId;
    return webClient.get().uri(url).retrieve().bodyToFlux(Review.class)
      .log(LOG.getName(), FINE)
      .onErrorResume(error -> Flux.empty());
  }

  @Override
  public Mono<Product> createProduct(Product body) {
    return Mono.fromCallable(() -> {
      sendMessage("products-out-0", new Event<>(CREATE, body.getProductId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {
    return Mono.fromRunnable(() ->
      sendMessage("products-out-0", new Event<>(DELETE, productId, null)))
      .subscribeOn(publishEventScheduler).then();
  }

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {
    return Mono.fromCallable(() -> {
      sendMessage("recommendations-out-0", new Event<>(CREATE, body.getProductId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {
    return Mono.fromRunnable(() ->
      sendMessage("recommendations-out-0", new Event<>(DELETE, productId, null)))
      .subscribeOn(publishEventScheduler).then();
  }

  @Override
  public Mono<Review> createReview(Review body) {
    return Mono.fromCallable(() -> {
      sendMessage("reviews-out-0", new Event<>(CREATE, body.getProductId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  public Mono<Void> deleteReviews(int productId) {
    return Mono.fromRunnable(() ->
      sendMessage("reviews-out-0", new Event<>(DELETE, productId, null)))
      .subscribeOn(publishEventScheduler).then();
  }

  private void sendMessage(String bindingName, Event<?, ?> event) {
    Message<Event<?, ?>> message = MessageBuilder.<Event<?, ?>>withPayload(event)
      .setHeader("partitionKey", event.getKey())
      .build();
    streamBridge.send(bindingName, message);
  }

  private Throwable handleException(WebClientResponseException ex) {
    int status = ex.getStatusCode().value();
    if (status == 404) return new NotFoundException(getErrorMessage(ex));
    if (status == 422) return new InvalidInputException(getErrorMessage(ex));
    return ex;
  }

  private String getErrorMessage(WebClientResponseException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (JacksonException jsonEx) {
      return ex.getMessage();
    }
  }
}
