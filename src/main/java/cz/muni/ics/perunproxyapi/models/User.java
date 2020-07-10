package cz.muni.ics.perunproxyapi.models;


import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Represents user from Perun.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class User extends Model {
    @Getter
    private String firstName;
    @Getter
    private String lastName;

    public User() {
    }

    public User(long id, String firstName, String lastName) {
        super(id);
        this.setFirstName(firstName);
        this.setLastName(lastName);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        if (Strings.isNullOrEmpty(lastName)) {
            throw new IllegalArgumentException("name can't be null or empty");
        }

        this.lastName = lastName;
    }
}

