package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user_profile ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdOnce(id: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET pinCode = :pin, isPinRequired = :isRequired WHERE id = :id")
    suspend fun updateSecurityPinForUser(id: Long, pin: String?, isRequired: Boolean)

    @Query("UPDATE user_profile SET pinCode = :pin, isPinRequired = :isRequired WHERE id = 1")
    suspend fun updateSecurityPin(pin: String?, isRequired: Boolean)

    @Query("UPDATE user_profile SET defaultCurrency = :currency, usdToZigRate = :rate WHERE id = :id")
    suspend fun updateCurrencySettingsForUser(id: Long, currency: String, rate: Double)

    @Query("UPDATE user_profile SET defaultCurrency = :currency, usdToZigRate = :rate WHERE id = 1")
    suspend fun updateCurrencySettings(currency: String, rate: Double)

    @Query("DELETE FROM user_profile WHERE id = :id")
    suspend fun deleteUser(id: Long)
}
