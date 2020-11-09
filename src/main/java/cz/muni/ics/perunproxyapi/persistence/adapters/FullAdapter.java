package cz.muni.ics.perunproxyapi.persistence.adapters;

import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.enums.MemberStatus;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Affiliation;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.Member;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.UserExtSource;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Definition of all methods an adapter must implement. Extends the DataAdapter and adds some methods.
 */
public interface FullAdapter extends DataAdapter {

    /**
     * Get attributes for given entity.
     * @param entity Entity enumeration value. Specifies Perun entity.
     * @param entityId ID of the entity in Perun.
     * @param attributes List of attribute names. Specifies what attributes we want to fetch.
     * @return Map<String, PerunAttribute>, key is identifier of the attribute, value is the attribute.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    Map<String, PerunAttribute> getAttributes(@NonNull Entity entity,
                                              @NonNull Long entityId,
                                              @NonNull List<String> attributes) throws PerunUnknownException, PerunConnectionException;

    /**
     * Get attributes for given entity.
     * @param entity Entity enumeration value. Specifies Perun entity.
     * @param entityId ID of the entity in Perun.
     * @param attributes List of attribute names. Specifies what attributes we want to fetch.
     * @return Map<String, PerunAttribute>, key is identifier of the attribute, value is the attribute.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    Map<String, PerunAttribute> getAttributesWithUnrequiredValue(@NonNull Entity entity,
                                              @NonNull Long entityId,
                                              @NonNull List<String> attributes) throws PerunUnknownException, PerunConnectionException;

    /**
     * Get a single attribute for given entity.
     * @param entity Entity enumeration value. Specifies Perun entity.
     * @param entityId ID of the entity in Perun.
     * @param attribute Attribute identifier Specifies what attribute we want to fetch.
     * @return PerunAttribute or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    PerunAttribute getAttribute(@NonNull Entity entity,
                                @NonNull Long entityId,
                                @NonNull String attribute) throws PerunUnknownException, PerunConnectionException;

    /**
     * Get UserExtSource by name and login of the user.
     * @param extSourceName Name of the user ext source.
     * @param extSourceLogin Login of the user.
     * @return UserExtSource or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    UserExtSource getUserExtSource(@NonNull String extSourceName, @NonNull String extSourceLogin) throws PerunUnknownException, PerunConnectionException;

    /**
     * Get UserExtSources by the userId
     * @param userId the id of the user whose extSources are to be obtained
     * @return list of UserExtSources
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    List<UserExtSource> getUserExtSources(@NonNull Long userId) throws PerunUnknownException, PerunConnectionException;

    /**
     * Get status of user in VO.
     * @param userId ID of the user.
     * @param voId ID of the VO.
     * @return MemberStatus representing status of the user in the VO, NULL if member not found.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    MemberStatus getMemberStatusByUserAndVo(@NonNull Long userId, @NonNull Long voId) throws PerunUnknownException, PerunConnectionException;

    /**
     * Set attributes for given entity.
     * @param entity Entity enumeration value. Specifies Perun entity.
     * @param entityId ID of the entity in Perun.
     * @param attributes List of attributes to be set.
     * @return TRUE if everything went OK, FALSE otherwise.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    boolean setAttributes(@NonNull Entity entity, @NonNull Long entityId, @NonNull List<PerunAttribute> attributes) throws PerunUnknownException, PerunConnectionException;

    /**
     * Update timestamp representing last usage of the UserExtSource.
     * @param userExtSource UES object with updated timestamp.
     * @return TRUE if updated, FALSE otherwise.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    boolean updateUserExtSourceLastAccess(@NonNull UserExtSource userExtSource) throws PerunUnknownException, PerunConnectionException;

    /**
     * Get Member object of User in the given VO.
     * @param userId ID of the user.
     * @param voId ID of the VO.
     * @return Member object or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    Member getMemberByUser(@NonNull Long userId, @NonNull Long voId) throws PerunUnknownException, PerunConnectionException;

    /**
     * For the given user, gets all string values of the affiliation attribute of all UserExtSources of type ExtSourceIdp.
     *
     * @param userId ID of user
     * @param affiliationAttr identifier of affiliation attribute
     * @param orgUrlAttr identifier of organization URL attribute
     * @return list of values of attribute affiliation
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    List<Affiliation> getUserExtSourcesAffiliations(Long userId, String affiliationAttr, String orgUrlAttr) throws PerunUnknownException, PerunConnectionException;

    /**
     * Check if user is member of the group.
     *
     * @param userId ID of user
     * @param groupId ID of group
     * @return TRUE if the user is member of the group, FALSE otherwise
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    boolean isUserInGroup(Long userId, Long groupId) throws PerunUnknownException, PerunConnectionException;

    /**
     * For the given user, get all string values of the groupAffiliation attribute of groups of the user.
     *
     * @param userId ID of user
     * @param groupAffiliationsAttr Identifier of group affiliations attribute
     * @return List of values of the affiliation attribute (filled or empty)
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    List<Affiliation> getGroupAffiliations(Long userId, String groupAffiliationsAttr) throws PerunUnknownException, PerunConnectionException;

    /**
     *
     * @param perunProxyIdentifierAttr String attribute identifier of the perunProxyIdentifier attribute
     * @param proxyIdentifier String proxy identifier (value of perunProxyIdentifier attribute)
     * @return List of all facilities from given proxy
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    List<Facility> searchFacilitiesByAttributeValue(String perunProxyIdentifierAttr, String proxyIdentifier) throws PerunUnknownException, PerunConnectionException;
}
