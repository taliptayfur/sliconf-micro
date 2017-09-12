package javaday.istanbul.sliconf.micro.controller.event;

import javaday.istanbul.sliconf.micro.model.event.Event;
import javaday.istanbul.sliconf.micro.model.response.ResponseMessage;
import javaday.istanbul.sliconf.micro.provider.EventControllerMessageProvider;
import javaday.istanbul.sliconf.micro.service.event.EventRepositoryService;
import javaday.istanbul.sliconf.micro.util.EventUtil;
import javaday.istanbul.sliconf.micro.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Objects;

@Component
public class CreateEventController {

    @Autowired
    private EventControllerMessageProvider messageProvider;

    @Autowired
    private EventRepositoryService repositoryService;

    public ResponseMessage createEvent(Request request, Response response) {
        ResponseMessage responseMessage;

        String body = request.body();
        Event event = JsonUtil.fromJson(body, Event.class);
        //isim uzunluğu minimumdan düşük mü diye kontrol et
        if(EventUtil.checkEventName(event, 4)){
            responseMessage = new ResponseMessage(false,
                    messageProvider.getMessage("eventNameTooShort"), new Object());
            return responseMessage;
        }
        //event tarihinin geçip geçmediğin, kontrol et
//        if(!EventUtil.checkIfEventDateAfterOrInNow(event)){
//            responseMessage = new ResponseMessage(false,
//                    messageProvider.getMessage("eventDataInvalid"), new Object());
//            return responseMessage;
//        }
        // event var mı diye kontrol et
        List<Event> dbEvents = repositoryService.findByName(event.getName());
        if (Objects.nonNull(dbEvents) && !dbEvents.isEmpty()) {
            responseMessage = new ResponseMessage(false,
                    messageProvider.getMessage("eventAlreadyRegistered"), new Object());
            return responseMessage;
        }
        // eger event yoksa kayit et
        ResponseMessage dbResponse = repositoryService.save(event);
        if (!dbResponse.isStatus()) {
            return dbResponse;
        }

        responseMessage = new ResponseMessage(true,
                messageProvider.getMessage("eventCreatedSuccessfully"), event);

        return responseMessage;
    }

}
