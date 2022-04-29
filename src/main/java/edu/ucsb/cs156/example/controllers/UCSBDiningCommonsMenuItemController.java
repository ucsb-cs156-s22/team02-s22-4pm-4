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
 
    @ApiOperation(value = "Get a single menu item")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenuItem getById(
            @ApiParam("id, e.g. '1") @RequestParam Long id) {
        UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        return ucsbDiningCommonsMenuItem;
    }

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

    @ApiOperation(value = "Delete a UCSBDiningCommonsMenuItem")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBDiningCommonMenutem(
            @ApiParam("id, e.g. '1'") @RequestParam Long id) {
        UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        ucsbDiningCommonsMenuItemRepository.delete(ucsbDiningCommonsMenuItem);
        return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single menu item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenuItem updateUCSBDiningCommonMenuItem(
            @ApiParam("id, e.g. '1'") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

                UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        ucsbDiningCommonsMenuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
        ucsbDiningCommonsMenuItem.setName(incoming.getName());
        ucsbDiningCommonsMenuItem.setStation(incoming.getStation());

        ucsbDiningCommonsMenuItemRepository.save(ucsbDiningCommonsMenuItem);

        return ucsbDiningCommonsMenuItem;
    }
}
