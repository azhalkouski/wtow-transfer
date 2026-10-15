package com.example.wtow_transfer.controller;

import com.example.wtow_transfer.dto.request.TransferDto;
import com.example.wtow_transfer.service.TransferEngineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    private final TransferEngineService transferEngineService;

    public TransferController(TransferEngineService transferEngineService) {
        this.transferEngineService = transferEngineService;
    }

    @PostMapping
    public ResponseEntity<?> transferFunds(@Valid @RequestBody TransferDto transferDto) {
        UUID transactionId = transferEngineService.makeTransfer(transferDto);
        // transactionId will be persisted when immutable log is implemented
        // for now it's just a placeholder
        // Also, consider async processing and thus 202 Accepted
        // like this ResponseEntity.accepted().body(Map.of("transactionId", transactionId));
        return ResponseEntity.ok().body(Map.of("transactionId", transactionId));
    }
}
