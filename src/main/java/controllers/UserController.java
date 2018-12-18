package controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cbsexam.UserEndpoints;
import model.User;
import utils.Hashing;
import utils.Log;
import com.auth0.jwt.algorithms.Algorithm;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                    rs.getLong("created_at"));



        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                    rs.getLong("created_at"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  // update users informations
  public static boolean updateUser(User user, String token) {

    //Check for the database connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Decode the token and return an id
    try {
      DecodedJWT jwt = JWT.decode(token);
      int id = jwt.getClaim("userID").asInt();

      // A prepared statement to the database that it should update user with the id
      try {
        PreparedStatement updateUser = dbCon.getConnection().prepareStatement("UPDATE user SET " +
                "first_name=?, last_name=?, password=?, email=? WHERE id=?");

        updateUser.setString(1, user.getFirstname());
        updateUser.setString(2, user.getLastname());
        updateUser.setString(3, Hashing.addSaltSha(user.getPassword()));
        updateUser.setString(4, user.getEmail());
        updateUser.setInt(5, id);

        int affectedRows = updateUser.executeUpdate();

        // if affected rows equals 1, then we know that the change has been committed.
        if (affectedRows == 1){
          // force the cache to update since there have been added som changes to the database
          UserEndpoints.userCache.getUsers(true);
          return true;
        }

      }catch (SQLException ex) {
        ex.printStackTrace();
      }
    }catch (JWTDecodeException ex){
      ex.printStackTrace();
    }

    return false;
  }

  // delete user
  public static Boolean deleteUser(String token){

    // check for database connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // decode the token to an id
    try {

      DecodedJWT jwt = JWT.decode(token);
      int id = jwt.getClaim("userID").asInt();

      // a prepared statement that deletes the user with the exact same id.
      try {
        PreparedStatement deleteUser = dbCon.getConnection().prepareStatement("DELETE FROM user WHERE id=?");

        deleteUser.setInt(1, id);

        int affectedRows = deleteUser.executeUpdate();

        // if affected rows equals 1, then the user has succesfully been deleted.
        if (affectedRows == 1){
          // force the cache to update since there have been added som changes to the database
          UserEndpoints.userCache.getUsers(true);
          return true;
        }

      } catch (SQLException ex) {
        ex.printStackTrace();
      }

    }catch (JWTDecodeException ex) {
      ex.printStackTrace();
    }

    return false;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it: FIX
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + Hashing.addSaltSha(user.getPassword())
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // force the cache to update since there have been added som changes to the database
    UserEndpoints.userCache.getUsers(true);
    // Return user
    return user;
  }

  // login user
  public static String loginUser(User user){

    // checks database connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    ResultSet rs;
    User userLogin;
    String token = null;

    // making af prepared statement to database to find a user with the same password and email
    try {
      PreparedStatement loginUser = dbCon.getConnection().prepareStatement("SELECT * FROM user WHERE email=? AND" +
              " password=?");

      loginUser.setString(1, user.getEmail());
      loginUser.setString(2, Hashing.addSaltSha(user.getPassword()));


      rs = loginUser.executeQuery();

      //Going through resultset to find the users information
      if (rs.next()) {
        userLogin = new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"));

        if (userLogin != null){
          try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            token = JWT.create()
                    .withClaim("userID", userLogin.getId())
                    .withIssuer("auth0")
                    .sign(algorithm);
          } catch (JWTCreationException ex){

          } finally {
            //if a User is found return a token
            return token;
          }
        }
      }else {
        System.out.println("Could not find user");
      }

    } catch (SQLException ex){
      ex.printStackTrace();
    }
    return "";
  }
}
