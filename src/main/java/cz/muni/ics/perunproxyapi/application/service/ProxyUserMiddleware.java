package cz.muni.ics.perunproxyapi.application.service;

import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;

import java.util.List;
import java.util.Map;

/**
 * Middleware for user related things. Purpose of this class to execute correct methods on the given adapter.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
public interface ProxyUserMiddleware {

    /**
     * Find user by identifiers via given adapter.
     *
     * @param preferredAdapter Adapter to be used.
     * @param idpEntityId Identifier of source Identity Provider.
     * @param userIdentifiers List of users identifiers.
     * @return User or null.
     */
    User findByIdentifiers(DataAdapter preferredAdapter, String idpEntityId, List<String> userIdentifiers);

    /**
     * Get user by given IdP identifier and attribute.
     *
     * @param preferredAdapter Adapter to be used.
     * @param idpIdentifier Identifier of source Identity Provider.
     * @param attribute User attribute.
     * @return User or null.
     */
    User getUserByAttribute(DataAdapter preferredAdapter, String idpIdentifier, String attribute);

    /**
     * Get attribute values for a given entity.
     *
     * @param preferredAdapter Adapter to be used.
     * @param entity Entity.
     * @param id Entity id.
     * @param attributes Attributes.
     * @return Map of attribute values for a given entity.
     */
    Map<String, PerunAttributeValue> getAttributesValues(DataAdapter preferredAdapter, Entity entity, long id, List<String> attributes);

}
