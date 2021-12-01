package uk.gov.hmcts.reform.hmc.service;

import uk.gov.hmcts.reform.hmc.model.DeleteHearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingRequest;
import uk.gov.hmcts.reform.hmc.model.HearingResponse;

public interface HearingManagementService {

    void getHearingRequest(Long hearingId, boolean isValid);

    HearingResponse saveHearingRequest(HearingRequest hearingRequest);

    void verifyAccess(String caseReference);

    void deleteHearingRequest(Long hearingId, DeleteHearingRequest deleteRequest);
}
