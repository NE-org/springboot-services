package com.example.demo.v1.dtos.structured;

import com.example.demo.v1.enumerations.ETransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankingDTO {
    private UUID customer;
    private UUID receiptCustomer;
    private String account;
    private String receiptAccount;
    private Double amount;
    @Schema(example = "TRANSFER")
    private ETransactionType type;
}
