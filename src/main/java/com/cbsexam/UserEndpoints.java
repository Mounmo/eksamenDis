package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON: FIX
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);

    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down?: FIX

    if (user != null){
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    }else {
      return Response.status(400).entity("Could not get user").build();
    }
  }

  /** @return Responses */

  public static UserCache userCache = new UserCache();
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON: FIX
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.: FIX
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    User user = new Gson().fromJson(body, User.class);

    String token = UserController.loginUser(user);

    if (token != ""){
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(token).build();
    }else {
      return Response.status(400).entity("Could not login").build();
    }
  }

  // TODO: Make the system able to delete users: FIX
  @DELETE
  @Path("/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(@PathParam("token") String token) {

   Boolean userDeleted = UserController.deleteUser(token);

    if (userDeleted){
      // Return a response with status 200 and JSON as type
      return Response.status(200).entity("User has been deleted").build();
    }else {
      // Return a response with status 200 and JSON as type
      return Response.status(400).entity("Could not delete user").build();
    }
  }

  // TODO: Make the system able to update users: FIX
  @PUT
  @Path("/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("token") String token, String body) {

    User user = new Gson().fromJson(body, User.class);

    Boolean updatedUser = UserController.updateUser(token, user);

    if (updatedUser){
      // Return a response with status 200 and JSON as type
      return Response.status(200).entity("User has been updated").build();
    }else {
      return Response.status(400).entity("Could not update user").build();
    }
  }
}
