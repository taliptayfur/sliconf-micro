package javaday.istanbul.sliconf.micro.event.service;

import javaday.istanbul.sliconf.micro.event.model.Event;
import javaday.istanbul.sliconf.micro.response.ResponseMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EventService {
    Event findOne(String id);

    List<Event> findAll();

    List<Event> findByName(String name);

    List<Event> findByNameAndDeleted(String name, Boolean deleted);


    List<Event> findByNameAndNotKeyAndDeleted(String name, String key, Boolean deleted);

    void delete(Event event);

    Event findByKeyAndExecutiveUser(String key, String userId);

    public ResponseMessage save(Event event);


    Event findEventByKeyEquals(String key);


    List<Event> getNotDeletedEvents(List<Event> events);


    Map<String, List<Event>> findByExecutiveUser(String executiveUser);


    boolean isKeyExists(String key);


    void hideEventElements(Event event);

    Optional<Event> findByKey(String eventIdentifier);
}
