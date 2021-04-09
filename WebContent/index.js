function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let starTableBodyElement = jQuery("#movie_table_body");
    console.log(resultData.length);
    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";

        //genres
        rowHTML += "<th>";
        for(let j = 0; j < resultData[i]["genres"].length; j++){
            rowHTML += resultData[i]["genres"][j];
            rowHTML +=",";
        }
        //删除多余的逗号
        rowHTML = rowHTML.substring(0,rowHTML.length-1);
        rowHTML += "</th>";

        //stars
        rowHTML += "<th>";
        for(let j = 0; j < resultData[i]["stars"].length; j++){
            rowHTML += '<a href="single-star.html?id=' + resultData[i]["stars"][j]['star_id'] + '">'
                + resultData[i]["stars"][j]['star_name'] +     // display star_name for the link text
                '</a>';
            rowHTML+= ",";
        }
        //删除多余的逗号
        rowHTML = rowHTML.substring(0,rowHTML.length-1);
        rowHTML += "</th>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movie-list", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});