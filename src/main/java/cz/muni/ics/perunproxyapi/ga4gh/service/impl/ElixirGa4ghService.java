package cz.muni.ics.perunproxyapi.ga4gh.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
import cz.muni.ics.perunproxyapi.ga4gh.JWKSetPublishingEndpoint;
import cz.muni.ics.perunproxyapi.ga4gh.JWTSigningAndValidationService;
import cz.muni.ics.perunproxyapi.ga4gh.model.ActiveClaimRepository;
import cz.muni.ics.perunproxyapi.ga4gh.model.ClaimRepository;
import cz.muni.ics.perunproxyapi.ga4gh.model.config.ElixirGa4ghConfig;
import cz.muni.ics.perunproxyapi.ga4gh.model.PassportVisa;
import cz.muni.ics.perunproxyapi.ga4gh.model.Signer;
import cz.muni.ics.perunproxyapi.ga4gh.service.Ga4ghService;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InternalErrorException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Affiliation;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of GA4GH for ELIXIR.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class ElixirGa4ghService implements Ga4ghService {

    private static final String ACCEPTED_TERMS_AND_POLICIES = "AcceptedTermsAndPolicies";
    private static final String RESEARCHER_STATUS = "ResearcherStatus";
    private static final String AFFILIATION_AND_ROLE = "AffiliationAndRole";
    private static final String LINKED_IDENTITIES = "LinkedIdentities";

    private static final String SELF = "self";
    private static final String PEER = "peer";
    private static final String BONA_FIDE_URL = "https://doi.org/10.1038/s41431-018-0219-y";
    private static final String ELIXIR_ORG_URL = "https://elixir-europe.org/";
    private static final String FACULTY_AT = "faculty@";
    private static final String SYSTEM = "system";
    private static final String SO = "so";
    private static final String TYPE = "type";
    private static final String VALUE = "value";
    private static final String ASSERTED = "asserted";
    private static final String BY = "by";
    private static final String SOURCE = "source";
    private static final String SUB = "sub";
    private static final String ISS = "iss";
    private static final String CONDITION = "condition";
    private static final String GA4GH_VISA_V1 = "ga4gh_visa_v1";
    private static final String GA4GH_CLAIM = "ga4gh_passport_v1";
    private static final String EXP = "exp";

    //variables

    private final ElixirGa4ghConfig config;
    private final List<ActiveClaimRepository> claimRepositories = new LinkedList<>();
    private final Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets = new HashMap<>();
    private final Map<URI, String> signers = new HashMap<>();
    private final JWTSigningAndValidationService jwt;

    public ElixirGa4ghService(@NonNull ElixirGa4ghConfig config, JWKSetPublishingEndpoint publishingEndpoint)
            throws MalformedURLException
    {
        this.config = config;
        this.initializeClaimRepositories(config.getRepositories());
        this.initializeJwkSetsAndSigners(config.getSigners());
        this.jwt = new JWTSigningAndValidationService(config.getKeystore(), config.getDefaultSignerKeyId(),
                config.getDefaultSigningAlgorithmName(), publishingEndpoint);
    }

    @Override
    public JsonNode getPassportsAndVisas(@NonNull FullAdapter adapter, @NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException
    {
        List<Affiliation> affiliations = adapter.getUserExtSourcesAffiliations(userId, config.getAffiliation(),
                config.getOrgUrl());
        PerunAttributeValue sub = adapter.getAttributeValue(Entity.USER, userId, config.getSub());

        if (sub == null || sub.valueAsString() == null) {
            throw new RuntimeException("Could not fetch sub for user");
        }
        String subject = sub.valueAsString();

        ArrayNode ga4gh_passport_v1 = JsonNodeFactory.instance.arrayNode();
        long now = Instant.now().getEpochSecond();

        try {
            addAffiliationAndRoles(now, ga4gh_passport_v1, affiliations, userId, subject);
            addAcceptedTermsAndPolicies(now, ga4gh_passport_v1, adapter, userId, subject);
            addResearcherStatuses(now, ga4gh_passport_v1, affiliations, adapter, userId, subject);
            addControlledAccessGrants(now, ga4gh_passport_v1, userId, subject);
        } catch (URISyntaxException e) {
            log.error("Failure when generating GA4GH Passports and visas {}", e.getMessage(), e);
            throw new InternalErrorException("Could not get GA4GH Passports and Visas");
        }
        return ga4gh_passport_v1;
    }

    private void addAffiliationAndRoles(long now, ArrayNode passport, List<Affiliation> affiliations,
                                       Long userId, String subject)
            throws URISyntaxException
    {
        if (affiliations == null || affiliations.isEmpty()) {
            return;
        }

        for (Affiliation affiliation : affiliations) {
            long expires = getExpiration(affiliation.getAsserted(), 1L);
            if (expires >= now) {
                addVisa(passport, AFFILIATION_AND_ROLE, affiliation.getValue(), affiliation.getSource(), SYSTEM,
                        affiliation.getAsserted(), expires, null, userId, subject);
            }
        }
    }

    private void addAcceptedTermsAndPolicies(long now, ArrayNode passport, FullAdapter adapter, Long userId,
                                            String subject)
            throws PerunUnknownException, PerunConnectionException, URISyntaxException
    {
        if (adapter.isUserInGroup(userId, config.getGroupId())) {
            long asserted;
            PerunAttribute bonaFideStatus = adapter.getAttribute(Entity.USER, userId, config.getBonaFideStatus());
            if (bonaFideStatus != null && bonaFideStatus.getValueCreatedAt() != null) {
                asserted = Timestamp.valueOf(bonaFideStatus.getValueCreatedAt()).getTime() / 1000L;
            } else {
                asserted = System.currentTimeMillis() / 1000L;
            }
            long expires = getExpiration(asserted, 100L);
            if (expires >= now) {
                addVisa(passport, ACCEPTED_TERMS_AND_POLICIES, BONA_FIDE_URL, ELIXIR_ORG_URL, SELF,
                        asserted, expires, null, userId, subject);
            }
        }
    }

    private void addResearcherStatuses(long now, ArrayNode passport, List<Affiliation> affiliations,
                                      FullAdapter adapter, Long userId, String subject)
            throws PerunUnknownException, PerunConnectionException, URISyntaxException
    {
        PerunAttribute elixirBonaFideStatusREMS = adapter.getAttribute(Entity.USER,
                userId, config.getElixirBonaFideStatusREMS());

        if (elixirBonaFideStatusREMS != null && elixirBonaFideStatusREMS.getValueCreatedAt() != null) {
            long asserted = Timestamp.valueOf(elixirBonaFideStatusREMS.getValueCreatedAt()).getTime() / 1000L;
            long expires = ZonedDateTime.now().plusYears(1L).toEpochSecond();
            if (expires > now) {
                addVisa(passport, RESEARCHER_STATUS, BONA_FIDE_URL, ELIXIR_ORG_URL, PEER,
                        asserted, expires, null, userId, subject);
            }
        }

        if (affiliations != null) {
            for (Affiliation affiliation : affiliations) {
                if (affiliation != null && affiliation.getValue() != null &&
                        affiliation.getValue().startsWith(FACULTY_AT)) {
                    long expires = getExpiration(affiliation.getAsserted(), 1L);
                    if (expires >= now) {
                        addVisa(passport, RESEARCHER_STATUS, BONA_FIDE_URL, affiliation.getSource(), SYSTEM,
                                affiliation.getAsserted(), expires, null, userId, subject);
                    }
                }
            }
        }

        for (Affiliation affiliation : adapter.getGroupAffiliations(userId, config.getGroupAffiliations())) {
            if (affiliation.getValue().startsWith(FACULTY_AT)) {
                long expires = ZonedDateTime.now().plusYears(1L).toEpochSecond();
                addVisa(passport, RESEARCHER_STATUS, BONA_FIDE_URL, ELIXIR_ORG_URL, SO,
                        affiliation.getAsserted(), expires, null, userId, subject);
            }
        }
    }

    private void addControlledAccessGrants(long now, ArrayNode passport, Long userId, String subject)
            throws URISyntaxException
    {
        Set<String> linkedIdentities = new HashSet<>();

        for (ActiveClaimRepository repo : claimRepositories) {
            callPermissionsJwtAPI(repo, Collections.singletonMap(config.getElixirId(), subject), passport,
                    linkedIdentities, signers, remoteJwkSets);
        }
        if (linkedIdentities.isEmpty()) {
            return;
        }
        for (String linkedIdentity : linkedIdentities) {
            addVisa(passport, LINKED_IDENTITIES, linkedIdentity, config.getElixirOrgUrl(),
                    SYSTEM, now, getExpiration(now, 1L), null, userId, subject);
        }
    }

    private JsonNode createPassportVisa(String type, String value, String source, String by, long asserted,
                                               long expires, JsonNode condition, Long userId, String subject)
            throws URISyntaxException
    {
        long now = System.currentTimeMillis() / 1000L;

        if (asserted > now) {
            log.warn("visa asserted in future ! perunUserId {} sub {} type {} value {} source {} by {} asserted {}",
                    userId, subject, type, value, source, by, Instant.ofEpochSecond(asserted));
            return null;
        }

        if (expires <= now) {
            log.warn("visa already expired ! perunUserId {} sub {} type {} value {} source {} by {} expired {}",
                    userId, subject, type, value, source, by, Instant.ofEpochSecond(expires));
            return null;
        }

        URI jku = new URI(config.getIssuer() + JWKSetPublishingEndpoint.URL);

        Map<String, Object> passportVisaObject = new HashMap<>();
        passportVisaObject.put(TYPE, type);
        passportVisaObject.put(ASSERTED, asserted);
        passportVisaObject.put(VALUE, value);
        passportVisaObject.put(SOURCE, source);
        passportVisaObject.put(BY, by);

        if (condition != null && !condition.isNull() && !condition.isMissingNode()) {
            passportVisaObject.put(CONDITION, condition);
        }

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(jwt.getDefaultAlgorithm().getName()))
                .keyID(jwt.getDefaultSignerKeyId())
                .type(JOSEObjectType.JWT)
                .jwkURL(jku)
                .build();

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .issuer(config.getIssuer())
                .issueTime(new Date())
                .expirationTime(new Date(expires * 1000L))
                .subject(subject)
                .jwtID(UUID.randomUUID().toString())
                .claim(GA4GH_VISA_V1, passportVisaObject)
                .build();

        SignedJWT myToken = new SignedJWT(jwsHeader, jwtClaimsSet);
        jwt.signJwt(myToken);

        return JsonNodeFactory.instance.textNode(myToken.serialize());
    }

    private void callPermissionsJwtAPI(ActiveClaimRepository repo, Map<String, String> uriVariables,
                                       ArrayNode passport, Set<String> linkedIdentities,
                                       Map<URI, String> signers,
                                       Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets)
    {
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
    private JsonNode callHttpJsonAPI(ActiveClaimRepository repo, Map<String, String> uriVariables) {
        try {
            JsonNode result;
            try {
                log.debug("calling Permissions API on repo {}", repo);
                result = repo.getForObject(JsonNode.class, uriVariables);
            } catch (HttpClientErrorException ex) {
                MediaType contentType = null;
                if (ex.getResponseHeaders() != null) {
                    contentType = ex.getResponseHeaders().getContentType();
                }
                String body = ex.getResponseBodyAsString();
                log.error("HTTP ERROR {} URL {} Content-Type: {}", ex.getRawStatusCode(), repo.getActionUrl(), contentType);
                if (ex.getRawStatusCode() == 404) {
                    log.warn("Got status 404 from Permissions endpoint {}, ELIXIR AAI user is not linked to user " +
                            "at Permissions API", repo.getActionUrl());
                    return null;
                }
                if (contentType != null) {
                    if ("json".equals(contentType.getSubtype())) {
                        try {
                            log.error(new ObjectMapper().readValue(body, JsonNode.class).path("message").asText());
                        } catch (IOException e) {
                            log.error("cannot parse error message from JSON", e);
                        }
                    } else {
                        log.error("cannot make REST call, exception: {} message: {}",
                                ex.getClass().getName(), ex.getMessage());
                    }
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

    private PassportVisa parseAndVerifyVisa(String jwtString, Map<URI, String> signers,
                                                   Map<URI, RemoteJWKSet<SecurityContext>> remoteJwkSets)
    {
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

            List<JWK> keys = remoteJWKSet.get(new JWKSelector(
                    new JWKMatcher.Builder().keyID(signedJWT.getHeader().getKeyID()).build()), null);
            RSASSAVerifier verifier = new RSASSAVerifier(((RSAKey) keys.get(0)).toRSAPublicKey());
            visa.setVerified(signedJWT.verify(verifier));

            if (visa.isVerified()) {
                processPayload(visa, signedJWT.getPayload());
            }
        } catch (Exception ex) {
            log.error("visa {} cannot be parsed and verified", jwtString, ex);
        }
        return visa;
    }

    private void processPayload(PassportVisa visa, Payload payload) throws IOException {
        ObjectMapper JSON_MAPPER = new ObjectMapper();

        JsonNode doc = JSON_MAPPER.readValue(payload.toString(), JsonNode.class);
        checkVisaKey(visa, doc, SUB);
        checkVisaKey(visa, doc, EXP);
        checkVisaKey(visa, doc, ISS);
        JsonNode visa_v1 = doc.path(GA4GH_VISA_V1);
        checkVisaKey(visa, visa_v1, TYPE);
        checkVisaKey(visa, visa_v1, ASSERTED);
        checkVisaKey(visa, visa_v1, VALUE);
        checkVisaKey(visa, visa_v1, SOURCE);
        checkVisaKey(visa, visa_v1, BY);
        if (!visa.isVerified()) return;
        long exp = doc.get(EXP).asLong();
        if (exp < Instant.now().getEpochSecond()) {
            log.warn("visa expired on {}", isoDateTime(exp));
            visa.setVerified(false);
            return;
        }
        visa.setLinkedIdentity(
                URLEncoder.encode(doc.get(SUB).asText(), StandardCharsets.UTF_8)
                        + ',' + URLEncoder.encode(doc.get(ISS).asText(), StandardCharsets.UTF_8)
        );
        visa.setPrettyPayload(
                visa_v1.get(TYPE).asText() + ":  \"" + visa_v1.get(VALUE).asText() +
                        "\" asserted " + isoDate(visa_v1.get(ASSERTED).asLong())
        );
    }

    private void checkVisaKey(PassportVisa visa, JsonNode jsonNode, String key) {
        if (jsonNode.path(key).isMissingNode()) {
            log.warn("{} is missing", key);
            visa.setVerified(false);
        } else {
            switch (key) {
                case SUB:
                    visa.setSub(jsonNode.path(key).asText());
                    break;
                case ISS:
                    visa.setIss(jsonNode.path(key).asText());
                    break;
                case TYPE:
                    visa.setType(jsonNode.path(key).asText());
                    break;
                case VALUE:
                    visa.setValue(jsonNode.path(key).asText());
                    break;
            }
        }
    }

    private String isoDateTime(long linuxTime) {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(linuxTime), ZoneId.systemDefault()));
    }

    private String isoDate(long linuxTime) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(linuxTime), ZoneId.systemDefault()));
    }

    private long getExpiration(long asserted, long plusYears) {
        return Instant.ofEpochSecond(asserted)
                .atZone(ZoneId.systemDefault())
                .plusYears(plusYears)
                .toEpochSecond();
    }

    private void addVisa(ArrayNode passport, String type, String value, String source, String by, long asserted,
                                long expires, JsonNode condition, Long userid, String subject)
            throws URISyntaxException
    {
        JsonNode visa = createPassportVisa(type, value, source, by, asserted, expires,
                condition, userid, subject);
        if (visa != null) {
            passport.add(visa);
        }
    }

    private void initializeClaimRepositories(List<ClaimRepository> repositories) {
        for (ClaimRepository repo : repositories) {
            String actionURL = repo.getActionUrl();
            String authHeader = repo.getAuthHeader();
            String authValue = repo.getAuthValue();
            if (actionURL == null || authHeader == null || authValue == null) {
                log.error("claim repository {} not defined with url|auth_header|auth_value ", repo);
                continue;
            }
            String name = repo.getName();
            claimRepositories.add(new ActiveClaimRepository(name, actionURL, authHeader, authValue));
            log.info("GA4GH Claims Repository {} configured at {}", name, actionURL);
        }
    }

    private void initializeJwkSetsAndSigners(List<Signer> signers) {
        for (Signer signer : signers) {
            String name = signer.getName();
            String jwks = signer.getJwks();
            try {
                URL jku = new URL(jwks);
                remoteJwkSets.put(jku.toURI(), new RemoteJWKSet<>(jku));
                this.signers.put(jku.toURI(), name);
                log.info("JWKS Signer {} added with keys {}", name, jwks);
            } catch (MalformedURLException | URISyntaxException e) {
                log.error("cannot add to RemoteJWKSet map: {} {} ", name, jwks, e);
            }
        }
    }

}
