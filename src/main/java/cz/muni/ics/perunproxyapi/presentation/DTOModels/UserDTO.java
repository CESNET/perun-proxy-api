package cz.muni.ics.perunproxyapi.presentation.DTOModels;

import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

/**
 * Model representing User DTO object which is being returned by API methods
 *
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
@Data
public class UserDTO {

    @NonNull private String login;
    @NonNull private String firstName;
    @NonNull private String lastName;
    @NonNull private String displayName;
    @NonNull private long perunUserId;
    @NonNull private Map<String, PerunAttributeValue> perunAttributes;

}
