package cz.muni.ics.perunproxyapi.ga4gh;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.perunproxyapi.ga4gh.model.JWKSetKeyStore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class providing service about JWT signing and validation.
 */
@Getter
@Setter
@ToString
@Slf4j
@NoArgsConstructor
@Component
public class JWTSigningAndValidationService {

    private String defaultSignerKeyId;
    private JWSAlgorithm defaultAlgorithm;

    private Map<String, JWK> keys = new HashMap<>();
    private Map<String, JWSSigner> signers = new HashMap<>();
    private Map<String, JWSVerifier> verifiers = new HashMap<>();

    public JWTSigningAndValidationService(@NonNull String keyStoreLocation,
                                          @NonNull String defaultSignerKeyId,
                                          @NonNull String defaultAlgorithm,
                                          @NonNull JWKSetPublishingEndpoint publishingEndpoint)
            throws MalformedURLException
    {
        JWKSetKeyStore keyStore = new JWKSetKeyStore();
        keyStore.setLocation(new FileUrlResource(keyStoreLocation));
        if (keyStore.getJwkSet() != null) {
            for (JWK key : keyStore.getKeys()) {
                String keyId = key.getKeyID();
                if (keyId != null && !keyId.isEmpty()) {
                    this.keys.put(key.getKeyID(), key);
                } else {
                    String fakeKid = UUID.randomUUID().toString();
                    this.keys.put(fakeKid, key);
                }
            }
        }
        this.defaultSignerKeyId = defaultSignerKeyId;
        this.defaultAlgorithm = JWSAlgorithm.parse(defaultAlgorithm);
        buildSignersAndVerifiers();
        publishingEndpoint.setJwtService(this);
    }

    public void signJwt(SignedJWT jwt) {
        if (getDefaultSignerKeyId() == null) {
            throw new IllegalStateException("Tried to call default signing with no default signer ID set");
        }
        JWSSigner signer = signers.get(getDefaultSignerKeyId());

        try {
            jwt.sign(signer);
        } catch (JOSEException e) {
            log.error("Failed to sign JWT, error was: ", e);
        }
    }

    public Map<String, JWK> getAllPublicKeys() {
        Map<String, JWK> pubKeys = new HashMap<>();
        for (String keyId : keys.keySet()) {
            JWK key = keys.get(keyId);
            JWK pub = key.toPublicJWK();
            if (pub != null) {
                pubKeys.put(keyId, pub);
            }
        }
        return pubKeys;
    }

    private void buildSignersAndVerifiers() {
        for (Map.Entry<String, JWK> jwkEntry : keys.entrySet()) {
            String id = jwkEntry.getKey();
            JWK jwk = jwkEntry.getValue();

            try {
                if (jwk instanceof RSAKey) {
                    if (jwk.isPrivate()) { // only add the signer if there's a private key
                        RSASSASigner signer = new RSASSASigner((RSAKey) jwk);
                        signers.put(id, signer);
                    }
                    RSASSAVerifier verifier = new RSASSAVerifier((RSAKey) jwk);
                    verifiers.put(id, verifier);
                } else if (jwk instanceof ECKey) {
                    if (jwk.isPrivate()) {
                        ECDSASigner signer = new ECDSASigner((ECKey) jwk);
                        signers.put(id, signer);
                    }
                    ECDSAVerifier verifier = new ECDSAVerifier((ECKey) jwk);
                    verifiers.put(id, verifier);
                } else if (jwk instanceof OctetSequenceKey) {
                    if (jwk.isPrivate()) {
                        MACSigner signer = new MACSigner((OctetSequenceKey) jwk);
                        signers.put(id, signer);
                    }
                    MACVerifier verifier = new MACVerifier((OctetSequenceKey) jwk);
                    verifiers.put(id, verifier);
                } else {
                    log.warn("Unknown key type: {}", jwk);
                }
            } catch (JOSEException e) {
                log.warn("Exception loading signer/verifier", e);
            }
        }

        if (defaultSignerKeyId == null && keys.size() == 1) {
            setDefaultSignerKeyId(keys.keySet().iterator().next());
        }
    }

}
