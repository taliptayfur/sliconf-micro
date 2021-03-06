package javaday.istanbul.sliconf.micro.image;

import io.swagger.annotations.*;
import javaday.istanbul.sliconf.micro.response.ResponseMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

@Api
@javax.ws.rs.Path("/service/image/get/:id")
@Produces("application/json")
@Component
public class ImageGetRoute implements Route {

    private Logger logger = LogManager.getLogger(ImageGetRoute.class);

    @GET
    @ApiOperation(value = "Returns image with given id", nickname = "ImageGetRoute")
    @ApiImplicitParams({ //
            @ApiImplicitParam(required = true, dataType = "string", name = "token", paramType = "header"), //
            @ApiImplicitParam(required = true, dataType = "string", name = "id", paramType = "path") //
    }) //
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Success", response = ResponseMessage.class), //
            @ApiResponse(code = 400, message = "Invalid input data", response = ResponseMessage.class), //
            @ApiResponse(code = 401, message = "Unauthorized", response = ResponseMessage.class), //
            @ApiResponse(code = 404, message = "User not found", response = ResponseMessage.class) //
    })
    @Override
    public Object handle(@ApiParam(hidden = true) Request request, @ApiParam(hidden = true) Response response) throws Exception {

        ResponseMessage responseMessage = new ResponseMessage(false, "Image can not found", "");

        String id = request.params(":id");

        if (Objects.isNull(id)) {
            responseMessage.setMessage("Image id can not be null");
            return responseMessage;
        }

        try {

            Path tempFile = new File("upload/" + id).toPath();

            if (!tempFile.toFile().exists()) {
                response.type("application/json");
                return responseMessage;
            }

            response.type("image/png");
            response.header("Content-Disposition", "filename=" + id);

            Files.copy(tempFile, response.raw().getOutputStream());

            return "";
        } catch (InvalidPathException exception) {
            logger.error(exception.getMessage(), exception);
        }

        return responseMessage;
    }
}
