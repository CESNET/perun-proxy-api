package cz.muni.ics.perunproxyapi.ga4gh.model;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.List;

/**
 * A class represents JWK set key store.
 */
@NoArgsConstructor
public class JWKSetKeyStore {

    @Getter
    private JWKSet jwkSet;

    @Getter
    private Resource location;

    public JWKSetKeyStore(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
        initializeJwkSet();
    }

    private void initializeJwkSet() {
        if (jwkSet == null) {
            if (location != null) {
                if (location.exists() && location.isReadable()) {
                    try {
                        String s = Files.readString(location.getFile().toPath(), StandardCharsets.UTF_8);
                        jwkSet = JWKSet.parse(s);
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Key Set resource could not be read: " + location);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Key Set resource could not be parsed: " + location);
                    }
                } else {
                    throw new IllegalArgumentException("Key Set resource could not be read: " + location);
                }
            } else {
                throw new IllegalArgumentException("Key store must be initialized with at least one of a jwkSet " +
                        "or a location.");
            }
        }
    }

    public void setJwkSet(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
        initializeJwkSet();
    }

    public void setLocation(Resource location) {
        this.location = location;
        initializeJwkSet();
    }

    public List<JWK> getKeys() {
        if (jwkSet == null) {
            initializeJwkSet();
        }
        return jwkSet.getKeys();
    }

}
