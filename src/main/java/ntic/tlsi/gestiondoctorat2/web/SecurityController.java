package ntic.tlsi.gestiondoctorat2.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityController {

    @GetMapping("/notAuthorized")
    public String NotAuthorized(){
        return "NotAuthorized" ;
    }
}
