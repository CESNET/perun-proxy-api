package cz.muni.ics.perunproxyapi.ga4gh.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
@AllArgsConstructor
public class ClaimRepository {

    private String name;
    @JsonAlias({"actionUrl", "action_url"}) private String actionUrl;
    @JsonAlias({"authHeader", "auth_header"}) private String authHeader;
    @JsonAlias({"authValue", "auth_value"}) private String authValue;

}
