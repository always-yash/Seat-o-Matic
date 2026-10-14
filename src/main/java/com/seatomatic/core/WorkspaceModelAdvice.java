package com.seatomatic.core;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class WorkspaceModelAdvice {
    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) { return hasRole(authentication, "ROLE_ADMIN"); }
    @ModelAttribute("isFaculty")
    public boolean isFaculty(Authentication authentication) { return hasRole(authentication, "ROLE_FACULTY"); }
    @ModelAttribute("isInvigilator")
    public boolean isInvigilator(Authentication authentication) { return hasRole(authentication, "ROLE_INVIGILATOR"); }
    @ModelAttribute("currentUsername")
    public String currentUsername(Authentication authentication) { return authentication == null ? "" : authentication.getName(); }
    @ModelAttribute("currentRole")
    public String currentRole(Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) return "Administrator";
        if (hasRole(authentication, "ROLE_FACULTY")) return "Faculty";
        if (hasRole(authentication, "ROLE_INVIGILATOR")) return "Invigilator";
        return "Workspace user";
    }
    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
