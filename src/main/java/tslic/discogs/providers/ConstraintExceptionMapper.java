package tslic.discogs.providers;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConstraintExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

  @Override
  public Response toResponse(ConstraintViolationException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(prepareMessage(exception))
        .type("text/plain")
        .build();
  }

  private String prepareMessage(ConstraintViolationException exception) {
    StringBuilder message = new StringBuilder();
    for (ConstraintViolation<?> cv : exception.getConstraintViolations()) {
      message.append(cv.getPropertyPath()).append(" ").append(cv.getMessage()).append("\n");
    }
    return message.toString();
  }
}
