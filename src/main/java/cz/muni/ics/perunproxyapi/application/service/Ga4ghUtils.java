package cz.muni.ics.perunproxyapi.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import cz.muni.ics.perunproxyapi.application.service.ga4gh.DefaultJWTSigningAndValidationService;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.MissingOrInvalidFileException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.*;
import cz.muni.ics.perunproxyapi.presentation.rest.controllers.JWKSetPublishingEndpoint;
import cz.muni.ics.perunproxyapi.presentation.rest.interceptors.AddHeaderInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileUrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A class providing static methods used for getting GA4GH passport.
 */
@Slf4j
public class Ga4ghUtils {

    public static void addAffiliationAndRoles(long now, ArrayNode passport, List<Affiliation> affiliations, Ga4ghAttributes attrs, Long userId, FullAdapter adapter) throws PerunUnknownException, PerunConnectionException, URISyntaxException, NoSuchAlgorithmException, FileNotFoundException, MalformedURLException, InvalidKeySpecException {
        for (Affiliation affiliation : affiliations) {
            long expires = Instant.ofEpochSecond(affiliation.getAsserted()).atZone(ZoneId.systemDefault()).plusYears(1L).toEpochSecond();
            if (expires < now) continue;
            JsonNode visa = createPassportVisa("AffiliationAndRole", affiliation.getValue(), affiliation.getSource(), "system", affiliation.getAsserted(), expires, null, userId, attrs);
            if (visa != null) {
                passport.add(visa);
            }
        }
    }

    public static void addAcceptedTermsAndPolicies(long now, ArrayNode passport, FullAdapter adapter, Long userId, Ga4ghAttributes attrs) throws PerunUnknownException, PerunConnectionException, URISyntaxException, NoSuchAlgorithmException, FileNotFoundException, MalformedURLException, InvalidKeySpecException {
        String BONA_FIDE_URL = "https://doi.org/10.1038/s41431-018-0219-y";
        String ELIXIR_ORG_URL = "https://elixir-europe.org/";

        boolean userInGroup = adapter.isUserInGroup(userId, 10432L);

        if (userInGroup) {
            PerunAttribute bonaFideStatus = adapter.getAttribute(Entity.USER, userId, attrs.getBonaFideStatus());
            String valueCreatedAt = bonaFideStatus.getValueCreatedAt();
            long asserted;
            if (valueCreatedAt != null) {
                asserted = Timestamp.valueOf(valueCreatedAt).getTime() / 1000L;
            } else {
                asserted = System.currentTimeMillis() / 1000L;
            }
            long expires = Instant.ofEpochSecond(asserted).atZone(ZoneId.systemDefault()).plusYears(100L).toEpochSecond();
            if (expires < now) return;
            JsonNode visa = createPassportVisa("AcceptedTermsAndPolicies", BONA_FIDE_URL, ELIXIR_ORG_URL, "self", asserted, expires, null, userId, attrs);
            if (visa != null) {
                passport.add(visa);
            }
        }
    }

    public static void addResearcherStatuses(long now, ArrayNode passport, List<Affiliation> affiliations, FullAdapter adapter, Long userId, Ga4ghAttributes attrs) throws PerunUnknownException, PerunConnectionException, URISyntaxException, NoSuchAlgorithmException, FileNotFoundException, MalformedURLException, InvalidKeySpecException {
        String BONA_FIDE_URL = "https://doi.org/10.1038/s41431-018-0219-y";
        String ELIXIR_ORG_URL = "https://elixir-europe.org/";

        PerunAttribute elixirBonaFideStatusREMS = adapter.getAttribute(Entity.USER, userId, attrs.getElixirBonaFideStatusREMS());
        String valueCreatedAt = elixirBonaFideStatusREMS.getValueCreatedAt();

        if (valueCreatedAt != null) {
            long asserted = Timestamp.valueOf(valueCreatedAt).getTime() / 1000L;
            long expires = ZonedDateTime.now().plusYears(1L).toEpochSecond();
            if (expires > now) {
                JsonNode visa = createPassportVisa("ResearcherStatus", BONA_FIDE_URL, ELIXIR_ORG_URL, "peer", asserted, expires, null, userId, attrs);
                if (visa != null) {
                    passport.add(visa);
                }
            }
        }

        for (Affiliation affiliation : affiliations) {
            if (affiliation.getValue().startsWith("faculty@")) {
                long expires = Instant.ofEpochSecond(affiliation.getAsserted()).atZone(ZoneId.systemDefault()).plusYears(1L).toEpochSecond();
                if (expires < now) continue;
                JsonNode visa = createPassportVisa("ResearcherStatus", BONA_FIDE_URL, affiliation.getSource(), "system", affiliation.getAsserted(), expires, null, userId, attrs);
                if (visa != null) {
                    passport.add(visa);
                }
            }
        }

        for (Affiliation affiliation : adapter.getGroupAffiliations(userId, attrs.getGroupAffiliations())) {
            if (affiliation.getValue().startsWith("faculty@")) {
                long expires = ZonedDateTime.now().plusYears(1L).toEpochSecond();
                JsonNode visa = createPassportVisa("ResearcherStatus", BONA_FIDE_URL, ELIXIR_ORG_URL, "so", affiliation.getAsserted(), expires, null, userId, attrs);
                if (visa != null) {
                    passport.add(visa);
                }
            }
        }
    }

    public static void addControlledAccessGrants(long now, ArrayNode passport, List<ClaimRepository> claimRepositories, Map<URI, String> signers, Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets, Long userId, Ga4ghAttributes attrs) throws PerunUnknownException, PerunConnectionException, URISyntaxException, NoSuchAlgorithmException, FileNotFoundException, MalformedURLException, InvalidKeySpecException {
        Set<String> linkedIdentities = new HashSet<>();

        for (ClaimRepository repo : claimRepositories) {
            callPermissionsJwtAPI(repo, Collections.singletonMap(attrs.getElixirId(), attrs.getSub()), passport, linkedIdentities, signers, remoteJwkSets);
        }
        if (!linkedIdentities.isEmpty()) {
            for (String linkedIdentity : linkedIdentities) {
                JsonNode visa = createPassportVisa("LinkedIdentities", linkedIdentity, attrs.getElixirOrgUrl(), "system", now, now + 3600L * 24 * 365, null, userId, attrs);
                if (visa != null) {
                    passport.add(visa);
                }
            }
        }
    }

    public static void fillClaimRepositoriesAndRemoteJWKSetsAndSigners(String path,
                                                                       List<ClaimRepository> claimRepositories,
                                                                       Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets,
                                                                       Map<URI, String> signers) throws MissingOrInvalidFileException {
        try {
            YAMLMapper mapper = new YAMLMapper();
            JsonNode root = mapper.readValue(new File(path), JsonNode.class);

            for (JsonNode repo : root.path("repos")) {
                String name = repo.path("name").asText();
                String actionURL = repo.path("url").asText();
                String authHeader = repo.path("auth_header").asText();
                String authValue = repo.path("auth_value").asText();
                if (actionURL == null || authHeader == null || authValue == null) {
                    log.error("claim repository {} not defined with url|auth_header|auth_value ", repo);
                    continue;
                }
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.setRequestFactory(
                        new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(),
                                Collections.singletonList(new AddHeaderInterceptor(authHeader, authValue)))
                );
                claimRepositories.add(new ClaimRepository(name, restTemplate, actionURL));
                log.info("GA4GH Claims Repository " + name + " configured at " + actionURL);
            }

            for (JsonNode signer : root.path("signers")) {
                String name = signer.path("name").asText();
                String jwks = signer.path("jwks").asText();
                try {
                    URL jku = new URL(jwks);
                    remoteJwkSets.put(jku.toURI(), new RemoteJWKSet<>(jku));
                    signers.put(jku.toURI(), name);
                    log.info("JWKS Signer " + name + " added with keys " + jwks);
                } catch (MalformedURLException | URISyntaxException e) {
                    log.error("cannot add to RemoteJWKSet map: " + name + " " + jwks, e);
                }
            }
        } catch (IOException ex) {
            log.error("cannot read GA4GH config file", ex);
            throw new MissingOrInvalidFileException("Cannot read GA4GH config file.");
        }
    }

    private static JsonNode createPassportVisa(String type, String value, String source, String by, long asserted, long expires, JsonNode condition, Long userId, Ga4ghAttributes attrs) throws URISyntaxException, InvalidKeySpecException, MalformedURLException, NoSuchAlgorithmException {
        long now = System.currentTimeMillis() / 1000L;

        if (asserted > now) {
            log.warn("visa asserted in future ! perunUserId {} sub {} type {} value {} source {} by {} asserted {}", userId, attrs.getSub(), type, value, source, by, Instant.ofEpochSecond(asserted));
            return null;
        }

        if (expires <= now) {
            log.warn("visa already expired ! perunUserId {} sub {} type {} value {} source {} by {} expired {}", userId, attrs.getSub(), type, value, source, by, Instant.ofEpochSecond(expires));
            return null;
        }

        URI jku = new URI(attrs.getIssuer() + JWKSetPublishingEndpoint.URL);

        JWKSetKeyStore keyStore = new JWKSetKeyStore();
        keyStore.setLocation(new FileUrlResource(attrs.getKeystore()));

        DefaultJWTSigningAndValidationService jwt = new DefaultJWTSigningAndValidationService(keyStore);
        jwt.setDefaultSignerKeyId(attrs.getDefaultSignerKeyId());
        jwt.setDefaultSigningAlgorithmName(attrs.getDefaultSigningAlgorithmName());


        Map<String, Object> passportVisaObject = new HashMap<>();
        passportVisaObject.put("type", type);
        passportVisaObject.put("asserted", asserted);
        passportVisaObject.put("value", value);
        passportVisaObject.put("source", source);
        passportVisaObject.put("by", by);

        if (condition != null && !condition.isNull() && !condition.isMissingNode()) {
            passportVisaObject.put("condition", condition);
        }

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(jwt.getDefaultAlgorithm().getName()))
                .keyID(jwt.getDefaultSignerKeyId())
                .type(JOSEObjectType.JWT)
                .jwkURL(jku)
                .build();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .issuer(attrs.getIssuer())
                .issueTime(new Date())
                .expirationTime(new Date(expires * 1000L))
                .subject(attrs.getSub())
                .jwtID(UUID.randomUUID().toString())
                .claim("ga4gh_visa_v1", passportVisaObject)
                .build();

        SignedJWT myToken = new SignedJWT(jwsHeader, jwtClaimsSet);
        jwt.signJwt(myToken);

        return JsonNodeFactory.instance.textNode(myToken.serialize());
    }

    private static void callPermissionsJwtAPI(ClaimRepository repo, Map<String, String> uriVariables, ArrayNode passport, Set<String> linkedIdentities, Map<URI, String> signers, Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets) {
        String GA4GH_CLAIM = "ga4gh_passport_v1";

        JsonNode response = callHttpJsonAPI(repo, uriVariables);
        if (response != null) {
            JsonNode visas = response.path(GA4GH_CLAIM);
            if (visas.isArray()) {
                for (JsonNode visaNode : visas) {
                    if (visaNode.isTextual()) {
                        PassportVisa visa = parseAndVerifyVisa(visaNode.asText(), signers, remoteJwkSets);
                        if (visa.isVerified()) {
                            log.debug("adding a visa to passport: {}", visa);
                            passport.add(passport.textNode(visa.getJwt()));
                            linkedIdentities.add(visa.getLinkedIdentity());
                        } else {
                            log.warn("skipping visa: {}", visa);
                        }
                    } else {
                        log.warn("element of ga4gh_passport_v1 is not a String: {}", visaNode);
                    }
                }
            } else {
                log.warn("ga4gh_passport_v1 is not an array in {}", response);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private static JsonNode callHttpJsonAPI(ClaimRepository repo, Map<String, String> uriVariables) {
        //get permissions data
        try {
            JsonNode result;
            //make the call
            try {
                if (log.isDebugEnabled()) {
                    log.debug("calling Permissions API at {}", repo.getRestTemplate().getUriTemplateHandler().expand(repo.getActionURL(), uriVariables));
                }
                result = repo.getRestTemplate().getForObject(repo.getActionURL(), JsonNode.class, uriVariables);
            } catch (HttpClientErrorException ex) {
                MediaType contentType = ex.getResponseHeaders().getContentType();
                String body = ex.getResponseBodyAsString();
                log.error("HTTP ERROR " + ex.getRawStatusCode() + " URL " + repo.getActionURL() + " Content-Type: " + contentType);
                if (ex.getRawStatusCode() == 404) {
                    log.warn("Got status 404 from Permissions endpoint {}, ELIXIR AAI user is not linked to user at Permissions API", repo.getActionURL());
                    return null;
                }
                if ("json".equals(contentType.getSubtype())) {
                    try {
                        log.error(new ObjectMapper().readValue(body, JsonNode.class).path("message").asText());
                    } catch (IOException e) {
                        log.error("cannot parse error message from JSON", e);
                    }
                } else {
                    log.error("cannot make REST call, exception: {} message: {}", ex.getClass().getName(), ex.getMessage());
                }
                return null;
            }
            log.debug("Permissions API response: {}", result);
            return result;
        } catch (Exception ex) {
            log.error("Cannot get dataset permissions", ex);
        }
        return null;
    }

    private static PassportVisa parseAndVerifyVisa(String jwtString, Map<URI, String> signers, Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets) {
        PassportVisa visa = new PassportVisa(jwtString);
        try {
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(jwtString);
            URI jku = signedJWT.getHeader().getJWKURL();

            if (jku == null) {
                log.error("JKU is missing in JWT header");
                return visa;
            }

            visa.setSigner(signers.get(jku));
            RemoteJWKSet<SecurityContext> remoteJWKSet = remoteJwkSets.get(jku);

            if (remoteJWKSet == null) {
                log.error("JKU {} is not among trusted key sets", jku);
                return visa;
            }

            List<JWK> keys = remoteJWKSet.get(new JWKSelector(new JWKMatcher.Builder().keyID(signedJWT.getHeader().getKeyID()).build()), null);
            RSASSAVerifier verifier = new RSASSAVerifier(((RSAKey) keys.get(0)).toRSAPublicKey());
            visa.setVerified(signedJWT.verify(verifier));

            if (visa.isVerified()) {
                processPayload(visa, signedJWT.getPayload());
            }
        } catch (Exception ex) {
            log.error("visa " + jwtString + " cannot be parsed and verified", ex);
        }
        return visa;
    }

    private static void processPayload(PassportVisa visa, Payload payload) throws IOException {
        ObjectMapper JSON_MAPPER = new ObjectMapper();

        JsonNode doc = JSON_MAPPER.readValue(payload.toString(), JsonNode.class);
        checkVisaKey(visa, doc, "sub");
        checkVisaKey(visa, doc, "exp");
        checkVisaKey(visa, doc, "iss");
        JsonNode visa_v1 = doc.path("ga4gh_visa_v1");
        checkVisaKey(visa, visa_v1, "type");
        checkVisaKey(visa, visa_v1, "asserted");
        checkVisaKey(visa, visa_v1, "value");
        checkVisaKey(visa, visa_v1, "source");
        checkVisaKey(visa, visa_v1, "by");
        if (!visa.isVerified()) return;
        long exp = doc.get("exp").asLong();
        if (exp < Instant.now().getEpochSecond()) {
            log.warn("visa expired on " + isoDateTime(exp));
            visa.setVerified(false);
            return;
        }
        visa.setLinkedIdentity(URLEncoder.encode(doc.get("sub").asText(), "utf-8") + "," + URLEncoder.encode(doc.get("iss").asText(), "utf-8"));
        visa.setPrettyPayload(
                visa_v1.get("type").asText() + ":  \"" + visa_v1.get("value").asText() + "\" asserted " + isoDate(visa_v1.get("asserted").asLong())
        );
    }

    private static void checkVisaKey(PassportVisa visa, JsonNode jsonNode, String key) {
        if (jsonNode.path(key).isMissingNode()) {
            log.warn(key + " is missing");
            visa.setVerified(false);
        } else {
            switch (key) {
                case "sub":
                    visa.setSub(jsonNode.path(key).asText());
                    break;
                case "iss":
                    visa.setIss(jsonNode.path(key).asText());
                    break;
                case "type":
                    visa.setType(jsonNode.path(key).asText());
                    break;
                case "value":
                    visa.setValue(jsonNode.path(key).asText());
                    break;
            }
        }
    }

    private static String isoDateTime(long linuxTime) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(linuxTime), ZoneId.systemDefault()));
    }

    private static String isoDate(long linuxTime) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(linuxTime), ZoneId.systemDefault()));
    }

}
