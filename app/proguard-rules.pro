# Moshi
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keep class com.minepapa.kakaonotification.data.remote.sheets.model.** { *; }
-keep class com.minepapa.kakaonotification.domain.model.** { *; }

# Room
-keep class com.minepapa.kakaonotification.data.local.db.entity.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep interface retrofit2.** { *; }

# Service + DI entry points
-keep class com.minepapa.kakaonotification.service.** { *; }
-keep class com.minepapa.kakaonotification.core.di.** { *; }
