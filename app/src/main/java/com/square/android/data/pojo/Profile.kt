package com.square.android.data.pojo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class Profile(var message: String = "",
              var user: User = User()) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    class User(@field:JsonProperty("_id") var id: Long = 0,
               var newUser: Boolean = false,
               var accepted: Boolean = false,
               var admin: Boolean = false,
               var city: String = "",
               var currentAgency: String = "",
               var email: String = "",
               var gender: String = "",
               var credits: Long = 0,
               var birthDate: String = "",
               var instagram: Instagram = Instagram(),
               var level: Int = 0,
               var referralCode: String = "",
               var motherAgency: String = "",
               var name: String = "",
               var nationality: String = "",
               var phone: String = "",
               var photo: String = "",
               var surname: String = "") {

        class Instagram(var counts: Counts = Counts(),
                        @field:JsonProperty("full_name") var fullName: String = "",
                        var id: String = "",
                        var username: String = "") {

            class Counts(@field:JsonProperty("followed_by")
                         var followedBy: Int = 0,
                         var follows: Int = 0,
                         var media: Int = 0)
        }
    }
}