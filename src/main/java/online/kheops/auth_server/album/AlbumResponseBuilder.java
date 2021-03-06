package online.kheops.auth_server.album;

import online.kheops.auth_server.entity.AlbumUser;
import org.jooq.Record;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class AlbumResponseBuilder {

    private String id;
    private String name;
    private String description;
    private String[] modalities;
    private LocalDateTime createdTime;
    private LocalDateTime lastEventTime;
    private Integer numberOfUsers;
    private List<AlbumResponse.UserAlbumResponse> users;
    private Integer numberOfComments;
    private Integer numberOfStudies;
    private Boolean addUser;
    private Boolean downloadSeries;
    private Boolean sendSeries;
    private Boolean deleteSeries;
    private Boolean addSeries;
    private Boolean writeComments;
    private Boolean isFavorite;
    private Boolean notificationNewSeries;
    private Boolean notificationNewComment;
    private Boolean isAdmin;

    public AlbumResponseBuilder() {
        users = new ArrayList<>();
    }

    public AlbumResponseBuilder setAlbumFromUser(Record r) {
        this.id = r.getValue("album_id").toString();
        this.name = r.getValue("album_name").toString();
        this.description = r.getValue("album_description").toString();
        this.createdTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(((Timestamp) r.getValue("album_created_time")).getTime()), TimeZone.getDefault().toZoneId());
        this.lastEventTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(((Timestamp) r.getValue("album_last_event_time")).getTime()), TimeZone.getDefault().toZoneId());
        this.numberOfUsers = (Integer) r.getValue("number_of_users");
        this.numberOfStudies = (Integer) r.getValue("number_of_studies");

        this.addSeries = (boolean) (r.getValue("add_series_permission"));
        this.addUser = ((boolean) r.getValue("add_user_permission"));
        this.deleteSeries = ((boolean) r.getValue("delete_series_permision"));
        this.downloadSeries = ((boolean) r.getValue("download_user_permission"));
        this.sendSeries = ((boolean) r.getValue("send_series_permission"));
        this.writeComments = ((boolean) r.getValue("write_comment_permission"));
        this.numberOfComments = (Integer) r.getValue("number_of_comments");
        this.isFavorite = ((boolean) r.getValue("favorite"));
        this.notificationNewComment = ((boolean) r.getValue("new_comment_notifications"));
        this.notificationNewSeries = ((boolean) r.getValue("new_series_notifications"));
        this.isAdmin = ((boolean) r.getValue("admin"));
        if(r.getValue("modalities") != null) {
            this.modalities = r.getValue("modalities").toString().split("/");
        } else {
            this.modalities = new String[0];
        }
        return this;
    }

    public AlbumResponseBuilder setAlbumFromCapabilityToken(Record r) {
        this.id = r.getValue("album_id").toString();
        this.name = r.getValue("album_name").toString();
        this.description = r.getValue("album_description").toString();
        this.numberOfStudies = (Integer) r.getValue("number_of_studies");
        this.numberOfComments = (Integer) r.getValue("number_of_comments");
        if(r.getValue("modalities") != null) {
            this.modalities = r.getValue("modalities").toString().split("/");
        } else {
            this.modalities = new String[0];
        }
        return this;
    }

    public AlbumResponseBuilder addUser(AlbumUser albumUser) {
        AlbumResponse.UserAlbumResponse userAlbumResponse = new AlbumResponse.UserAlbumResponse();
        userAlbumResponse.userName = albumUser.getUser().getEmail();
        userAlbumResponse.isAdmin = albumUser.isAdmin();
        userAlbumResponse.userId = albumUser.getUser().getKeycloakId();
        users.add(userAlbumResponse);
        return this;
    }

    public AlbumResponse build() { return new AlbumResponse(this); }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getModalities() {
        return modalities;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getLastEventTime() {
        return lastEventTime;
    }

    public Integer getNumberOfUsers() {
        return numberOfUsers;
    }

    public List<AlbumResponse.UserAlbumResponse> getUsers() {
        return users;
    }

    public Integer getNumberOfComments() {
        return numberOfComments;
    }

    public Integer getNumberOfStudies() {
        return numberOfStudies;
    }

    public Boolean getAddUser() {
        return addUser;
    }

    public Boolean getDownloadSeries() {
        return downloadSeries;
    }

    public Boolean getSendSeries() {
        return sendSeries;
    }

    public Boolean getDeleteSeries() {
        return deleteSeries;
    }

    public Boolean getAddSeries() {
        return addSeries;
    }

    public Boolean getWriteComments() {
        return writeComments;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public Boolean getNotificationNewSeries() {
        return notificationNewSeries;
    }

    public Boolean getNotificationNewComment() {
        return notificationNewComment;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }
}
