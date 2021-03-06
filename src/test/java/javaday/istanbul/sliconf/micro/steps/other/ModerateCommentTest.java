package javaday.istanbul.sliconf.micro.steps.other;

import cucumber.api.java.tr.Diyelimki;
import javaday.istanbul.sliconf.micro.SpringBootTestConfig;
import javaday.istanbul.sliconf.micro.agenda.model.AgendaElement;
import javaday.istanbul.sliconf.micro.comment.CommentRepositoryService;
import javaday.istanbul.sliconf.micro.comment.controller.AddNewCommentRoute;
import javaday.istanbul.sliconf.micro.comment.controller.ModerateCommentRoute;
import javaday.istanbul.sliconf.micro.comment.model.Comment;
import javaday.istanbul.sliconf.micro.comment.model.ModerateCommentModel;
import javaday.istanbul.sliconf.micro.event.EventBuilder;
import javaday.istanbul.sliconf.micro.event.EventSpecs;
import javaday.istanbul.sliconf.micro.event.model.Event;
import javaday.istanbul.sliconf.micro.event.service.EventRepositoryService;
import javaday.istanbul.sliconf.micro.floor.Floor;
import javaday.istanbul.sliconf.micro.response.ResponseMessage;
import javaday.istanbul.sliconf.micro.room.Room;
import javaday.istanbul.sliconf.micro.speaker.Speaker;
import javaday.istanbul.sliconf.micro.user.model.User;
import javaday.istanbul.sliconf.micro.user.service.UserRepositoryService;
import javaday.istanbul.sliconf.micro.util.TestUtil;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore
public class ModerateCommentTest extends SpringBootTestConfig { // NOSONAR

    @Autowired
    UserRepositoryService userRepositoryService;

    @Autowired
    EventRepositoryService eventRepositoryService;

    @Autowired
    CommentRepositoryService commentRepositoryService;

    @Autowired
    AddNewCommentRoute addNewCommentRoute;

    @Autowired
    ModerateCommentRoute moderateCommentRoute;


    private List<Speaker> speakers = new ArrayList<>();
    private List<AgendaElement> agendaElements = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private List<Floor> floors = new ArrayList<>();


    @Diyelimki("^Moderator commenti onayliyor ya da onaylamiyor$")
    public void moderatorCommentiOnayliyorYaDaOnaylamiyor() throws Throwable {
        // Given
        User user = new User();
        user.setUsername("commentUser1");
        user.setEmail("commentuser1@sliconf.com");
        user.setPassword("123123123");
        ResponseMessage savedUserMessage = userRepositoryService.saveUser(user);

        assertTrue(savedUserMessage.isStatus());

        String userId = ((User) savedUserMessage.getReturnObject()).getId();

        Event event = new EventBuilder().setName("Comment Event")
                .setExecutiveUser(userId)
                .setDate(LocalDateTime.now().plusMonths(2)).build();

        EventSpecs.generateKanbanNumber(event, eventRepositoryService);

        TestUtil.generateFields(floors, rooms, speakers, agendaElements);

        event.setStatus(true);
        event.setFloorPlan(floors);
        event.setRooms(rooms);
        event.setSpeakers(speakers);
        event.setAgenda(agendaElements);

        ResponseMessage eventSaveMessage = eventRepositoryService.save(event);
        String eventId = ((Event) eventSaveMessage.getReturnObject()).getId();

        Comment comment1 = new Comment();
        comment1.setUserId(userId);
        comment1.setCommentValue("This talk is awesome");
        comment1.setEventId(eventId);
        comment1.setId("comment-1");
        comment1.setSessionId("agenda-element-1");
        comment1.setTime(LocalDateTime.now());

        Comment comment2 = new Comment();
        comment2.setUserId(userId);
        comment2.setCommentValue("This talk is awesome");
        comment2.setEventId(eventId);
        comment2.setId("comment-2");
        comment2.setSessionId("agenda-element-1");
        comment2.setTime(LocalDateTime.now());

        Comment comment3 = new Comment();
        comment3.setUserId(userId);
        comment3.setCommentValue("This talk is awesome");
        comment3.setEventId(eventId);
        comment3.setId("comment-3");
        comment3.setSessionId("agenda-element-1");
        comment3.setTime(LocalDateTime.now());

        Comment comment4 = new Comment();
        comment4.setUserId(userId);
        comment4.setCommentValue("This talk is awesome");
        comment4.setEventId(eventId);
        comment4.setId("comment-4");
        comment4.setSessionId("agenda-element-1");
        comment4.setTime(LocalDateTime.now());

        Comment comment5 = new Comment();
        comment5.setUserId(userId);
        comment5.setCommentValue("This talk is awesome");
        comment5.setEventId(eventId);
        comment5.setId("comment-5");
        comment5.setSessionId("agenda-element-1");
        comment5.setTime(LocalDateTime.now());


        // When
        ResponseMessage commentAddMessage1 = addNewCommentRoute.addNewComment(comment1);

        ResponseMessage commentAddMessage2 = addNewCommentRoute.addNewComment(comment2);
        ResponseMessage commentAddMessage3 = addNewCommentRoute.addNewComment(comment3);
        ResponseMessage commentAddMessage4 = addNewCommentRoute.addNewComment(comment4);
        ResponseMessage commentAddMessage5 = addNewCommentRoute.addNewComment(comment5);


        ModerateCommentModel model1 = new ModerateCommentModel();
        model1.setEventId(eventId);
        model1.setUserId(userId);
        model1.setApproved(Arrays.asList("comment-1", "comment-2", "comment-3"));
        model1.setDenied(Arrays.asList("comment-4", "comment-5"));

        ModerateCommentModel model2 = new ModerateCommentModel();
        model2.setEventId("false-eventId");
        model2.setUserId(userId);
        model2.setApproved(Arrays.asList("comment-1", "comment-2", "comment-3"));
        model2.setDenied(Arrays.asList("comment-4", "comment-5"));

        ModerateCommentModel model3 = new ModerateCommentModel();
        model3.setEventId(eventId);
        model3.setUserId("false-userId");
        model3.setApproved(Arrays.asList("comment-1", "comment-2", "comment-3"));
        model3.setDenied(Arrays.asList("comment-4", "comment-5"));


        ResponseMessage moderateCommentMessage1 = moderateCommentRoute.moderateComment(model1);
        ResponseMessage moderateCommentMessage2 = moderateCommentRoute.moderateComment(model2);
        ResponseMessage moderateCommentMessage3 = moderateCommentRoute.moderateComment(model3);


        // Then
        assertTrue(commentAddMessage1.isStatus());

        assertTrue(commentAddMessage2.isStatus());
        assertTrue(commentAddMessage3.isStatus());
        assertTrue(commentAddMessage4.isStatus());
        assertTrue(commentAddMessage5.isStatus());

        assertTrue(moderateCommentMessage1.isStatus());
        assertFalse(moderateCommentMessage2.isStatus());
        assertFalse(moderateCommentMessage3.isStatus());
    }


}
