package ntic.tlsi.gestiondoctorat2.web;


import ntic.tlsi.gestiondoctorat2.entities.DTO.VdDTO;
import ntic.tlsi.gestiondoctorat2.entities.VD;
import ntic.tlsi.gestiondoctorat2.service.serviceUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vd")
public class VdRestController extends BaseController{
    private final serviceUser serviceUser;

    @Autowired
    public VdRestController(ntic.tlsi.gestiondoctorat2.service.serviceUser serviceUser) {
        this.serviceUser = serviceUser;
    }

    @PostMapping
    public ResponseEntity<VdDTO> addVD(@RequestBody final VdDTO vdDTO){
        VD vd = serviceUser.addVD(VD.from(vdDTO));
        return new ResponseEntity<>(VdDTO.from(vd), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<VdDTO>> getVDs(){
        List<VD>  vds = serviceUser.getVDs();
        List<VdDTO> vdDTOS = vds.stream().map(VdDTO::from).collect(Collectors.toList());
        return new ResponseEntity<>(vdDTOS,HttpStatus.OK);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<VdDTO> getVD(@PathVariable final Long id){
        VD vd = serviceUser.getVD(id);
        return new ResponseEntity<>(VdDTO.from(vd),HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<VdDTO> deleteVD(@PathVariable final Long id){
        VD  vd = serviceUser.deleteVD(id);
        return new ResponseEntity<>(VdDTO.from(vd),HttpStatus.OK);
    }

   @PutMapping(value = "{id}")
    public ResponseEntity<VdDTO> editVD(@PathVariable final Long id,
                                              @RequestBody final VdDTO vdDTO){
        VD editVD = serviceUser.editVD(id,VD.from(vdDTO));
        return new ResponseEntity<>(VdDTO.from(editVD),HttpStatus.OK);

    }

    }










