package ntic.tlsi.gestiondoctorat2.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BaseController {
    @GetMapping("/")
    public String homePage() {
        return "index"; // index.html is your home page template file
    }
}
