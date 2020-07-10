package cz.muni.ics.perunproxyapi.models;



import cz.muni.ics.perunproxyapi.enums.MemberStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Member object model.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class Member extends Model {

    @Getter
    private Long userId;
    @Getter
    private Long voId;
    @Getter
    private MemberStatus status;

    public Member() {
    }

    public Member(Long id, Long userId, Long voId, MemberStatus status) {
        super(id);
        this.setUserId(userId);
        this.setVoId(voId);
        this.setStatus(status);
    }

    public void setUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        this.userId = userId;
    }

    public void setVoId(Long voId) {
        if (voId == null) {
            throw new IllegalArgumentException("voId cannot be null");
        }

        this.voId = voId;
    }

    public void setStatus(MemberStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null nor empty");
        }

        this.status = status;
    }
}
