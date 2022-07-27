package com.example.account.service;

import com.example.account.exception.SumNotExistException;
import com.example.account.model.command.ChangeAccount;
import com.example.account.model.command.CheckAccount;
import com.example.account.model.event.StocksRequestCanceled;
import org.springframework.stereotype.Component;

@Component
public class AccountCommandHandler {

    private final EventSender eventSender;
    private final AccountService accountService;

    public AccountCommandHandler(EventSender eventSender, AccountService accountService) {
        this.eventSender = eventSender;
        this.accountService = accountService;
    }

    public void checkAccount(CheckAccount command) {
        try {
            accountService.createBalanceRequest(command.getRequestId(), command.getPersonId(), command.getSum());
            eventSender.sendRequestConfirmed(command.getRequestId(), command.getSum());
        } catch (SumNotExistException e) {
            e.printStackTrace();
            eventSender.sendRequestRejected(command.getRequestId(), command.getSum());
        }
    }

    public void changeAccount(ChangeAccount command) {
        accountService.executeBalanceRequest(command.getRequestId(), command.getPersonId(), command.getSum());
        eventSender.sendAccountChanged(command.getRequestId(), command.getPersonId(), command.getSum());
    }

    public void cancelBalanceRequest(StocksRequestCanceled event) {
        accountService.cancelBalanceRequest(event.getRequestId());
    }
}
