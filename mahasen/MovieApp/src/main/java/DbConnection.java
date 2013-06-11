
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

import java.sql.*;
import java.util.Properties;

public class DbConnection {

    private String userName = "root";
    private String password = "1234" ;
    private String dbms ="mysql";
    private String serverName = "localhost";
    private int portNumber = 3306;
    private String dbName = "sakila";

    public static void main(String[] args) {
       DbConnection connection = new DbConnection();
        try {
            Connection con = connection.getConnection();
            connection.printResutls(con );
           // con.close();

            for(int i =0 ; i < 10 ; i++){
             String tags =connection.getTags(i, con);
                System.out.println("tags :"+tags);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
     public Connection getConnection() throws SQLException
        {
    Connection conn = null;
    Properties connectionProps = new Properties();
    connectionProps.put("user", this.userName);
    connectionProps.put("password", this.password);

    if (this.dbms.equals("mysql")) {
      conn = DriverManager.
        getConnection("jdbc:" + this.dbms + "://" + this.serverName +
                      ":" + this.portNumber + "/", connectionProps);
    } else if (this.dbms.equals("derby")) {
      conn = DriverManager.
        getConnection("jdbc:" + this.dbms + ":" + this.dbName + ";create=true", connectionProps);
    }
    System.out.println("Connected to database");

    return conn;
  }

    public void printResutls(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
  ResultSet rs = stmt.executeQuery("select * from sakila.film ");
  while (rs.next()) {
    String x = rs.getString("special_features");
    String rating = rs.getString("description");
      String[] tags =rating.split(" ");
      String[]features = x.split(",");
      for(String tag : tags){
          System.out.println("tag : "+tag);
      }
         for(String feature : features){
      System.out.println("special features: "+ feature);
        }
  }

    }

    public String getTags(int filmId, Connection con) throws SQLException {

        Statement statement = con.createStatement();
        String tags = "";

        ResultSet rs = statement.executeQuery("select * from sakila.film where film_id="+filmId);
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
        return tags;
    }


}
