package de.tsearch.clipcollector.service.twitch.entity.clip;

import de.tsearch.clipcollector.service.twitch.entity.Pagination;
import lombok.Data;

import java.util.List;

@Data
public class ClipsWrapper {
    private List<Clip> data;
    private Pagination pagination;
}
