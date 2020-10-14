package cz.muni.ics.perunproxyapi.ga4gh.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.ga4gh.service.Ga4ghService;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import lombok.NonNull;

/**
 * Implementation of GA4GH indicating that no actual implementation is available.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class UnsupportedGa4ghService implements Ga4ghService {

    @Override
    public JsonNode getPassportsAndVisas(@NonNull FullAdapter adapter, @NonNull Long perunUserId) {
        throw new UnsupportedOperationException("No GA4GH Implementation found");
    }

}
