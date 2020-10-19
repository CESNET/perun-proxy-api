package cz.muni.ics.perunproxyapi.ga4gh.model.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import cz.muni.ics.perunproxyapi.ga4gh.model.ClaimRepository;
import cz.muni.ics.perunproxyapi.ga4gh.model.Signer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of GA4GH for ELIXIR.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Slf4j
public class ElixirGa4ghConfig {

    @NonNull private String issuer;
    @NonNull private AttrIdentifiers identifiers;
    @NonNull private String keystore;
    @NonNull @JsonAlias({"defaultSignerKeyId", "default_signer_key_id"})
    private String defaultSignerKeyId;
    @NonNull @JsonAlias({"defaultSigningAlgorithmName", "default_signing_algorithm_name"})
    private String defaultSigningAlgorithmName;
    @NonNull @JsonAlias({"elixirOrgUrl", "elixir_org_url"}) private String elixirOrgUrl;
    @NonNull @JsonAlias({"elixirId", "elixir_id"}) private String elixirId;
    @NonNull @JsonAlias({"groupId", "group_id"}) private Long groupId;
    @NonNull private List<ClaimRepository> repositories = new ArrayList<>();
    @NonNull private List<Signer> signers = new ArrayList<>();

    public ElixirGa4ghConfig(@NonNull String issuer,
                             @NonNull AttrIdentifiers identifiers,
                             @NonNull String keystore,
                             @NonNull String defaultSignerKeyId,
                             @NonNull String defaultSigningAlgorithmName,
                             @NonNull String elixirOrgUrl,
                             @NonNull String elixirId,
                             @NonNull Long groupId,
                             @NonNull List<ClaimRepository> repositories,
                             @NonNull List<Signer> signers)
    {
        this.setIssuer(issuer);
        this.setIdentifiers(identifiers);
        this.setKeyStore(keystore);
        this.setDefaultSignerKeyId(defaultSignerKeyId);
        this.setDefaultSigningAlgorithmName(defaultSigningAlgorithmName);
        this.setElixirOrgUrl(elixirOrgUrl);
        this.setElixirId(elixirId);
        this.setGroupId(groupId);
        this.setRepositories(repositories);
        this.setSigners(signers);
    }

    @PostConstruct
    public void init() {
        log.info("Initialized GA4GH configuration for ELIXIR");
        log.debug("{}", this);
    }

    public void setIssuer(@NonNull String issuer) {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("Issuer cannot be empty.");
        }
        this.issuer = issuer;
    }

    public void setKeyStore(@NonNull String keystore) {
        if (!StringUtils.hasText(keystore)) {
            throw new IllegalArgumentException("Keystore cannot be empty.");
        }
        this.keystore = keystore;
    }

    public void setDefaultSignerKeyId(@NonNull String defaultSignerKeyId) {
        if (!StringUtils.hasText(defaultSignerKeyId)) {
            throw new IllegalArgumentException("DefaultSignerKeyId cannot be empty.");
        }
        this.defaultSignerKeyId = defaultSignerKeyId;
    }

    public void setDefaultSigningAlgorithmName(@NonNull String defaultSigningAlgorithmName) {
        if (!StringUtils.hasText(defaultSigningAlgorithmName)) {
            throw new IllegalArgumentException("DefaultSigningAlgorithmName cannot be empty.");
        }
        this.defaultSigningAlgorithmName = defaultSigningAlgorithmName;
    }

    public void setElixirOrgUrl(@NonNull String elixirOrgUrl) {
        if (!StringUtils.hasText(elixirOrgUrl)) {
            throw new IllegalArgumentException("ElixirOrgUrl cannot be empty.");
        }
        this.elixirOrgUrl = elixirOrgUrl;
    }

    public void setElixirId(@NonNull String elixirId) {
        if (!StringUtils.hasText(elixirId)) {
            throw new IllegalArgumentException("ElixirId cannot be empty.");
        }
        this.elixirId = elixirId;
    }

    public String getAffiliation() {
        return this.identifiers.getAffiliation();
    }

    public String getOrgUrl() {
        return this.identifiers.getOrgUrl();
    }

    public String getSub() {
        return this.identifiers.getSub();
    }

    public String getBonaFideStatus() {
        return this.identifiers.getBonaFideStatus();
    }

    public String getElixirBonaFideStatusREMS() {
        return this.identifiers.getElixirBonaFideStatusREMS();
    }

    public String getGroupAffiliations() {
        return this.identifiers.getGroupAffiliations();
    }

    @Setter
    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    private static class AttrIdentifiers {

        @NonNull @JsonAlias({"bonaFideStatus", "bona_fide_status"})
        private String bonaFideStatus;
        @NonNull @JsonAlias({"elixirBonaFideStatusREMS", "elixir_bona_fide_status_rems"})
        private String elixirBonaFideStatusREMS;
        @NonNull @JsonAlias({"groupAffiliations", "group_affiliations"})
        private String groupAffiliations;
        @NonNull
        private String affiliation;
        @NonNull @JsonAlias({"orgUrl", "org_url"})
        private String orgUrl;
        @NonNull
        private String sub;

        public AttrIdentifiers(@NonNull String bonaFideStatus,
                               @NonNull String elixirBonaFideStatusREMS,
                               @NonNull String groupAffiliations,
                               @NonNull String affiliation,
                               @NonNull String orgUrl,
                               @NonNull String sub)
        {
            this.setBonaFideStatus(bonaFideStatus);
            this.setElixirBonaFideStatusREMS(elixirBonaFideStatusREMS);
            this.setGroupAffiliations(groupAffiliations);
            this.setAffiliation(affiliation);
            this.setOrgUrl(orgUrl);
            this.setSub(sub);
        }

        public void setBonaFideStatus(@NonNull String bonaFideStatus) {
            if (!StringUtils.hasText(bonaFideStatus)) {
                throw new IllegalArgumentException("BonaFideStatus cannot be empty.");
            }
            this.bonaFideStatus = bonaFideStatus;
        }

        public void setElixirBonaFideStatusREMS(@NonNull String elixirBonaFideStatusREMS) {
            if (!StringUtils.hasText(elixirBonaFideStatusREMS)) {
                throw new IllegalArgumentException("ElixirBonaFideStatusREMS cannot be empty.");
            }
            this.elixirBonaFideStatusREMS = elixirBonaFideStatusREMS;
        }

        public void setGroupAffiliations(@NonNull String groupAffiliations) {
            if (!StringUtils.hasText(groupAffiliations)) {
                throw new IllegalArgumentException("GroupAffiliations cannot be empty.");
            }
            this.groupAffiliations = groupAffiliations;
        }

        public void setAffiliation(@NonNull String affiliation) {
            if (!StringUtils.hasText(affiliation)) {
                throw new IllegalArgumentException("Affiliation cannot be empty.");
            }
            this.affiliation = affiliation;
        }

        public void setOrgUrl(@NonNull String orgUrl) {
            if (!StringUtils.hasText(orgUrl)) {
                throw new IllegalArgumentException("OrgUrl cannot be empty.");
            }
            this.orgUrl = orgUrl;
        }

        public void setSub(@NonNull String sub) {
            if (!StringUtils.hasText(sub)) {
                throw new IllegalArgumentException("Sub cannot be empty.");
            }
            this.sub = sub;
        }
    }

}
