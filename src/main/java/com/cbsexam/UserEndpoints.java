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

    // Adding encryption to JSON
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down?: FIX

    // If user returns null, it returns with status 400
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

    // Adding encryption to JSON
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
      // If user could not be created return status code 400
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.: FIX
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    // Read the json from body and transfer it to a user class
    User user = new Gson().fromJson(body, User.class);

    // use UserController to see if user exist in the database and then return af token
    String token = UserController.loginUser(user);

    // if token is not empty return response 200
    if (token != ""){
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(token).build();
    }else {
      //If user cannot login return 400
      return Response.status(400).entity("Could not login").build();
    }
  }

  // TODO: Make the system able to delete users: FIX
  @DELETE
  @Path("/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(@PathParam("token") String token) {

    //Take token and call the method deleteUser from Usercontroller to delete the User from the database.
   Boolean userDeleted = UserController.deleteUser(token);

   // If user has been deleted return status code 200
    if (userDeleted){
      // Return a response with status 200 and JSON as type
      return Response.status(200).entity("User has been deleted").build();
    }else {
      // if user couldn't be deleted return status code 400
      return Response.status(400).entity("Could not delete user").build();
    }
  }

  // TODO: Make the system able to update users: FIX
  @PUT
  @Path("{idUser}/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("token") String token, String body) {

    // read the json from body and transfer it to a user class.
    User user = new Gson().fromJson(body, User.class);

    // takes token and check if the token matches a users id, and then changes the users information
    Boolean updatedUser = UserController.updateUser(user, token);

    if (updatedUser){
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("user has been updated").build();
    }else {
      //Return status code 400 if user couldn't update
      return Response.status(400).entity("Could not update user").build();
    }
  }
}
