/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.mahasen.client.Upload;
import org.mahasen.exception.MahasenClientException;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Manager {

  DbConnection dbConnection;
  Connection con;

  public Manager(){
    this.intialize();
  }

 private void intialize(){
     dbConnection = new DbConnection();
     try {
         con = dbConnection.getConnection();
     } catch (SQLException e) {
         System.out.println("Error while connecting to database");
     }
 }

    public void loadMovieFiles() throws SQLException {
        File folder = new File(Configuration.UPLOAD_FOLDER);
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String movieName = this.getFilm((i+1)*8);
            movieName = movieName.trim().toLowerCase().replace(" ","_");

            System.out.println(movieName);
            file.renameTo(new File(Configuration.UPLOAD_FOLDER +File.separator+ movieName+".mp4"));
        }
        System.out.println("Renamed :"+ files.length + "Files");
 }

 public String getFilm(int id) throws SQLException {

     Statement statement = con.createStatement();
     String query = "select * from sakila.film where film_id="+id;
     ResultSet resultSet = statement.executeQuery(query);
     String movieTitle = null;
       while (resultSet.next()){
      movieTitle = resultSet.getString("title");
       }
     System.out.println("Selected file name :" + movieTitle);
     return  movieTitle;
 }

 public int getFilmId(String filmName) throws SQLException {
      Statement statement = con.createStatement();
     String query = "select * from sakila.film where title="+"'"+filmName+"'";
     ResultSet resultSet = statement.executeQuery(query);
     int movieId = 0;
       while (resultSet.next()){
     movieId = resultSet.getInt("film_id");
       }
     //System.out.println("Selected file name :" + movieTitle);
     return  movieId;
 }

 public void closeConnection() throws SQLException {
     if(con != null){
         con.close();
     }
 }

    public static void main(String[] args) throws SQLException {
        Manager manager = new Manager();
        try {
           //manager.loadMovieFiles();
            try {
                manager.uploadFiles();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MahasenClientException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            manager.closeConnection();
        }
    }

    public void uploadFiles() throws SQLException, IOException, MahasenClientException {
      Client client = new Client();
      Upload upload = new Upload(client.getLoginData());

          File folder = new File(Configuration.UPLOAD_FOLDER);
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String movieName = files[i].getName();
            System.out.println("uploading "+movieName+"Movie no :"+i);
            movieName = movieName.trim().toUpperCase().replace("_"," ").replace(".MP4","");
            int filmId = getFilmId(movieName);
            try{


            upload.upload(file, getTags(filmId), "/mahasen", getNameValuePairs(filmId));

            }catch(Exception e){
                System.out.println("Skipping error");
            }
            }

    }

    public List<NameValuePair> getNameValuePairs(int filmId) throws SQLException {

         ArrayList<NameValuePair> nameValuePairsList = new ArrayList<NameValuePair>();

        // adding film table info
        Statement statement = con.createStatement();
        String query = "select * from sakila.film where film_id="+filmId;
        ResultSet resultSet = statement.executeQuery(query);

       while (resultSet.next()){
       NameValuePair nameValuePair1 = new BasicNameValuePair("title",resultSet.getString("title"));
       NameValuePair nameValuePair2 = new BasicNameValuePair("length",String.valueOf(resultSet.getInt("length")));
       NameValuePair nameValuePair3 = new BasicNameValuePair("rating",resultSet.getString("rating"));
       NameValuePair nameValuePair4 = new BasicNameValuePair("release_year",resultSet.getString("release_year"));
       NameValuePair nameValuePair5 = new BasicNameValuePair("rental_rate",String.valueOf(resultSet.getInt("rental_rate")));
       NameValuePair nameValuePair6 = new BasicNameValuePair("description",resultSet.getString("description"));

       nameValuePairsList.add(nameValuePair1);
       nameValuePairsList.add(nameValuePair2);
       nameValuePairsList.add(nameValuePair3);
       nameValuePairsList.add(nameValuePair4);
       nameValuePairsList.add(nameValuePair5);
       nameValuePairsList.add(nameValuePair6);

       }

        //adding movie category
       query = "select * from sakila.film_category Inner JOIN sakila.category ON " +
               "sakila.film_category.category_id = sakila.category.category_id where film_id="+filmId;

       resultSet = statement.executeQuery(query);

         while (resultSet.next()){
         NameValuePair nameValuePair7 = new BasicNameValuePair("category", resultSet.getString("name"));
          nameValuePairsList.add(nameValuePair7);
         }

         // adding actors list
        query = "select * from sakila.actor Inner JOIN sakila.film_actor ON sakila.actor.actor_id = " +
                "sakila.film_actor.actor_id where film_id="+filmId;

        String actorList = "";
         int i = 1;
         resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            actorList = actorList + "," + resultSet.getString("first_name");
            nameValuePairsList.add(new BasicNameValuePair("actor"+i, resultSet.getString("first_name")));
            System.out.println(actorList);
            i ++;
        }
        nameValuePairsList.add(new BasicNameValuePair("actors", actorList));

        // adding customer properties

        return nameValuePairsList;



    }

    public String getTags(int filmId) throws SQLException {

        Statement statement = con.createStatement();
        String tags = "";

        String desQuery = "select * from sakila.film where film_id="+filmId;
        ResultSet rs = statement.executeQuery(desQuery);
        while (rs.next()) {
            String x = rs.getString("special_features");
            String rating = rs.getString("description");
            String[] description = rating.split(" ");
            String[] features = x.split(",");

            for (String tag : description) {
                tags = tags +","+ tag;
            }
            for (String tag : features) {
                tags = tags +","+ tag;
            }

            // getting actor names
            String query = "select * from sakila.actor Inner JOIN sakila.film_actor ON sakila.actor.actor_id = " +
                    "sakila.film_actor.actor_id where film_id=" + filmId;

            rs = statement.executeQuery(query);
            while (rs.next()) {
                tags = tags + "," + rs.getString("first_name");
            }

        }
        System.out.println("Adding tags :" + tags);
        return tags;
    }
}
