package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents user from Perun.
 *
 * @author Dominik Frantisek Bucik <bucik@.ics.muni.cz>
 * @author Ondrej Ernst <ondra.ernst@gmail.com>
 */
@Data
public class User {

    @NonNull private Long id;
    @NonNull private String firstName;
    @NonNull private String lastName;

    public void setLastName(String lastName) {
        if (lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("name can't be null or empty");
        }

        this.lastName = lastName;
    }

}
