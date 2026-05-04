package com.minepapa.kakaonotification.data.remote.sheets

import com.minepapa.kakaonotification.data.remote.sheets.model.AppendRequest
import com.minepapa.kakaonotification.data.remote.sheets.model.AppendResponse
import com.minepapa.kakaonotification.data.remote.sheets.model.GetValuesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleSheetsApi {

    @POST("v4/spreadsheets/{spreadsheetId}/values/{range}:append")
    suspend fun appendValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path("range") range: String,
        @Query("valueInputOption") valueInputOption: String = "USER_ENTERED",
        @Body body: AppendRequest,
    ): AppendResponse

    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path(value = "range", encoded = true) range: String,
    ): GetValuesResponse

    // valueRenderOption=FORMULA 로 수식 문자열 그대로 반환
    @GET("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun getFormulas(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path(value = "range", encoded = true) range: String,
        @Query("valueRenderOption") valueRenderOption: String,
    ): GetValuesResponse

    // 특정 셀/범위 값 덮어쓰기
    @PUT("v4/spreadsheets/{spreadsheetId}/values/{range}")
    suspend fun updateValues(
        @Path("spreadsheetId") spreadsheetId: String,
        @Path(value = "range", encoded = true) range: String,
        @Query("valueInputOption") valueInputOption: String = "USER_ENTERED",
        @Body body: AppendRequest,
    ): AppendResponse
}
