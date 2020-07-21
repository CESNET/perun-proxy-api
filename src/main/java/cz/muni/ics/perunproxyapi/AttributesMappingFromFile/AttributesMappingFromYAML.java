package cz.muni.ics.perunproxyapi.AttributesMappingFromFile;

import cz.muni.ics.perunproxyapi.attributes.AttributeObjectMapping;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * List of attributes from .yml file is mapped as instance of this class.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AttributesMappingFromYAML {

    private List<AttributeObjectMapping> attributes;
}
