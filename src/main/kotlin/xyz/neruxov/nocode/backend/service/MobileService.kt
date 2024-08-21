package xyz.neruxov.nocode.backend.service

import org.springframework.stereotype.Service
import xyz.neruxov.nocode.backend.data.mobile.model.MobileDevice
import xyz.neruxov.nocode.backend.data.mobile.repo.MobileDeviceRepository
import xyz.neruxov.nocode.backend.data.user.model.User

@Service
class MobileService(
    val mobileDeviceRepository: MobileDeviceRepository
) {

    fun saveToken(user: User, token: String) {
        val mobileDevice = mobileDeviceRepository.getByUserId(user.id)
            .orElse(MobileDevice(0, user, token))

        mobileDevice.token = token
        mobileDeviceRepository.save(mobileDevice)
    }

}