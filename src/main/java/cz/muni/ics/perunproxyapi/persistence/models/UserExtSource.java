package cz.muni.ics.perunproxyapi.models;


import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * Model of Perun UserExtSource
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class UserExtSource extends Model {

    @Getter
    private ExtSource extSource;
    @Getter
    private String login;
    @Getter
    private int loa = 0;
    @Getter
    private boolean persistent;
    @Getter
    private Timestamp lastAccess;

    public UserExtSource() {
    }

    public UserExtSource(Long id, ExtSource extSource, String login, int loa, boolean persistent, Timestamp lastAccess) {
        super(id);
        this.setExtSource(extSource);
        this.setLogin(login);
        this.setLoa(loa);
        this.setPersistent(persistent);
        this.setLastAccess(lastAccess);
    }

    public void setExtSource(ExtSource extSource) {
        if (extSource == null) {
            throw new IllegalArgumentException("extSource can't be null");
        }

        this.extSource = extSource;
    }

    public void setLogin(String login) {
        if (Strings.isNullOrEmpty(login)) {
            throw new IllegalArgumentException("login can't be null or empty");
        }

        this.login = login;
    }

    public void setLoa(int loa) {
        if (loa < 0) {
            throw new IllegalArgumentException("loa has to be 0 or higher");
        }

        this.loa = loa;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public void setLastAccess(Timestamp lastAccess) {
        this.lastAccess = lastAccess;
    }

}
