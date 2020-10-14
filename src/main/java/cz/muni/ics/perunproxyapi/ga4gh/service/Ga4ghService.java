package cz.muni.ics.perunproxyapi.ga4gh.service;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;

public interface Ga4ghService {

    /**
     * Get GA4GH Passports and visas.
     * @param adapter Adapter to be used to contact Perun.
     * @param perunUserId ID of user in Perun.
     * @return List of Visas and passports as JSON
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    JsonNode getPassportsAndVisas(@NonNull FullAdapter adapter, @NonNull Long perunUserId)
            throws PerunUnknownException, PerunConnectionException;

}
