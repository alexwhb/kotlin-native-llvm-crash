import com.blackstone.ApiClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


class Api (private val client: ApiClient) {
    suspend fun fetchSomeData(): Result<SomeApiDataType> {
        return client.get("/my-api-route")
    }
}


@Serializable
data class SomeApiDataType(
    val name: String,
    @SerialName("thread_count") val threadCount: String
)