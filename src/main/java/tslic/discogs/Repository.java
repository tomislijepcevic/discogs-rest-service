package tslic.discogs;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static tslic.discogs.Tables.ARTISTS;
import static tslic.discogs.Tables.ARTIST_ALIASES;
import static tslic.discogs.Tables.ARTIST_GROUPS;
import static tslic.discogs.Tables.ARTIST_MEMBERS;
import static tslic.discogs.Tables.ARTIST_NAMEVARIATIONS;
import static tslic.discogs.Tables.ARTIST_URLS;
import static tslic.discogs.Tables.LABELS;
import static tslic.discogs.Tables.LABEL_SUBLABELS;
import static tslic.discogs.Tables.LABEL_URLS;
import static tslic.discogs.Tables.MASTERS;
import static tslic.discogs.Tables.RELEASES;
import static tslic.discogs.Tables.RELEASE_ARTIST_MAPS;
import static tslic.discogs.Tables.RELEASE_COMPANIES;
import static tslic.discogs.Tables.RELEASE_EXTRA_ARTIST_MAPS;
import static tslic.discogs.Tables.RELEASE_FORMATS;
import static tslic.discogs.Tables.RELEASE_GENRES;
import static tslic.discogs.Tables.RELEASE_LABELS;
import static tslic.discogs.Tables.RELEASE_STYLES;
import static tslic.discogs.Tables.RELEASE_VIDEOS;
import static tslic.discogs.Tables.TRACKS;
import static tslic.discogs.Tables.TRACK_ARTIST_MAPS;
import static tslic.discogs.Tables.TRACK_EXTRA_ARTIST_MAPS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import tslic.discogs.Responses.Artist;
import tslic.discogs.Responses.ArtistAlias;
import tslic.discogs.Responses.ArtistAssocsPaginated;
import tslic.discogs.Responses.Group;
import tslic.discogs.Responses.GroupMember;
import tslic.discogs.Responses.Label;
import tslic.discogs.Responses.Master;
import tslic.discogs.Responses.MasterVersionsPaginated;
import tslic.discogs.Responses.Release;
import tslic.discogs.Responses.ReleaseArtist;
import tslic.discogs.Responses.ReleaseCompany;
import tslic.discogs.Responses.ReleaseExtraArtist;
import tslic.discogs.Responses.ReleaseFormat;
import tslic.discogs.Responses.ReleaseLabel;
import tslic.discogs.Responses.ReleaseVideo;
import tslic.discogs.Responses.SubLabel;
import tslic.discogs.Responses.Track;
import tslic.discogs.Responses.TrackArtist;
import tslic.discogs.Responses.TrackExtraArtist;
import tslic.discogs.tables.records.TrackArtistMapsRecord;
import tslic.discogs.tables.records.TrackExtraArtistMapsRecord;

@ApplicationScoped
public class Repository {

  private final DSLContext create;

  @Inject
  Repository(DSLContext dslContext) {
    this.create = dslContext;
  }

  public CompletionStage<Artist> findArtistById(Integer artistId) {
    var artistFuture =
        create
            .selectFrom(ARTISTS)
            .where(ARTISTS.ID.eq(artistId))
            .fetchAsync()
            .thenApply(rs -> rs.isNotEmpty() ? rs.get(0) : null);

    return artistFuture.thenCompose(
        artistsRecord -> {
          if (artistsRecord == null) {
            return CompletableFuture.completedFuture(null);
          }

          var nameVariationsFuture =
              create
                  .select(ARTIST_NAMEVARIATIONS.NAMEVARIATION)
                  .from(ARTIST_NAMEVARIATIONS)
                  .where(ARTIST_NAMEVARIATIONS.ARTIST_ID.eq(artistId))
                  .orderBy(ARTIST_NAMEVARIATIONS.OFST)
                  .fetchAsync()
                  .thenApply(r -> r.getValues(ARTIST_NAMEVARIATIONS.NAMEVARIATION))
                  .toCompletableFuture();

          var urlsFuture =
              create
                  .select(ARTIST_URLS.URL)
                  .from(ARTIST_URLS)
                  .where(ARTIST_URLS.ARTIST_ID.eq(artistId))
                  .orderBy(ARTIST_URLS.OFST)
                  .fetchAsync()
                  .thenApply(r -> r.getValues(ARTIST_URLS.URL))
                  .toCompletableFuture();

          var aliasesFuture =
              create
                  .select(ARTIST_ALIASES.ARTIST2_ID, ARTIST_ALIASES.NAME)
                  .from(ARTIST_ALIASES)
                  .where(ARTIST_ALIASES.ARTIST_ID.eq(artistId))
                  .orderBy(ARTIST_ALIASES.OFST)
                  .fetchAsync()
                  .thenApply(
                      results ->
                          results.map(
                              r -> {
                                var alias = new ArtistAlias();
                                alias.setId(r.getValue(ARTIST_ALIASES.ARTIST2_ID));
                                alias.setName(r.getValue(ARTIST_ALIASES.NAME));
                                return alias;
                              }))
                  .toCompletableFuture();

          var groupsFuture =
              create
                  .select(ARTIST_GROUPS.ARTIST2_ID, ARTIST_GROUPS.NAME)
                  .from(ARTIST_GROUPS)
                  .where(ARTIST_GROUPS.ARTIST_ID.eq(artistId))
                  .orderBy(ARTIST_GROUPS.OFST)
                  .fetchAsync()
                  .thenApply(
                      results ->
                          results.map(
                              r -> {
                                var group = new Group();
                                group.setId(r.getValue(ARTIST_GROUPS.ARTIST2_ID));
                                group.setName(r.getValue(ARTIST_GROUPS.NAME));
                                return group;
                              }))
                  .toCompletableFuture();

          var membersFuture =
              create
                  .select(ARTIST_MEMBERS.ARTIST2_ID, ARTIST_MEMBERS.NAME)
                  .from(ARTIST_MEMBERS)
                  .where(ARTIST_MEMBERS.ARTIST_ID.eq(artistId))
                  .orderBy(ARTIST_MEMBERS.OFST)
                  .fetchAsync()
                  .thenApply(
                      results ->
                          results.map(
                              r -> {
                                var member = new GroupMember();
                                member.setId(r.getValue(ARTIST_MEMBERS.ARTIST2_ID));
                                member.setName(r.getValue(ARTIST_MEMBERS.NAME));
                                return member;
                              }))
                  .toCompletableFuture();

          return CompletableFuture.allOf(
                  nameVariationsFuture, urlsFuture, aliasesFuture, groupsFuture, membersFuture)
              .thenApply(
                  (Void) -> {
                    var artist = new Artist();
                    artist.setId(artistsRecord.getId());
                    artist.setName(artistsRecord.getName());
                    artist.setRealname(artistsRecord.getRealName());
                    artist.setProfile(artistsRecord.getProfile());
                    artist.setStatus(artistsRecord.getStatus());
                    artist.setDataQuality(artistsRecord.getDataQuality());
                    artist.setNamevariations(nameVariationsFuture.join());
                    artist.setUrls(urlsFuture.join());
                    artist.setAliases(aliasesFuture.join());
                    artist.setGroups(groupsFuture.join());
                    artist.setMembers(membersFuture.join());
                    return artist;
                  });
        });
  }

  public CompletionStage<Release> findReleaseById(Integer releaseId) {
    var releaseStage =
        create
            .selectFrom(RELEASES)
            .where(RELEASES.ID.eq(releaseId))
            .fetchAsync()
            .thenApply(rs -> rs.isNotEmpty() ? rs.get(0) : null);

    return releaseStage.thenCompose(
        releaseRecord -> {
          if (releaseRecord == null) {
            return CompletableFuture.completedFuture(null);
          }

          var genresFuture =
              create
                  .select(RELEASE_GENRES.GENRE)
                  .from(RELEASE_GENRES)
                  .where(RELEASE_GENRES.RELEASE_ID.eq(releaseId))
                  .orderBy(RELEASE_GENRES.OFST)
                  .fetchAsync()
                  .thenApply(rs -> rs.map(r -> r.get(RELEASE_GENRES.GENRE)))
                  .toCompletableFuture();

          var stylesFuture =
              create
                  .select(RELEASE_STYLES.STYLE)
                  .from(RELEASE_STYLES)
                  .where(RELEASE_STYLES.RELEASE_ID.eq(releaseId))
                  .orderBy(RELEASE_STYLES.OFST)
                  .fetchAsync()
                  .thenApply(rs -> rs.map(r -> r.get(RELEASE_STYLES.STYLE)))
                  .toCompletableFuture();

          var videosFuture =
              create
                  .selectFrom(RELEASE_VIDEOS)
                  .where(RELEASE_VIDEOS.RELEASE_ID.eq(releaseId))
                  .orderBy(RELEASE_VIDEOS.OFST)
                  .fetchAsync()
                  .thenApply(
                      rs ->
                          rs.map(
                              r -> {
                                var video = new ReleaseVideo();
                                video.setUrl(r.getSrc());
                                video.setTitle(r.getTitle());
                                video.setDescription(r.getDescription());
                                video.setEmbed(r.getEmbed());
                                video.setDuration(r.getDuration());
                                return video;
                              }))
                  .toCompletableFuture();

          var formatsFuture =
              create
                  .selectFrom(RELEASE_FORMATS)
                  .where(RELEASE_FORMATS.RELEASE_ID.eq(releaseId))
                  .orderBy(RELEASE_FORMATS.OFST)
                  .fetchAsync()
                  .thenApply(
                      rs ->
                          rs.map(
                              r -> {
                                var format = new ReleaseFormat();
                                format.setName(r.getName());
                                format.setQty(r.getQty());
                                format.setText(r.getText());
                                return format;
                              }))
                  .toCompletableFuture();

          var companiesFuture =
              create
                  .selectFrom(RELEASE_COMPANIES)
                  .where(RELEASE_COMPANIES.RELEASE_ID.eq(releaseId))
                  .orderBy(RELEASE_COMPANIES.OFST)
                  .fetchAsync()
                  .thenApply(
                      rs ->
                          rs.map(
                              r -> {
                                var company = new ReleaseCompany();
                                company.setId(r.getCompanyId());
                                company.setName(r.getName());
                                company.setCatno(r.getCatno());
                                company.setEntityType(r.getEntityType());
                                company.setEntityTypeName(r.getEntityTypeName());
                                return company;
                              }))
                  .toCompletableFuture();

          var labelsFuture =
              create
                  .selectFrom(RELEASE_LABELS)
                  .where(RELEASE_LABELS.RELEASE_ID.eq(releaseId))
                  .orderBy(RELEASE_LABELS.OFST)
                  .fetchAsync()
                  .thenApply(
                      rs ->
                          rs.map(
                              r -> {
                                var label = new ReleaseLabel();
                                label.setId(r.getLabelId());
                                label.setName(r.getName());
                                label.setCatno(r.getCatno());
                                return label;
                              }))
                  .toCompletableFuture();

          var artistsFuture =
              create
                  .selectFrom(RELEASE_ARTIST_MAPS)
                  .where(RELEASE_ARTIST_MAPS.RELEASE_ID.eq(releaseId))
                  .orderBy(RELEASE_ARTIST_MAPS.OFST)
                  .fetchAsync()
                  .thenApply(
                      rs ->
                          rs.map(
                              r -> {
                                var artist = new ReleaseArtist();
                                artist.setId(r.getArtistId());
                                artist.setAnv(r.getAnv());
                                artist.setJoin(r.getJoinRelation());
                                artist.setName(r.getName());
                                artist.setTracks(r.getTracks());
                                return artist;
                              }))
                  .toCompletableFuture();

          var extraArtistsFuture =
              create
                  .selectFrom(RELEASE_EXTRA_ARTIST_MAPS)
                  .where(RELEASE_EXTRA_ARTIST_MAPS.RELEASE_ID.eq(releaseId))
                  .orderBy(RELEASE_EXTRA_ARTIST_MAPS.OFST)
                  .fetchAsync()
                  .thenApply(
                      rs ->
                          rs.map(
                              r -> {
                                var artist = new ReleaseExtraArtist();
                                artist.setId(r.getArtistId());
                                artist.setAnv(r.getAnv());
                                artist.setJoin(r.getJoinRelation());
                                artist.setName(r.getName());
                                artist.setTracks(r.getTracks());
                                artist.setRole(r.getRole());
                                return artist;
                              }))
                  .toCompletableFuture();

          var trackToArtistsFuture =
              create
                  .selectFrom(TRACK_ARTIST_MAPS)
                  .where(TRACK_ARTIST_MAPS.RELEASE_ID.eq(releaseId))
                  .orderBy(TRACK_ARTIST_MAPS.ARTIST_OFST)
                  .fetchAsync()
                  .thenApply(
                      rs ->
                          rs.stream()
                              .collect(
                                  groupingBy(
                                      TrackArtistMapsRecord::getTrackOfst,
                                      mapping(
                                          r -> {
                                            var artist = new TrackArtist();
                                            artist.setId(r.getArtistId());
                                            artist.setName(r.getName());
                                            artist.setAnv(r.getAnv());
                                            artist.setJoin(r.getJoinRelation());
                                            return artist;
                                          },
                                          toList()))))
                  .toCompletableFuture();

          var trackToExtraArtistsFuture =
              create
                  .selectFrom(TRACK_EXTRA_ARTIST_MAPS)
                  .where(TRACK_EXTRA_ARTIST_MAPS.RELEASE_ID.eq(releaseId))
                  .orderBy(TRACK_EXTRA_ARTIST_MAPS.ARTIST_OFST)
                  .fetchAsync()
                  .thenApply(
                      rs ->
                          rs.stream()
                              .collect(
                                  groupingBy(
                                      TrackExtraArtistMapsRecord::getTrackOfst,
                                      mapping(
                                          r -> {
                                            var artist = new TrackExtraArtist();
                                            artist.setId(r.getArtistId());
                                            artist.setName(r.getName());
                                            artist.setAnv(r.getAnv());
                                            artist.setJoin(r.getJoinRelation());
                                            artist.setRole(r.getRole());
                                            return artist;
                                          },
                                          toList()))))
                  .toCompletableFuture();

          var tracksDependantFutures =
              CompletableFuture.allOf(trackToArtistsFuture, trackToExtraArtistsFuture);

          var tracksFuture =
              create
                  .selectFrom(TRACKS)
                  .where(TRACKS.RELEASE_ID.eq(releaseId))
                  .orderBy(TRACKS.OFST)
                  .fetchAsync()
                  .thenCombine(
                      tracksDependantFutures,
                      (rs, Void) -> {
                        var trackToArtists = trackToArtistsFuture.join();
                        var trackToExtraArtists = trackToExtraArtistsFuture.join();

                        return rs.map(
                            r -> {
                              var track = new Track();
                              track.setTitle(r.getTitle());
                              track.setDuration(r.getDuration());
                              track.setPosition(r.getPosition());
                              track.setArtists(trackToArtists.get(r.getOfst()));
                              track.setExtraartists(trackToExtraArtists.get(r.getOfst()));
                              return track;
                            });
                      })
                  .toCompletableFuture();

          return CompletableFuture.allOf(
                  genresFuture,
                  stylesFuture,
                  videosFuture,
                  formatsFuture,
                  companiesFuture,
                  labelsFuture,
                  artistsFuture,
                  extraArtistsFuture,
                  tracksFuture)
              .thenApply(
                  Void -> {
                    var release = new Release();
                    release.setId(releaseId);
                    release.setCountry(releaseRecord.getCountry());
                    release.setDataQuality(releaseRecord.getDataQuality());
                    release.setMasterId(releaseRecord.getMasterId());
                    release.setNotes(releaseRecord.getNotes());
                    release.setReleased(releaseRecord.getReleased());
                    release.setStatus(releaseRecord.getStatus());
                    release.setTitle(releaseRecord.getTitle());
                    release.setGenres(genresFuture.join());
                    release.setStyles(stylesFuture.join());
                    release.setVideos(videosFuture.join());
                    release.setFormats(formatsFuture.join());
                    release.setCompanies(companiesFuture.join());
                    release.setLabels(labelsFuture.join());
                    release.setArtists(artistsFuture.join());
                    release.setExtraartists(extraArtistsFuture.join());
                    release.setTracklist(tracksFuture.join());

                    return release;
                  });
        });
  }

  public CompletionStage<Label> findLabelById(Integer labelId) {
    var labelFuture =
        create
            .selectFrom(LABELS)
            .where(LABELS.ID.eq(labelId))
            .fetchAsync()
            .thenApply(rs -> rs.isNotEmpty() ? rs.get(0) : null);

    return labelFuture.thenCompose(
        labelRecord -> {
          if (labelRecord == null) {
            return CompletableFuture.completedFuture(null);
          }

          var subLabelsFuture =
              create
                  .select(LABEL_SUBLABELS.LABEL2_ID, LABEL_SUBLABELS.NAME)
                  .from(LABEL_SUBLABELS)
                  .where(LABEL_SUBLABELS.LABEL_ID.eq(labelId))
                  .orderBy(LABEL_SUBLABELS.OFST)
                  .fetchAsync()
                  .thenApply(
                      rs ->
                          rs.map(
                              r -> {
                                var subLabelDto = new SubLabel();
                                subLabelDto.setId(r.get(LABEL_SUBLABELS.LABEL2_ID));
                                subLabelDto.setName(r.get(LABEL_SUBLABELS.NAME));
                                return subLabelDto;
                              }))
                  .toCompletableFuture();

          var urlsFuture =
              create
                  .select(LABEL_URLS.URL)
                  .from(LABEL_URLS)
                  .where(LABEL_URLS.LABEL_ID.eq(labelId))
                  .orderBy(LABEL_URLS.OFST)
                  .fetchAsync()
                  .thenApply(rs -> rs.map(r -> r.get(LABEL_URLS.URL)))
                  .toCompletableFuture();

          return CompletableFuture.allOf(subLabelsFuture, urlsFuture)
              .thenApply(
                  Void -> {
                    var label = new Label();
                    label.setId(labelId);
                    label.setName(labelRecord.getName());
                    label.setProfile(labelRecord.getProfile());
                    label.setDataQuality(labelRecord.getDataQuality());
                    label.setContactInfo(labelRecord.getContactInfo());
                    label.setSublabels(subLabelsFuture.join());
                    label.setUrls(urlsFuture.join());
                    return label;
                  });
        });
  }

  public CompletionStage<Master> findMasterById(Integer masterId) {
    return create
        .select(MASTERS.MAIN_RELEASE_ID)
        .from(MASTERS)
        .where(MASTERS.ID.eq(masterId))
        .fetchAsync()
        .thenApply(
            rs -> {
              if (rs.isEmpty()) {
                return null;
              }

              var r = rs.get(0);
              var master = new Master();
              master.setId(masterId);
              master.setMainReleaseId(r.getValue(MASTERS.MAIN_RELEASE_ID));
              return master;
            });
  }

  public CompletionStage<ArtistAssocsPaginated> findReleasesOfArtist(Integer artistId) {
    // TODO
    return null;
  }

  public CompletionStage<MasterVersionsPaginated> findMVersionsOfMaster(Integer masterId) {
    // TODO
    return null;
  }

  public CompletionStage<MasterVersionsPaginated> findReleasesOfLabel(Integer labelId) {
    // TODO
    return null;
  }

  public CompletionStage<Object> search() {
    // TODO
    return null;
  }

  @ApplicationScoped
  public static class DSLContextProducer {

    private DSLContext dslContext;

    @PostConstruct
    public void init() {
      String dbUser = System.getenv("DB_USER");
      String dbPass = System.getenv("DB_PASSWORD");
      String dbUrl = System.getenv("DB_URL");

      try {
        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        dslContext = DSL.using(conn);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }

    @Produces
    public DSLContext produce() {
      return dslContext;
    }

    @PreDestroy
    public void destroy() {
      dslContext.close();
    }
  }
}
