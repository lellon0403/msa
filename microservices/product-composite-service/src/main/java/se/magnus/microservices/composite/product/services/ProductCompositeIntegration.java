package se.magnus.microservices.composite.product.services;

import org.springframework.stereotype.Component;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.ReviewService;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import se.magnus.api.core.product.Product;

@Component
public class ProductCompositeIntegration
        implements ProductService, RecommendationService, ReviewService {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;


    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,

            @Value("${app.product-service.host}")
            String productServiceHost,

            @Value("${app.product-service.port}")
            int productServicePort,

            @Value("${app.recommendation-service.host}")
            String recommendationServiceHost,

            @Value("${app.recommendation-service.port}")
            int recommendationServicePort,

            @Value("${app.review-service.host}")
            String reviewServiceHost,

            @Value("${app.review-service.port}")
            int reviewServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        productServiceUrl = "http://" + productServiceHost + ":" +
                productServicePort + "/product/";

        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" +
                recommendationServicePort + "/recommendation?productId=";

        reviewServiceUrl = "http://" + reviewServiceHost + ":" +
                reviewServicePort + "/review?productId=";
    }

    // 75쪽 3번: getProduct() 메서드는 이 아래에 추가하세요. (클래스의 마지막 중괄호 앞)
    public Product getProduct(int productId) {
        String url = productServiceUrl + productId;
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }


}