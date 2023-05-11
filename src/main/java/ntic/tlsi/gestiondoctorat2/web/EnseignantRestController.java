package ntic.tlsi.gestiondoctorat2.web;


import ntic.tlsi.gestiondoctorat2.entities.DTO.EnseignantDTO;
import ntic.tlsi.gestiondoctorat2.entities.Enseignant;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/Restenseignant")
public class EnseignantRestController extends BaseController{
    private final serviceUser serviceUser;

    @Autowired
    public EnseignantRestController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @PostMapping
    public ResponseEntity<EnseignantDTO> addEnseignant(@RequestBody final EnseignantDTO enseignantDTO){
        Enseignant enseignant = serviceUser.addEnseignant(Enseignant.from(enseignantDTO));
        return new ResponseEntity<>(EnseignantDTO.from(enseignant), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<EnseignantDTO>> getEnseignants(){
        List<Enseignant>  enseignants = serviceUser.getEnseignants();
        List<EnseignantDTO> enseignantDTOS = enseignants.stream().map(EnseignantDTO::from).collect(Collectors.toList());
        return new ResponseEntity<>(enseignantDTOS,HttpStatus.OK);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<EnseignantDTO> getEnseignant(@PathVariable final Long id){
        Enseignant enseignant = serviceUser.getEnseignant(id);
        return new ResponseEntity<>(EnseignantDTO.from(enseignant),HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<EnseignantDTO> deleteEnseignant(@PathVariable final Long id){
        Enseignant enseignant = serviceUser.deleteEnseignant(id);
        return new ResponseEntity<>(EnseignantDTO.from(enseignant),HttpStatus.OK);
    }

   @PutMapping(value = "{id}")
    public ResponseEntity<EnseignantDTO> editEnseignant(@PathVariable final Long id,
                                              @RequestBody final EnseignantDTO enseignantDTO){
       Enseignant editEnseignant = serviceUser.editEnseignant(id,Enseignant.from(enseignantDTO));
        return new ResponseEntity<>(EnseignantDTO.from(editEnseignant),HttpStatus.OK);

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

