package com.example.ElectricStations.Controller;

import com.example.ElectricStations.entities.Borne;
import com.example.ElectricStations.entities.Stations;
import com.example.ElectricStations.enums.Connecteur;
import com.example.ElectricStations.enums.Dispo;
import com.example.ElectricStations.enums.Mode;
import com.example.ElectricStations.repositories.BorneRepository;
import com.example.ElectricStations.services.BorneService;
import com.example.ElectricStations.services.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/electricbornes")
public class BorneController {
    private final StationService stationService;
    private final BorneService borneService;
    @Autowired
    private BorneRepository borneRepository;

    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }
    public BorneController(StationService stationService, BorneService borneService) {
        this.stationService = stationService;
        this.borneService = borneService;
    }
    @GetMapping("/AllBornes")
    public ResponseEntity<Map<String, Object>> getAllBornesPage(
            @RequestParam(required = false) Dispo disponibilite,
            @RequestParam(required = false) Float puissance,
            @RequestParam(required = false) Mode mode,
            @RequestParam(required = false) Connecteur connecteur,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

        try {
            List<Sort.Order> orders = new ArrayList<Sort.Order>();

            if (sort[0].contains(",")) {
                // will sort more than 2 fields
                // sortOrder="field, direction"
                for (String sortOrder : sort) {
                    String[] _sort = sortOrder.split(",");
                    orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
                }
            } else {
                // sort=[field, direction]
                orders.add(new Sort.Order(getSortDirection(sort[1]), sort[0]));
            }

            List<Borne> bornes = new ArrayList<Borne>();
            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

            Page<Borne> pageTuts = null;
            if (disponibilite !=null) {
                pageTuts = borneRepository.findAll(pagingSort);
                borneService.rechercheBorne( Optional.ofNullable(puissance), Optional.ofNullable(String.valueOf(mode)), Optional.ofNullable(String.valueOf(connecteur)));
            } else {
                pageTuts = borneRepository.findAll(pagingSort);
            }

//            else
//                pageTuts = borneRepository.findByTitleContaining(title, pagingSort);

            bornes = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("datas", bornes);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping
    public ResponseEntity<Boolean> save(@RequestBody Borne borne) {
        boolean isSuccess = borneService.save(borne);
        return ResponseEntity.ok(isSuccess);
    }

    @GetMapping
    public ResponseEntity<List<Borne>> findAll() {
        return ResponseEntity.ok(borneService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Borne> findById(@PathVariable("id") String id) {
        return ResponseEntity.ok(borneService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateBorne(@PathVariable String id, @RequestBody Borne newBorne) {
        boolean isSuccess = borneService.updateBorne(id, newBorne);
        return ResponseEntity.ok(isSuccess);
    }



    @DeleteMapping("/{borne-id}")
    public ResponseEntity<Void> delete(@PathVariable("borne-id") String id) {
        borneService.delete(id);
        return ResponseEntity.accepted().build();
    }

    /* @PostMapping("/borne/{id}/reserver")
     public void reserverBorne(@PathVariable String id) {
         borneService.reserverBorne(id);
     }*/
//    @PostMapping("/{id}/reserver")
//    public ResponseEntity<Map<String, Boolean>> reserverBorne(@PathVariable String id) throws BorneNotFoundException {
//        Map<String, Boolean> response = new HashMap<>();
//        Boolean reservationResponse = borneService.reserverBorne(id);
//        response.put("success", reservationResponse);
//        return ResponseEntity.ok().body(response);
//    }
//    @PostMapping("/{id}/liberer")
//    public ResponseEntity<Map<String, Boolean>> libererBorne(@PathVariable String id) throws BorneNotFoundException {
//        Map<String, Boolean> response = new HashMap<>();
//        Boolean liberationResponse = borneService.libererBorne(id);
//        response.put("success", liberationResponse);
//        return ResponseEntity.ok().body(response);
//    }
    /*@GetMapping("/bornes/{disponibilite}/{puissance}/{mode}/{connecteur}")
    public String rechercheBorne(@PathVariable Disponibilite disponibilite, @PathVariable float puissance, @PathVariable Mode mode, @PathVariable Connecteur connecteur) {
        return borneService.rechercheBorne(disponibilite, puissance, mode , connecteur);
    }*/
    @GetMapping("/recherche")
    public ResponseEntity<List<Borne>> rechercherBorne(

            @RequestParam(required = false) Optional<Float> puissance,
            @RequestParam(required = false) Optional<String> mode,
            @RequestParam(required = false) Optional<String> connecteur) {

        List<Borne> bornes = borneService.rechercheBorne( puissance, mode, connecteur);

        if (!bornes.isEmpty()) {
            return ResponseEntity.ok(bornes);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @GetMapping("/bornes")
    public ResponseEntity<List<Borne>> findAllApprovedBornes() {
        List<Borne> approvedBornes = borneService.findAllApprovedBornes();
        return ResponseEntity.ok(approvedBornes);
    }

//    @GetMapping("/Lcalstations")
//    public ResponseEntity<String> findStation(
//            @RequestParam Disponibilite disponibilite,
//            @RequestParam float puissance,
//            @RequestParam Mode mode,
//            @RequestParam Connecteur connecteur,
//            @RequestParam float autonomieRestante) {
//
//        String message = borneService.find(disponibilite, puissance, mode, connecteur, autonomieRestante);
//
//        if (message.contains("Aucune station de recharge ne correspond à votre recherche")) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
//        } else {
//            return ResponseEntity.ok(message);
//        }
//    }

}










