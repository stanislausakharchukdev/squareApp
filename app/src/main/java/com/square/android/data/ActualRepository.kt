package com.square.android.data

import com.square.android.SOCIAL
import com.square.android.data.local.LocalDataManager
import com.square.android.data.network.ApiService
import com.square.android.data.network.response.AuthResponse
import com.square.android.data.network.response.ERRORS
import com.square.android.data.network.response.MessageResponse
import com.square.android.data.pojo.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import retrofit2.Call
import retrofit2.HttpException

private const val TOKEN_PREFIX = "Bearer "

class ActualRepository(private val api: ApiService,
                       private val localManager: LocalDataManager) : Repository {
    override fun getIntervals(placeId: Long, date: String) =
            api.getIntervals(placeId, date)

    override fun addOfferToBook(bookId: Long, offerId: Long) = performRequest {
        api.addOfferToBook(bookId, OfferToBook(offerId))
    }

    override fun getBadgeCount(): Deferred<BadgeInfo> {
        val id = getUserInfo().id

        return api.getBadgeCount(id)
    }

    override fun getFeedbackContent(placeId: Long) = api.getFeedbackBody(placeId)

    override fun getPlaceOffers(placeId: Long) = api.getPlaceOffers(placeId)

    override fun addReview(offerId: Long, info: ReviewInfo) = performRequest {
        api.addReview(offerId, info)
    }

    override fun claimOffer(offerId: Long) = performRequest {
        api.claimRedemption(offerId)
    }

    override fun setSocialLink(username: String) {
        val link = SOCIAL.SOCIAL_LINK_FORMAT.format(username)
        localManager.setSocialLink(link)
    }

    override fun getOffer(offerId: Long) = GlobalScope.async {
        val userId = getUserInfo().id
        val data = performRequest { api.getOffer(offerId, userId) }

        val currentUserId = getUserInfo().id

        data.posts = data.posts.filter { it.user == currentUserId }.toMutableList()

        data
    }

    override fun setUserName(name: String, surname: String) {
        localManager.setUserName("$name $surname")
    }

    override fun getUserInfo() = localManager.getUserInfo()

    override fun setAvatarUrl(url: String) {
        localManager.setAvatarUrl(url)
    }

    override fun getRedemption(redemptionId: Long) = api.getRedemption(redemptionId)

    override fun clearUserData() {
        localManager.clearUserData()
    }

    override fun deleteRedemption(id: Long) = api.deleteRedemption(id)

    override fun getRedemptions(): Deferred<List<RedemptionInfo>> {
        return api.getRedemptions(getUserInfo().id)
    }

    override fun setUserId(id: Long) {
        localManager.setId(id)
    }

    override fun book(placeId: Long, bookInfo: BookInfo): Deferred<MessageResponse> = GlobalScope.async {
        val data = performRequestCheckingMessage { api.book(placeId, bookInfo) }

        data
    }

    override fun getPlace(id: Long): Deferred<Place> {
        return api.getPlace(id)
    }

    override fun getPlaces(): Deferred<List<Place>> = GlobalScope.async {
        val places = performRequest { api.getPlaces() }

        places.forEach { place ->
            val prices = place.offers.map { it.price }

            place.award = prices.min() ?: 0
        }

        places
    }

    override fun setLoggedIn(isLogged: Boolean) {
        localManager.setLoggedIn(isLogged)
    }

    override fun getCurrentUser(): Deferred<Profile.User> = api.getCurrentProfile()

    override fun isProfileFilled() = localManager.isProfileFilled()

    override fun setProfileFilled(isFilled: Boolean) {
        localManager.setProfileFilled(isFilled)
    }

    override fun setUserToken(token: String) {
        localManager.setAuthToken(TOKEN_PREFIX + token)
    }

    override fun fillProfile(info: ProfileInfo): Deferred<MessageResponse> = GlobalScope.async {
        val data = performRequestCheckingMessage { api.editProfile(info) }

        data
    }

    override fun registerUser(instagramCode: String): Deferred<AuthResponse> = GlobalScope.async {
        val data = performRequest { api.registerUser(instagramCode) }

        if (data.token.isNullOrEmpty()) {
            throw Exception(data.message)
        }

        data
    }

    override fun introDisplayed() {
        localManager.setShouldDisplayIntro(false)
    }

    override fun shouldDisplayIntro(): Boolean = localManager.shouldDisplayIntro()


    override fun isLoggedIn(): Boolean = localManager.isLoggedIn()

    private inline fun <T> performRequest(block: () -> Call<T>): T {
        val response = block().execute()

        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw HttpException(response)
        }
    }

    private inline fun performRequestCheckingMessage(block: () -> Call<MessageResponse>): MessageResponse {
        val result = performRequest(block)

        if (result.message in ERRORS) {
            throw Exception(result.message)
        }

        return result
    }
}