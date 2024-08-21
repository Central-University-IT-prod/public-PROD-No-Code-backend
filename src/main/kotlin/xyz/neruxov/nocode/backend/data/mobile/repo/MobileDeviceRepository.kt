package xyz.neruxov.nocode.backend.data.mobile.repo

import org.springframework.data.jpa.repository.JpaRepository
import xyz.neruxov.nocode.backend.data.mobile.model.MobileDevice
import java.util.*

interface MobileDeviceRepository : JpaRepository<MobileDevice, Long> {

    fun getByUserId(userId: Long): Optional<MobileDevice>

}