package se.magnus.api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface ReviewService {

  @PostMapping(
      value    = "/review",
      consumes = "application/json",
      produces = "application/json")
  Mono<Review> createReview(@RequestBody Review body);

  @GetMapping(
    value = "/review",
    produces = "application/json")
  Flux<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);

  @DeleteMapping(value = "/review")
  Mono<Void> deleteReviews(@RequestParam(value = "productId", required = true)  int productId);
}
