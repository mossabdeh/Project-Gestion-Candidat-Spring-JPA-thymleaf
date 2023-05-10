package ntic.tlsi.gestiondoctorat2.web;


import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.CFD;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CfdDTO;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cfd")
public class CfdController extends BaseController{
    private final serviceUser serviceUser;

    @Autowired
    public CfdController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @PostMapping
    public ResponseEntity<CfdDTO> addCFD(@RequestBody final CfdDTO cfdDTO){
        CFD cfd = serviceUser.addCFD(CFD.from(cfdDTO));
        return new ResponseEntity<>(CfdDTO.from(cfd), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CfdDTO>> getCFDs(){
        List<CFD>  cfds = serviceUser.getCFDs();
        List<CfdDTO> cfdDTOS = cfds.stream().map(CfdDTO::from).collect(Collectors.toList());
        return new ResponseEntity<>(cfdDTOS,HttpStatus.OK);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<CfdDTO> getCFD(@PathVariable final Long id){
        CFD cfd = serviceUser.getCFD(id);
        return new ResponseEntity<>(CfdDTO.from(cfd),HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<CfdDTO> deleteCFD(@PathVariable final Long id){
        CFD cfd = serviceUser.deleteCfd(id);
        return new ResponseEntity<>(CfdDTO.from(cfd),HttpStatus.OK);
    }

   @PutMapping(value = "{id}")
    public ResponseEntity<CfdDTO> editCFD(@PathVariable final Long id,
                                              @RequestBody final CfdDTO cfdDTO){
        CFD editCfd = serviceUser.editCFD(id,CFD.from(cfdDTO));
        return new ResponseEntity<>(CfdDTO.from(editCfd),HttpStatus.OK);

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

