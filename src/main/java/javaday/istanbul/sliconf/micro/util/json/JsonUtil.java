package javaday.istanbul.sliconf.micro.util.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import javaday.istanbul.sliconf.micro.survey.GeneralException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ResponseTransformer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * Created by ttayfur on 7/6/17.
 */
public class JsonUtil {

    private JsonUtil() {
        // private constructor for static
    }

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .create();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        T returnedClass = null;

        try {
            if (Objects.nonNull(json) && Objects.nonNull(clazz)) {
                returnedClass = gson.fromJson(json, clazz);
            }
        } catch (JsonSyntaxException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }

        return returnedClass;
    }

    public static <T> List<T> fromJsonForList(String json, Class<T> clazz) {
        List<T> returnList = null;

        Type type = new ListParameterizedType(clazz);

        try {
            if (Objects.nonNull(json) && Objects.nonNull(clazz)) {
                returnList = gson.fromJson(json, type);
            }
        } catch (JsonSyntaxException e) {
            logger.error(e.getMessage(), e);

        }

        return returnList;
    }

    // json çevirme başarılı olamaz ise ön tarafa ilgili exceptionu döndürür.
    public static <T> T fromJsonOrElseThrow(String jsonString, Class<T> clazz) {
        T object = null;
        try {
            object = JsonUtil.fromJson(jsonString, clazz);
        } catch (Exception e) {
            throw new GeneralException(e.getMessage(), e.getCause());
        }

        return object;
    }

    public static ResponseTransformer json() {
        return JsonUtil::toJson;
    }
}