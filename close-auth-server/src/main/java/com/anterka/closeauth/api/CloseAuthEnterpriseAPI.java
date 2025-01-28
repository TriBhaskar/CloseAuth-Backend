package com.anterka.closeauth.api;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(APIPaths.API_PREFIX)
@RequiredArgsConstructor
public class CloseAuthEnterpriseAPI {

    private static final Logger log = LoggerFactory.getLogger(CloseAuthEnterpriseAPI.class);


}
