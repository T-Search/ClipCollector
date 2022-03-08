package de.tsearch.clipcollector.service.twitch.entity.clip;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class Clip {
    @JsonProperty("id")
    private String id;
    @JsonProperty("url")
    private String url;
    @JsonProperty("embed_url")
    private String embedURL;
    @JsonProperty("broadcaster_id")
    private String broadcasterID;
    @JsonProperty("broadcaster_name")
    private String broadcasterName;
    @JsonProperty("creator_id")
    private String creatorID;
    @JsonProperty("creator_name")
    private String creatorName;
    @JsonProperty("video_id")
    private String videoID;
    @JsonProperty("game_id")
    private String gameID;
    @JsonProperty("language")
    private String language;
    @JsonProperty("title")
    private String title;
    @JsonProperty("view_count")
    private long viewCount;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("thumbnail_url")
    private String thumbnailURL;
    @JsonProperty("duration")
    private double duration;
}
