package cz.muni.ics.perunproxyapi.ga4gh.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class representing JWK signer for GA4GH Passport and Visas.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Signer {

    private String name;
    private String jwks;

}
