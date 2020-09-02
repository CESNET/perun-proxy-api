package cz.muni.ics.perunproxyapi.presentation.DTOModels;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import com.fasterxml.jackson.databind.JsonNode;
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
    @NonNull private Map<String, JsonNode> attributes;

    public UserDTO(String login, Map<String, JsonNode> attributes) {
        this.login = login;
        this.attributes = attributes;
    }

}
