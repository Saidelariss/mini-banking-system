package com.services.banking.apis;


import com.services.banking.dtos.base.TransferSearchFilter;
import com.services.banking.dtos.request.TransferRequest;
import com.services.banking.dtos.response.TransferResponse;
import com.services.banking.services.TransferService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/transfers")
public class TransferApi {
    private final TransferService transferService;

    @PostMapping
    TransferResponse doTransfer(@RequestBody @Valid TransferRequest request) {
        return transferService.doTransfer(request);
    }

    @GetMapping
    List<TransferResponse> getTransfers() {
        return transferService.getAllTransfers();
    }

    @GetMapping("/search")
    List<TransferResponse> getTransfersByCriteria(TransferSearchFilter transferSearchFilter) {
        return transferService.getAllTransfers(transferSearchFilter);
    }
}
