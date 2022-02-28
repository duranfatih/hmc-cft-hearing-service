package uk.gov.hmcts.reform.hmc.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Repository
public interface HearingRepository extends CrudRepository<HearingEntity, Long> {

    @Query("SELECT status from HearingEntity where id = :hearingId")
    String getStatus(Long hearingId);

    @Query("select id FROM HearingEntity h WHERE h.caseHearingRequest.hmctsServiceID = :hmctsServiceCode")
    List<String> getUnNotifiedHearings(String hmctsServiceCode, String hearingStartDateFrom, String hearingStartDateTo);
}
