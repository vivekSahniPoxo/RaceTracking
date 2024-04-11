package com.example.racetracking.retrofit



import com.example.racetracking.sprint.data.SprintDataModel
import com.example.racetracking.bpet.datamodel.EventModel
import com.example.racetracking.bpet.datamodel.SubmitEvent
import com.example.racetracking.bpet.datamodel.SubmitPPTEvent
import com.example.racetracking.bpet.datamodel.UpdateMidTimeModelItem
import com.example.racetracking.race_type.data.PostRaceResultItelItem
import com.example.racetracking.race_type.data.RaceRegsModel
import com.example.racetracking.race_type.data.RaceTypeDataModel
import com.example.racetracking.sprint.data.CreateSprintResultModelItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface RetrofitApi {

    @GET("/api/Events")
    fun getEventS():Call<EventModel>

    @POST("/api/Create-Result")
    fun submitEvent(@Body submitEvent: ArrayList<SubmitEvent>):Call<String>

    @POST("api/Update-Race-Result")
    fun updateMidTime(@Body updateMidTimeModelItem: ArrayList<UpdateMidTimeModelItem>):Call<String>

    @GET("/api/ppet-event")
    fun getPPTEvent():Call<EventModel>

    @POST("/api/create-ppt-result")
    fun createPPTEventResult(@Body submitPPTEvent: ArrayList<SubmitPPTEvent>):Call<String>

    @GET("/api/racetype")
    fun getRaceType():Call<RaceTypeDataModel>

    @GET("/api/event-attendees")
    fun getEventAttandee():Call<RaceRegsModel>

    @POST("/api/Insert-Race-Result")
    fun postRaceResult(@Body postRaceResultItelItem: ArrayList<PostRaceResultItelItem>):Call<String>

    @GET("/api/get-sprint-master")
    fun getSprintData():Call<SprintDataModel>

    @POST("/api/create-sprint-result")
    fun createSprintResult(@Body createSprintResultModelItem: ArrayList<CreateSprintResultModelItem>):Call<String>




}