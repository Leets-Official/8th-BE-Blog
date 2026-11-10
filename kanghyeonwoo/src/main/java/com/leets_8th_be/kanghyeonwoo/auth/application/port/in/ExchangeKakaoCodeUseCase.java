package com.leets_8th_be.kanghyeonwoo.auth.application.port.in;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Exchange;

public interface ExchangeKakaoCodeUseCase {
    Exchange exchange(String code, String browser);
}
