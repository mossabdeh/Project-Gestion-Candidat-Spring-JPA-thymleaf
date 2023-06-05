package ntic.tlsi.gestiondoctorat2.web;


import jakarta.validation.Valid;
import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController{

    @GetMapping("/test")
    public String test(Authentication authentication, Model model){
        model.addAttribute("username",authentication.getName());
        model.addAttribute("roles",authentication.getAuthorities());
        return "Test";

    }
    private final serviceUser serviceUser;
    @Autowired
    private AdminRepo adminRepo ;

    @Autowired
    public AdminController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @GetMapping("/AdminAdd")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String AdminAdminAdd(Model model){
        model.addAttribute("admin",new Admin());
        return "AdminAdminAdd";
    }
    @PostMapping("/saveAdmin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")

    public String saveAdmin(Model model , @Valid Admin admin , BindingResult bindingResult){
        if (bindingResult.hasErrors()) return "AdminAdminAdd";

        // Check if username already exists
        if (adminRepo.existsByUsername(admin.getUsername())) {
            bindingResult.rejectValue("username", "error.admin", "Username already exists");
            return "AdminAdminAdd";
        }
        admin.setTypeRole(Role.ADMIN);
        admin.setLogDate(new Date());
        // Set the password as the same value as the username
        admin.setPassword(admin.getUsername());
        adminRepo.save(admin);
        return "redirect:/admin/getAdmins";
    }


    @GetMapping("/getAdmins")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getAdmins(Model model,
                            @RequestParam(name = "page",defaultValue = "0") int page,
                            @RequestParam(name = "size",defaultValue = "5") int size,
                            @RequestParam(name = "keyword",defaultValue = "") String keyword) {
        Page<Admin> pageAdmins=adminRepo.findByNomContains(keyword, PageRequest.of(page,size));
        model.addAttribute("ListAdmins",pageAdmins.getContent());
        model.addAttribute("pages",new int[pageAdmins.getTotalPages()]);
        model.addAttribute("currentPage",page);
        model.addAttribute("keyword",keyword);

        return "AdminAdmin";
    }

  /*  @GetMapping(value = "{id}")
    public ResponseEntity<AdminDTO> getAdmin(@PathVariable final Long id){
        Admin admin = serviceUser.getAdmin(id);
        return new ResponseEntity<>(AdminDTO.from(admin),HttpStatus.OK);
    }*/

    @GetMapping("/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteAdmin(final Long id,String keyword,int page){
       adminRepo.deleteById(id);
        return "redirect:/admin/getAdmins?page="+page+"&keyword="+keyword;
    }

   @GetMapping("/editAdmin")
   @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editAdmin(Model model,Long id,String keyword,int page){

        Admin editAdmin = adminRepo.findAdminById(id);
        String existedPassword = editAdmin.getPassword();
        editAdmin.setTypeRole(Role.ADMIN);
        editAdmin.setLogDate(new Date());
        editAdmin.setPassword(existedPassword);
        model.addAttribute("admin",editAdmin);
       model.addAttribute("page",page);
       model.addAttribute("keyword",keyword);

        return "AdminAdminEdit";

    }

    }











  /*  @Autowired
    private serviceUser userService;

    @PostMapping("/user")
    public String addUser(@ModelAttribute("user") User user, Model model) {
        User newUser = userService.addUser(user);
        model.addAttribute("message", "New user added with id: " + newUser.getId());
        return "success";
    }

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable paging = PageRequest.of(page, size);
        Page<User> users = userService.getUsers();

        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }*/

