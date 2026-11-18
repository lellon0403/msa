package se.magnus.microservices.core.review.services;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.EventProcessingException;

@Configuration
public class MessageProcessorConfig {
  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);
  private final ReviewService reviewService;

  public MessageProcessorConfig(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @Bean
  public Consumer<Event<Integer, Review>> messageProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE:
          reviewService.createReview(event.getData()).block();
          break;
        case DELETE:
          reviewService.deleteReviews(event.getKey()).block();
          break;
        default:
          throw new EventProcessingException("Incorrect event type: " +
            event.getEventType() + ", expected a CREATE or DELETE event");
      }
      LOG.debug("Processed event: {} for productId: {}", event.getEventType(), event.getKey());
    };
  }
}
