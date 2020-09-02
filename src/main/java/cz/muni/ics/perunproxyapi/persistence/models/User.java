package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Represents user from Perun.
 *
 * @author Dominik Frantisek Bucik <bucik@.ics.muni.cz>
 * @author Ondrej Ernst <ondra.ernst@gmail.com>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User {

    @NonNull private Long perunId;
    @NonNull private String firstName;
    @NonNull private String lastName;

    private String login;
    private Map<String, PerunAttributeValue> perunAttributes;

    public User(@NonNull Long perunId, @NonNull String firstName, @NonNull String lastName) {
        this.setPerunId(perunId);
        this.setFirstName(firstName);
        this.setLastName(lastName);
    }

    public User(@NonNull Long perunId, @NonNull String firstName, @NonNull String lastName, Map<String, PerunAttributeValue> perunAttributes) {
        this.setPerunId(perunId);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.perunAttributes.putAll(perunAttributes);
    }

    public void setLastName(String lastName) {
        if (lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("name can't be null or empty");
        }

        this.lastName = lastName;
    }

}
