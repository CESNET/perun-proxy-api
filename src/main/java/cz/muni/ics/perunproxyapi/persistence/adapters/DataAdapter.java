package cz.muni.ics.perunproxyapi.persistence.adapters;


import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.Group;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.Vo;

import java.util.List;
import java.util.Map;

public interface DataAdapter {

    /**
     * Gets a user from Perun
     * @param userId the user id
     * @param uids list of user identifiers received from remote idp used as userExtSourceLogin
     * @return User or null if not exists
     */
    User getPerunUser(Long userId, List<String> uids);

    /**
     * @param user the user
     * @param vo vo we are working with
     * @return groups from VO that the user is a member of. Including VO members group.
     */
    List<Group> getMemberGroups(User user, Vo vo);

    /**
     *
     * @param spEntityId entity id of the sp
     * @return list of groups from vo which are assigned to all facilities with spEntityId.
     * Registering to those groups should allow access to the service
     */
    List<Group> getSpGroups(String spEntityId);

    /**
     *
     * @param voId the id of the VO
     * @param name group name. Note that name of group is without VO name prefix.
     * @return group
     */
    Group getGroupByName(Long voId, String name);

    /**
     *
     * @param voShortName the voShortName
     * @return Vo
     */
    Vo getVoByShortName(String voShortName);

    /**
     *
     * @param voId id
     * @return Vo
     */
    Vo getVoById(Long voId);

    /**
     *
     * @param entity enum that says for which entity we want to get attribute values
     * @param entityId the id of the entity
     * @param attributes list of attributes whose values we want to get
     * @return the map of the attributes with their names
     */
    Map<String, PerunAttributeValue> getAttributesValues(Entity entity, Long entityId, List<String> attributes);

    /**
     *
     * @param name attribute name
     * @param attrValue attribute value
     * @return list of facilities with the attribute
     */
    List<Facility> getFacilitiesByAttribute(String name, String attrValue);

    List<Group> getUsersGroupsOnFacility(Long spEntityId, Long userId);

    List<Facility> searchFacilitiesByAttributeValue(PerunAttribute attribute);
}
