package uk.gov.hmcts.reform.hmc.helper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.hmc.data.CaseCategoriesEntity;
import uk.gov.hmcts.reform.hmc.data.HearingAttendeeDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayDetailsEntity;
import uk.gov.hmcts.reform.hmc.data.HearingDayPanelEntity;
import uk.gov.hmcts.reform.hmc.data.HearingEntity;
import uk.gov.hmcts.reform.hmc.data.HearingPartyEntity;
import uk.gov.hmcts.reform.hmc.data.HearingResponseEntity;
import uk.gov.hmcts.reform.hmc.data.IndividualDetailEntity;
import uk.gov.hmcts.reform.hmc.data.NonStandardDurationsEntity;
import uk.gov.hmcts.reform.hmc.data.PanelRequirementsEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredFacilitiesEntity;
import uk.gov.hmcts.reform.hmc.data.RequiredLocationsEntity;
import uk.gov.hmcts.reform.hmc.data.UnavailabilityEntity;
import uk.gov.hmcts.reform.hmc.model.Attendee;
import uk.gov.hmcts.reform.hmc.model.CaseCategory;
import uk.gov.hmcts.reform.hmc.model.CaseDetails;
import uk.gov.hmcts.reform.hmc.model.GetHearingResponse;
import uk.gov.hmcts.reform.hmc.model.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.HearingLocation;
import uk.gov.hmcts.reform.hmc.model.HearingWindow;
import uk.gov.hmcts.reform.hmc.model.IndividualDetails;
import uk.gov.hmcts.reform.hmc.model.OrganisationDetails;
import uk.gov.hmcts.reform.hmc.model.PanelRequirements;
import uk.gov.hmcts.reform.hmc.model.PartyDetails;
import uk.gov.hmcts.reform.hmc.model.PartyType;
import uk.gov.hmcts.reform.hmc.model.RelatedParty;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityDow;
import uk.gov.hmcts.reform.hmc.model.UnavailabilityRanges;
import uk.gov.hmcts.reform.hmc.model.hmi.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hmi.RequestDetails;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetHearingResponseMapper {

    public GetHearingResponse toHearingResponse(HearingEntity hearingEntity) {
        GetHearingResponse getHearingResponse = new GetHearingResponse();
        getHearingResponse.setRequestDetails(setRequestDetails(hearingEntity));
        getHearingResponse.setHearingDetails(setHearingDetails(hearingEntity));
        getHearingResponse.setCaseDetails(setCaseDetails(hearingEntity));
        getHearingResponse.setPartyDetails(setPartyDetails(hearingEntity));
        getHearingResponse.setHearingResponse(setHearingResponse(hearingEntity));
        return getHearingResponse;
    }

    private RequestDetails setRequestDetails(HearingEntity hearingEntity) {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setStatus(hearingEntity.getStatus());
        requestDetails.setTimestamp(hearingEntity.getCaseHearingRequest().getHearingRequestReceivedDateTime());
        requestDetails.setVersionNumber(hearingEntity.getCaseHearingRequest().getVersionNumber());
        return requestDetails;
    }

    private CaseDetails setCaseDetails(HearingEntity hearingEntity) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setHmctsServiceCode(hearingEntity.getCaseHearingRequest().getHmctsServiceID());
        caseDetails.setCaseRef(hearingEntity.getCaseHearingRequest().getCaseReference());
        caseDetails.setExternalCaseReference(hearingEntity.getCaseHearingRequest().getExternalCaseReference());
        caseDetails.setCaseDeepLink(hearingEntity.getCaseHearingRequest().getCaseUrlContextPath());
        caseDetails.setHmctsInternalCaseName(hearingEntity.getCaseHearingRequest().getHmctsInternalCaseName());
        caseDetails.setPublicCaseName(hearingEntity.getCaseHearingRequest().getPublicCaseName());
        caseDetails.setCaseAdditionalSecurityFlag(
            hearingEntity.getCaseHearingRequest().getAdditionalSecurityRequiredFlag());
        caseDetails.setCaseInterpreterRequiredFlag(
            hearingEntity.getCaseHearingRequest().getInterpreterBookingRequiredFlag());
        caseDetails.setCaseCategories(setCaseCategories(hearingEntity));
        caseDetails.setCaseManagementLocationCode(hearingEntity.getCaseHearingRequest().getOwningLocationId());
        caseDetails.setCaseRestrictedFlag(hearingEntity.getCaseHearingRequest().getCaseRestrictedFlag());
        caseDetails.setCaseSlaStartDate(hearingEntity.getCaseHearingRequest().getCaseSlaStartDate());
        return caseDetails;
    }

    private ArrayList<CaseCategory> setCaseCategories(HearingEntity hearingEntity) {
        ArrayList<CaseCategory> caseCategories = new ArrayList<>();
        if (hearingEntity.getCaseHearingRequest().getCaseCategories() != null) {
            if (!hearingEntity.getCaseHearingRequest().getCaseCategories().isEmpty()) {
                for (CaseCategoriesEntity caseCategoriesEntity
                    : hearingEntity.getCaseHearingRequest().getCaseCategories()) {
                    CaseCategory caseCategory = new CaseCategory();
                    caseCategory.setCategoryType(retrieveLabel(caseCategoriesEntity.getCategoryType().getLabel()));
                    caseCategory.setCategoryValue(caseCategoriesEntity.getCaseCategoryValue());
                    caseCategories.add(caseCategory);
                }
            }
        }
        return caseCategories;
    }

    private ArrayList<PartyDetails> setPartyDetails(HearingEntity hearingEntity) {
        ArrayList<PartyDetails> partyDetailsList = new ArrayList<>();
        for (HearingPartyEntity hearingPartyEntity : hearingEntity.getCaseHearingRequest().getHearingParties()) {
            PartyDetails partyDetails = new PartyDetails();
            partyDetails.setPartyID(hearingPartyEntity.getPartyReference());
            partyDetails.setPartyType(retrieveLabel(hearingPartyEntity.getPartyType().getLabel()));
            partyDetails.setPartyRole(hearingPartyEntity.getPartyRoleType());
            if (PartyType.IND.getLabel().equals(hearingPartyEntity.getPartyType().getLabel())) {
                partyDetails.setIndividualDetails(setIndividualDetails(hearingPartyEntity).get(0));
            } else {
                partyDetails.setOrganisationDetails(setOrganisationDetails(hearingPartyEntity));
            }
            setUnavailability(hearingPartyEntity, partyDetails);
            partyDetailsList.add(partyDetails);
        }
        return partyDetailsList;
    }

    private void setUnavailability(HearingPartyEntity hearingPartyEntity, PartyDetails partyDetails) {
        ArrayList<UnavailabilityDow> unavailabilityDowArrayList = new ArrayList<>();
        ArrayList<UnavailabilityRanges> unavailabilityRangesArrayList = new ArrayList<>();
        if (hearingPartyEntity.getUnavailabilityEntity() != null) {
            if (!hearingPartyEntity.getUnavailabilityEntity().isEmpty()) {
                for (UnavailabilityEntity unavailabilityEntity : hearingPartyEntity.getUnavailabilityEntity()) {
                    UnavailabilityDow unavailabilityDow = new UnavailabilityDow();
                    UnavailabilityRanges unavailabilityRanges = new UnavailabilityRanges();
                    unavailabilityDow.setDowUnavailabilityType(
                        retrieveLabel(unavailabilityEntity.getDayOfWeekUnavailableType().getLabel()));
                    unavailabilityDow.setDow(retrieveLabel(unavailabilityEntity.getDayOfWeekUnavailable().getLabel()));
                    unavailabilityRanges.setUnavailableToDate(unavailabilityEntity.getEndDate());
                    unavailabilityRanges.setUnavailableFromDate(unavailabilityEntity.getStartDate());
                    unavailabilityRangesArrayList.add(unavailabilityRanges);
                    unavailabilityDowArrayList.add(unavailabilityDow);
                }
            }
        }
        partyDetails.setUnavailabilityDow(unavailabilityDowArrayList);
        partyDetails.setUnavailabilityRanges(unavailabilityRangesArrayList);
    }

    private OrganisationDetails setOrganisationDetails(HearingPartyEntity hearingPartyEntity) {
        OrganisationDetails organisationDetails = new OrganisationDetails();
        if (hearingPartyEntity.getOrganisationDetailEntity() != null) {
            organisationDetails.setName(hearingPartyEntity.getOrganisationDetailEntity().getOrganisationName());
            organisationDetails.setOrganisationType(
                hearingPartyEntity.getOrganisationDetailEntity().getOrganisationTypeCode());
            organisationDetails.setCftOrganisationID(
                hearingPartyEntity.getOrganisationDetailEntity().getHmctsOrganisationReference());
        }
        return organisationDetails;
    }

    private ArrayList<IndividualDetails> setIndividualDetails(HearingPartyEntity hearingPartyEntity) {
        ArrayList<IndividualDetails> individualDetailsArrayList = new ArrayList<>();
        if (hearingPartyEntity.getIndividualDetailEntity() != null) {
            for (IndividualDetailEntity individualDetailEntity : hearingPartyEntity.getIndividualDetailEntity()) {
                IndividualDetails individualDetails = new IndividualDetails();
                individualDetails.setTitle(individualDetailEntity.getTitle());
                individualDetails.setFirstName(individualDetailEntity.getFirstName());
                individualDetails.setLastName(individualDetailEntity.getLastName());
                individualDetails.setPreferredHearingChannel(individualDetailEntity.getChannelType());
                individualDetails.setInterpreterLanguage(individualDetailEntity.getInterpreterLanguage());
                if (hearingPartyEntity.getReasonableAdjustmentsEntity() != null) {
                    if (!hearingPartyEntity.getReasonableAdjustmentsEntity().isEmpty()) {
                        individualDetails.setReasonableAdjustments(
                            List.of(hearingPartyEntity.getReasonableAdjustmentsEntity().get(
                                0).getReasonableAdjustmentCode()));
                    }
                }
                individualDetails.setVulnerableFlag(individualDetailEntity.getVulnerableFlag());
                individualDetails.setVulnerabilityDetails(individualDetailEntity.getVulnerabilityDetails());
                if (hearingPartyEntity.getContactDetails() != null) {
                    if (!hearingPartyEntity.getContactDetails().isEmpty()) {
                        if (hearingPartyEntity.getContactDetails().get(0).getContactDetails().contains("@")) {
                            individualDetails.setHearingChannelEmail(
                                hearingPartyEntity.getContactDetails().get(0).getContactDetails());
                        } else {
                            individualDetails.setHearingChannelPhone(
                                hearingPartyEntity.getContactDetails().get(0).getContactDetails());
                        }
                    }
                }
                RelatedParty relatedParty = new RelatedParty();
                relatedParty.setRelatedPartyID(individualDetailEntity.getRelatedPartyID());
                relatedParty.setRelationshipType(individualDetailEntity.getRelatedPartyRelationshipType());

                individualDetails.setRelatedParties(List.of(relatedParty));
                individualDetailsArrayList.add(individualDetails);
            }
        }
        return individualDetailsArrayList;
    }


    private ArrayList<HearingResponse> setHearingResponse(HearingEntity hearingEntity) {
        ArrayList<HearingResponse> hearingResponses = new ArrayList<>();
        for (HearingResponseEntity hearingResponseEntity : hearingEntity.getHearingResponses()) {
            HearingResponse hearingResponse = new HearingResponse();
            hearingResponse.setListAssistTransactionID(
                hearingResponseEntity.getHearingResponseId());
            hearingResponse.setReceivedDateTime(hearingResponseEntity.getRequestTimeStamp());
            hearingResponse.setResponseVersion(hearingResponseEntity.getHearingResponseId());
            hearingResponse.setLaCaseStatus(hearingResponseEntity.getListingCaseStatus());
            hearingResponse.setListingStatus(hearingResponseEntity.getListingStatus());
            setHearingDaySchedule(hearingResponse, List.of(hearingResponseEntity));
            hearingResponses.add(hearingResponse);
        }
        return hearingResponses;
    }


    private HearingDetails setHearingDetails(HearingEntity hearingEntity) {
        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setAutoListFlag(hearingEntity.getCaseHearingRequest().getAutoListFlag());
        hearingDetails.setHearingType(hearingEntity.getCaseHearingRequest().getHearingType());
        hearingDetails.setHearingWindow(setHearingWindow(hearingEntity));
        hearingDetails.setDuration(hearingEntity.getCaseHearingRequest().getRequiredDurationInMinutes());
        hearingDetails.setNonStandardHearingDurationReasons(setHearingPriorityType(hearingEntity));
        hearingDetails.setHearingPriorityType(hearingEntity.getCaseHearingRequest().getHearingPriorityType());
        hearingDetails.setNumberOfPhysicalAttendees(
            hearingEntity.getCaseHearingRequest().getNumberOfPhysicalAttendees());
        hearingDetails.setHearingInWelshFlag(hearingEntity.getCaseHearingRequest().getHearingInWelshFlag());
        hearingDetails.setHearingLocations(setHearingLocations(hearingEntity));
        hearingDetails.setFacilitiesRequired(setFacilityType(hearingEntity));
        hearingDetails.setListingComments(hearingEntity.getCaseHearingRequest().getListingComments());
        hearingDetails.setHearingRequester(hearingEntity.getCaseHearingRequest().getRequester());
        hearingDetails.setPrivateHearingRequiredFlag(
            hearingEntity.getCaseHearingRequest().getPrivateHearingRequiredFlag());
        hearingDetails.setLeadJudgeContractType(hearingEntity.getCaseHearingRequest().getLeadJudgeContractType());
        hearingDetails.setPanelRequirements(setPanelRequirements(hearingEntity));
        hearingDetails.setHearingIsLinkedFlag(hearingEntity.getCaseHearingRequest().getIsLinkedFlag());
        return hearingDetails;
    }

    private ArrayList<String> setHearingPriorityType(HearingEntity hearingEntity) {
        ArrayList<String> hearingPriorityType = new ArrayList<>();
        if (hearingEntity.getCaseHearingRequest().getNonStandardDurations() != null) {
            if (!hearingEntity.getCaseHearingRequest().getNonStandardDurations().isEmpty()) {
                for (NonStandardDurationsEntity nonStandardDurationsEntity
                    : hearingEntity.getCaseHearingRequest().getNonStandardDurations()) {
                    hearingPriorityType.add(nonStandardDurationsEntity.getNonStandardHearingDurationReasonType());
                }
            }
        }
        return hearingPriorityType;
    }

    private PanelRequirements setPanelRequirements(HearingEntity hearingEntity) {
        PanelRequirements panelRequirement = new PanelRequirements();
        if (hearingEntity.getCaseHearingRequest().getPanelRequirements() != null) {
            if (!hearingEntity.getCaseHearingRequest().getPanelRequirements().isEmpty()) {
                for (PanelRequirementsEntity panelRequirementsEntity
                    : hearingEntity.getCaseHearingRequest().getPanelRequirements()) {
                    panelRequirement.setRoleType(List.of(panelRequirementsEntity.getRoleType()));
                }
            }
        }
        return panelRequirement;
    }

    private ArrayList<String> setFacilityType(HearingEntity hearingEntity) {
        ArrayList<String> facilityType = new ArrayList<>();
        if (hearingEntity.getCaseHearingRequest().getRequiredFacilities() != null) {
            if (!hearingEntity.getCaseHearingRequest().getRequiredFacilities().isEmpty()) {
                for (RequiredFacilitiesEntity requiredFacilitiesEntity
                    : hearingEntity.getCaseHearingRequest().getRequiredFacilities()) {
                    facilityType.add(requiredFacilitiesEntity.getFacilityType());
                }
            }
        }
        return facilityType;
    }

    private ArrayList<HearingLocation> setHearingLocations(HearingEntity hearingEntity) {
        ArrayList<HearingLocation> hearingLocations = new ArrayList<>();
        if (hearingEntity.getCaseHearingRequest().getRequiredLocations() != null) {
            if (!hearingEntity.getCaseHearingRequest().getRequiredLocations().isEmpty()) {
                for (RequiredLocationsEntity requiredLocationsEntity
                    : hearingEntity.getCaseHearingRequest().getRequiredLocations()) {
                    HearingLocation hearingLocation = new HearingLocation();
                    hearingLocation.setLocationId(retrieveLabel(requiredLocationsEntity.getLocationId().getLabel()));
                    hearingLocation.setLocationType(requiredLocationsEntity.getLocationLevelType());
                    hearingLocations.add(hearingLocation);
                }
            }
        }
        return hearingLocations;
    }

    private HearingWindow setHearingWindow(HearingEntity hearingEntity) {
        HearingWindow hearingWindow = new HearingWindow();
        hearingWindow.setHearingWindowStartDateRange(
            hearingEntity.getCaseHearingRequest().getHearingWindowStartDateRange());
        hearingWindow.setHearingWindowEndDateRange(
            hearingEntity.getCaseHearingRequest().getHearingWindowEndDateRange());
        hearingWindow.setFirstDateTimeMustBe(hearingEntity.getCaseHearingRequest().getFirstDateTimeOfHearingMustBe());
        return hearingWindow;
    }

    private void setHearingDaySchedule(HearingResponse caseHearing,
                                       List<HearingResponseEntity> hearingResponses) {
        List<HearingDaySchedule> scheduleList = new ArrayList<>();

        for (HearingResponseEntity hearingResponseEntity : hearingResponses) {
            List<HearingDayDetailsEntity> hearingDayDetailEntities = hearingResponseEntity.getHearingDayDetails();
            if (hearingDayDetailEntities != null) {
                if (!hearingDayDetailEntities.isEmpty()) {
                    for (HearingDayDetailsEntity detailEntity : hearingDayDetailEntities) {
                        HearingDaySchedule hearingDaySchedule = setHearingDayScheduleDetails(detailEntity);
                        setHearingJudgeAndPanelMemberIds(detailEntity.getHearingDayPanel().get(0), hearingDaySchedule);
                        setAttendeeDetails(detailEntity.getHearingAttendeeDetails(), hearingDaySchedule);
                        scheduleList.add(hearingDaySchedule);
                    }
                }
            }
            caseHearing.setHearingDaySchedule(scheduleList);
        }
    }

    private HearingDaySchedule setHearingDayScheduleDetails(HearingDayDetailsEntity detailEntity) {
        HearingDaySchedule hearingDaySchedule = new HearingDaySchedule();
        hearingDaySchedule.setHearingStartDateTime(detailEntity.getStartDateTime());
        hearingDaySchedule.setHearingEndDateTime(detailEntity.getEndDateTime());
        hearingDaySchedule.setListAssistSessionId(detailEntity.getListAssistSessionId());
        hearingDaySchedule.setHearingVenueId(detailEntity.getVenueId());
        hearingDaySchedule.setHearingRoomId(detailEntity.getRoomId());
        return hearingDaySchedule;
    }

    private void setAttendeeDetails(List<HearingAttendeeDetailsEntity> attendeeDetailsEntities,
                                    HearingDaySchedule hearingDaySchedule) {
        List<Attendee> attendeeList = new ArrayList<>();
        for (HearingAttendeeDetailsEntity attendeeDetailEntity : attendeeDetailsEntities) {
            Attendee attendee = new Attendee();
            attendee.setPartyId(attendeeDetailEntity.getPartyId());
            attendee.setHearingSubChannel(attendeeDetailEntity.getPartySubChannelType());
            attendeeList.add(attendee);
        }
        hearingDaySchedule.setAttendees(attendeeList);
    }

    private void setHearingJudgeAndPanelMemberIds(HearingDayPanelEntity hearingDayPanelEntity,
                                                  HearingDaySchedule hearingDaySchedule) {
        if (null == hearingDayPanelEntity.getIsPresiding() || !hearingDayPanelEntity.getIsPresiding()) {
            hearingDaySchedule.setPanelMemberId(hearingDayPanelEntity.getPanelUserId());
        } else {
            hearingDaySchedule.setHearingJudgeId(hearingDayPanelEntity.getPanelUserId());
        }
    }

    private String retrieveLabel(String label) {
        return (label == null) ? null : label;

    }
}
