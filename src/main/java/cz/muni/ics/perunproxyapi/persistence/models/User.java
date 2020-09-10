package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.HashMap;
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
    @NonNull private String login;
    private Map<String, PerunAttributeValue> attributes = new HashMap<>();

    public User(Long perunId, String firstName, String lastName, Map<String, PerunAttributeValue> attributes) {
        this.setPerunId(perunId);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setAttributes(attributes);
    }

    public User(Long perunId, String firstName, String lastName, String login,
                Map<String, PerunAttributeValue> attributes)
    {
        this(perunId, firstName, lastName, attributes);
        this.setPerunId(perunId);
        this.setLogin(login);
    }

    public void setAttributes(@NonNull Map<String, PerunAttributeValue> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
    }

    public void setLastName(@NonNull String lastName) {
        if (!StringUtils.hasText(lastName)) {
            throw new IllegalArgumentException("name can't be null or empty");
        }

        this.lastName = lastName;
    }

    public void setLogin(@NonNull String login) {
        if (!StringUtils.hasText(login)) {
            throw new IllegalArgumentException("Login can't be null nor empty");
        }

        this.login = login;
    }

}
