package ntic.tlsi.gestiondoctorat2.web;


import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.DTO.AdminDTO;
import ntic.tlsi.gestiondoctorat2.entities.DTO.CandidatDTO;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/candidat")
public class CandidatController extends BaseController{
    private final serviceUser serviceUser;

    @Autowired
    public CandidatController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @PostMapping
    public ResponseEntity<CandidatDTO> addCandidat(@RequestBody final CandidatDTO candidatDTO){
        Candidat candidat = serviceUser.addCandidat(Candidat.from(candidatDTO));
        return new ResponseEntity<>(CandidatDTO.from(candidat), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CandidatDTO>> getCandidats(){
        List<Candidat>   candidats = serviceUser.getCandidats();
        List<CandidatDTO> candidatDTOS = candidats.stream().map(CandidatDTO::from).collect(Collectors.toList());
        return new ResponseEntity<>(candidatDTOS,HttpStatus.OK);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<CandidatDTO> getCandidat(@PathVariable final Long id){
        Candidat  candidat = serviceUser.getCandidat(id);
        return new ResponseEntity<>(CandidatDTO.from(candidat),HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<CandidatDTO> deleteCandidat(@PathVariable final Long id){
        Candidat candidat = serviceUser.deleteCandidat(id);
        return new ResponseEntity<>(CandidatDTO.from(candidat),HttpStatus.OK);
    }

   @PutMapping(value = "{id}")
    public ResponseEntity<CandidatDTO> editCandidat(@PathVariable final Long id,
                                              @RequestBody final CandidatDTO candidatDTO){
        Candidat editCandidat = serviceUser.editCandidat(id,Candidat.from(candidatDTO));
        return new ResponseEntity<>(CandidatDTO.from(editCandidat),HttpStatus.OK);

    }

    }












