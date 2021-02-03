package cz.muni.ics.perunproxyapi.ga4gh.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InternalErrorException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * External repository for claims.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class ClaimRepository {

    @NonNull private String name;
    @NonNull @JsonAlias({"actionUrl", "action_url"}) private String actionUrl;
    @NonNull private List<Header> headers = new ArrayList<>();

    public ClaimRepository(@NonNull String name, @NonNull String actionUrl, @NonNull List<Header> headers) {
        setName(name);
        setActionUrl(actionUrl);
        setHeaders(headers);
    }

    public void setName(@NonNull String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Claim repository cannot have empty name");
        }
        this.name = name;
    }

    public void setActionUrl(String actionUrl) {
        if (!StringUtils.hasText(actionUrl)) {
            throw new IllegalArgumentException("Claim repository cannot have empty actionUrl");
        }
        this.actionUrl = actionUrl;
    }

    public void setHeaders(List<Header> headers) {
        if (headers != null) {
            this.headers.addAll(headers);
        }
        if (this.headers.isEmpty()) {
            throw new InternalErrorException("Claim repository cannot have empty list of request headers");
        }
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        @NonNull private String header;
        @NonNull private String value;
    }

}
