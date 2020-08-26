package cz.muni.ics.perunproxyapi.presentation.DTOModels;

import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Model representing User DTO object which is being returned by API methods
 *
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UserDTO {

    @NonNull private String login;
    @NonNull private Map<String, PerunAttributeValue> perunAttributes;

    public UserDTO(String login, Map<String, PerunAttributeValue> perunAttributes) {
        this.login = login;
        this.perunAttributes = perunAttributes;
    }

}
