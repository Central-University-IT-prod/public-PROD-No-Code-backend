package xyz.neruxov.nocode.backend.service

import org.springframework.stereotype.Service
import org.springframework.web.servlet.view.RedirectView
import xyz.neruxov.nocode.backend.data.links.repo.LinkRepository
import xyz.neruxov.nocode.backend.exception.type.StatusCodeException

@Service
class LinkService(
    val linkRepository: LinkRepository
) {

    fun getLink(code: String): RedirectView {
        val link = linkRepository.findByCode(code)
            .orElseThrow { throw StatusCodeException(404, "Ссылка не найдена") }

        link.clicks++
        linkRepository.save(link)

        return RedirectView(link.url)
    }

}