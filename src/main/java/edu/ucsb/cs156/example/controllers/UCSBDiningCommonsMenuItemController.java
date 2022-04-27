package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(description = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

    @Autowired
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;
 
    @ApiOperation(value = "List all UCSB dining commons menu item")
    @PreAuthorize("hasRole('ROLE_USER')") 
    @GetMapping("/all")
    public Iterable<UCSBDiningCommonsMenuItem> allMenuItems() {
        Iterable<UCSBDiningCommonsMenuItem> menuItems = ucsbDiningCommonsMenuItemRepository.findAll();
        return menuItems;
    }

    // @ApiOperation(value = "Get a single menu item")
    // @PreAuthorize("hasRole('ROLE_USER')")
    // @GetMapping("")
    // public UCSBDiningCommonsMenuItem getById(
    //         @ApiParam("code") @RequestParam String code) {
    //     UCSBDiningCommonsMenuItem menuItems = ucsbDiningCommonsMenuItemRepository.findById(code)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, code));

    //     return menuItems;
    // }

    @ApiOperation(value = "Create a new dining commons menu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenuItem postCommons(
        @ApiParam("Dining Commons Code, e.g. 'ortega'") @RequestParam String diningCommonsCode,
        @ApiParam("Menu Item Name, e.g. 'Chicken Caesar Salad'") @RequestParam String name,
        @ApiParam("Station Name, e.g. 'Entrees'") @RequestParam String station
        )
        {

        UCSBDiningCommonsMenuItem menuItem = new UCSBDiningCommonsMenuItem();
        menuItem.setDiningCommonsCode(diningCommonsCode);
        menuItem.setName(name);
        menuItem.setStation(station);

        UCSBDiningCommonsMenuItem savedMenuItem = ucsbDiningCommonsMenuItemRepository.save(menuItem);

        return savedMenuItem;
    }

    // @ApiOperation(value = "Delete a UCSBDiningCommonsMenuItem")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @DeleteMapping("")
    // public Object deleteCommons(
    //         @ApiParam("code") @RequestParam String code) {
    //     UCSBDiningCommonsMenuItem item = ucsbDiningCommonsMenuItemRepository.findById(code)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, code));

    //     ucsbDiningCommonsMenuItemRepository.delete(item);
    //     return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(code));
    // }

    // @ApiOperation(value = "Update a single commons")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @PutMapping("")
    // public UCSBDiningCommonsMenuItem updateCommons(
    //         @ApiParam("code") @RequestParam String code,
    //         @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    //     UCSBDiningCommonsMenuItem item = ucsbDiningCommonsMenuItemRepository.findById(code)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, code));


    //     item.setDiningCommonsCode(incoming.getDiningCommonsCode());  
    //     item.setName(incoming.getName());
    //     item.setStation(incoming.getStation());

    //     ucsbDiningCommonsMenuItemRepository.save(item);

    //     return item;
    // }
}
