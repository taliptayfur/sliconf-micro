package javaday.istanbul.sliconf.micro.event;

import com.google.api.client.util.Lists;
import javaday.istanbul.sliconf.micro.event.model.*;
import javaday.istanbul.sliconf.micro.event.service.EventService;
import javaday.istanbul.sliconf.micro.mail.IMailSendService;
import javaday.istanbul.sliconf.micro.response.ResponseMessage;
import javaday.istanbul.sliconf.micro.util.Constants;
import javaday.istanbul.sliconf.micro.util.RandomGenerator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import spark.QueryParamsMap;
import spark.Request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static javaday.istanbul.sliconf.micro.event.model.LifeCycleState.EventStatus.ACTIVE;
import static javaday.istanbul.sliconf.micro.event.model.LifeCycleState.EventStatus.PASSIVE;


/**
 * Event ile ilgili cok kullanilan isleri barindiran sinif
 * Business rules checklist
 */

public class EventSpecs {


    private EventSpecs() {
        // private constructor for static
    }



    public static Pageable getPageableFromRequest(Request request) {
        String pageSize = request.queryParamOrDefault("pageSize", "20");
        String pageNumber = request.queryParamOrDefault("pageNumber", "0");
        Pageable pageable;

        try {
            pageable = new PageRequest(Integer.parseInt(pageNumber), Integer.parseInt(pageSize));
        } catch (NumberFormatException e) {
            pageable = new PageRequest(0, 20);
        }

        return pageable;
    }

    public static EventFilter getEventFilterFromRequest(Request request) {
        QueryParamsMap lifeCycleStates =  request.queryMap("lifeCycleStates");
        String name = request.queryParamOrDefault("name", "");

        String sort = request.queryParamOrDefault("sort", "");

        String showDeleted = request.queryParamOrDefault("showDeleted", "");

        List<String> filters = null;
        if(lifeCycleStates.hasValue())
            filters = Lists.newArrayList(Arrays.asList(lifeCycleStates.values()[0].split(",")));
        else {
             // lets check if lifeCycleStates send by queryParam
            String lifeCycleState = request.queryParamOrDefault("lifeCycleStates", "");
            if (lifeCycleState != null && lifeCycleState.length() > 0) {
                // sent by queryParam, lets put the value into filter
                filters = new ArrayList<>();
                filters.add(lifeCycleState);
            } else {
                filters = new ArrayList<>();
            }

        }
        name = "^.*" + name;
        return EventFilter.builder().eventStatuses(filters).nameLike(name).sort(sort).showDeleted(showDeleted).build();

    }

    /**
     * Event name'in minimum uzunlugunu gelen parametreye gore kontrol eder
     *
     * @param event
     * @param nameLength
     * @return
     */
    public static boolean checkEventName(Event event, int nameLength) {
        return event.getName().length() >= nameLength;
    }

    /**
     * Olusturulan eventin tarihi geri donuk olmamali
     *
     * @param event
     * @return
     */
    public static boolean checkIfEventDateAfterOrInNow(Event event) {
        LocalDateTime now = LocalDateTime.now();

        if (Objects.nonNull(event) && Objects.nonNull(event.getStartDate())) {
            return now.isBefore(event.getStartDate());
        }

        return false;
    }

    /**
     * Event'in tarihi verilen tarihten sonra mi kontrolu
     *
     * @param event
     * @return
     */
    public static boolean checkIfEventDateAfterFromGivenDate(Event event, LocalDateTime dateTime) {
        return dateTime.isBefore(event.getEndDate());
    }

    public static String generateKanbanNumber(Event event, EventService eventRepositoryService) {
        boolean isKeyUnique = false;
        String key;

        while (!isKeyUnique) {
            key = RandomGenerator.generateRandom(Constants.EVENT_KEY_LENGTH);

            if (!eventRepositoryService.isKeyExists(key)) {
                event.setKey(key);
                isKeyUnique = true;
            }
        }

        return event.getKey();
    }

    /**
     * Event icin verilmesi gereken bilgileri kontrol eder ve bu bilgilerin olup olmamasina gore
     * statusu true ya da false ve girilmis degerler ve girilmememis degerleri belirtir
     *
     * @param event
     */
    public static void generateStatusDetails(Event event, IMailSendService mailSendService) {
        if (Objects.nonNull(event)) {

            StatusDetails statusDetails = new StatusDetails();

            List<String> failed = new ArrayList<>();
            List<String> passed = new ArrayList<>();

            List<String> optionalFailed = new ArrayList<>();
            List<String> optionalPassed = new ArrayList<>();

            checkRequeiredFields(event, passed, failed);

            checkOptionalFields(event, optionalPassed, optionalFailed);

            statusDetails.setPassed(passed);
            statusDetails.setFailed(failed);

            statusDetails.setOptionalPassed(optionalPassed);
            statusDetails.setOptionalFailed(optionalFailed);

            int count = passed.size() + failed.size();
            int percentage = (int) (passed.size() / ((double) count) * 100);
            statusDetails.setPercentage(percentage);

            event.setStatusDetails(statusDetails);

            if (percentage >= 100) {
                // if are there any acive event in the system
                //if(!event.getLifeCycleState().getEventStatuses().contains(LifeCycleState.EventStatus.ACTIVE) ){
                //    ResponseMessage mailResponse=completeEventSendMail(event,mailSendService);
                //}
                //Bug fixed : https://redmine.kodcu.com/issues/1604 issue
                // if event is finished don't touch
                if ( !event.getLifeCycleState().getEventStatuses().contains(LifeCycleState.EventStatus.FINISHED)) {
                    event.getLifeCycleState().getEventStatuses().add(ACTIVE);
                    event.getLifeCycleState().getEventStatuses().remove(PASSIVE);
                }



            } else {

                if (!event.getLifeCycleState().getEventStatuses().contains(LifeCycleState.EventStatus.FINISHED)) {
                    event.getLifeCycleState().getEventStatuses().remove(ACTIVE);
                    event.getLifeCycleState().getEventStatuses().add(PASSIVE);
                }

            }
        }

    }

    /**
     * Bir kat, bir oda, bir konuşmacı, bir konuşma, tarih, etkinlik ismi, lokasyon zorunlu.
     *
     * @param event
     * @param passed
     * @param failed
     */
    private static void checkRequeiredFields(Event event, List<String> passed, List<String> failed) {
        if (Objects.nonNull(event)) {
            checkFloorAndRoom(event, passed, failed);
            checkSpeakersAndAgenda(event, passed, failed);
            checkDateAndName(event, passed, failed);

            if (Objects.isNull(event.getAbout()) || Objects.isNull(event.getAbout().getLocation()) ||
                    Objects.isNull(event.getAbout().getLocation().getDescription()) ||
                    Objects.isNull(event.getAbout().getLocation().getLat()) ||
                    Objects.isNull(event.getAbout().getLocation().getLng()) ||
                    Objects.isNull(event.getAbout().getLocation().getVenue()) ||
                    event.getAbout().getLocation().getDescription().isEmpty() ||
                    event.getAbout().getLocation().getLng().isEmpty() ||
                    event.getAbout().getLocation().getLat().isEmpty() ||
                    event.getAbout().getLocation().getVenue().isEmpty()){
                failed.add("Event location must be filled correctly");
            } else {
                passed.add("Event location added");
            }
        }
    }

    private static void checkFloorAndRoom(Event event, List<String> passed, List<String> failed) {
        if (Objects.isNull(event.getFloorPlan()) || event.getFloorPlan().isEmpty()) {
            failed.add("At least one floor must be added");
        } else {
            passed.add("At least one floor added");
        }

        if (Objects.isNull(event.getRooms()) || event.getRooms().isEmpty()) {
            failed.add("At least one room must be added");
        } else {
            passed.add("At least one room added");
        }
    }

    private static void checkSpeakersAndAgenda(Event event, List<String> passed, List<String> failed) {
        if (Objects.isNull(event.getSpeakers()) || event.getSpeakers().isEmpty()) {
            failed.add("At least one speaker must be added");
        } else {
            passed.add("At least one speaker added");
        }

        if (Objects.isNull(event.getAgenda()) || event.getAgenda().isEmpty()) {
            failed.add("At least one agenda must be added");
        } else {
            passed.add("At least one agenda added");
        }
    }

    private static void checkDateAndName(Event event, List<String> passed, List<String> failed) {
        if (Objects.isNull(event.getStartDate())) {
            failed.add("Start date must be added");
        } else {
            passed.add("Start date added");
        }

        if (Objects.isNull(event.getName()) || event.getName().isEmpty()) {
            failed.add("Event name must be added");
        } else {
            passed.add("Event name added");
        }
    }

    /**
     * Etkinlik aciklamasi, logo, sosyal alanlar, web sitesi, telefon numarası, e-mail istege bagli.
     *
     * @param event
     * @param optionalPassed
     * @param optionalFailed
     */
    private static void checkOptionalFields(Event event, List<String> optionalPassed, List<String> optionalFailed) {
        if (Objects.nonNull(event)) {
            checkDescription(event, optionalPassed, optionalFailed);

            checkLogo(event, optionalPassed, optionalFailed);

            checkAbout(event, optionalPassed, optionalFailed);
        }
    }

    private static void checkDescription(Event event, List<String> optionalPassed, List<String> optionalFailed) {
        if (Objects.isNull(event.getDescription()) || event.getDescription().isEmpty()) {
            optionalFailed.add("Event description can be added");
        } else {
            optionalPassed.add("Event description added");
        }
    }

    private static void checkLogo(Event event, List<String> optionalPassed, List<String> optionalFailed) {
        if (Objects.isNull(event.getLogoPath()) || event.getLogoPath().isEmpty()) {
            optionalFailed.add("Event logo can be added");
        } else {
            optionalPassed.add("Logo added");
        }
    }

    private static void checkAbout(Event event, List<String> optionalPassed, List<String> optionalFailed) {
        if (Objects.nonNull(event.getAbout())) {
            About about = event.getAbout();


            checkSocial(about, optionalPassed, optionalFailed);
            checkWeb(about, optionalPassed, optionalFailed);
            checkPhone(about, optionalPassed, optionalFailed);
            checkEmail(about, optionalPassed, optionalFailed);
        } else {
            optionalFailed.add("Social details can be added");
            optionalFailed.add("Web site address can be added");
            optionalFailed.add("Phone can be added");
            optionalFailed.add("E-mail address can be added");
        }
    }

    private static void checkSocial(About about, List<String> optionalPassed, List<String> optionalFailed) {
        if (Objects.isNull(about.getSocial()) || about.getSocial().isEmpty()) {
            optionalFailed.add("Social details can be added");
        } else {
            optionalPassed.add("Social details added");
        }
    }

    private static void checkWeb(About about, List<String> optionalPassed, List<String> optionalFailed) {
        if (Objects.isNull(about.getWeb()) || about.getWeb().isEmpty()) {
            optionalFailed.add("Web site address can be added");
        } else {
            optionalPassed.add("Web site address added");
        }
    }

    private static void checkPhone(About about, List<String> optionalPassed, List<String> optionalFailed) {
        if (Objects.isNull(about.getPhone()) || about.getPhone().isEmpty()) {
            optionalFailed.add("Phone can be added");
        } else {
            optionalPassed.add("Phone added");
        }
    }

    private static void checkEmail(About about, List<String> optionalPassed, List<String> optionalFailed) {
        if (Objects.isNull(about.getEmail()) || about.getEmail().isEmpty()) {
            optionalFailed.add("E-mail address can be added");
        } else {
            optionalPassed.add("E-mail address added");
        }
    }

    public static ResponseMessage checkIfEventStateFinished(Event event) {

        ResponseMessage responseMessage = new ResponseMessage(false, "", event);

        List<LifeCycleState.EventStatus> eventStatuses = new ArrayList<>();

        //eventStatuses.add(LifeCycleState.EventStatus.FAILED); // risk, no need
        eventStatuses.add(LifeCycleState.EventStatus.FINISHED);
        eventStatuses.add(LifeCycleState.EventStatus.DELETED);

        Predicate<LifeCycleState.EventStatus> eventStatusPredicate = eventStatuses::contains;

        if(event.getLifeCycleState().getEventStatuses().stream().anyMatch(eventStatusPredicate)){
            responseMessage.setStatus(true);
        }


        return responseMessage;

    }

    public static ResponseMessage checkIfEventStateHappening (Event event) {

        ResponseMessage responseMessage = new ResponseMessage(false, "", event);

        if(event.getLifeCycleState().getEventStatuses().contains(LifeCycleState.EventStatus.HAPPENING)){
            responseMessage.setStatus(true);
        }

        return responseMessage;

    }
    public static ResponseMessage completeEventSendMail(Event event,IMailSendService mailSendService){
        String templateCode="textMail7";
        ResponseMessage mailResponse= mailSendService.sendCompleteEventStateMail(event,templateCode);
        if (!mailResponse.isStatus()) {
            return mailResponse;
        }
        return mailResponse;

    }


}
