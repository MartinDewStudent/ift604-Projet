package projet.ift604.broomitclient

import android.location.Location
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import projet.ift604.broomitclient.api.UserService
import projet.ift604.broomitclient.models.Task
import projet.ift604.broomitclient.models.User
import retrofit2.Call
import java.lang.Math.pow
import java.lang.Math.sqrt

class ApplicationState {
    class HttpException(val code: Int, val msg: String = "") : Throwable()

    val loggedIn: Boolean get() = _user != null

    var user: User
        get() = _user!!
        set(value) { _user = value }

    var _user: User? = null

    @Throws(HttpException::class)
    fun <T> callAPI(call: Call<T>): T? {
        val resp = call.execute()
        val body = resp.body()

        if (resp.code() in 200..299) {
            if (body != null)
                return body
        } else {
            val err = resp.errorBody()
            if (err != null)
                throw HttpException(resp.code(), err.string())
            throw HttpException(resp.code())
        }
        return null
    }

    @Throws(HttpException::class)
    fun getUser(userId: String) {
        val service = UserService.getInstance()

        _user = callAPI(service.getUser(userId))
    }


    // This logs in the server and fetches the user
    @Throws(HttpException::class)
    fun login(loginReq: UserService.LoginRequest) {
        val service = UserService.getInstance()
        val userId = callAPI(service.login(loginReq))

        getUser(userId!!)
    }

    fun logout() {
        _user = null
    }

    @Throws(HttpException::class)
    fun create(createReq: UserService.CreateRequest) {
        val service = UserService.getInstance()

        // fetch user with username and password
        val userId = callAPI(service.create(createReq))

        getUser(userId!!)
    }

    fun getScheduleTasks(): ArrayList<Task> {
        if (!loggedIn) throw Exception("User not loaded")

        val tasks = ArrayList<Task>()
        for (loc in user.locations) {
            for (task in loc.tasks) {
                if (task.isScheduled()) {
                    task.parent = loc
                    tasks.add(task)
                }
            }
        }

        return tasks
    }

    fun getLocationInProximity(loc: Location, range: Double): ArrayList<projet.ift604.broomitclient.models.Location> {
        if (!loggedIn) throw Exception("User not loaded")

        val locs = ArrayList<projet.ift604.broomitclient.models.Location>()

        user.locations.forEach {

            val LocLong = it.position.longitude
            val LocLat = it.position.latitude

            if (sqrt(pow((LocLong - loc.longitude), 2.0) + pow(LocLat - loc.latitude, 2.0)) < range)
                locs.add(it)
        }

        return locs
    }

    // Refreshes the user loaded with the api one
    @Throws(HttpException::class)
    fun refreshUser() {
        if (!loggedIn) throw Exception("User not loaded")

        getUser(user.id)
    }

    // Updates the user on the api with the current loaded user
    @Throws(HttpException::class)
    fun updateUser(passwordModified: Boolean = false) {
        if (!loggedIn) throw Exception("User not loaded")

        val service = UserService.getInstance()

        if (!passwordModified)
            user.password = ""

        callAPI(service.updateUser(user.id, user))
    }

    companion object {
        private var _instance: ApplicationState? = null

        val instance: ApplicationState
            get() {
            if (_instance == null)
                _instance = ApplicationState()

            return _instance as ApplicationState
        }
    }
}