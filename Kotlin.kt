import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Base64

//Those Classes are to read the Json File and Get the data from it
@kotlinx.serialization.Serializable
data class SnapshotResponse(
    val results: List<Item>
)

@kotlinx.serialization.Serializable
data class Item(
    val confidence: String,
    val image: Image,
    val value: String,
    val state: String,
    val timestamp: String
)

@kotlinx.serialization.Serializable
data class Image(
    val data: String,
    @kotlinx.serialization.SerialName(" width")
    val width: String
)

suspend fun main() {
    val client = HttpClient(CIO){
        install(HttpCookies)
        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    val response: HttpResponse = client.submitForm(
        url = "http://192.168.1.1/login",
        formParameters = Parameters.build {
            append("username", "admin")
            append("password", "secret")
        }//Sending the username and password with x-www-urlencoded form
    )

    val siu: HttpResponse = client.submitForm(
        url = "http://192.168.111.120/system/ntp",
        formParameters = Parameters.build {
            append("ntp", "true")
        }
    ){
         method = HttpMethod.Patch
        }//This variable is patching x-www-form-urlencoded body request from the server. As an example ntp is used.

    // Our main examination is this. This request gets plate results with count parameter. Our count is 2 in example.
    val resp = client.get("http://192.168.111.120/text/results-with-images/2").body<SnapshotResponse>()

    resp.results.forEachIndexed { i, it ->
        val b64data = it.image.data //Gets the output image data in Base64 encoded form.
        println("Image ${i + 1}: license plate: ${it.state} / ${it.value}, time: ${it.timestamp}")//prints Image informations; plate and time.
        val b64Decode = Base64.getDecoder().decode(b64data)
        File("Image$i.jpeg").writeBytes(b64Decode)//Saving the Images into the file with the name of Image1, Image2 etc.

    }
    //val snapshot:HttpResponse = client.get("http://192.168.1.1/live/snapshot") //Use this to get live snapshots.
    //println(snapshot.bodyAsText()) // You can change bodyAsText() to something else.
}

/*
THIS PART CONTAINS ALTERNATIVE USAGES OF THE MAIN CODES. IN NEED YOU CAN GET SOME INFORMATION HERE.
**************************************************************************************************
Different Style of initializing forEachIndexed
    val n = resp.results.size
    for (i in 0 until n) {
        val it = resp.results[i]
        val b64data = it.image.data
        println("Image ${i + 1}: lıcense plate: ${it.state} / ${it.value}, width: ${it.image.width}")
    }
*/

    /*
    withContext(Dispatchers.IO) {
        FileWriter("Test.json").use { it.write(photo.bodyAsText()) }
        FileWriter("Test-data.txt").use { writer ->
            writer.write(resp.results[0].image.data)
        }
    }

    resp.results.forEach { it ->
        val b64data = it.image.data
        println("Next Image: license plate: ${it.state} / ${it.value}, width: ${it.image.width}")
    }
*/


