package com.themoa.policysearch.policy.dev;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("local")
public class DevConsolePageController {
    @GetMapping("/dev-console")
    public String devConsole() {
        return "dev-console";
    }
}
