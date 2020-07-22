package cz.muni.ics.perunproxyapi.persistence.adapters;

import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.enums.MemberStatus;
import cz.muni.ics.perunproxyapi.persistence.models.Member;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.UserExtSource;
import cz.muni.ics.perunproxyapi.persistence.models.Vo;

import java.util.List;
import java.util.Map;

public interface FullAdapter extends DataAdapter {

    /**
     * @param entity enum that says for which entity we want to get attributes
     * @param entityId entity id
     * @param attributes list of attribute names
     * @return the map of the attributes with their names
     */
    Map<String, PerunAttribute> getAttributes(Entity entity, Long entityId, List<String> attributes);

    /**
     * @param extSourceName name of the user ext source
     * @param extSourceLogin login
     * @return user ext source
     */
    UserExtSource getUserExtSource(String extSourceName, String extSourceLogin);

    /**
     *
     * @param user user
     * @param vo vo
     * @return MemberStatus of the user in specific vo
     */
    MemberStatus getMemberStatusByUserAndVo(User user, Vo vo);

    /**
     *
     * @param entity enum that says for which entity we want to set attributes
     * @param entityId entity id
     * @param attributes attributes we want to set
     */
    boolean setAttributes(Entity entity, Long entityId, List<PerunAttribute> attributes);

    /**
     *
     * @param userExtSource user ext source to update
     */
    boolean updateUserExtSourceLastAccess(UserExtSource userExtSource);

    /**
     *
     * @param user user
     * @param vo vo
     * @return member
     */
    Member getMemberByUser(User user, Vo vo);

}
