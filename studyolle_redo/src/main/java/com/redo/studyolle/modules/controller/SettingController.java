package com.redo.studyolle.modules.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redo.studyolle.modules.domain.entity.Account;
import com.redo.studyolle.modules.domain.entity.Tag;
import com.redo.studyolle.modules.domain.entity.Zone;
import com.redo.studyolle.modules.domain.form.*;
import com.redo.studyolle.modules.domain.validator.NicknameValidator;
import com.redo.studyolle.modules.domain.validator.PasswordFormValidator;
import com.redo.studyolle.modules.repository.TagRepository;
import com.redo.studyolle.modules.repository.ZoneRepository;
import com.redo.studyolle.modules.service.AccountService;
import com.redo.studyolle.modules.service.TagService;
import com.redo.studyolle.security.CurrentAccount;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/settings")
public class SettingController {
    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final ObjectMapper objectMapper;

    private final TagService tagService;
    private final TagRepository tagRepository;

    private final ZoneRepository zoneRepository;

    /**
     * PasswordFormValidator
     * @param webDataBinder
     */
    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    /**
     * NicknameValidator
     * @param webDataBinder
     */
    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    /**
     * ???????????? ?????? ??????
     * @param account
     * @param model
     * @return
     */
    @GetMapping("/password")
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return "/settings/password";
    }

    /**
     * ???????????? ??????
     * @param account
     * @param passwordForm
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping("/password")
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/password";
        }

        /* ???????????? ?????? */
        accountService.updatePassword(account, passwordForm.getNewPassword());

        attributes.addFlashAttribute("message", "??????????????? ??????????????????.");

        return "redirect:/settings/password";
    }

    /**
     * ?????? ?????? ?????? ??????
     * @param account
     * @param model
     * @return
     */
    @GetMapping("/account")
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return "settings/account";
    }

    /**
     * ?????? ?????? ??????
     * @param account
     * @param nicknameForm
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping("/account")
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/account";
        }

        /* ????????? ???????????? */
        accountService.updateNickname(account, nicknameForm.getNickname());

        attributes.addFlashAttribute("message", "???????????? ??????????????????.");

        return "redirect:/settings/account";
    }

    /**
     * ????????? ?????? ??????
     * @param account
     * @param model
     * @return
     */
    @GetMapping("/profile")
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, ProfileForm.class));

        return "settings/profile";
    }

    /**
     * ????????? ??????
     * @param account
     * @param profileForm
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping("/profile")
    public String updateProfile(@CurrentAccount Account account, @Valid ProfileForm profileForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        /* error check */
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "settings/profile";
        }

        /* ????????? ?????? */
        accountService.updateProfile(account, profileForm);

        attributes.addFlashAttribute("message", "???????????? ??????????????????.");

        return "redirect:/settings/profile";
    }

    /**
     * ?????? ?????? ??????
     * @param account
     * @param model
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/tags")
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        /* tag ??? title String List */
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        /* selectBox ???????????? ?????? allTags */
        List<String> allTags = tagRepository.findAll().stream()
                                        .map(Tag::getTitle)
                                        .collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "settings/tags";
    }

    /**
     * ?????? ??????
     * @param account
     * @param tagForm
     * @return
     */
    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        /* ?????? ?????? ?????? ??? ?????? */
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());

        /* ????????? ?????? ?????? */
        accountService.addTag(account, tag);

        return ResponseEntity.ok().build();
    }

    /**
     * ?????? ??????
     * @param account
     * @param tagForm
     * @return
     */
    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        /* ?????? ?????? */
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);

        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        /* ?????? ?????? */
        accountService.removeTag(account, tag);

        return ResponseEntity.ok().build();
    }

    /**
     * ???????????? ?????? ??????
     * @param account
     * @param model
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/zones")
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        /* ????????? ???????????? ?????? */
        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        /* ?????? ?????? ????????? ?????? */
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return "settings/zones";
    }

    /**
     * ?????? ??????
     * @param account
     * @param zoneForm
     * @return
     */
    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        /* ???????????? ?????? ?????? */
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        /* ?????? ?????? */
        accountService.addZone(account, zone);

        return ResponseEntity.ok().build();
    }

    /**
     * ?????? ??????
     * @param account
     * @param zoneForm
     * @return
     */
    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        /* ???????????? ?????? ?????? */
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        /* ?????? ?????? */
        accountService.removeZone(account, zone);

        return ResponseEntity.ok().build();
    }
}
