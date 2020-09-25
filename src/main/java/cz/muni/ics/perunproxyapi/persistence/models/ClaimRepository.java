package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.web.client.RestTemplate;

/**
 * A class representing claim repository.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@AllArgsConstructor
public class ClaimRepository {
    @NonNull
    private final String name;

    @NonNull
    private final RestTemplate restTemplate;

    @NonNull
    private final String actionURL;

}
