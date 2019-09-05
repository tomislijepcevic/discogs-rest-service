package tslic.discogs;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class Responses {

  @Data
  public static class Artist {

    Integer id;
    String name;
    String realname;
    String profile;
    String status;
    String dataQuality;
    List<String> namevariations;
    List<String> urls;
    List<ArtistAlias> aliases;
    List<Group> groups;
    List<GroupMember> members;
    List<Image> images;
    URL releasesUrl;
    URI uri;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createArtistResourceUrl(id);
    }
  }

  @Data
  public static class ArtistAlias {

    Integer id;
    String name;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createArtistResourceUrl(getId());
    }
  }

  @Data
  public static class Group {

    Integer id;
    String name;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createArtistResourceUrl(getId());
    }
  }

  @Data
  public static class GroupMember {

    Integer id;
    String name;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createArtistResourceUrl(getId());
    }
  }

  @Data
  public static class Image {}

  @Data
  public static class Release {

    Integer id;
    String country;
    String dataQuality;
    Integer masterId;
    String notes;
    String released;
    String status;
    String title;
    Integer year;
    String dateChanged;
    List<ReleaseArtist> artists;
    List<ReleaseExtraArtist> extraartists;
    List<ReleaseCompany> companies;
    List<String> genres;
    List<String> styles;
    List<ReleaseLabel> labels;
    List<Track> tracklist;
    List<ReleaseVideo> videos;
    List<ReleaseFormat> formats;
    URI uri;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createReleaseResourceUrl(getId());
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class ReleaseArtist extends TrackArtist {

    String tracks;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class ReleaseExtraArtist extends ReleaseArtist {

    String role;
  }

  @Data
  public static class TrackArtist {

    Integer id;
    String anv;
    String join;
    String name;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createArtistResourceUrl(getId());
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class TrackExtraArtist extends TrackArtist {

    String role;
  }

  @Data
  public static class ReleaseCompany {

    Integer id;
    String name;
    String catno;
    Integer entityType;
    String entityTypeName;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createArtistResourceUrl(getId());
    }
  }

  @Data
  public static class ReleaseFormat {

    String name;
    String qty;
    String text;
  }

  @Data
  public static class ReleaseLabel {

    Integer id;
    String name;
    String catno;
    Integer entityType;
    String entityTypeName;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createArtistResourceUrl(getId());
    }
  }

  @Data
  public static class ReleaseVideo {

    String url;
    String title;
    String description;
    String duration;
    Boolean embed;
  }

  @Data
  public static class Track {

    String title;
    String duration;
    String position;
    List<TrackArtist> artists;
    List<TrackExtraArtist> extraartists;
  }

  @Data
  public static class Label {

    Integer id;
    String contactInfo;
    String dataQuality;
    String name;
    String profile;
    List<SubLabel> sublabels;
    List<String> urls;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createLabelResourceUrl(getId());
    }
  }

  @Data
  public static class SubLabel {

    Integer id;
    String name;
  }

  @Data
  public static class Master {

    Integer id;
    Integer mainReleaseId;
  }

  @Data
  public static class MasterVersion {

    Integer id;
    String status;
    String thumb;
    String format;
    String country;
    String title;
    String label;
    String released;
    List<String> majorFormats;
    String catno;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createReleaseResourceUrl(getId());
    }
  }

  @Data
  public static class LabelRelease {

    Integer id;
    String status;
    String artist;
    String catno;
    String thumb;
    String format;
    String title;
    String year;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createReleaseResourceUrl(getId());
    }
  }

  @Data
  public abstract static class ArtistAssoc {

    Integer id;
    String title;
    String artist;
    Integer year;
    String role;
    String thumb;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class ArtistReleaseAssoc extends ArtistAssoc {

    final String type = "release";

    String label;
    String format;
    String status;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createReleaseResourceUrl(getId());
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class ArtistMasterAssoc extends ArtistAssoc {

    final String type = "master";

    Integer mainRelease;
    URL resourceUrl;

    public void setId(Integer id) {
      this.id = id;
      this.resourceUrl = createMasterResourceUrl(getId());
    }
  }

  @Data
  public static class ArtistAssocsPaginated {

    Pagination pagination;
    List<ArtistAssoc> releases;
  }

  @Data
  public static class MasterVersionsPaginated {

    Pagination pagination;
    List<MasterVersion> releases;
  }

  @Data
  public static class LabelReleasesPaginated {

    Pagination pagination;
    List<LabelRelease> releases;
  }

  @Data
  public static class Pagination {

    Integer perPage;
    Integer items;
    Integer page;
    Integer pages;
    PaginationUrls urls;
  }

  @Data
  public static class PaginationUrls {

    String last;
    String next;
  }

  private static URL createArtistResourceUrl(Integer artistId) {
    try {
      return new URL(String.format("%s/artists/%d", getBaseUrl(), artistId));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private static URL createReleaseResourceUrl(Integer releaseId) {
    try {
      return new URL(String.format("%s/releases/%d", getBaseUrl(), releaseId));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private static URL createLabelResourceUrl(Integer labelId) {
    try {
      return new URL(String.format("%s/labels/%d", getBaseUrl(), labelId));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private static URL createMasterResourceUrl(Integer masterId) {
    try {
      return new URL(String.format("%s/masters/%d", getBaseUrl(), masterId));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private static String getBaseUrl() {
    return "https://api.discogs.com";
  }
}
