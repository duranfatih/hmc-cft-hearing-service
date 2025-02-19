package uk.gov.hmcts.reform.hmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.hmc.exceptions.ValidationError;
import uk.gov.hmcts.reform.hmc.validator.EnumPattern;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class HearingLocation {

    @NotEmpty(message = ValidationError.LOCATION_TYPE_EMPTY)
    private String locationType;

    @EnumPattern(enumClass = LocationId.class, fieldName = "locationId")
    private String locationId;

}
