package tslic.discogs;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import tslic.discogs.Requests.PageRequest;

public class Resources {

  @RequestScoped
  @Path("artists")
  @Produces(MediaType.APPLICATION_JSON)
  public static class Artists {

    @Inject private Repository repository;

    @GET
    @Path("{artistId}")
    public void get(
        @PathParam("artistId") @Min(1) Integer artistId, @Suspended AsyncResponse asyncResponse) {
      repository.findArtistById(artistId).thenAccept(asyncResponse::resume);
    }

    @GET
    @Path("{artistId}/releases")
    public void getReleases(
        @PathParam("artistId") @Min(1) Integer artistId,
        @Valid @BeanParam PageRequest pageRequest,
        @Suspended AsyncResponse asyncResponse) {
      repository.findReleasesOfArtist(artistId).thenAccept(asyncResponse::resume);
    }
  }

  @RequestScoped
  @Path("labels")
  @Produces(MediaType.APPLICATION_JSON)
  public static class Labels {

    @Inject private Repository repository;

    @GET
    @Path("{labelId}")
    public void get(@PathParam("labelId") Integer labelId, @Suspended AsyncResponse asyncResponse) {
      repository.findLabelById(labelId).thenAccept(asyncResponse::resume);
    }
  }

  @RequestScoped
  @Path("masters")
  @Produces(MediaType.APPLICATION_JSON)
  public static class Masters {

    @Inject private Repository repository;

    @GET
    @Path("{masterId}")
    public void get(
        @PathParam("masterId") @Min(1) Integer masterId, @Suspended AsyncResponse asyncResponse) {
      repository.findMasterById(masterId).thenAccept(asyncResponse::resume);
    }
  }

  @RequestScoped
  @Path("releases")
  @Produces(MediaType.APPLICATION_JSON)
  public static class Releases {

    @Inject private Repository repository;

    @GET
    @Path("{releaseId}")
    public void get(
        @PathParam("releaseId") @Min(1) Integer releaseId, @Suspended AsyncResponse asyncResponse) {
      repository.findReleaseById(releaseId).thenAccept(asyncResponse::resume);
    }
  }
}
