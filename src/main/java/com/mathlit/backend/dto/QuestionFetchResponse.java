package com.mathlit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionFetchResponse {

    private String section;
    private String category;
    private int totalAvailable;  // total questions in DB for this category
    private int returned;        // how many returned in this response
    private List<QuestionDto> questions;
}