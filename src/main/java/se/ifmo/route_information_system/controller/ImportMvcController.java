package se.ifmo.route_information_system.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import se.ifmo.route_information_system.dto.RouteImportDto;
import se.ifmo.route_information_system.model.ImportOperation;
import se.ifmo.route_information_system.model.ImportStatus;
import se.ifmo.route_information_system.model.User;
import se.ifmo.route_information_system.repository.ImportRepository;
import se.ifmo.route_information_system.service.FileStorageService;
import se.ifmo.route_information_system.service.ImportService;
import se.ifmo.route_information_system.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Controller
@RequestMapping("/imports")
@RequiredArgsConstructor
public class ImportMvcController {

    private final ImportRepository importRepo;
    private final UserService users;

    private final FileStorageService storage;
    private final ImportService importService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String list(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        boolean isAdmin = isAdmin();
        Page<ImportOperation> ops = isAdmin
                ? importRepo.findAllByOrderByIdDesc(pageable)
                : importRepo.findByStartedByOrderByIdDesc(
                        users.getCurrentUser().orElseThrow(), pageable);

        model.addAttribute("ops", ops);
        model.addAttribute("isAdmin", isAdmin);
        return "imports";
    }

    @PostMapping("/upload")
    public String uploadAndImport(@RequestParam("file") MultipartFile file,
            RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Файл пустой");
            return "redirect:/imports";
        }

        User currentUser = users.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String objectKey = storage.upload(file);

        ImportOperation op = new ImportOperation();
        op.setStatus(ImportStatus.RUNNING);
        op.setStartedBy(currentUser);
        op.setStartedAt(Instant.now());
        op.setSourceFilename(file.getOriginalFilename());
        op.setSourceObjectKey(objectKey);
        op.setSourceContentType(file.getContentType());
        op.setSourceSize(file.getSize());
        op = importRepo.save(op);

        try (InputStream is = file.getInputStream()) {
            // JSON → List<RouteImportDto>
            List<RouteImportDto> items = objectMapper.readValue(
                    is,
                    new TypeReference<List<RouteImportDto>>() {
                    });

            importService.importRoutesForOperation(op, items);

            ra.addFlashAttribute("message",
                    "Импорт из файла «" + file.getOriginalFilename() + "» завершён");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Ошибка импорта: " + e.getMessage());
        }

        return "redirect:/imports";
    }

    @GetMapping("/{id}/file")
    public void downloadFile(@PathVariable Long id,
            HttpServletResponse response) throws IOException {
        ImportOperation op = importRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isAdmin = isAdmin();
        User currentUser = users.getCurrentUser().orElse(null);

        if (!isAdmin && (currentUser == null || !currentUser.getId().equals(op.getStartedBy().getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        if (op.getSourceObjectKey() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл для этого импорта не сохранён");
        }

        String filename = op.getSourceFilename() != null ? op.getSourceFilename() : "import.json";
        String contentType = op.getSourceContentType() != null ? op.getSourceContentType() : "application/json";

        response.setContentType(contentType);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + filename + "\"");

        try (InputStream in = storage.download(op.getSourceObjectKey())) {
            in.transferTo(response.getOutputStream());
        }
    }

    private boolean isAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream()
                .anyMatch(ga -> "ROLE_ADMIN".equals(ga.getAuthority()));
    }
}
