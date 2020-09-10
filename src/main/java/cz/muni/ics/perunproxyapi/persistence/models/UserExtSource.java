package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;

/**
 * Model of Perun UserExtSource
 *
 * @author Dominik Frantisek Bucik <bucik@.ics.muni.cz>
 * @author Ondrej Ernst <ondra.ernst@gmail.com>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UserExtSource {

    @NonNull private Long id;
    @NonNull private ExtSource extSource;
    @NonNull private String login;
    private int loa = 0;
    private boolean persistent;
    private Timestamp lastAccess;

    public UserExtSource(Long id, ExtSource extSource, String login, int loa, boolean persistent, Timestamp lastAccess) {
        this.setId(id);
        this.setExtSource(extSource);
        this.setLogin(login);
        this.setLoa(loa);
        this.setPersistent(persistent);
        this.setLastAccess(lastAccess);
    }

    public void setLogin(@NonNull String login) {
        if (!StringUtils.hasText(login)) {
            throw new IllegalArgumentException("login cannot be empty");
        }

        this.login = login;
    }

    public void setLoa(int loa) {
        if (loa < 0) {
            throw new IllegalArgumentException("loa has to be 0 or higher");
        }

        this.loa = loa;
    }

}
