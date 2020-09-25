package cz.muni.ics.perunproxyapi.application.service.ga4gh;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cz.muni.ics.perunproxyapi.persistence.models.JWKSetKeyStore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
import org.springframework.stereotype.Component;

/**
 * A class providing service about JWT signing and validation.
 */
@Slf4j
@NoArgsConstructor
@Component
public class DefaultJWTSigningAndValidationService {

    @Getter
    @Setter
    private String defaultSignerKeyId;

    @Getter
    private JWSAlgorithm defaultAlgorithm;

    private Map<String, JWK> keys = new HashMap<>();
    private Map<String, JWSSigner> signers = new HashMap<>();
    private Map<String, JWSVerifier> verifiers = new HashMap<>();

    /**
     * Build this service based on the keys given. All public keys will be used
     * to make verifiers, all private keys will be used to make signers.
     *
     * @param keys
     *            A map of key identifier to key
     *
     * @throws InvalidKeySpecException
     *             If the keys in the JWKs are not valid
     * @throws NoSuchAlgorithmException
     *             If there is no appropriate algorithm to tie the keys to.
     */
    public DefaultJWTSigningAndValidationService(Map<String, JWK> keys) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.keys = keys;
        buildSignersAndVerifiers();
    }

    /**
     * Build this service based on the given keystore. All keys must have a key
     * id ({@code kid}) field in order to be used.
     *
     * @param keyStore
     *            the keystore to load all keys from
     *
     * @throws InvalidKeySpecException
     *             If the keys in the JWKs are not valid
     * @throws NoSuchAlgorithmException
     *             If there is no appropriate algorithm to tie the keys to.
     */
    public DefaultJWTSigningAndValidationService(JWKSetKeyStore keyStore) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // convert all keys in the keystore to a map based on key id
        if (keyStore!= null && keyStore.getJwkSet() != null) {
            for (JWK key : keyStore.getKeys()) {
                String keyId = key.getKeyID();
                if (keyId != null && !keyId.isEmpty()) {
                    // use the key ID that's built into the key itself
                    this.keys.put(key.getKeyID(), key);
                } else {
                    // create a random key id
                    String fakeKid = UUID.randomUUID().toString();
                    this.keys.put(fakeKid, key);
                }
            }
        }
        buildSignersAndVerifiers();
    }

    public void setDefaultSigningAlgorithmName(String algName) {
        defaultAlgorithm = JWSAlgorithm.parse(algName);
    }

    public String getDefaultSigningAlgorithmName() {
        if (defaultAlgorithm != null) {
            return defaultAlgorithm.getName();
        } else {
            return null;
        }
    }

    /**
     * Build all of the signers and verifiers for this based on the key map.
     * @throws InvalidKeySpecException If the keys in the JWKs are not valid
     * @throws NoSuchAlgorithmException If there is no appropriate algorithm to tie the keys to.
     */
    private void buildSignersAndVerifiers() throws NoSuchAlgorithmException, InvalidKeySpecException {
        for (Map.Entry<String, JWK> jwkEntry : keys.entrySet()) {

            String id = jwkEntry.getKey();
            JWK jwk = jwkEntry.getValue();

            try {
                if (jwk instanceof RSAKey) {
                    // build RSA signers & verifiers

                    if (jwk.isPrivate()) { // only add the signer if there's a private key
                        RSASSASigner signer = new RSASSASigner((RSAKey) jwk);
                        signers.put(id, signer);
                    }

                    RSASSAVerifier verifier = new RSASSAVerifier((RSAKey) jwk);
                    verifiers.put(id, verifier);

                } else if (jwk instanceof ECKey) {
                    // build EC signers & verifiers

                    if (jwk.isPrivate()) {
                        ECDSASigner signer = new ECDSASigner((ECKey) jwk);
                        signers.put(id, signer);
                    }

                    ECDSAVerifier verifier = new ECDSAVerifier((ECKey) jwk);
                    verifiers.put(id, verifier);

                } else if (jwk instanceof OctetSequenceKey) {
                    // build HMAC signers & verifiers

                    if (jwk.isPrivate()) { // technically redundant check because all HMAC keys are private
                        MACSigner signer = new MACSigner((OctetSequenceKey) jwk);
                        signers.put(id, signer);
                    }

                    MACVerifier verifier = new MACVerifier((OctetSequenceKey) jwk);
                    verifiers.put(id, verifier);

                } else {
                    log.warn("Unknown key type: " + jwk);
                }
            } catch (JOSEException e) {
                log.warn("Exception loading signer/verifier", e);
            }
        }

        if (defaultSignerKeyId == null && keys.size() == 1) {
            // if there's only one key, it's the default
            setDefaultSignerKeyId(keys.keySet().iterator().next());
        }
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

    public void signJwt(SignedJWT jwt, JWSAlgorithm alg) {

        JWSSigner signer = null;

        for (JWSSigner s : signers.values()) {
            if (s.supportedJWSAlgorithms().contains(alg)) {
                signer = s;
                break;
            }
        }

        if (signer == null) {
            log.error("No matching algirthm found for alg=" + alg);

        }

        try {
            jwt.sign(signer);
        } catch (JOSEException e) {

            log.error("Failed to sign JWT, error was: ", e);
        }

    }

    public boolean validateSignature(SignedJWT jwt) {

        for (JWSVerifier verifier : verifiers.values()) {
            try {
                if (jwt.verify(verifier)) {
                    return true;
                }
            } catch (JOSEException e) {

                log.error("Failed to validate signature with " + verifier + " error message: " + e.getMessage());
            }
        }
        return false;
    }

    public Map<String, JWK> getAllPublicKeys() {
        Map<String, JWK> pubKeys = new HashMap<>();

        // pull all keys out of the verifiers if we know how
        for (String keyId : keys.keySet()) {
            JWK key = keys.get(keyId);
            JWK pub = key.toPublicJWK();
            if (pub != null) {
                pubKeys.put(keyId, pub);
            }
        }

        return pubKeys;
    }

    public Collection<JWSAlgorithm> getAllSigningAlgsSupported() {

        Set<JWSAlgorithm> algs = new HashSet<>();

        for (JWSSigner signer : signers.values()) {
            algs.addAll(signer.supportedJWSAlgorithms());
        }

        for (JWSVerifier verifier : verifiers.values()) {
            algs.addAll(verifier.supportedJWSAlgorithms());
        }

        return algs;

    }


}
