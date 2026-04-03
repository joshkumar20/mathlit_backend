package com.mathlit.backend.controller;

import com.mathlit.backend.filter.AdminTokenFilter;
import com.mathlit.backend.model.CompetitiveCategory;
import com.mathlit.backend.model.Question;
import com.mathlit.backend.repository.CompetitiveCategoryRepository;
import com.mathlit.backend.repository.QuestionRepository;
import com.mathlit.backend.service.CompetitiveCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Admin REST API — all routes under /admin/api/**.
 * Protected by AdminTokenFilter (except /admin/api/auth/login).
 * Credentials come from env vars ADMIN_USERNAME / ADMIN_PASSWORD.
 */
@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
public class AdminApiController {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    private final QuestionRepository questionRepo;
    private final CompetitiveCategoryRepository categoryRepo;
    private final CompetitiveCategoryService categoryService;

    // ── Auth ──────────────────────────────────────────────────────────────────

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String user = body.getOrDefault("username", "");
        String pass = body.getOrDefault("password", "");
        if (!adminUsername.equals(user) || !adminPassword.equals(pass)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        String token = AdminTokenFilter.generateToken();
        return ResponseEntity.ok(Map.of(
                "token", token,
                "expiresIn", "24h"
        ));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            AdminTokenFilter.revoke(auth.substring(7));
        }
        return ResponseEntity.ok().build();
    }

    // ── Dashboard stats ───────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        long totalQuestions = questionRepo.count();
        long totalCategories = categoryRepo.count();

        Map<String, Long> perSection = new LinkedHashMap<>();
        for (Object[] row : questionRepo.countBySection()) {
            perSection.put((String) row[0], (Long) row[1]);
        }

        return ResponseEntity.ok(Map.of(
                "totalQuestions", totalQuestions,
                "totalCategories", totalCategories,
                "perSection", perSection
        ));
    }

    // ── Questions — CRUD ──────────────────────────────────────────────────────

    /**
     * GET /admin/api/questions?page=0&size=20&section=&category=&difficulty=&search=
     */
    @GetMapping("/questions")
    public ResponseEntity<Map<String, Object>> listQuestions(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String section,
            @RequestParam(required = false)    String category,
            @RequestParam(required = false)    String difficulty,
            @RequestParam(required = false)    String search) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Question> result = questionRepo.findWithFilters(
                blankToNull(section), blankToNull(category),
                blankToNull(difficulty), blankToNull(search),
                pageable);

        return ResponseEntity.ok(Map.of(
                "questions",    result.getContent(),
                "totalPages",   result.getTotalPages(),
                "totalElements", result.getTotalElements(),
                "currentPage",  page
        ));
    }

    /** POST /admin/api/questions — add a single question */
    @PostMapping("/questions")
    public ResponseEntity<Question> addQuestion(@RequestBody Question question) {
        question.setId(null); // ensure insert, not update
        return ResponseEntity.ok(questionRepo.save(question));
    }

    /** PUT /admin/api/questions/{id} — update a question */
    @PutMapping("/questions/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long id,
                                                   @RequestBody Question question) {
        if (!questionRepo.existsById(id)) return ResponseEntity.notFound().build();
        question.setId(id);
        return ResponseEntity.ok(questionRepo.save(question));
    }

    /** DELETE /admin/api/questions/{id} */
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ── Bulk CSV upload ───────────────────────────────────────────────────────

    /**
     * POST /admin/api/questions/bulk  (multipart/form-data, field name: file)
     *
     * CSV format (first row = header, ignored):
     * section,category,difficulty,questionNo,questionText,
     * optionA,optionB,optionC,optionD,correctIndex,
     * solution,questionImageUrl,solutionImageUrl,tag,examSource
     *
     * correctIndex: 0=A 1=B 2=C 3=D
     */
    @PostMapping("/questions/bulk")
    public ResponseEntity<Map<String, Object>> bulkUpload(
            @RequestParam("file") MultipartFile file) {

        List<Question> saved  = new ArrayList<>();
        List<String>   errors = new ArrayList<>();
        int rowNum = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; } // skip header

                rowNum++;
                try {
                    List<String> cols = parseCsvLine(line);
                    if (cols.size() < 11) {
                        errors.add("Row " + rowNum + ": not enough columns (need at least 11)");
                        continue;
                    }

                    Question q = new Question();
                    q.setSection(cols.get(0).toUpperCase().trim());
                    q.setCategory(cols.get(1).toUpperCase().trim());
                    q.setDifficulty(cols.get(2).toUpperCase().trim());
                    q.setQuestionNo(parseIntOrNull(cols.get(3)));
                    q.setQuestionText(cols.get(4));
                    q.setOptionA(cols.get(5));
                    q.setOptionB(cols.get(6));
                    q.setOptionC(cols.get(7));
                    q.setOptionD(cols.get(8));
                    q.setCorrectIndex(Integer.parseInt(cols.get(9).trim()));
                    q.setSolution(cols.get(10));
                    if (cols.size() > 11) q.setQuestionImageUrl(nullIfBlank(cols.get(11)));
                    if (cols.size() > 12) q.setSolutionImageUrl(nullIfBlank(cols.get(12)));
                    if (cols.size() > 13) q.setTag(nullIfBlank(cols.get(13)));
                    if (cols.size() > 14) q.setExamSource(nullIfBlank(cols.get(14)));

                    saved.add(questionRepo.save(q));
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to read file: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "uploaded", saved.size(),
                "errors",   errors,
                "total",    rowNum
        ));
    }

    // ── Competitive categories ────────────────────────────────────────────────

    @GetMapping("/competitive/structure")
    public ResponseEntity<?> getCompetitiveStructure() {
        return ResponseEntity.ok(categoryService.getStructure());
    }

    @PostMapping("/competitive/categories")
    public ResponseEntity<CompetitiveCategory> saveCategory(
            @RequestBody CompetitiveCategory category) {
        if (category.getId() == null) category.setActive(true);
        return ResponseEntity.ok(categoryRepo.save(category));
    }

    @DeleteMapping("/competitive/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private Integer parseIntOrNull(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    /**
     * Minimal CSV line parser — handles double-quoted fields containing commas.
     */
    private List<String> parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); i++; // escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cols.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        cols.add(cur.toString().trim());
        return cols;
    }
}