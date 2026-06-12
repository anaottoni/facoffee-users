package facoffe.users.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/teste")
public class TesteController {
    @Autowired
    
    @GetMapping("/path")
    @PreAuthorize("hasRole('MANAGER')")
    public String rotaProtegida(){
        return "Acesso permitido! ";
    }
}
