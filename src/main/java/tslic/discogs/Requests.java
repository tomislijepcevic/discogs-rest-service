package tslic.discogs;

import javax.validation.constraints.Min;
import javax.ws.rs.QueryParam;
import lombok.Data;

class Requests {

  @Data
  public static class PageRequest {

    @QueryParam("previous")
    @Min(1)
    Integer previous = 0;

    @QueryParam("total")
    @Min(1)
    Integer total = 10;
  }
}
