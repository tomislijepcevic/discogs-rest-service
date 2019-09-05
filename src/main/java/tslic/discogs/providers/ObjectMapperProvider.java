package tslic.discogs.providers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

  private final ObjectMapper objectMapper;

  public ObjectMapperProvider() {
    objectMapper =
        new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
            .setDefaultPropertyInclusion(Include.NON_NULL)
            .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    ;
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }
}
