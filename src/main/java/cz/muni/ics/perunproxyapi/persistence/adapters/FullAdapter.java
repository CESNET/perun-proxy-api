package cz.muni.ics.perunproxyapi.persistence.adapters;

import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.enums.MemberStatus;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.RpcConnectionException;
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
     */
    Map<String, PerunAttribute> getAttributes(@NonNull Entity entity,
                                              @NonNull Long entityId,
                                              @NonNull List<String> attributes) throws PerunUnknownException, RpcConnectionException;

    /**
     * Get UserExtSource by name and login of the user.
     * @param extSourceName Name of the user ext source.
     * @param extSourceLogin Login of the user.
     * @return UserExtSource or null.
     */
    UserExtSource getUserExtSource(@NonNull String extSourceName, @NonNull String extSourceLogin) throws PerunUnknownException, RpcConnectionException;

    /**
     * Get status of user in VO.
     * @param userId ID of the user.
     * @param voId ID of the VO.
     * @return MemberStatus representing status of the user in the VO, NULL if member not found.
     */
    MemberStatus getMemberStatusByUserAndVo(@NonNull Long userId, @NonNull Long voId) throws PerunUnknownException, RpcConnectionException;

    /**
     * Set attributes for given entity.
     * @param entity Entity enumeration value. Specifies Perun entity.
     * @param entityId ID of the entity in Perun.
     * @param attributes List of attributes to be set.
     * @return TRUE if everything went OK, FALSE otherwise.
     */
    boolean setAttributes(@NonNull Entity entity, @NonNull Long entityId, @NonNull List<PerunAttribute> attributes) throws PerunUnknownException, RpcConnectionException;

    /**
     * Update timestamp representing last usage of the UserExtSource.
     * @param userExtSource UES object with updated timestamp.
     * @return TRUE if updated, FALSE otherwise.
     */
    boolean updateUserExtSourceLastAccess(@NonNull UserExtSource userExtSource) throws PerunUnknownException, RpcConnectionException;

    /**
     * Get Member object of User in the given VO.
     * @param userId ID of the user.
     * @param voId ID of the VO.
     * @return Member object or null.
     */
    Member getMemberByUser(@NonNull Long userId, @NonNull Long voId) throws PerunUnknownException, RpcConnectionException;

}
