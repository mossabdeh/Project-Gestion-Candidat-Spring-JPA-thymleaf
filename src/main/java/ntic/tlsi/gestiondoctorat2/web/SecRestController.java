package ntic.tlsi.gestiondoctorat2.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecRestController {

    @GetMapping("/profile")
    public Authentication authentication(Authentication authentication){
        return authentication;
    }
}
