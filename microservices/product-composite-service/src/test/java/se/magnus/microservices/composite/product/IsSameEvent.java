package se.magnus.microservices.composite.product;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import se.magnus.api.event.Event;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

public class IsSameEvent extends TypeSafeDiagnosingMatcher<String> {
  private final ObjectNode expected;
  private final ObjectMapper mapper = new ObjectMapper();

  private IsSameEvent(Event<?, ?> event) {
    expected = (ObjectNode) mapper.readTree(mapper.writeValueAsString(event));
    expected.remove("eventCreatedAt");
  }

  public static Matcher<String> sameEventExceptCreatedAt(Event<?, ?> event) {
    return new IsSameEvent(event);
  }

  @Override
  protected boolean matchesSafely(String json, Description mismatch) {
    try {
      ObjectNode actual = (ObjectNode) mapper.readTree(json);
      actual.remove("eventCreatedAt");
      if (expected.equals(actual)) return true;
      mismatch.appendText("event was ").appendValue(actual);
    } catch (RuntimeException ex) {
      mismatch.appendText("invalid event JSON: ").appendText(ex.getMessage());
    }
    return false;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("event, excluding eventCreatedAt, equal to ").appendValue(expected);
  }
}
