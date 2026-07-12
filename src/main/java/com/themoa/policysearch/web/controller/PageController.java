package com.themoa.policysearch.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/policies/{policyId}")
    public String policyDetail(@PathVariable Integer policyId) {
        return "policy-detail";
    }

    @GetMapping("/bookmarks")
    public String bookmarks() {
        return "bookmarks";
    }

    @GetMapping("/calendar")
    public String calendar() {
        return "calendar";
    }
}
