package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.util.StringUtils;

/**
 * A class containing identifiers of attributes needed for successful get of GA4GH passport.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@ToString
@EqualsAndHashCode
public class Ga4ghAttributes {

    @NonNull
    private String issuer;

    @NonNull
    private String bonaFideStatus;

    @NonNull
    private String elixirBonaFideStatusREMS;

    @NonNull
    private String groupAffiliations;

    @NonNull
    private String affiliation;

    @NonNull
    private String orgUrl;

    @NonNull
    private String sub;

    @NonNull
    private String keystore;

    @NonNull
    private String defaultSignerKeyId;

    @NonNull
    private String defaultSigningAlgorithmName;

    @NonNull
    private String elixirOrgUrl;

    @NonNull
    private String elixirId;

    public Ga4ghAttributes(String issuer,
                           String bonaFideStatus,
                           String elixirBonaFideStatusREMS,
                           String groupAffiliations,
                           String affiliation,
                           String orgUrl,
                           String sub,
                           String keystore,
                           String defaultSignerKeyId,
                           String defaultSigningAlgorithmName,
                           String elixirOrgUrl,
                           String elixirId) {
        this.setIssuer(issuer);
        this.setBonaFideStatus(bonaFideStatus);
        this.setElixirBonaFideStatusREMS(elixirBonaFideStatusREMS);
        this.setGroupAffiliations(groupAffiliations);
        this.setAffiliation(affiliation);
        this.setOrgUrl(orgUrl);
        this.setSub(sub);
        this.setKeyStore(keystore);
        this.setDefaultSignerKeyId(defaultSignerKeyId);
        this.setDefaultSigningAlgorithmName(defaultSigningAlgorithmName);
        this.setElixirOrgUrl(elixirOrgUrl);
        this.setElixirId(elixirId);
    }

    public void setIssuer(String issuer) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("Issuer cannot be empty.");
        }

        this.issuer = issuer;
    }

    public void setBonaFideStatus(String bonaFideStatus) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("BonaFideStatus cannot be empty.");
        }

        this.bonaFideStatus = bonaFideStatus;
    }

    public void setElixirBonaFideStatusREMS(String elixirBonaFideStatusREMS) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("ElixirBonaFideStatusREMS cannot be empty.");
        }

        this.elixirBonaFideStatusREMS = elixirBonaFideStatusREMS;
    }

    public void setGroupAffiliations(String groupAffiliations) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("GroupAffiliations cannot be empty.");
        }

        this.groupAffiliations = groupAffiliations;
    }

    public void setAffiliation(String affiliation) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("Affiliation cannot be empty.");
        }

        this.affiliation = affiliation;
    }

    public void setOrgUrl(String orgUrl) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("OrgUrl cannot be empty.");
        }

        this.orgUrl = orgUrl;
    }

    public void setSub(String sub) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("Sub cannot be empty.");
        }

        this.sub = sub;
    }

    public void setKeyStore(String keystore) {
        if (!StringUtils.hasText(keystore)) {
            throw new IllegalArgumentException("Keystore cannot be empty.");
        }

        this.keystore = keystore;
    }

    public void setDefaultSignerKeyId(String defaultSignerKeyId) {
        if (!StringUtils.hasText(defaultSignerKeyId)) {
            throw new IllegalArgumentException("DefaultSignerKeyId cannot be empty.");
        }

        this.defaultSignerKeyId = defaultSignerKeyId;
    }

    public void setDefaultSigningAlgorithmName(String defaultSigningAlgorithmName) {
        if (!StringUtils.hasText(defaultSigningAlgorithmName)) {
            throw new IllegalArgumentException("DefaultSigningAlgorithmName cannot be empty.");
        }

        this.defaultSigningAlgorithmName = defaultSigningAlgorithmName;
    }

    public void setElixirOrgUrl(String elixirOrgUrl) {
        if (!StringUtils.hasText(elixirOrgUrl)) {
            throw new IllegalArgumentException("ElixirOrgUrl cannot be empty.");
        }

        this.elixirOrgUrl = elixirOrgUrl;
    }

    public void setElixirId(String elixirId) {
        if (!StringUtils.hasText(elixirId)) {
            throw new IllegalArgumentException("ElixirId cannot be empty.");
        }

        this.elixirId = elixirId;
    }
}
