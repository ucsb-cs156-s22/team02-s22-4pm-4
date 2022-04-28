package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
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


@Api(description = "UCSBOrganization")
@RequestMapping("/api/ucsborganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

    @Autowired
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @ApiOperation(value = "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganization> allOrganizations() {
        Iterable<UCSBOrganization> orgs = ucsbOrganizationRepository.findAll();
        return orgs;
    }

    @ApiOperation(value = "Get a single org")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganization getById(
            @ApiParam("orgCode") @RequestParam String orgCode) {
        UCSBOrganization orgs = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        return orgs;
    }

    @ApiOperation(value = "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganization postOrganization(
        @ApiParam("orgCode") @RequestParam String orgCode,
        @ApiParam("orgTranslationShort") @RequestParam String orgTranslationShort,
        @ApiParam("orgTranslation") @RequestParam String orgTranslation,
        @ApiParam("inactive") @RequestParam boolean inactive
        )
        {

        UCSBOrganization orgs = new UCSBOrganization();
        orgs.setOrgCode(orgCode);
        orgs.setOrgTranslationShort(orgTranslationShort);
        orgs.setOrgTranslation(orgTranslation);
        orgs.setInactive(inactive);

        UCSBOrganization savedOrgs = ucsbOrganizationRepository.save(orgs);

        return savedOrgs;
    }

    // @ApiOperation(value = "Delete a UCSBDiningCommons")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @DeleteMapping("")
    // public Object deleteCommons(
    //         @ApiParam("code") @RequestParam String code) {
    //     UCSBDiningCommons commons = ucsbDiningCommonsRepository.findById(code)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommons.class, code));

    //     ucsbDiningCommonsRepository.delete(commons);
    //     return genericMessage("UCSBDiningCommons with id %s deleted".formatted(code));
    // }

    @ApiOperation(value = "Update a single org")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganization updateOrg(
            @ApiParam("orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrganization incoming) {

        UCSBOrganization org = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));


        org.setOrgCode(incoming.getOrgCode());  
        org.setOrgTranslation(incoming.getOrgTranslation());
        org.setOrgTranslationShort(incoming.getOrgTranslationShort());
        org.setInactive(incoming.getInactive());

        ucsbOrganizationRepository.save(org);

        return org;
    }
}
