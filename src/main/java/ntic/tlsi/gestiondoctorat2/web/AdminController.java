package ntic.tlsi.gestiondoctorat2.web;


import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController{
    private final serviceUser serviceUser;

    @Autowired
    public AdminController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @PostMapping
    public ResponseEntity<AdminDTO> addAdmin(@RequestBody final AdminDTO adminDTO){
        Admin admin = serviceUser.addAdmin(Admin.from(adminDTO));
        return new ResponseEntity<>(AdminDTO.from(admin), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<AdminDTO>> getAdmins(){
        List<Admin>  admins = serviceUser.getAdmins();
        List<AdminDTO> adminDTOs = admins.stream().map(AdminDTO::from).collect(Collectors.toList());
        return new ResponseEntity<>(adminDTOs,HttpStatus.OK);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<AdminDTO> getAdmin(@PathVariable final Long id){
        Admin admin = serviceUser.getAdmin(id);
        return new ResponseEntity<>(AdminDTO.from(admin),HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<AdminDTO> deleteAdmin(@PathVariable final Long id){
        Admin admin = serviceUser.deleteAdmin(id);
        return new ResponseEntity<>(AdminDTO.from(admin),HttpStatus.OK);
    }

   @PutMapping(value = "{id}")
    public ResponseEntity<AdminDTO> editAdmin(@PathVariable final Long id,
                                              @RequestBody final AdminDTO adminDTO){
        Admin editAdmin = serviceUser.editAdmin(id,Admin.from(adminDTO));
        return new ResponseEntity<>(AdminDTO.from(editAdmin),HttpStatus.OK);

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

