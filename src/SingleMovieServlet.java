import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT M.id, M.title, M.year, M.director,R.rating\n" +
                    "FROM movies M, ratings R\n" +
                    "WHERE M.id = R.movieId AND M.id = ?";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);

                //use id to get genres
                String queryGenreInMovie ="SELECT G.name as 'genre'\n" +
                        "FROM genres G,genres_in_movies GIM\n" +
                        "WHERE G.id = GIM.genreId and GIM.movieId = '"+movie_id+"';";
                Statement statementGenre = conn.createStatement();
                ResultSet rsGenre = statementGenre.executeQuery(queryGenreInMovie);
                JsonArray jsonGenreArray = new JsonArray();
                while(rsGenre.next()){
                    String genre = rsGenre.getString("genre");
                    jsonGenreArray.add(genre);
                }
                //use id to get stars
                String queryStarInMovie ="SELECT S.id, S.name as 'star_name'\n" +
                        "FROM Stars S, stars_in_movies SIM\n" +
                        "WHERE S.id = SIM.starId and SIM.movieId = '"+movie_id+"';";
                Statement statementStar = conn.createStatement();
                ResultSet rsStar = statementStar.executeQuery(queryStarInMovie);
                JsonArray jsonStarArray = new JsonArray();
                while(rsStar.next()){
                    String star_id = rsStar.getString("id");
                    String star_name = rsStar.getString("star_name");
                    JsonObject singleStarJsonObject = new JsonObject();
                    singleStarJsonObject.addProperty("star_id",star_id);
                    singleStarJsonObject.addProperty("star_name",star_name);
                    jsonStarArray.add(singleStarJsonObject);
                }
                //
                jsonObject.add("genres",jsonGenreArray);
                jsonObject.add("stars",jsonStarArray);


//                jsonObject.add("genres",jsonGenreArray);
//                jsonObject.add("stars",jsonStarArray);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
